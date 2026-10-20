# Pruebas de la app

## Hoy

JVM, con JUnit y un servidor falso (`mockwebserver3`). **140 pruebas, 0 fallos** (2026-10-20):

| Clase | Pruebas | Qué cubre |
|---|---|---|
| `domain/CheckInRulesTest` | 5 | A tiempo, justo en la hora, tarde con motivo, registro a tiempo sin salida y formato de hora |
| `domain/RoutineRulesTest` | 4 | Avance de la rutina |
| `domain/ReportValidatorTest` | 3 | Reportes sin texto o sin niños |
| `domain/TimeOfDayTest` | 2 | Hora con dos dígitos; duración en minutos, horas o ambas |
| `domain/OfflineAttendanceTest` | 11 | La cola sin conexión con dobles en memoria: con red se manda; sin red espera; un error permanente se avisa y no se guarda; `flush` manda y vacía; de varias marcas del mismo niño solo la última; se detiene si la red sigue caída sin perder nada; descarta lo que ya no tiene remedio; una marca nueva no la pisa una vieja de la cola; marcas pendientes por día; la marca hecha sin red lleva su hora y la que se manda al momento no (la pone el servidor). **Mutación comprobada:** quitar el `removeFor` hace fallar justo la prueba que lo cubre |
| `data/OfflineErrorsTest` | 4 | Qué se reintenta (red, 5xx, 401, 408, 429) y qué no (400, 403, 404, 409, 422) |
| `data/RoomChildMappingTest` | 4 | Mi sala desde la API: solo `PRESENT` cuenta como presente, llegan notas y resumen, hora de entrada, fechas ISO en UTC |
| `domain/RoomRulesTest` | 7 | Mi sala: sin pase de lista nadie está presente, orden (presentes con pendientes primero, luego por nombre), notas vistas, avisos de ausentes, filtros y cola de resúmenes |
| `domain/ChildProfileRulesTest` | 6 | Ficha del niño: edad en años y meses (y cuándo se cumple el mes), teléfono marcable |
| `domain/DeparturePassRulesTest` | 7 | Pase de salida: rutina terminada, quién ya salió, pendientes y cuándo se puede finalizar el día |
| `domain/ChatRulesTest` | 11 | Chat docente-familia: mezclar sin duplicados, pendientes y fallidos al final, último id confirmado, no poder mandar vacío, foto sin texto, vista previa y nombre de los tutores |
| `domain/ResumenSemanaTextosTest` | 5 | Resumen semanal: lo más frecuente primero, empates, sin registros, valores desconocidos, siesta y estado del día |
| `domain/ResumenDelDiaTest` | 5 | Resumen del día de la docente: ánimo y comida por código de la API, códigos desconocidos, límites de pañales y siesta, texto de la siesta |
| `domain/ChildHealthRulesTest` | 5 | Salud en Mi hijo (padre): limpiar campos y cuándo se puede guardar |
| `domain/ChildChoiceTest` | 4 | Selector de hijo: conserva el elegido al recargar, cae al primero, sin hijos, solo aparece con más de uno |
| `domain/InfanteValidatorTest` | 8 | Fecha de nacimiento real al inscribir: formato, día y mes de un dígito, fechas que no existen, 29 de febrero, futuras, de 0 a 12 años, año completo |
| `domain/HorarioRulesTest` | 6 | Horario del centro: entrada antes de salida, cierre después de la salida, al menos un día, días ISO |
| `domain/MenuRulesTest` | 6 | Menú del día: lunes de la semana, cambio de mes y año, días hábiles, al menos una comida, largo máximo |
| `domain/PersonaAutorizadaRulesTest` | 2 | Solo el nombre es obligatorio; orden por nombre |
| `domain/AuthValidatorTest` | 7 | Contraseña de 8 (no) y 9 (sí) caracteres, tope de 128, ni el correo ni distinta de su confirmación, código obligatorio |
| `domain/InviteTest` | 13 | Enlace de invitación (https y `codea://`), teléfono y enlace de WhatsApp, código solo con letras y números, guion después de 4, correo sin espacios ni caracteres invisibles |
| `data/TokenAuthenticatorTest` | 8 | El token en cada petición; con el token vencido renueva **una vez** y repite con el nuevo; refresh rechazado = sesión terminada; servidor caído al renovar = sesión conservada; sin ciclo si el token nuevo también falla; `/auth/` no lleva token ni renueva; **4 peticiones simultáneas = un solo refresh**; `USER_INACTIVE` cierra la sesión |
| `data/SessionRepositoryTest` | 6 | Sin sesión guardada, login que sobrevive a reabrir la app, login fallido, cerrar sesión aunque el servidor falle, llave del Keystore perdida = sin sesión, refrescar conserva nombre y roles |
| `ExampleUnitTest` | 1 | De plantilla |

Estas pruebas ejercitan el interceptor, el autenticador y el `SessionRepository` **reales** contra un servidor falso, no una copia de la lógica. Se comprobó con una mutación: quitar la guarda del refresh hace fallar justo la prueba de peticiones simultáneas.

```bash
./gradlew test
./gradlew test --tests "mx.tec.codea.data.TokenAuthenticatorTest"
```

Los mismos casos de reglas están en el backend (`guarderia-api/tests/`), para que la misma regla dé la misma respuesta en los dos lados.
Instrumentadas: `app/src/androidTest/` solo tiene `ExampleInstrumentedTest`. El cifrado real con el Keystore (`KeystoreTokenCipher`) solo se puede probar en un dispositivo; se comprobó a mano en el emulador (los tokens en disco no contienen ningún JWT en claro).

## Pendiente

- Pruebas de los validadores de admin: `SalaValidator`, `DocenteValidator`, `PadreValidator` (`InfanteValidator` ya tiene).
- Pruebas de `SessionViewModel` y `AccountViewModel` (el servidor falso y `SessionRepository` ya están listos para armarlas; falta agregar `kotlinx-coroutines-test` y usar `Dispatchers.setMain`).
- Pruebas de interfaz con **Compose UI Test** de los flujos críticos: check-in, reporte, asistencia, inscribir infante.
- La base Room (cola y caché, versión 5) no tiene pruebas en JVM: solo pruebas manuales en el emulador.
- ~~Registrar los resultados en la matriz de trazabilidad~~ Ya está en [`docs/entrega/matriz-trazabilidad.md`](../../../docs/entrega/matriz-trazabilidad.md); antes de entregar, volver a correr `./gradlew test` y anotar la fecha.
