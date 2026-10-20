# Manual de instalación

Cómo levantar el backend (API, base de datos y almacén de archivos), cómo funciona el servidor compartido `codeaapp.site` y cómo compilar e instalar la app Android. Estado a **2026-10-20**, `main` con los PR hasta el #17 más el commit de respaldos (`043c2d6`); el equipo reporta 343 pruebas de la API y 140 JVM en verde.

Todo sale de archivos reales del repositorio: `guarderia-api/README.md`, `docker-compose.yml`, `docker-compose.override.yml`, `docker-compose.prod.yml`, `Dockerfile`, `.env.dev`, `.env.example`, `.env.prod.example`, `scripts/respaldo.sh`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `docs/RECORRIDO.md`, `docs/BITACORA.md` y `docs/INCIDENTE-2026-10-19.md`. **Los comandos no se ejecutaron para escribir este documento**, así que cada paso dice de dónde sale. Lo que no se puede confirmar con los archivos está marcado como **«No confirmado»**.

**Dos servidores, no los mezcles** (`docs/INCIDENTE-2026-10-19.md`):

| | Servidor compartido (el de todos) | Backend local (el tuyo) |
|---|---|---|
| Para qué | La app de todo el equipo, la demostración y los datos de prueba compartidos | Trabajar en el backend y correr `pytest` |
| Dirección | `https://api.codeaapp.site/api/v1/` | `http://localhost:8000/api/v1/` |
| Base y fotos | Aiven (`codea_prod`) y SeaweedFS en la Mac servidor (`archivos.codeaapp.site`) | Postgres y SeaweedFS en contenedores de tu máquina |
| Dónde se explica | Sección 2.8 y `guarderia-api/README.md`, «Producción en codeaapp.site» | Secciones 2.1 a 2.7 |

**Desde el 19/oct la app apunta siempre al servidor compartido.** Una cuenta dada de alta en un servidor no existe en el otro: si un código de acceso «no es válido», revisa primero a qué servidor apunta cada teléfono. Para firmar un APK, ver [`plan-implantacion.md`](plan-implantacion.md).

---

## 1. Lo que necesitas

### Backend

| Requisito | Detalle | Fuente |
|---|---|---|
| Docker con Compose v2 (`docker compose`, sin guion) | El `docker-compose.yml` no declara `version`, así que es el formato moderno. La versión mínima **no está confirmada** | `docker-compose.yml` |
| Puertos libres en `127.0.0.1`: **8000** (API), **5432** (Postgres) y **9000** (almacén) | Los publica el archivo de desarrollo | `docker-compose.override.yml` |
| Python 3.12 *solo si vas a correr las pruebas fuera de Docker* | La imagen usa `python:3.12-slim` | `Dockerfile` |
| Internet para descargar las imágenes | `postgres:16`, `chrislusf/seaweedfs:3.80`, `python:3.12-slim`; en la Mac servidor además `cloudflare/cloudflared:2026.10.1` (túnel) y `postgres:18` (respaldos) | `docker-compose.yml`, `docker-compose.prod.yml`, `Dockerfile`, `scripts/respaldo.sh` |

### App

| Requisito | Detalle | Fuente |
|---|---|---|
| Android Studio reciente (o línea de comandos con el SDK de Android) | La app usa el plugin de Android 9.3.3, Gradle 9.5.0, Kotlin 2.2.10 y compila con la API 37 (`compileSdk`), `targetSdk` 36 | `gradle/libs.versions.toml`, `gradle-wrapper.properties`, `app/build.gradle.kts` |
| JDK | Las versiones recientes del plugin de Android piden JDK 17 o más nuevo (dato de la documentación de Android, **no figura en el repositorio**). Android Studio trae uno. **No está confirmado** cuál usa el equipo | — |
| SDK de Android con la plataforma 37 | Lo descarga Android Studio | `app/build.gradle.kts` |
| Dependencias nuevas del modo sin conexión (las descarga Gradle la primera vez; hace falta internet) | **Room 2.8.4** (base local), **WorkManager 2.11.2** (envío de la cola al volver la red) y el plugin **KSP 2.3.12** (genera el código de Room). Al compilar, Room exporta el esquema de la base a `app/schemas/` (se versiona) | `gradle/libs.versions.toml`, `app/build.gradle.kts` |
| Teléfono con Android 7.0 o más nuevo (`minSdk` 24), o un emulador | Con depuración USB activada si es físico | `app/build.gradle.kts` |
| `adb` (viene con las herramientas de plataforma del SDK) | Solo para el teléfono físico por USB | `docs/BITACORA.md` |
| Archivo `local.properties` en la raíz del repo | Android Studio lo crea con `sdk.dir`; está ignorado por git. Lleva `API_BASE_URL=https://api.codeaapp.site/api/v1/` para usar el servidor compartido (sección 3.1); opcionalmente `API_BASE_URL_RELEASE` e `INVITE_HOST` | `.gitignore`, `app/build.gradle.kts`, `docs/GUIA-CONTINUAR.md` |

---

## 2. Backend

Todos los comandos se corren desde `guarderia-api/`. Las secciones 2.1 a 2.7 son el **backend local** (para trabajar en la API y correr las pruebas); el servidor compartido está en 2.8 y sus respaldos en 2.9.

### 2.1 Variables de entorno (`.env`)

```bash
cd guarderia-api
cp .env.dev .env        # listo para desarrollo, sin secretos reales (o .env.example para llenarlo a mano)
```

`.env.dev` (desde el 19/oct) trae valores **solo de desarrollo**, públicos y distintos de los de producción. Revisa estas variables. Las que no aparecen en el archivo tienen un valor por omisión en `app/core/config.py`.

| Variable | Qué es | Obligatoria | Nota |
|---|---|---|---|
| `JWT_SECRET` | Llave que firma los access tokens (HS256) | **Sí.** La API **no arranca** sin ella: mínimo 32 caracteres | `.env.dev` trae una pública, solo para tu máquina. Para cualquier otro uso genera una con `python3 -c "import secrets; print(secrets.token_urlsafe(48))"` |
| `ENV` | `dev` o producción | No (`dev` por omisión) | Con `dev` la API crea sola el bucket al arrancar y el seed pone contraseñas conocidas. Fuera de `dev` se activa HSTS |
| `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB` | Credenciales del contenedor `db` | Sí para `docker compose` | Los valores de `.env.dev` y `.env.example` son de desarrollo: **nunca** los uses fuera de tu máquina |
| `DATABASE_URL` | Base externa (por ejemplo Aiven) | No | Si tiene valor se usa esa base y no el contenedor `db`. Formato del ejemplo: `postgresql+asyncpg://USUARIO:PASSWORD@HOST:PUERTO/BASE?ssl=require`. **No** pongas aquí la base del servidor compartido: esa solo vive en `.env.prod` de la Mac servidor |
| `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_BUCKET` | Credenciales y nombre del bucket de fotos y documentos | Sí | Igual: los de los archivos de ejemplo son de desarrollo |
| `S3_PUBLIC_ENDPOINT_URL` | Dirección del almacén **tal como la ve el teléfono** | Sí, para ver fotos | `.env.dev` trae `http://localhost:9000` (teléfono físico por USB con `adb reverse`). Emulador: `http://10.0.2.2:9000`. Servidor compartido: `https://archivos.codeaapp.site`. Si está mal, las fotos no cargan |
| `PUBLIC_BASE_URL`, `ANDROID_CERT_SHA256` | Dirección pública de la API y huella del certificado de la app, para el enlace de invitación que abre la app | No | Vacías en desarrollo: el código de acceso se comparte a mano. En el servidor compartido, `PUBLIC_BASE_URL=https://api.codeaapp.site` |
| `ACCESS_TTL_S`, `REFRESH_TTL_S` | Duración de los tokens (300 s y 604800 s) | No | |
| `AUTH_MAX_FAILURES`, `AUTH_MAX_FAILURES_IP`, `AUTH_WINDOW_S` | Bloqueo por intentos fallidos (5, 30 y 900 s) | No | |
| `CORS_ORIGINS` | Orígenes web permitidos | No | Vacío = ninguno. La app Android no lo necesita |
| `SEED_PASSWORD` | Contraseña de las cuentas del seed (`codea1234` por omisión) | No | Solo se usa con `ENV=dev` |
| `SEED_ADMIN_EMAIL` | Correo del administrador del seed | No | `admin@oruga.test` |

`.env` y `.env.prod` están ignorados por git (`.gitignore`) y por Docker (`.dockerignore`). No los subas. En git solo están `.env.dev`, `.env.example` y `.env.prod.example`, sin secretos reales.

### 2.2 Levantar los contenedores

```bash
docker compose up -d --build
```

Esto construye la imagen `api` y arranca tres servicios (`docker-compose.yml`):

| Servicio | Qué es | Puerto en tu máquina |
|---|---|---|
| `db` | Postgres 16 con un volumen `pgdata` | `127.0.0.1:5432` |
| `storage` | SeaweedFS con API compatible con S3 (arranca con `docker/seaweedfs-s3.sh`: hasta 100 volúmenes de 1 GB desde el 20/oct). Es el mismo que guarda las fotos del servidor compartido | `127.0.0.1:9000` |
| `api` | FastAPI (uvicorn) | `127.0.0.1:8000` |

`docker compose` lee **también** `docker-compose.override.yml`, que publica los puertos, monta `app/` y `migrations/` dentro del contenedor y arranca uvicorn con `--reload`. Eso es para desarrollo: **no es el modo de producción** (el servidor compartido usa `docker-compose.prod.yml`, sección 2.8).

### 2.3 Migraciones (Alembic)

```bash
docker compose exec api alembic upgrade head
```

Aplica en orden las migraciones de `guarderia-api/migrations/versions/`:

| Revisión | Qué crea |
|---|---|
| `63224ea4bc31` | Usuarios, centro, salas y niños |
| `caf6344f99d5` | El resto del modelo de la primera entrega |
| `98306224d8b5` | Login con JWT (quita `firebase_uid`, agrega tokens e invitaciones con hash) |
| `b41c7d2e9a10` | Bitácora de auditoría (`audit_log`) e intentos de acceso (`auth_attempt`) |
| `c52d8e3f0b21` | Revisión de solicitudes de borrado de fotos (columnas `decided_by`, `decided_at`, `decision_note`) |
| `d63e9f4a1c32` | Salida de la docente (columnas `checked_out_at`, `out_latitude`, `out_longitude`, `out_distance_m` en `staff_check_in`) |
| `e74f0a5b3d43` | Checador sin ubicación: quita `latitude`, `longitude`, `distance_m` y `out_latitude`, `out_longitude`, `out_distance_m` de `staff_check_in` y agrega `auto_closed`. Su `downgrade` no recupera las coordenadas borradas |
| `f85a1b6c4e54` | Horario del centro: agrega `entry_time`, `exit_time`, `auto_close_time` y `work_days` a `daycare_center`, con valores por omisión (8:00, 16:00, 21:00, lunes a viernes) |
| `a96b2c7d5f65` | Foto en el chat (`message.media_id`) |
| `a07c3d8e6f76` | Salud del niño (cuatro columnas en `child`) y pase de salida (tabla `child_departure`) |
| `b18e4d7a2c93` | Hora de entrada del niño (`attendance.arrived_at`) |
| `c63f9e5b3d04` | Imagen en los avisos (`announcement.media_id`) |
| `d74a0f6c4e15` | Menú del día (tabla `daily_menu`). Es la última (*head*): son 13 migraciones |

La base del servidor compartido (`codea_prod` en Aiven) se creó vacía con las 13 y está en `d74a0f6c4e15` (`guarderia-api/README.md`).

Para comprobar en qué revisión quedó la base: `docker compose exec api alembic current`.

> Si la base ya tiene datos de una versión anterior al login con JWT, **lee antes** `guarderia-api/migrations/README.md`: `98306224d8b5` elimina `app_user.firebase_uid` y deja a todos sin contraseña. Haz un respaldo (en el servidor compartido, con `scripts/respaldo.sh`, sección 2.9).

### 2.4 Datos de ejemplo (seed)

```bash
docker compose exec api python -m app.seed
```

- **Solo siembra si la base está vacía.** Si ya hay un centro, imprime «La base ya tiene datos; no se siembra nada» y no hace nada.
- Crea: el centro «La Oruga» (con coordenadas de Monterrey), 2 turnos, la plantilla de rutina de 9 momentos, 3 salas (Maternal, Preescolar 1 y Preescolar 2), **1 administrador, 7 docentes, 6 tutores y 11 niños**, y 2 documentos y 2 personas autorizadas para el niño Mateo Iglesias. Así sale del código (`app/seed.py`).
- Con `ENV=dev` todas las cuentas quedan con la contraseña `SEED_PASSWORD` (`codea1234`). Con otro `ENV` no se pone ninguna contraseña conocida: cada persona recibe un código de invitación que se imprime una sola vez.

**Datos de prueba completos (`app/seed_demo.py`, desde el 19/oct).** `python -m app.seed_demo --borrar-todo` (con `--con-fotos` si se quieren fotos generadas) **vacía toda la base y el bucket** y siembra 22 cuentas y dos semanas de un centro con tres salas: pase de lista, rutina, reportes, resúmenes, salidas, checador, menús, chats, avisos y notas. Quién es quién: `docs/DATOS-DE-PRUEBA.md`. Fuera de `ENV=dev` se niega a usar la contraseña por omisión. **Cuidado:** el 19/oct se corrió sobre el servidor compartido y borró cuentas, fotos y avisos que no tenían respaldo (`docs/INCIDENTE-2026-10-19.md`). El candado que debe impedirlo fuera de desarrollo **está pendiente**: no lo corras contra el servidor compartido sin acuerdo del equipo y sin un respaldo recién hecho (2.9).

Cuentas de prueba (con `ENV=dev`):

| Rol | Correo |
|---|---|
| Administrador | `admin@oruga.test` |
| Docente | `karla.robles@oruga.test` (sala Preescolar 2) |
| Padre o madre | `laura.iglesias@oruga.test` (mamá de Mateo Iglesias) |

El seed imprime los demás correos, con la forma `nombre.apellido@oruga.test` sin acentos.

### 2.5 Comprobar que funciona

| Comprobación | Dirección | Esperado |
|---|---|---|
| La API está viva | `http://localhost:8000/api/v1/health/live` | `{"status":"ok"}` |
| Puede hablar con la base | `http://localhost:8000/api/v1/health/ready` | `{"status":"ok","database":"ok"}` (si no, `503 DB_UNAVAILABLE`) |
| Documentación | `http://localhost:8000/docs` | Swagger. Haz `POST /auth/login`, copia el `access_token` y pégalo en **Authorize** (dura 5 minutos) |

Si la API se cierra al arrancar, revisa los registros con `docker compose logs api`: lo más probable es que falte `JWT_SECRET` (o tenga menos de 32 caracteres).

### 2.6 Pruebas del backend

Las pruebas corren **contra un Postgres real**: la base `guarderia_test` en el contenedor `db`, que `tests/conftest.py` crea sola. Necesitan el contenedor `db` prendido y su puerto 5432 publicado.

```bash
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt -r requirements-dev.txt
.venv/bin/pytest
```

- `conftest.py` lee `POSTGRES_USER` y `POSTGRES_PASSWORD` del `.env` o del entorno, y usa `TEST_DB_HOST` (por omisión `localhost`). Con el `.env` copiado de `.env.dev` no hace falta nada más. Si tu `.env` tiene otra contraseña que la del contenedor local, pásala así: `POSTGRES_PASSWORD=<la del contenedor local> .venv/bin/pytest`. **Nunca** corras las pruebas contra Aiven ni contra el servidor compartido.
- Al 20/oct el equipo reporta **343 pruebas** de la API en verde (`docs/BITACORA.md`); `guarderia-api/tests/README.md` todavía dice 276. No se ejecutaron para este documento.

### 2.7 Apagar y reiniciar

```bash
docker compose down        # detiene los contenedores, conserva los volúmenes (datos)
docker compose down -v     # ADEMÁS borra la base y los archivos: empieza de cero
```

Estos comandos, sin `-p codea-prod`, solo tocan tu backend local, no el servidor compartido.

### 2.8 Servidor compartido (`codeaapp.site`)

Desde el 19/oct la API corre en `https://api.codeaapp.site/api/v1/` ([decisión #7](../DECISIONES-PENDIENTES.md), aprobada). Resumen de `guarderia-api/README.md`, «Producción en codeaapp.site», que tiene los pasos completos:

| Pieza | Qué es |
|---|---|
| Máquina | La Mac de un integrante del equipo, con Docker. **Tiene que estar prendida y sin dormirse**: si se duerme, la app deja de funcionar para todos |
| Contenedores | Proyecto `codea-prod` (`docker compose -p codea-prod --env-file .env.prod -f docker-compose.yml -f docker-compose.prod.yml`): `api` (uvicorn con `--proxy-headers`), `storage` (SeaweedFS), `db` (sin uso: la base está en Aiven) y `tunnel` (`cloudflared` por HTTP/2, porque la red bloquea QUIC). No se abre ningún puerto |
| Nombres públicos | `api.codeaapp.site` → `api:8000` y `archivos.codeaapp.site` → `storage:9000` (las fotos se suben y se bajan directo de ahí con URLs firmadas). Se configuran una vez en el panel del túnel de Cloudflare |
| Base | Aiven, base `codea_prod` (Postgres 18), por `DATABASE_URL` en `.env.prod`. Migración `d74a0f6c4e15` |
| Secretos | Solo en `.env.prod` de esa Mac (llaves de la base, del almacén, JWT y token del túnel). Nunca se comparten ni se suben |

Comandos frecuentes en la Mac servidor (con el alias `prod` del README):

```bash
prod up -d --build && prod exec api alembic upgrade head   # actualizar después de git pull
curl https://api.codeaapp.site/api/v1/health/ready          # {"status":"ok","database":"ok"}
prod logs -f api tunnel                                     # ver registros
docker restart codea-prod-tunnel-1                          # si Cloudflare responde 530 al despertar la Mac
```

**Primer administrador:** `prod exec api python -m app.seed` lo crea e imprime su código de acceso, pero también mete los datos de ejemplo; no existe un comando que cree solo al admin ([`plan-implantacion.md`](plan-implantacion.md), 5.3). Las demás cuentas las da de alta el admin desde la app.

**Lentitud:** una petición desde el teléfono tarda ~1.6 s (Cloudflare, túnel y viajes a Aiven; medido el 20/oct, `guarderia-api/README.md`, «Por qué tarda»).

### 2.9 Respaldos del servidor compartido (desde el 20/oct)

`guarderia-api/scripts/respaldo.sh` corre **cada día a las 3:00** con `launchd` en la Mac servidor (`scripts/site.codeaapp.respaldo.plist`; si la Mac estaba dormida, corre al despertar). Deja en `~/respaldos-codea/` (carpeta solo del usuario, modo 700):

- `codea_prod-diario-AAAAMMDD-HHMM.dump`: la base de Aiven con `pg_dump -Fc` desde la imagen `postgres:18`; antes de guardarlo comprueba con `pg_restore --list` que traiga tablas.
- `fotos-diario-AAAAMMDD-HHMM.tar.gz`: el volumen de fotos `codea-prod_media` (pausa el almacén unos segundos para copiarlo).
- `respaldo.log`: una línea por paso.

Borra los diarios de más de 14 días (`RESPALDOS_DIAS`). Instalarlo, correrlo a mano y **restaurar** la base o las fotos: `guarderia-api/README.md`, «Respaldos». Se probó restaurar la base en un Postgres 18 temporal el 20/oct. Restaurar sobre `codea_prod` solo con permiso del equipo. **Límites:** los respaldos están en la misma Mac que el servidor y no están cifrados.

---

## 3. App Android

Todos los comandos se corren desde la **raíz** del repositorio.

### 3.1 Cómo elige la app a qué API hablar

La dirección sale de `BuildConfig.API_BASE_URL`, que `app/build.gradle.kts` resuelve en este orden:

1. `-PapiBaseUrl=...` en la línea de comandos de Gradle.
2. `API_BASE_URL=...` en `local.properties`.
3. Si no hay ninguna: `http://10.0.2.2:8000/api/v1/` (el emulador de Android ve tu máquina en `10.0.2.2`). Cambiar este valor por omisión al servidor compartido está pendiente (`docs/INCIDENTE-2026-10-19.md`, pendiente 5).

En **release** va al servidor compartido, `https://api.codeaapp.site/api/v1/`; `API_BASE_URL_RELEASE` en `local.properties` la cambia. Cómo se levanta: sección 2.8.

El tráfico sin cifrar (HTTP) solo se permite en compilaciones debug y únicamente hacia `10.0.2.2` y `localhost` (`app/src/debug/res/xml/network_security_config.xml`). Un release exige HTTPS.

> Un APK compilado con `localhost` no conecta en un teléfono sin `adb reverse` (en el teléfono, `localhost` es el propio teléfono): así le pasó a la tablet el 19/oct (`docs/INCIDENTE-2026-10-19.md`, punto 4). Antes de instalar en un teléfono, revisa a qué dirección apunta.

### 3.2 Contra el servidor compartido (lo normal)

Sirve igual en el emulador y en un teléfono físico, por USB o no; no hace falta levantar nada ni `adb reverse`.

1. En `local.properties` pon `API_BASE_URL=https://api.codeaapp.site/api/v1/` (`docs/GUIA-CONTINUAR.md`, sección 2).
2. Instala:

```bash
./gradlew :app:installDebug
```

o abre el proyecto en Android Studio y ejecútalo (Android Studio también lee `local.properties`). Las cuentas de prueba están en `docs/DATOS-DE-PRUEBA.md`; la contraseña se pide al equipo (no está en el repositorio). Usa una cuenta distinta a la de tus compañeros.

### 3.3 Contra tu backend local, en el emulador (solo para trabajar en el backend)

1. Levanta el backend (sección 2) con `S3_PUBLIC_ENDPOINT_URL=http://10.0.2.2:9000` en `.env`.
2. Quita `API_BASE_URL` de `local.properties` (o compila con `-PapiBaseUrl=http://10.0.2.2:8000/api/v1/`) e instala con `./gradlew :app:installDebug`.

### 3.4 Contra tu backend local, en un teléfono físico por USB

Es el procedimiento que usó el equipo el 18 y el 19 de octubre (`docs/BITACORA.md`), antes de tener el servidor compartido.

1. Activa las **opciones de desarrollador** y la **depuración USB** en el teléfono y conéctalo. Acepta la huella de la computadora cuando el teléfono la pida.
2. Comprueba que se ve: `adb devices` debe listar el teléfono como `device`.
3. En `guarderia-api/.env` pon `S3_PUBLIC_ENDPOINT_URL=http://localhost:9000` y reinicia la API (`docker compose up -d` vuelve a leer el `.env`).
4. Redirige los puertos del teléfono a tu computadora:

```bash
adb reverse tcp:8000 tcp:8000
adb reverse tcp:9000 tcp:9000
```

5. Instala apuntando a `localhost`:

```bash
./gradlew :app:installDebug -PapiBaseUrl=http://localhost:8000/api/v1/
```

Si desconectas y vuelves a conectar el cable, **repite el paso 4**: `adb reverse` se pierde.

### 3.5 Mapa del checador

Ya no hay mapa ni `MAPS_API_KEY`: el checador no usa ubicación (18/oct).

### 3.6 Pruebas de la app

```bash
./gradlew :app:testDebugUnitTest      # pruebas JVM (JUnit con servidor falso)
```

`./gradlew test` también corre las pruebas JVM. Las pruebas de interfaz (`connectedAndroidTest`) necesitan un dispositivo y solo existe un ejemplo vacío. Al 20/oct el equipo reporta **140 pruebas JVM** en verde (24 clases; `docs/BITACORA.md`); `app/src/test/README.md` todavía dice 74. No se ejecutaron para este documento.

#### Probar el modo sin conexión en el teléfono

El procedimiento que usó el equipo (`docs/BITACORA.md`, «Operación sin conexión»):

1. Con la app instalada y la docente dentro, abre Mi sala (así se guarda la copia local).
2. Quita la red: pon el teléfono en **modo avión**. Si trabajas contra tu backend local, también sirve apagar la API con `docker compose stop api` (desde `guarderia-api/`). **Nunca** detengas la API del servidor compartido: se cae para todo el equipo.
3. Marca una falta: la marca se ve y aparece el banner «1 cambio sin enviar». Cierra la app por completo y ábrela: Mi sala sale de lo guardado.
4. Devuelve la red (o `docker compose start api`). Sin tocar la app, la marca debe llegar al servidor en menos de un minuto y el banner desaparece.

El equipo lo probó el 18/oct contra la API local apagada; con modo avión no hay una prueba registrada.

La base local (`codea.db`) y la cola se guardan en el teléfono; **no** hay pruebas de Room en JVM. La base está en la versión 5: `MIGRATION_1_2` agrega `userId` a la cola, que va ligada a la docente que hizo cada marca; `MIGRATION_2_3` agrega `notesJson` y `hasSummaryToday` a `cached_child`; `MIGRATION_3_4` agrega `arrivedAt`; y `MIGRATION_4_5` crea `cached_response`, la última respuesta de la API de cada pantalla de los tres roles, que se muestra al instante y se borra al cerrar la sesión (20/oct). Si cambias el esquema (`AppDatabase.kt`) hay que subir la versión y escribir la migración de Room.

### 3.7 Generar el APK de release

```bash
./gradlew :app:assembleRelease
```

**No está listo para entrega.** Según `app/build.gradle.kts` y `app/README.md`:

- No existe `signingConfig`: el APK sale **sin firmar** y no se puede instalar en un teléfono tal cual.
- La minificación está desactivada (`optimization.enable = false`).
- La dirección de la API es la del servidor compartido (sección 3.1): si la Mac servidor está apagada o dormida, la app no conecta.
- El keystore y sus contraseñas **no existen en el repositorio** (ni deben existir). Quien lo cree debe guardarlo fuera de git. Qué comandos usará el equipo para crearlo y firmar **no está confirmado**; el tema está en [`plan-implantacion.md`](plan-implantacion.md).

---

## 4. Problemas frecuentes

| Síntoma | Causa probable | Qué hacer |
|---|---|---|
| La API se reinicia o se cierra al arrancar | Falta `JWT_SECRET` o tiene menos de 32 caracteres | Genera uno y ponlo en `.env`; `docker compose up -d` |
| `docker compose up` falla por el puerto 5432, 8000 o 9000 | Otro programa usa ese puerto | Detén el otro programa o cambia el puerto en `docker-compose.override.yml` |
| El teléfono dice que no conecta | El APK apunta a `localhost` o a `10.0.2.2` (por ejemplo, porque `local.properties` no tiene `API_BASE_URL`), o falta `adb reverse` si usas tu backend local | Revisa la dirección (3.1) y repite 3.2, o 3.4 si usas tu backend local |
| Desde fuera, el servidor compartido responde **530** | La Mac servidor se durmió y el túnel perdió su conexión con Cloudflare | En la Mac servidor: `docker restart codea-prod-tunnel-1` (sección 2.8) y evitar que se duerma |
| «El código no es válido» al activar una cuenta nueva | La cuenta se dio de alta en otro servidor (por ejemplo, el local) | Dar de alta a la persona desde un admin que apunte al servidor compartido (`docs/INCIDENTE-2026-10-19.md`, punto 3) |
| El almacén registra «only 0 volumes left» | SeaweedFS arrancaba con 8 volúmenes | Corregido el 20/oct en `docker/seaweedfs-s3.sh` (hasta 100); se aplica con `docker restart codea-prod-storage-1` |
| Las fotos no cargan | `S3_PUBLIC_ENDPOINT_URL` no es la dirección que ve el teléfono | Ajusta el valor (sección 2.1) y reinicia la API |
| `401 INVALID_CREDENTIALS` con una cuenta del seed | El seed no corrió, o corrió con `ENV` distinto de `dev` | Corre el seed en base vacía; con `ENV` distinto de `dev` usa el código de invitación |
| `429 TOO_MANY_ATTEMPTS` | 5 fallos seguidos con el mismo correo | Espera 15 minutos |
| Las pruebas de la API no conectan a la base | Contraseña del `.env` distinta de la del contenedor local | `POSTGRES_PASSWORD=<la del contenedor> .venv/bin/pytest` |
| `ModuleNotFoundError: pwdlib` al correr pruebas | El `venv` es anterior al login con JWT | `.venv/bin/pip install -r requirements.txt -r requirements-dev.txt` |

## 5. Lo que este manual no pudo confirmar

| Tema | Por qué |
|---|---|
| Versión mínima de Docker y de Compose, y versión de JDK | No están declaradas en el repositorio |
| Que cada comando funcione en una máquina limpia | No se ejecutó nada para escribirlo |
| Cómo se crea el keystore y se firma el APK | No hay configuración de firma en el repositorio |
| Que el servidor compartido se pueda reconstruir en otra máquina siguiendo el README | Solo se ha levantado en una Mac (19/oct) |
| Que el respaldo de las fotos se pueda restaurar | El README da los pasos; solo se registró la prueba de restaurar la base |
