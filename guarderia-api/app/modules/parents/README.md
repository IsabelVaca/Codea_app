# `parents`: padres de familia

Tablas: `parent`, `parent_child_rel` (un padre puede tener varios hijos y un niño, varios padres; parentesco `Relationship`).

| Endpoint | Rol | Qué hace |
|---|---|---|
| `GET /parents` | admin | Padres con parentesco, hijos, teléfono, correo y acceso a la app |
| `POST /parents` | admin | Alta + invitación; hijos por id |
| `GET /parents/{id}` | admin | Detalle |
| `POST /parents/{id}/deactivate` / `reactivate` | admin | Quita o devuelve el acceso |
| `POST /parents/{id}/resend-invitation` | admin | Renueva la vigencia de la invitación (no manda correo todavía) |

## Estado

| Hecho | Pendiente |
|---|---|
| Alta, baja, reactivación, relación con hijos | **Correo real de invitación** (tarea abierta) |
| | Editar los hijos de un padre después del alta |
| | HN4: que las respuestas del lado del padre (`/me`, `/children/...`) soporten bien varios hijos |

Pruebas: `tests/test_admin.py` (`test_parents`).
