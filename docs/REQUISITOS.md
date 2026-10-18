# Requisitos y su estado

Catálogo tomado del tablero de Teams (buckets *Req. de CODEA*, *Req. base*, *Requisitos no funcionales* e *Historias de usuario*). Los requisitos valen el 30 % de la calificación de la aplicación.

**Estado a 2026-10-16**, revisado leyendo el código; no se corrió la app. ✅ = existe en el código · 🟡 = parcial · ❌ = no existe.
**Avance estimado: 18 ✅ + 6 🟡 + 8 ❌ de 32, es decir ~69 %** (los parciales cuentan medio). Actualizado el 2026-10-17 con el login JWT (rama `feature/login-jwt`, sin subir).

> Los planes y la referencia de endpoints que ya existían son la fuente para el *cómo*: [`plan/`](plan/README.md) (pantallas, conexión de la app, estado de la API, login con JWT), [`estilo.md`](estilo.md) (convenciones) y [`api/endpoints.md`](api/endpoints.md) (los 65 endpoints). Este archivo solo dice qué requisito cubre cada cosa.

## Requisitos del cliente (CRF)

| ID | Requisito | Estado | Evidencia / qué falta |
|---|---|---|---|
| CRF-01 | Bitácora de pagos por infante | ❌ | **No cumplido: el equipo decidió no hacerlo** (18/oct); queda como desviación del alcance. No hay tabla, endpoint ni pantalla, y no hay que integrar `feature/pagos`. |
| CRF-02 | Administración de perfiles y salas | ✅ | Backend: módulos `classrooms`, `teachers`, `children`, `parents`. App: `ui/screens/admin/*`. |
| CRF-03 | Calendario de eventos y citas | 🟡 | El calendario del padre muestra asistencia del mes (`GET /children/{id}/attendance`). No existe el modelo de eventos ni de citas. |
| CRF-04 | Dictado de registro y consulta con IA | 🟡 | **Dictado a texto sí**: `rememberDictation` (en `ui/components/DeviceLaunchers.kt`) usa `RecognizerIntent` del sistema para llenar el reporte. **Consulta con IA no existe.** Ver [decisión #10](DECISIONES-PENDIENTES.md). |
| CRF-05 | Checador para docentes | ✅ | `staff_attendance`: **entrada y salida** con GPS real del teléfono (`DeviceLocation`), radio, retardo y aviso a coordinación; la API valida la distancia. Probado en el teléfono: ubicación y hora reales, entrada, salida y rechazo fuera del perímetro. La pantalla `CheckerScreen` ya no es un prototipo (los casos simulados solo salen en compilaciones debug). **Límites:** el mapa de Google sale en blanco sin `MAPS_API_KEY` ([decisión #3](DECISIONES-PENDIENTES.md)); no hay salida automática al cerrar el día; la ubicación del centro del seed no es la del lugar de la demo (ver Centro → «Usar mi ubicación actual»). |

## Requisitos base (RF)

| ID | Requisito | Estado | Evidencia / qué falta |
|---|---|---|---|
| RF-01 | Acceso con dos tipos de perfil | ✅ | Login con correo y contraseña (JWT) en `guarderia-api` y en la app (`ui/screens/auth/`); el rol sale de `GET /me`. Probado en el emulador con los tres roles. Rama `feature/login-jwt`. |
| RF-02 | Lista de niños de la sala | ✅ | `GET /classrooms/{id}/children` y `MyRoomScreen`. |
| RF-03 | Registro de entrada y salida | 🟡 | Solo hay asistencia `PRESENT`/`ABSENT` por día (`PUT /attendance/{child_id}`). No hay hora de entrada ni de salida. |
| RF-04 | Bitácora del día | ✅ | Rutina por momentos, reportes, `daily-entries`, `GET /children/{id}/day`. |
| RF-05 | Registro para varios niños a la vez | ✅ | `report_child` (muchos a muchos) y `SelectStudentsScreen`. |
| RF-06 | Vista del familiar | ✅ | `ui/screens/parent/*` (Hoy, Calendario, Fotos, Documentos). |
| RF-07 | Cada familia ve solo a sus hijos | ✅ | `can_see_child` / `get_child_in_scope` en `core/security.py`; fuera de alcance responde 404. |
| RF-08 | Avisos del centro | ✅ | `POST` y `GET /announcements` (con alcance por rol). El admin publica y ve el historial en Centro; la docente los lee en Menú → Avisos (`AvisosFundacionScreen`); el padre, en la campana de Hoy. Limitación: el estado de "leído" vive solo en el teléfono (`LeidosStore`). |
| RF-09 | Fotografía de la actividad | ✅ | `media` (URL firmada), `MediaUploader`, reportes con `media_id`. La cámara usa `TakePicturePreview` (miniatura; ver nota en `ui/components/README.md`). |
| RF-10 | La familia puede pedir que se borre una foto | ✅ | `POST /media/{id}/deletion-request` y botón en la pantalla de fotos del padre. El admin revisa desde Centro → «Fotos que piden borrar» (`SolicitudesBorradoScreen`): ve la foto, el motivo y los niños; **borra** (sale del bucket y de todas las galerías) o **conserva**. API auditada y con 9 pruebas (`test_media_deletion.py`). **Probado de punta a punta en el teléfono** (18/oct) con una foto de prueba: la familia pide, el admin ve la foto real, confirma «Borrar», la lista se vacía, la galería de la familia queda sin la foto y la bitácora registra quién la borró. |
| RF-11 | Funciona sin internet | ❌ | No hay Room, DataStore ni WorkManager. |
| RF-12 | Resumen semanal | ✅ | `GET /children/{id}/weekly-summary` agrega la semana (asistencia, ánimo, comida, siesta, reportes, logros, notas) con 6 pruebas en `test_weekly_summary.py` y alcance por registro. La app lo muestra en Calendario → «Resumen de la semana» (5 pruebas de los textos en `ResumenSemanaTextosTest`). Probado en el teléfono con la cuenta de una mamá (semana del 12 al 16 de octubre, 2 días asistidos). **Límite:** no se envía ni se imprime; se ve dentro de la app. |
| RF-13 | Desbloqueo con huella | ❌ | No hay `BiometricPrompt` ([decisión #4](DECISIONES-PENDIENTES.md)). |
| RF-14 | Registro de quién hizo qué | ✅ | Tabla `audit_log` y `GET /audit-log` (solo admin, solo se agrega): logins, activaciones, cambios de contraseña, códigos de acceso, altas, bajas y cambios de docentes, padres e infantes. Nunca guarda contraseñas ni tokens. Además hay `created_by`/`marked_by`/`recorded_by` en reportes y asistencia. **Límite:** no registra la lectura de fotos y documentos. 23 pruebas en `tests/test_security.py`. |

## Requisitos no funcionales (RNF)

| ID | Requisito | Estado | Evidencia / qué falta |
|---|---|---|---|
| RNF01 | Contraseñas de más de 8 caracteres | ✅ | Mínimo 9 y máximo 128, en la app (`AuthValidator`) y en la API (`rules.check_password`); 8 no pasa. Probado en ambos lados. |
| RNF02 | Control de acceso por roles (RBAC) | ✅ | `require_role` + permisos por registro. Matriz de las 72 rutas en [`api/matriz-de-roles.md`](api/matriz-de-roles.md), generada del código y comprobada en `tests/test_access_matrix.py` (sin token 401, rol no permitido 403, fuera de alcance 404). Probado con una mutación: al romper `can_see_child` fallan 5 pruebas. |
| RNF03 | Contraseñas almacenadas como hash | ✅ | Argon2id (`pwdlib`) en `app_user.password_hash`; los refresh tokens y los códigos de acceso solo se guardan como SHA-256. |
| RNF04 | Disponibilidad del 99 % | ❌ | No hay despliegue ni monitoreo. Existen `/health/live` y `/health/ready`. |
| RNF05 | Consultas de fotos en menos de 4 s | 🟡 | URLs firmadas y paginación por cursor. Nadie lo ha medido. |
| RNF06 | Segundo plano mínimo y respetando ahorro de energía | ✅ | La app no usa trabajo en segundo plano todavía. Al agregar WorkManager, respetar restricciones del sistema. |
| RNF07 | Funciones principales sin conexión | ❌ | Igual que RF-11. |
| RNF08 | Código con Jetpack Compose (Kotlin) | ✅ | |

## Historias de usuario (HN)

| ID | Historia | Estado | Evidencia / qué falta |
|---|---|---|---|
| HN1 | Imprimir reportes para coordinación | ❌ | Exportar a PDF o usar `PrintManager`. |
| HN2 | Asistente ve la llegada de los padres | ❌ | Sin modelo ni pantalla. |
| HN3 | Padre ve el menú del día | ❌ | `MenuScreen` es el menú de navegación de la docente, no el de comida. |
| HN4 | Padre ve a varios hijos en la misma app | 🟡 | El modelo (`parent_child_rel`) lo permite; `ParentViewModel` usa solo el primer hijo. |
| HN5 | Avisos por grupo y a todos con imágenes | 🟡 | Por sala o a todo el centro, ya con menú desplegable en la app. **No encontré soporte de imágenes** en los avisos. |

## Fuera de las listas anteriores

Buckets *Mejoras SF* y *\*extras* del tablero: notificaciones, imprimir/descargar bitácoras, llegada de padres, menú del día para admin, diseño para varios hijos, avisos por grupo con fotos, bloquear capturas, objetos perdidos. Ninguno está hecho, salvo lo ya contado arriba.

## Cómo cerrar cada hueco dentro del marco

| Hueco | Camino |
|---|---|
| ~~Login y RNF01/03~~ | **Hecho** (rama `feature/login-jwt`). |
| Sin conexión | Room como caché local + WorkManager, como en [`estilo.md`](estilo.md); empezar por asistencia y reportes. |
| Avisos con imágenes (HN5) | Agregar `media_id` al aviso; el resto de avisos ya está. |
| Auditoría | Tabla `audit_log` escrita desde los `service.py`. |
| Resumen semanal | Endpoint que agrega `daily_summary` por semana. |
| Tiempo real del chat | Hoy se consulta con `?after=`; el marco permite SSE o WebSocket. |
