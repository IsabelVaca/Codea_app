# `staff_attendance`: checador de la docente

Prefijo `/staff-attendance`. Tablas: `staff_check_in` (hora de entrada, estado, minutos de retardo, motivo, hora de salida y si la salida fue automática) y `coordination_notice`.

El checador **no usa ubicación**: registrar la entrada es un toque, la hora la pone el servidor y el estado (`ON_TIME` o `LATE`) sale del turno de la docente.

| Endpoint | Rol | Qué hace |
|---|---|---|
| `GET /staff-attendance/today` | docente | El registro de hoy, o `null` |
| `POST /staff-attendance/check-in` | docente | Registra la entrada (cuerpo opcional: `late_reason`). `LATE` si llegó tarde. Repetirla devuelve la misma con 200 |
| `POST /staff-attendance/check-out` | docente | Registra la salida. 409 `NOT_CHECKED_IN` sin entrada. Repetirla devuelve la misma con 200 |
| `POST /staff-attendance/notify-coordination` | docente | Avisa a coordinación que llegará tarde |

## Cierre automático a las 9 pm

Una entrada sin salida se cierra sola a las **21:00 hora del centro** (`AUTO_CLOSE_AT` en `service.py`): `checked_out_at` queda a las 21:00 y `auto_closed` en `true`. No hay proceso programado: `close_stale` corre al inicio de `today`, `check-in` y `check-out`, así que el registro se cierra la primera vez que alguien lo consulta después de las 9 pm (o al día siguiente). Si la docente llegó después de las 9, la salida no queda antes de su entrada. Hay un solo centro, por eso se usa su zona horaria para todos.

## Estado

| Hecho | Pendiente |
|---|---|
| Entrada, salida, retardo y cierre automático | Que el admin vea los avisos de coordinación (hoy se guardan, no hay endpoint de lectura) |
| | Notificaciones push: el marco excluye Firebase; usar WorkManager en la app |

Pruebas: `tests/test_teacher_day.py` (`test_check_in_*`, `test_check_out_*`, `test_open_check_in_closes_at_9pm` y las demás del cierre).
