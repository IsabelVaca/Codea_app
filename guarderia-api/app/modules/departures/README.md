# `departures`: pase de salida

Al terminar la rutina, la docente registra a qué hora sale cada niño y con quién se va. Tabla: `child_departure` (niño, día, hora, persona autorizada o tutor, nombre de quien lo recogió, quién lo registró). Hay una por niño y por día.

| Endpoint | Rol | Qué hace |
|---|---|---|
| `GET /classrooms/{id}/departures` | admin o docente de esa sala | Los niños presentes hoy, con las personas que pueden recogerlos (las autorizadas que registró el admin y los tutores) y la salida de los que ya se fueron. `all_done` es `true` cuando ya salieron todos |
| `POST /children/{id}/departure` | docente de la sala | Registra la salida con `pickup_id` o `guardian_id` (solo uno). La hora la pone el servidor. 409 `CHILD_NOT_PRESENT` o `ALREADY_DEPARTED`; 404 `PICKUP_NOT_FOUND`; 422 `PICKUP_REQUIRED` |
| `DELETE /children/{id}/departure` | docente de la sala | Quita la salida de hoy, por si se eligió a la persona equivocada. 404 `DEPARTURE_NOT_FOUND` |

- Solo salen los niños con pase de lista `PRESENT` hoy (sin registro cuenta como ausente).
- El nombre de quien lo recogió se guarda como texto: si el admin quita después a la persona de la lista, el registro del día no cambia.
- Cada salida y cada corrección quedan en la bitácora de auditoría (`CHILD_DEPARTED`, `CHILD_DEPARTURE_UNDONE`).
- Para terminar el turno, la docente usa `POST /staff-attendance/check-out`. La API no exige que todas las salidas estén registradas: esa regla vive en la app (`DepartureRules.canFinishDay`).

## Estado

| Hecho | Pendiente |
|---|---|
| Registrar, corregir y consultar la salida, con alcance por sala | Salida sin conexión (hoy necesita red) |
| Los tutores del niño aparecen siempre como opción, aunque no haya personas autorizadas | |
| La familia ve a qué hora salió su hijo y con quién: último evento de `GET /children/{id}/day` (`DEPARTURE`) | |

Pruebas: `tests/test_departures_and_health.py`.
