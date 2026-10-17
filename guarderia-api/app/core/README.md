# `app/core/`

Lo compartido por todos los módulos.

| Archivo | Qué hace |
|---|---|
| `config.py` | `Settings` (pydantic-settings): lee `.env`. `AUTH_MODE=dev` solo se permite con `ENV=dev` (lo valida al arrancar) |
| `db.py` | Motor asíncrono, `Db` (una sesión por petición), `Base`, mixins `TimestampMixin`/`IdMixin`, `str_enum` (enums guardados como texto) |
| `security.py` | `Identity` (quién dice el token) y `CurrentUser` (persona con roles), `require_role`, atajos `AdminUser`, `TeacherUser`, `ParentUser`, `StaffUser`, `AnyUser`, `AdminOrParent`, y el permiso por registro (`can_see_child`, `get_child_in_scope`, `ensure_classroom_access`) |
| `errors.py` | `ApiError(status, code, title, detail, **extra)` y el formato Problem Details para todos los errores |
| `rules.py` | Validaciones iguales a las de `domain/` en la app (nombre, teléfono, correo, cupo, edad) |
| `clock.py` | `now()`, `local_now(tz)`, `local_today(tz)`; las pruebas fijan el reloj aquí |
| `storage.py` | Cliente S3: URLs firmadas de subida (5 min) y de descarga (10 min), bucket |
| `pagination.py` | Paginación por cursor (`Page`, `parse_cursor`) |
| `registry.py` | Importa todos los modelos para que Alembic y el seed vean todas las tablas |
| `schemas.py` | Esquemas compartidos (por ejemplo `ClassroomRef`) |

## Cómo funciona la identidad

1. `get_identity` lee `Authorization: Bearer ...`. Con `AUTH_MODE=dev` acepta `dev:<correo>`; con `firebase` verifica el token con `firebase-admin`.
2. `get_current_user` busca a la persona por `firebase_uid` en `app_user`. Sin cuenta: 403 `INVITATION_REQUIRED`. Desactivada: 403 `USER_INACTIVE`.
3. `require_role(...)` revisa los roles que salen de Postgres (`admin`, `teacher`, `parent`), no del token.
4. El servicio revisa el alcance por registro. Fuera de alcance: **404**, para no revelar que existe.

## Pendiente

- **JWT propio** en lugar de Firebase ([decisión #1](../../../docs/DECISIONES-PENDIENTES.md)): `get_identity` pasaría a verificar un JWT firmado por la propia API; `app_user` necesitaría `password_hash`.
- **Rate limiting y cabeceras de seguridad:** un middleware nuevo (y el `CORSMiddleware` configurado) en `main.py`.
- **Auditoría:** helper `audit(db, current, action, entity, id)` que escriba en `audit_log`.
- **Logs estructurados** sin datos personales.
