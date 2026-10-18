# Codea

App Android para una guardería (docente, administrador y padre de familia) con su propio backend. Proyecto de la materia TC2007B.

| Parte | Tecnología | Dónde |
|---|---|---|
| App | Kotlin, Jetpack Compose, Material 3, Navigation, Retrofit | [`app/`](app/README.md) |
| Backend | FastAPI, SQLAlchemy asíncrono, PostgreSQL, almacén S3, Docker | [`guarderia-api/`](guarderia-api/README.md) |
| Documentación | Markdown y prototipos HTML | [`docs/`](docs/README.md) |

> El marco técnico es el de <https://startdroid.pages.dev/>. Lo que se salga requiere aprobación: [`docs/MARCO-TECNICO.md`](docs/MARCO-TECNICO.md).

## Si vas a trabajar aquí

1. [`docs/GUIA-CONTINUAR.md`](docs/GUIA-CONTINUAR.md): cómo levantar todo, mapa del repo, trampas y por dónde empezar.
2. [`docs/ESTADO.md`](docs/ESTADO.md): qué está hecho y qué falta (foto del 2026-10-16).
3. [`docs/REQUISITOS.md`](docs/REQUISITOS.md): los 32 requisitos y su estado.
4. [`docs/PLAN.md`](docs/PLAN.md) y [`docs/ENTREGA-3.md`](docs/ENTREGA-3.md): plan y checklist de la entrega final (22 de octubre de 2026, 23:59).
5. [`docs/DECISIONES-PENDIENTES.md`](docs/DECISIONES-PENDIENTES.md): excepciones al marco que esperan aprobación.

## Arranque rápido

```bash
# backend
cd guarderia-api && cp .env.example .env && docker compose up -d --build
docker compose exec api alembic upgrade head && docker compose exec api python -m app.seed

# app (emulador)
./gradlew installDebug
```

Antes de levantar la API, edita `guarderia-api/.env` y llena `JWT_SECRET` (sin él la API no arranca): `python -c "import secrets; print(secrets.token_urlsafe(48))"`. Cada quien usa su propio `.env`; está en `.gitignore` y nunca se sube. Para usar Aiven en vez del Postgres local, llena `DATABASE_URL` en ese archivo (ver `.env.example`).

Detalles y pruebas en la guía.

## Cómo entrar (cuentas de prueba)

El seed (`python -m app.seed`, solo con `ENV=dev`) crea estas cuentas. Todas usan la contraseña `codea1234` (la de `SEED_PASSWORD` en el `.env`).

| Rol | Correo | Qué ves |
|---|---|---|
| Administrador | `admin@oruga.test` | Padres, salas, infantes y personal |
| Docente | `karla.robles@oruga.test` | Preescolar 2: asistencia, rutina y reportes |
| Madre | `laura.iglesias@oruga.test` | Su hijo Mateo Iglesias |

- **En la app:** escribe el correo y la contraseña en la pantalla de inicio de sesión.
- **En Swagger** (`/docs`): `POST /auth/login`, copia el `access_token` y pégalo en **Authorize** (dura 5 minutos).
- Son datos de prueba. No uses esta contraseña en una base pública: si corres el seed contra Aiven, cambia `SEED_PASSWORD` y compártela por privado.
