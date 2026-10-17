# App Android (`app/`)

Módulo único. Paquete `mx.tec.codea`, minSdk 24. Kotlin, Jetpack Compose, Material 3, Navigation Compose, Retrofit/OkHttp con kotlinx.serialization y Coil.
Marco técnico: solo lo de <https://startdroid.pages.dev/> ([`docs/MARCO-TECNICO.md`](../docs/MARCO-TECNICO.md)). La app depende hoy de dos cosas fuera de la lista: Google Maps Compose y `reorderable` ([`docs/DECISIONES-PENDIENTES.md`](../docs/DECISIONES-PENDIENTES.md)).

## Comandos

```bash
./gradlew assembleDebug
./gradlew installDebug
./gradlew test                                   # 11 pruebas JVM de dominio
./gradlew connectedAndroidTest                   # necesita dispositivo; solo hay un ejemplo vacío
```

`MAPS_API_KEY=...` en `local.properties` (ignorado por git) para que se vea el mapa del checador.

## Capas

`ui/screens` → `ui/state` → `data` → `domain`. Una capa solo conoce a las de su derecha.

| Carpeta | Qué hay | README |
|---|---|---|
| `domain/` | Modelos y reglas en Kotlin puro | [`domain/README.md`](src/main/java/mx/tec/codea/domain/README.md) |
| `data/` | Repositorios | [`data/README.md`](src/main/java/mx/tec/codea/data/README.md) |
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

1. `CodeaApp` muestra `RoleSelectionScreen` (no hay login real).
2. El rol elegido fija `ApiConfig.devEmail` (un correo del seed) y las banderas `isAdmin`/`isTeacher`/`isParent`.
3. `key(role)` crea un `NavController` nuevo por rol; la barra inferior muestra solo las pestañas de ese rol.
4. Cada ViewModel pide sus datos a la API y se vuelve a cargar cuando `DataEvents.changed` avisa que algo cambió.

## Estado

| Rol | Estado | Falta |
|---|---|---|
| Docente | Check-in con GPS, rutina del día, reportes (escrito, dictado, foto), Mi sala con asistencia, chat | Documentos y perfil (abren `ComingSoon`); chat en tiempo real; sin conexión |
| Admin | Centro y salas, docentes, infantes, padres, rutinas y catálogo de momentos, publicar avisos y ver su historial | Solicitudes de borrado de fotos, pagos, menú del día |
| Padre | Hoy, calendario, fotos, documentos y personas autorizadas, nota a la asistente | Chat, varios hijos, menú del día, paginación de fotos |
| Transversal | Compose, Retrofit, Coil | Login real, sesión con DataStore/Keystore, Room, WorkManager, huella, release firmado, HTTPS |

Detalle completo: [`docs/ESTADO.md`](../docs/ESTADO.md) y [`docs/REQUISITOS.md`](../docs/REQUISITOS.md).

## Configuración del release

`app/build.gradle.kts`: `release` con `optimization.enable = false` y sin `signingConfig`. El manifiesto declara `CAMERA`, `RECORD_AUDIO`, `INTERNET` y `usesCleartextTraffic="true"`. Para la entrega hay que firmar, minificar y pasar a HTTPS.
