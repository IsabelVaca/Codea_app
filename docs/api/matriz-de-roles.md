# Matriz de roles

Quién puede llamar a cada endpoint. **Se genera leyendo las dependencias reales de cada ruta** (`require_role`) y se comprueba en [`tests/test_access_matrix.py`](../../guarderia-api/tests/test_access_matrix.py): sin token toda ruta privada responde `401`, y cada rol que no está permitido recibe `403 FORBIDDEN`. Si se agrega un endpoint sin protegerlo, esas pruebas fallan.

- ✅ puede llamarlo · — recibe `403`
- **Sesión** = basta con tener cuenta; el servicio decide por registro (un padre solo ve a sus hijos, una docente solo a los de su sala, y lo que está fuera de alcance responde `404`, no `403`, para no revelar que existe).
- **Pública** = no pide sesión (las de entrada están limitadas por intentos, ver [`auth`](../../guarderia-api/app/modules/auth/README.md)).

| Método | Ruta | Admin | Docente | Padre | Tipo |
|---|---|:-:|:-:|:-:|---|
| GET | `/announcements` | ✅ | ✅ | ✅ | Sesión |
| POST | `/announcements` | ✅ | — | — | Por rol |
| PUT | `/attendance/{child_id}` | ✅ | ✅ | — | Por rol |
| GET | `/audit-log` | ✅ | — | — | Por rol |
| POST | `/auth/activate` | ✅ | ✅ | ✅ | Pública |
| POST | `/auth/login` | ✅ | ✅ | ✅ | Pública |
| POST | `/auth/logout` | ✅ | ✅ | ✅ | Pública |
| POST | `/auth/refresh` | ✅ | ✅ | ✅ | Pública |
| GET | `/center` | ✅ | — | — | Por rol |
| PATCH | `/center` | ✅ | — | — | Por rol |
| GET | `/children` | ✅ | — | — | Por rol |
| POST | `/children` | ✅ | — | — | Por rol |
| GET | `/children/{child_id}` | ✅ | ✅ | ✅ | Sesión |
| PATCH | `/children/{child_id}` | ✅ | — | — | Por rol |
| GET | `/children/{child_id}/attendance` | ✅ | ✅ | ✅ | Sesión |
| GET | `/children/{child_id}/authorized-pickups` | ✅ | ✅ | ✅ | Sesión |
| POST | `/children/{child_id}/authorized-pickups` | ✅ | — | ✅ | Por rol |
| DELETE | `/children/{child_id}/authorized-pickups/{person_id}` | ✅ | — | ✅ | Por rol |
| POST | `/children/{child_id}/daily-entries` | ✅ | ✅ | — | Por rol |
| GET | `/children/{child_id}/day` | ✅ | ✅ | ✅ | Sesión |
| POST | `/children/{child_id}/deactivate` | ✅ | — | — | Por rol |
| GET | `/children/{child_id}/documents` | ✅ | — | ✅ | Por rol |
| PUT | `/children/{child_id}/documents/{document_id}` | ✅ | — | ✅ | Por rol |
| POST | `/children/{child_id}/notes` | — | — | ✅ | Por rol |
| POST | `/children/{child_id}/reactivate` | ✅ | — | — | Por rol |
| GET | `/classrooms` | ✅ | — | — | Por rol |
| POST | `/classrooms` | ✅ | — | — | Por rol |
| DELETE | `/classrooms/{classroom_id}` | ✅ | — | — | Por rol |
| GET | `/classrooms/{classroom_id}` | ✅ | — | — | Por rol |
| PATCH | `/classrooms/{classroom_id}` | ✅ | — | — | Por rol |
| GET | `/classrooms/{classroom_id}/children` | ✅ | ✅ | — | Por rol |
| GET | `/classrooms/{classroom_id}/routine` | ✅ | ✅ | — | Por rol |
| PUT | `/classrooms/{classroom_id}/routine-template` | ✅ | — | — | Por rol |
| PUT | `/classrooms/{classroom_id}/staff` | ✅ | — | — | Por rol |
| GET | `/conversations` | ✅ | ✅ | ✅ | Sesión |
| GET | `/conversations/{conversation_id}/messages` | ✅ | ✅ | ✅ | Sesión |
| POST | `/conversations/{conversation_id}/messages` | ✅ | ✅ | ✅ | Sesión |
| POST | `/conversations/{conversation_id}/read` | ✅ | ✅ | ✅ | Sesión |
| GET | `/health/live` | ✅ | ✅ | ✅ | Pública |
| GET | `/health/ready` | ✅ | ✅ | ✅ | Pública |
| GET | `/invitations` | ✅ | — | — | Por rol |
| POST | `/invitations` | ✅ | — | — | Por rol |
| GET | `/me` | ✅ | ✅ | ✅ | Sesión |
| POST | `/me/password` | ✅ | ✅ | ✅ | Sesión |
| DELETE | `/me/sessions` | ✅ | ✅ | ✅ | Sesión |
| GET | `/media` | ✅ | ✅ | ✅ | Sesión |
| GET | `/media/deletion-requests` | ✅ | — | — | Por rol |
| POST | `/media/deletion-requests/{request_id}/approve` | ✅ | — | — | Por rol |
| POST | `/media/deletion-requests/{request_id}/reject` | ✅ | — | — | Por rol |
| POST | `/media/uploads` | ✅ | ✅ | ✅ | Sesión |
| POST | `/media/{media_id}/complete` | ✅ | ✅ | ✅ | Sesión |
| POST | `/media/{media_id}/deletion-request` | — | — | ✅ | Por rol |
| GET | `/parents` | ✅ | — | — | Por rol |
| POST | `/parents` | ✅ | — | — | Por rol |
| GET | `/parents/{parent_id}` | ✅ | — | — | Por rol |
| POST | `/parents/{parent_id}/deactivate` | ✅ | — | — | Por rol |
| POST | `/parents/{parent_id}/reactivate` | ✅ | — | — | Por rol |
| POST | `/parents/{parent_id}/resend-invitation` | ✅ | — | — | Por rol |
| GET | `/reports` | ✅ | ✅ | — | Por rol |
| POST | `/reports` | — | ✅ | — | Por rol |
| POST | `/routine-moments/{moment_id}/confirm` | — | ✅ | — | Por rol |
| GET | `/routine-templates` | ✅ | — | — | Por rol |
| POST | `/routine-templates` | ✅ | — | — | Por rol |
| PUT | `/routine-templates/{template_id}` | ✅ | — | — | Por rol |
| GET | `/shifts` | ✅ | — | — | Por rol |
| POST | `/staff-attendance/check-in` | — | ✅ | — | Por rol |
| POST | `/staff-attendance/notify-coordination` | — | ✅ | — | Por rol |
| GET | `/staff-attendance/today` | — | ✅ | — | Por rol |
| GET | `/teachers` | ✅ | — | — | Por rol |
| POST | `/teachers` | ✅ | — | — | Por rol |
| GET | `/teachers/{teacher_id}` | ✅ | — | — | Por rol |
| PATCH | `/teachers/{teacher_id}` | ✅ | — | — | Por rol |
| POST | `/teachers/{teacher_id}/deactivate` | ✅ | — | — | Por rol |
| POST | `/teachers/{teacher_id}/reactivate` | ✅ | — | — | Por rol |
| POST | `/users/{user_id}/access-code` | ✅ | — | — | Por rol |

Total: 75 endpoints (6 públicos, 54 por rol, 15 con alcance por registro).

## Cómo se regenera

```bash
cd guarderia-api
JWT_SECRET=una-llave-de-al-menos-32-caracteres-aaaa .venv/bin/python scripts/roles_matrix.py > ../docs/api/matriz-de-roles.md
```
