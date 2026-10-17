# Pruebas de la app

## Hoy

JVM, con JUnit, en `java/mx/tec/codea/domain/`:

| Clase | Pruebas | Qué cubre |
|---|---|---|
| `CheckInRulesTest` | 4 | A tiempo, tarde y fuera del radio |
| `RoutineRulesTest` | 4 | Avance de la rutina |
| `ReportValidatorTest` | 3 | Reportes sin texto o sin niños |
| `ExampleUnitTest` | 1 | De plantilla |

```bash
./gradlew test
./gradlew test --tests "mx.tec.codea.domain.ReportValidatorTest"
```

Los mismos casos están en el backend (`guarderia-api/tests/`), para que la misma regla dé la misma respuesta en los dos lados.
Instrumentadas: `app/src/androidTest/` solo tiene `ExampleInstrumentedTest`.

## Pendiente

- Pruebas de los validadores de admin: `SalaValidator`, `DocenteValidator`, `InfanteValidator`, `PadreValidator`.
- Pruebas de ViewModels con un servidor falso (el marco lo menciona).
- Pruebas de interfaz con **Compose UI Test** de los flujos críticos: check-in, reporte, asistencia, inscribir infante.
- Registrar los resultados en la matriz de trazabilidad ([`docs/ENTREGA-3.md`](../../../docs/ENTREGA-3.md), parte 3).
