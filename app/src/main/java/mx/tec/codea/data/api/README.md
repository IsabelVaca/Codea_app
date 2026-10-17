# `data/api/`

Todo lo que sabe de HTTP.

| Archivo | Qué hace |
|---|---|
| `CodeaApi.kt` | Interfaz Retrofit: una función `suspend` por endpoint de la API |
| `ApiClient.kt` | Arma Retrofit + OkHttp + kotlinx.serialization (`ignoreUnknownKeys`) y el interceptor que agrega `Authorization` |
| `ApiConfig.kt` | `BASE_URL`, correo de desarrollo y banderas de rol (`isAdmin`, `isTeacher`, `isParent`) |
| `ApiError.kt` | `Throwable.toUserMessage()`: convierte un error de red o un Problem Details de la API en texto para el snackbar |
| `Dtos.kt`, `TeacherDtos.kt`, `ParentDtos.kt` | Objetos de transferencia (`@Serializable`), organizados por rol |

## Cómo funciona hoy

- `BASE_URL = http://10.0.2.2:8000/api/v1/` (el emulador ve la Mac así). Con un celular físico por USB: `http://localhost:8000/api/v1/` más `adb reverse tcp:8000 tcp:8000 && adb reverse tcp:9000 tcp:9000`.
- El interceptor manda `Authorization: Bearer dev:<correo>` con el correo que fija `CodeaApp` según el rol elegido. **Esto solo funciona con el backend en `AUTH_MODE=dev`.**
- Los errores de la API son Problem Details (`title`, `detail`, `code`, `status`). `toUserMessage()` muestra `detail` o, si no hay, `title`. El `code` aún no se traduce a textos propios.
- El logging de OkHttp está en nivel `BODY`: **imprime tokens y datos personales en Logcat**. Quitarlo o bajarlo a `NONE` en release.

## Pendiente

- **Login y sesión** ([decisión #1](../../../../../../../../../docs/DECISIONES-PENDIENTES.md)): reemplazar `dev:<correo>` por un JWT. Con el marco: DataStore + Keystore para guardar el token, y un `Authenticator` de OkHttp que renueve la sesión cuando el token (5 min) expire.
- **HTTPS:** cambiar `BASE_URL` a la URL desplegada y quitar `usesCleartextTraffic` del manifiesto.
- **Endpoints que faltan en `CodeaApi`** porque la API tampoco los tiene: `GET /announcements`, pagos, eventos y citas, resumen semanal, revisión de solicitudes de borrado.
- **Tiempo real del chat:** hoy se consulta con `?after=`; el marco permite SSE o WebSocket.
- **Traducir `code`** de los errores a mensajes propios en español.
