# Bitácora de trabajo

Registro cronológico de lo que se hizo, por qué y cómo se verificó. Lo nuevo va arriba. El plan está en [`PLAN-ACCION.md`](PLAN-ACCION.md) y las fallas encontradas en [`RECORRIDO.md`](RECORRIDO.md).

## 2026-10-19 (lunes)

### Producción en codeaapp.site con túnel de Cloudflare
- **Decisión #7 aprobada** (Oscar): HTTPS público con un túnel de Cloudflare. Dominio comprado: `codeaapp.site`.
- `guarderia-api/docker-compose.prod.yml`: servicio `cloudflared` (sin puertos abiertos), `restart: unless-stopped`, `.env.prod` propio y uvicorn con `--proxy-headers` confiando solo en la red interna `172.30.0.0/24` (la IP real llega en `X-Forwarded-For`; uvicorn la lee de derecha a izquierda, así que un valor inventado por el cliente no cuela). Se levanta con `-p codea-prod` para no reutilizar los volúmenes de desarrollo.
- Nombres: `api.codeaapp.site` → `api:8000` y `archivos.codeaapp.site` → `storage:9000` (las URLs firmadas de fotos se firman con ese host).
- `guarderia-api/.env.prod.example` (ignorado como `.env.prod` en git y en la imagen) y la sección «Producción en codeaapp.site» del README de la API.
- App: release apunta a `https://api.codeaapp.site/api/v1/` y los enlaces de invitación a `api.codeaapp.site` por omisión.
- **Levantado y verificado (19/oct, en la Mac de Oscar):** túnel `codea-prod` *Healthy* con las rutas `api` y `archivos`; migraciones en `a07c3d8e6f76`; bucket creado. `GET https://api.codeaapp.site/api/v1/health/ready` → `{"status":"ok","database":"ok"}`; cabeceras `Strict-Transport-Security`, `X-Content-Type-Options`, `X-Frame-Options` y `Cache-Control: no-store`; certificado de Let's Encrypt emitido por Cloudflare (tardó ~8 min); `archivos.codeaapp.site` responde 403 sin firma (S3 vivo); `assetlinks.json` 200; un login fallido queda con la IP real del cliente en el registro de uvicorn.
- El túnel no conectaba por QUIC (UDP bloqueado en la red): se fijó `--protocol http2`.
- Base de producción vacía: todavía no hay ningún usuario.
- Sigue abierta la #9: el almacén de producción es el mismo SeaweedFS del compose.

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

### Admin: fijar la ubicación del centro desde el teléfono
- Centro tiene la tarjeta «Ubicación del checador» (muestra latitud, longitud y radio reales de `GET /center`) y el botón «Usar mi ubicación actual», que pide confirmación («las docentes solo podrán registrar su entrada a menos de N metros de aquí»), pide el permiso de ubicación si falta y manda `PATCH /center`. Sin esto no había forma de poner la estancia donde se hará la demo.
- Backend: `PATCH /center` ahora queda en la bitácora (`CENTER_UPDATED`, con quién, qué campos y los valores nuevos) y 1 prueba nueva en `test_admin_extras.py`.
- Verificado en el teléfono: la tarjeta muestra el valor del servidor (25.65160, −100.28960, radio 60 m, el del seed) y el diálogo de confirmación abre. **No se confirmó el cambio** a propósito: guardaría la ubicación exacta del teléfono en la base compartida de Aiven. Pendiente de decidir con el equipo dónde se hará la demo.
- Valor anterior del centro, por si hay que restaurarlo: latitud 25.651600, longitud −100.289600, radio 60 m.

### RF-12: resumen semanal (API)
- `GET /children/{id}/weekly-summary?week_start=`: de lunes a viernes de la semana de esa fecha (sábado y domingo cuentan para la semana que acaba de terminar). Asistencia, ánimo, comida, siesta promedio, cambios, reportes por tipo, logros, notas de la familia y el detalle de cada día. El mismo alcance por registro que el resto del niño (404 fuera de alcance).
- 6 pruebas nuevas (`test_weekly_summary.py`); la ruta se agregó a la prueba de IDOR y la matriz de roles se regeneró (76 endpoints). Total: **267 pruebas de la API en verde**.
- Falta mostrarlo en la app del padre.

### Salida de la docente (hallazgo de la sesión de documentos)
- **Hallazgo:** la app decía «la salida se registra sola al cerrar el día» y no existía código de salida; y el aviso de permiso decía que la ubicación «no se guarda», cuando `staff_check_in` guarda latitud y longitud. Los dos textos eran falsos.
- API: `POST /staff-attendance/check-out` (docente; 409 `NOT_CHECKED_IN` sin entrada, 422 `OUT_OF_RANGE` fuera del radio, repetida devuelve la misma sin pisar la hora) y `checked_out_at` en `GET /today`. Migración `d63e9f4a1c32` (cuatro columnas en `staff_check_in`), aplicada también en Aiven. 5 pruebas nuevas. **274 pruebas de la API en verde.**
- App: «Tu jornada de hoy» con el botón «Registrar mi salida» (misma regla de ubicación), tarjeta del Menú con «Entrada 13:59 · Salida 14:01», y los textos corregidos. El selector de pruebas ya sale también con la entrada registrada; el mapa dice «Fuera del perímetro» cuando corresponde.
- Probado en el teléfono: entrada con retardo (13:59, el servidor la validó con las coordenadas del simulador), salida a las 14:01, y con la ubicación real (842 m) la app bloquea la salida.
- Quedan sin resolver (anotados por la otra sesión): la fecha de nacimiento se inventa a partir de la edad (`nacimientoParaEdad`), no hay política de retención ni respaldos, y no hay comando para crear solo al primer admin.

### RF-10 probado de punta a punta en el teléfono
- Con la docente ya con entrada, se subió una foto de prueba (por la API, al bucket local), se creó un reporte con foto y la mamá pidió borrarla. En la app del admin: «1 por revisar» → pantalla con la foto real, el niño, quién la pidió y el motivo → «Borrar la foto» → confirmación → «Foto borrada»; la lista se vació en 1–2 s. Por la API: solicitud `APPROVED` sin `photo_url`, galería de la familia vacía y la bitácora registra al admin.
- Datos de demostración que quedaron en Aiven: la entrada y salida de Karla Robles del 18/oct, el reporte «Prueba: Diego pintó un sol…» (ya sin foto) y la solicitud de borrado aprobada.

### Inscripción con la fecha de nacimiento real (hallazgo de la sesión de documentos)
- **Hallazgo:** el formulario pedía la **edad** y `nacimientoParaEdad` inventaba una fecha de nacimiento; en la base quedaba una fecha que no era la real.
- Ahora el formulario pide la **fecha de nacimiento (dd/mm/aaaa)**. `InfanteValidator.parseNacimiento` (dominio puro) valida que exista (incluye 29 de febrero), que no sea futura y que el niño tenga de 0 a 12 años, y la manda a la API en `aaaa-mm-dd`. Se eliminó la función que la inventaba.
- 8 pruebas JVM nuevas (`InfanteValidatorTest`). **51 pruebas JVM de la app, 0 fallos.**
- Aclaración: los niños que ya estaban en Aiven conservan la fecha que se les inventó al inscribirlos con la versión anterior; hay que corregirlas a mano si se quiere el dato real.

### RF-12: el resumen semanal en la app del padre
- Calendario → tarjeta «Resumen de la semana»: rango («12 al 16 de octubre»), cinco días con ✓ / ✗ / ·, días asistidos y faltados, ánimo, comidas, siesta promedio, cambios, notas de la familia y los logros que escribió la asistente. Los textos se arman en el dominio (`ResumenSemanaTextos`) con 5 pruebas JVM (**56 pruebas JVM, 0 fallos**).
- Probado en el teléfono con la cuenta de la mamá de Mateo: 2 días asistidos; ánimo, comida y siesta salen con «—» porque la base de demostración casi no tiene esos datos.
- La etiqueta «Documentos» de la barra inferior se achicó a 10 sp porque a 11 sp se cortaba la última letra.

### Operación sin conexión: asistencia de Mi sala (RF-11 y RNF07, parcial)
- **Qué hace:** si no hay conexión, la marca de asistencia se pinta igual, queda en una cola local (Room) y WorkManager la manda sola cuando vuelve la red (reintento con espera creciente de 10 s). La última Mi sala que se vio queda guardada para abrirla sin conexión, con las marcas pendientes ya aplicadas. Un banner dice «N cambios sin enviar».
- **Cómo:** `domain/OfflineAttendance.kt` (lógica pura con interfaces `PendingStore` y `AttendanceRemote`), `data/offline/` (Room: `AppDatabase`, WorkManager: `Offline.kt`, clasificador `OfflineErrors`), cableado en `CodeaViewModel`. Dependencias nuevas: Room 2.8.4, WorkManager 2.11.2 y KSP 2.3.12 (las tres dentro del marco; el esquema de la base se exporta a `app/schemas/`).
- **Reglas que importan:** de varias marcas del mismo niño y día solo se manda la última; una marca hecha con conexión borra las viejas de la cola para que no la pisen después; un error permanente (404, 422) se avisa y no se encola; 401 cuenta como temporal porque al arrancar en segundo plano la sesión puede no haber cargado; la cola espera a que haya sesión y no se pierde si nadie ha entrado.
- **Pruebas:** 13 JVM nuevas (**69 pruebas JVM, 0 fallos**) y una mutación comprobada (quitar `removeFor` hace fallar su prueba).
- **Probado en el teléfono (con la API apagada con `docker compose stop api`):** se marcó falta a Diego → se pintó, salió «Sin conexión: la marca se enviará sola…» y el banner «1 cambio sin enviar»; se cerró la app por completo y se abrió sin servidor → Mi sala salió de la caché con la falta aplicada y el aviso «Sin conexión: se muestra lo último que se guardó»; se encendió la API **sin tocar la app** → en menos de un minuto el servidor tenía a Diego ausente y el banner desapareció. Después se dejó a Diego otra vez como presente.
- **Privacidad de la copia local:** la base `codea.db` del teléfono **no está cifrada** (los tokens de sesión sí, con el Keystore). La caché de Mi sala guarda **nombres de niños**, así que se borra sola cuando la sesión queda sin usuario (cerrar sesión o sesión terminada, en `AppContainer`). La cola de marcas solo guarda ids numéricos, estado y fecha; **no** se borra al cerrar sesión para no perder marcas sin enviar. Pendiente si hay tiempo: cifrar la base. No se verificó el borrado en el teléfono (la lógica es corta; sin `sqlite3` en el dispositivo no se pudo mirar la tabla).
- **Límites (honestos):** solo la asistencia funciona sin conexión; reportes, fotos, chat, check-in, documentos y avisos siguen necesitando red. No hay pruebas de la base Room en JVM (hace falta un dispositivo); se comprobó a mano.
- Correcciones a REQUISITOS.md tras una revisión independiente de la otra sesión: el avance real era 18 ✅ + 6 🟡 + 8 ❌ = **66 %** (yo había escrito 69 %, estaba mal); RF-04, RF-05 y RF-09 bajaron a 🟡 por partes sin probar; RF-11 y RNF07 subieron a 🟡; ahora el avance se recalcula con un script al editar la tabla.

### Cierre del 18 de octubre: resumen del día, cola por persona y migración
- **RF-04 cerrado (hallazgo de la sesión de documentos):** la docente no tenía dónde capturar el resumen del día. Nueva pantalla **Mi sala → «Resumen del día»** (`ResumenDelDiaScreen`, `ResumenDelDiaViewModel`, dominio `ResumenDelDia.kt` con 5 pruebas): ánimo, comida, pañales, siesta, nota, y muestra la nota que dejó la familia; solo manda lo que se llenó. **Probado de punta a punta en el teléfono:** la docente guardó el resumen de Diego y su mamá lo vio en Hoy («Contento», «Casi todo», «2 cambios», «45 minutos», la nota y el reporte del momento).
- **Cola sin conexión ligada a quien la hizo (hallazgo de la sesión de documentos):** antes, si otra docente iniciaba sesión en el mismo teléfono, la cola de la primera se mandaba con la sesión de la segunda. Ahora cada marca guarda su `userId` (base Room v2 con `MIGRATION_1_2`, exportada en `app/schemas/`) y solo se envía, se cuenta y se pinta con la sesión de su dueño. **Probado en el teléfono:** Karla marcó falta sin conexión; cerró sesión; con la API encendida entró otra docente y durante 50 s el servidor siguió con el niño presente y la fila quedó en la cola; al volver Karla se envió sola y la cola quedó vacía. También se verificó leyendo la base del teléfono (`run-as` + `sqlite3` desde la PC) que la migración dejó versión 2 con la columna `userId` y que la caché de niños queda en 0 filas al cerrar sesión.
- **RF-05:** 2 pruebas nuevas de la API (un reporte para varios niños; no se puede incluir a un niño de otra sala). La pantalla de selección sigue sin recorrerse en el teléfono, por eso RF-05 queda 🟡.
- **RNF06:** el texto decía que la app no usa trabajo en segundo plano; ya usa WorkManager solo para la cola. Corregido.
- **Totales al cierre:** API **276 pruebas en verde**; app JVM **74 pruebas en verde**. Avance de requisitos recalculado con script desde la tabla (ver encabezado de [`REQUISITOS.md`](REQUISITOS.md)): 16 ✅ + 10 🟡 + 6 ❌ = ~66 %.
- **Quedó sin hacer:** probar la captura con la cámara, recorrer la pantalla de selección de niños en el teléfono, selector de varios hijos (HN4), avisos con imágenes (HN5), hora de entrada y salida del niño (RF-03), cifrar la base local, salida automática de la docente, mapa (clave de Google), despliegue con HTTPS y APK firmado.

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

## 2026-10-18 · Checador sin ubicación y con cierre automático

- Decisión del equipo: el checador ya no usa GPS, radio ni mapa. Registrar la entrada es un toque, la salida otro, y lo que quede abierto se cierra solo a las 9 pm.
- API: `check-in` y `check-out` no piden coordenadas; se quitan `OUT_OF_RANGE` y `CENTER_LOCATION_MISSING`. `close_stale` cierra las entradas sin salida a las 21:00 hora del centro (corre al inicio de `today`, `check-in` y `check-out`). Migración `e74f0a5b3d43`: quita `latitude`, `longitude`, `distance_m` y las tres columnas `out_*` de `staff_check_in` y agrega `auto_closed`. **No está aplicada en Aiven** (borra las coordenadas ya guardadas). 281 pruebas de la API en verde.
- App: `CheckerScreen` sin mapa ni distancia; se quitan `DeviceLocation`, `GeoMath`, `CheckInRepository`, los permisos de ubicación, Ajustes → Checador y Google Maps (`maps-compose`, `MAPS_API_KEY`). La decisión #3 queda resuelta.
- Documentos corregidos: `REQUISITOS.md` (CRF-05), `DECISIONES-PENDIENTES.md` (#3 resuelta), `MARCO-TECNICO.md`, `ESTADO.md`, `RECORRIDO.md`, `api/endpoints.md` y, en `docs/entrega/`, aviso de privacidad (2.4), clasificación de datos (3.5), ética y legal (ETL-05, ETL-06 y el dilema de ubicación), manual de usuario (2.1 y 3), manual de instalación, matriz de trazabilidad, administración y plan de implantación.
- Pendiente: aplicar la migración `e74f0a5b3d43` en Aiven (necesita aprobación) y probar el checador nuevo en el teléfono.

## 2026-10-18 · Pestañas del administrador: Familias y Ajustes

- **Familias** reemplaza a Infantes y Padres: una lista con selector «Infantes» / «Padres y tutores». El detalle y el inscribir de cada uno cuelgan del mismo grafo de navegación (`AdminFamiliasGraph`).
- **Ajustes** reemplaza a Rutinas: un menú con **Rutinas** (la lista de siempre, ahora con título y botón de volver) y **Mi cuenta**. Mi cuenta ya no se abre desde el avatar de Centro. La ubicación del checador salió de Centro y, con el checador sin ubicación, desapareció.
- Barra inferior del admin: **Centro · Docentes · Familias · Ajustes**. Subido a `main` en `26515d4`.

## 2026-10-18 · Se quita «Avisar a coordinación» del checador

- El botón había pasado a la pantalla de retardo al quitar el caso «fuera del perímetro»; se quitó de la app, junto con `onNotifyCoordination`, `NotifyBody` y la llamada a la API. Los textos «coordinación lo verá» y «Coordinación recibe el aviso de retardo» se cambiaron porque nadie leía esos avisos.
- Sigue en el backend `POST /staff-attendance/notify-coordination` y la tabla `coordination_notice`, sin quien los use.

## 2026-10-18 · Horario del centro configurable

- Hasta ahora la hora de entrada salía del turno de cada docente (`shift`, seed Matutino 8:00 a 16:00 y Vespertino 12:00 a 20:00), la salida solo se mostraba en el Menú, el cierre automático era la constante 21:00 y no había días de trabajo. Nada se podía cambiar desde la app.
- API: `daycare_center` trae `entry_time`, `exit_time`, `auto_close_time` y `work_days` (migración `f85a1b6c4e54`, con valores por omisión 8:00, 16:00, 21:00 y lunes a viernes). `GET` y `PATCH /center` los aceptan, con `422 SCHEDULE_INVALID` y `WORKDAYS_INVALID`, y el cambio queda en la auditoría. El retardo se mide contra la entrada del centro, el cierre automático usa `auto_close_time` y la entrada en un día no laboral responde `409 NOT_WORKING_DAY`. 289 pruebas de la API en verde.
- App: Ajustes → **Horario** (tres horas con selector y siete chips de días, con las mismas validaciones que la API). El checador de la docente toma la entrada y la salida del centro y, en un día no laboral, dice «Hoy no es día de trabajo».
- Los turnos (`shift`) se quedan en la base y en el alta de la docente, pero ya no afectan el retardo. Falta decidir si se quitan del alta.
- Ojo: por omisión el centro **no trabaja sábado ni domingo**, así que hasta que el admin los marque, el checador no deja registrar entrada esos días.
- Pendiente: aplicar `e74f0a5b3d43` y `f85a1b6c4e54` en Aiven (necesita aprobación) y probar la pantalla en el teléfono.

## 2026-10-18 · «¡Entrada registrada!» ya no se cierra sola

- La pantalla de confirmación de la entrada abría Mi día por sí sola a los 2 segundos. Se quitó el temporizador y el texto «La app te lleva sola en 2 segundos»: ahora se queda hasta que la docente toca «Ir a Mi día».


## 2026-10-18 · Rediseño de Mi sala y pase de lista en Mi día

Propuesta y decisiones en [`plan/06-mi-sala-propuesta.md`](plan/06-mi-sala-propuesta.md).

- **Pase de lista (Mi día):** nueva tarjeta «Pase de lista» arriba de la rutina y pantalla `RollCallScreen` (toda la sala, un toque por niño, «Marcar a todos presentes», «Listo»). Cada toque se guarda al instante con la cola sin conexión que ya existía.
- **Sin registro = ausente (API):** no hay tercer estado. `GET /classrooms/{id}/children`, el día del niño, los reportes (`409 CHILD_ABSENT` para quien no está presente), el calendario del papá y el resumen semanal usan la misma regla (`attendance/queries.py::present_child_ids`). Un día con rutina y sin pase de lista cuenta como falta en el calendario y la semana. **Ojo:** los días ya registrados sin pase de lista (datos de pruebas) pasan de «día completo» a «falta».
- **Mi sala (app):** sin cuadros de números ni botones Presente/Falta. «De casa hoy» (avisos de los papás con su texto, **Responder** y **Ya lo vi**), filtros, lista de niños con etiquetas, ausentes plegados. `RoomChild` de la API ganó `notes[]` y `has_summary_today`.
- **Ficha del niño:** alergias, edad, avisos, resumen, **Corregir asistencia** (diálogo), «Familia» y «Quién lo recoge» con botón **Llamar** (abre el marcador, sin permisos). **Avisos de casa:** bandeja con todos los del día.
- **Ya lo vi:** solo en el teléfono (`SeenNotesStore`, por usuario y solo ids). Room pasa a la versión 3 (`MIGRATION_2_3`: `notesJson` y `hasSummaryToday` en `cached_child`).
- **Verificado:** 292 pruebas de la API en verde (tres nuevas) y 92 pruebas JVM de la app en verde (`RoomRulesTest`, `ChildProfileRulesTest` y `RoomChildMappingTest` son nuevas). En el Pixel 8 Pro, con la API local y Aiven: login, Mi día con la tarjeta «Pase de lista» («1 de 2 presentes»), el pase de lista, Mi sala (aviso de casa con su texto, filtros, renglón con etiquetas, ausentes plegado), la ficha (edad, aviso, resumen, asistencia, familia con **Llamar**, autorizados) y el diálogo de corregir asistencia. Se corrigió el contraste del botón «Responder» y de las opciones del diálogo.
- **Aiven:** se aplicaron `e74f0a5b3d43` (borra las columnas de ubicación del checador, aprobado por el usuario) y `f85a1b6c4e54` (horario del centro); sin ellas el login daba 500. Aiven queda en `f85a1b6c4e54`. Además, el contenedor de la API había perdido el DNS (no resolvía Aiven) y se reinició con `docker restart guarderia-api-api-1`.
- **Sin probar en el teléfono:** «Ya lo vi» (se dejó el aviso de prueba sin tocar), «Marcar a todos presentes», «Avisos de casa» (solo aparece con más de tres avisos), el recuadro de alergias (Diego no tiene), la caché sin conexión con los campos nuevos, y todo en modo oscuro.
- **Resumen guiado (después):** botón «Capturar resúmenes (N)» en Mi sala, solo si hay niños presentes sin resumen. Abre el resumen del primero («Resumen 1 de N») y al guardar pasa solo al siguiente; «Saltar este niño» o «Terminar sin guardar» avanzan sin guardar. La cola se arma al tocar el botón (`RoomRules.pendingSummaries`) y viaja en la ruta (`ResumenDelDiaRoute.pending` y `total`). Probado en el teléfono con un niño en la cola (se marcó presente a Renata y se regresó a falta); sin probar con varios niños seguidos.
- **Para correr la app en un teléfono físico** hay que compilar con `-PapiBaseUrl=http://localhost:8000/api/v1/` (o ponerlo en `local.properties`) y activar `adb reverse tcp:8000 tcp:8000` y `tcp:9000`; sin eso la build apunta a `10.0.2.2` (emulador).
## 2026-10-18 · Limpieza de la app (candidatos 1 a 5)

- **1. Cinco pantallas sin uso:** `checadorScreen.kt`, `miDiaScreen.kt`, `miDiaEnCursoScreen.kt`, `confirmarMomentoScreen.kt` y `ajustarRutinaScreen.kt` (1,010 líneas). Ninguna se abría desde la app; solo existían sus `@Preview`.
- **2. Siete repositorios de ejemplo:** `ChildRepository`, `DocenteRepository`, `InfanteRepository`, `PadreRepository`, `RoutineRepository`, `SalaRepository` y `TeacherRepository` (263 líneas). Se borraron también las nueve vistas previas que los usaban; `RoutineRulesTest` trae ahora su propio dato de ejemplo.
- **3. «Mis documentos»:** se quita del Menú de la docente, junto con `ComingSoonScreen`, `MenuOptionRoute` y sus textos.
- **4. Turno en el alta de la docente:** se quita el campo y la llamada `GET /shifts` de la app; la app ya no manda `shift_id` (la API lo sigue aceptando como opcional).
- **5. «Abrir chat» del administrador:** se quita el botón del detalle del padre, que llevaba a una pantalla que no terminaba de cargar.
- Quedan para decidir: el 6 (arrastrar para ordenar rutinas, librería `reorderable`) y el 7 (avisos a coordinación en el servidor).
- Compila y las 77 pruebas JVM pasan.

## 2026-10-18 · Mi hijo, personas autorizadas del admin y pase de salida

- **Pedido del equipo:** el tab «Documentos» del padre pasa a ser «Mi hijo» con datos del niño y salud editable; las personas autorizadas para recoger las administra el admin; la docente cierra el día con un pase de salida.
- **API:** `PATCH /children/{id}/health` (padre o admin; alergias, condiciones médicas, intolerancias, medicamentos y otras indicaciones, hasta 1000 caracteres; la auditoría guarda qué campos cambiaron, no su contenido). `authorized-pickups` POST y DELETE pasan a solo admin, con auditoría. Módulo nuevo `departures`: `GET /classrooms/{id}/departures`, `POST` y `DELETE /children/{id}/departure`. Migración `a07c3d8e6f76` (cuatro columnas en `child` y la tabla `child_departure`). **No está aplicada en Aiven.** 308 pruebas de la API en verde (las pruebas de personas autorizadas se ajustaron al nuevo permiso).
- **App padre:** `MyChildScreen` reemplaza a `ParentDocumentsScreen`; se borran `AddAuthorizedDialog` y `ParentDocumentsSampleData`. Los documentos siguen dentro de «Mi hijo».
- **App admin:** Ajustes → Personas autorizadas (lista de niños y detalle por niño con agregar y quitar).
- **App docente:** tarjeta «Pase de salida» en Mi día al terminar todos los momentos; `DeparturePassScreen` con «Registrar salida», «Corregir» y «Finalizar día»; `DayFinishedScreen` cuando el turno terminó. La ficha del niño muestra lo que escribió la familia.
- **Decisiones mías, por confirmar:** los tutores salen siempre como opción en el pase (si no, un niño sin personas autorizadas bloquearía «Finalizar día»); solo salen los niños con pase de lista; la hora la pone el servidor; el pase necesita conexión.
- **Verificado:** compila, 106 pruebas JVM en verde (14 nuevas) y el APK de debug se arma. **No se probó en el teléfono.**
- **Pendiente:** aplicar `a07c3d8e6f76` en Aiven, después de `a96b2c7d5f65` de main (necesita aprobación), probar los tres flujos en el teléfono, y que la familia vea la hora de salida de su hijo.
- **Ajustes posteriores (misma noche):** el pase de salida pasó a ser el último paso de la rutina en Mi día (fila gris con candado y, al terminar, tarjeta violeta «SIGUIENTE PASO» a la que la pantalla baja sola) y, con la rutina terminada, ya no queda ningún momento seleccionado con el botón de reporte. Centro del admin ya no muestra «Fotos que piden borrar»; su pantalla y endpoints quedan sin forma de llegar a ellos. Visto en el emulador; sin registrar salidas de punta a punta.
- **«Mi hijo» sin documentos:** se quitó la subida de documentos de la pestaña (`RequestedDocument`, el selector de archivos y las llamadas del padre). La API de documentos (`GET` y `PUT /children/{id}/documents`) sigue en el backend sin pantalla; el CRF-02 y los manuales de `docs/entrega/` que mencionen documentos del padre hay que revisarlos.
- **Chats de la docente:** la bandeja y el encabezado de la conversación muestran al niño y a sus tutores (`GET /conversations` ahora trae `child.guardians[]`; `ChatRules.guardiansLabel`). 309 pruebas de la API y 109 JVM en verde. Sin verlo en pantalla. Con el seed, solo Mateo Iglesias y Valentina Ruiz tienen tutor registrado; en los demás chats la línea no aparece.

## 2026-10-18 · Limpieza visual, propuesta 1: un solo encabezado

- Había cuatro encabezados distintos: título pequeño en las pestañas del admin; sobretítulo y título grande en las de la docente y el padre; flecha sin círculo en Horario y Rutinas; y nada (sin botón de volver) en Detalle de rutina, Nueva rutina, Catálogo, Orden y Agregar momento.
- Ahora las pestañas del admin (Centro, Docentes, Familias, Ajustes) y la bandeja de Chats usan `ScreenHeader`; las ocho pantallas internas del admin que dibujaban su flecha a mano, Horario, la lista de Rutinas y las cinco de rutinas usan `BackHeader`. El detalle de sala, docente, infante y padre ganó el espaciado de las demás.
- Sin cambios de función. Compila y pasan las 77 pruebas JVM; se revisó en el emulador con el admin y la docente.
- Siguen las propuestas 2 a 7 de la presentación de limpieza visual (color, botones, filas, vacíos, modo oscuro y detalles).


## 2026-10-18 · Invitación por enlace de WhatsApp (rama `invitacion-whatsapp`)

Sin correos ni SMTP: el admin manda la invitación por WhatsApp y el padre solo crea su contraseña. Quien no está dado de alta no tiene código y no puede crear cuenta. La prueba de identidad es que solo esa persona recibió el enlace (la misma seguridad del código de acceso, que ya tiene límite de intentos y auditoría).

- **Admin (app):** el diálogo del código ahora tiene **«Enviar por WhatsApp»**: abre el chat con el teléfono de la persona (10 dígitos = México, `52`) y el mensaje escrito (enlace y, por si no abre la app, el correo y el código). Sin teléfono válido o sin WhatsApp cae al selector de compartir. `Invite.whatsAppUrl` y `Invite.whatsAppPhone` (Kotlin puro, con pruebas).
- **Padre (app):** `Invite.parseLink` lee `https://<servidor>/invitar?email=&code=` y `codea://invitar?…`; `MainActivity` lo recibe (también con la app abierta), `CodeaApp` salta el login y deja «Activar mi cuenta» con correo y código puestos. Con la sesión abierta lo descarta y avisa. El manifiesto tiene el filtro https (`INVITE_HOST`, con `autoVerify`) y el de `codea://`.
- **API:** `access_code.link` (con `PUBLIC_BASE_URL`), página `GET /invitar` (solo lee, nunca gasta el código; escapa y valida lo que refleja; CSP propia) y `GET /.well-known/assetlinks.json` (`ANDROID_CERT_SHA256`). Ambas son públicas y están en la matriz de acceso. Sin migración.
- **Verificado:** 299 pruebas de la API (siete nuevas en `tests/test_invitations.py`) y 102 JVM de la app, en verde; la app compila e instala.
- **Sin probar en el teléfono** (se bloqueó la pantalla): el enlace con la sesión abierta, el prefill de «Activar mi cuenta» (se puede probar con `adb shell am start -a android.intent.action.VIEW -d "codea://invitar?email=…&code=…" mx.tec.codea` con la sesión cerrada), el botón «Enviar por WhatsApp» y el flujo completo.
- **Falta para que el enlace https abra la app:** una URL pública con HTTPS que llegue a la API (`PUBLIC_BASE_URL`; para la demostración basta un túnel), la huella del certificado (`ANDROID_CERT_SHA256`) y el mismo dominio en la app (`-PinviteHost=` o `INVITE_HOST` en `local.properties`). Sin eso el mensaje solo lleva el código escrito y funciona igual a mano.

## 2026-10-18 · Reconocimiento con el código y documentos al día

- **Qué se hizo:** se leyó el código de `main` (después del PR #9) contra cada documento, sin teléfono. `pytest` de la API: **318 pruebas en verde**. Las pruebas JVM se contaron con `@Test` (121); no se corrieron. La matriz de roles generada del código (83 rutas) coincide con `docs/api/matriz-de-roles.md`.
- **Hallazgos en el código:** `ParentViewModel` sigue con el primer hijo (HN4); el padre conserva «Pedir que se borre» y `SolicitudesBorradoScreen` no tiene forma de abrirse (RF-10 baja a 🟡); `GET /audit-log` no tiene pantalla; `announcement` no tiene `media_id` (HN5) y `message` sí (fotos del chat); los turnos y `notify-coordination` siguen en el backend sin uso; el reporte con foto sigue con `TakePicturePreview` (miniatura) y solo el chat usa Photo Picker y cámara completa; la familia no puede ver la salida de su hijo.
- **Documentos corregidos:** `ESTADO.md` (reescrito), `FALTANTES.md`, `REQUISITOS.md` (15 ✅ + 11 🟡 + 6 ❌, ~64 %), `PLAN-ACCION.md`, `ENTREGA-3.md` (la lista de la rúbrica decía ❌ para documentos que ya existen), `RECORRIDO.md` (lista de lo que falta recorrer), `plan/README.md` y los planes 01, 02, 03 y 05 (marcados como históricos), `guarderia-api/PENDIENTES.md` (reescrito), `api/endpoints.md`, `GUIA-CONTINUAR.md`, `DECISIONES-PENDIENTES.md` y los READMEs con conteos viejos.
- **Documentos de entrega:** `manual-usuario.md` (pase de lista, Mi sala nueva, pase de salida, «Mi hijo», chat con fotos, invitación por WhatsApp, personas autorizadas del admin; ya no dice que sin registro el niño cuenta como presente), `aviso-privacidad.md` y `clasificacion-datos.md` (datos de salud que escribe la familia, `child_departure`, foto del chat, caché local versión 3), `matriz-trazabilidad.md`, `administracion.md` (D5, D16, D17 y la nueva D18), `manual-instalacion.md`, `plan-implantacion.md` y `etica-legal.md` (encabezados y conteos).
- **Hueco real que se encontró:** no existe el **documento de arquitectura** (diagrama de capas y relaciones) que pide la rúbrica.
- **Sin cambios de código.** Sin commit: los cambios están en el árbol de trabajo.

## 2026-10-18 · Selector de hijo, salida visible para la familia, borrado directo de fotos y Aiven migrado

- **Aiven:** se aplicaron `a96b2c7d5f65` (foto en el chat) y `a07c3d8e6f76` (salud del niño y pase de salida) con permiso del usuario. Aiven queda en `a07c3d8e6f76`, la última.
- **Selector de hijo (HN4, app):** `ParentViewModel` guarda la lista de hijos y el elegido; `ChildChoice.pick` conserva al elegido cuando todo se recarga. La tira `ChildSwitcher` sale arriba de Hoy, Calendario, Fotos y Mi hijo solo con más de un hijo. 4 pruebas JVM nuevas (`ChildChoiceTest`). Sin verlo en un teléfono; el seed trae un hijo por padre.
- **Salida visible (RF-03, API y app):** `GET /children/{id}/day` agrega al final de la línea de tiempo un evento `DEPARTURE` («Salida», «Lo recogió …») si la docente registró la salida; desaparece si la corrige. 1 prueba nueva.
- **Borrado directo de fotos (RF-10, API y app):** `DELETE /media/{id}` (solo padre; 204). Borra del bucket y de las galerías, deja los reportes sin foto, resuelve la solicitud pendiente y queda en la bitácora (`MEDIA_DELETED_BY_FAMILY`). **Regla que apliqué, por confirmar con el equipo:** solo si todos los niños de la foto son hijos de quien borra; si no, `409 MEDIA_SHARED` y nadie lo puede resolver desde la app. En la app el botón «Pedir que se borre» pasó a «Borrar» con confirmación. 3 pruebas nuevas.
- **Verificado:** `pytest` 324 en verde; `./gradlew :app:testDebugUnitTest` 125 pruebas, 0 fallas. La matriz de roles se regeneró (84 rutas). **Sin probar en un teléfono** (no había uno conectado).

## 2026-10-19 · Hora de entrada del niño (RF-03) y pendiente de borrado de fotos

- **Hora de entrada (API):** columna `attendance.arrived_at` (migración `b18e4d7a2c93`, **aplicada en Aiven** con permiso del usuario; Aiven queda en esa revisión). `PUT /attendance/{id}` la guarda al marcar presente: la del servidor, o la que manda la app (`arrived_at` con zona horaria) si la marca se hizo sin conexión; una hora en el futuro se ignora. Repetir `PRESENT` conserva la primera hora y `ABSENT` la borra. Sale en `AttendanceOut`, en `GET /classrooms/{id}/children` (`arrived_at`) y como primer evento `ARRIVAL` («Entrada») de `GET /children/{id}/day`. Las marcas anteriores quedan sin hora.
- **Hora de entrada (app):** el pase de lista muestra «Entró a las 8:05» bajo el nombre; la cola sin conexión manda la hora en que se hizo cada marca (`PendingAttendance.createdAt`); la caché de Mi sala sube a la versión 4 de Room (`MIGRATION_3_4`, esquema `4.json`); en Hoy del padre el evento «Entrada» sale con el mismo punto verde que la llegada y la salida.
- **Verificado:** `pytest` 332 en verde (8 pruebas nuevas en `test_arrival_time.py` y dos aserciones ajustadas); `./gradlew :app:testDebugUnitTest` 129 pruebas, 0 fallas; `assembleDebug` compila. Se comprobó con la API real contra Aiven que Mi sala devuelve `arrived_at`. **Sin probar en un teléfono.**
- **Pendiente anotado a petición del usuario:** que el padre borre las fotos de sus hijos de una vez, sin aprobación de la docente ni del admin (ver `guarderia-api/PENDIENTES.md` y `FALTANTES.md`). Ya funciona para fotos solo de sus hijos; falta el caso de fotos con niños de otras familias.
- **Sin commit:** los cambios están en el árbol de trabajo.

## 2026-10-19 · Avisos con imagen (HN5)

- **API:** columna `announcement.media_id` (migración `c63f9e5b3d04`, **aplicada en Aiven**; queda en esa revisión). `POST /announcements` acepta `{text?, media_id?, classroom_id?}`: texto, imagen o ambos (sin ninguno, `422 TEXT_REQUIRED`). La imagen debe ser del admin, estar lista y ser `image/*` (`422 MEDIA_NOT_VALID`; `404` si es de otra persona). `AnnouncementOut.media_url` es una URL firmada de 10 minutos y es `null` si el aviso no trae imagen o ya se borró. La imagen no tiene niños etiquetados, así que no aparece en la galería de ninguna familia.
- **App:** Centro → «Agregar imagen» (selector de fotos del sistema) con vista previa, «Cambiar imagen» y «Quitar»; se comprime con `ImageCompressor` y se sube como `PHOTO`. El historial del admin, la lista de avisos de la docente y la campana de la familia muestran la imagen (`AvisoImagen`, con Coil). Un aviso solo de imagen no deja un texto vacío.
- **Verificado:** `pytest` 338 en verde (6 pruebas nuevas en `test_announcement_images.py`); `./gradlew :app:testDebugUnitTest :app:assembleDebug` sin fallas. **Sin probar en un teléfono.**
- **Sin commit:** los cambios siguen en el árbol de trabajo.

## 2026-10-19 · El padre borra las fotos de una vez (RF-10) y se quita la revisión del admin

- **Decisión (usuario):** el padre borra las fotos de sus hijos «de una», sin aprobación de la docente ni del admin, y basta con que salga un hijo suyo; si salen niños de otras familias, **se borra también para ellas** (propuesta mía, aprobada por el usuario).
- **API:** `DELETE /media/{id}` ya no responde `409 MEDIA_SHARED`. Se quitaron `POST /media/{id}/deletion-request`, `GET /media/deletion-requests` y `…/approve` / `…/reject` (de 84 a **80 endpoints**; matriz de roles regenerada). La bitácora `MEDIA_DELETED_BY_FAMILY` guarda los niños que salían cuando la foto era compartida. La tabla `media_deletion_request` **se conserva sin uso** (no se hizo una migración que borre datos); decidir si se elimina.
- **App:** se borraron `SolicitudesBorradoScreen`, `SolicitudesBorradoViewModel`, `SolicitudBorrado`, `AdminSolicitudesBorradoRoute`, el repositorio, los DTOs y los endpoints de Retrofit de la revisión. El botón «Borrar» del padre no cambió.
- **Pruebas:** `test_media_deletion.py` se reescribió (5 pruebas: borra directo, foto con niños de otras familias, solo el padre del niño, bucket caído, las rutas de solicitud ya no existen); se ajustaron `test_media_documents.py` y `test_access_matrix.py`.
- **Documentos:** `REQUISITOS` (RF-10 sigue 🟡 hasta verlo en un teléfono), `FALTANTES`, `ESTADO`, `PLAN-ACCION`, `PENDIENTES`, `endpoints.md`, README de `media`, `etica-legal` (Dilema 1 actualizado: ETL-27 baja a 🟡, ETL-28 se reescribe), `aviso-privacidad`, `clasificacion-datos`, `manual-usuario` y la matriz de trazabilidad.
- **Sin commit:** los cambios siguen en el árbol de trabajo.
- **Verificado:** `pytest` 323 en verde (bajó de 338 porque se quitaron las 9 pruebas de la revisión del admin y se reescribieron las de borrado); `./gradlew :app:testDebugUnitTest :app:assembleDebug` sin fallas. Aiven no necesita migración (no cambió el esquema). **Sin probar en un teléfono.**

## 2026-10-19 · Menú del día (HN3)

- **API:** módulo nuevo `menus` (tabla `daily_menu`, migración `d74a0f6c4e15`, **aplicada en Aiven**; queda en esa revisión). `GET /menus?start&end` (cualquier rol; sin fechas, el de hoy), `PUT /menus/{fecha}` y `DELETE /menus/{fecha}` (solo admin). Desayuno, comida y colación en texto libre (500 caracteres cada una, al menos una con texto). `GET /children/{id}/day` trae `menu`. Cambios y bajas en la bitácora (`MENU_SAVED`, `MENU_DELETED`). De 80 a **83 endpoints**; matriz de roles regenerada.
- **App:** Ajustes → «Menú del día» (semana de lunes a viernes con botones de semana anterior y siguiente; cada día se agrega, edita o quita en un cuadro); tarjeta «Menú del día» en Hoy de la familia (`MenuCard`, con los colores del tema para claro y oscuro) y «Menú de hoy» al inicio de Mi día de la docente. Si no hay menú, no aparece nada. Las cuentas de fechas viven en `MenuRules` (sin `java.time`, por el minSdk 24).
- **Decisiones que tomé** (avisadas al usuario antes de empezar, sin objeción): un menú por centro (no por sala), texto libre y no un catálogo de platillos, de lunes a viernes.
- **Verificado:** `pytest` **336** en verde (8 pruebas nuevas en `test_daily_menu.py`); `./gradlew :app:testDebugUnitTest` **135** pruebas, 0 fallas (6 nuevas en `MenuRulesTest`); `assembleDebug` compila. Se comprobó con la API real contra Aiven que `GET /menus` responde 200. **Sin probar en un teléfono.**
- **Requisitos:** HN3 pasa de ❌ a 🟡 (pasa a ✅ al verlo en un teléfono): ahora son 15 ✅ + 12 🟡 + 5 ❌, ~66 %. Con esto quedan hechos los cuatro puntos de «funciones que aún podemos meter antes del congelamiento»: hora de entrada, avisos con imagen, borrado directo de fotos y menú del día.
- **Sin commit:** los cambios siguen en el árbol de trabajo.

## 2026-10-19 · Túnel con dominio (dado por hecho)

- El usuario avisó que **otro compañero ya está haciendo el túnel de Cloudflare con el dominio comprado** y pidió darlo por hecho. La decisión #7 queda aprobada. No se configuró nada en este repositorio ni se verificó: falta poner el dominio real en `PUBLIC_BASE_URL`, `S3_PUBLIC_ENDPOINT_URL` y `ANDROID_CERT_SHA256` (API) y en `API_BASE_URL_RELEASE` e `INVITE_HOST` (app). Documentos tocados: `DECISIONES-PENDIENTES`, `PENDIENTES`, `FALTANTES`, `ESTADO`, `plan-implantacion` y `administracion`.

## 2026-10-19 (noche) · La base de producción será Aiven

- **Decisión del usuario:** producción usa Aiven en lugar del Postgres local del servidor de Oscar. Cloudflare no cambia nada (el túnel solo enruta `api` y `storage`; la base se elige con `DATABASE_URL` en `.env.prod`).
- **Verificado:** las 13 migraciones levantan una base vacía completa hasta `d74a0f6c4e15` (se probó en una base temporal del Postgres local y se borró). La base de producción actual de Oscar estaba vacía, así que no hay datos que mover.
- **Documentado** en `guarderia-api/README.md` («La base de producción: Aiven») y en `plan-implantacion.md`. La base debe ser **aparte** de la de pruebas manuales y de la de pruebas automáticas.
- **Pendiente de Oscar:** crear la base en Aiven, poner `DATABASE_URL` en su `.env.prod` y correr `alembic upgrade head`. No se tocó su servidor ni su `.env.prod`.

## 2026-10-19 (noche) · Recorrido en el teléfono y correcciones

- **Recorrido** (detalle en [`RECORRIDO.md`](RECORRIDO.md)): docente, padre y admin en un Pixel 8 Pro con modo oscuro. Se probaron la hora de entrada, el pase de salida completo con «Finalizar día», el menú del día, los avisos con imagen, «Mi hijo» y el borrado directo de fotos. **RF-03, RF-10, HN3 y HN5 pasan a ✅**: ahora son 19 ✅ + 8 🟡 + 5 ❌, ~72 %.
- **Correcciones salidas del recorrido (fallas 11 a 17):** Fotos con colores del tema y mensaje sin fotos; números del calendario legibles en oscuro y «1 día»; etiqueta de la semana del menú legible; `imePadding()` global para que el teclado no tape los campos; Fotos y Hoy se refrescan al entrar; inicial de la maestra en el visor; «Publicando…» al subir un aviso.
- **Causa de «No se pudo conectar»:** Gradle reutilizó la configuración sin `-PapiBaseUrl` y el APK quedó con `10.0.2.2`. Se compila con `--no-configuration-cache`. El `adb reverse` también se pierde al reinstalar.
- **Ética:** ETL-27 pasa a ✅ (el borrado directo se vio en el teléfono); ETL-28 sigue en 🟡 por el aviso a las otras familias.
- **Verificado:** `./gradlew :app:testDebugUnitTest` sin fallas y la app instalada en el teléfono. **Sin commit.**

## 2026-10-19 (noche) · Admin: Centro pasa a Avisos y las salas a Docentes; sin sobretítulos

- **Pestaña Avisos** (antes «Centro»): solo publicar el aviso y el historial. Ícono de megáfono. Ruta `AdminAvisosGraph`, pantalla `avisosAdminScreen.kt`, ViewModel `AvisosAdminViewModel`. Sigue siendo la primera pestaña del admin.
- **Docentes con selector Docentes | Salas** (como Familias). Salas trae la ocupación que estaba en Centro y «+ Crear sala»; el detalle de sala y crear sala ahora cuelgan de `AdminDocentesGraph`, así que la barra se queda en «Docentes».
- **Por la checada de Paola que no aparecía:** la lista de docentes solo se recargaba con cambios hechos desde el mismo teléfono; la checada llega desde el teléfono de la docente. Ahora se vuelve a pedir al abrir la pestaña y se puede jalar hacia abajo para recargar. Quien no ha checado dice «Sin checar» (antes «Checó --:--»). La tarjeta amarilla de resumen se quitó de las dos listas a pedido del usuario.
- **Sin sobretítulos:** a pedido del usuario se quitó la línea gris de arriba («Estancia La Oruga · Administración», «Familias», «Ajustes · Rutinas»…) en las pantallas de los tres perfiles, también en Hoy y Fotos del padre. Se dejó solo donde lleva un dato (reporte, elegir niños, resumen guiado, Mi día y el chat de la docente). Se borraron 10 textos sin uso de `strings.xml`.
- **Verificado:** `assembleDebug` y `./gradlew test` en verde; en el emulador se vieron Avisos y Docentes → Salas. No se probó la lista de docentes, jalar para recargar ni abrir una sala (el emulador se estaba usando a la vez).
