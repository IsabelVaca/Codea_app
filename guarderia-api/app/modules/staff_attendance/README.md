# `staff_attendance`: checador de la docente

Prefijo `/staff-attendance`. Tablas: `staff_check_in` (hora, coordenadas, distancia, estado, minutos de retardo, motivo; y la salida: `checked_out_at`, `out_latitude`, `out_longitude`, `out_distance_m`), `coordination_notice`.

| Endpoint | Rol | Qué hace |
|---|---|---|
| `GET /staff-attendance/today` | docente | El registro de hoy, o `null` |
| `POST /staff-attendance/check-in` | docente | Registra la entrada. 422 `OUT_OF_RANGE` fuera del radio (incluye `distance_m` y `allowed_radius_m`); `LATE` si llegó tarde. Repetirla devuelve la misma con 200 |
| `POST /staff-attendance/check-out` | docente | Registra la salida del día. Pide haber registrado la entrada (409 `NOT_CHECKED_IN`) y estar dentro del radio (422 `OUT_OF_RANGE`). Repetirla devuelve la misma salida con 200 (no se pisa la hora) |
| `POST /staff-attendance/notify-coordination` | docente | Avisa a coordinación (llegará tarde o está fuera del radio) |

El radio, la ubicación y la zona horaria vienen del centro ([`admins`](../admins/README.md)). Mismas reglas que `CheckInRules` en la app.

## Estado

| Hecho | Pendiente |
|---|---|
| Entrada con GPS, retardo y aviso, y **salida** con la misma regla de ubicación (5 pruebas) | Salida automática al cerrar el día (hoy la docente debe registrarla; no hay worker) |
| | Que el admin vea los avisos de coordinación (hoy se guardan, no hay endpoint de lectura) |
| | Notificaciones push: el marco excluye Firebase; usar WorkManager en la app |

Pruebas: `tests/test_teacher_day.py` (`test_check_in_*` y `test_check_out_*`).
