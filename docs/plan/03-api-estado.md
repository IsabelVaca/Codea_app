# API: estado actual

La API ya tiene **todos los endpoints de la primera entrega** del PDF: 56 en 12 módulos, con su lista en [`../api/endpoints.md`](../api/endpoints.md). Las **30 pruebas pasan** contra Postgres real (corridas el 16 de octubre de 2026). Lo que falta no son endpoints de la entrega, sino lo que hay alrededor: el login propio, quién recibe los avisos y las invitaciones, el worker y lo que piden las pantallas nuevas.

## Lo que está hecho

| Módulo | Endpoints | Pruebas | Nota |
| --- | --- | --- | --- |
| `auth` | `GET /me`, `POST /auth/accept-invitation` | 6 | Funciona con Firebase o con `dev:<correo>`; se reemplaza por JWT |
| `classrooms` | 7 | 4 | Ocupación calculada, staff con principal, borrar con `409` si tiene niños |
| `teachers` | 7 (incluye `GET /shifts`) | 1 | Alta crea invitación; baja y reactivación |
| `children` | 6 | 2 | `birth_date` y edad calculada; el tutor se registra al inscribir |
| `parents` | 6 | 1 | Hijos por id; reenviar invitación solo renueva la vigencia |
| `staff_attendance` | 3 | 4 | Reglas de `CheckInRules` del lado del servidor; checar arranca la rutina |
| `attendance` | 2 | 1 + 1 | Presente o ausente y calendario del mes |
| `routines` | 2 | 1 | Un solo momento activo por sala y día (índice único parcial + `SELECT … FOR UPDATE`) |
| `reports` | 4 | 1 + 1 | Reglas de `ReportValidator`; resumen del día y pantalla Hoy |
| `media` | 4 | 3 | URL firmada para subir (5 min) y para leer (10 min); feed paginado |
| `documents` | 5 | 1 | Documentos con su archivo y personas autorizadas |
| `messaging` | 6 | 3 | Chat por niño con `client_id`, `after` y leídos; avisos (solo publicar); notas del papá |

"1 + 1" = una prueba propia y otra que lo cubre desde otro flujo. La cobertura es pareja en las reglas (checador, reportes, rutina) y delgada en padres, docentes, chat y documentos.

## Lo que está a medias

| Tema | Qué pasa hoy | Qué falta |
| --- | --- | --- |
| Login | Firebase (Google) o `dev:<correo>` | Login propio con correo, contraseña y JWT ([04](04-api-login-jwt.md)) |
| Invitaciones | Se crean con el correo y una vigencia de 7 días, **sin código**; la persona entra si su correo de Google coincide | Código de invitación; mostrarlo al admin o mandarlo por correo |
| Reenviar invitación | Solo para padres, y solo renueva la vigencia | También para docentes; regresar el código nuevo |
| Avisos | `POST /announcements` guarda el aviso | Nadie lo puede leer: falta `GET /announcements` |
| Aviso a coordinación | Se guarda y se anota en el log | Push o correo al `ADMIN` (con el worker) |
| Notas del papá | Se guardan y salen en `GET /children/{id}/day` | Mi sala no indica que hay nota nueva |
| Documentos pedidos | Solo los que crea el seed | Lista por defecto al inscribir, o un endpoint para pedirlos |
| Documentos del admin | `PUT /children/{id}/documents/{doc_id}` acepta `ADMIN` | El admin no puede pedir permiso para subir `kind: DOCUMENT` (solo el `PARENT`); decidir si el admin sube documentos |
| Fotos | Se guardan tal como llegan | Quitar EXIF (ubicación GPS), miniaturas y validar que sí sea imagen: necesita el worker |
| Módulo `admins` | Solo el modelo; `router.py`, `service.py` y `schemas.py` vacíos | Nada para el mínimo (los admins vienen del seed). Borrar los archivos vacíos o usarlos para `GET/POST /admins` después |
| `app/workers/` y `monitoring/` | Vacíos | Fase 2 |

## Lo que no existe

Para el mínimo (las pantallas P0 y P1 de [01](01-app-pantallas.md)):

- Login, refresh, logout, activar cuenta, cambiar y recuperar contraseña.
- `GET /announcements` (y marcar como leído, opcional).
- Código de invitación en las respuestas del admin y `POST /teachers/{id}/resend-invitation`.
- Indicador de nota del papá en `GET /classrooms/{id}/children`.

Para después (la sección "Para después" del PDF): `PATCH /me`, dispositivos para push, `GET /me/documents`, invitaciones genéricas, check-out del checador, entrada y salida de niños con persona autorizada, ajustar rutina y plantillas, acuse de incidentes, transcripción de dictado, salud y consentimientos, pedir documentos, solicitudes de borrado para el admin, borrar fotos, medicamentos, calendario de eventos, exportaciones y auditoría. También el `Idempotency-Key` y el `If-Match` que propone la arquitectura (hoy la idempotencia se resuelve por registro: checada por día, `client_id` en el chat, una solicitud de borrado por foto).

## Diferencias con la propuesta de arquitectura

El código sigue el diagrama ER y el PDF, que son posteriores a la [propuesta](https://claude.ai/artifact/K4CqXjp82tkRLfpKDRdV5H). Conviene actualizar la propuesta en estos puntos:

| Tema | La propuesta dice | El código hace |
| --- | --- | --- |
| Identidad | Firebase Auth con Google; la API verifica el ID token | Hoy Firebase o modo dev; **se decidió cambiar a login propio con JWT** |
| Invitación | `code_hash` en `invitations`; `POST /auth/accept-invitation` con el código | Sin código: se compara el correo verificado de Google |
| Ids | UUID en todas las tablas | `int` en casi todo; `uuid` solo en personas (`app_user`, `teacher`, `parent`, `admin`) |
| Roles | Tabla `user_roles` | Una tabla por rol (`admin`, `teacher`, `parent`) con `user_id` como llave |
| Familias | `families`, `guardians`, `child_guardians` | `parent` + `parent_child_rel` (con `relationship` e `is_primary`) |
| Sala del niño | `enrollments` con fechas | `child.classroom_id`; se cambia con `PATCH /children/{id}` |
| Sala de la docente | `classroom_staff` | `teacher.classroom_id` + `teacher.is_primary` (una sala por docente) |
| Bitácora | `daily_entries` con JSONB por tipo | `daily_summary` con columnas fijas (ánimo, comida, pañales, siesta, nota) |
| Reporte con foto | `report_media` | `report.media_id` (una foto por reporte) |
| Padres | No tenía endpoints | Módulo `parents` con alta, baja, reactivación y reenvío |
| Borrado | `deleted_at` en todo | `deleted_at` solo en `classroom`; personas y niños usan `status` |
| Errores | Problem Details + `code` | Igual |
| Worker, Redis, monitoreo, túnel | En la fase 1 | Todavía no existen |
