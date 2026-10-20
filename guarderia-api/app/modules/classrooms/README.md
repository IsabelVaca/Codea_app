# `classrooms`: salas

Tablas: `daycare_center`, `classroom`. `centers.py` resuelve el centro de una sala.

| Endpoint | Rol | Qué hace |
|---|---|---|
| `GET /classrooms` | admin | Salas con cupo, inscritos, cupo disponible y asistente principal |
| `POST /classrooms` | admin | Crea sala. 422 `NAME_REQUIRED`, `CAPACITY_INVALID` |
| `GET /classrooms/{id}` | admin | Detalle con niños y asistentes |
| `PATCH /classrooms/{id}` | admin | Edita (por ejemplo el cupo) |
| `PUT /classrooms/{id}/staff` | admin | Asigna asistentes y la principal |
| `DELETE /classrooms/{id}` | admin | 409 `CLASSROOM_NOT_EMPTY` si tiene niños; sus asistentes quedan sin sala |
| `GET /classrooms/{id}/children` | admin o docente de esa sala | "Mi sala": niños con asistencia del día y mensajes sin leer |

La docente solo ve su sala (`ensure_classroom_access`).

## Estado

| Hecho | Pendiente |
|---|---|
| CRUD completo, cupo, asistentes, "Mi sala" | Impedir pasarse del cupo al inscribir o mover niños (hoy la API no lo revisa) |
| | Auditar altas y bajas |

Pruebas: `tests/test_admin.py`, `tests/test_teacher_day.py`.
