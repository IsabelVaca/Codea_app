# Plan de implantación

Canal, versión, configuración, despliegue, migración de datos y rollback. Estado a **2026-10-20**, `main` con los PR hasta el #17 más el commit de respaldos (`043c2d6`); el equipo reporta 343 pruebas de la API y 140 JVM en verde.

> **Léelo así:** desde el 19/oct el backend está publicado en `https://api.codeaapp.site/api/v1/` (Docker en la Mac de un integrante del equipo, túnel de Cloudflare, base en Aiven y fotos en SeaweedFS) y desde el 20/oct tiene respaldo diario. Hoy es el **servidor compartido de pruebas y demostración**, con datos ficticios. **No existe** un APK de release firmado. Este plan dice qué hay, qué falta y qué proponemos. Lo que es una **propuesta** o necesita una decisión del equipo se marca como tal; no se afirma que algo funcione si no se probó. Se sustenta en `guarderia-api/README.md`, `docker-compose.yml`, `docker-compose.prod.yml`, `Dockerfile`, `.env.example`, `.env.prod.example`, `scripts/respaldo.sh`, `app/build.gradle.kts`, `guarderia-api/migrations/README.md`, `docs/DECISIONES-PENDIENTES.md`, `docs/INCIDENTE-2026-10-19.md` y `docs/ENTREGA-3.md`.

Documentos relacionados: [`manual-instalacion.md`](manual-instalacion.md) (cómo levantar el entorno de desarrollo) y [`clasificacion-datos.md`](clasificacion-datos.md) (qué hay que proteger y respaldar).

---

## 1. Estado de partida

| Pieza | Estado | Evidencia |
|---|---|---|
| API en una URL pública con HTTPS | ✅ Levantada y verificada el 19/oct | `https://api.codeaapp.site` por túnel de Cloudflare (`guarderia-api/docker-compose.prod.yml`, proyecto `codea-prod`); `health/ready`, cabeceras y certificado comprobados con `curl` (`docs/BITACORA.md`). Corre en una Mac que tiene que estar prendida y despierta |
| APK de release firmado | ❌ No existe | Sin `signingConfig`, sin keystore, minificación desactivada (`app/build.gradle.kts`) |
| Dirección de la API en release | ✅ | `https://api.codeaapp.site/api/v1/` (`app/build.gradle.kts`; `API_BASE_URL_RELEASE` la cambia) |
| Imagen de Docker de la API | ✅ | `Dockerfile` (Python 3.12, sin usuario no administrador ni `HEALTHCHECK`) |
| Base de datos del servidor compartido | ✅ En Aiven | Base `codea_prod` (Postgres 18), creada vacía el 19/oct con las 13 migraciones (`d74a0f6c4e15`). Tiene los **datos de prueba** de `seed_demo.py`, no de familias reales |
| Almacén de archivos | 🟡 En uso, sin aprobar | SeaweedFS de `codea-prod` en la misma Mac (`archivos.codeaapp.site`, URLs firmadas); desde el 20/oct arranca con hasta 100 volúmenes de 1 GB (`docker/seaweedfs-s3.sh`). Decisión #9 abierta |
| Respaldos | ✅ Desde el 20/oct | `scripts/respaldo.sh` cada día a las 3:00 con `launchd`: base (`pg_dump` con `postgres:18`) y volumen de fotos, 14 días en `~/respaldos-codea/` de la Mac servidor. Restauración de la base probada en un Postgres 18 temporal (`guarderia-api/README.md`, «Respaldos»). Antes del 20/oct no había respaldo y se perdieron datos (`docs/INCIDENTE-2026-10-19.md`) |
| Monitoreo | 🟡 | Solo `/health/live` y `/health/ready`; Grafana está en la decisión #6, sin aprobar |
| Etiqueta (tag) de versión en git | ❌ | `git tag` no devuelve nada |
| Decisiones abiertas que afectan al despliegue | ⏳ | #6 (monitoreo) y #9 (almacén compatible con S3). La #7 (túnel) se aprobó el 19/oct y la #11 (respaldo con `launchd`) el 20/oct |

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
| Contenedor Docker en un servidor propio con certificado | Alternativa dentro del marco técnico (decisión #7). No se eligió |
| Túnel de Cloudflare (HTTPS) | ✅ **Elegido y en uso** (decisión #7, aprobada el 19/oct): `codeaapp.site`, contenedor `cloudflared` (HTTP/2, porque la red bloquea QUIC) en `docker-compose.prod.yml`, sobre Docker en la Mac de un integrante. Pasos en `guarderia-api/README.md`, «Producción en codeaapp.site» |
| Postgres alojado (Aiven) | ✅ **En uso** (decisión del usuario, 19/oct): base `codea_prod`, aparte de la de las pruebas automáticas |
| Almacén compatible con S3 | SeaweedFS en la misma Mac, **en uso sin aprobación** (decisión #9: SeaweedFS no es el que nombra el marco; sí lo es RustFS). Cambiarlo después implica mover el bucket (6.3) |

---

## 3. Versión

### 3.1 Qué versiones existen hoy

| Componente | Cómo se versiona hoy | Valor actual |
|---|---|---|
| App | `versionCode` y `versionName` en `app/build.gradle.kts` | `1` y `"1.0"` |
| API | Prefijo de ruta `/api/v1`. FastAPI no declara una versión propia en `main.py` | `v1` |
| Base de datos | Revisión de Alembic | `d74a0f6c4e15` (la 13.ª y última; el servidor compartido está en ella) |
| Base local del teléfono (Room) | `version` en `@Database` (`app/src/main/java/mx/tec/codea/data/offline/AppDatabase.kt`); el esquema de cada versión se exporta a `app/schemas/mx.tec.codea.data.offline.AppDatabase/` (`1.json` a `5.json`) | `5` (con `MIGRATION_1_2` a `MIGRATION_4_5`) |
| Código | Commits en `main`; sin tags | — |

### 3.2 Reglas propuestas

1. **Un tag por versión entregada**, en la forma `v1.0.0`, sobre el commit exacto del que sale el APK. La rúbrica pide «enlace al repositorio con tag de la versión entregada».
2. **`versionCode` sube siempre** (1, 2, 3…) con cada APK que se distribuya; `versionName` sigue `MAJOR.MINOR.PATCH`. Android no permite instalar encima un APK con `versionCode` menor.
3. **La API es compatible hacia atrás dentro de `/api/v1`**: se pueden agregar campos y endpoints; **quitar o cambiar el significado de uno exige `/api/v2`** y una app nueva. Si una app vieja choca con una API nueva, la API responde errores con `code` estable y, si el token ya no sirve, la app cierra sesión.
4. **Cada cambio al modelo trae su migración** y no se edita una ya aplicada (`migrations/README.md`).
5. La **versión de la app y la de la migración** que exige quedan escritas en las notas de cada entrega **[completar por el equipo]**.

---

## 4. Configuración

### 4.1 Backend: qué cambia entre desarrollo y producción

Variables de `guarderia-api/.env` en desarrollo (`.env.dev`) y de `guarderia-api/.env.prod` en el servidor compartido (plantilla en `.env.prod.example`; detalle en [`manual-instalacion.md`](manual-instalacion.md), secciones 2.1 y 2.8).

| Variable | Desarrollo | Producción |
|---|---|---|
| `ENV` | `dev` | `prod` en `.env.prod` (cualquier valor distinto de `dev`). Con `dev` la API intenta crear el bucket y el seed pone contraseñas conocidas. Fuera de `dev` se activa HSTS |
| `JWT_SECRET` | cualquiera de 32+ caracteres | Generado con `secrets.token_urlsafe(48)`, **distinto del de desarrollo**, guardado fuera del repositorio. No hay rotación todavía |
| `POSTGRES_PASSWORD` / `DATABASE_URL` | valores de desarrollo de `.env.dev` | `DATABASE_URL` de Aiven (base `codea_prod`) con `?ssl=require`; solo vive en `.env.prod` |
| `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_BUCKET` | valores de desarrollo | Llaves propias, nuevas, solo en `.env.prod`. El bucket **debe crearse una vez**: `_ensure_bucket` solo corre con `ENV=dev` (comando en `guarderia-api/README.md`) |
| `S3_ENDPOINT_URL` | `http://storage:9000` (lo fija `docker-compose.yml`) | Igual: el almacén es el contenedor `storage` de `codea-prod` |
| `S3_PUBLIC_ENDPOINT_URL` | `http://10.0.2.2:9000` o `http://localhost:9000` | `https://archivos.codeaapp.site`: la dirección **HTTPS pública** del almacén, la que ve el teléfono |
| `PUBLIC_BASE_URL`, `ANDROID_CERT_SHA256` | vacías | `https://api.codeaapp.site` y la huella del certificado de la app, para el enlace de invitación |
| `CLOUDFLARE_TUNNEL_TOKEN` | — | El token del túnel `codea-prod`; si se filtra, se rota desde el panel de Cloudflare |
| `CORS_ORIGINS` | vacío | Vacío (la app Android no usa CORS) |
| `SEED_PASSWORD` | `codea1234` | **No se usa** (el seed no pone contraseñas conocidas fuera de `dev`) |
| `AUTH_MAX_FAILURES`, `AUTH_MAX_FAILURES_IP`, `AUTH_WINDOW_S` | 5 / 30 / 900 | Iguales; la IP real llega por el proxy (punto 1 de abajo) |

Tres puntos que antes quedaban abiertos y que `docker-compose.prod.yml` resolvió el 19/oct:

1. **IP real detrás de un proxy.** El límite de intentos y la bitácora usan la IP del cliente. Detrás del túnel, uvicorn corre con `--proxy-headers` y confía solo en la red interna `172.30.0.0/24`, así que la IP llega de `X-Forwarded-For` y un valor inventado por el cliente no cuela. Se comprobó con un login fallido el 19/oct (`docs/BITACORA.md`).
2. **Proxy con HTTPS.** Lo hace Cloudflare: el contenedor `tunnel` (`cloudflared`) abre una conexión de salida y Cloudflare termina el HTTPS (certificado emitido por Cloudflare).
3. **`docker-compose.override.yml` no es de producción**: monta el código, activa `--reload` y publica puertos. El servidor compartido usa `-p codea-prod --env-file .env.prod -f docker-compose.yml -f docker-compose.prod.yml`, que no carga el override, no publica ningún puerto y tiene volúmenes propios. El servicio `storage` (SeaweedFS) del archivo base es el que guarda las fotos del servidor compartido.

### 4.2 App: qué cambia entre debug y release

| Ajuste | Debug | Release |
|---|---|---|
| Dirección de la API | `-PapiBaseUrl`, `API_BASE_URL` o `http://10.0.2.2:8000/api/v1/` | `https://api.codeaapp.site/api/v1/`, o `API_BASE_URL_RELEASE` de `local.properties` |
| Tráfico HTTP | Permitido solo a `10.0.2.2` y `localhost` | **Prohibido** (`network_security_config.xml`) |
| Firma | Llave de depuración de Android | **Pendiente:** keystore propio y `signingConfig` |
| Minificación | — | **Pendiente:** hoy `optimization.enable = false`; activar y probar que no rompe la serialización ni la navegación |
| Modo sin conexión y caché | WorkManager envía la cola de asistencia con la restricción de red conectada y reintento con espera creciente; Room guarda la cola, la caché de Mi sala y la última respuesta de cada pantalla (`cached_response`) en `codea.db` | Igual |
| Botones «Simular llegada» del checador | Visibles (solo `BuildConfig.DEBUG`) | Ocultos |
| `allowBackup` | `false` | `false` |

El **keystore y sus contraseñas** los crea y guarda el equipo, fuera del repositorio; el asistente de trabajo no debe inventarlos (`PLAN-ACCION.md`). Si se pierde el keystore, **no se puede publicar una actualización que instale sobre la anterior**.

---

## 5. Despliegue

### 5.1 Requisitos previos (go / no-go)

| # | Requisito | Estado |
|---|---|---|
| 1 | Decisiones #6, #7 y #9 resueltas | 🟡 La #7 se aprobó (19/oct); #6 y #9 siguen abiertas |
| 2 | Servidor o plataforma elegida, con dominio y certificado HTTPS | ✅ `codeaapp.site` por túnel de Cloudflare, en la Mac de un integrante (19/oct). No es un servidor dedicado |
| 3 | Almacén de archivos de producción creado, con bucket y llaves | ✅ SeaweedFS de `codea-prod` con su bucket y llaves propias (19/oct); sin aprobar (#9) |
| 4 | Postgres de producción (distinto del de pruebas) | ✅ Aiven, base `codea_prod` (19/oct) |
| 5 | Respaldo programado de la base y del bucket, **con una restauración probada** | 🟡 Programado cada día desde el 20/oct; la restauración de la base se probó en un Postgres temporal; la de las fotos no está registrada |
| 6 | Todos los secretos generados y guardados fuera del repo | ✅ Solo en `.env.prod` de la Mac servidor (`docs/INCIDENTE-2026-10-19.md`, «Decisiones») |
| 7 | Plan para crear el primer administrador (ver 5.3) | ❌ En el servidor compartido el admin salió del seed |
| 8 | Textos corregidos en la app y aviso de privacidad aprobado | 🟡 Los dos textos falsos ya se corrigieron (`f814763`); falta aprobar el [`aviso-privacidad.md`](aviso-privacidad.md) y mostrarlo en la app |
| 9 | Pruebas de la API y de la app en verde sobre el commit a desplegar | ⏳ El equipo reporta 343 y 140 en verde el 20/oct; hay que correrlas sobre el commit que se etiquete |
| 10 | Tag de versión creado | ❌ |

### 5.2 Pasos para la API

Así se levantó el servidor compartido el 19/oct en la Mac de un integrante; los comandos exactos están en `guarderia-api/README.md`, «Producción en codeaapp.site», y para otra máquina se repiten igual. Los pasos 5 y 9 (respaldo antes de migrar y tag) son **propuesta**: el 19/oct no se hicieron.

1. **Preparar** el túnel en el panel de Cloudflare (`codea-prod`, con los nombres `api` y `archivos` de `codeaapp.site`), una vez.
2. **Crear** la base `codea_prod` en Aiven.
3. **Escribir** `.env.prod` desde `.env.prod.example` con secretos nuevos (4.1). No dejar valores del ejemplo.
4. **Arrancar:** `prod up -d --build` (el alias `prod` del README usa `-p codea-prod`, `.env.prod` y `docker-compose.prod.yml`).
5. **Respaldar** antes de cada migración: `scripts/respaldo.sh` a mano, además del respaldo diario (2.9 del manual de instalación).
6. **Migrar:** `prod exec api alembic upgrade head`, **antes** de recibir tráfico. Debe terminar en la revisión esperada (`alembic current`; hoy `d74a0f6c4e15`).
7. **Crear el bucket** una vez (`ensure_bucket`, comando en el README).
8. **Verificar** (5.4).
9. **Etiquetar** el commit desplegado (`vX.Y.Z`).
10. **Crear al primer administrador** (5.3).
11. **Dar de alta** docentes, infantes y tutores desde la app; cada persona activa su cuenta con su código (**no se mandan correos**: el administrador comparte el código por WhatsApp).
12. **Instalar el respaldo diario** con `launchd` (`scripts/site.codeaapp.respaldo.plist`; hecho el 20/oct en la Mac servidor).

Actualizar después: `git pull`, respaldo, `prod up -d --build` y `prod exec api alembic upgrade head`. Cuidado: el 19/oct se desplegó un cambio en el contenedor `api` **antes de hacer commit** (`docs/INCIDENTE-2026-10-19.md`, punto 3); lo que corre en el servidor debe salir siempre de un commit.

### 5.3 El problema del primer administrador

El repositorio **no tiene un comando para crear solo al administrador**. La única forma de tener uno es `python -m app.seed`, que además crea el centro de ejemplo «La Oruga» con coordenadas de Monterrey, 7 docentes, 6 tutores y 11 niños ficticios. Fuera de `ENV=dev` imprime los códigos de invitación en lugar de contraseñas, pero **sí inserta los datos de ejemplo**. (El servidor compartido tiene hoy las 22 cuentas de `seed_demo.py`, que **vacía la base** antes de sembrar: no sirve para crear un admin en una base con datos.)

| Opción | Costo |
|---|---|
| Correr el seed y luego dar de baja o eliminar a mano los datos de ejemplo | Quedan filas ficticias en producción y en la bitácora |
| Escribir un script `create_admin` (nuevo) | Pequeño, pero **no existe**; requiere código y prueba |

**[Decisión del equipo.]** Recomendación: la segunda opción antes del despliegue real.

### 5.4 Verificación después de desplegar

| Prueba | Cómo | Esperado |
|---|---|---|
| API viva | `GET https://api.codeaapp.site/api/v1/health/live` | `{"status":"ok"}` |
| Base conectada | `GET …/health/ready` | `{"status":"ok","database":"ok"}` |
| Cabeceras | `curl -sI https://api.codeaapp.site/api/v1/health/live` | `X-Content-Type-Options`, `X-Frame-Options`, `Cache-Control: no-store` y `Strict-Transport-Security` presentes |
| Login | `POST …/auth/login` con el administrador | `200` con `access_token` y `refresh_token` |
| Rol | `GET …/me` con ese token | Roles y centro |
| Bloqueo | 6 logins fallidos con un correo inventado | El sexto responde `429 TOO_MANY_ATTEMPTS` |
| Archivos | Subir una foto de prueba con el flujo `uploads` → POST al almacén → `complete`, y leerla con la URL firmada desde un teléfono | La foto se ve; vence en 10 minutos |
| Escaneo dinámico | OWASP ZAP contra la URL de pruebas | Hallazgos documentados (pendiente en la rúbrica, parte 2) |
| Rendimiento de fotos | Medir `GET /media` | Menos de 4 s (RNF05; **nadie lo ha medido**; una petición del teléfono al servidor compartido tarda ~1.6 s) |

El 19/oct se comprobaron en `api.codeaapp.site` la base conectada, las cabeceras (con `Strict-Transport-Security`), el certificado, que el almacén responde 403 sin firma y que la IP real del cliente llega a la API (`docs/BITACORA.md`). Las demás filas no tienen una comprobación registrada contra el servidor.

### 5.5 Despliegue de la app

1. La dirección de release ya es `https://api.codeaapp.site/api/v1/`; `API_BASE_URL_RELEASE` en `local.properties` solo hace falta para apuntar a otra.
2. Subir `versionCode` / `versionName` y hacer commit.
3. Crear el tag `vX.Y.Z`.
4. Generar el keystore (la primera vez) y configurar `signingConfig`; activar la minificación y **probar el APK minificado** en un teléfono.
5. `./gradlew :app:assembleRelease`.
6. Instalar el APK en un teléfono limpio, **sin `adb reverse`**, y recorrer los tres roles contra `api.codeaapp.site` (no se ha hecho un recorrido completo contra él).
7. Subir el APK al canal de 2.1 y enlazarlo en el PDF.

---

## 6. Migración de datos

### 6.1 De dónde a dónde

| Origen | Destino | Recomendación |
|---|---|---|
| Servidor compartido (`codea_prod` en Aiven) con los datos ficticios de `seed_demo.py` (22 cuentas con una contraseña que conoce todo el equipo) y las cuentas que el equipo dio de alta después | Producción con familias reales (piloto) | **No migrarla.** Son datos de prueba con contraseñas compartidas; antes del piloto, este servidor pasa a ser de pruebas y producción arranca con una base vacía, dando de alta a las personas reales (`docs/GUIA-CONTINUAR.md`) |
| Contenedor local de cada integrante | — | Se descarta |
| Producción versión N | Producción versión N+1 | Migraciones de Alembic (6.2) |

### 6.2 Cambios de esquema entre versiones

1. **Respaldo** de la base y de los archivos justo antes: `scripts/respaldo.sh` a mano, además del diario de las 3:00.
2. `alembic upgrade head` con la API detenida o en modo de mantenimiento.
3. `alembic current` debe mostrar la revisión esperada.
4. Arrancar la nueva versión y pasar las pruebas de 5.4.

Revisiones con cuidado especial (`migrations/README.md`):

| Revisión | Riesgo |
|---|---|
| `98306224d8b5` (login con JWT) | **Elimina `app_user.firebase_uid`**: el dato no se recupera (el `downgrade` recrea la columna vacía). Después de migrar, **nadie tiene contraseña** hasta que el administrador les genere un código; las invitaciones pendientes se dan por vencidas. Solo aplica a una base creada antes del login propio |
| `d63e9f4a1c32` (salida de la docente) | Agrega cuatro columnas a `staff_check_in`. Su `downgrade` las elimina y **se pierden las salidas ya registradas** (hora y ubicación de salida) |
| `e74f0a5b3d43` (checador sin ubicación) | Quita seis columnas de `staff_check_in` y agrega `auto_closed`. Su `downgrade` las recrea vacías: **las coordenadas y distancias ya guardadas se pierden para siempre** y no se recuperan. La base del servidor compartido ya nació con ella |
| `f85a1b6c4e54` (horario del centro) | Agrega cuatro columnas a `daycare_center` con valores por omisión; su `downgrade` las elimina y **se pierde el horario configurado** (vuelve a los valores por omisión al volver a subir). Va después de `e74f0a5b3d43` |
| `a96b2c7d5f65`, `a07c3d8e6f76`, `b18e4d7a2c93`, `c63f9e5b3d04` y `d74a0f6c4e15` (19/oct) | Agregan la foto del chat, la salud del niño y el pase de salida, la hora de entrada, la imagen de los avisos y el menú del día. Cada `downgrade` borra lo que su `upgrade` agregó: **se pierden** esas fotos de mensajes y avisos (la referencia), los datos de salud, las salidas, las horas de entrada o los menús ya capturados |
| `c52d8e3f0b21` (revisión de borrado de fotos) | Agrega `decided_by`, `decided_at` y `decision_note` a `media_deletion_request`. Su `downgrade` las elimina y **se pierden las decisiones ya tomadas**. El valor `DELETED` de `media.status` no necesita cambio de esquema, pero las fotos ya marcadas así seguirían marcadas tras bajar de versión |

### 6.3 Archivos (fotos y documentos)

- Los archivos viven en el bucket, con la llave `media/AAAA/MM/<uuid>/original.<ext>`, y la base apunta a ellos (`media.storage_key`).
- Cambiar de proveedor de almacén exige **copiar todos los objetos con las mismas llaves** y actualizar `S3_*`. Hoy no hay herramienta para eso.
- Las URLs firmadas no se guardan; se generan en cada lectura, así que cambiar el dominio del almacén no rompe datos viejos.
- En el servidor compartido, el respaldo diario copia el volumen completo del almacén (`codea-prod_media`) a un `.tar.gz`; para moverlo a otra máquina con el mismo SeaweedFS basta desempacarlo en el volumen nuevo (pasos en `guarderia-api/README.md`, «Respaldos»).

### 6.4 Datos que no se deben llevar

La bitácora de auditoría y los intentos de acceso de pruebas (`audit_log`, `auth_attempt`) no tienen valor en producción y contienen correos e IPs de prueba.

---

### 6.5 Base local del teléfono (`codea.db`)

La app crea en cada teléfono una base Room (`codea.db`, **versión 5**) con cuatro tablas locales: `pending_attendance`, `cached_room`, `cached_child` y `cached_response` (ver [`clasificacion-datos.md`](clasificacion-datos.md), 4.2.1). No se migra "de un servidor a otro": vive en el teléfono.

- **Actualizar la app** conserva la base (misma aplicación y misma firma). Si una versión nueva cambia el esquema, hay que **subir la versión y escribir una `Migration` de Room**. El código **no** usa `fallbackToDestructiveMigration`, así que sin migración la app falla al abrir la base nueva.
- **Qué se pierde si la base se recrea o se desinstala la app:** la caché (se recarga con red) y la **cola de marcas de asistencia sin enviar** (no se recupera). Antes de desinstalar o de forzar una migración destructiva hay que asegurarse de que no haya marcas pendientes (el banner «N cambios sin enviar» lo indica).
- **Ya existen cuatro migraciones:** `MIGRATION_1_2` agrega la columna `userId` a `pending_attendance` (cada marca queda ligada a su dueña; las marcas de la versión 1 quedan con `userId` vacío), `MIGRATION_2_3` agrega `notesJson` y `hasSummaryToday` a `cached_child`, `MIGRATION_3_4` agrega `arrivedAt` y `MIGRATION_4_5` crea `cached_response` (20/oct). La 1 → 2 se verificó a mano en el teléfono leyendo la base y la 4 → 5 en el emulador (`docs/BITACORA.md`). **No hay pruebas automáticas** de migración (no hay pruebas de Room en JVM); falta agregarlas antes de la siguiente.
- El esquema exportado en `app/schemas/` **debe entrar al control de versiones** con cada cambio de la base: es la referencia para escribir y probar migraciones.

## 7. Rollback

### 7.1 Principios

- Todo cambio de producción empieza con un **respaldo** y con la versión anterior identificada (imagen y APK).
- **Volver el código atrás es fácil; volver los datos atrás no.** Una vez que la API escribe con el esquema nuevo, regresar la imagen sin regresar la base puede romperla.
- Desde el 20/oct hay **respaldo diario** de la base y de las fotos (14 días) y se probó restaurar la base en un Postgres temporal. Un respaldo diario puede perder hasta un día de cambios: antes de migrar o de una operación riesgosa hay que sacar uno a mano (`scripts/respaldo.sh`). Antes del 20/oct no había respaldo y el borrado del 19/oct no se pudo deshacer (`docs/INCIDENTE-2026-10-19.md`).

### 7.2 Tabla de rollback

| Qué falló | Cómo se revierte | Pérdida | Estado |
|---|---|---|---|
| La API nueva no arranca o falla en las pruebas de 5.4, y **no hubo migración** | Volver a arrancar la imagen anterior | Ninguna | Propuesta; requiere conservar la imagen anterior |
| La API nueva falla **después de migrar** | 1) Detener la API. 2) `alembic downgrade -1` (o a la revisión previa). 3) Arrancar la imagen anterior | Los datos que usaban las columnas o tablas nuevas | **El `downgrade` de cada revisión no está probado** (`migrations/README.md`, «Pendiente»). Solo `98306224d8b5` se probó (sube, baja y sube) y baja con pérdida de `firebase_uid` |
| La migración dañó datos (o se borraron datos por error) | Restaurar con `pg_restore` el respaldo hecho antes de migrar, o el diario más reciente; primero en una base de prueba y sobre `codea_prod` solo con permiso del equipo (`guarderia-api/README.md`, «Respaldos») | Lo escrito después del respaldo (hasta un día con el diario) | 🟡 Respaldo diario desde el 20/oct; restauración de la base probada en un Postgres temporal, no sobre `codea_prod` |
| Un secreto se filtró (`JWT_SECRET`, llaves del almacén, token del túnel) | Generar uno nuevo en `.env.prod` y reiniciar (el token del túnel se rota desde el panel de Cloudflare). Cambiar `JWT_SECRET` invalida **todos** los access tokens; los refresh tokens siguen en la base, así que lo más limpio es también revocarlos (`UPDATE refresh_token SET revoked_at = now()`) | Todas las personas deben volver a iniciar sesión | Propuesta |
| Una persona perdió su teléfono o una cuenta está comprometida | El administrador da de baja a la persona: se corta su acceso en la siguiente petición y se revocan sus refresh tokens | Ninguna | ✅ Existe (`POST /teachers/{id}/deactivate`, `/parents/{id}/deactivate`) |
| Un reporte o una foto se subió por error | La foto la puede borrar la familia de un niño que sale en ella (`DELETE /media/{id}`, al momento y del almacén); ni la docente ni el administrador tienen cómo borrarla. Un reporte **no se puede editar ni anular** hoy | — | Parcial |
| El APK nuevo falla en los teléfonos | Android **no permite instalar un APK con `versionCode` menor** encima de otro mayor. Hay dos caminos: desinstalar y reinstalar el APK anterior (se pierde la sesión en el teléfono, no los datos del servidor), o publicar un APK nuevo con el código anterior y un `versionCode` mayor | La sesión local | Propuesta; la segunda opción es la limpia |
| Un APK nuevo cambió la versión de la base local y falla al abrirla | No se puede bajar la versión de una base Room sin migración inversa o recreación. Publicar un APK con `versionCode` mayor que arregle la migración (hoy hay migraciones de la 1 a la 5; ninguna hacia atrás: instalar una versión anterior que espere la versión 4 sobre una base 5 no es posible sin desinstalar). Desinstalar y reinstalar recrea la base y **pierde las marcas de asistencia sin enviar** | Marcas pendientes de ese teléfono | Propuesta; existen las migraciones 1→5 pero no hay pruebas automáticas de migración |
| El APK nuevo necesita una API que aún no se desplegó (o al revés) | Mantener la compatibilidad dentro de `/api/v1` (3.2). Desplegar **primero la API** y después distribuir el APK | — | Propuesta |

### 7.3 Orden de despliegue y de rollback

1. Despliegue: respaldo → API (con migración) → verificación → APK.
2. Rollback: retirar el APK nuevo del canal → revertir la API (y la base si hace falta, con el respaldo) → verificar con 5.4.

---

## 8. Operación después de implantar

| Tema | Estado hoy |
|---|---|
| Disponibilidad del 99 % (RNF04) | ❌ Sin medición. El servidor compartido depende de una Mac prendida y despierta: si se duerme, Cloudflare puede responder 530 hasta `docker restart codea-prod-tunnel-1` (`guarderia-api/README.md`). `/health/live` y `/health/ready` permiten monitorear desde fuera; no hay monitoreo configurado (Grafana está en la decisión #6) |
| Registros (logs) | La API escribe en la salida estándar del contenedor (`prod logs -f api tunnel`). El aviso a coordinación incluye el nombre de la docente. No hay política de cuánto tiempo se guardan |
| Respaldos | ✅ Cada día a las 3:00 (`launchd`), 14 días en `~/respaldos-codea/` de la Mac servidor; cada corrida deja una línea por paso en `respaldo.log` y su salida en `/tmp/codea-respaldo.log`. Nadie los revisa de forma programada **[completar por el equipo]** |
| Lentitud | Una petición del teléfono tarda ~1.6 s (Cloudflare, túnel y viajes a Aiven, ~50 ms por consulta; medido el 20/oct). Lo que más ayudaría es tener la base en la misma máquina que la API, a cambio de perder lo que da Aiven (`guarderia-api/README.md`, «Por qué tarda») |
| Limpieza periódica | No existe: tokens y códigos vencidos se acumulan; los archivos en estado `PENDING` nunca se limpian |
| Rotación de `JWT_SECRET` | No existe (no hay `kid`) |
| Códigos de acceso por correo | No se mandan correos: el administrador los comparte |
| Quién atiende una falla fuera de horario | **[completar por el equipo]** |

---

## 9. Lo que falta para poder implantar

| # | Pendiente | Responsable |
|---|---|---|
| 1 | Decidir #6 y #9 y el almacén de producción (#7 aprobada: túnel en `codeaapp.site`) | **[completar por el equipo]** |
| 2 | Proxy con HTTPS y manejo de la IP real | ✅ Túnel de Cloudflare y `--proxy-headers` (`docker-compose.prod.yml`), levantado y verificado el 19/oct |
| 3 | Respaldos y una restauración probada | ✅ Respaldo diario desde el 20/oct y restauración de la base probada. Falta probar la restauración de las fotos y guardar una copia fuera de la Mac servidor: **[completar por el equipo]** |
| 4 | Script para crear solo al primer administrador | **[completar por el equipo]** |
| 5 | Keystore, `signingConfig` y minificación probada | **[completar por el equipo]**: el keystore lo crea una persona del equipo |
| 6 | `API_BASE_URL_RELEASE` real | ✅ `https://api.codeaapp.site/api/v1/` por omisión en release |
| 7 | Pruebas de `downgrade` de cada migración | **[completar por el equipo]** |
| 8 | Tag de versión y notas de la entrega | **[completar por el equipo]** |
| 9 | Revisar **todos** los textos de la app contra lo que hace el sistema (los dos conocidos ya se corrigieron) | Ver [`etica-legal.md`](etica-legal.md), ETL-03 |
| 10 | Pruebas automáticas de migración de Room y del borrado de las cachés al cerrar sesión (hoy verificados a mano solo la migración 1 → 2 y el borrado de Mi sala) | **[completar por el equipo]** |
| 11 | Candado en `seed_demo.py --borrar-todo`: que se niegue fuera de desarrollo salvo que se escriba el nombre de la base, y que saque un respaldo antes (`docs/INCIDENTE-2026-10-19.md`, pendiente 3) | **[completar por el equipo]** |
| 12 | Que la app apunte a la API por omisión (`API_BASE_URL` en `app/build.gradle.kts` hoy cae en `10.0.2.2`) | **[completar por el equipo]** |
