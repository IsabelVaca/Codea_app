# Guía para continuar el proyecto

Si llegas nuevo, lee en este orden: este archivo → [`ESTADO.md`](ESTADO.md) → [`MARCO-TECNICO.md`](MARCO-TECNICO.md) → [`REQUISITOS.md`](REQUISITOS.md) → el README de la carpeta donde vas a trabajar.

Ya existían y **son la referencia para el cómo** (no los dupliques): [`estilo.md`](estilo.md) (convenciones), [`plan/`](plan/README.md) (pantallas, conexión de la app, estado de la API, login JWT, tareas) y [`api/endpoints.md`](api/endpoints.md) (los 65 endpoints).

## 1. Reglas que no se negocian

1. **El marco técnico es el de <https://startdroid.pages.dev/>.** Nada fuera de esa lista sin aprobación; se anota en [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md).
2. **Firebase está excluido** para notificaciones y tiempo real. El login ya es propio (JWT, rama `feature/login-jwt`); no agregues más uso de Firebase.
3. Texto de la interfaz en español; comentarios del código en inglés en lo que ya existe (los más recientes están en español: sigue el estilo del archivo vecino).
4. Dependencias en un solo sentido: `ui/screens` → `ui/state` → `data` → `domain`. `domain/` no importa Android.
5. La API repite las reglas de `domain/` (`guarderia-api/app/core/rules.py`): si cambias una regla, cámbiala en los dos lados y en sus pruebas.

## 2. Levantar todo

**Backend**
```bash
cd guarderia-api
cp .env.example .env            # cambia las claves
docker compose up -d --build
docker compose exec api alembic upgrade head
docker compose exec api python -m app.seed
```
API en `http://localhost:8000/api/v1`, Swagger en `/docs`.

**Antes de arrancar**, `guarderia-api/.env` necesita `JWT_SECRET` (mínimo 32 caracteres; la API no arranca sin él): `python3 -c "import secrets; print(secrets.token_urlsafe(48))"`.

**Entrar:** el seed deja a todos con la contraseña `codea1234` (`SEED_PASSWORD`). Correos: `admin@oruga.test`, `karla.robles@oruga.test` (docente), `laura.iglesias@oruga.test` (madre). En Swagger haz `POST /auth/login`, copia el `access_token` y pégalo en **Authorize** (dura 5 minutos). En la app, esos mismos correo y contraseña en la pantalla de inicio de sesión.

**Personas nuevas:** el admin las da de alta, la app muestra un **código de acceso** (`XXXX-XXXX`, solo se ve una vez) y la persona lo usa en "Es mi primera vez: activar mi cuenta".

**App**
```bash
./gradlew installDebug
```
- Emulador: ya apunta a `http://10.0.2.2:8000/api/v1/`.
- Celular físico por USB: compila con `-PapiBaseUrl=http://localhost:8000/api/v1/` (o ponlo en `local.properties`) y corre `adb reverse tcp:8000 tcp:8000 && adb reverse tcp:9000 tcp:9000`; sin eso la app apunta a `10.0.2.2` (emulador). Detalle en [`entrega/manual-instalacion.md`](entrega/manual-instalacion.md).

**Pruebas**
```bash
./gradlew test                                     # 125 pruebas JVM
cd guarderia-api && .venv/bin/pytest               # 324 pruebas de la API (Postgres real; con POSTGRES_PASSWORD si cambiaste la contraseña)
```

## 3. Mapa del repo

| Ruta | Qué es | README |
|---|---|---|
| `app/` | La app Android | [`app/README.md`](../app/README.md) |
| `app/src/main/java/mx/tec/codea/domain/` | Modelos y reglas puras | README propio |
| `.../data/`, `.../data/api/` | Repositorios y cliente HTTP | README propio |
| `.../ui/state/` | ViewModels | README propio |
| `.../ui/screens/` (+ `admin/`, `parent/`) | Pantallas | README propio |
| `.../ui/navigation/`, `.../ui/components/` | Navegación y piezas reutilizables | README propio |
| `guarderia-api/` | El backend | [`guarderia-api/README.md`](../guarderia-api/README.md), [`PENDIENTES.md`](../guarderia-api/PENDIENTES.md) |
| `guarderia-api/app/modules/<área>/` | Un módulo por área | README por módulo |
| `docs/` | Documentación | este archivo |
| `docs/prototipos/` | Maquetas HTML de diseño | [`README.md`](README.md) |

## 4. Cómo agregar una función de punta a punta

1. Elige el requisito en [`REQUISITOS.md`](REQUISITOS.md) y confirma que cabe en el marco.
2. **Backend:** modelo → migración (`alembic revision --autogenerate -m "..."`, revisarla) → esquemas → servicio (con alcance por registro y `ApiError` con `code` estable) → router → prueba en `tests/`.
3. **App:** endpoint y DTO en `data/api/` → repositorio en `data/` → ViewModel en `ui/state/` (escrituras con `launchAction`) → pantalla en `ui/screens/` → ruta en `ui/navigation/Routes.kt` y destino en `CodeaNavHost.kt`.
4. Actualiza el estado en [`REQUISITOS.md`](REQUISITOS.md) y el `PENDIENTES.md` / README de la carpeta tocada.

## 5. Trampas conocidas

- **`CodeaViewModel` es compartido:** se crea una vez en `CodeaApp`. No lo pidas con `viewModel()` dentro de un destino: ese destino recibiría una copia aparte.
- **`ApiConfig.isAdmin/isTeacher/isParent`:** cada ViewModel solo pide datos si su rol lo permite; si no, la API contesta 403.
- **`DataEvents.changed`:** al terminar una escritura todos los ViewModels recargan. Si agregas un ViewModel con lista, suscríbete a ese flujo.
- **Fuera de alcance = 404**, no 403.
- **Fotos:** nunca pasan por la API; se suben con URL firmada (`MediaUploader`).
- **`CodeaNavHost.kt`** (849 líneas) es el archivo con más conflictos: haz ramas cortas y mezcla seguido.
- Hay pantallas y repositorios duplicados de la etapa sin backend; revisa los READMEs de `ui/screens/` y `data/` antes de borrar o reutilizar.

## 6. Ramas de git

| Rama | Estado |
|---|---|
| `main` | La buena. Última actividad: "Pantallas Rutinas-Admin" (2026-10-15). |
| `backend` | Ya contenida en `main` (mismo commit). |
| `documents-tab`, `estructura-curso`, `fotos-padres`, `my-room-and-chats-screens`, `pantallas-admin-checador`, `parent-tabs-scaffolding`, `presentation-build` | Ya contenidas en `main`; se pueden borrar. |
| `checador-menu` | **No** está en `main`. Su último commit dibuja el mapa del prototipo; ya no hace falta, porque el checador no usa mapa ni ubicación ([decisión #3](DECISIONES-PENDIENTES.md)). |

Verifícalo tú con `git branch -r --merged origin/main` y `git branch -r --no-merged origin/main`.

## 7. Qué hacer primero (si no sabes por dónde empezar)

1. Lee [`FALTANTES.md`](FALTANTES.md): lista lo que falta de la app, por rol, con el orden sugerido antes del congelamiento.
2. Aplica las migraciones pendientes en Aiven y recorre los tres roles en un teléfono; anota las fallas en [`RECORRIDO.md`](RECORRIDO.md).
3. Resuelve [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md): nada de eso se implementa sin aprobación.
4. Completa los huecos **[completar por el equipo]** de `docs/entrega/` y la [lista de la rúbrica](ENTREGA-3.md).
