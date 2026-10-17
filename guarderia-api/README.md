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

Con `AUTH_MODE=firebase` la app manda el ID token de Firebase (`Authorization: Bearer <token>`) y la API lo verifica con `firebase-admin`. El rol y lo que cada quien puede ver salen de Postgres, no de Firebase.

Para desarrollar sin proyecto de Firebase, `AUTH_MODE=dev` (solo funciona con `ENV=dev`) acepta `Authorization: Bearer dev:<correo>`. El seed imprime los correos; por ejemplo:

| Rol | Correo |
| --- | --- |
| ADMIN | `admin@oruga.test` |
| TEACHER | `karla.robles@oruga.test` (Preescolar 2) |
| PARENT | `laura.iglesias@oruga.test` (mamá de Mateo Iglesias) |

En Swagger, el botón **Authorize** pide solo el token: escribe `dev:karla.robles@oruga.test`.

### Invitaciones

Nadie entra solo por tener cuenta de Google. Al dar de alta a una docente (`POST /teachers`), a un tutor (`POST /children`) o a un padre (`POST /parents`), la API crea una invitación para su correo. La primera vez que esa persona entra, la app llama `POST /auth/accept-invitation`, que liga su cuenta con la invitación. Después de eso, `GET /me` ya regresa su rol. Por ahora no se mandan correos: la invitación solo queda en el log.

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
