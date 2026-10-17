# `auth`: perfil e invitaciones

Tablas: `app_user` (cuenta base), `invitation` (pendiente / aceptada / vencida).

| Endpoint | Rol | Qué hace |
|---|---|---|
| `GET /me` | cualquiera con cuenta | Rol, sala o hijos; si es docente, también turno y centro para el checador |
| `POST /auth/accept-invitation` | solo identidad (aún sin cuenta) | La primera vez, liga la cuenta con la invitación del admin |
| `POST /invitations` | admin | Invita a una persona nueva con un rol. 409 `EMAIL_TAKEN` / `ALREADY_HAS_ACCOUNT` |
| `GET /invitations` | admin | Lista con estado `PENDING`/`ACCEPTED`/`EXPIRED`; filtros `?status=` y `?role=` |

## Cómo funciona

Nadie entra solo por tener cuenta. Al dar de alta a una docente, tutor o padre se crea una invitación (vigencia: `INVITATION_DAYS`, 7 por defecto). Con la primera entrada se liga `firebase_uid` a `app_user`. El rol y lo que cada quien ve salen de Postgres.

## Estado

| Hecho | Pendiente |
|---|---|
| Invitaciones con vigencia, reenvío (renueva la vigencia), `/me` por rol, baja que revoca sesiones de Firebase | **Login propio con JWT** (hoy `firebase` o `dev`) ([decisión #1](../../../../docs/DECISIONES-PENDIENTES.md)): `password_hash`, `/auth/login`, `/auth/refresh`, `/auth/logout`, contraseña de más de 8 caracteres con hash |
| | **Correo real de invitación**: hoy solo se escribe en el log |
| | Bloqueo tras intentos fallidos y rate limiting en login |
| | Auditar altas, bajas y cambios de rol |

Pruebas: `tests/test_auth.py`, `tests/test_admin_extras.py` (invitaciones).
