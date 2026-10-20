# `audit`: bitácora de quién hizo qué (RF-14)

Tabla `audit_log`: quién (`actor_id`, o `actor_email` si todavía no se sabe quién es), qué (`action`), sobre qué (`entity_type` + `entity_id`), datos para entender el hecho (`detail`), desde dónde (`ip`) y cuándo (`created_at`).
Solo se agregan filas: la API no tiene forma de editarlas ni de borrarlas (`PUT`, `PATCH`, `DELETE` y `POST` sobre `/audit-log` dan 405).

| Endpoint | Rol | Qué hace |
|---|---|---|
| `GET /audit-log` | admin | Lo más nuevo primero. Filtros `?action=` y `?actor_id=`; `?limit=` (1 a 200, por omisión 50) y `?cursor=` para paginar |

## Qué se registra

| Acción (`action`) | Cuándo | `detail` |
|---|---|---|
| `LOGIN_OK` / `LOGIN_FAILED` | cada intento de entrar | — |
| `LOGIN_LOCKED` | el fallo que bloquea el correo (una sola vez por bloqueo) | — |
| `ACCOUNT_ACTIVATED` / `ACTIVATION_FAILED` | activar con código | `purpose`; en el fallo, `locked` |
| `PASSWORD_CHANGED`, `SESSIONS_CLOSED` | cuenta propia | `revoked` (cuántas sesiones) |
| `TOKEN_REUSE_DETECTED` | llegó un refresh token ya rotado (posible robo) | — |
| `ACCESS_CODE_ISSUED`, `USER_INVITED` | el admin genera un código o invita | `purpose` o `role` |
| `USER_CREATED`, `USER_UPDATED`, `USER_DEACTIVATED`, `USER_REACTIVATED` | altas, cambios y bajas de docentes y padres (también ligar más hijos a un padre) | `role`, sala, turno, hijos (`linked_children`) |
| `CHILD_ENROLLED`, `CHILD_UPDATED`, `CHILD_DEACTIVATED`, `CHILD_REACTIVATED` | inscripción, cambio de sala y bajas | sala |
| `CHILD_HEALTH_UPDATED` | la familia o el admin cambian la salud del niño | `fields` (qué campos; nunca su contenido) |
| `PICKUP_ADDED`, `PICKUP_REMOVED` | el admin agrega o quita una persona autorizada | `pickup_id` |
| `CHILD_DEPARTED`, `CHILD_DEPARTURE_UNDONE` | la docente registra o quita la salida del niño | `picked_up_by` |
| `CENTER_UPDATED` | cambios del centro (`PATCH /center`) | campos, ubicación, radio y horario |
| `MENU_SAVED`, `MENU_DELETED` | el admin guarda o quita el menú del día | `date` |
| `MEDIA_DELETED_BY_FAMILY` | un padre borra una foto | `children` si salían niños de otras familias |

`MEDIA_DELETION_APPROVED` y `MEDIA_DELETION_REJECTED` siguen en `service.py` y en filas viejas, pero ya no se generan: la revisión de borrado se quitó el 19/oct.

**Nunca se guardan** contraseñas, códigos de acceso, tokens ni sus hashes (hay una prueba que lo verifica en `tests/test_security.py`).
El correo que se escribió en un login fallido sí queda (`actor_email`): hace falta para investigar un ataque y se considera dato personal (ver la clasificación de datos en `docs/entrega/`).

## Cómo se agrega una acción nueva

1. Una constante en `service.py` (los nombres viven en un solo lugar).
2. En el `service.py` del módulo: `audit.record(db, audit.NOMBRE, actor_id=..., entity_type=..., entity_id=..., detail={...})` **antes** del `commit` (se guarda junto con lo que la causó).
3. Si el hecho termina en un error (como un login fallido), hacer `await db.commit()` antes de lanzar el `ApiError`, o la fila se pierde.
4. Una prueba en `tests/test_security.py`.

## Lo que todavía no se registra

Lectura de fotos y documentos, reportes y mensajes (esas tablas ya guardan quién las creó: `recorded_by`, `marked_by`…), y cambios de salas y plantillas de rutina.
