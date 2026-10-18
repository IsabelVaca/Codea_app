# Bitácora de trabajo

Registro cronológico de lo que se hizo, por qué y cómo se verificó. Lo nuevo va arriba. El plan está en [`PLAN-ACCION.md`](PLAN-ACCION.md) y las fallas encontradas en [`RECORRIDO.md`](RECORRIDO.md).

## 2026-10-18 (domingo)

### Integración de ramas
- Rama local `integracion` desde `main`: se mergearon `feature/login-jwt` y `feature/chat` (sin conflictos).
- `feature/pagos` **no** se integró: el equipo decidió que no hay módulo de pagos.
- Verificación: `./gradlew :app:testDebugUnitTest` en verde.

### Entorno de pruebas
- `guarderia-api/.env` actualizado con la base de Aiven; se conservaron `JWT_SECRET` (generado) y `SEED_PASSWORD`. `S3_PUBLIC_ENDPOINT_URL=http://localhost:9000` porque el teléfono es físico y usa `adb reverse`. El `.env` anterior se guardó fuera del repositorio.
- Aiven estaba en la migración `caf6344f99d5`. Se corrió `alembic upgrade head` (`98306224d8b5`, login con JWT; elimina `firebase_uid`) y a los 15 usuarios se les puso la contraseña del seed (`codea1234`). El equipo confirmó que nadie estaba trabajando sobre la base.
- Pruebas de la API: 68 en verde con el Postgres local (`guarderia_test`). El `venv` no tenía `pwdlib`; se reinstaló con `requirements.txt`.
- Teléfono físico (modo oscuro) por USB: `adb reverse tcp:8000` y `tcp:9000`; la app se instala con `./gradlew :app:installDebug -PapiBaseUrl=http://localhost:8000/api/v1/`.

### Correcciones de la app (recorrido en teléfono)
| Falla | Arreglo | Verificado en el teléfono |
|---|---|---|
| La tab Documentos del padre cerraba la app (clave duplicada en `LazyColumn`) | Claves con prefijo `documento-` y `autorizado-` | Sí: abre y muestra documentos y autorizados |
| Login ilegible en modo oscuro | Fondo del tema en `AuthFrame` | Pendiente de ver (hay que cerrar sesión) |
| "Hoy" del padre ilegible en modo oscuro | Título con `onBackground`; tarjeta pastel con texto `Ink` | Sí |
| Mi sala decía "Preescolar 2" para todas | Título con `roomName` de `GET /me` | Pendiente de ver con la docente |
| Calendario decía "Agosto" | Mes tomado de los datos del calendario | Sí: "Octubre en una línea" |
| "Documentos" se partía en dos líneas | 11 sp, una línea y sin salto en la barra con 5 tabs | Sí, al abrir la tab |

### Seguridad de la API y bitácora de auditoría (RF-14)
- **Límite de intentos:** `auth/throttle.py` + tabla `auth_attempt`. 5 fallos por correo y 30 por IP en 15 min en `/auth/login` y `/auth/activate` → `429 TOO_MANY_ATTEMPTS` con `retry_after_s` y `Retry-After`. El bloqueo no revisa la contraseña (tampoco la buena) y cuenta también correos que no existen, para no delatar cuentas. Entrar bien borra los fallos.
- **Cabeceras de seguridad** en todas las respuestas, también en errores: `nosniff`, `X-Frame-Options: DENY`, CSP `default-src 'none'` (salvo Swagger), `Referrer-Policy`, `Permissions-Policy`, `Cache-Control: no-store` en `/api/`, HSTS fuera de `ENV=dev`. CORS cerrado salvo `CORS_ORIGINS`.
- **Auditoría:** módulo `audit` (`audit_log`, `GET /audit-log` solo admin, solo se agrega). Registra logins (ok, fallido, bloqueo), activaciones, cambios de contraseña, códigos de acceso, altas, bajas y cambios de docentes, padres e infantes. Nunca guarda contraseñas, códigos ni tokens (prueba incluida).
- Migración `b41c7d2e9a10` (tablas `audit_log` y `auth_attempt`), aplicada también en Aiven.
- Verificación: 91 pruebas de la API en verde (68 + 23 nuevas en `tests/test_security.py`); cabeceras confirmadas con `curl` contra la API en vivo.
- Pendiente: HTTPS real (despliegue), registrar lectura de fotos/documentos y `PATCH /center`, límite en endpoints que no son de entrada.
- Trabajo delegado: la sesión `codea-app-24` redacta los documentos de la entrega en `docs/entrega/` (ética y legal, clasificación de datos, manuales, implantación, administración). Se revisan antes de hacer commit.

### Pruebas de control de acceso (rúbrica, parte 3)
- `tests/test_access_matrix.py` (151 pruebas, casi todas parametrizadas por ruta): lee de las 72 rutas reales qué rol exige cada una y comprueba que sin token responde `401` y que cada rol no permitido recibe `403`; IDOR por registro (un padre no lee datos de otro niño, una docente solo los de su sala, chats, fotos, notas y personas autorizadas); inyección en login, filtros y texto guardado.
- Las pruebas detectan fallos de verdad: se rompió `can_see_child` a propósito (mutación) y fallaron 5; se restauró.
- Documento nuevo [`api/matriz-de-roles.md`](api/matriz-de-roles.md), generado con `guarderia-api/scripts/roles_matrix.py`.
- Total: **242 pruebas de la API en verde** (68 + 23 + 151).

### Mi sala: niños con nota de la familia
- API: `GET /classrooms/{id}/children` agrega `notes_today` y `has_note_today` (notas `parent_note` de hoy en la zona del centro). 2 pruebas nuevas en `test_teacher_day.py` (marca al niño correcto, no cruza salas, al día siguiente ya no cuenta).
- App: la tarjeta del niño en Mi sala muestra «Nota de la familia hoy».
- Para probarlo se dejó una nota de prueba de la mamá de Diego Hernández en Aiven (`Prueba: hoy llegó con tos…`, 18/oct); es dato de demostración.
- El teléfono se bloqueó solo durante la prueba; se activó `svc power stayon usb` (pantalla encendida mientras esté conectado). Para revertirlo: `adb shell svc power stayon false`.

### RF-10: el admin revisa las solicitudes de borrado de fotos (API)
- `GET /media/deletion-requests`, `POST …/{id}/approve` y `…/reject` (solo admin). Aprobar borra el archivo del bucket (`Storage.delete`), marca la foto `DELETED` (sale de todas las galerías), deja los reportes sin foto pero con su texto y cierra las demás solicitudes de esa foto. Si el bucket falla responde `502` y no marca nada. Una solicitud se resuelve una sola vez (`409`).
- Migración `c52d8e3f0b21` (`decided_by`, `decided_at`, `decision_note`), aplicada también en Aiven. Acciones auditadas: `MEDIA_DELETION_APPROVED/REJECTED`.
- 9 pruebas nuevas (`test_media_deletion.py`). Total: **259 pruebas de la API en verde**. La matriz de roles se regeneró sola con las 3 rutas nuevas (75 endpoints).
- Falta la pantalla del admin en la app (siguiente paso).

### RF-10: pantalla del admin y limpieza de pagos
- App: Centro tiene la tarjeta «Fotos que piden borrar» (con contador de pendientes) que abre `SolicitudesBorradoScreen`: foto con Coil, niños, quién la pidió, motivo, «Borrar la foto» (con confirmación, no se puede deshacer) y «Conservar». ViewModel `SolicitudesBorradoViewModel`, repositorio `SolicitudBorradoApiRepository`, ruta `AdminSolicitudesBorradoRoute`.
- Verificado en el teléfono: la tarjeta y la pantalla abren y piden la lista a la API real (estado vacío). **No se pudo ver una solicitud real**: no hay fotos en Aiven y crear una exige registrar la entrada de la docente dentro del perímetro del centro. El flujo completo lo cubren las 9 pruebas de la API.
- También en el teléfono (tema claro esta vez): «Sala · Maternal» y la etiqueta «Nota de la familia hoy» de Mi sala, ya con datos reales.
- Se quitaron las menciones a pagos de los README y de los documentos (CRF-01 queda como «no cumplido por decisión del equipo»).

### Checador con GPS y hora reales
- Antes: prototipo con datos fijos y `registrar()` mandaba siempre las coordenadas del centro (el servidor nunca rechazaba por distancia).
- Ahora: `data/DeviceLocation.kt` (LocationManager de Android, sin Play Services; GPS y luego red, con tiempo límite), `domain/GeoMath.kt` (distancia haversine, con 4 pruebas en `GeoMathTest`) y `CheckerViewModel` con estados de ubicación (buscando, sin permiso, sin señal, lista). La fecha y la hora son las del teléfono; la entrada se registra con la posición real.
- Permisos nuevos en el manifiesto: `ACCESS_FINE_LOCATION` y `ACCESS_COARSE_LOCATION`. Los chips "Simular llegada" solo aparecen en compilaciones debug (`BuildConfig.DEBUG`).
- Probado en el teléfono: permiso del sistema → "Domingo 18 de octubre", 13:29, "Estás a 842 m", fuera del perímetro, botón bloqueado. Pruebas JVM en verde.
- Pendiente: probar una entrada dentro del perímetro (hay que ajustar la ubicación del centro con `PATCH /center`; ver [`RECORRIDO.md`](RECORRIDO.md)) y el mapa en blanco (falta `MAPS_API_KEY`).

### Documentación
- Se crearon [`PLAN-ACCION.md`](PLAN-ACCION.md), [`RECORRIDO.md`](RECORRIDO.md) y esta bitácora.

### Decisiones de trabajo
- Commits en `integracion`, sin coautor de Claude y sin `push` hasta que el equipo revise.
- Lo que depende de [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md) no se implementa.
