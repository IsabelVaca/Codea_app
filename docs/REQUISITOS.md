# Requisitos y su estado

Catálogo tomado del tablero de Teams (buckets *Req. de CODEA*, *Req. base*, *Requisitos no funcionales* e *Historias de usuario*). Los requisitos valen el 30 % de la calificación de la aplicación.

**Estado a 2026-10-18.** ✅ = funciona y tiene evidencia (prueba automática o prueba en el teléfono, que se cita en cada fila) · 🟡 = parcial o con una parte sin probar · ❌ = no existe. Las filas ya no se marcan ✅ solo porque el código exista: lo que no se ha probado se dice en la fila.
**Avance estimado: 15 ✅ + 11 🟡 + 6 ❌ de 32, es decir ~64 %** (los parciales cuentan medio). Revisión del 18/oct por la noche contra el código de `main` (318 pruebas de la API en verde): RF-10 baja a 🟡 porque el administrador ya no tiene pantalla para resolver las solicitudes (el equipo pidió quitarla), y RF-03 avanza (ya hay hora de salida del niño). El 18/oct se revisó cada fila contra su evidencia real (con ayuda de una revisión independiente de otra sesión): RF-04, RF-05 y RF-09 bajaron a 🟡 por partes sin probar, y RF-11 y RNF07 subieron a 🟡 con la asistencia sin conexión.

> Los planes y la referencia de endpoints que ya existían son la fuente para el *cómo*: [`plan/`](plan/README.md) (pantallas, conexión de la app, estado de la API, login con JWT), [`estilo.md`](estilo.md) (convenciones) y [`api/endpoints.md`](api/endpoints.md) (los 83 endpoints). Este archivo solo dice qué requisito cubre cada cosa.

## Requisitos del cliente (CRF)

| ID | Requisito | Estado | Evidencia / qué falta |
|---|---|---|---|
| CRF-01 | Bitácora de pagos por infante | ❌ | **No cumplido: el equipo decidió no hacerlo** (18/oct); queda como desviación del alcance. No hay tabla, endpoint ni pantalla, y no hay que integrar `feature/pagos`. |
| CRF-02 | Administración de perfiles y salas | ✅ | Backend: módulos `classrooms`, `teachers`, `children`, `parents`. App: `ui/screens/admin/*`. |
| CRF-03 | Calendario de eventos y citas | 🟡 | El calendario del padre muestra asistencia del mes (`GET /children/{id}/attendance`). No existe el modelo de eventos ni de citas. |
| CRF-04 | Dictado de registro y consulta con IA | 🟡 | **Dictado a texto sí**: `rememberDictation` (en `ui/components/DeviceLaunchers.kt`) usa `RecognizerIntent` del sistema para llenar el reporte. **Consulta con IA no existe.** Ver [decisión #10](DECISIONES-PENDIENTES.md). |
| CRF-05 | Checador para docentes | ✅ | `staff_attendance`: la docente registra su **entrada y su salida con un toque**, sin ubicación ni mapa. El servidor pone la hora, calcula el retardo contra la **hora de entrada del centro** y guarda el motivo si lo hubo. El administrador configura en **Ajustes → Horario** la entrada, la salida, la hora de **cierre automático** y los **días de trabajo**; si no se registra la salida, el servidor la cierra solo a esa hora (21:00 por omisión) y la marca como automática; en un día no laboral no se puede registrar entrada. Pruebas: `tests/test_teacher_day.py` (entrada, retardo, salida, cierre automático y horario del centro) y `CheckInRulesTest`. **Límites:** el cierre se hace al consultar, no con un proceso programado; no se ha probado en el teléfono; la app ya no tiene botón de aviso a coordinación (el endpoint y la tabla siguen en el backend, sin pantalla que los lea). |

## Requisitos base (RF)

| ID | Requisito | Estado | Evidencia / qué falta |
|---|---|---|---|
| RF-01 | Acceso con dos tipos de perfil | ✅ | Login con correo y contraseña (JWT) en `guarderia-api` y en la app (`ui/screens/auth/`); el rol sale de `GET /me`. Probado en el emulador con los tres roles. Rama `feature/login-jwt`. |
| RF-02 | Lista de niños de la sala | ✅ | `GET /classrooms/{id}/children` y `MyRoomScreen` (rediseñada el 18/oct: «De casa hoy», filtros, ficha del niño con alergias, familia y quién lo recoge). El **pase de lista** está en Mi día (`RollCallScreen`) y la regla es «sin registro = ausente» en toda la API (`attendance/queries.py::present_child_ids`). Visto en el Pixel 8 Pro; sin probar «Ya lo vi», «Marcar a todos presentes» ni el modo oscuro. |
| RF-03 | Registro de entrada y salida | 🟡 | La asistencia es `PRESENT`/`ABSENT` por día (`PUT /attendance/{child_id}`); **no hay hora de entrada** del niño. La **salida** sí: el pase de salida (`departures`, `POST /children/{id}/departure`) guarda la hora que pone el servidor y quién lo recogió; 8 pruebas en `test_departures_and_health.py`. Visto en el emulador la tarjeta y la lista del pase; no se ha registrado una salida de punta a punta en un teléfono. La familia todavía no ve la hora de salida. |
| RF-04 | Bitácora del día | ✅ | La rutina por momentos, los reportes y la pantalla Hoy del padre existen. La docente captura el resumen de cada niño en **Mi sala → «Resumen del día»** (ánimo, comida, pañales, siesta y nota; ve además la nota que dejó la familia). **Probado de punta a punta en el teléfono:** la docente guardó el resumen de un niño y la mamá lo vio en Hoy («Contento», «Casi todo», «2 cambios», «45 minutos», la nota y el reporte del momento). Pruebas: `test_parent_views.py` (API) y `ResumenDelDiaTest` (JVM). |
| RF-05 | Registro para varios niños a la vez | 🟡 | `report_child` (muchos a muchos) y `SelectStudentsScreen` existen. **API probada** (`test_teacher_day.py`: un reporte para dos niños lo ven sus papás y no otros; no se puede incluir a un niño de otra sala). **Sin probar:** el flujo de la pantalla de selección de niños no se ha recorrido en el teléfono. |
| RF-06 | Vista del familiar | ✅ | `ui/screens/parent/*`: Hoy, Calendario, Fotos, Chat y **Mi hijo** (datos del niño y salud editable; `PATCH /children/{id}/health`). La pestaña «Documentos» ya no existe (se quitó a petición del equipo el 18/oct). «Mi hijo» sin probar en el teléfono. |
| RF-07 | Cada familia ve solo a sus hijos | ✅ | `can_see_child` / `get_child_in_scope` en `core/security.py`; fuera de alcance responde 404. |
| RF-08 | Avisos del centro | ✅ | `POST` y `GET /announcements` (con alcance por rol). El admin publica y ve el historial en Centro; la docente los lee en Menú → Avisos (`AvisosFundacionScreen`); el padre, en la campana de Hoy. Limitación: el estado de "leído" vive solo en el teléfono (`LeidosStore`). |
| RF-09 | Fotografía de la actividad | 🟡 | `media` (URL firmada), `MediaUploader` y reportes con `media_id`. Probado con una foto subida por la API: llega al reporte, a la galería de la familia y se borra desde el admin (RF-10). **La captura con la cámara no se ha probado** (necesita una persona; la cámara usa `TakePicturePreview`, una miniatura: ver `ui/components/README.md`). |
| RF-10 | La familia puede pedir que se borre una foto | 🟡 | **Cambio pedido el 18/oct, a medias:** el equipo quiere que el padre borre la foto directamente. Hoy el padre sigue con «Pedir que se borre» y el administrador **ya no tiene pantalla** para resolver (Centro quitó «Fotos que piden borrar»); `SolicitudesBorradoScreen` y los endpoints siguen en el código sin forma de llegar a ellos. Lo que sigue es lo probado antes del cambio: | `POST /media/{id}/deletion-request` y botón en la pantalla de fotos del padre. El admin revisa desde Centro → «Fotos que piden borrar» (`SolicitudesBorradoScreen`): ve la foto, el motivo y los niños; **borra** (sale del bucket y de todas las galerías) o **conserva**. API auditada y con 9 pruebas (`test_media_deletion.py`). **Probado de punta a punta en el teléfono** (18/oct) con una foto de prueba: la familia pide, el admin ve la foto real, confirma «Borrar», la lista se vacía, la galería de la familia queda sin la foto y la bitácora registra quién la borró. |
| RF-11 | Funciona sin internet | 🟡 | **Asistencia (pase de lista y corregir en la ficha) sin conexión:** Room guarda la última Mi sala y una cola de marcas pendientes; WorkManager las manda solas al volver la red (se colapsan las marcas repetidas y se reintenta con espera creciente). Probado en el teléfono: con la API apagada se marcó una falta, sobrevivió a cerrar y abrir la app, y al encender la API llegó sola. **Lo demás sigue necesitando conexión:** reportes, fotos, chat, check-in, documentos y avisos. |
| RF-12 | Resumen semanal | ✅ | `GET /children/{id}/weekly-summary` agrega la semana (asistencia, ánimo, comida, siesta, reportes, logros, notas) con 6 pruebas en `test_weekly_summary.py` y alcance por registro. La app lo muestra en Calendario → «Resumen de la semana» (5 pruebas de los textos en `ResumenSemanaTextosTest`). Probado en el teléfono con la cuenta de una mamá (semana del 12 al 16 de octubre, 2 días asistidos). **Límite:** no se envía ni se imprime; se ve dentro de la app. |
| RF-13 | Desbloqueo con huella | ❌ | No hay `BiometricPrompt` ([decisión #4](DECISIONES-PENDIENTES.md)). |
| RF-14 | Registro de quién hizo qué | ✅ | Tabla `audit_log` y `GET /audit-log` (solo admin, solo se agrega): logins, activaciones, cambios de contraseña, códigos de acceso, altas, bajas y cambios de docentes, padres e infantes. Nunca guarda contraseñas ni tokens. Además hay `created_by`/`marked_by`/`recorded_by` en reportes y asistencia. **Límite:** no registra la lectura de fotos y documentos. 23 pruebas en `tests/test_security.py`. |

## Requisitos no funcionales (RNF)

| ID | Requisito | Estado | Evidencia / qué falta |
|---|---|---|---|
| RNF01 | Contraseñas de más de 8 caracteres | ✅ | Mínimo 9 y máximo 128, en la app (`AuthValidator`) y en la API (`rules.check_password`); 8 no pasa. Probado en ambos lados. |
| RNF02 | Control de acceso por roles (RBAC) | ✅ | `require_role` + permisos por registro. Matriz de los 77 endpoints en [`api/matriz-de-roles.md`](api/matriz-de-roles.md), generada del código y comprobada en `tests/test_access_matrix.py` (sin token 401, rol no permitido 403, fuera de alcance 404). Probado con una mutación: al romper `can_see_child` fallan 5 pruebas. |
| RNF03 | Contraseñas almacenadas como hash | ✅ | Argon2id (`pwdlib`) en `app_user.password_hash`; los refresh tokens y los códigos de acceso solo se guardan como SHA-256. |
| RNF04 | Disponibilidad del 99 % | ❌ | No hay despliegue ni monitoreo. Existen `/health/live` y `/health/ready`. |
| RNF05 | Consultas de fotos en menos de 4 s | 🟡 | URLs firmadas y paginación por cursor. Nadie lo ha medido. |
| RNF06 | Segundo plano mínimo y respetando ahorro de energía | ✅ | La app solo usa trabajo en segundo plano para mandar la cola de asistencia sin conexión (WorkManager, solo con red y con espera creciente); no hay sondeos ni servicios permanentes. Se respeta el ahorro de energía porque el sistema decide cuándo corre. |
| RNF07 | Funciones principales sin conexión | 🟡 | Igual que RF-11: solo la asistencia (y ver lo último que se cargó de Mi sala) funciona sin conexión. |
| RNF08 | Código con Jetpack Compose (Kotlin) | ✅ | |

## Historias de usuario (HN)

| ID | Historia | Estado | Evidencia / qué falta |
|---|---|---|---|
| HN1 | Imprimir reportes para coordinación | ❌ | Exportar a PDF o usar `PrintManager`. |
| HN2 | Asistente ve la llegada de los padres | ❌ | Sin modelo ni pantalla. |
| HN3 | Padre ve el menú del día | ❌ | `MenuScreen` es el menú de navegación de la docente, no el de comida. |
| HN4 | Padre ve a varios hijos en la misma app | 🟡 | El modelo (`parent_child_rel`) lo permite; `ParentViewModel` usa solo el primer hijo (`me.parent?.children?.firstOrNull()`); «Mi hijo» tampoco deja elegir. |
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
