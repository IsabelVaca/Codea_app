# `attendance`: asistencia del niño y calendario

Tabla: `attendance` (una fila por niño y día; `PRESENT` o `ABSENT`; quién la marcó; `arrived_at`, la hora de entrada). **Sin registro, el niño cuenta como ausente**: la docente le pasa lista en *Mi día* (pase de lista) y cada marca `PRESENT` lo deja presente. No hay un tercer estado «sin pasar lista».

| Endpoint | Rol | Qué hace |
|---|---|---|
| `PUT /attendance/{child_id}` | admin o docente | Marca presente o ausente (hoy, o `date`). Sirve para el pase de lista y para corregir una marca. Al marcar presente guarda la hora de entrada (`arrived_at`): la del servidor, o la que manda la app si la marca se hizo sin conexión (si viene en el futuro se ignora). Repetir `PRESENT` no la cambia; `ABSENT` la borra |
| `GET /children/{child_id}/attendance` | quien tenga alcance sobre el niño | Calendario del mes: `FULL_DAY` y `WITH_NOTE` solo con pase de lista `PRESENT` (o un reporte/nota de ese día); `ABSENT` si se marcó ausente **o si su sala tuvo rutina y nadie lo pasó presente**; `NO_RECORD` en fines de semana, días sin rutina y días futuros |

## Estado

| Hecho | Pendiente |
|---|---|
| Asistencia del día, calendario mensual y **hora de entrada** (RF-03; la salida está en `departures`) | Corregir la hora de entrada a mano (hoy solo se borra marcando ausente) |
| | **Llegada de los padres** (HN2) |
| | Eventos y citas en el calendario (CRF-03): no existen tablas ni endpoints |
| | Auditar quién cambió una asistencia (hoy solo `marked_by`) |

Pruebas: `tests/test_teacher_day.py` (`test_my_room_and_attendance`), `tests/test_parent_views.py` (`test_calendar`), `tests/test_arrival_time.py` (8, hora de entrada).
