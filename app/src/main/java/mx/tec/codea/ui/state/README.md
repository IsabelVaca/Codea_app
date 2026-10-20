# `ui/state/`

ViewModels. Las pantallas solo dibujan y llaman funciones de aquí; el estado sale como `mutableStateOf` con `private set` o como `StateFlow`.

**Caché (admin y padre, desde el 20/oct):** `DocentesAdminViewModel`, `SalasAdminViewModel`, `InfantesAdminViewModel`, `PadresAdminViewModel`, `AvisosAdminViewModel`, `RutinasAdminViewModel`, `AvisosViewModel` y `ParentViewModel` muestran primero lo que guardó el repositorio (`cached()` / `…Guardado()`) y luego lo reemplazan con la API, así no se quedan vacíos al abrir la app ni al cambiar de pestaña. En Docentes, el indicador de carga solo sale al jalar hacia abajo (`cargar(conIndicador = true)`); las recargas al abrir la pestaña son silenciosas. `ParentViewModel` muestra lo guardado una vez por hijo (`guardadoMostradoDe`), para no tapar lo que ya llegó de la API.

**Caché (docente, desde el 20/oct):** `CodeaViewModel.cargar()` pinta primero lo guardado (`mostrarGuardado()`: perfil, Mi sala, checada y rutina de hoy) si todavía no hay niños en pantalla, y luego lo de la API. La rutina guardada no mueve el momento elegido (`aplicarRutina(dto, desdeCache = true)`). `ChildProfileViewModel` muestra la ficha guardada y solo marca error si no hay nada que mostrar. `DeparturePassViewModel` muestra las salidas guardadas, pero «Finalizar día» solo se habilita con la lista recién traída de la API (`actualizada`).

## Los ViewModels

| Archivo | Rol | Qué maneja | Estilo de estado |
|---|---|---|---|
| `SessionViewModel.kt` | Todos | **La sesión de la app**: Cargando / Anónimo / Activa, y el estado del formulario de entrada (`AuthFormState`). Se crea con una fábrica a la que `CodeaApp` le da el `SessionRepository` del `AppContainer` | `StateFlow` |
| `AccountViewModel.kt` | Todos | Mi cuenta: el perfil y cambiar la contraseña | `mutableStateOf` |
| `CodeaViewModel.kt` | Docente | **El compartido.** Check-in, rutina, momento elegido, reportes, niños y asistencia, chat abierto y mensajes | `mutableStateOf` |
| `CheckerViewModel.kt` | Docente | Pantalla del checador (hora, a tiempo o tarde, motivo de retardo) | — |
| `ReportFormViewModel.kt` | Docente | Formulario de reporte (método, texto, foto, niños elegidos) | — |
| `ParentViewModel.kt` | Padre | Hoy, calendario, documentos, fotos y personas autorizadas del hijo | `mutableStateOf` |
| `AvisosAdminViewModel.kt` | Admin | Pestaña Avisos: el aviso que se escribe y el historial | — |
| `SalasAdminViewModel.kt`, `CrearSalaViewModel.kt` | Admin | Salas | `StateFlow` |
| `DocentesAdminViewModel.kt`, `AltaAsistenteViewModel.kt` | Admin | Docentes | `StateFlow` |
| `InfantesAdminViewModel.kt`, `InscribirInfanteViewModel.kt` | Admin | Infantes | `StateFlow` |
| `PadresAdminViewModel.kt`, `InscribirPadreViewModel.kt` | Admin | Padres | `StateFlow` |
| `RutinasAdminViewModel.kt`, `NuevaRutinaViewModel.kt`, `AgregarMomentoViewModel.kt` | Admin | Plantillas de rutina y momentos | — |
| `Actions.kt` | Todos | `launchAction`: corre una escritura, avisa a `DataEvents.changed` si sale bien, o manda el error al snackbar | — |

(Donde dice "—" no revisé el tipo de estado; mira el archivo.)

## Reglas

1. **`CodeaViewModel` se crea una sola vez por sesión** en `CodeaApp` (en el almacén de ViewModels de la sesión, ver `SessionScopesViewModel` en `ui/navigation/CodeaApp.kt`). Nunca lo pidas con `viewModel()` dentro de un destino: ese destino recibiría una copia aparte.
2. Los ViewModels de pantalla se piden con `viewModel()` en `CodeaNavHost.kt`.
3. Cada `cargar()` revisa la bandera de rol de `ApiConfig` (`isAdmin`, `isTeacher`, `isParent`; `CodeaApp` las fija con el rol de la sesión) para no pedir datos que su rol no puede ver (la API respondería 403).
   Los ViewModels que necesitan algo del `AppContainer` (sesión) se crean con `viewModel(factory = ...)`; los demás con `viewModel()` y todos viven en el almacén de la sesión: al cerrar sesión se borran y la persona siguiente empieza limpia.
4. Cada ViewModel con lista escucha `DataEvents.changed` y vuelve a cargar. Si agregas uno, haz lo mismo.
5. Las escrituras van con `launchAction`; no repitas el try/catch.
6. Los errores se muestran con `DataEvents.notice(e.toUserMessage())`.

## Pendiente

- **Unificar el estilo:** el marco pide StateFlow/Flow; `CodeaViewModel` y `ParentViewModel` usan `mutableStateOf`.
- **Dividir `CodeaViewModel`** (229 líneas, mezcla check-in, rutina, niños y chat) y hacer que `ParentViewModel` soporte varios hijos (hoy usa el primero).
- **Estado de carga y de error por pantalla:** hoy los errores solo salen en el snackbar y no hay indicador de carga.
- **Sin conexión:** cuando exista Room, estos ViewModels deben leer del repositorio local y no de la API directa.
- **Pruebas:** no hay pruebas de ViewModels; el marco habla de servidores falsos para eso.
