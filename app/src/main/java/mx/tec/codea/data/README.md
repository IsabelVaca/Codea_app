# `data/`

Repositorios: lo que las capas de arriba piden y mandan. Convierten DTOs (`data/api/`) a modelos de `domain/` y al revés. La sesión (`SessionRepository`) se crea una sola vez en `di/AppContainer` (DI manual) y se pasa a quien la necesita; los demás repositorios todavía los instancia el ViewModel directo.

## Repositorios que hablan con la API (los vigentes)

| Archivo | Para quién | Qué hace |
|---|---|---|
| `SessionRepository.kt` | Todos | **La única puerta a la sesión.** Estado `Loading`/`Anonymous`/`Active`, login, activar, renovar (`refresh`, con un candado para que cinco peticiones hagan un solo refresh), cerrar sesión y guardar los tokens |
| `AccountApiRepository.kt` | Todos | Mi cuenta: el perfil y cambiar la contraseña |
| `local/SessionStore.kt`, `local/TokenCipher.kt` | Todos | La sesión en disco con **DataStore**; los dos tokens se guardan cifrados con **AES-GCM y una llave del Android Keystore** (`KeystoreTokenCipher`). `TokenCipher` es interfaz para poder probar con un cifrador falso |
| `TeacherApiRepository.kt` | Docente | Perfil, check-in, Mi sala, asistencia, rutina, reportes, chat |
| `ParentApiRepository.kt` | Padre | Hoy, calendario, fotos, documentos, personas autorizadas, notas |
| `AdminApiRepositories.kt`, `AdminDirectory.kt` | Admin | Docentes, infantes, padres, centro y avisos; directorio de personas para los selectores |
| `SalaApiRepository.kt` | Admin | Salas |
| `RutinaRepository.kt` | Admin | Plantillas de rutina y catálogo de momentos |
| `MediaUploader.kt` | Todos | Subida en 3 pasos: pide permiso a la API → sube a S3 con URL firmada → avisa que terminó. Devuelve el `media_id` |
| `DataEvents.kt` | Todos | Bus global: `changed` (recargar listas), `notices` (texto para el snackbar) y `accessCodes` (el código de acceso de alguien recién dado de alta; `CodeaNavHost` lo muestra en un diálogo) |
| `TimeUtils.kt` | Todos | Conversión de horas ISO / "HH:mm" |

## Repositorios en memoria

**Ya no hay.** El 18/oct se borraron los siete de la etapa sin backend (`ChildRepository`, `DocenteRepository`, `InfanteRepository`, `PadreRepository`, `RoutineRepository`, `SalaRepository` y `TeacherRepository`): solo alimentaban vistas previas de Android Studio y las pruebas de `RoutineRules`, que ahora traen su propio dato de ejemplo. Todo lo que lee la app sale de los `*ApiRepository`.

## Cómo agregar un repositorio o una función

1. Agrega el endpoint a `CodeaApi` y sus DTOs en `data/api/`.
2. Crea la función aquí, devolviendo modelos de `domain/` (no DTOs).
3. Una escritura se llama desde el ViewModel con `launchAction`, que avisa a `DataEvents` al terminar.

## Pendiente

- **Sin conexión:** no hay Room ni caché local; todo se pide a la API cada vez (RF-11, RNF07). Cuando se agregue, el repositorio debe ser la única fuente de verdad (lee de Room, sincroniza desde la API).
- **Interfaces:** el marco pide DI manual con interfaces y contenedores. `AppContainer` ya existe y entrega la sesión; falta pasar por él los demás repositorios (hoy cada ViewModel crea el suyo) y ponerles interfaz para poder probar los ViewModels con fakes.
- **Fotos:** comprimir y corregir orientación (EXIF) antes de subir, como pide el marco. Hoy `uploadPhoto` solo comprime el bitmap a JPEG 85.
