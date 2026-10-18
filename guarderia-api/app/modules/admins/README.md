# `admins`: administradores y datos del centro

Tabla: `admin` (un `app_user` con rol admin).

| Endpoint | Rol | Qué hace |
|---|---|---|
| `GET /center` | admin | Nombre, ubicación, radio del checador y zona horaria |
| `PATCH /center` | admin | Cambia ubicación o radio. 422 `LOCATION_INVALID`, `LOCATION_INCOMPLETE`, `RADIUS_INVALID`, `TIMEZONE_INVALID` |

Para invitar a un nuevo admin se usa `POST /invitations` (módulo [`auth`](../auth/README.md)).

## Estado

| Hecho | Pendiente |
|---|---|
| Lectura y edición del centro con validaciones | Varios centros: hoy se asume uno |
| Solo admin (probado en `test_center_is_admin_only`) | Auditar los cambios al radio del checador |

Pruebas: `tests/test_admin_extras.py`.
