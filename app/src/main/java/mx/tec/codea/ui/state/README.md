# `ui/state/`

ViewModels. Las pantallas solo dibujan y llaman funciones de aquí; el estado sale como `mutableStateOf` con `private set` o como `StateFlow`.

## Los ViewModels

| Archivo | Rol | Qué maneja | Estilo de estado |
|---|---|---|---|
| `CodeaViewModel.kt` | Docente | **El compartido.** Check-in, rutina, momento elegido, reportes, niños y asistencia, chat abierto y mensajes | `mutableStateOf` |
| `CheckerViewModel.kt` | Docente | Pantalla del checador (ubicación, estado, motivo de retardo) | — |
| `ReportFormViewModel.kt` | Docente | Formulario de reporte (método, texto, foto, niños elegidos) | — |
| `ParentViewModel.kt` | Padre | Hoy, calendario, documentos, fotos y personas autorizadas del hijo | `mutableStateOf` |
| `CentroViewModel.kt` | Admin | Centro, avisos y radio del checador | — |
| `SalasAdminViewModel.kt`, `CrearSalaViewModel.kt` | Admin | Salas | `StateFlow` |
| `DocentesAdminViewModel.kt`, `AltaAsistenteViewModel.kt` | Admin | Docentes | `StateFlow` |
| `InfantesAdminViewModel.kt`, `InscribirInfanteViewModel.kt` | Admin | Infantes | `StateFlow` |
| `PadresAdminViewModel.kt`, `InscribirPadreViewModel.kt` | Admin | Padres | `StateFlow` |
| `RutinasAdminViewModel.kt`, `NuevaRutinaViewModel.kt`, `AgregarMomentoViewModel.kt` | Admin | Plantillas de rutina y momentos | — |
| `Actions.kt` | Todos | `launchAction`: corre una escritura, avisa a `DataEvents.changed` si sale bien, o manda el error al snackbar | — |

(Donde dice "—" no revisé el tipo de estado; mira el archivo.)

## Reglas

1. **`CodeaViewModel` se crea una sola vez** en `CodeaApp` (ámbito de la actividad). Nunca lo pidas con `viewModel()` dentro de un destino: ese destino recibiría una copia aparte.
2. Los ViewModels de pantalla se piden con `viewModel()` en `CodeaNavHost.kt`.
3. Cada `cargar()` revisa la bandera de rol de `ApiConfig` (`isAdmin`, `isTeacher`, `isParent`) para no pedir datos que su rol no puede ver (la API respondería 403).
4. Cada ViewModel con lista escucha `DataEvents.changed` y vuelve a cargar. Si agregas uno, haz lo mismo.
5. Las escrituras van con `launchAction`; no repitas el try/catch.
6. Los errores se muestran con `DataEvents.notice(e.toUserMessage())`.

## Pendiente

- **Unificar el estilo:** el marco pide StateFlow/Flow; `CodeaViewModel` y `ParentViewModel` usan `mutableStateOf`.
- **Dividir `CodeaViewModel`** (229 líneas, mezcla check-in, rutina, niños y chat) y hacer que `ParentViewModel` soporte varios hijos (hoy usa el primero).
- **Estado de carga y de error por pantalla:** hoy los errores solo salen en el snackbar y no hay indicador de carga.
- **Sin conexión:** cuando exista Room, estos ViewModels deben leer del repositorio local y no de la API directa.
- **Pruebas:** no hay pruebas de ViewModels; el marco habla de servidores falsos para eso.
