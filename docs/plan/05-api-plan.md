# API: plan de trabajo

> **Documento histórico (16–17 de octubre).** Describe el plan de entonces; el login, la conexión con la API y casi todas las pantallas que aquí figuran como pendientes ya se hicieron. Lo vigente está en [`../FALTANTES.md`](../FALTANTES.md) (qué falta), [`../ESTADO.md`](../ESTADO.md) (dónde estamos), [`../REQUISITOS.md`](../REQUISITOS.md) y [`../api/endpoints.md`](../api/endpoints.md).
>
> **Estado a 2026-10-20:** abajo están marcados con `[x]` los pasos hechos y tachados los que se descartaron; la tabla «Después del mínimo» tiene una columna con lo que pasó con cada tema.

Cinco bloques en el mismo orden que las fases de la app ([README](README.md#orden-de-trabajo)). Cada bloque termina con sus pruebas en verde y con [`../api/endpoints.md`](../api/endpoints.md) actualizado.

## 1. Login con JWT

Diseño completo en [04-api-login-jwt.md](04-api-login-jwt.md).

- [x] Dependencias y configuración (`PyJWT`, `pwdlib[argon2]`, `JWT_SECRET`, duraciones, `SEED_PASSWORD`); quitar Firebase.
- [x] `core/passwords.py` y `core/tokens.py`.
- [x] Migración: columnas de contraseña y `token_version` en `app_user`, `code_hash` y `purpose` en `invitation`, tabla `refresh_token`, quitar `firebase_uid` (`98306224d8b5`).
- [x] `get_current_user` con JWT; `revoke_all_sessions`.
- [x] Endpoints: `POST /auth/login`, `/auth/activate`, `/auth/refresh`, `/auth/logout`, `POST /me/password`, `DELETE /me/sessions`, `POST /users/{id}/access-code`. Quitar `/auth/accept-invitation`.
- [x] Altas de docentes, padres e infantes regresan `access_code`; `has_account` con `password_hash`.
- [x] Seed con contraseña; `conftest.py` con tokens reales; pruebas nuevas.
- [x] README de la API: sección "Entrar".

**Listo cuando:** los 3 usuarios del seed entran por `/auth/login` en Swagger, el refresh rota, la baja corta el acceso y las pruebas pasan.

## 2. Lo que pide el día de la docente

- [x] `GET /classrooms/{id}/children`: agregar `has_note_today` (o `notes_today: int`) a `RoomChild`, para marcar en Mi sala al niño con nota del papá. (Trae los dos y además `notes[]`.)
- [x] Revisar con la app que `GET /me` traiga todo lo del checador. Desde el 18/oct el checador ya no usa ubicación: usa el horario del centro, que también viene en `center`.
- [x] Pruebas: `daily-entries` desde la docente y notas del papá vistas por la docente (en Mi sala).

**Listo cuando:** la app de la docente hace todo su día contra la API sin datos de prueba.

## 3. Lo que pide el padre

Sin endpoints nuevos: Hoy, Calendario, Fotos, Documentos y Chat ya existen.

- [x] Pruebas de alcance del padre: no ve fotos, documentos, calendario ni conversaciones de otro niño (404) (`test_access_matrix.py`).
- [ ] Documentos por defecto al inscribir: al crear un infante, crear sus `child_document` desde una lista configurable (por ejemplo acta de nacimiento, CURP, cartilla de vacunación, comprobante de domicilio). Hoy solo existen los del seed.
- [x] Decidir si el `ADMIN` puede subir documentos: sí, el admin puede pedir los dos tipos en `POST /media/uploads`.

**Listo cuando:** un padre nuevo, inscrito desde la app del admin, ve sus documentos pedidos y puede subir uno.

## 4. Lo que pide el admin

- [x] El diálogo de la app con el código de invitación usa `access_code` (bloque 1), con botón para mandarlo por WhatsApp.
- [ ] `POST /users/{id}/access-code` reemplaza a `resend-invitation` en la app (docentes y padres). Para padres no hizo falta: `resend-invitation` ya regresa un código nuevo y la app lo muestra. Para docentes la app todavía no tiene cómo pedir otro código.
- [x] Pruebas de docentes y padres: alta con `access_code`, reactivar, cambiar de turno, `EMAIL_TAKEN`.

**Listo cuando:** el admin da de alta a una docente y a un padre desde la app y los dos activan su cuenta con el código.

## 5. Completar el mínimo (P1)

- [x] `GET /announcements`: avisos visibles para quien pregunta. `TEACHER`: los del centro y los de su sala. `PARENT`: los del centro y los de las salas de sus hijos. `ADMIN`: todos. Hecho el 17 de octubre, del más nuevo al más viejo y con `?limit=`; **falta paginar por cursor**.
- [ ] `POST /announcements/{id}/read` y `unread_count` en la respuesta (opcional; la tabla `announcement_reads` de la propuesta). Hoy la app guarda los leídos en el teléfono.
- [x] Probar `GET /announcements` contra Postgres (`test_announcement` pasa).
- [ ] ~~Recuperar contraseña por correo (`POST /auth/password/forgot`)~~ **Descartado:** no hay correo; el admin genera el código con `access-code`.
- [x] Pruebas de avisos y de su alcance (escritas en `test_announcement`).

**Listo cuando:** todas las pantallas P0 y P1 de [01-app-pantallas.md](01-app-pantallas.md) funcionan contra la API.

## Después del mínimo

Ordenado por lo que más se nota en la app:

| Tema | Endpoints o piezas | Pantalla que lo usa | Estado (20/oct) |
| --- | --- | --- | --- |
| Chat en vivo | WebSocket en FastAPI, como la práctica 12 (token antes de aceptar, mensajes con `tipo`) | Chats de la docente y del padre | Pendiente: la app pregunta con `?after=` cada 10 s |
| Notificaciones | Endpoints `?after=` / `?desde=` para que el WorkManager de la app pregunte cada 15 min, y SSE con la app abierta, como la práctica 8. El curso descarta FCM | Chat, fotos nuevas, incidentes, avisos | Pendiente |
| Worker | `app/workers/` con ARQ + Redis: quitar EXIF, miniaturas, correos, salida automática del checador | — | No se hizo (Redis está fuera del marco); el cierre automático del checador se resolvió sin worker |
| Correo | Invitaciones y recuperación por correo (SMTP o un proveedor) | Activar cuenta, Recuperar contraseña | **Descartado:** código y enlace por WhatsApp |
| Editar perfil | `PATCH /me` | Mi cuenta, Menú → Mi perfil | Pendiente |
| Borrado de fotos | ~~`GET /media/deletion-requests`, resolverlas~~ (quitado el 19/oct), `DELETE /media/{id}` | Padre (borra directo) | **Hecho** (19/oct) |
| Pedir documentos | `POST /children/{id}/document-requests` | Admin (pantalla nueva) | Pendiente |
| Ajustar rutina | `POST /classrooms/{id}/routine/moments`, `PATCH /routine-moments/{id}`, plantillas | `AjustarRutinaScreen` | Solo las plantillas (`/routine-templates`); ajustar el día no existe |
| Salida del niño | `POST /attendance/check-in`, `/check-out` con persona autorizada | Momento "Entrega a familias" | **Hecho de otra forma:** pase de salida (`/classrooms/{id}/departures`, `POST /children/{id}/departure`) y hora de entrada en el pase de lista (`arrived_at`) |
| Check-out de la docente | `POST /staff-attendance/check-out` o cierre automático en el worker | Menú | **Hecho:** `check-out` y cierre automático a la hora del centro, sin worker |
| Acuse de incidentes | `POST /reports/{id}/acknowledge` | Hoy del padre | Pendiente |
| Configuración del centro | Editar centro, radio y turnos | Admin (pantalla nueva) | **Hecho:** `PATCH /center` con horario (Ajustes → Horario); los turnos ya no se usan |
| Monitoreo | `/metrics`, Prometheus, Grafana | — | Solo `/health/live` y `/health/ready` |
| Seguridad | Ver [04, fase de seguridad](04-api-login-jwt.md#para-la-fase-de-seguridad) | — | En parte: ver el estado en 04 |

## Limpieza

- [x] ~~Borrar `app/modules/admins/router.py`, `service.py` y `schemas.py` (vacíos)~~: ya no están vacíos, tienen `GET/PATCH /center`.
- [ ] Actualizar la [propuesta de arquitectura](https://claude.ai/artifact/K4CqXjp82tkRLfpKDRdV5H) con las diferencias de [03](03-api-estado.md#diferencias-con-la-propuesta-de-arquitectura) y el login con JWT.
- [ ] Borrar `Endpoints_API_Codea.docx.pdf` de la raíz (hoy sí está en git y tiene el mismo texto que `docs/api/endpoints-api-codea.pdf`). La referencia vigente es [`../api/endpoints.md`](../api/endpoints.md); el PDF queda como histórico.
