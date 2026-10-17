# Marco técnico (regla del proyecto)

> **Regla:** el marco técnico del proyecto no puede salir de lo que lista <https://startdroid.pages.dev/> (materia TC2007B).
> Si hace falta algo fuera de esa lista, **no se agrega**: se anota en [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md) y lo aprueba el responsable del equipo.
> Esto aplica a librerías, servicios, infraestructura y patrones, tanto en la app como en el backend.

Resumen tomado del sitio el 2026-10-16. Si hay duda, el sitio manda: vuelve a leerlo antes de agregar algo.

**Ya existen y se usan en lugar de repetirlos aquí:** [`estilo.md`](estilo.md) (convenciones de las prácticas: capas, nombres, estado, red, sesión, Hilt, pruebas), y los planes en [`plan/`](plan/README.md). Este archivo solo agrega la regla de excepciones y el veredicto de cada pieza del proyecto.

## Lo que el marco permite

| Tema | Permitido |
|---|---|
| UI | Jetpack Compose, Material 3, Navigation |
| Estado | ViewModel, StateFlow, Flow |
| Concurrencia | Corrutinas, Dispatchers, concurrencia estructurada |
| Red | Retrofit, OkHttp, DTOs, SSE, WebSocket (FastAPI) |
| Persistencia local | Room (SQLite: Entity, DAO, Database), DataStore (tokens) |
| Seguridad | JWT, Android Keystore, renovación de sesión (el token expira cada 5 min en la Práctica 6) |
| Inyección de dependencias | Manual primero (interfaces y contenedores); después Hilt con KSP |
| Segundo plano | WorkManager. Notificaciones **sin Firebase ni segunda app** |
| Medios | Coil, Photo Picker, FileProvider, EXIF (comprimir antes de subir), Multipart |
| Pruebas | JUnit, Compose UI Test, pruebas de mutación, servidores falsos, pytest |
| Backend | FastAPI (REST y WebSocket), PostgreSQL, Docker, RustFS o almacén compatible con S3 |
| Arquitectura | MVVM sobre un dominio chico, patrón Repository, capa de dominio sin Android, una sola fuente de verdad |

## Lo que el marco excluye

- **Firebase** (notificaciones y tiempo real) y **Google Play Services** para push.
- Instalar algo extra para que funcione la app ("sin que el usuario instale nada más").
- Depender de internet donde se pide funcionar sin conexión.

## Estado del proyecto frente al marco

| Elemento | Veredicto | Detalle |
|---|---|---|
| Compose, Material 3, Navigation, ViewModel, corrutinas | ✅ | |
| Retrofit, OkHttp, kotlinx.serialization | ✅ | kotlinx.serialization no aparece en el resumen del sitio, pero es el convertidor natural de Retrofit; confirmar. |
| Coil | ✅ | Solo se usa en la pantalla de fotos del padre. |
| FastAPI, PostgreSQL, Docker, pytest, JUnit | ✅ | |
| Almacén S3 | 🟡 | Se usa SeaweedFS (compatible con S3). El sitio nombra RustFS. Confirmar que cuenta. |
| Aiven como Postgres hospedado | 🟡 | Es dónde corre, no una tecnología nueva. |
| **Firebase Auth** (backend `AUTH_MODE=firebase`, tareas "Configurar Firebase" y "JWT con Firebase") | ⚠️ | Choca con la exclusión de Firebase. El equipo ya diseñó el reemplazo con JWT propio ([`plan/04-api-login-jwt.md`](plan/04-api-login-jwt.md)); falta implementarlo y las tareas de Teams que mencionan Firebase deben cambiarse. |
| **Notificaciones con Firebase** (tarea pendiente) | ❌ | Excluido. Alternativa: WorkManager + notificaciones locales. |
| **Google Maps Compose** (mapa del checador) | ⚠️ | Usa Play Services; no está en la lista. **Requiere decisión.** |
| **`reorderable`** (arrastrar momentos de la rutina) | ⚠️ | Librería de terceros fuera de la lista. **Requiere decisión.** |
| **Redis, Grafana, Cloudflare tunnel** (tareas pendientes) | ⚠️ | No están en la lista. **Requieren decisión.** |
| **`androidx.biometric`** (huella, RF-13) | ⚠️ | No está en la lista. **Requiere decisión.** |
| Dictado por voz con `RecognizerIntent` | 🟡 | Es una API del sistema Android, sin dependencia nueva, pero usa el reconocedor de Google. Confirmar. |
| `mutableStateOf` en `CodeaViewModel` y `ParentViewModel` | 🟡 | El marco pide StateFlow/Flow. Migrar. |
| Sin DI (repositorios creados dentro de los ViewModels) | 🟡 | El marco pide DI manual con interfaces y contenedores. |

## Lo que el marco pide y todavía no existe en el proyecto

Room, DataStore, WorkManager, Android Keystore, Photo Picker/FileProvider, EXIF/compresión previa a la subida, Compose UI Test, WebSocket o SSE para el chat, pruebas con servidor falso.
Casi todo coincide con requisitos sin cumplir (sin conexión, login, notificaciones): ver [`REQUISITOS.md`](REQUISITOS.md).

## Cómo pedir una excepción

1. Antes de escribir código, agrega una fila en `DECISIONES-PENDIENTES.md`: qué se necesita, por qué, alternativa dentro del marco y costo de usar esa alternativa.
2. Espera la aprobación del responsable.
3. Si se aprueba, registra la decisión (fecha y quién) en esa misma tabla y menciónala en la sección de desviaciones de la entrega final.
