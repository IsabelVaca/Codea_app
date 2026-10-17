# `staff_attendance`: checador de la docente

Prefijo `/staff-attendance`. Tablas: `staff_check_in` (hora, coordenadas, distancia, estado, minutos de retardo, motivo), `coordination_notice`.

| Endpoint | Rol | Qué hace |
|---|---|---|
| `GET /staff-attendance/today` | docente | El registro de hoy, o `null` |
| `POST /staff-attendance/check-in` | docente | Registra la entrada. 422 `OUT_OF_RANGE` fuera del radio (incluye `distance_m` y `allowed_radius_m`); `LATE` si llegó tarde. Repetirla devuelve la misma con 200 |
| `POST /staff-attendance/notify-coordination` | docente | Avisa a coordinación (llegará tarde o está fuera del radio) |

El radio, la ubicación y la zona horaria vienen del centro ([`admins`](../admins/README.md)). Mismas reglas que `CheckInRules` en la app.

## Estado

| Hecho | Pendiente |
|---|---|
| Entrada con GPS, retardo y aviso | **Salida** de la docente (solo hay entrada) |
| | Que el admin vea los avisos de coordinación (hoy se guardan, no hay endpoint de lectura) |
| | Notificaciones push: el marco excluye Firebase; usar WorkManager en la app |

Pruebas: `tests/test_teacher_day.py` (`test_check_in_*`).
