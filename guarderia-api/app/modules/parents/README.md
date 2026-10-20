# `parents`: padres de familia

Tablas: `parent`, `parent_child_rel` (un padre puede tener varios hijos y un niño, varios padres; parentesco `Relationship`).

| Endpoint | Rol | Qué hace |
|---|---|---|
| `GET /parents` | admin | Padres con parentesco, hijos, teléfono, correo y acceso a la app |
| `POST /parents` | admin | Alta + invitación; hijos por id. 422 `CHILD_INACTIVE` si alguno está de baja |
| `GET /parents/{id}` | admin | Detalle |
| `POST /parents/{id}/children` | admin | Liga más hijos a un padre que ya existe (`{child_ids, relationship?}`; sin parentesco usa el que ya tiene). Los ya ligados se ignoran. **No crea código nuevo**: el pendiente sigue sirviendo. 422 `CHILD_INACTIVE`, `RELATIONSHIP_REQUIRED`; queda en la bitácora (`USER_UPDATED`) |
| `POST /parents/{id}/deactivate` / `reactivate` | admin | Quita o devuelve el acceso |
| `POST /parents/{id}/resend-invitation` | admin | Genera un código nuevo y lo devuelve en `access_code` (no manda correo). 409 `INVITATION_ALREADY_ACCEPTED` si ya entró |

## Estado

| Hecho | Pendiente |
|---|---|
| Alta, baja, reactivación, relación con hijos | ~~Correo real de invitación~~: descartado, la invitación va por WhatsApp (decisión del equipo) |
| Ligar más hijos después del alta (20/oct; en la app: detalle del padre → "Vincular a otro niño") | Desligar un hijo |
| HN4: `GET /me` trae la lista de hijos (`parent.children[]`) y la app tiene el selector | Verlo con varios hijos en un teléfono |

Pruebas: `tests/test_admin.py` (`test_parents`, `test_link_more_children_to_a_parent`, `test_link_children_keeps_the_pending_code`, `test_inactive_children_cannot_be_linked`).
