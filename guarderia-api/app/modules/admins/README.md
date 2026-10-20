# `admins`: administradores y datos del centro

Tabla: `admin` (un `app_user` con rol admin).

| Endpoint | Rol | Qué hace |
|---|---|---|
| `GET /center` | admin | Nombre, ubicación, radio, zona horaria y horario (entrada, salida, cierre automático y días de trabajo). 409 `CENTER_MISSING` |
| `PATCH /center` | admin | Cambia lo que se mande: nombre, ubicación, radio, zona horaria u horario. 422 `LOCATION_INVALID`, `LOCATION_INCOMPLETE`, `RADIUS_INVALID`, `TIMEZONE_INVALID`, `SCHEDULE_INVALID`, `WORKDAYS_INVALID` |

Para invitar a un nuevo admin se usa `POST /invitations` (módulo [`auth`](../auth/README.md)).

## Estado

| Hecho | Pendiente |
|---|---|
| Lectura y edición del centro y de su horario, con validaciones | Varios centros: hoy se asume uno |
| Solo admin (probado en `test_center_is_admin_only`) | |
| Cada cambio queda en la bitácora (`CENTER_UPDATED`, con ubicación, radio y horario) | |

Pruebas: `tests/test_admin_extras.py`.
