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
| `offline/` | Docente, admin y padre | **Room.** La cola de asistencia sin conexión y la caché de Mi sala (docente), y `ApiCache`: la última respuesta de cada lista o pantalla de los tres roles (ver abajo) |

## Repositorios en memoria

**Ya no hay.** El 18/oct se borraron los siete de la etapa sin backend (`ChildRepository`, `DocenteRepository`, `InfanteRepository`, `PadreRepository`, `RoutineRepository`, `SalaRepository` y `TeacherRepository`): solo alimentaban vistas previas de Android Studio y las pruebas de `RoutineRules`, que ahora traen su propio dato de ejemplo. Todo lo que lee la app sale de los `*ApiRepository`.

## Caché de respuestas (los tres roles)

Desde el 20/oct, las listas del admin (docentes, salas, infantes, padres, avisos, rutinas), las pantallas del padre (perfil, día, calendario, resumen semanal, Mi hijo, fotos) y las de la docente (perfil, checada de hoy, rutina de hoy, ficha del niño con quién lo recoge y pase de salida) guardan la última respuesta de la API en Room (`offline/ApiCache.kt`, tabla `cached_response`, como JSON del DTO). Mi sala de la docente ya tenía su propia caché (`cached_room`, `cached_child`). El resumen del día no se guarda: es un formulario.

- Cada lectura del repositorio pasa por `fetchAndCache(nombre, serializer) { api.… }`: pide a la API y guarda lo que llega.
- Cada repositorio tiene su `cached()` (en el del padre, `…Guardado()`) con lo último que se guardó. El ViewModel lo muestra primero, si todavía no hay nada en pantalla, y luego lo reemplaza con lo de la API.
- La llave lleva el id del usuario (`usuario|nombre`), y **todo se borra al cerrar la sesión** (`AppContainer`). Las que son de un día (el día del padre; la checada, la rutina y el pase de salida de la docente) llevan la fecha, para que nunca se muestre lo de ayer como si fuera hoy.
- En `TeacherApiRepository` lo guardado se pide con `meGuardado()`, `checkInGuardado()`, `routineGuardada(sala)`, `childProfileGuardado(niño)` y `departuresGuardadas(sala)`. Confirmar un momento también actualiza la rutina guardada.
- Si no se puede leer lo guardado (por ejemplo, porque cambió un DTO), se ignora y se espera a la API.
- Las imágenes llegan con URL firmada (cambia en cada carga): `ui/components/ImagenEstable.kt` le da a Coil la URL sin firma como llave, para que no las vuelva a bajar.

## Cómo agregar un repositorio o una función

1. Agrega el endpoint a `CodeaApi` y sus DTOs en `data/api/`.
2. Crea la función aquí, devolviendo modelos de `domain/` (no DTOs).
3. Una escritura se llama desde el ViewModel con `launchAction`, que avisa a `DataEvents` al terminar.

## Pendiente

- **Sin conexión:** la docente ya marca asistencia sin red (cola en Room) y los tres roles ven lo último guardado (caché de respuestas). Falta que las demás escrituras (reportes, resúmenes, salidas, las del admin y del padre) esperen la red como la asistencia (RF-11, RNF07).
- **Interfaces:** el marco pide DI manual con interfaces y contenedores. `AppContainer` ya existe y entrega la sesión; falta pasar por él los demás repositorios (hoy cada ViewModel crea el suyo) y ponerles interfaz para poder probar los ViewModels con fakes.
- **Fotos:** comprimir y corregir orientación (EXIF) antes de subir, como pide el marco. Hoy `uploadPhoto` solo comprime el bitmap a JPEG 85.
