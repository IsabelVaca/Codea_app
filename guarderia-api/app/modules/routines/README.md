# `routines`: plantillas y rutina diaria

Tablas: `routine_template`, `routine_template_moment` (la plantilla), `routine_moment` (la rutina real de una sala en un día; estado `DONE`/`ACTIVE`/`PENDING`).
Un índice único parcial garantiza **un solo momento `ACTIVE` por sala y día**.

| Endpoint | Rol | Qué hace |
|---|---|---|
| `GET /classrooms/{id}/routine` | admin o docente de la sala | La rutina del día con el estado de cada momento |
| `POST /routine-moments/{id}/confirm` | docente | Cierra el momento activo y arranca el siguiente. 409 `MOMENT_NOT_ACTIVE` |
| `GET /routine-templates` | admin | Plantillas con sus momentos y las salas que las usan |
| `POST /routine-templates` | admin | Crea. 422 `NAME_REQUIRED`, `MOMENTS_REQUIRED` |
| `PUT /routine-templates/{id}` | admin | Reemplaza nombre y momentos (no cambia días ya iniciados) |
| `PUT /classrooms/{id}/routine-template` | admin | Elige la plantilla de la sala (`null` = la default) |

La rutina del día se genera a partir de la plantilla la primera vez que se consulta o se hace check-in; el check-in también la arranca.

## Estado

| Hecho | Pendiente |
|---|---|
| Plantillas, asignación, avance, un solo activo | **"Finalizar día"** al terminar la rutina (tarea abierta): cerrar el día y consolidar el resumen |
| | Catálogo de momentos del lado del servidor (la app tiene `CatalogoMomentosScreen`; revisar si se persiste) |
| | **Menú del día** (HN3): no existe |

Pruebas: `tests/test_teacher_day.py`, `tests/test_admin_extras.py` (plantillas).
