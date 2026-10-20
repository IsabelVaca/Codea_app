# Administración del proyecto

Parte 5 de la rúbrica: bitácora de hitos, desviaciones respecto al plan, riesgos, distribución del trabajo y retrospectiva. Corte: **2026-10-20**, commit `043c2d6` (`main` con los PR hasta el #17 más el commit de respaldos del servidor; 139 commits en su historia: 111 de trabajo y 28 de fusión).

Qué sale de qué:

- **Hitos y distribución:** `git log` y `git shortlog` (comandos al final de cada sección, para volver a calcularlos).
- **Desviaciones:** [`docs/PLAN.md`](../PLAN.md), [`docs/PLAN-ACCION.md`](../PLAN-ACCION.md), [`docs/REQUISITOS.md`](../REQUISITOS.md), [`docs/DECISIONES-PENDIENTES.md`](../DECISIONES-PENDIENTES.md) y [`docs/MARCO-TECNICO.md`](../MARCO-TECNICO.md).
- **Riesgos:** lectura del código y de esos documentos. Son juicios míos; el equipo debe corregirlos.
- **Lo que solo el equipo sabe** (horas, acuerdos, causas, el tablero) está marcado **«[completar por el equipo]»**. No se inventó.

El tablero (Planner o Teams) no se puede consultar desde el repositorio: el enlace y la captura que pide la rúbrica son **[completar por el equipo]**.

---

## 1. Bitácora de hitos

Cada fila agrupa los commits de `git log` por lo que lograron. «Autoría» es el nombre que aparece en git, que no siempre coincide con la persona (ver sección 4). El detalle día por día del 18 al 20 de octubre está en [`docs/BITACORA.md`](../BITACORA.md) y el del incidente del 19/oct en [`docs/INCIDENTE-2026-10-19.md`](../INCIDENTE-2026-10-19.md).

| Fecha | Hito | Commits (autoría en git) |
|---|---|---|
| 2026-09-16 | **Arranque.** Proyecto base de Jetpack Compose | `0d37fa1`, `4726807` (Isabel Vaca, IsabelV) |
| 2026-09-24 | **Navegación con 4 pestañas**, pantalla Mi día con tema del prototipo, flujo «Aún el día no comienza» → checador | `0fc6c25`, `428b707` (Tristan Olivares) |
| 2026-09-24 a 09-26 | **Arquitectura MVVM** y pantallas del administrador y del checador; barra de navegación del administrador | `7b247e3`, `24d1a22`, `e360ac9` (IsabelV) |
| 2026-09-29 | **Día de integración de las pantallas de la primera entrega** (20 commits): pantallas de administrador completas, reporte, Mi sala y Chats, selección de rol al abrir la app, pestañas vacías del padre, pantalla Fotos, pestaña Documentos, estructura del curso, checador y menú, Mi día interactivo. Fusión de los PR #1 a #5 | `639c760`, `bacbc38`, `e9db1fb`, `31f3bdb`, `f8f8d5e`, `75b597a`, `bfcd9a8`, entre otros (IsabelV, StarLord133, Carlos Arias, osc4r-t, Carlos-EAC) |
| 2026-09-30 | **Vista del padre:** página Hoy y Calendario | `6b243d7` (StarLord133) |
| 2026-10-10 | **CRUD de padres** con validación | `a232ac6` (IsabelV) |
| 2026-10-12 | **Backend: esqueleto** (Docker, Alembic, FastAPI, Postgres) y modelos iniciales | `9c31792`, `c77b11f` (IsabelV) |
| 2026-10-13 | **API construida «sin testear»** (así lo dice el mensaje del commit): los módulos, el seed y las migraciones | `c51ac75` (StarLord133) |
| 2026-10-14 | **Conexión de la app a los endpoints** de los tres roles; logo como icono de la app | `359e4e1`, `12da735` (IsabelV, StarLord133) |
| 2026-10-15 | **Base de datos de Aiven configurada**; pantallas de Rutinas del administrador | `457859b`, `e372fa8` (IsabelV) |
| 2026-10-16 | **CRUD de rutinas y de avisos** en los tres roles | `d39f0a7` (IsabelV) |
| 2026-10-17 | **Planes de acción** y **documentación del proyecto** (estado, requisitos, marco técnico, README por carpeta) | `481a7e8`, `03dddec`, `91ec7a3` (StarLord133, IsabelV, Oscar Olivares) |
| 2026-10-17 | **Login propio con JWT** en la API y en la app (sesión cifrada, Mi cuenta), sustituyendo a Firebase | `a802c08`, `0c05ef0`, `e9ae873`, `328b250` (Oscar Olivares) |
| 2026-10-17 | **Chat entre docentes y padres** con bandeja, pestaña del padre y consulta cada 10 s | `85fa66d` (Oscar Olivares) |
| 2026-10-18 | **Integración:** rama `integracion` desde `main` con el login JWT y el chat fusionados; entorno de pruebas (Aiven migrada, teléfono físico por USB) | `4a71c80`, `22dad6c` (StarLord133) |
| 2026-10-18 | **Recorrido en teléfono físico** y correcciones: cierre de la pestaña Documentos del padre, colores en modo oscuro, mes del calendario, sala de la docente | `07f6d7e` (StarLord133) |
| 2026-10-18 | **Checador con GPS y hora reales**, en lugar del prototipo con datos fijos | `0494690` (StarLord133) |
| 2026-10-18 | **Seguridad de la API:** límite de intentos, cabeceras, CORS cerrado y bitácora de auditoría (RF-14) | `a50164d` (StarLord133) |
| 2026-10-18 | **Pruebas de control de acceso:** matriz de roles, IDOR e inyección; conteo de pruebas de la API | `637efb7`, `e2616e4` (StarLord133) |
| 2026-10-18 | **Mi sala** marca a los niños con nota de la familia | `71d0f2e` (StarLord133) |
| 2026-10-18 | **Revisión de solicitudes de borrado de fotos (RF-10)** en la API y en la app; los documentos del proyecto dejan de mencionar pagos | `79deb06`, `8af6958` (StarLord133) |
| 2026-10-18 | **El administrador fija la ubicación del checador desde el teléfono**; los cambios al centro quedan en la bitácora de auditoría | `985f536` (StarLord133) |
| 2026-10-18 | **Resumen semanal (RF-12)**: la API, y después la tarjeta «Resumen de la semana» en el Calendario del padre | `4f65a42`, `13b3b92` (StarLord133) |
| 2026-10-18 | **Salida de la docente** en el checador y corrección de dos textos de la app que decían lo contrario de lo que hacía el sistema | `f814763` (StarLord133) |
| 2026-10-18 | **Inscripción con la fecha de nacimiento real**, en lugar de inventarla desde la edad | `38bc8fa` (StarLord133) |
| 2026-10-18 | **Asistencia de Mi sala sin conexión**: Room (cola de marcas y caché de la última sala) y WorkManager, que envía la cola al volver la red; probado en el teléfono con la API apagada | `99fac75` (StarLord133) |
| 2026-10-18 | **La caché con nombres de niños se borra** cuando la sesión termina | `1f2d2a0` (StarLord133) |
| 2026-10-18 | **Cierre del 18 de octubre:** pantalla «Resumen del día» para la docente (RF-04), cola sin conexión ligada a su dueña (Room v2 con `MIGRATION_1_2`), 2 pruebas de la API para reportes de varios niños (RF-05) y requisitos al día | `20e7de1`, `22c0008` (StarLord133) |
| 2026-10-18 | **Documentos de la entrega final** (esta carpeta `docs/entrega/`) | `df08f30` (StarLord133) |
| 2026-10-18 | **Admin con pestañas Familias y Ajustes; checador sin ubicación** (entrada y salida con un toque, cierre automático a las 9 pm), **horario del centro** configurable y limpieza de pantallas sin uso | `6a0c990`, `7704aaa`, `7a8a070`, `b98a5d4`, `31197cd`, `5b70069` (Oscar Olivares) |
| 2026-10-18 | **Mi sala rediseñada**, ficha del niño, **pase de lista en Mi día** y resumen guiado; sin pase de lista el niño cuenta como ausente | `22e2951`, `302e957` (StarLord133) |
| 2026-10-18 | **Chat con fotos**, **invitación por WhatsApp** con enlace que abre la app, **pase de salida** y «Finalizar día», personas autorizadas del admin; selector de hijo y borrado directo de fotos para el padre | `e76eb8f`, `95ba592`, `0bb160d`, `8d72727` (Oscar Olivares, StarLord133, IsabelV) |
| 2026-10-19 | **Servidor público en `codeaapp.site`** con túnel de Cloudflare (decisión #7 aprobada), base de producción en Aiven | `e74160a`, `06e16ea`, `5825f71` (Oscar Olivares) |
| 2026-10-19 | **Hora de entrada del niño, avisos con imagen (HN5), menú del día (HN3) y borrado directo de fotos** en la API y en la app; recorrido en el teléfono y requisitos al 72 % | `ecc249f`, `6b6426e`, `9c6b981` (StarLord133) |
| 2026-10-19 | **Incidente:** el seed de prueba (`seed_demo.py --borrar-todo`) borró los datos del servidor compartido, que no tenía respaldo; endurecimiento de la activación y `.env.dev` para el equipo | `d36fb17`, `945215b`, `5faf9b4` (Oscar Olivares) |
| 2026-10-19 | **Rediseño del admin:** Centro pasa a **Avisos**, las salas a **Docentes → Salas**, «+ Nuevo aviso», filtro Activos / Todos | `c523cdc`, `2be3fe3`, `fc05847`, entre otros (Oscar Olivares) |
| 2026-10-20 | **Vincular a un padre con más niños**, pantallas del padre y de la docente con el estilo del admin, **caché en Room** de los tres roles; Mi día sin menú | `1c45b52`, `496c395`, `da38d39`, `a6c5dd5`, `60e5ba5` (Oscar Olivares), `e1e4549`, `66c21cb` (StarLord133) |
| 2026-10-20 | **Respaldo diario** de la base y de las fotos del servidor compartido (`launchd`, 14 días, restauración probada) y almacén con lugar para más fotos | `043c2d6` (Oscar Olivares) |

### Hitos por cumplir antes de la entrega

| Fecha límite | Hito | Estado |
|---|---|---|
| Lunes 19 de octubre, noche | Congelamiento de funciones nuevas (`PLAN-ACCION.md`) | ⚠️ No se cumplió del todo: el 20/oct entraron la caché en Room y vincular padres a más niños |
| 20 y 21 de octubre | Solo corregir y documentar | ⏳ En curso |
| Antes de la entrega | Guion, video de respaldo (~3 min), APK de release firmado, captura del tablero (la [matriz de trazabilidad](matriz-trazabilidad.md) ya está escrita; falta volver a correr las pruebas y anotar la fecha) | ❌ Ver `docs/ENTREGA-3.md` |
| **Jueves 22 de octubre de 2026, 23:59** | **Entrega** en Canvas, un solo PDF por grupo (100 puntos) | ⏳ |

Para volver a calcular esta tabla: `git log --format='%h|%ad|%an|%s' --date=short`.

---

## 2. Desviaciones respecto al plan

Se llama desviación a lo que el plan, el marco técnico o los requisitos pedían y no está así. Se registran para la sección de desviaciones de la entrega (`ENTREGA-3.md`, parte 5), donde también van las excepciones al marco técnico.

### 2.1 Alcance

| # | Desviación | Qué dice el plan o el requisito | Qué pasó | Motivo | Efecto |
|---|---|---|---|---|---|
| D1 | **Sin pagos** | CRF-01: bitácora de pagos por infante | **Decisión del equipo** del 18 de octubre: no se hace. No hay tablas, endpoints ni pantallas, y la rama `feature/pagos` no se integró a `integracion` | **[completar por el equipo]** | CRF-01 queda **no cumplido** por decisión. Es uno de los cinco requisitos del cliente |
| D2 | **Sin conexión (parcial)** | RF-11 y RNF07: las funciones principales sin internet; sincronización posterior | Solo la **asistencia de Mi sala** se puede escribir sin conexión (Room, cola de marcas y WorkManager, commit `99fac75`; probada en el teléfono con la API apagada). Desde el 20/oct los tres roles **ven lo último que cargaron** (caché `cached_response` en Room, `da38d39` y `a6c5dd5`), pero reportes, fotos, chat, checador, resumen del día, pase de salida y avisos siguen necesitando red para escribir. No hay pruebas de Room en JVM. El Bloque 3 de `PLAN-ACCION.md` decía «solo si el bloque 2 termina el lunes» | Falta de tiempo **[confirmar por el equipo]** | RF-11 y RNF07 en 🟡 (antes ❌); las dos filas de la rúbrica (operación sin conexión y sincronización) en 🟡. Trae tres dependencias nuevas dentro del marco: Room 2.8.4, WorkManager 2.11.2 y KSP 2.3.12 |
| D3 | **Consulta con IA** | CRF-04: dictado y consulta con IA | Solo el **dictado** (con el reconocedor de voz del sistema). La consulta con IA no existe | **[completar por el equipo]** | CRF-04 en 🟡 |
| D4 | **Calendario de eventos y citas** | CRF-03 | El calendario del padre muestra solo asistencia | **[completar por el equipo]** | CRF-03 en 🟡 |
| D5 | **Entrada y salida del niño** | RF-03 | Hay presente o falta por día (el pase de lista está en Mi día; **sin registro cuenta como ausente**) y, desde el 18/oct, la **hora de salida del niño y con quién se fue** (pase de salida). Desde el 19/oct también se guarda la **hora de entrada** del niño (la de su pase de lista). (La entrada y la **salida de la docente** sí existen desde el commit `f814763`, con los textos de la app corregidos) | **[completar por el equipo]** | **Resuelta:** RF-03 pasó a ✅ al probarse de punta a punta en el teléfono el 19/oct |
| D6 | **Huella, historias de usuario** | RF-13, HN1 a HN5 | RF-13, HN1 y HN2 sin hacer. HN3 (menú del día) y HN5 (avisos con imagen) se hicieron y se probaron en el teléfono el 19/oct; HN4 (varios hijos) está hecho, pero no se ha visto con una familia de dos hijos. El resumen semanal (RF-12) ya se hizo, en la API y en la app | **[completar por el equipo]** | Ver 2.4 |
| D7 | **Notificaciones** | Tarea abierta del tablero | Ninguna notificación del teléfono (ni push ni local). El chat se actualiza consultando cada 10 s; nada de WebSocket ni SSE | Firebase está excluido por el marco; la alternativa (WorkManager) no se hizo | Las familias no se enteran de mensajes ni avisos si no abren la app |
| D8 | **Resumen del día sin captura en la app (resuelto)** | P1 #12 de `docs/plan/01-app-pantallas.md` (Día del niño) | Era una brecha: la docente no tenía dónde capturar el resumen. Se resolvió en el commit `20e7de1` con **Mi sala → «Resumen del día»**, con 5 pruebas JVM y prueba de punta a punta en el teléfono (la mamá lo vio en Hoy) | Hallazgo de la revisión de los documentos de la entrega | RF-04 vuelve a ✅ |

### 2.2 Cambios técnicos

| # | Desviación | Plan | Qué pasó | Efecto |
|---|---|---|---|---|
| D9 | **Login con JWT propio en lugar de Firebase Auth** | Tareas del tablero que mencionaban Firebase | Se reemplazó por un JWT de 5 minutos con refresh rotado (decisión #1). Firebase está excluido por el marco | Cierra RF-01, RNF01 y RNF03. **Falta la aprobación formal** de la decisión |
| D10 | **Checador: el prototipo se había contado como terminado** | CRF-05 aparecía en ✅ | El 18 de octubre el recorrido en el teléfono mostró que la pantalla era un prototipo con fecha, hora y distancia fijas, y que el servidor nunca podía rechazar por distancia. Se rehízo con GPS y hora reales | Una lección sobre cómo se marcaba el avance; ver la retrospectiva |
| D11 | **API construida antes de probarse** | Pruebas junto con el código | El commit `c51ac75` se llama «API CONSTRUIDA SIN TESTEAR». Las pruebas llegaron después (de 43 pruebas el 16 de octubre a 276 el 18 de octubre, según `guarderia-api/tests/README.md`) | El riesgo de errores ocultos duró varios días |
| D12 | **Release sin firmar y sin minificar; servidor en una Mac del equipo** | Rúbrica, parte 1: build de release firmado y backend en URL pública | No existe keystore ni `signingConfig`; la minificación está apagada. El backend sí está en una URL pública con HTTPS desde el 19/oct (`api.codeaapp.site`), pero corre en la Mac de un integrante con un túnel de Cloudflare, no en un servidor dedicado | Ver [`plan-implantacion.md`](plan-implantacion.md) |
| D13 | ~~Mapa del checador en blanco~~ | Mapa del prototipo | **Resuelto (18/oct):** el checador ya no usa mapa ni ubicación; se quitó Google Maps | — |
| D14 | **Pruebas de interfaz inexistentes** | Rúbrica, parte 3 | Solo hay `ExampleInstrumentedTest`. Pruebas JVM de dominio y de sesión sí hay | El marco pide Compose UI Test |
| D15 | **Estado de los ViewModels** | Marco: `StateFlow` | `CodeaViewModel` y `ParentViewModel` siguen usando `mutableStateOf` (`MARCO-TECNICO.md`, 🟡). La inyección de dependencias es manual y está a medias | Deuda técnica |
| D16 | **Fotos de baja calidad** | Marco: Photo Picker, FileProvider, EXIF y compresión | La cámara usa `TakePicturePreview`, que devuelve una miniatura. No hay compresión previa | Las fotos de los reportes son miniaturas (**Excepción: las fotos del chat** sí usan Photo Picker, cámara con FileProvider, EXIF y compresión; los reportes con foto siguen con la miniatura) |
| D17 | **Documentación desactualizada en puntos concretos** | Documentos al día | Al 20/oct, `docs/GUIA-CONTINUAR.md` cita 125 pruebas JVM y 324 de la API, `app/src/test/README.md` 74 y `guarderia-api/tests/README.md` 276 (hoy son 140 JVM y 343 de la API). Varios documentos se ponían al día a la par, el mismo 20/oct | Confusión de quien llegue nuevo |
| D18 | **Cambios pedidos por el equipo el 18/oct** | Alcance y pantallas anteriores | (1) La pestaña «Documentos» del padre pasó a «Mi hijo» y **se quitó la subida de documentos** (la API sigue sin pantalla). (2) Las **personas autorizadas** las administra solo el administrador. (3) El código de invitación se manda por **WhatsApp** con un enlace que abre la app, **sin correos ni SMTP**. (4) La solicitud de borrado de fotos se cambió por **borrado directo del padre** (`DELETE /media/{id}`, 19/oct; se quitaron la solicitud y la revisión del admin). (5) El checador ya no usa GPS ni mapa | Cambian filas de [`REQUISITOS.md`](../REQUISITOS.md) (RF-06, RF-10); la entrega debe decir qué se quitó y por qué |
| D19 | **Cambios de pantallas del 19 y 20/oct** | Pantallas del prototipo y de la primera entrega | Admin: **Centro** pasó a **Avisos** (con «+ Nuevo aviso»), la ocupación de salas a **Docentes → Salas**, y las listas tienen filtro Activos / Todos. Docente: Mi día ya no muestra el menú del día (la familia lo sigue viendo en Hoy). Padre, docente y admin con el mismo estilo de tarjetas | Los manuales y la matriz se pusieron al día el 20/oct; `docs/RECORRIDO.md` conserva los nombres viejos porque registra lo que se probó el 19/oct |
| D20 | **Datos del servidor compartido borrados sin respaldo** | Respaldos antes de producción ([`plan-implantacion.md`](plan-implantacion.md), 5.1) | El 19/oct se corrió `seed_demo.py --borrar-todo` sobre la base compartida: se perdieron 16 cuentas, 6 fotos y 3 avisos que no tenían respaldo ([`INCIDENTE-2026-10-19.md`](../INCIDENTE-2026-10-19.md)) | Desde el 20/oct hay respaldo diario con restauración probada. **Sigue pendiente** el candado que impida correr ese seed fuera de desarrollo |

### 2.3 Excepciones al marco técnico (decisiones pendientes)

El marco técnico del curso (<https://startdroid.pages.dev/>) manda; lo que se sale se registra en [`DECISIONES-PENDIENTES.md`](../DECISIONES-PENDIENTES.md) y no se implementa sin aprobación. Al 20/oct hay **dos aprobadas** (#7, túnel de Cloudflare, el 19/oct, y #11, respaldo diario con `launchd`, el 20/oct) y una resuelta (#3); la #1 está «implementada, falta la aprobación formal» y las demás siguen en «⏳ Pendiente».

| # | Tema | ¿Está en el código? | Estado real |
|---|---|---|---|
| 1 | Login con JWT propio | ✅ Sí | Implementado en `integracion`; falta la aprobación formal |
| 2 | Notificaciones con Firebase | ❌ No | No implementado. La alternativa dentro del marco (WorkManager y notificaciones locales) tampoco |
| 3 | Google Maps Compose | ✅ Resuelta | **Quitado del código (18/oct).** El checador ya no usa mapa ni ubicación |
| 4 | Huella (`androidx.biometric`) | ❌ No | No implementado (RF-13) |
| 5 | Redis | ❌ No | No se usa; el límite de intentos usa Postgres |
| 6 | Grafana | ❌ No | No implementado; hay `/health/live` y `/health/ready` |
| 7 | Túnel de Cloudflare | ✅ Sí | **Aprobado (19/oct).** `docker-compose.prod.yml` con `cloudflared` para `codeaapp.site`; levantado y verificado el 19/oct en la Mac servidor |
| 8 | Librería `reorderable` | ✅ Sí | **En el código sin aprobación** (arrastrar momentos de la rutina) |
| 9 | SeaweedFS como almacén | ✅ Sí | **En uso sin aprobación**, en desarrollo y en el servidor compartido, donde guarda las fotos (desde el 20/oct con hasta 100 volúmenes) |
| 10 | Dictado con `RecognizerIntent` | ✅ Sí | **En el código sin aprobación**; usa el reconocedor del sistema |
| 11 | Respaldo diario con `launchd` (macOS) | ✅ Sí | **Aprobado (20/oct).** `guarderia-api/scripts/respaldo.sh` y `site.codeaapp.respaldo.plist`, sin librerías nuevas |

En suma: **tres** piezas fuera de la lista del marco (#8, #9 y #10) siguen en el código sin aprobación (la #3, el mapa, salió del código el 18/oct); **dos** se aprobaron y están en uso (#7 y #11); **cuatro** (#2, #4, #5 y #6) no se implementaron, y por eso no cuentan como excepción pero sí como requisitos o tareas sin cubrir. La #1 se implementó y espera la aprobación formal. Además, `MARCO-TECNICO.md` pide confirmar `PyJWT` y `pwdlib[argon2]` (librerías del backend) y `kotlinx.serialization`.

### 2.4 Estado final de los requisitos

La fuente de verdad es [`docs/REQUISITOS.md`](../REQUISITOS.md); la matriz de trazabilidad con evidencia y resultado de pruebas está en [`matriz-trazabilidad.md`](matriz-trazabilidad.md) y no se repite aquí. Al corte, ese documento cuenta **19 cumplidos, 8 parciales y 5 no cumplidos de 32**. Con la regla de que los parciales cuentan medio, son 23 de 32, es decir **~72 %**. (Una versión anterior anotaba ~69 % con 18/6/8: era un error de suma. La revisión de cada fila contra su evidencia bajó RF-04, RF-05 y RF-09 a 🟡; después RF-04 volvió a ✅ al construirse su pantalla, RF-11 y RNF07 subieron a 🟡 por la asistencia sin conexión, y el recorrido en el teléfono del 19/oct pasó RF-03, RF-10, HN3 y HN5 a ✅.) Lo no cumplido o parcial:

| Estado | Requisitos |
|---|---|
| ❌ No cumplido | CRF-01 (por decisión del equipo), RF-13, RNF04 (hay servidor, pero sin monitoreo), HN1, HN2 |
| 🟡 Parcial | CRF-03, CRF-04, RF-05 (la API está probada con dos niños; la pantalla de selección no se recorrió en el teléfono), RF-09 (captura con cámara sin probar), RF-11 y RNF07 (sin red solo se escribe la asistencia de Mi sala; desde el 20/oct se ve lo último en los tres roles), RNF05 (sin medir), HN4 (sin verlo con una familia de dos hijos) |

Queda sin hacer, aparte de los requisitos: cifrar la base local del teléfono (desde el 20/oct guarda también datos de salud), APK firmado, monitoreo del servidor, política de retención, un comando para crear solo al primer administrador y el candado del seed de prueba.

Los parciales cuentan medio en ese porcentaje. Desde la revisión del 18 de octubre un ✅ solo se escribe si la fila cita evidencia (prueba automática o prueba en el teléfono); el caso del checador (D10) muestra por qué.

---

## 3. Riesgos

Probabilidad e impacto: **A**lta, **M**edia, **B**aja (juicio del documento, a corregir por el equipo). La entrega es el jueves 22 de octubre a las 23:59; al corte (20/oct) quedan dos días.

| ID | Riesgo | Prob. | Imp. | Mitigación | Responsable | Estado |
|---|---|---|---|---|---|---|
| R1 | **El backend público corre en la Mac de un integrante** (`api.codeaapp.site`, túnel de Cloudflare, desde el 19/oct): si se duerme, se apaga o pierde la red, la app deja de funcionar para todos, también durante la presentación | M | A | Que la Mac no se duerma (o `caffeinate -s`) y esté conectada a corriente; `docker restart codea-prod-tunnel-1` si Cloudflare responde 530; tener el video de respaldo y las instrucciones de [`manual-instalacion.md`](manual-instalacion.md) (2.8) | **[completar]** | Mitigado en parte (hay URL pública con HTTPS; sin monitoreo) |
| R2 | **No hay APK de release firmado**: sin keystore no se puede instalar | A | A | Crear el keystore (una persona del equipo, fuera de git), configurar `signingConfig`, probar el APK en un teléfono limpio | **[completar]** | Abierto |
| R3 | **Pérdida del keystore** una vez creado | B | A | Respaldo en dos lugares privados; sin él no se puede actualizar la app | **[completar]** | Preventivo |
| R4 | ~~Demo en dispositivo físico con el radio del checador~~ y ~~mapa en blanco~~ | — | — | **Resuelto (18/oct):** el checador ya no usa ubicación ni mapa | — | Cerrado |
| R6 | **Textos de la app que prometen lo que no se hace** («la ubicación no se guarda», «la salida se registra sola») | B | M | **Corregidos** en `f814763`. Queda hacer una revisión completa de los textos de la interfaz ([`etica-legal.md`](etica-legal.md), ETL-03) | **[completar]** | Mitigado |
| R7 | **Datos de menores sin política de retención y sin consentimiento registrado** | M | A | Aprobar la política de [`clasificacion-datos.md`](clasificacion-datos.md) (incluido cuánto duran los respaldos, hoy 14 días), responsable y canal en el aviso, y el procedimiento ante filtraciones. El respaldo diario existe desde el 20/oct | **[completar]** | Abierto |
| R8 | **Servidor compartido con cuentas de prueba que todo el equipo conoce** (las 22 de `seed_demo.py`, con una sola contraseña de grupo) y valores débiles por omisión en `.env.example` y `.env.dev` | M | M | No poner datos reales en esa base mientras sea de pruebas; antes de un piloto con familias, empezar con una base limpia y contraseñas propias | **[completar]** | Abierto |
| R9 | **Pruebas o seeds que apuntan a la base equivocada** | M | A | Las pruebas usan siempre `guarderia_test` en el Postgres local; el backend local se configura con `.env.dev`, que no trae la base del servidor compartido (solo vive en `.env.prod`). Nunca correr pruebas ni seeds contra Aiven | **[completar]** | Mitigado en el diseño; el 19/oct pasó con el seed (R20) |
| R10 | **Conflictos de fusión**: `CodeaNavHost.kt` concentra todas las rutas (cientos de líneas) y fue el archivo con más conflictos | M | M | Ramas cortas y fusiones frecuentes; a futuro, partirlo por rol | **[completar]** | Abierto |
| R11 | **Ramas por integrar**: `checador-menu` no está en `main` (solo en el remoto); `feature/pagos` no se integra por decisión | B | B | Decidir qué ramas se borran y cuáles se conservan antes de la entrega | **[completar]** | Abierto |
| R12 | **Rúbrica sin cubrir**: escaneo con OWASP ZAP, pruebas de interfaz, medición de fotos en menos de 4 s, y escribir sin conexión **solo la asistencia** (RF-11 y RNF07 en 🟡) | A | M | Priorizar: ZAP y la medición son cortos de hacer (ya hay una URL pública contra la cual correr ZAP); ampliar el modo sin conexión a reportes depende del tiempo y de resolver antes la privacidad de la copia local (R18) | **[completar]** | Abierto |
| R13 | **Código que depende de una sola persona o de pocos días**: gran parte de la API, la seguridad y la integración se hizo entre el 13 y el 18 de octubre | M | M | Repartir la presentación y la documentación; que más de una persona pueda levantar el sistema siguiendo el manual | **[completar]** | Abierto |
| R14 | **Material de la presentación sin hacer**: guion de 15 minutos, video de respaldo, captura del tablero | A | M | Reservar el martes y el miércoles (días de solo corregir y documentar) | **[completar]** | Abierto |
| R15 | **Decisiones pendientes sin responsable**: de las once, solo #7 y #11 están aprobadas; #8, #9 y #10 están en el código sin aprobación | M | M | Pedir al responsable del equipo que apruebe o rechace antes de la entrega | **[completar]** | Abierto |
| R16 | **Diferencias entre el prototipo y lo hecho**: otras pantallas pueden ser prototipos con datos fijos, como lo era el checador | M | M | Recorrer los tres roles en el teléfono contra el backend y anotar cada falla en `RECORRIDO.md` | **[completar]** | En curso (Bloque 0) |
| R17 | **Disponibilidad de personas del equipo** hasta el jueves | **[completar]** | **[completar]** | **[completar]** | **[completar]** | — |
| R18 | **Copia local sin cifrar** en el teléfono (`codea.db`): nombres de niños de Mi sala y, desde el 20/oct, la última respuesta de la API de los tres roles (datos de salud, teléfonos, listas del admin); además, las fotos ya vistas en la caché de imágenes | M | M | Mitigado en parte: las cachés se borran al terminar la sesión (la de Mi sala, verificado a mano en el teléfono) y la cola de marcas va ligada a su dueña. Pendiente: cifrar los campos con el Keystore, caducar la caché, borrar la caché de imágenes al cerrar sesión y una prueba automática del borrado (ver [`clasificacion-datos.md`](clasificacion-datos.md), 4.2.1). Riesgo MEDIO con sesión abierta y BAJO sin ella | **[completar]** | Abierto |
| R19 | **WorkManager en una app que declaraba «sin segundo plano»** (RNF06) | B | B | Texto de RNF06 ya corregido; el trabajo corre solo con red y con espera creciente | **[completar]** | Mitigado |
| R20 | **Volver a borrar los datos del servidor compartido** con `seed_demo.py --borrar-todo` (ya pasó el 19/oct, [`INCIDENTE-2026-10-19.md`](../INCIDENTE-2026-10-19.md)) | M | A | Respaldo diario desde el 20/oct (14 días, restauración probada). Pendiente: el candado que haga que el seed se niegue fuera de desarrollo salvo que se escriba el nombre de la base, y que saque un respaldo antes | **[completar]** | Mitigado en parte |
| R21 | **Respaldos en la misma máquina que el servidor**, sin cifrar | B | A | Copiar los respaldos a un segundo lugar privado; si la Mac falla, hoy se pierden las fotos y sus respaldos (la base sigue en Aiven) | **[completar]** | Abierto |

---

## 4. Distribución del trabajo

### 4.1 Lo que dice el historial de git

Corte: commit `043c2d6` (20/oct). Se cuentan los commits de trabajo (sin fusiones) de su historia (`git shortlog -sn --no-merges HEAD`).

| Nombre en git | Commits | Primer y último día | Archivos tocados (suma por commit) | Líneas añadidas / eliminadas |
|---|---:|---|---|---|
| Oscar Olivares | 49 | 17-oct al 20-oct | app 390 · API 87 · pruebas API 13 · pruebas app 16 · docs 93 · otros 7 | 13 265 / 7 187 |
| StarLord133 | 33 | 29-sep al 20-oct | app 253 · API 179 · pruebas API 42 · pruebas app 18 · docs 147 · otros 5 | 24 052 / 2 637 |
| IsabelV | 16 | 16-sep al 18-oct | app 275 · API 131 · pruebas API 8 · pruebas app 6 · docs 10 · otros 16 | 16 299 / 1 531 |
| Carlos Arias | 9 | 29-sep | app 30 · otros 1 | 1 824 / 538 |
| Tristan Olivares | 2 | 24-sep | app 34 · docs 4 · otros 3 | 2 110 / 137 |
| osc4r-t | 1 | 29-sep | app 60 · pruebas app 3 · docs 1 · otros 1 | 3 749 / 2 498 |
| Isabel Vaca | 1 | 16-sep | otros 2 | 28 / 0 |

Y 28 commits de fusión: `oscaro214` (8), `StarLord133` (7), `Isabel Vaca` (4), `IsabelV` (4), `Carlos-EAC` (2), `Oscar Olivares` (2) y `osc4r-t` (1).

**Cómo leerlo, con cuidado:**

1. **Los nombres de git no son personas.** Hay al menos tres pares que parecen la misma persona con dos identidades: *IsabelV* e *Isabel Vaca* (y la cuenta de GitHub *IsabelVaca* dueña de los PR), *Oscar Olivares* y *osc4r-t* (el correo de GitHub del primero contiene `Osc4r-t`), y *Carlos Arias* y *Carlos-EAC*. *oscaro214* solo aparece en fusiones de PR y **no se puede saber** desde git de quién es. *StarLord133* era la cuenta del usuario de la sesión que redactó la primera versión de este documento. *Tristan Olivares* usa otro correo y **no se puede saber** si es otra persona. **El equipo debe llenar la siguiente tabla.**
2. **Los commits y las líneas no miden esfuerzo.** Un commit puede ser un archivo generado o un cambio mínimo; las líneas incluyen documentos y recursos. Faltan además las revisiones, la coordinación, el diseño de los prototipos, el tablero y todo lo que no deja un commit.
3. **La historia de la rama no incluye todo.** `git shortlog -sn --all` cuenta más commits porque suma ramas no integradas (por ejemplo `checador-menu` y `feature/pagos`).
4. **El 29 de septiembre concentra 20 commits** de la primera entrega, de varias personas el mismo día, y del 17 al 20 de octubre se hicieron 81 de los 111 commits de trabajo. Entre el 30 de septiembre y el 10 de octubre casi no hay commits.

### 4.2 Distribución por persona y por área (a llenar por el equipo)

| Persona | Nombres en git | Rol o responsabilidad acordada | Áreas principales | Horas aproximadas |
|---|---|---|---|---|
| **[completar por el equipo]** | **[completar]** | **[completar]** | **[completar]** | **[completar]** |
| **[completar por el equipo]** | **[completar]** | **[completar]** | **[completar]** | **[completar]** |
| **[completar por el equipo]** | **[completar]** | **[completar]** | **[completar]** | **[completar]** |
| **[completar por el equipo]** | **[completar]** | **[completar]** | **[completar]** | **[completar]** |

### 4.3 Qué área mueve cada nombre (según archivos)

Con los archivos tocados en 4.1, a modo de pista y no de reparto oficial:

| Área | Quién deja más huella en git |
|---|---|
| Pantallas y navegación de la app | IsabelV (también el pase de salida), osc4r-t, Oscar Olivares (rediseño del admin, del padre y de la docente, 19 y 20/oct), StarLord133 (Mi sala, pase de lista y Mi día), Tristan Olivares y Carlos Arias |
| Backend (módulos, modelos, migraciones) | IsabelV (esqueleto y modelos) y StarLord133 (API completa, seguridad) |
| Login con JWT (API y app), chat, checador sin ubicación, horario del centro, servidor compartido (túnel y respaldos), caché en Room, seed de prueba y documentación del proyecto | Oscar Olivares |
| Pruebas de la API | StarLord133 (control de acceso, seguridad, borrado de fotos, hora de entrada, menú, avisos con imagen), Oscar Olivares (login, checador, horario, vincular padres) |
| Integración, recorrido en teléfono, seguridad, auditoría, invitación por WhatsApp, hora de entrada, avisos con imagen, menú del día y borrado directo de fotos | StarLord133 |
| Planes de acción y bitácora | StarLord133. Los documentos de `docs/entrega/` los debe revisar y firmar el equipo |

El tablero de Planner con las tarjetas asignadas es la otra mitad de la evidencia de distribución: **[completar por el equipo: enlace y captura]**.

Para volver a calcular: `git shortlog -sn --no-merges HEAD` y `git log --no-merges --numstat --format='@@%an'`.

---

## 5. Retrospectiva

Está escrita en dos partes. La primera sale de lo que **se puede ver** en el repositorio y los documentos. La segunda solo la pueden llenar las personas del equipo; no se inventó.

### 5.1 Lo que muestran los datos

**Lo que funcionó**

- **Capas y convenciones:** la app separa pantallas, estado, datos y dominio, y la API separa `router`, `service`, `schemas` y `models`. Eso permitió agregar módulos y reglas en el mismo estilo (login, chat, auditoría, revisión de fotos) en pocos días.
- **Diseño del login antes del código:** [`docs/plan/04-api-login-jwt.md`](../plan/04-api-login-jwt.md) existía antes de implementarlo; el resultado cerró tres requisitos (RF-01, RNF01, RNF03) con pruebas en ambos lados.
- **Reglas iguales en la API y en la app:** las mismas pruebas de dominio en los dos lados evitan que una regla dé distinta respuesta.
- **Documentar mientras se trabaja:** README por carpeta, `BITACORA.md`, `RECORRIDO.md` y `PLAN-ACCION.md`. La bitácora del 18 de octubre permite reconstruir qué se hizo, por qué y cómo se verificó.
- **Probar contra un Postgres real**, no contra uno simulado, y pasar de 43 pruebas de la API (16/oct) a 343 (20/oct), y a 140 JVM de la app, con matriz de roles, IDOR e inyección.
- **Reaccionar a un incidente con documentos y con un arreglo:** la noche del borrado de datos (19/oct) se escribió `INCIDENTE-2026-10-19.md` con causas y pendientes, y al día siguiente ya había respaldo diario con restauración probada.
- **Recorrer la app en un teléfono físico** destapó fallas que ninguna prueba JVM mostraba: una pestaña que cerraba la app, colores ilegibles en modo oscuro y un checador que era un prototipo.

**Lo que no funcionó**

- **Los avances se declararon sin ejecutar la app.** El requisito CRF-05 figuraba como cumplido cuando el checador usaba datos fijos (D10). Los porcentajes de `REQUISITOS.md` se calcularon leyendo código.
- **Se construyó la API sin probarla** (commit `c51ac75`) y se probó días después (D11).
- **El trabajo se concentró en pocos días** (29 de septiembre; 12 al 18 de octubre) y casi no hubo actividad entre el 30 de septiembre y el 10 de octubre.
- **Las decisiones técnicas quedaron abiertas.** De las once de `DECISIONES-PENDIENTES.md` solo dos se aprobaron (#7 el 19/oct y #11 el 20/oct, a días de la entrega), y tres piezas fuera del marco siguen en el código sin aprobación (#8, #9 y #10).
- **El despliegue y el release se dejaron para el final:** el servidor público llegó el 19/oct (tres días antes de la entrega), sobre la Mac de un integrante, y al corte no hay APK firmado; el modo sin conexión llegó a última hora y solo escribe la asistencia de Mi sala.
- **Se compartió un servidor sin respaldo:** el 19/oct un seed de prueba borró cuentas, fotos y avisos del servidor compartido y no se pudieron recuperar (D20).
- **Se mezclaron los servidores:** cuentas dadas de alta en el servidor local se intentaron activar en el compartido, y un APK apuntaba a `localhost` (`INCIDENTE-2026-10-19.md`).
- **Los textos de privacidad de la app no se revisaron contra el código** (ubicación «no se guarda»; «salida automática»).
- **La documentación se desactualiza rápido** (D17): varios documentos tienen cifras de pruebas distintas.
- **Un archivo de navegación gigante** concentra los conflictos de fusión.
- **Identidades de git duplicadas** impiden saber, sin preguntar, quién hizo qué.

**Qué cambiaríamos** *(propuestas basadas en lo anterior; el equipo decide)*

1. Marcar un requisito como cumplido solo después de verlo en un dispositivo.
2. Escribir la prueba junto con cada endpoint.
3. Resolver las decisiones del marco técnico la primera semana, no la última.
4. Desplegar una versión mínima pronto y mejorar sobre ella, con respaldos desde el primer día y un solo servidor de referencia para todo el equipo.
5. Una identidad de git por persona.
6. Partir el archivo de navegación por rol.
7. Revisar los textos de la interfaz con la misma atención que el código.

### 5.2 Lo que solo el equipo puede contar

| Pregunta | Respuesta |
|---|---|
| ¿Cuántas personas trabajaron, con qué horas por semana y en qué turnos? | **[completar por el equipo]** |
| ¿Cómo se repartieron las tareas y cómo se decidía quién hacía qué? | **[completar por el equipo]** |
| ¿Qué pasó entre el 30 de septiembre y el 10 de octubre? | **[completar por el equipo]** |
| ¿Por qué se decidió quitar el módulo de pagos? ¿Quién lo decidió? | **[completar por el equipo]** |
| ¿Qué comunicación con el cliente hubo y qué cambió a partir de ella? | **[completar por el equipo]** |
| ¿Qué fue lo más difícil técnicamente y cómo se resolvió? | **[completar por el equipo]** |
| ¿Qué fue lo mejor del trabajo en equipo? ¿Qué fricciones hubo? | **[completar por el equipo]** |
| ¿Qué herramienta de seguimiento se usó (Planner o Teams) y cómo? | **[completar por el equipo]** |
| ¿Qué aprendieron cada una y cada uno de los integrantes? | **[completar por el equipo]** |
| ¿Qué harían distinto con otra oportunidad? | **[completar por el equipo]** |
| ¿Qué pendientes se llevan a una siguiente etapa y quién los toma? | **[completar por el equipo]** |
| Autoevaluación de la calidad del producto entregado | **[completar por el equipo]** |
