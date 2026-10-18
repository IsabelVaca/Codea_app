# Administración del proyecto

Parte 5 de la rúbrica: bitácora de hitos, desviaciones respecto al plan, riesgos, distribución del trabajo y retrospectiva. Corte: **2026-10-18**, rama `integracion`, commit `22c0008` (66 commits en su historia: 55 de trabajo y 11 de fusión).

Qué sale de qué:

- **Hitos y distribución:** `git log` y `git shortlog` (comandos al final de cada sección, para volver a calcularlos).
- **Desviaciones:** [`docs/PLAN.md`](../PLAN.md), [`docs/PLAN-ACCION.md`](../PLAN-ACCION.md), [`docs/REQUISITOS.md`](../REQUISITOS.md), [`docs/DECISIONES-PENDIENTES.md`](../DECISIONES-PENDIENTES.md) y [`docs/MARCO-TECNICO.md`](../MARCO-TECNICO.md).
- **Riesgos:** lectura del código y de esos documentos. Son juicios míos; el equipo debe corregirlos.
- **Lo que solo el equipo sabe** (horas, acuerdos, causas, el tablero) está marcado **«[completar por el equipo]»**. No se inventó.

El tablero (Planner o Teams) no se puede consultar desde el repositorio: el enlace y la captura que pide la rúbrica son **[completar por el equipo]**.

---

## 1. Bitácora de hitos

Cada fila agrupa los commits de `git log` por lo que lograron. «Autoría» es el nombre que aparece en git, que no siempre coincide con la persona (ver sección 4). El detalle día por día del 18 de octubre está en [`docs/BITACORA.md`](../BITACORA.md).

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

### Hitos por cumplir antes de la entrega

| Fecha límite | Hito | Estado |
|---|---|---|
| Lunes 19 de octubre, noche | Congelamiento de funciones nuevas (`PLAN-ACCION.md`) | ⏳ |
| 20 y 21 de octubre | Solo corregir y documentar | ⏳ |
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
| D2 | **Sin conexión (parcial)** | RF-11 y RNF07: las funciones principales sin internet; sincronización posterior | Solo la **asistencia de Mi sala** funciona sin conexión (Room, cola de marcas y WorkManager, commit `99fac75`; probada en el teléfono con la API apagada). Reportes, fotos, chat, checador, documentos y avisos siguen necesitando red; el padre y el administrador no tienen nada sin conexión. No hay pruebas de Room en JVM. El Bloque 3 de `PLAN-ACCION.md` decía «solo si el bloque 2 termina el lunes» | Falta de tiempo **[confirmar por el equipo]** | RF-11 y RNF07 en 🟡 (antes ❌); las dos filas de la rúbrica (operación sin conexión y sincronización) en 🟡. Trae tres dependencias nuevas dentro del marco: Room 2.8.4, WorkManager 2.11.2 y KSP 2.3.12 |
| D3 | **Consulta con IA** | CRF-04: dictado y consulta con IA | Solo el **dictado** (con el reconocedor de voz del sistema). La consulta con IA no existe | **[completar por el equipo]** | CRF-04 en 🟡 |
| D4 | **Calendario de eventos y citas** | CRF-03 | El calendario del padre muestra solo asistencia | **[completar por el equipo]** | CRF-03 en 🟡 |
| D5 | **Entrada y salida del niño** | RF-03 | Solo presente o falta por día; no hay hora de entrada ni de salida **del niño**. (La entrada y la **salida de la docente** sí existen desde el commit `f814763`, con los textos de la app corregidos) | **[completar por el equipo]** | RF-03 en 🟡 |
| D6 | **Huella, historias de usuario** | RF-13, HN1 a HN5 | Sin hacer, o parciales (HN4 y HN5). El resumen semanal (RF-12) ya se hizo, en la API y en la app | **[completar por el equipo]** | Ver 2.4 |
| D7 | **Notificaciones** | Tarea abierta del tablero | Ninguna notificación del teléfono (ni push ni local). El chat se actualiza consultando cada 10 s; nada de WebSocket ni SSE | Firebase está excluido por el marco; la alternativa (WorkManager) no se hizo | Las familias no se enteran de mensajes ni avisos si no abren la app |
| D8 | **Resumen del día sin captura en la app (resuelto)** | P1 #12 de `docs/plan/01-app-pantallas.md` (Día del niño) | Era una brecha: la docente no tenía dónde capturar el resumen. Se resolvió en el commit `20e7de1` con **Mi sala → «Resumen del día»**, con 5 pruebas JVM y prueba de punta a punta en el teléfono (la mamá lo vio en Hoy) | Hallazgo de la revisión de los documentos de la entrega | RF-04 vuelve a ✅ |

### 2.2 Cambios técnicos

| # | Desviación | Plan | Qué pasó | Efecto |
|---|---|---|---|---|
| D9 | **Login con JWT propio en lugar de Firebase Auth** | Tareas del tablero que mencionaban Firebase | Se reemplazó por un JWT de 5 minutos con refresh rotado (decisión #1). Firebase está excluido por el marco | Cierra RF-01, RNF01 y RNF03. **Falta la aprobación formal** de la decisión |
| D10 | **Checador: el prototipo se había contado como terminado** | CRF-05 aparecía en ✅ | El 18 de octubre el recorrido en el teléfono mostró que la pantalla era un prototipo con fecha, hora y distancia fijas, y que el servidor nunca podía rechazar por distancia. Se rehízo con GPS y hora reales | Una lección sobre cómo se marcaba el avance; ver la retrospectiva |
| D11 | **API construida antes de probarse** | Pruebas junto con el código | El commit `c51ac75` se llama «API CONSTRUIDA SIN TESTEAR». Las pruebas llegaron después (de 43 pruebas el 16 de octubre a 276 el 18 de octubre, según `guarderia-api/tests/README.md`) | El riesgo de errores ocultos duró varios días |
| D12 | **Release sin firmar, sin minificar y sin servidor** | Rúbrica, parte 1: build de release firmado y backend en URL pública | No existe keystore ni `signingConfig`; la minificación está apagada; no hay despliegue ni HTTPS público | Ver [`plan-implantacion.md`](plan-implantacion.md) |
| D13 | **Mapa del checador en blanco** | Mapa del prototipo | Se usa Google Maps Compose, que necesita `MAPS_API_KEY`. No hay clave en el repositorio ni en `local.properties` entregable, y la decisión #3 sigue sin aprobarse | El mapa sale en blanco; registrar la entrada sí funciona |
| D14 | **Pruebas de interfaz inexistentes** | Rúbrica, parte 3 | Solo hay `ExampleInstrumentedTest`. Pruebas JVM de dominio y de sesión sí hay | El marco pide Compose UI Test |
| D15 | **Estado de los ViewModels** | Marco: `StateFlow` | `CodeaViewModel` y `ParentViewModel` siguen usando `mutableStateOf` (`MARCO-TECNICO.md`, 🟡). La inyección de dependencias es manual y está a medias | Deuda técnica |
| D16 | **Fotos de baja calidad** | Marco: Photo Picker, FileProvider, EXIF y compresión | La cámara usa `TakePicturePreview`, que devuelve una miniatura. No hay compresión previa | Las fotos de los reportes son miniaturas |
| D17 | **Documentación desactualizada en puntos concretos** | Documentos al día | `docs/GUIA-CONTINUAR.md` todavía dice que para un teléfono físico hay que editar `BASE_URL` en `ApiConfig.kt` (ahora es `-PapiBaseUrl`) y que hay 11 y 43 pruebas | Confusión de quien llegue nuevo |

### 2.3 Excepciones al marco técnico (decisiones pendientes)

El marco técnico del curso (<https://startdroid.pages.dev/>) manda; lo que se sale se registra en [`DECISIONES-PENDIENTES.md`](../DECISIONES-PENDIENTES.md) y no se implementa sin aprobación. **Ninguna de las diez tiene aprobación del responsable** (todas siguen en «⏳ Pendiente», salvo la #1, «implementada, falta la aprobación formal»).

| # | Tema | ¿Está en el código? | Estado real |
|---|---|---|---|
| 1 | Login con JWT propio | ✅ Sí | Implementado en `integracion`; falta la aprobación formal |
| 2 | Notificaciones con Firebase | ❌ No | No implementado. La alternativa dentro del marco (WorkManager y notificaciones locales) tampoco |
| 3 | Google Maps Compose | ✅ Sí | **En el código sin aprobación.** Sin clave, el mapa sale en blanco |
| 4 | Huella (`androidx.biometric`) | ❌ No | No implementado (RF-13) |
| 5 | Redis | ❌ No | No se usa; el límite de intentos usa Postgres |
| 6 | Grafana | ❌ No | No implementado; hay `/health/live` y `/health/ready` |
| 7 | Túnel de Cloudflare | ❌ No | No implementado; sin HTTPS público |
| 8 | Librería `reorderable` | ✅ Sí | **En el código sin aprobación** (arrastrar momentos de la rutina) |
| 9 | SeaweedFS como almacén | ✅ Sí | **En uso en desarrollo sin aprobación**; sin almacén de producción elegido |
| 10 | Dictado con `RecognizerIntent` | ✅ Sí | **En el código sin aprobación**; usa el reconocedor del sistema |

En suma: **cuatro** piezas fuera de la lista del marco (#3, #8, #9 y #10) estaban en el código antes de que se escribiera `PLAN-ACCION.md` y siguen ahí sin aprobación; **cinco** (#2, #4, #5, #6 y #7) no se implementaron, y por eso no cuentan como excepción pero sí como requisitos o tareas sin cubrir. La #1 se implementó y espera la aprobación formal. Además, `MARCO-TECNICO.md` pide confirmar `PyJWT` y `pwdlib[argon2]` (librerías del backend) y `kotlinx.serialization`.

### 2.4 Estado final de los requisitos

La fuente de verdad es [`docs/REQUISITOS.md`](../REQUISITOS.md); la matriz de trazabilidad con evidencia y resultado de pruebas está en [`matriz-trazabilidad.md`](matriz-trazabilidad.md) y no se repite aquí. Al corte, ese documento cuenta **16 cumplidos, 10 parciales y 6 no cumplidos de 32**. Con la regla de que los parciales cuentan medio, son 21 de 32, es decir **~66 %**. (Una versión anterior anotaba ~69 % con 18/6/8: era un error de suma. La revisión de cada fila contra su evidencia bajó RF-04, RF-05 y RF-09 a 🟡; después RF-04 volvió a ✅ al construirse su pantalla, y RF-11 y RNF07 subieron a 🟡 por la asistencia sin conexión.) Lo no cumplido o parcial:

| Estado | Requisitos |
|---|---|
| ❌ No cumplido | CRF-01 (por decisión del equipo), RF-13, RNF04, HN1, HN2, HN3 |
| 🟡 Parcial | CRF-03, CRF-04, RF-03 (hora de entrada y salida **del niño**), RF-05 (la API está probada con dos niños; la pantalla de selección no se recorrió en el teléfono), RF-09 (captura con cámara sin probar), RF-11 y RNF07 (solo la asistencia de Mi sala), RNF05 (sin medir), HN4, HN5 |

Queda sin hacer, aparte de los requisitos: cifrar la base local del teléfono, salida automática de la docente, mapa sin clave de Google, despliegue con HTTPS, APK firmado, política de retención y un comando para crear solo al primer administrador.

Los parciales cuentan medio en ese porcentaje. Desde la revisión del 18 de octubre un ✅ solo se escribe si la fila cita evidencia (prueba automática o prueba en el teléfono); el caso del checador (D10) muestra por qué.

---|---|
| ❌ No cumplido | CRF-01 (por decisión del equipo), RF-13, RNF04, HN1, HN2, HN3 |
| 🟡 Parcial | CRF-03, CRF-04, RF-03, RF-04 (falta la pantalla del resumen del día), RF-05 (sin prueba con dos o más niños), RF-09 (captura con cámara sin probar), RF-11 y RNF07 (solo la asistencia de Mi sala), RNF05 (sin medir), HN4, HN5 |

Los parciales cuentan medio en ese porcentaje. Desde la revisión del 18 de octubre un ✅ solo se escribe si la fila cita evidencia (prueba automática o prueba en el teléfono); el caso del checador (D10) muestra por qué.

---|---|
| ❌ No cumplido | CRF-01 (por decisión del equipo), RF-11, RF-13, RNF04, RNF07, HN1, HN2, HN3 |
| 🟡 Parcial | CRF-03, CRF-04, RF-03, RNF05 (sin medir), HN4, HN5 |

Los parciales cuentan medio en ese porcentaje. Lo declarado como cumplido se revisó leyendo el código, no ejecutando la app, salvo lo que dice `RECORRIDO.md` sobre el teléfono físico; el caso del checador (D10) muestra que un ✅ puede sobreestimar.

---

## 3. Riesgos

Probabilidad e impacto: **A**lta, **M**edia, **B**aja (juicio del documento, a corregir por el equipo). La entrega es el jueves 22 de octubre a las 23:59; al corte quedan cuatro días.

| ID | Riesgo | Prob. | Imp. | Mitigación | Responsable | Estado |
|---|---|---|---|---|---|---|
| R1 | **No hay backend desplegado ni HTTPS**: la rúbrica pide una URL pública o instrucciones para levantarlo | A | A | Si no hay despliegue, entregar las instrucciones de [`manual-instalacion.md`](manual-instalacion.md) y el video de respaldo; decidir #7 y #9 | **[completar]** | Abierto |
| R2 | **No hay APK de release firmado**: sin keystore no se puede instalar | A | A | Crear el keystore (una persona del equipo, fuera de git), configurar `signingConfig`, probar el APK en un teléfono limpio | **[completar]** | Abierto |
| R3 | **Pérdida del keystore** una vez creado | B | A | Respaldo en dos lugares privados; sin él no se puede actualizar la app | **[completar]** | Preventivo |
| R4 | **Demo en dispositivo físico**: el checador compara con las coordenadas del seed (Monterrey); donde se haga la demo la docente saldrá «fuera del perímetro» | M | M | El administrador puede fijar la ubicación del centro desde la app («Usar mi ubicación actual», estando en el lugar). Registrar una entrada dentro del perímetro **todavía no se probó en el teléfono** (`RECORRIDO.md`) | **[completar]** | Abierto |
| R5 | **Mapa en blanco** por falta de `MAPS_API_KEY` | A | B | Obtener una clave o aprobar el mapa dibujado de la rama `checador-menu` (decisión #3). No impide registrar la entrada | **[completar]** | Abierto |
| R6 | **Textos de la app que prometen lo que no se hace** («la ubicación no se guarda», «la salida se registra sola») | B | M | **Corregidos** en `f814763`. Queda hacer una revisión completa de los textos de la interfaz ([`etica-legal.md`](etica-legal.md), ETL-03) | **[completar]** | Mitigado |
| R7 | **Datos de menores sin política de retención, sin respaldo y sin consentimiento registrado** | M | A | Aprobar la política de [`clasificacion-datos.md`](clasificacion-datos.md), responsable y canal en el aviso, y el procedimiento ante filtraciones | **[completar]** | Abierto |
| R8 | **Base de Aiven compartida con contraseña conocida** (`codea1234`, 15 usuarios de prueba) y valores débiles por omisión en `.env.example` | M | M | No poner datos reales en esa base; cambiar contraseñas antes de cualquier uso fuera de las pruebas | **[completar]** | Abierto |
| R9 | **Pruebas que apuntan a la base equivocada**: el `.env` lleva la URL de Aiven y las pruebas leen su contraseña | M | A | Las pruebas usan siempre `guarderia_test` en el Postgres local; correrlas con `POSTGRES_PASSWORD=<la del contenedor>` y nunca contra Aiven | **[completar]** | Mitigado en el diseño; sigue siendo un descuido posible |
| R10 | **Conflictos de fusión**: `CodeaNavHost.kt` concentra todas las rutas (cientos de líneas) y fue el archivo con más conflictos | M | M | Ramas cortas y fusiones frecuentes; a futuro, partirlo por rol | **[completar]** | Abierto |
| R11 | **Ramas por integrar**: `checador-menu` no está en `main`; `feature/pagos` no se integra por decisión | B | B | Decidir qué ramas se borran y cuáles se conservan antes de la entrega | **[completar]** | Abierto |
| R12 | **Rúbrica sin cubrir**: escaneo con OWASP ZAP, pruebas de interfaz, medición de fotos en menos de 4 s, y el modo sin conexión **solo para la asistencia** (RF-11 y RNF07 en 🟡) | A | M | Priorizar: ZAP y la medición son cortos de hacer; ampliar el modo sin conexión a reportes depende del tiempo y de resolver antes la privacidad de la copia local (R18) | **[completar]** | Abierto |
| R13 | **Código que depende de una sola persona o de pocos días**: gran parte de la API, la seguridad y la integración se hizo entre el 13 y el 18 de octubre | M | M | Repartir la presentación y la documentación; que más de una persona pueda levantar el sistema siguiendo el manual | **[completar]** | Abierto |
| R14 | **Material de la presentación sin hacer**: guion de 15 minutos, video de respaldo, captura del tablero | A | M | Reservar el martes y el miércoles (días de solo corregir y documentar) | **[completar]** | Abierto |
| R15 | **Decisiones pendientes sin responsable**: ninguna de las diez tiene aprobación | M | M | Pedir al responsable del equipo que apruebe o rechace antes del congelamiento | **[completar]** | Abierto |
| R16 | **Diferencias entre el prototipo y lo hecho**: otras pantallas pueden ser prototipos con datos fijos, como lo era el checador | M | M | Recorrer los tres roles en el teléfono contra el backend y anotar cada falla en `RECORRIDO.md` | **[completar]** | En curso (Bloque 0) |
| R17 | **Disponibilidad de personas del equipo** hasta el jueves | **[completar]** | **[completar]** | **[completar]** | **[completar]** | — |
| R18 | **Copia local de nombres de niños sin cifrar** en el teléfono de la docente (`codea.db`) | M | M | Mitigado en parte: la caché se borra al terminar la sesión (verificado a mano en el teléfono) y la cola de marcas ya va ligada a su dueña (Room v2). Pendiente: cifrar los campos con el Keystore, caducar la caché y una prueba automática del borrado (ver [`clasificacion-datos.md`](clasificacion-datos.md), 4.2.1). Riesgo MEDIO con sesión abierta y BAJO sin ella | **[completar]** | Abierto |
| R19 | **WorkManager en una app que declaraba «sin segundo plano»** (RNF06) | B | B | Texto de RNF06 ya corregido; el trabajo corre solo con red y con espera creciente | **[completar]** | Mitigado |

---

## 4. Distribución del trabajo

### 4.1 Lo que dice el historial de git

Corte: commit `22c0008`. Se cuentan los commits de trabajo (sin fusiones) de la rama `integracion` (`git shortlog -sn --no-merges HEAD`).

| Nombre en git | Commits | Primer y último día | Archivos tocados (suma por commit) | Líneas añadidas / eliminadas |
|---|---:|---|---|---|
| StarLord133 | 22 | 29-sep al 18-oct | app 149 · API 120 · pruebas API 24 · pruebas app 10 · docs 66 · otros 5 | 14 697 / 553 |
| IsabelV | 14 | 16-sep al 17-oct | app 237 · API 110 · pruebas API 4 · pruebas app 1 · docs 5 · otros 16 | 13 402 / 933 |
| Carlos Arias | 9 | 29-sep | app 30 · otros 1 | 1 824 / 538 |
| Oscar Olivares | 6 | 17-oct | app 78 · API 44 · pruebas API 6 · pruebas app 6 · docs 17 · otros 4 | 5 511 / 593 |
| Tristan Olivares | 2 | 24-sep | app 34 · docs 4 · otros 3 | 2 110 / 137 |
| osc4r-t | 1 | 29-sep | app 60 · pruebas app 3 · docs 1 · otros 1 | 3 749 / 2 498 |
| Isabel Vaca | 1 | 16-sep | otros 2 | 28 / 0 |

Y 11 commits de fusión: `StarLord133` (2: integración del login JWT y del chat), `Isabel Vaca` (3), `IsabelV` (3), `Carlos-EAC` (2) y `osc4r-t` (1).

**Cómo leerlo, con cuidado:**

1. **Los nombres de git no son personas.** Hay al menos tres pares que parecen la misma persona con dos identidades: *IsabelV* e *Isabel Vaca* (y la cuenta de GitHub *IsabelVaca* dueña de los PR), *Oscar Olivares* y *osc4r-t* (el correo de GitHub del primero contiene `Osc4r-t`), y *Carlos Arias* y *Carlos-EAC*. *StarLord133* es la cuenta del correo del usuario de esta sesión. *Tristan Olivares* usa otro correo y **no se puede saber** si es otra persona. **El equipo debe llenar la siguiente tabla.**
2. **Los commits y las líneas no miden esfuerzo.** Un commit puede ser un archivo generado o un cambio mínimo; las líneas incluyen documentos y recursos. Faltan además las revisiones, la coordinación, el diseño de los prototipos, el tablero y todo lo que no deja un commit.
3. **La historia de la rama no incluye todo.** `git shortlog -sn --all` cuenta más commits porque suma ramas no integradas (por ejemplo `checador-menu` y `feature/pagos`).
4. **El 29 de septiembre concentra 20 commits** de la primera entrega, de varias personas el mismo día, y el 17 y el 18 de octubre suman 28 más (de los 66 de la historia). Entre el 30 de septiembre y el 10 de octubre casi no hay commits.

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
| Pantallas y navegación de la app | IsabelV, osc4r-t, Oscar Olivares, Tristan Olivares y Carlos Arias |
| Backend (módulos, modelos, migraciones) | IsabelV (esqueleto y modelos) y StarLord133 (API completa, seguridad) |
| Login con JWT (API y app), chat y documentación del proyecto | Oscar Olivares |
| Pruebas de la API | StarLord133 (control de acceso, seguridad, borrado de fotos), Oscar Olivares (login) |
| Integración, recorrido en teléfono, checador real, seguridad, auditoría | StarLord133 |
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
- **Probar contra un Postgres real**, no contra uno simulado, y pasar de 43 a 276 pruebas de la API (y a 74 JVM de la app) en dos días, con matriz de roles, IDOR e inyección.
- **Recorrer la app en un teléfono físico** destapó fallas que ninguna prueba JVM mostraba: una pestaña que cerraba la app, colores ilegibles en modo oscuro y un checador que era un prototipo.

**Lo que no funcionó**

- **Los avances se declararon sin ejecutar la app.** El requisito CRF-05 figuraba como cumplido cuando el checador usaba datos fijos (D10). Los porcentajes de `REQUISITOS.md` se calcularon leyendo código.
- **Se construyó la API sin probarla** (commit `c51ac75`) y se probó días después (D11).
- **El trabajo se concentró en pocos días** (29 de septiembre; 12 al 18 de octubre) y casi no hubo actividad entre el 30 de septiembre y el 10 de octubre.
- **Las decisiones técnicas quedaron abiertas.** Las diez de `DECISIONES-PENDIENTES.md` siguen sin respuesta, cuatro piezas fuera del marco se quedaron en el código sin aprobación y el despliegue depende de tres de ellas.
- **El despliegue y el release se dejaron para el final** y, al corte, no están hechos; el modo sin conexión llegó a última hora y cubre solo la asistencia de Mi sala.
- **Los textos de privacidad de la app no se revisaron contra el código** (ubicación «no se guarda»; «salida automática»).
- **La documentación se desactualiza rápido** (D17): varios documentos tienen cifras de pruebas distintas.
- **Un archivo de navegación gigante** concentra los conflictos de fusión.
- **Identidades de git duplicadas** impiden saber, sin preguntar, quién hizo qué.

**Qué cambiaríamos** *(propuestas basadas en lo anterior; el equipo decide)*

1. Marcar un requisito como cumplido solo después de verlo en un dispositivo.
2. Escribir la prueba junto con cada endpoint.
3. Resolver las decisiones del marco técnico la primera semana, no la última.
4. Desplegar una versión mínima pronto y mejorar sobre ella.
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
