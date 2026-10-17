# `domain/`

Modelos y reglas de negocio en **Kotlin puro**: sin imports de Android ni de Retrofit. Es la única capa con pruebas JVM (`app/src/test/.../domain/`).

| Archivo | Qué contiene |
|---|---|
| `CheckIn.kt`, `CheckInRules.kt` | Registro de entrada de la docente y la regla de a tiempo / tarde / fuera de radio |
| `Routine.kt`, `RoutineRules.kt`, `TimeOfDay.kt` | Rutina del día y sus momentos (`Subprocess`), y la regla de avance |
| `Report.kt`, `ReportValidator.kt` | Reportes (`WRITTEN`, `DICTATION`, `PHOTO`) y sus validaciones |
| `Child.kt`, `ChatMessage.kt`, `Teacher.kt` | Modelos del flujo de la docente |
| `Sala.kt`, `SalaValidator.kt`, `SalaError.kt` | Salas del admin |
| `Docente.kt`, `DocenteValidator.kt` | Docentes del admin |
| `Infante.kt`, `InfanteValidator.kt` | Infantes del admin |
| `Padre.kt`, `PadreValidator.kt` | Padres del admin |
| `MomentoCatalogo.kt`, `MomentoRutina.kt`, `RutinaItem.kt` | Catálogo de momentos y rutinas editables del admin |

## Reglas de esta carpeta

- Nada de Android aquí. Si necesitas `Context`, `Bitmap` o similares, va en `data/` o `ui/`.
- Cada regla que también existe en la API (`guarderia-api/app/core/rules.py`) debe dar la misma respuesta en los dos lados y compartir casos de prueba.
- Los errores de validación se expresan con códigos que la API también usa (`NAME_REQUIRED`, `PHONE_INVALID`, `CAPACITY_INVALID`…).

## Pendiente

- Reglas para lo que aún no existe: pagos, eventos y citas, resumen semanal, menú del día, entrada y salida del niño.
- Pruebas para los validadores de admin (`Sala`, `Docente`, `Infante`, `Padre`): hoy solo hay pruebas de check-in, rutina y reportes.
