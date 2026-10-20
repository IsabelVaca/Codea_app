# API de la guardería

FastAPI + PostgreSQL + un almacén compatible con S3 para fotos y documentos. Los endpoints son los de la primera entrega de [`docs/api/endpoints-api-codea.pdf`](../docs/api/endpoints-api-codea.pdf); la lista vigente, sacada del código, está en [`docs/api/endpoints.md`](../docs/api/endpoints.md). Siguen la [propuesta de arquitectura](https://claude.ai/artifact/K4CqXjp82tkRLfpKDRdV5H). El contrato vivo es el OpenAPI: con la API prendida, ábrelo en http://localhost:8000/docs.

## Levantarla

```bash
cp .env.example .env              # y cambia las claves
docker compose up -d --build      # api, db (Postgres) y storage (SeaweedFS, compatible con S3)
docker compose exec api alembic upgrade head
docker compose exec api python -m app.seed   # datos de ejemplo, los mismos de la app
```

- API: http://localhost:8000/api/v1 · Swagger: http://localhost:8000/docs
- Almacén S3: http://localhost:9000. En desarrollo la API crea el bucket sola al arrancar.

## Producción en codeaapp.site

Corre en cualquier máquina con Docker y salida a internet, sin abrir puertos: un túnel de Cloudflare ([decisión #7](../docs/DECISIONES-PENDIENTES.md), aprobada el 19/oct) lleva el HTTPS hasta los contenedores. La máquina tiene que estar prendida para que la app funcione.

| Nombre público | Va a | Para qué |
|---|---|---|
| `https://api.codeaapp.site` | `http://api:8000` | La API (`/api/v1`), `/invitar` y `/.well-known/assetlinks.json` |
| `https://archivos.codeaapp.site` | `http://storage:9000` | El almacén: el teléfono sube y baja las fotos directo de aquí con URLs firmadas |

**Una vez, en el panel de Cloudflare** (el dominio ya está en la cuenta):

1. Zero Trust → Networks → Tunnels → *Create a tunnel* → tipo *Cloudflared* → nombre `codea-prod`. Copia el token (lo que sigue de `--token` en el comando que muestra); no hace falta instalar nada, el conector es el contenedor `tunnel`.
2. En *Public hostnames* del túnel agrega los dos nombres de la tabla (subdominio `api` y `archivos`, dominio `codeaapp.site`, tipo `HTTP`, URL `api:8000` y `storage:9000`). Cloudflare crea los registros DNS solo.

**En la máquina:**

```bash
cp .env.prod.example .env.prod    # llena todo: secretos nuevos, llaves S3 y CLOUDFLARE_TUNNEL_TOKEN
alias prod='docker compose -p codea-prod --env-file .env.prod -f docker-compose.yml -f docker-compose.prod.yml'
prod up -d --build
prod exec api alembic upgrade head
prod exec api python -c "from app.core.storage import get_storage; get_storage().ensure_bucket()"   # el bucket, una vez
curl https://api.codeaapp.site/api/v1/health/ready   # {"status":"ok","database":"ok"}
```

- `-p codea-prod` le da volúmenes propios: la base y las fotos de desarrollo (con contraseñas conocidas) no salen a internet.
- `docker-compose.override.yml` no se usa aquí: con `-f` explícitos Compose no lo carga.
- La IP de cada cliente llega en `X-Forwarded-For` y uvicorn la toma con `--proxy-headers`, confiando solo en la red interna (`172.30.0.0/24`); así funciona el límite de intentos por IP.
- **Primer administrador:** `prod exec api python -m app.seed` lo crea con `SEED_ADMIN_EMAIL` e imprime su código de acceso, pero **también mete los datos de ejemplo** («La Oruga»). No hay todavía un comando que cree solo al admin ([plan de implantación](../docs/entrega/plan-implantacion.md), 5.3).
- Actualizar: `git pull`, `prod up -d --build` y `prod exec api alembic upgrade head`. Ver los registros: `prod logs -f api tunnel`.
- Si el túnel no conecta (`Failed to dial a quic connection`), la red bloquea UDP: por eso el compose fija `--protocol http2`.
- Un dominio recién agregado a Cloudflare tarda unos minutos en tener certificado; mientras, HTTPS falla con `handshake failure`.

**Compartirla con el equipo.** Los developers apuntan su app a `https://api.codeaapp.site/api/v1/` (cómo y con qué reglas: [`docs/GUIA-CONTINUAR.md`](../docs/GUIA-CONTINUAR.md), sección 2). Las cuentas salen del seed o de un admin; fuera de `ENV=dev` el seed imprime un código de acceso por persona: pásale a cada quien su correo y su código por privado. Nunca compartas `.env.prod` ni el token del túnel; si el token se filtra, ve al panel del túnel → *Rotate token* y cámbialo en `.env.prod`.

### La base de producción: Aiven (decisión del 19/oct)

Producción usa **Aiven**, no el Postgres del contenedor `db`. Cloudflare no interviene: el túnel solo lleva tráfico web a `api` y `storage`, y la API habla con la base por `DATABASE_URL`. Para pasar de la base local a Aiven:

1. En Aiven crea una **base nueva** dentro del servicio (por ejemplo `codea_prod`). **No** uses la de pruebas manuales (`codea1234`) ni la de las pruebas automáticas.
2. En `.env.prod` descomenta `DATABASE_URL=postgresql+asyncpg://USUARIO:PASSWORD@HOST:PUERTO/codea_prod?ssl=require` (con `?ssl=require`). Con ese valor la API ignora el servicio `db` del compose.
3. `prod up -d --build` y `prod exec api alembic upgrade head`: arma la base vacía completa desde cero (13 migraciones, hasta `d74a0f6c4e15`; se comprobó en una base temporal). No hay nada que migrar: la base local de producción estaba vacía.
4. El almacén de fotos sigue siendo el SeaweedFS del compose (`prod exec api python -c "from app.core.storage import get_storage; get_storage().ensure_bucket()"` una vez).
5. `prod exec api python -m app.seed` crea el primer administrador con `SEED_ADMIN_EMAIL`, pero **también mete los datos de ejemplo**; hoy no hay un comando que cree solo al admin.
6. Cada actualización del código que traiga migraciones: `git pull`, `prod up -d --build` y `prod exec api alembic upgrade head`.

Revisa en Aiven cómo se respaldan las bases de tu plan (los respaldos siguen sin estar documentados en el proyecto) y cuántas conexiones permite.

## Entrar

Login propio con correo y contraseña (JWT). La regla: **el token dice quién eres y Postgres dice qué puedes hacer**: el JWT solo lleva el id de la persona; el rol, el alcance y si está de baja se leen de la base en cada petición. Diseño completo en [`docs/plan/04-api-login-jwt.md`](../docs/plan/04-api-login-jwt.md).

Antes de arrancar, `.env` necesita `JWT_SECRET` (mínimo 32 caracteres; la API no arranca sin él):

```bash
python3 -c "import secrets; print(secrets.token_urlsafe(48))"   # pega el resultado en .env
```

| Token | Dura | Para qué |
| --- | --- | --- |
| `access_token` (JWT, HS256) | 5 minutos | Va en cada petición: `Authorization: Bearer <access_token>` |
| `refresh_token` (texto aleatorio) | 7 días | Se cambia por un par nuevo en `POST /auth/refresh`; **cada uso lo rota** y el viejo deja de servir. En la base solo queda su hash |

Si llega un refresh token que ya se había rotado, alguien lo copió: se cierra toda la sesión de esa familia.

El seed (`python -m app.seed`, solo con `ENV=dev`) deja a todos con la contraseña `SEED_PASSWORD` (`codea1234` por omisión) e imprime los correos; por ejemplo:

| Rol | Correo |
| --- | --- |
| ADMIN | `admin@oruga.test` |
| TEACHER | `karla.robles@oruga.test` (Preescolar 2) |
| PARENT | `laura.iglesias@oruga.test` (mamá de Mateo Iglesias) |

En Swagger: haz `POST /auth/login` con correo y contraseña, copia el `access_token` y pégalo en **Authorize**. Dura 5 minutos; para uno nuevo usa `POST /auth/refresh`.

La contraseña lleva de 9 a 128 caracteres y no puede ser el correo (RNF01: "mayores a 8"). Se guarda con Argon2id.

### Códigos de acceso (invitaciones)

Nadie entra solo por saber un correo. Al dar de alta a una docente (`POST /teachers`), a un tutor (`POST /children`) o a un padre (`POST /parents`), la respuesta trae `access_code`: un código `XXXX-XXXX` que **se muestra una sola vez** (en la base queda su hash) y dura 7 días. El administrador se lo manda a la persona (la app tiene el botón **Enviar por WhatsApp**), que lo usa en `POST /auth/activate` junto con su correo y la contraseña que elija. Con eso queda con sesión iniciada.

Si `PUBLIC_BASE_URL` está configurada, la respuesta trae además `access_code.link` (`https://<servidor>/invitar?email=…&code=…`): al tocarlo, la app se abre con el correo y el código puestos y solo falta la contraseña. La página `/invitar` solo lee: no gasta el código. Ver [`auth`](app/modules/auth/README.md).

- **Otro código:** `POST /users/{id}/access-code` (admin). Si la persona nunca entró es de tipo `INVITATION`; si ya entró, `PASSWORD_RESET` (olvidó su contraseña): al usarlo se cierran sus sesiones anteriores. Un código nuevo deja sin vigencia a los anteriores.
- **Cambiar la contraseña:** `POST /me/password`. Cierra todas las sesiones y devuelve tokens nuevos para la actual. `DELETE /me/sessions` cierra todas las sesiones.
- **Baja:** dar de baja a alguien sube su `token_version` y revoca sus refresh tokens, así que su acceso se corta en la siguiente petición. Al reactivarla debe volver a entrar.
- Por ahora no se mandan correos: el administrador comparte el código.

## Fotos y documentos

Los archivos no pasan por la API:

1. `POST /media/uploads` regresa `url` y `fields` (un permiso de 5 minutos).
2. La app manda un POST multipart a `url` con todos los `fields` y el archivo en el campo `file`.
3. `POST /media/{id}/complete` avisa que ya subió. Después, el `media_id` se usa en `POST /reports` o en `PUT /children/{id}/documents/{doc_id}`.

Para leerlos, la API regresa URLs firmadas de 10 minutos. Se firman con `S3_PUBLIC_ENDPOINT_URL`, que debe ser la dirección que ve el teléfono: `http://10.0.2.2:9000` desde el emulador de Android.

## Errores

Todos los errores salen con el mismo formato (Problem Details) y un `code` estable que la app traduce, igual que hoy traduce `SalaError` o `InfanteError`:

```json
{"type": "about:blank", "title": "Primero registra tu entrada de hoy", "status": 409, "code": "NOT_CHECKED_IN",
 "detail": "Estás a 1401 m; el radio permitido es de 60 m", "distance_m": 1401, "allowed_radius_m": 60}
```

Lo que queda fuera del alcance de alguien (el niño de otra familia, la sala de otra docente) responde 404, no 403.

## Pruebas

Corren contra un Postgres real: la base `guarderia_test`, en el mismo contenedor `db` y creada sola.

```bash
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt -r requirements-dev.txt
.venv/bin/pytest
```

Las reglas tienen los mismos casos que las pruebas de `domain/` de la app (`CheckInRulesTest`, `RoutineRulesTest` y `ReportValidatorTest`), para que la misma regla dé la misma respuesta en los dos lados.

## Estructura

Cada módulo de `app/modules/` separa `router.py` (HTTP y rol), `service.py` (reglas, consultas y alcance), `schemas.py` (Pydantic) y `models.py` (SQLAlchemy). Lo compartido vive en `app/core/`:

- `security.py`: login, roles y alcance
- `rules.py`: validaciones de `domain/`
- `storage.py`: el bucket
- `clock.py`: el reloj, que las pruebas fijan
- `pagination.py`: paginación por cursor

Cada cambio al modelo lleva su migración: `alembic revision --autogenerate -m "..."`, y se revisa antes de aplicarla.

## Documentación por carpeta

- Estado y pendientes del backend: [`PENDIENTES.md`](PENDIENTES.md)
- Índice de módulos, cada uno con su README: [`app/modules/README.md`](app/modules/README.md)
- Lo compartido: [`app/core/README.md`](app/core/README.md) · pruebas: [`tests/README.md`](tests/README.md) · migraciones: [`migrations/README.md`](migrations/README.md)
- Marco técnico (solo lo de startdroid) y estado general: [`../docs/MARCO-TECNICO.md`](../docs/MARCO-TECNICO.md), [`../docs/ESTADO.md`](../docs/ESTADO.md)


