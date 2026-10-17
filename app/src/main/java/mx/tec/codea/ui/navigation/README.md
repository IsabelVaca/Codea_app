# `ui/navigation/`

| Archivo | Qué hace |
|---|---|
| `CodeaApp.kt` | Raíz de la interfaz: selección de rol, banderas de `ApiConfig`, `key(role)`, `Scaffold` con barra inferior y snackbar |
| `Routes.kt` | Rutas como objetos `@Serializable`. Solo llevan ids, nunca objetos completos |
| `CodeaNavHost.kt` | El grafo: une cada ruta con su pantalla y su ViewModel (849 líneas, 32 destinos) |
| `TopLevelDestination.kt` | Las pestañas de la barra inferior y qué roles las ven (`roles`) |
| `NavControllerExtensions.kt` | Comportamiento al tocar una pestaña (navegar o reseleccionar) |

## Roles y pestañas

| Rol | Grafo inicial | Pestañas |
|---|---|---|
| Docente | `MyDayGraph` | Mi día, Mi sala, Chats, Menú |
| Admin | `AdminCentroGraph` | Centro, Docentes, Infantes, Padres, Rutinas |
| Padre | `ParentTodayRoute` | Hoy, Calendario, Fotos, Documentos |

Las pestañas exactas salen de `TopLevelDestination.kt`: ahí es donde se agregan o se quitan.

## Cómo agregar una pantalla

1. Declara la ruta en `Routes.kt` (`data object` si no lleva datos, `data class` si lleva ids).
2. Agrega `composable<MiRuta> { ... }` en `CodeaNavHost.kt`, dentro del grafo de su pestaña.
3. Si va en la barra inferior, agrégala a `TopLevelDestination`.
4. Lo que aún no existe puede abrir `ComingSoonScreen`.

## Pendiente

- **Login:** hoy `CodeaApp` muestra `RoleSelectionScreen`. Con login real, el rol sale del `GET /me` y esta pantalla desaparece.
- **Partir `CodeaNavHost.kt`:** es el archivo con más conflictos de merge. Separar un archivo por rol (`TeacherNavGraph`, `AdminNavGraph`, `ParentNavGraph`) reduce choques.
- **Destinos `ComingSoon` de la docente:** documentos y perfil (`MenuOption` en `MenuScreen.kt`).
- **Rutas del padre que faltan:** chat, menú del día, selector de hijo.
- **Rutas del admin que faltan:** solicitudes de borrado, pagos.
