# `data/api/`

Todo lo que sabe de HTTP.

| Archivo | Qué hace |
|---|---|
| `CodeaApi.kt` | Interfaz Retrofit: una función `suspend` por endpoint de la API (con sesión) |
| `AuthApi.kt` | Los endpoints de entrada: `login`, `activate`, `refresh` y `logout`. Va con un cliente HTTP **sin** token ni autenticador |
| `ApiClient.kt` | Arma Retrofit + OkHttp + kotlinx.serialization (`ignoreUnknownKeys`) con `AuthInterceptor` y `TokenAuthenticator`; `createAuthApi()` para entrar |
| `AuthInterceptor.kt` | Pone `Authorization: Bearer <access token>` (menos en `/auth/`) y cierra la sesión si la API dice `USER_INACTIVE` |
| `TokenAuthenticator.kt` | Con un 401 renueva el token y repite la petición una sola vez |
| `ApiConfig.kt` | `BASE_URL` (de `BuildConfig`), `userKey` y las banderas de rol (`isAdmin`, `isTeacher`, `isParent`) |
| `ApiError.kt` | `Throwable.toUserMessage()`: convierte un error de red o un Problem Details de la API en texto para el snackbar |
| `Dtos.kt`, `TeacherDtos.kt`, `ParentDtos.kt`, `AuthDtos.kt` | Objetos de transferencia (`@Serializable`), organizados por rol |

## Cómo funciona la sesión

1. `login`/`activate` devuelven `access_token` (5 min) y `refresh_token` (7 días) más `me`; `SessionRepository` los guarda cifrados.
2. `AuthInterceptor` agrega el access token a cada petición.
3. Si la API contesta **401**, `TokenAuthenticator` llama a `SessionRepository.refresh(tokenQueFalló)`. Tres guardas evitan ciclos: la petición no traía token, era de `/auth/`, o ya era un reintento.
4. El refresh está **serializado** (un `Mutex`) y no se repite si otra petición ya renovó: con 4 peticiones simultáneas y el token vencido hay **un solo** `POST /auth/refresh`. Está probado (`TokenAuthenticatorTest`) y se vio en el servidor.
5. Si el refresh responde 401/403, la sesión terminó: se borra y `CodeaApp` muestra el inicio de sesión. Si no hay red o el servidor falla (5xx), la sesión se conserva.
6. Los errores de la API son Problem Details (`title`, `detail`, `code`, `status`). `toUserMessage()` muestra `detail` o, si no hay, `title` (la API ya los manda en español).
7. El registro de OkHttp está en nivel `BASIC` y oculta `Authorization`: el cuerpo trae tokens y datos de menores, así que nunca se imprime.

## Dirección de la API

`ApiConfig.BASE_URL = BuildConfig.API_BASE_URL`, que sale del build (ver `app/README.md`). Emulador: `http://10.0.2.2:8000/api/v1/`. Celular físico por USB: `http://localhost:8000/api/v1/` y `adb reverse tcp:8000 tcp:8000 && adb reverse tcp:9000 tcp:9000`.

## Pendiente

- **HTTPS en release:** definir `API_BASE_URL_RELEASE` cuando haya dominio.
- **Endpoints que faltan en `CodeaApi`** porque la API tampoco los tiene: eventos y citas, resumen semanal.
- **Tiempo real del chat:** hoy se consulta con `?after=`; el marco permite SSE o WebSocket.
- **Traducir `code`** de los errores a mensajes propios cuando el texto de la API no sirva.
- **Fijar el certificado (pinning)** del servidor, si se pide en la parte de seguridad.
