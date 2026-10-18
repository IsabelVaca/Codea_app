# Manual de instalación

Cómo levantar el backend (API, base de datos y almacén de archivos) y cómo compilar e instalar la app Android. Estado a **2026-10-18**, rama `integracion`, commit `22c0008`.

Todo sale de archivos reales del repositorio: `guarderia-api/README.md`, `docker-compose.yml`, `docker-compose.override.yml`, `Dockerfile`, `.env.example`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `docs/RECORRIDO.md` y `docs/BITACORA.md`. **Los comandos no se ejecutaron para escribir este documento** (la base, los puertos y el teléfono estaban ocupados por otra sesión de trabajo), así que cada paso dice de dónde sale. Lo que no se puede confirmar con los archivos está marcado como **«No confirmado»**.

Para publicar el backend en un servidor y firmar un APK, ver [`plan-implantacion.md`](plan-implantacion.md): este manual cubre la instalación de **desarrollo y pruebas**.

---

## 1. Lo que necesitas

### Backend

| Requisito | Detalle | Fuente |
|---|---|---|
| Docker con Compose v2 (`docker compose`, sin guion) | El `docker-compose.yml` no declara `version`, así que es el formato moderno. La versión mínima **no está confirmada** | `docker-compose.yml` |
| Puertos libres en `127.0.0.1`: **8000** (API), **5432** (Postgres) y **9000** (almacén) | Los publica el archivo de desarrollo | `docker-compose.override.yml` |
| Python 3.12 *solo si vas a correr las pruebas fuera de Docker* | La imagen usa `python:3.12-slim` | `Dockerfile` |
| Internet para descargar las imágenes | `postgres:16`, `chrislusf/seaweedfs:3.80`, `python:3.12-slim` | `docker-compose.yml`, `Dockerfile` |

### App

| Requisito | Detalle | Fuente |
|---|---|---|
| Android Studio reciente (o línea de comandos con el SDK de Android) | La app usa el plugin de Android 9.3.3, Gradle 9.5.0, Kotlin 2.2.10 y compila con la API 37 (`compileSdk`), `targetSdk` 36 | `gradle/libs.versions.toml`, `gradle-wrapper.properties`, `app/build.gradle.kts` |
| JDK | Las versiones recientes del plugin de Android piden JDK 17 o más nuevo (dato de la documentación de Android, **no figura en el repositorio**). Android Studio trae uno. **No está confirmado** cuál usa el equipo | — |
| SDK de Android con la plataforma 37 | Lo descarga Android Studio | `app/build.gradle.kts` |
| Dependencias nuevas del modo sin conexión (las descarga Gradle la primera vez; hace falta internet) | **Room 2.8.4** (base local), **WorkManager 2.11.2** (envío de la cola al volver la red) y el plugin **KSP 2.3.12** (genera el código de Room). Al compilar, Room exporta el esquema de la base a `app/schemas/` (se versiona) | `gradle/libs.versions.toml`, `app/build.gradle.kts` |
| Teléfono con Android 7.0 o más nuevo (`minSdk` 24), o un emulador | Con depuración USB activada si es físico | `app/build.gradle.kts` |
| `adb` (viene con las herramientas de plataforma del SDK) | Solo para el teléfono físico por USB | `docs/BITACORA.md` |
| Archivo `local.properties` en la raíz del repo | Android Studio lo crea con `sdk.dir`; está ignorado por git. Opcionalmente lleva `API_BASE_URL` y `API_BASE_URL_RELEASE` | `.gitignore`, `app/build.gradle.kts` |

---

## 2. Backend

Todos los comandos se corren desde `guarderia-api/`.

### 2.1 Variables de entorno (`.env`)

```bash
cd guarderia-api
cp .env.example .env
```

Abre `.env` y revisa estas variables. Las que no aparecen en `.env.example` tienen un valor por omisión en `app/core/config.py`.

| Variable | Qué es | Obligatoria | Nota |
|---|---|---|---|
| `JWT_SECRET` | Llave que firma los access tokens (HS256) | **Sí.** La API **no arranca** sin ella: mínimo 32 caracteres | Genera una con `python3 -c "import secrets; print(secrets.token_urlsafe(48))"` |
| `ENV` | `dev` o producción | No (`dev` por omisión) | Con `dev` la API crea sola el bucket al arrancar y el seed pone contraseñas conocidas. Fuera de `dev` se activa HSTS |
| `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB` | Credenciales del contenedor `db` | Sí para `docker compose` | El ejemplo trae `POSTGRES_PASSWORD=123456`: **cámbialo** |
| `DATABASE_URL` | Base externa (por ejemplo Aiven) | No | Si tiene valor se usa esa base y no el contenedor `db`. Formato del ejemplo: `postgresql+asyncpg://USUARIO:PASSWORD@HOST:PUERTO/BASE?ssl=require` |
| `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_BUCKET` | Credenciales y nombre del bucket de fotos y documentos | Sí | El ejemplo trae `S3_SECRET_KEY=cambia-esta-clave`: **cámbialo** |
| `S3_PUBLIC_ENDPOINT_URL` | Dirección del almacén **tal como la ve el teléfono** | Sí, para ver fotos | Emulador: `http://10.0.2.2:9000`. Teléfono físico por USB con `adb reverse`: `http://localhost:9000`. Si está mal, las fotos no cargan |
| `ACCESS_TTL_S`, `REFRESH_TTL_S` | Duración de los tokens (300 s y 604800 s) | No | |
| `AUTH_MAX_FAILURES`, `AUTH_MAX_FAILURES_IP`, `AUTH_WINDOW_S` | Bloqueo por intentos fallidos (5, 30 y 900 s) | No | |
| `CORS_ORIGINS` | Orígenes web permitidos | No | Vacío = ninguno. La app Android no lo necesita |
| `SEED_PASSWORD` | Contraseña de las cuentas del seed (`codea1234` por omisión) | No | Solo se usa con `ENV=dev` |
| `SEED_ADMIN_EMAIL` | Correo del administrador del seed | No | `admin@oruga.test` |

`.env` está ignorado por git (`.gitignore`) y por Docker (`.dockerignore`). No lo subas.

### 2.2 Levantar los contenedores

```bash
docker compose up -d --build
```

Esto construye la imagen `api` y arranca tres servicios (`docker-compose.yml`):

| Servicio | Qué es | Puerto en tu máquina |
|---|---|---|
| `db` | Postgres 16 con un volumen `pgdata` | `127.0.0.1:5432` |
| `storage` | SeaweedFS con API compatible con S3 (solo desarrollo) | `127.0.0.1:9000` |
| `api` | FastAPI (uvicorn) | `127.0.0.1:8000` |

`docker compose` lee **también** `docker-compose.override.yml`, que publica los puertos, monta `app/` y `migrations/` dentro del contenedor y arranca uvicorn con `--reload`. Eso es para desarrollo: **no es el modo de producción**.

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
| `d63e9f4a1c32` | Salida de la docente (columnas `checked_out_at`, `out_latitude`, `out_longitude`, `out_distance_m` en `staff_check_in`). Según `docs/BITACORA.md`, la base de Aiven ya está migrada a esta revisión |
| `e74f0a5b3d43` | Checador sin ubicación: quita `latitude`, `longitude`, `distance_m` y `out_latitude`, `out_longitude`, `out_distance_m` de `staff_check_in` y agrega `auto_closed`. **No está aplicada en Aiven.** Su `downgrade` no recupera las coordenadas borradas |


Para comprobar en qué revisión quedó la base: `docker compose exec api alembic current`.

> Si la base ya tiene datos de una versión anterior (por ejemplo, la de Aiven), **lee antes** `guarderia-api/migrations/README.md`: `98306224d8b5` elimina `app_user.firebase_uid` y deja a todos sin contraseña. Haz un respaldo.

### 2.4 Datos de ejemplo (seed)

```bash
docker compose exec api python -m app.seed
```

- **Solo siembra si la base está vacía.** Si ya hay un centro, imprime «La base ya tiene datos; no se siembra nada» y no hace nada.
- Crea: el centro «La Oruga» (con coordenadas de Monterrey), 2 turnos, la plantilla de rutina de 9 momentos, 3 salas (Maternal, Preescolar 1 y Preescolar 2), **1 administrador, 7 docentes, 6 tutores y 11 niños**, y 2 documentos y 2 personas autorizadas para el niño Mateo Iglesias. Así sale del código (`app/seed.py`); la bitácora del equipo menciona 15 usuarios en la base de Aiven, una más de las que crea el seed, y **no se confirmó por qué** (probablemente una cuenta creada a mano).
- Con `ENV=dev` todas las cuentas quedan con la contraseña `SEED_PASSWORD` (`codea1234`). Con otro `ENV` no se pone ninguna contraseña conocida: cada persona recibe un código de invitación que se imprime una sola vez.

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

- `conftest.py` lee `POSTGRES_USER` y `POSTGRES_PASSWORD` del `.env` o del entorno, y usa `TEST_DB_HOST` (por omisión `localhost`). Si tu `.env` apunta a Aiven, la contraseña del contenedor local puede ser otra: pásala así, `POSTGRES_PASSWORD=<la del contenedor local> .venv/bin/pytest` (`docs/RECORRIDO.md`). **Nunca** corras las pruebas contra Aiven.
- La documentación de pruebas (`guarderia-api/tests/README.md`) habla de 276 pruebas de la API al 2026-10-18 No se ejecutaron para este documento.

### 2.7 Apagar y reiniciar

```bash
docker compose down        # detiene los contenedores, conserva los volúmenes (datos)
docker compose down -v     # ADEMÁS borra la base y los archivos: empieza de cero
```

---

## 3. App Android

Todos los comandos se corren desde la **raíz** del repositorio.

### 3.1 Cómo elige la app a qué API hablar

La dirección sale de `BuildConfig.API_BASE_URL`, que `app/build.gradle.kts` resuelve en este orden:

1. `-PapiBaseUrl=...` en la línea de comandos de Gradle.
2. `API_BASE_URL=...` en `local.properties`.
3. Si no hay ninguna: `http://10.0.2.2:8000/api/v1/` (el emulador de Android ve tu máquina en `10.0.2.2`).

En **release** se lee `API_BASE_URL_RELEASE` de `local.properties`; si no existe queda `https://api.codea.invalid/api/v1/` **a propósito**, para que nadie entregue un APK que apunte a la computadora de alguien.

El tráfico sin cifrar (HTTP) solo se permite en compilaciones debug y únicamente hacia `10.0.2.2` y `localhost` (`app/src/debug/res/xml/network_security_config.xml`). Un release exige HTTPS.

> `docs/GUIA-CONTINUAR.md` dice que para un teléfono físico se cambia `BASE_URL` en `ApiConfig.kt`. Está desactualizado: `ApiConfig.BASE_URL` ahora toma el valor de `BuildConfig`. Usa `-PapiBaseUrl`.

### 3.2 En el emulador

1. Levanta el backend (sección 2) con `S3_PUBLIC_ENDPOINT_URL=http://10.0.2.2:9000` en `.env`.
2. Instala:

```bash
./gradlew :app:installDebug
```

o abre el proyecto en Android Studio y ejecútalo. No hace falta indicar la dirección: usa `10.0.2.2:8000`.

### 3.3 En un teléfono físico por USB

Es el procedimiento que usó el equipo el 18 de octubre (`docs/BITACORA.md`).

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

### 3.4 Mapa del checador

Ya no hay mapa ni `MAPS_API_KEY`: el checador no usa ubicación (18/oct).

### 3.5 Pruebas de la app

```bash
./gradlew :app:testDebugUnitTest      # pruebas JVM (JUnit con servidor falso)
```

`./gradlew test` también corre las pruebas JVM. Las pruebas de interfaz (`connectedAndroidTest`) necesitan un dispositivo y solo existe un ejemplo vacío. El conteo de pruebas JVM está en `app/src/test/README.md` (74 al 2026-10-18, en verde según `docs/BITACORA.md`); no se ejecutaron para este documento.

#### Probar el modo sin conexión en el teléfono

El procedimiento que usó el equipo (`docs/BITACORA.md`, «Operación sin conexión»):

1. Con la app instalada y la docente dentro, abre Mi sala (así se guarda la copia local).
2. Apaga la API: `docker compose stop api` (desde `guarderia-api/`).
3. Marca una falta: la marca se ve y aparece el banner «1 cambio sin enviar». Cierra la app por completo y ábrela: Mi sala sale de lo guardado.
4. Enciende la API: `docker compose start api`. Sin tocar la app, la marca debe llegar al servidor en menos de un minuto y el banner desaparece.

La base local (`codea.db`) y la cola se guardan en el teléfono; **no** hay pruebas de Room en JVM. La base está en la versión 2 (`MIGRATION_1_2` agrega `userId` a la cola, que va ligada a la docente que hizo cada marca). Si cambias el esquema (`AppDatabase.kt`) hay que subir la versión y escribir la migración de Room.

### 3.6 Generar el APK de release

```bash
./gradlew :app:assembleRelease
```

**No está listo para entrega.** Según `app/build.gradle.kts` y `app/README.md`:

- No existe `signingConfig`: el APK sale **sin firmar** y no se puede instalar en un teléfono tal cual.
- La minificación está desactivada (`optimization.enable = false`).
- La dirección de la API es la inválida de la sección 3.1 mientras no pongas `API_BASE_URL_RELEASE` en `local.properties`.
- El keystore y sus contraseñas **no existen en el repositorio** (ni deben existir). Quien lo cree debe guardarlo fuera de git. Qué comandos usará el equipo para crearlo y firmar **no está confirmado**; el tema está en [`plan-implantacion.md`](plan-implantacion.md).

---

## 4. Problemas frecuentes

| Síntoma | Causa probable | Qué hacer |
|---|---|---|
| La API se reinicia o se cierra al arrancar | Falta `JWT_SECRET` o tiene menos de 32 caracteres | Genera uno y ponlo en `.env`; `docker compose up -d` |
| `docker compose up` falla por el puerto 5432, 8000 o 9000 | Otro programa usa ese puerto | Detén el otro programa o cambia el puerto en `docker-compose.override.yml` |
| El teléfono dice que no conecta | Falta `adb reverse`, o instalaste sin `-PapiBaseUrl` | Repite 3.3 |
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
| Cómo instalar el backend en un servidor público | No hay despliegue ni dominio (ver el plan de implantación) |
| Por qué la base de Aiven tiene 15 usuarios y el seed crea 14 | La bitácora lo menciona; no hay explicación en los archivos |
