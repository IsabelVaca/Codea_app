# Consideraciones éticas y legales

Parte 6 de la rúbrica, puntos 1 (criterios como requisitos verificables) y 4 (dilemas éticos). Los puntos 2 y 3 están en [`aviso-privacidad.md`](aviso-privacidad.md) y [`clasificacion-datos.md`](clasificacion-datos.md).

Estado a **2026-10-18**, `main` con los PR #7 a #9 y la invitación por WhatsApp (puesto al día el 18/oct por la noche; en esa revisión se corrió `pytest` de la API: 318 pruebas en verde). La columna «Estado» sale de **leer el código y los documentos**; en esta sesión no se corrió ninguna prueba, así que «Cumple» significa «el código lo hace y existe una prueba o una forma de comprobarlo», no «se corrió hoy y pasó». La [matriz de trazabilidad](matriz-trazabilidad.md) recoge estos mismos requisitos con su evidencia.

**Contexto:** Codea maneja datos de **menores** (nombre, fecha de nacimiento, alergias, reportes, fotos, documentos), datos de **salud**, la **ubicación** de las docentes al checar y las conversaciones entre familias y docentes. Eso exige más cuidado que una app común.

Marco de referencia: la Ley Federal de Protección de Datos Personales en Posesión de los Particulares y su normativa vigente (consentimiento, aviso de privacidad, derechos de acceso, rectificación, cancelación y oposición, datos sensibles, medidas de seguridad). **[Verificar con asesoría legal qué versión aplica al centro; este documento no es opinión jurídica.]** Es una guía de requisitos para el proyecto, no una certificación.

---

## 1. Requisitos éticos y legales verificables

Leyenda de estado: ✅ cumple · 🟡 cumple en parte · ❌ no cumple · ⚪ no evaluable sin despliegue.

Cada requisito dice **cómo se comprueba** (una prueba, un comando, una inspección o una demostración). Los comandos son de solo lectura.

### 1.1 Transparencia y consentimiento

| ID | Requisito | Cómo se verifica | Estado | Evidencia o hueco |
|---|---|---|---|---|
| ETL-01 | Antes de dar datos, la persona puede leer un aviso de privacidad completo. | Demostración: abrir la app sin sesión y en la activación de cuenta y buscar el aviso. Inspección: `grep -ri "privacidad" app/src/main` debe encontrar una pantalla. | ❌ | El aviso existe como documento ([`aviso-privacidad.md`](aviso-privacidad.md)) pero la app **no lo muestra ni pide aceptarlo**. |
| ETL-02 | El tratamiento de datos de salud y de imagen de menores cuenta con consentimiento expreso de la madre, el padre o el tutor, y queda constancia. | Inspección: debe existir una tabla o un campo con la fecha de aceptación; demostración: activar una cuenta nueva y ver que la pide. | ❌ | No hay campo ni pantalla de consentimiento. |
| ETL-03 | Lo que la app le dice a la persona coincide con lo que el sistema hace. | Inspección: revisar cada texto de privacidad y de funciones en `strings.xml` contra el código. | 🟡 | Los dos textos falsos que se habían encontrado (`checker_permission_body`: la ubicación «no se guarda»; `checker_exit_is_automatic` y `checker_done_exit`: la salida «automática») **se corrigieron** en el commit `f814763`, y el servidor ahora sí tiene salida (`POST /staff-attendance/check-out`). Falta una revisión de **todos** los textos; hasta que se haga, se cuenta en parte. |
| ETL-04 | Los permisos del teléfono se piden en el momento en que se usan y se explican. | Demostración en el teléfono: el permiso de ubicación aparece al abrir el checador con una explicación; la cámara y el micrófono, al usarlos. | ✅ | Pantallas `checker_permission_*`, `camera_permission_needed`, `dictation_permission_needed`. |

### 1.2 Minimización y finalidad

| ID | Requisito | Cómo se verifica | Estado | Evidencia o hueco |
|---|---|---|---|---|
| ETL-05 | El checador no toma la ubicación del teléfono ni hay rastreo en segundo plano. | Inspección: `grep -n "LOCATION" app/src/main/AndroidManifest.xml` no debe encontrar nada; `staff_check_in` tiene una restricción única por docente y día y ya no tiene columnas de ubicación. | ✅ | Desde el 18/oct el manifiesto no declara permisos de ubicación y la migración `e74f0a5b3d43` borra las coordenadas. En Aiven la migración **todavía no está aplicada**. |
| ETL-06 | El checador no guarda la posición de la docente en ningún caso. | Pruebas: `tests/test_teacher_day.py::test_check_in_ignores_any_location`: aunque el teléfono mande coordenadas, la respuesta no las trae ni se guardan. | ✅ | El servidor ya no recibe coordenadas (el cuerpo de `check-in` solo trae `late_reason`). |
| ETL-07 | No hay herramientas de analítica, publicidad ni Firebase en la app ni en el servidor. | Inspección de `app/build.gradle.kts` y `guarderia-api/requirements.txt`. | ✅ | Las dependencias no incluyen ninguna. |
| ETL-08 | Ningún menor tiene cuenta propia. | Inspección: el `enum Role` solo tiene `ADMIN`, `TEACHER` y `PARENT`. | ✅ | `app/modules/auth/models.py`. |
| ETL-09 | La bitácora y los registros no guardan contraseñas, códigos ni tokens. | Prueba: `tests/test_security.py::test_the_log_never_stores_passwords_codes_or_tokens`. | ✅ | Existe la prueba; no se corrió en esta sesión. |

### 1.3 Control de acceso y confidencialidad

| ID | Requisito | Cómo se verifica | Estado | Evidencia o hueco |
|---|---|---|---|---|
| ETL-10 | Una familia solo ve a sus hijos; una docente solo a los de su sala. Lo demás responde 404. | Pruebas: `tests/test_access_matrix.py` (`test_a_parent_cannot_read_another_childs_data` recorre día, asistencia, autorizados, documentos y fotos; `test_a_teacher_only_reads_children_of_her_room`; `test_not_even_the_existence_of_other_children_leaks`; `test_conversations_are_private_to_the_family_and_the_room`), más `test_child_scope` y `test_chat_scope`. | ✅ | Existen pruebas explícitas de IDOR para niños, día, calendario, autorizados, documentos, fotos y chats. Falta cubrir mensajes del chat leídos por el administrador (ETL-12). |
| ETL-11 | Cada rol solo llama a los endpoints de su rol. | Pruebas: `tests/test_access_matrix.py` (`test_every_private_endpoint_needs_a_token` y `test_roles_that_are_not_allowed_get_403` leen los roles de las rutas reales; `test_every_role_is_covered_by_the_matrix`). Documento generado: [`docs/api/matriz-de-roles.md`](../api/matriz-de-roles.md). | ✅ | Si se agrega una ruta sin protegerla, las pruebas fallan. |
| ETL-12 | El administrador no puede leer los mensajes entre familias y docentes a través de la API. | Prueba nueva a escribir: con el token del administrador, `GET /conversations/{id}/messages` debe dar 404 y `GET /conversations` debe devolver `[]`. Inspección de `messaging/service.py::_get_conversation`. | 🟡 | El código lo cumple (`_get_conversation` solo acepta a la docente de la sala y al tutor). **No hay prueba que lo fije.** |
| ETL-13 | Las fotos y los documentos solo se entregan con una URL firmada de corta vida, y la llave del objeto no lleva datos personales. | Prueba: `tests/test_media_documents.py` y `test_upload_rules` (permiso de 300 s). Inspección de `core/storage.py` (descarga de 600 s) y de `media/service.py` (llave `media/AAAA/MM/<uuid>/original.<ext>`). | ✅ | |
| ETL-14 | Solo se aceptan imágenes JPEG, PNG y WebP (y PDF en documentos) de hasta 10 MB, y solo quien las subió las puede completar. | Prueba: `test_upload_rules`. | ✅ | |
| ETL-15 | Dar de baja a una persona le corta el acceso de inmediato. | Prueba: `tests/test_auth.py` (baja que corta el acceso). | ✅ | `revoke_all_sessions`. |
| ETL-16 | La bitácora de auditoría solo se agrega: la API no puede editar ni borrar filas, y solo la ve el administrador. | Pruebas: `test_the_log_cannot_be_edited_or_deleted`, `test_only_the_admin_can_read_the_log`. | ✅ | La inmutabilidad es de la API, no de la base. |
| ETL-17 | Las lecturas de datos de menores (fotos, documentos, expedientes, reportes) quedan registradas en la bitácora. | Demostración: leer una foto con un token de padre y buscar la entrada en `GET /audit-log`. | ❌ | `audit/README.md`: «Lo que todavía no se registra: lectura de fotos y documentos, reportes y mensajes». |

### 1.4 Seguridad de la información

| ID | Requisito | Cómo se verifica | Estado | Evidencia o hueco |
|---|---|---|---|---|
| ETL-18 | Las contraseñas se guardan con un hash lento; tienen entre 9 y 128 caracteres y no pueden ser el correo. | Pruebas: `tests/test_auth.py` (reglas de contraseña) y `AuthValidatorTest` en la app. Inspección de `core/passwords.py`. | ✅ | Argon2id. |
| ETL-19 | Los tokens y los códigos de acceso no se guardan en claro. | Inspección: `refresh_token.token_hash` e `invitation.code_hash` (SHA-256). | ✅ | |
| ETL-20 | Tras 5 intentos fallidos por correo (o 30 por IP) en 15 minutos, el acceso se bloquea temporalmente y el bloqueo no revela qué correos existen. | Pruebas: `tests/test_security.py` (`test_five_failures_lock…`, `test_unknown_emails_are_locked_the_same_way`). | ✅ | |
| ETL-21 | Todas las respuestas llevan cabeceras de seguridad; HSTS en producción. | Pruebas: `test_security_headers_on_every_response`, `test_hsts_only_outside_development`. Comando contra el servidor desplegado: `curl -sI https://<dominio>/api/v1/health/live`. | 🟡 | Probado en código; ⚪ en el servidor real (no hay despliegue). |
| ETL-22 | El tráfico entre la app y la API va cifrado (HTTPS) en la versión entregada. | Inspección del APK de release: `API_BASE_URL` de release debe ser `https://` y válida. Demostración: instalar el APK en un teléfono sin `adb reverse`. | 🟡 | La dirección de release ya es `https://api.codeaapp.site/api/v1/` (túnel de Cloudflare, 19/oct). Falta levantarlo e instalar el APK en un teléfono sin `adb reverse`. |
| ETL-23 | Los secretos no están en el repositorio. | Comando: `git ls-files \| grep -E "\.env\|local.properties\|\.jks\|keystore"` solo debe listar `.env.example`. | 🟡 | Hoy solo aparece `.env.example`. Pero `.env.example` y `config.py` traen valores débiles por omisión (`POSTGRES_PASSWORD=123456`, `S3_SECRET_KEY`, `SEED_PASSWORD=codea1234`) que hay que cambiar. |
| ETL-24 | La sesión en el teléfono está cifrada con una llave que no sale del Keystore. | Pruebas: `SessionRepositoryTest` (llave perdida = sin sesión). Demostración (solo en dispositivo): los archivos de DataStore no contienen ningún JWT en claro. | ✅ | La prueba del cifrado real solo se hizo a mano (`app/src/test/README.md`). |
| ETL-25 | Existe un respaldo de la base y se probó restaurarlo. | Demostración: restaurar el respaldo en una base vacía y entrar con un usuario. | ❌ | «Respaldos: tarea abierta». |
| ETL-26 | El almacén y la base de producción cifran en reposo. | Revisar la configuración del proveedor elegido. | ⚪ | No hay proveedor de producción definido. |

### 1.5 Derechos de las personas y retención

| ID | Requisito | Cómo se verifica | Estado | Evidencia o hueco |
|---|---|---|---|---|
| ETL-27 | Una familia puede borrar una foto de su hijo desde la app, sin pedirle permiso a nadie. | Demostración: pantalla Fotos → «Borrar» → confirmar; la foto desaparece de la galería y la URL ya no entrega la imagen. Pruebas de la API: `tests/test_media_deletion.py` (`DELETE /media/{id}`). | 🟡 | Existe el botón y el endpoint, con 5 pruebas automáticas; **falta verlo en un teléfono** con una foto real (cambio del 19/oct). |
| ETL-28 | El borrado es inmediato: el archivo se elimina del almacén en el momento y queda constancia en la bitácora. | Pruebas de la API: `tests/test_media_deletion.py` (borra del bucket, sale de todas las galerías, queda `MEDIA_DELETED_BY_FAMILY`, si el bucket falla no se marca nada). | 🟡 | Cumple en la API. **Falta** probarlo en un teléfono con una foto real, y decidir cómo se avisa a las otras familias que salían en una foto de grupo (hoy no se les avisa). |
| ETL-29 | Una persona puede corregir sus datos. | Demostración: editar nombre, teléfono o alergias desde la app. | ❌ | No hay edición de datos de cuenta ni de infante (P2 en `docs/plan/01-app-pantallas.md`). |
| ETL-30 | Una persona puede obtener una copia de sus datos. | Demostración: exportación a PDF o JSON. | ❌ | No existe. |
| ETL-31 | Hay una política de retención escrita y los datos se borran al vencer su plazo. | Inspección: documento aprobado + tarea de limpieza en el servidor (por ejemplo, un comando que borre `refresh_token` e `invitation` vencidos). | ❌ | La propuesta está en [`clasificacion-datos.md`](clasificacion-datos.md) y no está aprobada ni implementada. |
| ETL-32 | Dar de baja a un niño o a una persona no destruye su historial sin decisión humana. | Prueba: `deactivate`/`reactivate` conservan las filas (`test_enroll_child_*`, `test_teacher_rules_and_changes`). | ✅ | La baja solo cambia `status`. |
| ETL-33 | Existe un procedimiento escrito para avisar a las familias si hay una filtración de datos. | Inspección: documento con responsables, plazos y mensaje tipo. | ❌ | No existe. **[Completar por el equipo.]** |

### 1.6 Uso responsable de tecnología y de terceros

| ID | Requisito | Cómo se verifica | Estado | Evidencia o hueco |
|---|---|---|---|---|
| ETL-34 | Ningún dato de un menor se envía a un servicio de inteligencia artificial. | Inspección de dependencias y de llamadas de red (`CodeaApi.kt`, `AuthApi.kt`): solo apuntan a la API propia. | 🟡 | No hay IA. El **dictado** usa el reconocedor de voz del sistema (Google, según la decisión #10, sin confirmar): la voz de la docente puede salir del teléfono. La «consulta con IA» (CRF-04) no existe. |
| ETL-35 | Las licencias de los componentes de terceros permiten su uso y quedan a la vista. | Inspección: `docs/licencias/` y las dependencias. | 🟡 | Solo está la licencia de la tipografía Poppins (SIL OFL). No hay inventario de licencias de las librerías. |
| ETL-36 | Los datos de demostración son ficticios. | Inspección de `guarderia-api/app/seed.py`. | ✅ | Nombres inventados y correos `@oruga.test`. La base de Aiven con los 15 usuarios de prueba **no debe** recibir datos de menores reales. |
| ETL-37 | Las decisiones que se salen del marco técnico del curso están registradas y justificadas. | Inspección de [`docs/DECISIONES-PENDIENTES.md`](../DECISIONES-PENDIENTES.md). | 🟡 | Están registradas; ninguna tiene aprobación formal. |
| ETL-38 | La información de la app es accesible (los iconos con función tienen descripción para lector de pantalla). | Demostración con TalkBack. | ⚪ | No se evaluó. Hay `contentDescription` en varios íconos, pero no se probó con TalkBack. |
| ETL-39 | Los datos de menores que la app guarda en el teléfono (modo sin conexión) se limitan a lo necesario, se borran cuando la sesión termina y, mientras haya sesión, no quedan expuestos. | Inspección de `app/src/main/java/mx/tec/codea/data/offline/AppDatabase.kt` (tablas `pending_attendance`, `cached_room`, `cached_child`) y de `di/AppContainer.kt` (borrado de la caché cuando la sesión queda sin usuario). Demostración: abrir Mi sala sin conexión, cerrar sesión y comprobar con `adb shell run-as` en una compilación debug que `cached_child` queda vacía. | 🟡 | La base `codea.db` **no está cifrada** (los tokens sí); `cached_child` guarda **nombres** de niños. La caché se borra sola al terminar la sesión: **verificado a mano** leyendo la base del teléfono (0 filas de `cached_child` tras cerrar sesión, `docs/BITACORA.md`, «Cierre del 18 de octubre»), sin prueba automática. La cola `pending_attendance` solo lleva id del niño, estado, fecha y su dueña (`userId`, Room v2), solo se envía con la sesión de su dueña y no se borra al cerrar sesión. **Riesgo MEDIO con sesión abierta y BAJO sin ella.** Cifrar la base con el Keystore está pendiente. Ver [`clasificacion-datos.md`](clasificacion-datos.md), sección 4.2.1. |

### Resumen por estado

| Estado | Cantidad | IDs |
|---|---|---|
| ✅ Cumple | 19 | ETL-04, 05, 06, 07, 08, 09, 10, 11, 13, 14, 15, 16, 18, 19, 20, 24, 27, 32, 36 |
| 🟡 En parte | 9 | ETL-03, 12, 21, 23, 28, 34, 35, 37, 39 |
| ❌ No cumple | 9 | ETL-01, 02, 17, 22, 25, 29, 30, 31, 33 |
| ⚪ No evaluable | 2 | ETL-26 (sin proveedor de producción) y ETL-38 (sin prueba con TalkBack) |

Total: 39 requisitos. Es la lectura del código al 18 de octubre (commit `22c0008`); los resultados de ejecución van en la [matriz de trazabilidad](matriz-trazabilidad.md), sección 3.

---

## 2. Dilemas éticos

Cada dilema dice qué está en juego, las opciones, la **postura propuesta** y cómo se refleja (o no) en el sistema hoy. Las posturas son propuestas del documento; el equipo debe revisarlas y firmarlas.

### Dilema 1: fotos de menores y derecho al borrado

**La situación.** Las docentes toman fotos durante la rutina y las suben; las familias de los niños etiquetados las ven. Una madre pide que se borre una foto de su hijo. El centro, por su parte, puede querer conservarla (porque es parte de un reporte de incidente, porque aparecen otros niños cuyas familias sí quieren la foto, o porque ya es parte del historial).

**Qué está en juego.**

| A favor de borrar | A favor de conservar |
|---|---|
| La imagen de un menor pertenece a su familia; retirar el consentimiento debe ser fácil y efectivo. | Un reporte de incidente o de salud puede ser evidencia para proteger al niño, al personal y al centro. |
| Una foto que ya no se quiere es un riesgo de filtración inútil. | Otros niños de la misma foto tienen familias con derecho a conservarla. |

**Opciones.**

1. Rechazar o ignorar la solicitud.
2. Borrar la foto en todos los casos, sin excepción.
3. Atender siempre la solicitud, pero distinguiendo el **archivo** (imagen) del **registro** (el reporte de texto), y tratando distinto la foto de un solo niño y la foto de un grupo.

**Postura propuesta: opción 3.**

- La imagen de un menor se elimina cuando su familia lo pide. **La preferencia de la familia pesa más que la comodidad del centro.**
- Si la foto es de **un solo niño**: se borra el archivo del almacén y se quita del acceso de todos. El reporte escrito puede quedar, sin la imagen, si es necesario para la seguridad del niño o del personal.
- Si es una **foto de grupo**: se retira la etiqueta del niño que la pidió (para que su familia deje de verla) y se avisa al administrador; para que desaparezca para todos hace falta que él decida o que lo pidan todas las familias etiquetadas. Esa decisión debe quedar escrita.
- El plazo máximo de respuesta lo fija el centro **[completar por el equipo]**, y toda solicitud, aprobación o rechazo queda en la bitácora.
- Un rechazo debe explicarse a la familia por escrito.

**Actualización del 19/oct (decisión del equipo, que reemplaza la postura de arriba):** la familia borra **directamente** (`DELETE /media/{id}`), sin aprobación de la docente ni del administrador, y basta con que salga un hijo suyo. **Si en la foto salen niños de otras familias, también se borra para ellas** y no se les avisa. Se eligió la opción «la familia que aparece decide» sobre la «foto de grupo se atiende con el administrador»: es más sencilla y respeta de inmediato a quien pide retirar la imagen de su hijo, a costa de que otra familia pueda perder una foto que quería conservar. Se quitaron la solicitud, la revisión del administrador y el plazo de respuesta (ya no hay nada que esperar). Lo que sigue describe el sistema anterior al cambio.

**Qué hace el sistema hoy (commit `8af6958`).** La familia pide el borrado desde la pantalla Fotos. El administrador ve la solicitud en **Centro → «Fotos que piden borrar»** (foto, niños etiquetados, quién la pidió, fecha y motivo) y elige **«Borrar la foto»** o **«Conservar»**. Al borrar, la app advierte: «Se borra del servidor y desaparece de las galerías de todas las familias. No se puede deshacer». En la API, aprobar elimina el objeto del almacén, marca la foto `DELETED`, deja los reportes con su texto pero sin la referencia a la imagen, resuelve las demás solicitudes de la misma foto y escribe `MEDIA_DELETION_APPROVED` en la bitácora; rechazar deja la foto y guarda una nota opcional (no se verificó que la pantalla permita escribirla ni que la familia la vea).

Eso cumple la postura para una foto de **un solo niño**. **Sigue sin resolver:** (a) la foto de **grupo**: aprobar la borra para todas las familias etiquetadas y no hay forma de quitar solo la etiqueta de un niño (`media_child`); (b) el **plazo máximo** de respuesta, que no está definido ni vigilado (no hay recordatorio ni fecha límite); (c) que se haya probado con una foto real en un teléfono; (d) los **documentos** no entran en esta revisión.

### Dilema 2: ubicación de la docente en el checador

**La situación.** Para evitar que alguien registre una entrada desde su casa, el centro quiere verificar que la docente esté en la estancia. Eso significa tomar la ubicación de una persona trabajadora.

**Qué está en juego.**

| A favor de verificar | En contra |
|---|---|
| Responsabilidad con las familias: saber que quien dice estar, está. Cumple el requisito de cliente CRF-05. | La ubicación es un dato sensible; puede usarse para vigilar a la persona más allá de lo necesario. Existe una relación de poder entre el centro y su personal. |

**Opciones.**

1. No tomar ubicación (el registro se confía a la docente).
2. Tomar la ubicación de forma continua.
3. Tomar **un solo punto, en el momento de checar, comparado en el servidor, y guardar lo mínimo**.

**Postura adoptada (18/oct, equipo): no guardar ubicación.** El equipo eligió la salida más simple: el checador deja de usar GPS y mapa, registra la hora de entrada y de salida, y cierra solo a las 9 pm lo que quedó abierto. Con eso:

- **No hay dato de ubicación** que proteger: no se pide el permiso, no se guarda nada y la migración `e74f0a5b3d43` borra lo que ya había (pendiente de aplicar en Aiven).
- **Se pierde la verificación del lugar:** el registro prueba la hora, no que la docente estuviera en la estancia. Si más adelante hace falta, habría que volver a plantear el dilema con el aviso de privacidad por delante.
- **Aviso a coordinación:** la app ya no lo ofrece (se quitó el botón el 18/oct). El endpoint y la tabla `coordination_notice` siguen en el backend, sin pantalla que los use.

### Dilema 3: acceso del administrador a los chats

**La situación.** El chat une a las familias con las docentes de la sala. Si hay una queja de maltrato, de acoso o de un mensaje inadecuado, el administrador querría leer la conversación. Pero también es una comunicación privada entre una familia y la docente.

**Qué está en juego.**

| A favor de que el administrador pueda leer | En contra |
|---|---|
| Proteger a un menor o investigar una queja grave. El centro responde por lo que pasa en su operación. | La confianza de las familias y de las docentes en que su conversación no es vigilada de forma rutinaria. Un acceso sin control se presta a abuso. |

**Opciones.**

1. El administrador lee todo libremente.
2. Nadie ajeno a la conversación puede leerla, nunca.
3. Acceso excepcional, con motivo, aprobado por dos personas, registrado y notificado.

**Postura propuesta: opción 3 como política, y opción 2 como estado técnico actual.**

- **Hoy el sistema lo impide por diseño:** `GET /conversations` devuelve lista vacía al administrador y leer los mensajes de una conversación le responde 404. El administrador de la app no es participante. (Falta una prueba que lo fije: ETL-12.)
- **Pero esa barrera es de la API, no criptográfica.** Quien tenga acceso a la base (Aiven o el servidor) puede leer los mensajes. Hay que decirlo a las familias (está en el aviso de privacidad) y no vender el chat como «privado» en un sentido técnico.
- **Si un día se necesita acceso**, debe hacerse por un procedimiento escrito, no ad hoc: queja formal por escrito → autorización de dos personas distintas (por ejemplo, dirección y un representante legal) → acceso limitado a la conversación y al periodo en cuestión → registro en la bitácora (quién, cuándo, motivo) → aviso a las personas involucradas, salvo que la ley o la seguridad del menor lo impidan. **Ese procedimiento no existe todavía** **[completar por el equipo]**, y tampoco hay función en el sistema ni entrada de bitácora para registrar tal acceso.
- No se agrega al administrador como lector permanente de los chats.

### Dilema 4: fotos y reportes de grupo

**La situación.** Una foto de varios niños se etiqueta con todos ellos y **la ven las familias de todos**. Un reporte que involucra a varios niños tiene **una sola descripción** que ven todas esas familias. Si la docente escribe «Mateo mordió a Sofía», las dos familias ven los dos nombres.

**Qué está en juego.** Informar a cada familia de lo que le pasó a su hijo frente a proteger la identidad y la conducta de los demás niños (y de sus familias) y evitar estigmatizar a un menor.

**Opciones.**

1. Un solo texto para todos (como hoy).
2. Un texto distinto por niño.
3. Un texto común sin nombres de otros niños, más el aviso a cada familia con lo que le concierne.

**Postura propuesta: opción 3.**

- Los reportes por incidente deben redactarse sin nombrar a otros niños («otro niño de la sala»), salvo que sea imprescindible y el centro lo autorice.
- Las fotos de grupo solo se etiquetan con los niños que aparecen, y el centro debe informar a las familias de que sus hijos pueden aparecer en fotos que verán otras familias, al inscribirse **[consentimiento: ver ETL-02]**.
- ~~El administrador debería poder revisar las fotos de grupo que reciben solicitud de borrado~~ (Dilema 1): desde el 19/oct la familia borra directo y no hay revisión.

**Qué hace el sistema hoy.** No hay ninguna ayuda para esto: la descripción del reporte es un campo libre único (`report.description`) y la pantalla «¿A quién le pasó?» solo dice «Familias de X · coordinación de la estancia. El grupo no lo ve», sin advertir que cada familia leerá el mismo texto. Debe agregarse un aviso en la pantalla de reporte y una guía de redacción para las docentes.

---

## 3. Lo que el equipo debe decidir o completar

| Pendiente | Dónde se usa |
|---|---|
| Responsable legal, domicilio y canal de privacidad | Aviso de privacidad |
| Plazos de retención | Clasificación de datos, aviso |
| Formato de consentimiento para datos de salud y de imagen, y dónde se guarda | ETL-02 |
| ~~Plazo y procedimiento para atender solicitudes de borrado~~ (ya no aplica: el borrado es directo, 19/oct). Falta decidir si se avisa a las otras familias de una foto de grupo borrada | Dilema 1 |
| Si se dejan de guardar las coordenadas de la checada | Dilema 2 |
| Procedimiento de acceso excepcional a conversaciones | Dilema 3 |
| Procedimiento ante una filtración de datos | ETL-33 |
| Asesoría legal sobre la ley vigente y el consentimiento de menores | Todo el documento |
