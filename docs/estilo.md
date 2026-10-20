# Estilo y estructura de Codea

Codea sigue las convenciones de las prácticas del curso ([startdroid.com](https://startdroid.com/), prácticas 2 a 12 y sus anexos). Esta guía resume lo básico: dónde va cada cosa, cómo se nombra y qué patrón se usa. Lo que el curso no cubre se decide con criterio y se anota en [`plan/`](plan/README.md).

Cada sección dice de qué práctica sale, para ir a ver el ejemplo completo.

## Capas y paquetes

Práctica 2 (base), 4 (red), 6 (sesión), 8 (segundo plano) y 9 (Hilt).

```text
mx/tec/codea/
├── CodeaApplication.kt     # @HiltAndroidApp; canales de notificación, WorkManager
├── MainActivity.kt         # @AndroidEntryPoint; solo tema + CodeaApp
├── domain/                 # Kotlin puro: modelos, reglas, validadores. Cero imports de androidx
├── data/
│   ├── *Repository.kt      # la única puerta a los datos; regresa modelos de domain/
│   ├── local/              # DataStore, Room, cifrado
│   └── remote/             # Retrofit: *Api.kt, Dtos.kt, Mappers.kt, Network.kt, interceptores
├── di/                     # módulos de Hilt
├── ui/
│   ├── navigation/         # CodeaApp, NavHost, rutas, tabs
│   ├── screens/            # pantallas sin estado, una carpeta por rol o flujo
│   ├── components/         # piezas reutilizables (CargandoView, ErrorView, avatares…)
│   ├── state/              # ViewModels y UiState
│   └── theme/              # colores, tipografía, formas, espaciado
├── trabajo/                # Workers de WorkManager
└── notificaciones/         # canal, permiso y Notificador
```

La regla de dependencias: `ui` conoce a `data` y a `domain`; `data` conoce a `domain`; `domain` no conoce a nadie.

## Nombres

- **El archivo se llama como el tipo principal que contiene**, en PascalCase: `FamiliasScreen.kt` para `FamiliasScreen`.
- **Sufijos:** `…Screen`, `…ViewModel`, `…UiState`, `…Repository`, `…Api`, `…Dto` (lo que llega), `…Body` (lo que se manda), `…Validator`, `…Error`, `…Worker`, `…Test`; los fakes de prueba empiezan con `Fake…`.
- **Identificadores en inglés y textos en español**, como en la práctica 2 y en la API. Los textos de la interfaz viven en `strings.xml`, nunca en el código.
- **Glosario** para no tener dos nombres de la misma cosa:

| En la app (pantalla) | En el código y la API |
| --- | --- |
| Sala | `Classroom` |
| Docente, asistente, "miss" | `Teacher` |
| Infante, niño | `Child` |
| Padre, tutor | `Parent` / `Guardian` |
| Checador, entrada | `CheckIn` (`staff-attendance`) |
| Momento, subproceso | `RoutineMoment` (hoy `Subprocess` en la app) |
| Aviso | `Announcement` |

Lo que hoy no cumple (se corrige cuando se toque cada archivo para conectarlo a la API):

- Archivos en minúscula o con otro nombre que su composable: `avisosAdminScreen.kt`, `crearSalaScreen.kt`, `docenteScreenAdmin.kt` (`DocenteScreen`), `infanteScreenAdmin.kt` (`InfanteScreen`), `salaDetalleScreen.kt` (`SalaAdminDetalleScreen`), etc.
- Modelos en español y en inglés para la misma persona (`Infante` y `Child`, `Docente` y `Teacher`): se unifican con el glosario.

## Dominio

Práctica 2.

- Modelos como `data class` simples; listas nuevas en lugar de mutarlas.
- Reglas en un `object` con sus constantes: `object ReportValidator { const val DESCRIPTION_MIN = 10 }`.
- Errores como `sealed interface` **sin texto** (`SalaError.NombreVacio`); la UI traduce a `strings.xml`.
- Las reglas reciben lo que necesitan como parámetro (por ejemplo, la hora actual), para poder probarlas.
- La API repite cada regla y tiene la última palabra; la app valida para responder al instante.

## Estado y ViewModels

Prácticas 2, 4, 6 y 12.

- **Un ViewModel compartido** se crea una vez en la raíz (`CodeaApp`) para lo que ven varias tabs; **uno local** por destino para formularios, que se borra solo al salir.
- **Estado de pantalla:** `var x by mutableStateOf(...)` con `private set`. Cuando la fuente ya es un `Flow` (sesión, Room, WebSocket): `StateFlow` con `stateIn(…, SharingStarted.WhileSubscribed(5_000), inicial)` y `collectAsStateWithLifecycle()` en la UI.
- **Formulario:** un `data class …UiState` con propiedades derivadas (`canSave`) y `copy()`.
- **Datos remotos:** `sealed interface UiState<out T>` con `Cargando`, `Exito(datos)` y `Error(mensaje)`. El NavHost hace un `when` exhaustivo: `CargandoView()`, `ErrorView(...)` o la pantalla.
- **Eventos** con nombre de verbo (`onPublicar()`, `confirmSubprocess()`); los avisos de una sola vez (un mensaje nuevo, "guardado") van por `SharedFlow`.
- **El ViewModel no navega:** cambia el estado y la UI reacciona.

## Pantallas y componentes

Prácticas 2 y 7.

- Las pantallas **no tienen estado**: reciben datos y lambdas, nunca un `NavController` ni un ViewModel. El NavHost es el que las conecta.
- `modifier: Modifier = Modifier` es el último parámetro.
- Componentes reutilizables en `ui/components/`, con state hoisting (`valor` + `onValorChange`).
- `@Preview` privadas, envueltas en `CodeaTheme`, con datos falsos; una clara y una oscura cuando importa.

## Navegación

Práctica 2 (y la app ya va un paso adelante con rutas tipadas).

- Rutas `@Serializable` en `Routes.kt`; por la ruta viaja **el id, nunca el objeto**.
- Un `navigation<…Graph>` por tab cuando la tab tiene varias pantallas.
- **El login no es una ruta:** `CodeaApp` hace un `when` sobre el estado de la sesión (práctica 6). Cerrar sesión quita el NavHost entero; no se navega a ningún lado.
- Las tabs se filtran por rol; ocultar algo es comodidad, el permiso real lo da la API.

## Red

Práctica 4.

- Retrofit + OkHttp + kotlinx.serialization (`ignoreUnknownKeys = true`, `explicitNulls = false`).
- `Network.kt` con `BASE_URL` y la creación del cliente (con Hilt, en `di/RedModule.kt`).
- Una interfaz `…Api.kt` con funciones `suspend`; DTOs en `Dtos.kt`; conversión en `Mappers.kt` con `toDomain()`. El `id` y lo que calcula el servidor no van en los `…Body`.
- El repositorio regresa modelos de `domain/`, nunca DTOs.
- Errores: `IOException` (sin red) por separado de `HttpException` (respondió el servidor); un `ApiErrors.kt` traduce el `code` de la API a un texto.
- Qué hacer con cada respuesta (guía de síntesis): `401` en login = credenciales; `401` con token vencido = refrescar una vez; `403` = avisar, no reintentar; `409` = conflicto o duplicado; `422` = corregir el contenido; `429`/`503` = esperar; `CancellationException` se deja pasar.
- `HttpLoggingInterceptor` con `redactHeader("Authorization")`, para que el token no salga en Logcat.

## Sesión

Práctica 6 y el anexo de identidad.

- `SessionRepository` es la única puerta a la sesión y expone un `Flow<Session?>`.
- Tokens en DataStore, **cifrados con una llave de Android Keystore (AES-GCM)**. No `EncryptedSharedPreferences` (obsoleto). `android:allowBackup="false"`.
- `AuthInterceptor` pone el `Bearer`; `TokenAuthenticator` refresca ante un `401` con tres guardas (sin header, la petición era el refresh, ya se reintentó) y serializa los refresh.
- `SessionViewModel` con `Cargando`, `Anonimo` y `Activa(session)`.
- Cerrar sesión: primero se borra lo local y luego se avisa al servidor, ignorando si falla.
- Las reglas de rol viven en `domain/` (`session.canPublish`); el servidor decide de verdad.

## Inyección de dependencias

Práctica 9.

- Hilt con **KSP** (no kapt): `@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel` + `hiltViewModel()` (de `androidx.hilt.lifecycle.viewmodel.compose`).
- Módulos en `di/`: `@Binds` para decir qué implementación usa cada interfaz; `@Provides` para lo que no se puede anotar (Retrofit, OkHttp, `ImageLoader`).
- Repositorio = interfaz + implementación real; cambiar a un fake es cambiar una línea del módulo.

## Tiempo real, notificaciones y segundo plano

Prácticas 8 y 12.

- **Con la app abierta:** SSE (`okhttp-sse` con `callbackFlow` + `awaitClose`) para avisos, o WebSocket para chat. Se conecta solo mientras la pantalla está visible (`LifecycleStartEffect`).
- **Con la app cerrada:** WorkManager periódico (15 min, con red) que pregunta por lo nuevo (`?desde=` / `?after=`) y notifica. **FCM está descartado** en el curso (pide Play Services y una cuenta de Firebase).
- **Chat por WebSocket:** OkHttp, mensajes JSON con un campo `tipo`, `pingInterval` de 20 s, `StateFlow` para el estado y `SharedFlow` para los eventos; cada callback compara que sea el socket actual.
- **Notificaciones:** un canal creado en `CodeaApplication`, permiso `POST_NOTIFICATIONS` pedido después de entrar (no al arrancar), y `notify(id, …)` con el id del registro para no duplicar.

## Imágenes

Práctica 10 y anexo de imágenes.

- Coil 3 con un `ImageLoader` que usa el mismo `OkHttpClient` de la app; `AsyncImage` con proporción fija y placeholder para que la lista no brinque.
- Reducir la imagen antes de subirla; la cámara guarda su estado en `SavedStateHandle`, nunca un `Bitmap` en `rememberSaveable`.
- **Diferencia de Codea:** el curso sube multipart a la API; Codea pide una URL firmada y sube directo al almacén (ver [`guarderia-api/README.md`](../guarderia-api/README.md#fotos-y-documentos)).

## Diseño

Práctica 7.

- En `ui/theme/`: `Color.kt` (esquemas claro y oscuro), `Type.kt`, `Shape.kt` y `Espaciado.kt` (seis pasos: 4, 8, 12, 16, 24, 32 dp).
- **Color por rol, no por número:** nadie escribe `Color(0xFF…)` en una pantalla. Hoy hay unos 250 en `ui/screens/`; se mueven al tema (o a `DomainColors.kt`) conforme se toquen las pantallas.
- El modo oscuro sigue al sistema; sin color dinámico, para que la app se vea igual en todos los teléfonos.
- Accesibilidad: `contentDescription` en íconos e imágenes con significado, áreas táctiles de 48 dp y textos que crecen con la fuente del sistema.

## Pruebas

Práctica 11.

- `app/src/test` para Kotlin puro (reglas, mapeos, fechas); `app/src/androidTest` para lo que necesita Android (ViewModels con fakes, Compose).
- JUnit 4 y `kotlinx-coroutines-test`; `Dispatchers.setMain(...)` en `@Before` y `resetMain()` en `@After`.
- Fakes con prefijo `Fake` que implementan la interfaz (no Retrofit); lo que la prueba no usa lanza `error("no se usa en estas pruebas")`.
- Cada prueba: preparar, actuar, comprobar. Probar las orillas de cada regla (9 y 10 caracteres, 60 y 61 m).
- En Compose, buscar por texto o por `contentDescription`, como lo haría TalkBack, no por `testTag`.
- Nombres: el curso usa `snake_case` en español (`tocar_publicar_dos_veces_manda_un_solo_aviso`); las pruebas actuales de `domain/` usan inglés (`late_registersButMarksTheDelay`). Dentro de un archivo, un solo estilo; los archivos nuevos siguen al curso.

## Comentarios

Práctica 2.

- En español, cortos y sobre **por qué**, no sobre qué hace la línea.
- KDoc (`/** … */`) solo cuando dos piezas se pueden confundir.
- `// provisional` para lo que se va a reemplazar (datos de muestra, simulaciones).
- Parte del código de docente tiene comentarios en inglés; no se traducen de golpe, se cambian al tocar el archivo.

## API (FastAPI)

Anexos de identidad, de imágenes y del servidor con pruebas.

- Configuración con `pydantic-settings` y `.env` fuera de git; los secretos (`JWT_SECRET`) **no tienen valor por omisión** y la API no arranca sin ellos.
- Contraseñas con `pwdlib[argon2]` (no `passlib`); JWT con `PyJWT` (no `python-jose`), `HS256` fijado al verificar.
- Refresh token aleatorio, guardado solo como hash SHA-256 y rotado en cada uso; el mismo `401` para usuario desconocido y contraseña mal.
- Permisos con una dependencia `require_role(...)` y, además, filtro por registro en el servicio.
- Pruebas con pytest contra Postgres real.
- **Diferencias de Codea:** cada módulo separa `router`, `service`, `schemas` y `models` (el anexo tiene un solo `routers/`); los errores usan Problem Details con `code` en mayúsculas (`OUT_OF_RANGE`) en lugar de `{error, code, hint}`; el rol no va en el JWT, se lee de Postgres en cada request porque una persona puede tener varios roles y la baja debe cortar el acceso al instante.
