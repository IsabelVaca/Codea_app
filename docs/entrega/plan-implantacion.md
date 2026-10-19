# Plan de implantación

Canal, versión, configuración, despliegue, migración de datos y rollback. Estado a **2026-10-18**, `main` con los PR #7 a #9 y la invitación por WhatsApp (puesto al día el 18/oct por la noche; en esa revisión se corrió `pytest` de la API: 318 pruebas en verde).

> **Léelo así:** hoy **no existe ningún despliegue** del backend ni un APK de release firmado. Este plan dice qué hay en el repositorio, qué falta y qué proponemos. Lo que es una **propuesta** o necesita una decisión del equipo se marca como tal; no se afirma que algo funcione si no se probó. Se sustenta en `guarderia-api/README.md`, `docker-compose.yml`, `Dockerfile`, `.env.example`, `app/build.gradle.kts`, `guarderia-api/migrations/README.md`, `guarderia-api/PENDIENTES.md`, `docs/DECISIONES-PENDIENTES.md` y `docs/ENTREGA-3.md`.

Documentos relacionados: [`manual-instalacion.md`](manual-instalacion.md) (cómo levantar el entorno de desarrollo) y [`clasificacion-datos.md`](clasificacion-datos.md) (qué hay que proteger y respaldar).

---

## 1. Estado de partida

| Pieza | Estado | Evidencia |
|---|---|---|
| API en una URL pública con HTTPS | ❌ No existe | `docs/ENTREGA-3.md`, parte 1: «Desplegarlo en una URL pública» |
| APK de release firmado | ❌ No existe | Sin `signingConfig`, sin keystore, minificación desactivada (`app/build.gradle.kts`) |
| Dirección de la API en release | ⚠️ Inválida a propósito | `https://api.codea.invalid/api/v1/` |
| Imagen de Docker de la API | ✅ | `Dockerfile` (Python 3.12, sin usuario no administrador ni `HEALTHCHECK`) |
| Base de datos de pruebas manuales | ✅ En Aiven | `docs/BITACORA.md`; **con datos de ejemplo**, no de producción |
| Almacén de archivos de producción | ❓ Sin decidir | SeaweedFS solo para desarrollo (`docker/seaweedfs-s3.sh`); decisión #9 abierta |
| Respaldos | ❌ | `guarderia-api/PENDIENTES.md`: «Respaldos de la base — tarea abierta» |
| Monitoreo | 🟡 | Solo `/health/live` y `/health/ready`; Grafana está en la decisión #6, sin aprobar |
| Etiqueta (tag) de versión en git | ❌ | `git tag` no devuelve nada |
| Decisiones que bloquean el despliegue | ⏳ | #6 (monitoreo), #7 (túnel o servidor propio con HTTPS), #9 (almacén compatible con S3) |

---

## 2. Canal de distribución

### 2.1 App Android

| Canal | Cuándo | Estado |
|---|---|---|
| **APK firmado por enlace de descarga** (instalación directa) | Entrega del curso y piloto con el centro | **Propuesta.** La rúbrica pide un «enlace al APK de release» (`ENTREGA-3.md`, punto 12 del PDF). Hay que subir el archivo a un lugar con enlace estable **[completar por el equipo: dónde]** |
| APK `debug` por USB | Desarrollo y la demo en dispositivo físico | ✅ Ya se hace (`./gradlew :app:installDebug`) |
| Tienda de aplicaciones (Google Play) | Después de la entrega | ❌ No hay cuenta ni ficha. Fuera de alcance de esta entrega |

La persona que instale un APK de un enlace debe permitir la instalación de orígenes desconocidos en su teléfono. Hay que decirlo en las instrucciones a las familias **[completar por el equipo]**.

### 2.2 Backend

| Canal | Estado |
|---|---|
| Contenedor Docker en un servidor propio con certificado | Alternativa dentro del marco técnico (decisión #7). **Sin servidor elegido** |
| Túnel de Cloudflare (HTTPS rápido) | **Fuera del marco hasta que se apruebe** (decisión #7). No usar mientras tanto |
| Postgres alojado (Aiven) | En uso para pruebas; para producción **[decidir]** |
| Almacén compatible con S3 | **[decidir]** (decisión #9: SeaweedFS no es el que nombra el marco; sí lo es RustFS) |

---

## 3. Versión

### 3.1 Qué versiones existen hoy

| Componente | Cómo se versiona hoy | Valor actual |
|---|---|---|
| App | `versionCode` y `versionName` en `app/build.gradle.kts` | `1` y `"1.0"` |
| API | Prefijo de ruta `/api/v1`. FastAPI no declara una versión propia en `main.py` | `v1` |
| Base de datos | Revisión de Alembic | `d63e9f4a1c32` (la más reciente con commit; según `docs/BITACORA.md`, Aiven ya está en ella) |
| Base local del teléfono (Room) | `version` en `@Database` (`app/src/main/java/mx/tec/codea/data/offline/AppDatabase.kt`); el esquema de cada versión se exporta a `app/schemas/mx.tec.codea.data.offline.AppDatabase/` (`1.json` y `2.json`) | `2` (con `MIGRATION_1_2`) |
| Código | Commits en `integracion`; sin tags | — |

### 3.2 Reglas propuestas

1. **Un tag por versión entregada**, en la forma `v1.0.0`, sobre el commit exacto del que sale el APK. La rúbrica pide «enlace al repositorio con tag de la versión entregada».
2. **`versionCode` sube siempre** (1, 2, 3…) con cada APK que se distribuya; `versionName` sigue `MAJOR.MINOR.PATCH`. Android no permite instalar encima un APK con `versionCode` menor.
3. **La API es compatible hacia atrás dentro de `/api/v1`**: se pueden agregar campos y endpoints; **quitar o cambiar el significado de uno exige `/api/v2`** y una app nueva. Si una app vieja choca con una API nueva, la API responde errores con `code` estable y, si el token ya no sirve, la app cierra sesión.
4. **Cada cambio al modelo trae su migración** y no se edita una ya aplicada (`migrations/README.md`).
5. La **versión de la app y la de la migración** que exige quedan escritas en las notas de cada entrega **[completar por el equipo]**.

---

## 4. Configuración

### 4.1 Backend: qué cambia entre desarrollo y producción

Variables de `guarderia-api/.env` (detalle en [`manual-instalacion.md`](manual-instalacion.md), sección 2.1).

| Variable | Desarrollo | Producción |
|---|---|---|
| `ENV` | `dev` | **Cualquier otro valor** (por ejemplo `prod`). Con `dev` la API intenta crear el bucket y el seed pone contraseñas conocidas. Fuera de `dev` se activa HSTS |
| `JWT_SECRET` | cualquiera de 32+ caracteres | Generado con `secrets.token_urlsafe(48)`, **distinto del de desarrollo**, guardado fuera del repositorio. No hay rotación todavía |
| `POSTGRES_PASSWORD` / `DATABASE_URL` | `123456` en el ejemplo | Contraseña fuerte y única. Si es Aiven, la URL lleva `?ssl=require` |
| `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_BUCKET` | valores del ejemplo | Llaves propias del proveedor elegido. El bucket **debe existir de antemano**: `_ensure_bucket` solo corre con `ENV=dev` |
| `S3_ENDPOINT_URL` | `http://storage:9000` (lo fija `docker-compose.yml`) | Dirección interna del almacén de producción |
| `S3_PUBLIC_ENDPOINT_URL` | `http://10.0.2.2:9000` o `http://localhost:9000` | Dirección **HTTPS pública** del almacén, la que ve el teléfono |
| `CORS_ORIGINS` | vacío | Vacío (la app Android no usa CORS) |
| `SEED_PASSWORD` | `codea1234` | **No se usa** (el seed no pone contraseñas conocidas fuera de `dev`) |
| `AUTH_MAX_FAILURES`, `AUTH_MAX_FAILURES_IP`, `AUTH_WINDOW_S` | 5 / 30 / 900 | Revisar `AUTH_MAX_FAILURES_IP` si hay un proxy |

Hay tres puntos que **el repositorio no resuelve** y que hay que decidir antes de publicar:

1. **IP real detrás de un proxy.** El límite de intentos y la bitácora usan `request.client.host` (la IP del socket). Detrás de un proxy inverso eso sería la IP del proxy y todos compartirían el mismo contador por IP. Hay que configurar uvicorn para confiar en el proxy o ajustar el código (lo advierte el propio `auth/throttle.py`).
2. **Proxy con HTTPS.** No hay ningún `nginx`, `caddy` ni equivalente en el repositorio. Alguien debe terminar TLS y reenviar a `api:8000`.
3. **`docker-compose.override.yml` no es de producción**: monta el código, activa `--reload` y publica puertos en `127.0.0.1`. En un servidor hay que usar solo `docker-compose.yml` (`docker compose -f docker-compose.yml up -d`) y, con él, la API no publica ningún puerto: **falta definir cómo llega el proxy a ella**. Y el servicio `storage` (SeaweedFS) del archivo base es solo de desarrollo.

### 4.2 App: qué cambia entre debug y release

| Ajuste | Debug | Release |
|---|---|---|
| Dirección de la API | `-PapiBaseUrl`, `API_BASE_URL` o `http://10.0.2.2:8000/api/v1/` | `API_BASE_URL_RELEASE` de `local.properties` (HTTPS) |
| Tráfico HTTP | Permitido solo a `10.0.2.2` y `localhost` | **Prohibido** (`network_security_config.xml`) |
| Firma | Llave de depuración de Android | **Pendiente:** keystore propio y `signingConfig` |
| Minificación | — | **Pendiente:** hoy `optimization.enable = false`; activar y probar que no rompe la serialización ni la navegación |
| Modo sin conexión (asistencia) | WorkManager envía la cola con la restricción de red conectada y reintento con espera creciente; Room guarda la cola y la caché en `codea.db` | Igual |
| Botones «Simular llegada» del checador | Visibles (solo `BuildConfig.DEBUG`) | Ocultos |
| `allowBackup` | `false` | `false` |

El **keystore y sus contraseñas** los crea y guarda el equipo, fuera del repositorio; el asistente de trabajo no debe inventarlos (`PLAN-ACCION.md`). Si se pierde el keystore, **no se puede publicar una actualización que instale sobre la anterior**.

---

## 5. Despliegue

### 5.1 Requisitos previos (go / no-go)

| # | Requisito | Estado |
|---|---|---|
| 1 | Decisiones #6, #7 y #9 resueltas | ⏳ |
| 2 | Servidor o plataforma elegida, con dominio y certificado HTTPS | ❌ |
| 3 | Almacén de archivos de producción creado, con bucket y llaves | ❌ |
| 4 | Postgres de producción (distinto del de pruebas) | ❌ |
| 5 | Respaldo programado de la base y del bucket, **con una restauración probada** | ❌ |
| 6 | Todos los secretos generados y guardados fuera del repo | ❌ |
| 7 | Plan para crear el primer administrador (ver 5.3) | ❌ |
| 8 | Textos corregidos en la app y aviso de privacidad aprobado | 🟡 Los dos textos falsos ya se corrigieron (`f814763`); falta aprobar el [`aviso-privacidad.md`](aviso-privacidad.md) y mostrarlo en la app |
| 9 | Pruebas de la API y de la app en verde sobre el commit a desplegar | ⏳ El equipo debe correrlas |
| 10 | Tag de versión creado | ❌ |

### 5.2 Pasos propuestos para la API

Son una **propuesta** armada con los archivos del repo; ningún paso se ha ejecutado en un servidor.

1. **Preparar** el servidor, el dominio y el proxy HTTPS (requisito 2). El proxy envía a la API y conserva la IP del cliente (ver 4.1).
2. **Crear** el bucket y las llaves en el almacén de producción; anotar `S3_ENDPOINT_URL` y `S3_PUBLIC_ENDPOINT_URL`.
3. **Crear** la base de datos de producción y su usuario.
4. **Respaldar** (aunque esté vacía): confirma que el mecanismo funciona.
5. **Construir y publicar** la imagen desde el commit etiquetado: `docker build -t codea-api:v1.0.0 guarderia-api/` (el nombre es una propuesta).
6. **Escribir** el `.env` de producción con los valores de 4.1. No dejar valores del ejemplo.
7. **Migrar:** `alembic upgrade head` dentro de la imagen, **antes** de recibir tráfico. Debe terminar en la revisión esperada (`alembic current`).
8. **Arrancar** la API con la imagen y `docker-compose.yml` sin el archivo override.
9. **Verificar** (5.4).
10. **Crear al primer administrador** (5.3).
11. **Dar de alta** docentes, infantes y tutores desde la app; cada persona activa su cuenta con su código (**no se mandan correos**: el administrador comparte el código).

### 5.3 El problema del primer administrador

El repositorio **no tiene un comando para crear solo al administrador**. La única forma de tener uno es `python -m app.seed`, que además crea el centro de ejemplo «La Oruga» con coordenadas de Monterrey, 7 docentes, 6 tutores y 11 niños ficticios. Fuera de `ENV=dev` imprime los códigos de invitación en lugar de contraseñas, pero **sí inserta los datos de ejemplo**.

| Opción | Costo |
|---|---|
| Correr el seed y luego dar de baja o eliminar a mano los datos de ejemplo | Quedan filas ficticias en producción y en la bitácora |
| Escribir un script `create_admin` (nuevo) | Pequeño, pero **no existe**; requiere código y prueba |

**[Decisión del equipo.]** Recomendación: la segunda opción antes del despliegue real.

### 5.4 Verificación después de desplegar

| Prueba | Cómo | Esperado |
|---|---|---|
| API viva | `GET https://<dominio>/api/v1/health/live` | `{"status":"ok"}` |
| Base conectada | `GET …/health/ready` | `{"status":"ok","database":"ok"}` |
| Cabeceras | `curl -sI https://<dominio>/api/v1/health/live` | `X-Content-Type-Options`, `X-Frame-Options`, `Cache-Control: no-store` y `Strict-Transport-Security` presentes |
| Login | `POST …/auth/login` con el administrador | `200` con `access_token` y `refresh_token` |
| Rol | `GET …/me` con ese token | Roles y centro |
| Bloqueo | 6 logins fallidos con un correo inventado | El sexto responde `429 TOO_MANY_ATTEMPTS` |
| Archivos | Subir una foto de prueba con el flujo `uploads` → POST al almacén → `complete`, y leerla con la URL firmada desde un teléfono | La foto se ve; vence en 10 minutos |
| Escaneo dinámico | OWASP ZAP contra la URL de pruebas | Hallazgos documentados (pendiente en la rúbrica, parte 2) |
| Rendimiento de fotos | Medir `GET /media` | Menos de 4 s (RNF05; **nadie lo ha medido**) |

### 5.5 Despliegue de la app

1. Poner `API_BASE_URL_RELEASE=https://<dominio>/api/v1/` en `local.properties` (no se versiona).
2. Subir `versionCode` / `versionName` y hacer commit.
3. Crear el tag `vX.Y.Z`.
4. Generar el keystore (la primera vez) y configurar `signingConfig`; activar la minificación y **probar el APK minificado** en un teléfono.
5. `./gradlew :app:assembleRelease`.
6. Instalar el APK en un teléfono limpio, **sin `adb reverse`**, y recorrer los tres roles contra el servidor desplegado.
7. Subir el APK al canal de 2.1 y enlazarlo en el PDF.

---

## 6. Migración de datos

### 6.1 De dónde a dónde

| Origen | Destino | Recomendación |
|---|---|---|
| Base de pruebas (Aiven) con 15 usuarios y datos de ejemplo, contraseña `codea1234` | Producción | **No migrarla.** Son datos ficticios con una contraseña conocida; comenzar con una base vacía y dar de alta a las personas reales |
| Contenedor local de cada integrante | — | Se descarta |
| Producción versión N | Producción versión N+1 | Migraciones de Alembic (6.2) |

### 6.2 Cambios de esquema entre versiones

1. **Respaldo** de la base y de los archivos (si aún no hay respaldo automático, uno manual y verificado).
2. `alembic upgrade head` con la API detenida o en modo de mantenimiento.
3. `alembic current` debe mostrar la revisión esperada.
4. Arrancar la nueva versión y pasar las pruebas de 5.4.

Revisiones con cuidado especial (`migrations/README.md`):

| Revisión | Riesgo |
|---|---|
| `98306224d8b5` (login con JWT) | **Elimina `app_user.firebase_uid`**: el dato no se recupera (el `downgrade` recrea la columna vacía). Después de migrar, **nadie tiene contraseña** hasta que el administrador les genere un código; las invitaciones pendientes se dan por vencidas. Solo aplica a una base creada antes del login propio |
| `d63e9f4a1c32` (salida de la docente) | Agrega cuatro columnas a `staff_check_in`. Su `downgrade` las elimina y **se pierden las salidas ya registradas** (hora y ubicación de salida) |
| `e74f0a5b3d43` (checador sin ubicación) | Quita seis columnas de `staff_check_in` y agrega `auto_closed`. Su `downgrade` las recrea vacías: **las coordenadas y distancias ya guardadas se pierden para siempre** y no se recuperan. Aplicarla en Aiven requiere aprobación |
| `f85a1b6c4e54` (horario del centro) | Agrega cuatro columnas a `daycare_center` con valores por omisión; su `downgrade` las elimina y **se pierde el horario configurado** (vuelve a los valores por omisión al volver a subir). Va después de `e74f0a5b3d43`. Aplicarla en Aiven requiere aprobación |
| `c52d8e3f0b21` (revisión de borrado de fotos) | Agrega `decided_by`, `decided_at` y `decision_note` a `media_deletion_request`. Su `downgrade` las elimina y **se pierden las decisiones ya tomadas**. El valor `DELETED` de `media.status` no necesita cambio de esquema, pero las fotos ya marcadas así seguirían marcadas tras bajar de versión |

### 6.3 Archivos (fotos y documentos)

- Los archivos viven en el bucket, con la llave `media/AAAA/MM/<uuid>/original.<ext>`, y la base apunta a ellos (`media.storage_key`).
- Cambiar de proveedor de almacén exige **copiar todos los objetos con las mismas llaves** y actualizar `S3_*`. Hoy no hay herramienta para eso.
- Las URLs firmadas no se guardan; se generan en cada lectura, así que cambiar el dominio del almacén no rompe datos viejos.

### 6.4 Datos que no se deben llevar

La bitácora de auditoría y los intentos de acceso de pruebas (`audit_log`, `auth_attempt`) no tienen valor en producción y contienen correos e IPs de prueba.

---

### 6.5 Base local del teléfono (`codea.db`)

La app crea en cada teléfono una base Room (`codea.db`, **versión 2**) con tres tablas locales: `pending_attendance`, `cached_room` y `cached_child` (ver [`clasificacion-datos.md`](clasificacion-datos.md), 4.2.1). No se migra "de un servidor a otro": vive en el teléfono.

- **Actualizar la app** conserva la base (misma aplicación y misma firma). Si una versión nueva cambia el esquema, hay que **subir la versión y escribir una `Migration` de Room**. El código **no** usa `fallbackToDestructiveMigration`, así que sin migración la app falla al abrir la base nueva.
- **Qué se pierde si la base se recrea o se desinstala la app:** la caché (se recarga con red) y la **cola de marcas de asistencia sin enviar** (no se recupera). Antes de desinstalar o de forzar una migración destructiva hay que asegurarse de que no haya marcas pendientes (el banner «N cambios sin enviar» lo indica).
- **Ya existe una migración:** `MIGRATION_1_2` agrega la columna `userId` a `pending_attendance` (cada marca queda ligada a su dueña; las marcas de la versión 1 quedan con `userId` vacío). Se verificó a mano en el teléfono leyendo la base (versión 2 con la columna `userId`). **No hay pruebas automáticas** de migración (no hay pruebas de Room en JVM); falta agregarlas antes de la siguiente.
- El esquema exportado en `app/schemas/` **debe entrar al control de versiones** con cada cambio de la base: es la referencia para escribir y probar migraciones.

## 7. Rollback

### 7.1 Principios

- Todo cambio de producción empieza con un **respaldo** y con la versión anterior identificada (imagen y APK).
- **Volver el código atrás es fácil; volver los datos atrás no.** Una vez que la API escribe con el esquema nuevo, regresar la imagen sin regresar la base puede romperla.
- Hoy **no hay respaldos definidos ni una restauración probada**: sin ellos, un rollback con pérdida de datos no es recuperable. Es el primer pendiente de este plan.

### 7.2 Tabla de rollback

| Qué falló | Cómo se revierte | Pérdida | Estado |
|---|---|---|---|
| La API nueva no arranca o falla en las pruebas de 5.4, y **no hubo migración** | Volver a arrancar la imagen anterior | Ninguna | Propuesta; requiere conservar la imagen anterior |
| La API nueva falla **después de migrar** | 1) Detener la API. 2) `alembic downgrade -1` (o a la revisión previa). 3) Arrancar la imagen anterior | Los datos que usaban las columnas o tablas nuevas | **El `downgrade` de cada revisión no está probado** (`migrations/README.md`, «Pendiente»). Solo `98306224d8b5` se probó (sube, baja y sube) y baja con pérdida de `firebase_uid` |
| La migración dañó datos | Restaurar el respaldo hecho antes de migrar | Lo escrito después del respaldo | ❌ No hay respaldo ni restauración probada |
| Un secreto se filtró (`JWT_SECRET`, llaves del almacén) | Generar uno nuevo y reiniciar. Cambiar `JWT_SECRET` invalida **todos** los access tokens; los refresh tokens siguen en la base, así que lo más limpio es también revocarlos (`UPDATE refresh_token SET revoked_at = now()`) | Todas las personas deben volver a iniciar sesión | Propuesta |
| Una persona perdió su teléfono o una cuenta está comprometida | El administrador da de baja a la persona: se corta su acceso en la siguiente petición y se revocan sus refresh tokens | Ninguna | ✅ Existe (`POST /teachers/{id}/deactivate`, `/parents/{id}/deactivate`) |
| Un reporte o una foto se subió por error | La foto se borra con la revisión de solicitudes (el administrador la aprueba y se elimina del almacén); un reporte **no se puede editar ni anular** hoy | — | Parcial |
| El APK nuevo falla en los teléfonos | Android **no permite instalar un APK con `versionCode` menor** encima de otro mayor. Hay dos caminos: desinstalar y reinstalar el APK anterior (se pierde la sesión en el teléfono, no los datos del servidor), o publicar un APK nuevo con el código anterior y un `versionCode` mayor | La sesión local | Propuesta; la segunda opción es la limpia |
| Un APK nuevo cambió la versión de la base local y falla al abrirla | No se puede bajar la versión de una base Room sin migración inversa o recreación. Publicar un APK con `versionCode` mayor que arregle la migración (hoy hay una migración, 1→2; no hay una de 2→1: instalar una versión anterior que espere la versión 1 sobre una base 2 no es posible sin desinstalar). Desinstalar y reinstalar recrea la base y **pierde las marcas de asistencia sin enviar** | Marcas pendientes de ese teléfono | Propuesta; existe la migración 1→2 pero no hay pruebas automáticas de migración |
| El APK nuevo necesita una API que aún no se desplegó (o al revés) | Mantener la compatibilidad dentro de `/api/v1` (3.2). Desplegar **primero la API** y después distribuir el APK | — | Propuesta |

### 7.3 Orden de despliegue y de rollback

1. Despliegue: respaldo → API (con migración) → verificación → APK.
2. Rollback: retirar el APK nuevo del canal → revertir la API (y la base si hace falta, con el respaldo) → verificar con 5.4.

---

## 8. Operación después de implantar

| Tema | Estado hoy |
|---|---|
| Disponibilidad del 99 % (RNF04) | ❌ No hay despliegue ni medición. `/health/live` y `/health/ready` permiten monitorear desde fuera; no hay monitoreo configurado (Grafana está en la decisión #6) |
| Registros (logs) | La API escribe en la salida estándar del contenedor. El aviso a coordinación incluye el nombre de la docente. No hay política de cuánto tiempo se guardan |
| Limpieza periódica | No existe: tokens y códigos vencidos se acumulan; los archivos en estado `PENDING` nunca se limpian |
| Rotación de `JWT_SECRET` | No existe (no hay `kid`) |
| Códigos de acceso por correo | No se mandan correos: el administrador los comparte |
| Quién atiende una falla fuera de horario | **[completar por el equipo]** |

---

## 9. Lo que falta para poder implantar

| # | Pendiente | Responsable |
|---|---|---|
| 1 | Decidir #6, #7 y #9, y elegir servidor, dominio y almacén | **[completar por el equipo]** |
| 2 | Proxy con HTTPS y manejo de la IP real | **[completar por el equipo]** |
| 3 | Respaldos y una restauración probada | **[completar por el equipo]** |
| 4 | Script para crear solo al primer administrador | **[completar por el equipo]** |
| 5 | Keystore, `signingConfig` y minificación probada | **[completar por el equipo]**: el keystore lo crea una persona del equipo |
| 6 | `API_BASE_URL_RELEASE` real | Depende de 1 |
| 7 | Pruebas de `downgrade` de cada migración | **[completar por el equipo]** |
| 8 | Tag de versión y notas de la entrega | **[completar por el equipo]** |
| 9 | Revisar **todos** los textos de la app contra lo que hace el sistema (los dos conocidos ya se corrigieron) | Ver [`etica-legal.md`](etica-legal.md), ETL-03 |
| 10 | Pruebas automáticas de migración de Room y del borrado de la caché al cerrar sesión (hoy verificados a mano) | **[completar por el equipo]** |
