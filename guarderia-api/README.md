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

Nadie entra solo por saber un correo. Al dar de alta a una docente (`POST /teachers`), a un tutor (`POST /children`) o a un padre (`POST /parents`), la respuesta trae `access_code`: un código `XXXX-XXXX` que **se muestra una sola vez** (en la base queda su hash) y dura 7 días. El administrador se lo comparte a la persona, que lo usa en `POST /auth/activate` junto con su correo y la contraseña que elija. Con eso queda con sesión iniciada.

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
{"type": "about:blank", "title": "Estás fuera del perímetro del centro", "status": 422, "code": "OUT_OF_RANGE",
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


