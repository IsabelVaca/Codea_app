# `teachers`: docentes y turnos

Tablas: `teacher` (con su sala y turno), `shift`.

| Endpoint | Rol | Qué hace |
|---|---|---|
| `GET /teachers` | admin | Docentes con sala, turno, hora de checada de hoy, baja y niños en su sala |
| `POST /teachers` | admin | Alta + invitación. 422 `NAME_REQUIRED`, `PHONE_INVALID`, `EMAIL_INVALID` |
| `GET /teachers/{id}` | admin | Detalle |
| `PATCH /teachers/{id}` | admin | Cambia de sala o de turno |
| `POST /teachers/{id}/deactivate` | admin | Baja: quita el acceso y cierra sesiones |
| `POST /teachers/{id}/reactivate` | admin | Reactiva |
| `GET /shifts` | admin | Turnos del centro (para el formulario de alta) |

## Estado

| Hecho | Pendiente |
|---|---|
| CRUD con baja y reactivación, turnos | Crear y editar turnos (hoy solo se listan) |
| La baja cierra sus sesiones con `revoke_all_sessions` (sube `token_version` y revoca sus refresh tokens) | |

Pruebas: `tests/test_admin.py` (`test_teacher_rules_and_changes`).
