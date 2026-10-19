# `attendance`: asistencia del niño y calendario

Tabla: `attendance` (una fila por niño y día; `PRESENT` o `ABSENT`; quién la marcó). **Sin registro, el niño cuenta como ausente**: la docente le pasa lista en *Mi día* (pase de lista) y cada marca `PRESENT` lo deja presente. No hay un tercer estado «sin pasar lista».

| Endpoint | Rol | Qué hace |
|---|---|---|
| `PUT /attendance/{child_id}` | admin o docente | Marca presente o ausente (hoy, o `date`). Sirve para el pase de lista y para corregir una marca |
| `GET /children/{child_id}/attendance` | quien tenga alcance sobre el niño | Calendario del mes: `FULL_DAY` y `WITH_NOTE` solo con pase de lista `PRESENT` (o un reporte/nota de ese día); `ABSENT` si se marcó ausente **o si su sala tuvo rutina y nadie lo pasó presente**; `NO_RECORD` en fines de semana, días sin rutina y días futuros |

## Estado

| Hecho | Pendiente |
|---|---|
| Asistencia del día y calendario mensual | **Hora de entrada y de salida del niño** (RF-03): hoy solo hay presente/ausente |
| | **Llegada de los padres** (HN2) |
| | Eventos y citas en el calendario (CRF-03): no existen tablas ni endpoints |
| | Auditar quién cambió una asistencia (hoy solo `marked_by`) |

Pruebas: `tests/test_teacher_day.py` (`test_my_room_and_attendance`), `tests/test_parent_views.py` (`test_calendar`).
