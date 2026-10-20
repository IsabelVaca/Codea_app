# `ui/navigation/`

| Archivo | Qué hace |
|---|---|
| `CodeaApp.kt` | Raíz de la interfaz. Decide por el **estado de la sesión**: arranque, inicio de sesión / activar cuenta o la app del rol (`RoleApp`: `Scaffold`, barra inferior y snackbar). Aquí vive también `SessionScopesViewModel`, que da a cada sesión su almacén de ViewModels |
| `Routes.kt` | Rutas como objetos `@Serializable`. Solo llevan ids, nunca objetos completos. `AccountRoute` (Mi cuenta) la abren los tres roles |
| `CodeaNavHost.kt` | El grafo: une cada ruta con su pantalla y su ViewModel (849 líneas, 32 destinos) |
| `TopLevelDestination.kt` | Las pestañas de la barra inferior y qué roles las ven (`roles`) |
| `NavControllerExtensions.kt` | Comportamiento al tocar una pestaña (navegar o reseleccionar) |

## Roles y pestañas

| Rol | Grafo inicial | Pestañas |
|---|---|---|
| Docente | `MyDayGraph` | Mi día, Mi sala, Chats, Menú |
| Admin | `AdminAvisosGraph` | Avisos, Docentes, Familias, Ajustes |
| Padre | `ParentTodayRoute` | Hoy, Calendario, Fotos, Chat, Mi hijo |

Las pestañas exactas salen de `TopLevelDestination.kt`: ahí es donde se agregan o se quitan.

**Mi cuenta** (`AccountRoute`) cuelga del `NavHost` y no de una pestaña: la abre **Ajustes → Mi cuenta** (admin), el botón junto a la campana de **Hoy** (padre) y **Menú → Mi perfil** (docente).

## Cómo agregar una pantalla

1. Declara la ruta en `Routes.kt` (`data object` si no lleva datos, `data class` si lleva ids).
2. Agrega `composable<MiRuta> { ... }` en `CodeaNavHost.kt`, dentro del grafo de su pestaña.
3. Si va en la barra inferior, agrégala a `TopLevelDestination`.
4. Lo que aún no existe no se muestra: ya no hay pantalla de «Muy pronto».

## Pendiente

- **El login no es una ruta:** cerrar sesión cambia el estado de la sesión y `CodeaApp` dibuja otra cosa; no se navega a ningún lado. `RoleSelectionScreen` solo aparece para quien tiene más de un rol.
- **Partir `CodeaNavHost.kt`:** es el archivo con más conflictos de merge. Separar un archivo por rol (`TeacherNavGraph`, `AdminNavGraph`, `ParentNavGraph`) reduce choques.
- **Rutas del padre que faltan:** chat, menú del día, selector de hijo.
- **Rutas del admin:** el horario del centro se cambia en Ajustes → Horario (`AjustesHorarioRoute`) y las personas autorizadas para recoger, en Ajustes → Personas autorizadas (`AjustesPersonasAutorizadasRoute` y su detalle por niño).
- **Ruta de la docente:** `DeparturePassRoute` (pase de salida) se abre desde la tarjeta de Mi día cuando ya no queda ningún momento por hacer.
