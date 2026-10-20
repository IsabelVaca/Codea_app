# `app/core/`

Lo compartido por todos los módulos.

| Archivo | Qué hace |
|---|---|
| `config.py` | `Settings` (pydantic-settings): lee `.env`. `JWT_SECRET` es obligatorio (mínimo 32 caracteres) y no tiene valor por omisión |
| `db.py` | Motor asíncrono, `Db` (una sesión por petición), `Base`, mixins `TimestampMixin`/`IdMixin`, `str_enum` (enums guardados como texto) |
| `passwords.py` | Argon2id con `pwdlib`; corre en un hilo aparte porque tarda a propósito; compara contra un hash falso si el correo no existe |
| `tokens.py` | JWT HS256 (`PyJWT`, algoritmo fijado al verificar), refresh token aleatorio, hash SHA-256 y códigos `XXXX-XXXX`; `normalize_code` compara el código solo con letras y números (sin guion, espacios ni invisibles) |
| `security.py` | `CurrentUser` (persona con roles), `get_current_user` (decodifica el JWT), `revoke_all_sessions`, `require_role`, atajos `AdminUser`, `TeacherUser`, `ParentUser`, `StaffUser`, `AnyUser`, `AdminOrParent`, y el permiso por registro (`can_see_child`, `get_child_in_scope`, `ensure_classroom_access`) |
| `errors.py` | `ApiError(status, code, title, detail, **extra)` y el formato Problem Details para todos los errores |
| `rules.py` | Validaciones iguales a las de `domain/` en la app (nombre, teléfono, correo, cupo, edad, contraseña); `clean_email` quita espacios y caracteres invisibles del correo (alta, login y activación) |
| `clock.py` | `now()`, `local_now(tz)`, `local_today(tz)`; las pruebas fijan el reloj aquí |
| `storage.py` | Cliente S3: URLs firmadas de subida (5 min) y de descarga (10 min), bucket |
| `pagination.py` | Paginación por cursor (`Page`, `parse_cursor`) |
| `registry.py` | Importa todos los modelos para que Alembic y el seed vean todas las tablas |
| `schemas.py` | Esquemas compartidos (por ejemplo `ClassroomRef`) |

## Cómo funciona la identidad

1. `get_current_user` lee `Authorization: Bearer <access_token>` y decodifica el JWT (`401 UNAUTHENTICATED` si falta, `TOKEN_INVALID` si la firma, el tipo o el usuario no valen, `TOKEN_EXPIRED` si venció).
2. Busca a la persona por `sub` en `app_user`. De baja: `403 USER_INACTIVE`. Si su `token_version` ya no coincide con el `tv` del token (cambió su contraseña, o la dieron de baja y la reactivaron): `401 TOKEN_INVALID`.
3. `require_role(...)` revisa los roles que salen de Postgres (`admin`, `teacher`, `parent`), no del token.
4. El servicio revisa el alcance por registro. Fuera de alcance: **404**, para no revelar que existe.

La vigencia del token se compara con `clock.now()` (no con el reloj de PyJWT) para que las pruebas puedan mover la hora.

## Pendiente

Ya están el límite de intentos (`modules/auth/throttle.py`), las cabeceras de seguridad y el CORS cerrado (`main.py`) y la bitácora (`audit.record` en `modules/audit`). Falta:

- **Rotación de la llave del JWT** (`kid` en el header) y llave en Docker secrets.
- **Logs estructurados** sin datos personales.
