# `staff_attendance`: checador de la docente

Prefijo `/staff-attendance`. Tablas: `staff_check_in` (hora de entrada, estado, minutos de retardo, motivo, hora de salida y si la salida fue automática) y `coordination_notice`.

El checador **no usa ubicación**: registrar la entrada es un toque, la hora la pone el servidor y el estado (`ON_TIME` o `LATE`) sale de la **hora de entrada del centro**, igual para todas. El horario (entrada, salida, cierre automático y días de trabajo) vive en `daycare_center` y lo cambia el administrador con `PATCH /center` (en la app: Ajustes → Horario). Los turnos (`shift`) ya no intervienen.

| Endpoint | Rol | Qué hace |
|---|---|---|
| `GET /staff-attendance/today` | docente | El registro de hoy, o `null` |
| `POST /staff-attendance/check-in` | docente | Registra la entrada (cuerpo opcional: `late_reason`). `LATE` si llegó tarde. 409 `NOT_WORKING_DAY` si hoy no es día de trabajo. Repetirla devuelve la misma con 200 |
| `POST /staff-attendance/check-out` | docente | Registra la salida. 409 `NOT_CHECKED_IN` sin entrada. Repetirla devuelve la misma con 200 |
| `POST /staff-attendance/notify-coordination` | docente | Avisa a coordinación que llegará tarde. **La app ya no lo llama** (se quitó el botón el 18/oct); sigue disponible en la API |

## Cierre automático

Una entrada sin salida se cierra sola a la **hora de cierre del centro** (`auto_close_time`, 21:00 por omisión): `checked_out_at` queda en esa hora y `auto_closed` en `true`. No hay proceso programado: `close_stale` corre al inicio de `today`, `check-in` y `check-out`, así que el registro se cierra la primera vez que alguien lo consulta después de esa hora (o al día siguiente). Si la docente llegó después, la salida no queda antes de su entrada. Hay un solo centro, por eso se usa su zona horaria para todos.

## Estado

| Hecho | Pendiente |
|---|---|
| Entrada, salida, retardo y cierre automático | Decidir si se quita `notify-coordination` y la tabla `coordination_notice`, que ya no usa la app |
| | Notificaciones push: el marco excluye Firebase; usar WorkManager en la app |

Pruebas: `tests/test_teacher_day.py` (`test_check_in_*`, `test_check_out_*`, `test_open_check_in_closes_at_9pm` y las demás del cierre).
