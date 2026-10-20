# `domain/`

Modelos y reglas de negocio en **Kotlin puro**: sin imports de Android ni de Retrofit. Casi todas las pruebas JVM son de esta capa (`app/src/test/.../domain/`); las demás son de `data/` (sesión, errores sin conexión, mapeo de Mi sala).

| Archivo | Qué contiene |
|---|---|
| `CheckIn.kt`, `CheckInRules.kt` | Registro de entrada de la docente y la regla de a tiempo / tarde (sin ubicación) |
| `Routine.kt`, `RoutineRules.kt`, `TimeOfDay.kt` | Rutina del día y sus momentos (`Subprocess`), y la regla de avance |
| `Report.kt`, `ReportValidator.kt` | Reportes (`WRITTEN`, `DICTATION`, `PHOTO`) y sus validaciones |
| `Child.kt`, `ChatMessage.kt`, `ChatRules.kt`, `Teacher.kt` | Modelos del flujo de la docente; reglas del chat (mezclar mensajes, pendientes, vista previa) |
| `RoomRules.kt`, `ChildProfile.kt`, `ResumenDelDia.kt`, `DeparturePass.kt` | Mi sala (orden, filtros, cola de resúmenes), ficha del niño (edad, teléfono marcable), resumen del día y pase de salida |
| `OfflineAttendance.kt` | La cola de asistencia sin conexión (interfaces que implementa `data/offline/`) |
| `Sala.kt`, `SalaValidator.kt`, `SalaError.kt` | Salas del admin |
| `Docente.kt`, `DocenteValidator.kt` | Docentes del admin |
| `Infante.kt`, `InfanteValidator.kt` | Infantes del admin |
| `Padre.kt`, `PadreValidator.kt` | Padres del admin |
| `MomentoCatalogo.kt`, `MomentoCategoria.kt`, `MomentoRutina.kt`, `RutinaItem.kt` | Catálogo de momentos y rutinas editables del admin |
| `HorarioCentro.kt`, `MenuDelDia.kt`, `PersonaAutorizada.kt`, `Aviso.kt` | Horario del centro, menú del día, personas autorizadas para recoger y avisos |
| `ChildHealth.kt`, `ChildChoice.kt`, `ResumenSemana.kt` | Padre: salud de Mi hijo, selector de hijo y resumen semanal |
| `Session.kt`, `AuthValidator.kt`, `Invite.kt` | Sesión, contraseña y código al activar, invitación: leer el enlace, armar el de WhatsApp, limpiar el código y el correo y ponerle el guion al código |

## Reglas de esta carpeta

- Nada de Android aquí. Si necesitas `Context`, `Bitmap` o similares, va en `data/` o `ui/`.
- Cada regla que también existe en la API (`guarderia-api/app/core/rules.py`) debe dar la misma respuesta en los dos lados y compartir casos de prueba.
- Los errores de validación son tipos sin texto (`SalaError.NombreVacio`, `DocenteError.TelefonoInvalido`…) que equivalen a los `code` de la API (`NAME_REQUIRED`, `PHONE_INVALID`, `CAPACITY_INVALID`…); el texto lo pone la UI.

## Pendiente

- Reglas para lo que aún no existe: eventos y citas.
- Pruebas para los validadores de admin `Sala`, `Docente` y `Padre` (`InfanteValidator` ya tiene).
