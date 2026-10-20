# Requisitos y su estado

Catálogo tomado del tablero de Teams (buckets *Req. de CODEA*, *Req. base*, *Requisitos no funcionales* e *Historias de usuario*). Los requisitos valen el 30 % de la calificación de la aplicación.

**Estado a 2026-10-18.** ✅ = funciona y tiene evidencia (prueba automática o prueba en el teléfono, que se cita en cada fila) · 🟡 = parcial o con una parte sin probar · ❌ = no existe. Las filas ya no se marcan ✅ solo porque el código exista: lo que no se ha probado se dice en la fila.
**Avance estimado: 19 ✅ + 8 🟡 + 5 ❌ de 32, es decir ~72 %** (los parciales cuentan medio). *Actualización del 19/oct por la noche: se recorrió la app en un teléfono y RF-03, RF-10, HN3 y HN5 pasan a ✅ (HN3 venía de ❌); antes eran 15 ✅ + 11 🟡 + 6 ❌, ~64 %.* Revisión del 18/oct por la noche contra el código de `main` (324 pruebas de la API en verde): RF-10 baja a 🟡 (el administrador ya no tiene pantalla para resolver solicitudes; el padre ya borra directo, con un caso sin resolver), RF-03 avanza (hora de salida del niño, que la familia ve) y HN4 avanza (selector de hijo). El 18/oct se revisó cada fila contra su evidencia real (con ayuda de una revisión independiente de otra sesión): RF-04, RF-05 y RF-09 bajaron a 🟡 por partes sin probar, y RF-11 y RNF07 subieron a 🟡 con la asistencia sin conexión.

> Los planes y la referencia de endpoints que ya existían son la fuente para el *cómo*: [`plan/`](plan/README.md) (pantallas, conexión de la app, estado de la API, login con JWT), [`estilo.md`](estilo.md) (convenciones) y [`api/endpoints.md`](api/endpoints.md) (los 84 endpoints). Este archivo solo dice qué requisito cubre cada cosa.

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
| RF-03 | Registro de entrada y salida | ✅ | La asistencia es `PRESENT`/`ABSENT` por día (`PUT /attendance/{child_id}`) y, **desde el 19/oct, guarda la hora de entrada** (`attendance.arrived_at`: la pone el servidor al pasar lista, o la de la app si la marca se hizo sin conexión; volver a marcar presente no la cambia y marcar ausente la borra). La docente la ve bajo el nombre en el pase de lista («Entró a las 8:05») y **la familia la ve como primer evento de Hoy** («Entrada»); 8 pruebas en `test_arrival_time.py`. La **salida** también: el pase de salida (`departures`, `POST /children/{id}/departure`) guarda la hora que pone el servidor y quién lo recogió, y **la familia la ve como último evento de Hoy** («Salida · Lo recogió …»); 9 pruebas en `test_departures_and_health.py`. Visto en el emulador la tarjeta y la lista del pase; no se ha registrado una salida de punta a punta en un teléfono. **Probado de punta a punta en el teléfono el 19/oct:** el pase de lista muestra «Entró a las 18:59» bajo el nombre, la familia ve «Entrada · 18:59» en Hoy, y el pase de salida registra la salida con el tutor («Salió a las 19:10 con Marisol Hernández») con «Corregir» y «Finalizar día». |
| RF-04 | Bitácora del día | ✅ | La rutina por momentos, los reportes y la pantalla Hoy del padre existen. La docente captura el resumen de cada niño en **Mi sala → «Resumen del día»** (ánimo, comida, pañales, siesta y nota; ve además la nota que dejó la familia). **Probado de punta a punta en el teléfono:** la docente guardó el resumen de un niño y la mamá lo vio en Hoy («Contento», «Casi todo», «2 cambios», «45 minutos», la nota y el reporte del momento). Pruebas: `test_parent_views.py` (API) y `ResumenDelDiaTest` (JVM). |
| RF-05 | Registro para varios niños a la vez | 🟡 | `report_child` (muchos a muchos) y `SelectStudentsScreen` existen. **API probada** (`test_teacher_day.py`: un reporte para dos niños lo ven sus papás y no otros; no se puede incluir a un niño de otra sala). **Sin probar:** el flujo de la pantalla de selección de niños no se ha recorrido en el teléfono. |
| RF-06 | Vista del familiar | ✅ | `ui/screens/parent/*`: Hoy, Calendario, Fotos, Chat y **Mi hijo** (datos del niño y salud editable; `PATCH /children/{id}/health`). La pestaña «Documentos» ya no existe (se quitó a petición del equipo el 18/oct). «Mi hijo» sin probar en el teléfono. |
| RF-07 | Cada familia ve solo a sus hijos | ✅ | `can_see_child` / `get_child_in_scope` en `core/security.py`; fuera de alcance responde 404. |
| RF-08 | Avisos del centro | ✅ | `POST` y `GET /announcements` (con alcance por rol). El admin publica y ve el historial en Centro; la docente los lee en Menú → Avisos (`AvisosFundacionScreen`); el padre, en la campana de Hoy. Limitación: el estado de "leído" vive solo en el teléfono (`LeidosStore`). |
| RF-09 | Fotografía de la actividad | 🟡 | `media` (URL firmada), `MediaUploader` y reportes con `media_id`. Probado con una foto subida por la API: llega al reporte, a la galería de la familia y se borra desde el admin (RF-10). **La captura con la cámara no se ha probado** (necesita una persona; la cámara usa `TakePicturePreview`, una miniatura: ver `ui/components/README.md`). |
| RF-10 | La familia puede borrar una foto de su hijo | ✅ | **Desde el 19/oct el padre borra la foto directamente, sin aprobación de nadie** (`DELETE /media/{id}`, botón «Borrar» con confirmación en Fotos). Basta con que salga un hijo suyo: se borra del almacén y de todas las galerías, **también si salen niños de otras familias** (decisión aprobada por el usuario). Queda en la bitácora (`MEDIA_DELETED_BY_FAMILY`). 5 pruebas en `test_media_deletion.py` (borra, foto compartida, solo el padre del niño puede, bucket caído no marca nada, el flujo de solicitudes ya no existe). Se **quitaron** «Pedir que se borre», la revisión del admin (`SolicitudesBorradoScreen`) y los endpoints `media/deletion-requests`. **Probado en el teléfono el 19/oct:** foto de prueba en un reporte → Fotos → «Borrar» → confirmar → «Foto borrada»; la API ya no la devuelve y su URL del almacén responde 403. |
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
| HN3 | Padre ve el menú del día | ✅ | **Hecho y probado en el teléfono el 19/oct.** Módulo `menus` (`daily_menu`, migración `d74a0f6c4e15`, aplicada en Aiven): el admin captura desayuno, comida y colación de cada día en Ajustes → Menú del día (semana de lunes a viernes; `PUT /menus/{fecha}`). La familia lo ve en Hoy (`GET /children/{id}/day` trae `menu`) y la asistente en Mi día (`GET /menus`). 8 pruebas en `test_daily_menu.py` y 6 JVM en `MenuRulesTest`. Visto como admin (Ajustes → Menú del día: agregar, guardar y recargar la semana), como docente («Menú de hoy» en Mi día) y como familia («Menú del día» en Hoy), en modo oscuro. |
| HN4 | Padre ve a varios hijos en la misma app | 🟡 | **Hecho el 18/oct, sin verlo en un teléfono:** con más de un hijo aparece una tira con sus nombres (`ChildSwitcher`) arriba de Hoy, Calendario, Fotos y Mi hijo; al elegir uno se recarga todo y la selección se conserva en las recargas (`ChildChoice`, 4 pruebas JVM). El chat ya mostraba la bandeja por niño. Falta probarlo con una familia de dos hijos (el seed solo tiene uno por padre). |
| HN5 | Avisos por grupo y a todos con imágenes | ✅ | Por sala o a todo el centro, ya con menú desplegable en la app. **Desde el 19/oct los avisos llevan imagen** (`announcement.media_id`; el admin la elige en Centro y la ven la docente y la familia en sus avisos); 6 pruebas en `test_announcement_images.py`. **Probado en el teléfono el 19/oct:** el admin elige una imagen en Centro y publica un aviso solo con imagen; la ven la docente (Avisos de la fundación) y la familia (campana). |

## Fuera de las listas anteriores

Buckets *Mejoras SF* y *\*extras* del tablero: notificaciones, imprimir/descargar bitácoras, llegada de padres, menú del día para admin, diseño para varios hijos, avisos por grupo con fotos, bloquear capturas, objetos perdidos. Ninguno está hecho, salvo lo ya contado arriba.

## Cómo cerrar cada hueco dentro del marco

| Hueco | Camino |
|---|---|
| ~~Login y RNF01/03~~ | **Hecho** (rama `feature/login-jwt`). |
| Sin conexión | Room como caché local + WorkManager, como en [`estilo.md`](estilo.md); empezar por asistencia y reportes. |
| Auditoría | Tabla `audit_log` escrita desde los `service.py`. |
| Resumen semanal | Endpoint que agrega `daily_summary` por semana. |
| Tiempo real del chat | Hoy se consulta con `?after=`; el marco permite SSE o WebSocket. |
