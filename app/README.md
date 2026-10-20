# App Android (`app/`)

Módulo único. Paquete `mx.tec.codea`, minSdk 24. Kotlin, Jetpack Compose, Material 3, Navigation Compose, Retrofit/OkHttp con kotlinx.serialization, Coil, DataStore y Room + WorkManager (sin conexión).
Marco técnico: solo lo de <https://startdroid.pages.dev/> ([`docs/MARCO-TECNICO.md`](../docs/MARCO-TECNICO.md)). La app depende hoy de una cosa fuera de la lista: `reorderable` ([`docs/DECISIONES-PENDIENTES.md`](../docs/DECISIONES-PENDIENTES.md)).

## Comandos

```bash
./gradlew assembleDebug
./gradlew installDebug
./gradlew test                                   # 140 pruebas JVM (dominio, sesión, Mi sala, pase de salida, invitación, cola sin conexión)
./gradlew connectedAndroidTest                   # necesita dispositivo; solo hay un ejemplo vacío
```


## Capas

`ui/screens` → `ui/state` → `data` → `domain`. Una capa solo conoce a las de su derecha.

| Carpeta | Qué hay | README |
|---|---|---|
| `domain/` | Modelos y reglas en Kotlin puro | [`domain/README.md`](src/main/java/mx/tec/codea/domain/README.md) |
| `data/` | Repositorios, sesión guardada (`local/`) y sin conexión (`offline/`: Room, `ApiCache`) | [`data/README.md`](src/main/java/mx/tec/codea/data/README.md) |
| `data/api/` | Retrofit, DTOs, errores | [`data/api/README.md`](src/main/java/mx/tec/codea/data/api/README.md) |
| `ui/state/` | ViewModels | [`ui/state/README.md`](src/main/java/mx/tec/codea/ui/state/README.md) |
| `ui/navigation/` | Rutas, grafo, barra inferior | [`ui/navigation/README.md`](src/main/java/mx/tec/codea/ui/navigation/README.md) |
| `ui/screens/` | Pantallas de la docente | [`ui/screens/README.md`](src/main/java/mx/tec/codea/ui/screens/README.md) |
| `ui/screens/admin/` | Pantallas del administrador | [`ui/screens/admin/README.md`](src/main/java/mx/tec/codea/ui/screens/admin/README.md) |
| `ui/screens/parent/` | Pantallas del padre | [`ui/screens/parent/README.md`](src/main/java/mx/tec/codea/ui/screens/parent/README.md) |
| `ui/components/` | Piezas reutilizables y lanzadores de cámara/voz | [`ui/components/README.md`](src/main/java/mx/tec/codea/ui/components/README.md) |
| `ui/theme/` | Colores, tipografía, tema | [`ui/theme/README.md`](src/main/java/mx/tec/codea/ui/theme/README.md) |
| `src/test/` | Pruebas | [`src/test/README.md`](src/test/README.md) |

## Cómo arranca

1. `CodeaApplication` crea el `AppContainer` (DI manual) y este lee del teléfono la sesión guardada.
2. `CodeaApp` dibuja según el estado de la sesión (`SessionViewModel`): **Cargando** → arranque, **Anónimo** → iniciar sesión / activar cuenta, **Activa** → la app del rol. Cerrar sesión no navega: solo cambia el estado.
3. El rol sale de la sesión (`GET /me` → `roles`). Con un solo rol se entra directo; con varios, `RoleSelectionScreen` deja elegir. Las banderas `ApiConfig.isAdmin/isTeacher/isParent` siguen a ese rol.
4. Cada sesión tiene su propio almacén de ViewModels (`SessionScopesViewModel`): sobrevive a girar la pantalla y se borra al cerrar sesión, así lo que veía una persona nunca llega a la siguiente.
5. `key(role)` crea un `NavController` nuevo por rol; la barra inferior muestra solo las pestañas de ese rol.
6. Cada ViewModel pide sus datos a la API y se vuelve a cargar cuando `DataEvents.changed` avisa que algo cambió.

## Estado

| Rol | Estado | Falta |
|---|---|---|
| Docente | Check-in sin ubicación, pase de lista, rutina del día, reportes (escrito, dictado, foto), Mi sala, ficha y resumen del día del niño, pase de salida, chat con fotos, avisos, Mi cuenta. Asistencia sin conexión y caché de lo último que vio | Documentos de la docente (se quitó del Menú); chat en tiempo real; las demás escrituras sin conexión |
| Admin | Pestañas Avisos (con imagen), Docentes (con Salas), Familias (infantes y padres; vincular un padre a más niños), Ajustes (rutinas, horario, menú del día, personas autorizadas, Mi cuenta); **código de acceso** al dar de alta o reenviar | Calendario de eventos y citas; bitácoras para imprimir |
| Padre | Hoy (con menú del día), calendario con resumen semanal, fotos (borrar directo), chat, Mi hijo (datos y salud), nota a la asistente, avisos, selector de hijo, Mi cuenta | Paginación de fotos |
| Transversal | Compose, Retrofit, Coil, **login con JWT** (tokens cifrados con Keystore en DataStore, renovación automática), DI manual, Room (versión 5: cola de asistencia y caché de respuestas; la caché se borra al cerrar sesión), WorkManager, HTTPS contra `api.codeaapp.site` | Huella, release firmado y pruebas de interfaz |

Detalle completo: [`docs/ESTADO.md`](../docs/ESTADO.md) y [`docs/REQUISITOS.md`](../docs/REQUISITOS.md).

## Configuración del release

`app/build.gradle.kts`: `release` con `optimization.enable = false` y sin `signingConfig`: para la entrega hay que firmar y minificar.

La dirección de la API sale de `BuildConfig.API_BASE_URL`: `-PapiBaseUrl=...` en la línea de comandos, o `API_BASE_URL` en `local.properties`, o el emulador (`http://10.0.2.2:8000/api/v1/`) por omisión. En release va a producción, `https://api.codeaapp.site/api/v1/` (túnel de Cloudflare, ver `guarderia-api/README.md`), salvo que `API_BASE_URL_RELEASE` diga otra. Los enlaces de invitación que abren la app son los de `api.codeaapp.site` (`INVITE_HOST` lo cambia). El tráfico sin cifrar solo se permite en debug y solo a `10.0.2.2` y `localhost` (`src/debug/res/xml/network_security_config.xml`). `allowBackup="false"`.

El manifiesto declara `CAMERA`, `RECORD_AUDIO` e `INTERNET`.
