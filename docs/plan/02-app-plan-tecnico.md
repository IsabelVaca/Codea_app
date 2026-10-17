# App: plan técnico para conectarla a la API

Los repositorios en memoria son el único lugar donde entra la API: las pantallas siguen recibiendo datos y lambdas, y los ViewModels conservan su forma, pero pasan a llamar funciones `suspend` dentro de `viewModelScope`. Hoy la app no tiene cliente HTTP, ni permiso de `INTERNET`, ni sesión.

Todo sigue los patrones de las prácticas del curso, resumidos en [`../estilo.md`](../estilo.md): la red como la práctica 4, la sesión como la 6, Hilt como la 9 y las imágenes como la 10.

## Dependencias nuevas

| Para qué | Librería | Nota |
| --- | --- | --- |
| HTTP | Retrofit + OkHttp + `logging-interceptor` | Con el convertidor de kotlinx.serialization, que el proyecto ya usa para las rutas |
| JSON | `kotlinx-serialization-json` | `@SerialName("snake_case")` en los DTO, o `JsonNamingStrategy.SnakeCase` |
| Sesión | DataStore (Preferences) + Android Keystore | Tokens cifrados con AES-GCM, como el `Cifrador` de la práctica 6; no `EncryptedSharedPreferences` |
| Inyección | Hilt con KSP (`hilt-android`, `ksp(hilt-compiler)`, `hilt-lifecycle-viewmodel-compose`) | Como la práctica 9; nada de kapt |
| Imágenes | Coil (`coil-compose` + `coil-network-okhttp`) | Para Fotos y fotos de reportes; misma instancia de OkHttp |
| Ubicación | `play-services-location` | `FusedLocationProviderClient` para el checador |
| Corrutinas en ViewModel | `lifecycle-viewmodel-ktx` | Ya viene con `lifecycle-runtime-ktx` |

En `AndroidManifest.xml`: `INTERNET` y `ACCESS_FINE_LOCATION`. En `res/xml/network_security_config.xml`, permitir HTTP solo a `10.0.2.2` y solo en debug (la API local no tiene TLS).

## Configuración por entorno

`BASE_URL` en `buildConfigField`, distinta por `buildType`:

| Build | `BASE_URL` |
| --- | --- |
| debug (emulador) | `http://10.0.2.2:8000/api/v1/` |
| debug (teléfono físico en la misma red) | `http://<IP de la compu>:8000/api/v1/`, desde `local.properties` |
| release | `https://api.<dominio>/api/v1/` |

La API firma las URLs de fotos con `S3_PUBLIC_ENDPOINT_URL`; para el emulador debe ser `http://10.0.2.2:9000` (ver `guarderia-api/README.md`).

## Estructura

Se agregan `data/remote/`, `data/local/` y `di/`; `domain/` y `ui/` no cambian de lugar.

```text
mx/tec/codea/
├── CodeaApplication.kt        # @HiltAndroidApp
├── di/
│   ├── RedModule.kt           # @Provides OkHttp, Retrofit, las *Api, ImageLoader
│   └── RepositoriosModule.kt  # @Binds interfaz -> implementación real
├── data/
│   ├── remote/
│   │   ├── Network.kt         # BASE_URL y configuración de JSON
│   │   ├── *Api.kt            # interfaces de Retrofit: AuthApi, TeachersApi, ChildrenApi…
│   │   ├── Dtos.kt            # clases @Serializable iguales al JSON (…Dto y …Body)
│   │   ├── Mappers.kt         # toDomain()
│   │   ├── AuthInterceptor.kt
│   │   └── TokenAuthenticator.kt
│   ├── local/
│   │   ├── SessionStore.kt    # DataStore
│   │   └── TokenCipher.kt     # AES-GCM con llave en Android Keystore
│   ├── SessionRepository.kt
│   └── *Repository.kt         # los mismos nombres de hoy, ahora llaman a la API
├── domain/                    # modelos y reglas (sin cambios de lugar)
└── ui/
    ├── screens/auth/          # Splash, Login, ActivateAccount, RecoverPassword, ChangePassword
    ├── screens/account/       # AccountScreen (admin y padre)
    ├── screens/parent/chat/   # tab nuevo
    └── state/                 # SessionViewModel y los ViewModels nuevos
```

**Inyección:** Hilt, como la práctica 9. Cada repositorio es una interfaz con su implementación real; el módulo dice cuál usar, así que volver a los datos de prueba (los repositorios en memoria de hoy) es cambiar una línea. Los ViewModels pasan a `@HiltViewModel` con `@Inject constructor` y las pantallas los piden con `hiltViewModel()`. Hoy cada ViewModel crea su repositorio (`private val repository = SalaRepository()`); eso es lo que se quita.

## Sesión y token

| Pieza | Qué hace |
| --- | --- |
| `SessionRepository` | La única puerta a la sesión. Guarda los tokens cifrados en `SessionStore` y expone `session: Flow<Session?>`, que cambia cada vez que se guarda o se borra |
| `AuthInterceptor` | Pone `Authorization: Bearer <access>` en cada request, menos en `/auth/*` |
| `TokenAuthenticator` | Ante un 401, refresca **una sola vez** con las tres guardas de la práctica 6 (la petición no traía token, era el propio refresh, o ya se reintentó) y `synchronized` para que 5 requests a la vez no hagan 5 refresh. Si el refresh responde 401, borra la sesión; si no hay red, la conserva |
| `SessionViewModel` | `Cargando`, `Anonimo` o `Activa(session)`, con `stateIn(WhileSubscribed(5_000))`. En `Activa` pide `GET /me` para saber roles, sala o hijos |
| Cerrar sesión | Primero borra lo local, luego `POST /auth/logout` ignorando si falla; los ViewModels compartidos desaparecen con el NavHost |

`CodeaApp` cambia así: en lugar de `var selectedRole by rememberSaveable`, hace un `when` sobre el estado de la sesión, como `AvisosApp` en la práctica 6. `Cargando` muestra `CargandoView`; `Anonimo` dibuja un `NavHost` chico con Iniciar sesión, Activar cuenta y Recuperar contraseña; `Activa` dibuja `RoleApp` con `key(role)`, como hoy. El rol ya no se elige: lo dice `GET /me` (`roles`).

## Errores

La API responde siempre Problem Details con un `code` estable (`OUT_OF_RANGE`, `CHILD_ABSENT`, `INVALID_CREDENTIALS`…).

- Las pantallas que cargan datos usan `UiState` de la práctica 4: `Cargando`, `Exito(datos)` o `Error(mensaje)`, y el NavHost elige `CargandoView`, `ErrorView` o la pantalla.
- El repositorio separa `IOException` (sin red: "Sin conexión, intenta de nuevo") de `HttpException` (respondió la API).
- `ApiErrors.kt` lee el Problem Details del `HttpException` y traduce el `code` a un texto de `strings.xml`, igual que hoy `SalaError` o `InfanteError`. Un `code` desconocido muestra el `title` que mandó la API; los extras (por ejemplo `distance_m`) se leen del mismo JSON.
- Modo sin conexión (Room + WorkManager) queda para después.
- Los validadores de `domain/` se quedan: responden al instante; la API tiene la última palabra.

## Modelos

| Hoy en la app | En la API | Qué hacer |
| --- | --- | --- |
| `Child` (Mi sala) e `Infante` (admin) | `/children` y `RoomChild` | Un solo `Child` en `domain/` con los campos de los dos; cada pantalla usa los que necesita |
| `Teacher` (Menú) y `Docente` (admin) | `MeTeacher` y `/teachers` | Un solo modelo, o `Teacher` = la docente con sesión y `Docente` = la fila del admin, ambos llenados por la API |
| `nombre` en un solo texto | `name` + `surname` | Formularios con dos campos; `full_name` para mostrar |
| `edad` en Inscribir infante | `birth_date` (obligatorio) | Selector de fecha; la API regresa la edad calculada |
| Sala y asistente **por nombre** (`Infante.sala`, `asignarAsistente(nombre)`) | Por id (`classroom_id`, `teacher_ids`) | Cambiar a ids en ViewModels y rutas |
| Hijos del padre como texto separado por comas | `child_ids: [int]` | Lista de ids |
| Ids `String` en las rutas | Ids `int` (y `uuid` para personas) | Las rutas siguen con `String`; el mapper convierte |
| Horas `H:mm` y minutos desde medianoche | ISO 8601 en UTC; `date` para días | Convertir a la zona del teléfono en el mapper |

## Fotos y documentos

Los archivos no pasan por la API (ver `guarderia-api/README.md`):

1. `POST /media/uploads` con `content_type`, `size_bytes` y `kind` → `media_id`, `url` y `fields`.
2. POST multipart a `url` con todos los `fields` y el archivo en `file`. Se hace con el mismo OkHttp, **sin** el `AuthInterceptor`.
3. `POST /media/{id}/complete`.
4. El `media_id` va en `POST /reports` o en `PUT /children/{id}/documents/{doc_id}`.

Antes de subir: guardar el `Bitmap` de `ReportFormViewModel` como JPEG con el lado largo en unos 2048 px. Para el mínimo la subida corre en el ViewModel con una barra de progreso; pasarla a WorkManager (para que sobreviva si se cierra la app) queda para después. En Fotos, Coil usa como llave de caché el `media_id`, no la URL firmada, porque la URL cambia cada 10 minutos.

## Chat sin push

En el mínimo, el chat pregunta por lo nuevo:

- Con la conversación abierta: `GET /conversations/{id}/messages?after=<último id>` cada 10 segundos (un `while (isActive)` en el ViewModel, que se cancela al salir).
- Al abrir la bandeja o volver a la app: `GET /conversations`.
- Al enviar: el mensaje aparece al instante como "enviando", con un `client_id` (UUID) para que un reintento no lo duplique; la API regresa 200 si ya lo tenía.
- Al abrir la conversación: `POST /conversations/{id}/read`.

`ConversationScreen` hoy usa `isFromTeacher`; se cambia a `isMine` (la API ya lo manda en cada mensaje, junto con `author_name` e `is_read`) para que sirva igual a la docente y al padre.

Después del mínimo, el chat pasa a WebSocket como la práctica 12 (OkHttp, mensajes con `tipo`, ping cada 20 s, conectado solo con la pantalla visible), y los avisos con la app cerrada llegan con WorkManager periódico y notificaciones como la práctica 8. El curso descarta FCM.

## Orden de trabajo

Sigue las fases de [README.md](README.md#orden-de-trabajo).

1. **Base:** dependencias, permisos, `BASE_URL`, Hilt (`CodeaApplication`, `di/`), `UiState`, `ApiErrors.kt`, `CargandoView` y `ErrorView`.
2. **Sesión:** `SessionStore` con cifrado, `SessionRepository`, interceptor y authenticator, `SessionViewModel`, cambio de `CodeaApp`.
3. **Pantallas de entrada:** Arranque, Iniciar sesión, Activar cuenta, Mi cuenta y cerrar sesión. Probar con los 3 usuarios del seed.
4. **Docente:** `TeacherRepository` → `GET /me`; checador con ubicación real; `RoutineRepository`, reportes y fotos; `ChildRepository` (Mi sala); `ChatRepository`, bandeja y conversación.
5. **Padre:** Hoy, Calendario, Fotos (Coil), Documentos (selector de archivos del sistema) y el tab de Chat.
6. **Admin:** salas, docentes, infantes y padres por id; diálogo con el código de invitación.
7. **P1:** Día del niño, Avisos, Agregar autorizado, Cambiar y recuperar contraseña, selector de hijo.
8. **Limpieza:** borrar maquetas sin ruta y mover los `*SampleData.kt` (ver [01](01-app-pantallas.md#limpieza-que-conviene-hacer-antes)).

**Pruebas:** como la práctica 11. Las de `domain/` siguen igual; se agregan las de los mappers (JSON de ejemplo → modelo) en `test/`, y en `androidTest/` las de los ViewModels de login, reporte y chat con fakes (`FakeAuthApi`, `FakeChatRepository`).
