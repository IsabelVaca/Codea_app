# API: plan de trabajo

> **Documento histórico (16–17 de octubre).** Describe el plan de entonces; el login, la conexión con la API y casi todas las pantallas que aquí figuran como pendientes ya se hicieron. Lo vigente está en [`../FALTANTES.md`](../FALTANTES.md) (qué falta), [`../ESTADO.md`](../ESTADO.md) (dónde estamos), [`../REQUISITOS.md`](../REQUISITOS.md) y [`../api/endpoints.md`](../api/endpoints.md).

Cinco bloques en el mismo orden que las fases de la app ([README](README.md#orden-de-trabajo)). Cada bloque termina con sus pruebas en verde y con [`../api/endpoints.md`](../api/endpoints.md) actualizado.

## 1. Login con JWT

Diseño completo en [04-api-login-jwt.md](04-api-login-jwt.md).

- [ ] Dependencias y configuración (`PyJWT`, `pwdlib[argon2]`, `JWT_SECRET`, duraciones, `SEED_PASSWORD`); quitar Firebase.
- [ ] `core/passwords.py` y `core/tokens.py`.
- [ ] Migración: columnas de contraseña y `token_version` en `app_user`, `code_hash` y `purpose` en `invitation`, tabla `refresh_token`, quitar `firebase_uid`.
- [ ] `get_current_user` con JWT; `revoke_all_sessions`.
- [ ] Endpoints: `POST /auth/login`, `/auth/activate`, `/auth/refresh`, `/auth/logout`, `POST /me/password`, `DELETE /me/sessions`, `POST /users/{id}/access-code`. Quitar `/auth/accept-invitation`.
- [ ] Altas de docentes, padres e infantes regresan `access_code`; `has_account` con `password_hash`.
- [ ] Seed con contraseña; `conftest.py` con tokens reales; pruebas nuevas.
- [ ] README de la API: sección "Entrar".

**Listo cuando:** los 3 usuarios del seed entran por `/auth/login` en Swagger, el refresh rota, la baja corta el acceso y las pruebas pasan.

## 2. Lo que pide el día de la docente

- [ ] `GET /classrooms/{id}/children`: agregar `has_note_today` (o `notes_today: int`) a `RoomChild`, para marcar en Mi sala al niño con nota del papá.
- [ ] Revisar con la app que `GET /me` traiga todo lo del checador (centro con coordenadas, radio, turno). Hoy sí lo trae; falta probarlo desde el teléfono.
- [ ] Pruebas: `daily-entries` desde la docente y lectura de notas en `GET /children/{id}/day` con rol `TEACHER`.

**Listo cuando:** la app de la docente hace todo su día contra la API sin datos de prueba.

## 3. Lo que pide el padre

Sin endpoints nuevos: Hoy, Calendario, Fotos, Documentos y Chat ya existen.

- [ ] Pruebas de alcance del padre: no ve fotos, documentos, calendario ni conversaciones de otro niño (404).
- [ ] Documentos por defecto al inscribir: al crear un infante, crear sus `child_document` desde una lista configurable (por ejemplo acta de nacimiento, CURP, cartilla de vacunación, comprobante de domicilio). Hoy solo existen los del seed.
- [ ] Decidir si el `ADMIN` puede subir documentos; si sí, permitirle `kind: DOCUMENT` en `POST /media/uploads` (hoy solo el `PARENT`).

**Listo cuando:** un padre nuevo, inscrito desde la app del admin, ve sus documentos pedidos y puede subir uno.

## 4. Lo que pide el admin

- [ ] El diálogo de la app con el código de invitación usa `access_code` (bloque 1).
- [ ] `POST /users/{id}/access-code` reemplaza a `resend-invitation` en la app (docentes y padres).
- [ ] Pruebas de docentes y padres: alta con `access_code`, reactivar, cambiar de turno, `EMAIL_TAKEN`.

**Listo cuando:** el admin da de alta a una docente y a un padre desde la app y los dos activan su cuenta con el código.

## 5. Completar el mínimo (P1)

- [x] `GET /announcements`: avisos visibles para quien pregunta. `TEACHER`: los del centro y los de su sala. `PARENT`: los del centro y los de las salas de sus hijos. `ADMIN`: todos. Hecho el 17 de octubre, del más nuevo al más viejo y con `?limit=`; **falta paginar por cursor**.
- [ ] `POST /announcements/{id}/read` y `unread_count` en la respuesta (opcional; la tabla `announcement_reads` de la propuesta). Hoy la app guarda los leídos en el teléfono.
- [ ] Probar `GET /announcements` contra Postgres (la prueba ya está en `test_announcement`, falta correrla).
- [ ] Recuperar contraseña por correo (`POST /auth/password/forgot`) **solo si** se decide tener correo; si no, el admin genera el código con `access-code`.
- [x] Pruebas de avisos y de su alcance (escritas en `test_announcement`).

**Listo cuando:** todas las pantallas P0 y P1 de [01-app-pantallas.md](01-app-pantallas.md) funcionan contra la API.

## Después del mínimo

Ordenado por lo que más se nota en la app:

| Tema | Endpoints o piezas | Pantalla que lo usa |
| --- | --- | --- |
| Chat en vivo | WebSocket en FastAPI, como la práctica 12 (token antes de aceptar, mensajes con `tipo`) | Chats de la docente y del padre |
| Notificaciones | Endpoints `?after=` / `?desde=` para que el WorkManager de la app pregunte cada 15 min, y SSE con la app abierta, como la práctica 8. El curso descarta FCM | Chat, fotos nuevas, incidentes, avisos |
| Worker | `app/workers/` con ARQ + Redis: quitar EXIF, miniaturas, correos, salida automática del checador | — |
| Correo | Invitaciones y recuperación por correo (SMTP o un proveedor) | Activar cuenta, Recuperar contraseña |
| Editar perfil | `PATCH /me` | Mi cuenta, Menú → Mi perfil |
| Borrado de fotos | `GET /media/deletion-requests`, resolverlas, `DELETE /media/{id}` | Admin (pantalla nueva) |
| Pedir documentos | `POST /children/{id}/document-requests` | Admin (pantalla nueva) |
| Ajustar rutina | `POST /classrooms/{id}/routine/moments`, `PATCH /routine-moments/{id}`, plantillas | `AjustarRutinaScreen` |
| Salida del niño | `POST /attendance/check-in`, `/check-out` con persona autorizada | Momento "Entrega a familias" |
| Check-out de la docente | `POST /staff-attendance/check-out` o cierre automático en el worker | Menú |
| Acuse de incidentes | `POST /reports/{id}/acknowledge` | Hoy del padre |
| Configuración del centro | Editar centro, radio y turnos | Admin (pantalla nueva) |
| Monitoreo | `/metrics`, Prometheus, Grafana | — |
| Seguridad | Ver [04, fase de seguridad](04-api-login-jwt.md#para-la-fase-de-seguridad) | — |

## Limpieza

- [ ] Borrar `app/modules/admins/router.py`, `service.py` y `schemas.py` (vacíos) o dejarlos con un comentario de para qué serán.
- [ ] Actualizar la [propuesta de arquitectura](https://claude.ai/artifact/K4CqXjp82tkRLfpKDRdV5H) con las diferencias de [03](03-api-estado.md#diferencias-con-la-propuesta-de-arquitectura) y el login con JWT.
- [ ] Borrar `Endpoints_API_Codea.docx.pdf` de la raíz (no está en git y tiene el mismo texto que `docs/api/endpoints-api-codea.pdf`). La referencia vigente es [`../api/endpoints.md`](../api/endpoints.md); el PDF queda como histórico.
