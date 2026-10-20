# Guía para continuar el proyecto

Si llegas nuevo, lee en este orden: este archivo → [`ESTADO.md`](ESTADO.md) → [`MARCO-TECNICO.md`](MARCO-TECNICO.md) → [`REQUISITOS.md`](REQUISITOS.md) → el README de la carpeta donde vas a trabajar.

Ya existían y **son la referencia para el cómo** (no los dupliques): [`estilo.md`](estilo.md) (convenciones), [`plan/`](plan/README.md) (pantallas, conexión de la app, estado de la API, login JWT, tareas) y [`api/endpoints.md`](api/endpoints.md) (las 84 rutas de la API).

## 1. Reglas que no se negocian

1. **El marco técnico es el de <https://startdroid.pages.dev/>.** Nada fuera de esa lista sin aprobación; se anota en [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md).
2. **Firebase está excluido** para notificaciones y tiempo real. El login ya es propio (JWT, de la rama `feature/login-jwt`, ya en `main`); no agregues más uso de Firebase.
3. Texto de la interfaz en español; comentarios del código en inglés en lo que ya existe (los más recientes están en español: sigue el estilo del archivo vecino).
4. Dependencias en un solo sentido: `ui/screens` → `ui/state` → `data` → `domain`. `domain/` no importa Android.
5. La API repite las reglas de `domain/` (`guarderia-api/app/core/rules.py`): si cambias una regla, cámbiala en los dos lados y en sus pruebas.

## 2. Levantar todo

**Backend** (solo si vas a trabajar en `guarderia-api/` o correr `pytest`; la app usa el servidor compartido, ver abajo)
```bash
cd guarderia-api
cp .env.dev .env                # valores de desarrollo, sin secretos reales
docker compose up -d --build
docker compose exec api alembic upgrade head
docker compose exec api python -m app.seed
```
API en `http://localhost:8000/api/v1`, Swagger en `/docs`.

`.env.dev` ya trae un `JWT_SECRET` de prueba. Si partes de `.env.example`, llena `JWT_SECRET` (mínimo 32 caracteres; la API no arranca sin él): `python3 -c "import secrets; print(secrets.token_urlsafe(48))"`.

**Entrar:** el seed deja a todos con la contraseña `codea1234` (`SEED_PASSWORD`). Correos: `admin@oruga.test`, `karla.robles@oruga.test` (docente), `laura.iglesias@oruga.test` (madre). En Swagger haz `POST /auth/login`, copia el `access_token` y pégalo en **Authorize** (dura 5 minutos). En la app, esos mismos correo y contraseña en la pantalla de inicio de sesión.

**Personas nuevas:** el admin las da de alta, la app muestra un **código de acceso** (`XXXX-XXXX`, solo se ve una vez) y la persona lo usa en "Es mi primera vez: activar mi cuenta".

**App**
```bash
./gradlew installDebug
```
- Lo normal: el servidor compartido (siguiente apartado), con `API_BASE_URL` en `local.properties`.
- Contra tu backend local, en emulador: sin `API_BASE_URL` en `local.properties` la app apunta a `http://10.0.2.2:8000/api/v1/`.
- Contra tu backend local, en celular físico por USB: compila con `-PapiBaseUrl=http://localhost:8000/api/v1/` (o ponlo en `local.properties`) y corre `adb reverse tcp:8000 tcp:8000 && adb reverse tcp:9000 tcp:9000`; sin eso la app apunta a `10.0.2.2` (emulador). Detalle en [`entrega/manual-instalacion.md`](entrega/manual-instalacion.md).

**Sin levantar el backend: usar el servidor compartido (`codeaapp.site`)**

Desde el 19/oct hay una API pública con HTTPS (túnel de Cloudflare, [decisión #7](DECISIONES-PENDIENTES.md)). Es la que usa la app, en emulador o celular, sin Docker ni `adb reverse`. Corre con el proyecto `codea-prod` en una Mac: base en Aiven, fotos en SeaweedFS y respaldo diario de las dos desde el 20/oct.

| Qué | Dirección |
|---|---|
| API | `https://api.codeaapp.site/api/v1/` |
| Swagger | `https://api.codeaapp.site/docs` |
| Fotos (la usa la app sola, con URLs firmadas) | `https://archivos.codeaapp.site` |

1. En `local.properties` (no se versiona) pon `API_BASE_URL=https://api.codeaapp.site/api/v1/` y corre `./gradlew installDebug`. Para volver a tu backend local, borra la línea. Un APK de release ya apunta ahí sin configurar nada.
2. **Cuenta:** aquí no existe la contraseña `codea1234`. Todas las cuentas de prueba usan una contraseña común que **no se escribe en el repo (es público)**: pídela en el grupo del equipo. Los correos por rol, qué niño es de quién y qué hay en cada sala están en [`DATOS-DE-PRUEBA.md`](DATOS-DE-PRUEBA.md). Usa una cuenta distinta a la de los demás para no pisar el check-in ni la rutina del día.
3. Comprueba que está vivo: `https://api.codeaapp.site/api/v1/health/ready` debe responder `{"status":"ok","database":"ok"}`.

Lo que hay que saber:

- **La base es compartida.** Lo que crees o borres lo ven todos. Usa datos de prueba, nunca de familias reales. Antes del piloto con el centro, este servidor pasa a ser de pruebas y producción arranca con una base limpia ([plan de implantación](entrega/plan-implantacion.md)).
- **Corre en una computadora del equipo.** Si está apagada o sin red, la API no responde. Avisa en el grupo antes de contar con ella para una demostración.
- **El backend del servidor no cambia con tu rama.** Un cambio en `guarderia-api/` llega al servidor solo cuando el responsable hace merge, `git pull` y lo vuelve a levantar. Si tu cambio de la app necesita un endpoint nuevo, pruébalo primero con tu backend local.
- **Nunca pidas ni compartas** `.env.prod` ni el token del túnel: dan acceso a la base y a la cuenta de Cloudflare.
- Cómo se levanta y se actualiza el servidor: [`guarderia-api/README.md`](../guarderia-api/README.md#producción-en-codeaappsite).

**Pruebas**
```bash
./gradlew test                                     # 140 pruebas JVM
cd guarderia-api && .venv/bin/pytest               # 343 pruebas de la API (Postgres real; con POSTGRES_PASSWORD si cambiaste la contraseña)
```
Las de la API, siempre contra el Postgres local de `docker compose`, nunca contra el servidor compartido. Otras formas de correrlas: [`guarderia-api/tests/README.md`](../guarderia-api/tests/README.md).

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
- **`CodeaNavHost.kt`** (unas 1270 líneas) es el archivo con más conflictos: haz ramas cortas y mezcla seguido.
- Ya no hay pantallas ni repositorios en memoria de la etapa sin backend (se borraron el 18/oct); solo quedan los `*SampleData.kt` del padre, con los modelos de la interfaz. Revisa los READMEs de `ui/screens/` y `data/` antes de borrar o reutilizar.
- **Caché en Room:** las listas del admin y las pantallas del padre y de la docente muestran primero lo último guardado (`data/offline/ApiCache.kt`) y luego lo nuevo de la API. La llave lleva el id del usuario y todo se borra al cerrar sesión. Si cambias un DTO y lo guardado ya no se puede leer, la caché regresa `null` y la pantalla espera a la API.

## 6. Ramas de git

| Rama | Estado |
|---|---|
| `main` | La buena. Última actividad: merge del PR #17 (estilo y caché de la docente, 2026-10-20). Los PR #11 a #17 ya están dentro. |
| `feature/*` (menos `feature/pagos`), `fix-*`, `docs/servidor-tunel`, `documents-tab`, `estructura-curso`, `fotos`, `fotos-padres`, `integracion`, `invitacion-whatsapp`, `mi-sala-pase-de-lista`, `my-room-and-chats-screens`, `pantallas-admin-checador`, `parent-tabs-scaffolding`, `presentation-build` | Ya contenidas en `main`; se pueden borrar. |
| `backend` | **No** está en `main`: dos commits del 17/oct (un plan de acción, íconos y un PDF de endpoints). Revisar si algo sirve antes de borrarla. |
| `feature/pagos` | **No** está en `main` y no se integra: los pagos (CRF-01) quedaron fuera de alcance. |
| `checador-menu` | **No** está en `main`. Su último commit dibuja el mapa del prototipo; ya no hace falta, porque el checador no usa mapa ni ubicación ([decisión #3](DECISIONES-PENDIENTES.md)). |

Verifícalo tú con `git branch -r --merged origin/main` y `git branch -r --no-merged origin/main`.

## 7. Qué hacer primero (si no sabes por dónde empezar)

1. Lee [`FALTANTES.md`](FALTANTES.md): lista lo que falta de la app, por rol, con el orden sugerido para los días antes de la entrega (22/oct, 23:59). Las funciones nuevas se congelaron el 19/oct.
2. Recorre los tres roles en un teléfono contra el servidor compartido; anota las fallas en [`RECORRIDO.md`](RECORRIDO.md). Las 13 migraciones ya están aplicadas en Aiven (head `d74a0f6c4e15`).
3. Resuelve [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md): nada de eso se implementa sin aprobación.
4. Completa los huecos **[completar por el equipo]** de `docs/entrega/` y la [lista de la rúbrica](ENTREGA-3.md).
