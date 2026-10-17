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

Detalles y pruebas en la guía.
