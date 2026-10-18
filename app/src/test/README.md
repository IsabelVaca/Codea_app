# Pruebas de la app

## Hoy

JVM, con JUnit y un servidor falso (`mockwebserver3`). **74 pruebas, 0 fallos** (2026-10-18):

| Clase | Pruebas | Qué cubre |
|---|---|---|
| `domain/CheckInRulesTest` | 5 | A tiempo, justo en la hora, tarde con motivo, registro a tiempo sin salida y formato de hora |
| `domain/RoutineRulesTest` | 4 | Avance de la rutina |
| `domain/ReportValidatorTest` | 3 | Reportes sin texto o sin niños |
| `domain/OfflineAttendanceTest` | 9 | La cola sin conexión con dobles en memoria: con red se manda; sin red espera; un error permanente se avisa y no se guarda; `flush` manda y vacía; de varias marcas del mismo niño solo la última; se detiene si la red sigue caída sin perder nada; descarta lo que ya no tiene remedio; una marca nueva no la pisa una vieja de la cola; marcas pendientes por día. **Mutación comprobada:** quitar el `removeFor` hace fallar justo la prueba que lo cubre |
| `data/OfflineErrorsTest` | 4 | Qué se reintenta (red, 5xx, 401, 408, 429) y qué no (400, 403, 404, 409, 422) |
| `domain/ChatRulesTest` | 4 | Reglas del chat docente-familia |
| `domain/ResumenSemanaTextosTest` | 5 | Resumen semanal: lo más frecuente primero, empates, sin registros, valores desconocidos, siesta y estado del día |
| `domain/ResumenDelDiaTest` | 5 | Resumen del día de la docente: ánimo y comida por código de la API, códigos desconocidos, límites de pañales y siesta, texto de la siesta |
| `domain/InfanteValidatorTest` | 8 | Fecha de nacimiento real al inscribir: formato, día y mes de un dígito, fechas que no existen, 29 de febrero, futuras, de 0 a 12 años, año completo |
| `domain/AuthValidatorTest` | 7 | Contraseña de 8 (no) y 9 (sí) caracteres, tope de 128, ni el correo ni distinta de su confirmación, código obligatorio |
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
- Registrar los resultados en la matriz de trazabilidad ([`docs/ENTREGA-3.md`](../../../docs/ENTREGA-3.md), parte 3).
