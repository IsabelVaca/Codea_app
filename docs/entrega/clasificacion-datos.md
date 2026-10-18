# Clasificación de datos por campo

Parte 6 de la rúbrica (ética y legal), punto 3. Estado a **2026-10-18**, rama `integracion`, commit `22c0008`. Incluye la revisión de solicitudes de borrado de fotos (migración `c52d8e3f0b21`: columnas `decided_by`, `decided_at` y `decision_note` en `media_deletion_request` y estado `DELETED` en `media`) y la salida de la docente (migración `d63e9f4a1c32`: `checked_out_at`, `out_latitude`, `out_longitude` y `out_distance_m` en `staff_check_in`) y la base **local** del teléfono para la asistencia sin conexión (sección 4.2.1).

Cada campo de cada tabla sale de los `models.py` de `guarderia-api/app/modules/*` (las 32 tablas de las 4 migraciones de `guarderia-api/migrations/versions/`). Lo que vive fuera de Postgres (bucket de archivos, teléfono, variables de entorno, registros del servidor) está en la sección 4. Nada de lo que dice este documento se probó en un servidor de producción: no hay despliegue (ver [`plan-implantacion.md`](plan-implantacion.md)).

## 1. Cómo leer las tablas

### Niveles de sensibilidad

| Nivel | Qué es | Ejemplos en este proyecto |
|---|---|---|
| **Bajo** | No identifica a nadie por sí solo. | Estados, categorías, nombre de una sala, plantillas de rutina. |
| **Medio** | Dato personal ordinario de una persona adulta o dato laboral. | Nombre, correo y teléfono de una docente o de un tutor; hora de checada; dirección IP. |
| **Alto** | Dato de un **menor**, dato de **salud**, **ubicación** precisa de una persona, o archivo (foto o documento). La ley mexicana trata los datos de salud como sensibles. | Nombre, fecha de nacimiento y alergias del infante; reportes; fotos; documentos; mensajes sobre el menor; coordenadas del checador. |
| **Crítico** | Si se filtra, permite hacerse pasar por alguien o abrir todo lo demás. | Hash de contraseña, hash de tokens y de códigos, `JWT_SECRET`, llaves del bucket, contraseña de la base. |

### Columnas

- **Dónde vive:** `PG` = Postgres (en las pruebas manuales del equipo, la base alojada en Aiven; en desarrollo, el contenedor `db` de `docker-compose.yml`) · `S3` = bucket compatible con S3 (SeaweedFS en desarrollo) · `Tel` = teléfono · `Env` = variables de entorno del servidor.
- **Cómo se protege:** solo lo que el código hace hoy. Las protecciones que valen para todas las tablas están en la sección 2 y no se repiten.
- **Se conserva hoy:** lo que el código realmente hace con la fila. **«Sin borrado»** significa que ningún proceso la elimina; sigue mientras exista la base.
- **Retención propuesta:** es una **propuesta mía, sin aprobar** por el equipo ni por quien responda legalmente; no está implementada. Cada una se marca con `[decidir]`. El proyecto no tiene hoy ninguna política de retención escrita (se buscó en `docs/` y en el código).

### Columnas que tienen todas las tablas

`id` (entero o UUID), `created_at` y `updated_at` (`TimestampMixin` e `IdMixin` de `app/core/db.py`) son nivel **Bajo** por sí mismas, pero **heredan el nivel de la fila**: la fecha de creación de una foto de un menor ya dice cuándo estuvo el menor en el centro. No se repiten abajo. Las tablas `admin`, `teacher`, `parent`, `report_child`, `media_child` y `conversation_read` tienen `created_at` y `updated_at` pero no `id` propio (su llave es compuesta o es `user_id`).

## 2. Protecciones que valen para todo el sistema

| Protección | Qué hace | Evidencia | Límite |
|---|---|---|---|
| Autenticación | Cada petición lleva un JWT de 5 minutos; el JWT solo trae el id de la persona, nunca rol ni datos. | `app/core/tokens.py`, `app/core/security.py` | — |
| Control de acceso por rol | `require_role` por endpoint; el rol sale de Postgres en cada petición. | `app/core/security.py` | — |
| Alcance por registro | El padre solo ve a sus hijos; la docente, a los de su sala. Fuera de alcance responde **404**, no 403. | `can_see_child`, `get_child_in_scope`, `ensure_classroom_access` | El administrador ve a todos los niños (`can_see_child` devuelve verdadero para `admin`). |
| Baja inmediata | Dar de baja sube `token_version` y revoca los refresh tokens: el acceso se corta en la siguiente petición. | `revoke_all_sessions` | — |
| Límite de intentos | 5 fallos por correo o 30 por IP en 15 minutos bloquean con `429`. | `auth/throttle.py`, `tests/test_security.py` | La IP es la del socket: detrás de un proxy hay que configurar la IP real (lo dice el propio módulo). |
| Cabeceras | `X-Content-Type-Options`, `X-Frame-Options: DENY`, `Referrer-Policy`, `Permissions-Policy`, CSP y `Cache-Control: no-store` en `/api/`; HSTS cuando `ENV` no es `dev`. | `app/main.py` | Solo sirve si hay HTTPS delante; **no hay despliegue con HTTPS todavía**. |
| CORS cerrado | Sin orígenes por omisión. | `app/main.py`, `config.py` | — |
| Bitácora de auditoría | Registra inicios de sesión, altas, bajas y cambios del administrador; solo se agrega (la API no edita ni borra). | `app/modules/audit` | Registra también los cambios al centro (`CENTER_UPDATED`) y las decisiones sobre fotos. **No** registra lecturas de fotos, documentos, reportes ni mensajes. Quien tenga acceso directo a la base sí podría modificarla. |
| Archivos sin pasar por la API | Subida con URL firmada de 5 minutos (máx. 10 MB; JPEG, PNG, WebP y, solo en documentos, PDF); lectura con URL firmada de 10 minutos; la llave del objeto no lleva datos personales. | `app/core/storage.py`, `media/service.py` | Quien tenga la URL firmada puede ver el archivo durante esos 10 minutos. |
| Contraseñas | Argon2id; entre 9 y 128 caracteres; no puede ser el correo. | `app/core/passwords.py`, `app/core/rules.py` | — |
| Base local del modo sin conexión | Almacenamiento privado de la app, `allowBackup="false"`; la caché con nombres se borra al terminar la sesión | `di/AppContainer.kt`, `data/offline/` | **No está cifrada**; el borrado se verificó a mano y no tiene prueba automática (ver 4.2.1) |
| Tokens en el teléfono | Access y refresh cifrados con AES-GCM; la llave vive en el Android Keystore. | `app/.../data/local/TokenCipher.kt` | El nombre, el id y los roles se guardan sin cifrar. |
| Tráfico de la app | Release: solo HTTPS. Debug: texto plano únicamente hacia `10.0.2.2` y `localhost`. | `network_security_config.xml` (main y debug) | La dirección de release es hoy una dirección inválida a propósito (`api.codea.invalid`). |
| Cifrado en reposo (Postgres y bucket) | **No lo configura el proyecto.** Depende del proveedor. | — | **Por confirmar** con la cuenta de Aiven y con el almacén que se elija para producción. |
| Respaldos | **No hay** estrategia de respaldo (`guarderia-api/PENDIENTES.md`: «Respaldos de la base — tarea abierta»). | — | Sin respaldo no hay forma de recuperar datos; con respaldo hay que decidir cuánto tiempo se guardan (afecta el derecho de cancelación). |

## 3. Tablas de Postgres, campo por campo

### 3.1 Cuentas y sesión (`auth`)

#### `app_user`: la cuenta de toda persona (administrador, docente o tutor)

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `name`, `surname` | Medio | — | Sin borrado (la baja solo cambia `status`) | Mientras exista la relación con el centro + `[decidir]` |
| `phone` | Medio | — (texto claro en la base) | Sin borrado | Igual |
| `email` | Medio | Se guarda en minúsculas; único | Sin borrado | Igual |
| `password_hash` | **Crítico** | Argon2id; nunca sale en una respuesta | Sin borrado; se reemplaza al cambiar contraseña | Igual |
| `password_changed_at`, `last_login_at` | Medio | — | Sin borrado | Igual |
| `token_version` | Bajo | Subir su valor invalida los access tokens vivos | Sin borrado | — |
| `status` | Bajo | `ACTIVE` o `INACTIVE` | Sin borrado | — |

#### `invitation`: código de acceso de una sola vez

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `user_id` | Medio | FK a `app_user` | Sin borrado (**no hay tarea que limpie los vencidos**, está pendiente en `auth/README.md`) | Borrar 30 días después de vencer o usarse `[decidir]` |
| `email` | Medio | — | Igual | Igual |
| `role` | Bajo | — | Igual | Igual |
| `code_hash` | **Crítico** | Solo se guarda el hash SHA-256 del código; el código se muestra una vez | Igual | Igual |
| `purpose` | Bajo | `INVITATION` o `PASSWORD_RESET` | Igual | Igual |
| `expires_at` | Bajo | Vigencia de 7 días (`invitation_days`) | Igual | Igual |
| `accepted_at` | Bajo | Marca «ya se usó» | Igual | Igual |

#### `refresh_token`

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `user_id` | Medio | — | Sin borrado (**ni siquiera los vencidos o revocados**, pendiente en `auth/README.md`) | Borrar 30 días después de vencer o revocarse `[decidir]` |
| `token_hash` | **Crítico** | Solo el SHA-256; el token real nunca queda en la base | Igual | Igual |
| `family_id` | Bajo | Agrupa los tokens de un mismo inicio de sesión para detectar reuso | Igual | Igual |
| `expires_at`, `revoked_at`, `replaced_by_id`, `last_used_at` | Bajo | Rotación en cada uso; un token ya rotado que reaparece revoca toda la familia | Igual | Igual |
| `user_agent` | Medio | Se corta a 300 caracteres; identifica el dispositivo y la app | Igual | Igual |

#### `auth_attempt`: intentos fallidos de entrar o de activar

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `kind` | Bajo | `LOGIN` o `ACTIVATE` | Se borran las filas de más de **1 día** cada vez que se registra otro fallo; las de un correo se borran cuando ese correo entra bien (`auth/throttle.py`) | Ya cumplido: 24 horas |
| `email` | Medio | Se guarda aunque la cuenta no exista, para que el bloqueo no revele qué correos están registrados | Igual | Igual |
| `ip` | Medio | — | Igual | Igual |
| `attempted_at` | Bajo | — | Igual | Igual |

Hay un matiz: la limpieza ocurre cuando se registra un nuevo fallo. Si nadie vuelve a fallar, las últimas filas se quedan hasta el siguiente fallo.

### 3.2 Bitácora (`audit`)

#### `audit_log`: quién hizo qué (RF-14)

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `actor_id` | Medio | FK a `app_user`, indexado | Sin borrado; la API no puede editar ni borrar (405) | Entre 1 y 2 años `[decidir]`: el tiempo que el equipo necesite para investigar un incidente |
| `actor_email` | Medio | Queda el correo escrito en un login fallido aunque no exista la cuenta | Igual | Igual |
| `action` | Bajo | Nombres fijos (`LOGIN_OK`, `CHILD_ENROLLED`…) | Igual | Igual |
| `entity_type`, `entity_id` | Medio | Referencia a una persona o a un niño (el id como texto) | Igual | Igual |
| `detail` | Medio | JSON con rol, sala, turno o hijos; **nunca** contraseñas, códigos, tokens ni hashes (hay una prueba que lo verifica) | Igual | Igual |
| `ip` | Medio | — | Igual | Igual |

Solo el administrador la lee (`GET /audit-log`). Una bitácora que se borra pierde su valor: por eso su retención es distinta de la de los datos que describe.

### 3.3 Centro, salas, personal (`admins`, `classrooms`, `teachers`)

#### `admin`

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `user_id` | Medio | FK a `app_user` | Sin borrado | Como `app_user` |
| `daycare_center_id` | Bajo | — | Sin borrado | — |

#### `daycare_center`

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `name` | Bajo | — | Sin borrado | — |
| `latitude`, `longitude` | Medio | Solo el administrador las edita (`PATCH /center`; en la app, con el botón «Usar mi ubicación actual», que guarda la posición del teléfono del administrador); cada cambio queda en la bitácora (`CENTER_UPDATED`). Las docentes las reciben en `GET /me` para el checador | Sin borrado | — |
| `check_in_radius_m`, `timezone` | Bajo | Validadas (`RADIUS_INVALID`, `TIMEZONE_INVALID`) | Sin borrado | — |

#### `classroom`

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `daycare_center_id`, `name`, `school_grade`, `capacity`, `routine_template_id` | Bajo | — | Sin borrado | — |
| `deleted_at` | Bajo | Borrado lógico: «eliminar» una sala solo pone la fecha | La fila se queda | — |

#### `shift`

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `daycare_center_id`, `name`, `start_time`, `end_time` | Bajo | — | Sin borrado | — |

#### `teacher`

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `user_id` | Medio | — | Sin borrado | Como `app_user` |
| `classroom_id`, `is_primary`, `shift_id` | Medio | Dato laboral; solo el administrador lo cambia | Sin borrado | Igual |

### 3.4 Infantes y familias (`children`, `parents`)

#### `child`: el expediente del menor

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `name`, `surname` | **Alto** | Alcance por registro: solo sus tutores, su docente y el administrador | Sin borrado. La baja solo pone `status = INACTIVE` y se puede reactivar | Mientras esté inscrito + un periodo `[decidir]`; después, borrado o anonimización |
| `birth_date` | **Alto** | Igual; la API calcula la edad. Desde el commit `38bc8fa` el formulario de la app pide la **fecha de nacimiento real** (dd/mm/aaaa) y la valida. **Los niños inscritos antes de ese cambio conservan una fecha inventada** a partir de su edad (lo dice `docs/BITACORA.md`) y hay que corregirla a mano si se quiere el dato real | Igual | Igual |
| `allergies` | **Alto** (salud) | Igual | Igual | Igual |
| `classroom_id` | Medio | — | Igual | Igual |
| `status` | Medio | — | Igual | Igual |

#### `parent`

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `user_id` | Medio | FK a `app_user` | Sin borrado | Como `app_user` |

#### `parent_child_rel`: quién es tutor de quién

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `parent_id`, `child_id` | **Alto** | Es la llave del alcance: decide qué ve cada familia; única por pareja | Sin borrado | Como `child` |
| `relationship` | **Alto** | `MOTHER`, `FATHER`, `LEGAL_GUARDIAN`, `OTHER` | Igual | Igual |
| `is_primary` | Medio | Marca al tutor registrado al inscribir | Igual | Igual |

### 3.5 Checador de la docente (`staff_attendance`)

#### `staff_check_in`: la entrada y la salida de una docente (una fila por día)

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `teacher_id`, `date`, `checked_in_at` | Medio | Única por docente y día; la hora la pone el reloj del servidor, nunca el teléfono | Sin borrado | Mientras dure la relación laboral + `[decidir]` (dato de asistencia laboral) |
| `latitude`, `longitude` | **Alto** (ubicación) | El servidor calcula la distancia y **rechaza** la checada fuera del radio (`OUT_OF_RANGE`, que no se guarda). Solo se guarda **un punto**, el del momento de registrar la entrada: no hay rastreo | **Sin borrado** | Considerar guardar solo `distance_m` y no las coordenadas `[decidir]` |
| `checked_out_at` | Medio | La hora de la salida la pone el reloj del servidor; vacía hasta que la docente registra su salida (`POST /staff-attendance/check-out`) | Sin borrado | Como la fila |
| `out_latitude`, `out_longitude` | **Alto** (ubicación) | Un segundo punto, el del momento de la salida, con la misma regla de radio que la entrada. Vacíos hasta que hay salida | **Sin borrado** | Igual que la entrada `[decidir]` |
| `out_distance_m` | Alto | Calculada en el servidor | Sin borrado | Igual |
| `distance_m` | Alto | Calculada en el servidor | Sin borrado | Igual |
| `status`, `minutes_late` | Medio | El servidor calcula el retardo; no acepta el que mande el teléfono | Sin borrado | Como la fila |
| `late_reason` | Medio | Texto libre opcional de la docente | Sin borrado | Igual |

> **Texto de la app corregido (commit `f814763`).** El permiso de ubicación del checador decía «no se guarda»; ahora dice que se compara con la ubicación de la estancia y que, al registrar la entrada o la salida, «queda guardada junto con la hora», y que no se sigue a la persona. Coincide con lo que hace el servidor.

#### `coordination_notice`: aviso a coordinación

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `teacher_id`, `date`, `reason` | Medio | — | Sin borrado. **No hay endpoint de lectura**: nadie puede verla desde la app | Mientras dure la relación laboral `[decidir]` |
| `message` | Medio | Texto libre opcional | Sin borrado | Igual |

Además, al crear el aviso el servidor escribe en su registro (`log.warning`) el nombre completo de la docente y el motivo. Ese registro es un lugar más donde vive un dato personal (ver sección 4.4).

### 3.6 Asistencia y rutina (`attendance`, `routines`)

#### `attendance`: asistencia del infante (única por niño y día)

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `child_id`, `date`, `status` | **Alto** | Alcance por registro | Sin borrado | Como `child` |
| `marked_by` | Medio | Deja quién la marcó (administrador o docente) | Sin borrado | Igual |

#### `routine_template` y `routine_template_moment`

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `name`, `is_default` (plantilla) | Bajo | Solo el administrador las edita | Sin borrado | — |
| `template_id`, `number`, `name`, `category` (momento de plantilla) | Bajo | Al editar una plantilla se borran y recrean sus momentos | Sin borrado | — |

#### `routine_moment`: la rutina real de una sala en un día

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `classroom_id`, `template_id`, `date`, `number`, `name`, `category`, `status` | Bajo | Un solo momento activo por sala y día (índice único parcial) | Sin borrado | `[decidir]` junto con los reportes |
| `started_at`, `confirmed_at` | Bajo | — | Sin borrado | Igual |
| `confirmed_by` | Medio | Deja qué docente confirmó | Sin borrado | Igual |
| `description` | Bajo | Texto libre | Sin borrado | Igual |

### 3.7 Reportes y resumen del día (`reports`)

#### `report`

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `type` | **Alto** | `INCIDENT`, `ACHIEVEMENT`, `HEALTH`, `BEHAVIOR`: dice si hubo incidente, salud o conducta del menor | Sin borrado. **No se puede editar ni anular un reporte** (`reports/README.md`) | Como `child` `[decidir]` |
| `description` | **Alto** | Texto libre sobre menores, mínimo 10 caracteres (`DESCRIPTION_TOO_SHORT`). **Es una sola descripción para todos los niños del reporte** | Sin borrado | Igual |
| `method` | Bajo | `WRITTEN`, `DICTATION` o `PHOTO` | Sin borrado | — |
| `routine_moment_id` | Bajo | — | Sin borrado | — |
| `media_id` | **Alto** | FK a la foto del reporte | Sin borrado | Como `media` |
| `created_by` | Medio | Quién lo creó | Sin borrado | Igual |

#### `report_child`

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `report_id`, `child_id` | **Alto** | Decide a qué familias les llega el reporte | Sin borrado | Como `report` |

#### `daily_summary`: el «Hoy» que ve el padre (único por niño y día)

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `child_id`, `date` | **Alto** | Alcance por registro | Sin borrado | Como `child` |
| `mood`, `meals`, `diaper_changes`, `nap_minutes` | **Alto** | Valores de lista cerrada, salvo los números | Sin borrado | Igual |
| `general_note` | **Alto** | Texto libre | Sin borrado | Igual |
| `recorded_by` | Medio | Quién lo registró | Sin borrado | Igual |

### 3.8 Fotos y archivos (`media`)

#### `media`: el registro de un archivo (el archivo mismo está en el bucket, sección 4.1)

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `kind` | Bajo | `PHOTO` o `DOCUMENT` | Sin borrado | Como el archivo |
| `storage_key` | Medio | Única. **No lleva datos personales** (`media/AAAA/MM/<uuid>/original.<ext>`); no es secreta, pero sin URL firmada no sirve | Sin borrado | Igual |
| `content_type`, `size_bytes` | Bajo | Solo JPEG, PNG, WebP y PDF (documentos); máx. 10 MB | Sin borrado | — |
| `status` | Bajo | `PENDING` hasta que el archivo llega al bucket; `READY` | Sin borrado. **Los `PENDING` que nunca se completan quedan** | Borrar los `PENDING` de más de 1 día `[decidir]` |
| `uploaded_by` | Medio | Solo quien subió el archivo puede completarlo | Sin borrado | Como el archivo |

#### `media_child`: a quién se etiqueta en una foto

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `media_id`, `child_id` | **Alto** | Decide qué familias ven la foto: **una foto con varios niños la ven las familias de todos ellos** | Sin borrado | Como la foto |

#### `media_deletion_request`: «Pedir que se borre»

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `media_id`, `requested_by` | **Alto** | Una solicitud por foto y persona; repetirla devuelve la misma | Sin borrado | Mientras se atienda + la constancia `[decidir]` |
| `reason` | Medio | Texto libre opcional | Sin borrado | Igual |
| `status` | Bajo | `PENDING`, `APPROVED`, `REJECTED`. Solo el administrador las resuelve (`GET /media/deletion-requests`, `…/approve`, `…/reject`); resolver una pasa el estado de la foto a `DELETED` si se aprueba | Sin borrado de la solicitud (queda como constancia) | — |
| `decided_by` | Medio | FK al administrador que decidió | Sin borrado | Igual |
| `decided_at` | Bajo | — | Sin borrado | Igual |
| `decision_note` | Medio | Texto libre opcional de hasta 500 caracteres | Sin borrado | Igual

### 3.9 Documentos y personas autorizadas (`documents`)

#### `child_document`: documentos que el centro pide a la familia

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `child_id` | **Alto** | Alcance por registro (administrador y tutores del niño) | Sin borrado | Como `child` |
| `title` | **Alto** | En el seed aparecen «Certificado de alergias» e «Informe de discapacidad»: el título ya revela salud | Sin borrado | Igual |
| `due_date` | Bajo | — | Sin borrado | — |
| `media_id` | **Alto** | FK al archivo; si no hay archivo el documento está `MISSING` (el estado no se guarda, se calcula) | Sin borrado | Como el archivo |
| `uploaded_at` | Bajo | — | Sin borrado | — |

#### `authorized_pickup`: personas autorizadas para recoger

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `child_id` | **Alto** | Alcance por registro; la docente también puede leer la lista | **Se borra de verdad** cuando el tutor o el administrador la quita (`db.delete`) | Ya cumplido en el borrado manual |
| `full_name`, `relationship`, `phone` | **Alto** | Es un **tercero que no se registró ni aceptó nada**: el tutor lo dio de alta. Teléfono en texto claro | Igual | Igual |
| `added_by` | Medio | — | Igual | Igual |

### 3.10 Comunicación (`messaging`)

#### `conversation`, `message` y `conversation_read`

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `conversation.child_id` | **Alto** | Una conversación por niño | Sin borrado | Como `child` |
| `message.text` | **Alto** | Solo los tutores del niño y las docentes de su sala la leen. **El administrador no puede leerla por la API**: `GET /conversations` le devuelve lista vacía y los mensajes responden 404 | Sin borrado | Como `child` `[decidir]` |
| `message.author_id`, `conversation_id` | Medio | — | Sin borrado | Igual |
| `message.client_id` | Bajo | Evita duplicados en reintentos | Sin borrado | — |
| `conversation_read.conversation_id`, `user_id`, `last_read_message_id` | Bajo | Sirve para contar no leídos | Sin borrado | — |

> Los mensajes **no están cifrados de extremo a extremo**: quien administre la base (Aiven o el servidor) puede leerlos. Que el administrador de la app no los vea es una regla de la API, no una barrera técnica.

#### `announcement`

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `text` | Bajo | Lo escribe el administrador; lo ven todo el centro o solo una sala | Sin borrado. **No se pueden borrar avisos** (`docs/plan/01-app-pantallas.md`) | `[decidir]` |
| `daycare_center_id`, `classroom_id`, `author_id` | Bajo | — | Sin borrado | — |

#### `parent_note`: nota del tutor para la asistente

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `child_id`, `date` | **Alto** | Alcance por registro | Sin borrado | Como `child` |
| `text` | **Alto** | Texto libre del tutor sobre su hijo | Sin borrado | Igual |
| `author_id`, `read_at` | Medio | — | Sin borrado | Igual |

## 4. Fuera de las tablas

### 4.1 Archivos en el bucket

| Objeto | Nivel | Dónde vive | Cómo se protege | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|---|
| Foto de una actividad (`kind = PHOTO`) | **Alto** (imagen de menores) | `S3`: SeaweedFS en desarrollo; **la elección para producción está sin decidir** (la decisión #9 sigue abierta) | La API solo entrega el archivo con una URL firmada de 10 minutos, y solo a quien tiene alcance sobre el niño etiquetado; llave sin datos personales | **Se borra del bucket cuando el administrador aprueba una solicitud de borrado** (`Storage.delete`); si no hay solicitud, **no se borra nunca**. Los `PENDING` abandonados quedan | Mientras el niño esté inscrito + `[decidir]` |
| Documento del niño (`kind = DOCUMENT`) | **Alto** (puede contener salud o discapacidad) | `S3` | Igual | **Sin borrado**: la revisión de solicitudes es solo de fotos. Reemplazar el documento deja el objeto anterior huérfano en el bucket (el código solo cambia `media_id`) | Igual |

Que el bucket **no** sea legible sin URL firmada depende de la configuración del almacén de producción. En desarrollo, `docker/seaweedfs-s3.sh` define una sola identidad con permisos de administración; no se verificó que anónimos no puedan leer. **Por confirmar.**

### 4.2 En el teléfono

| Dato | Nivel | Dónde | Cómo se protege | Se conserva |
|---|---|---|---|---|
| `access_token` y `refresh_token` | **Crítico** | DataStore (`SessionStore`) | AES-GCM de 256 bits con llave en el Android Keystore; si la llave se pierde, la sesión se descarta | Hasta cerrar sesión (se borran) o hasta que vence el refresh (7 días) |
| `user_id`, nombre y roles | Medio | DataStore, **sin cifrar** | Almacenamiento privado de la app; `allowBackup="false"` | Hasta cerrar sesión |
| Avisos ya leídos | Bajo | SharedPreferences `leidos` (`LeidosStore`) | Almacenamiento privado de la app | **No se borra al cerrar sesión** (solo se separa por usuario); cambiar de teléfono los vuelve «nuevos» |
| Ubicación del teléfono (checador) | **Alto** | Solo en memoria durante la checada; se manda una vez a `POST /staff-attendance/check-in` y otra a `POST /staff-attendance/check-out` | Permiso de ubicación del sistema; sin rastreo continuo | No se guarda en el teléfono |
| Fotos que ve el padre | **Alto** | Las descarga Coil desde las URLs firmadas | **Por confirmar:** el código no configura el `ImageLoader`, así que se usa el comportamiento por omisión de Coil, que puede dejar una caché en disco dentro del almacenamiento privado de la app | Por confirmar |
| Cámara y dictado | Alto | La app usa la cámara y el reconocedor de voz **del sistema** (`TakePicturePreview`, `RecognizerIntent`) | El audio lo procesa el reconocedor del teléfono (la decisión #10 dice que usa el de Google); la app solo recibe el texto | La app no guarda el audio |

### 4.2.1 Base local `codea.db` (modo sin conexión)

Desde el commit `99fac75` la app guarda una base **Room** en el teléfono (`codea.db`, **versión 2**; el esquema de cada versión se exporta a `app/schemas/mx.tec.codea.data.offline.AppDatabase/1.json` y `2.json`) para que la docente pueda marcar asistencia sin red. Son tablas **locales**: no existen en Postgres.

| Tabla local | Campo | Nivel | Qué guarda | Se conserva |
|---|---|---|---|---|
| `pending_attendance` | `id` | Bajo | Consecutivo de la fila | Hasta que la marca se envía (se borra al llegar al servidor) |
| | `childId` | Medio (**pseudonimizado**: solo el número del niño, sin nombre) | Qué niño | Igual |
| | `status` | Medio | `PRESENT` o `ABSENT` | Igual |
| | `date` | Bajo | Día de la marca (aaaa-mm-dd) | Igual |
| | `createdAt` | Bajo | Cuándo se hizo la marca | Igual |
| | `userId` | Medio | Identificador (UUID) de la docente que hizo la marca; la cola solo se envía, se cuenta y se pinta con la sesión de su dueña (versión 2, `MIGRATION_1_2`) | Igual |
| `cached_room` | `id` (siempre 1), `classroomId`, `roomName`, `shiftStartMinutes`, `shiftEndMinutes`, `savedAt` | Bajo | Nombre de la sala y horario del turno | Se reemplaza en cada carga correcta; **se borra cuando la sesión queda sin usuario** |
| | `teacherFullName`, `teacherGreeting` | Medio | Nombre de la docente | Igual |
| `cached_child` | `id`, `classroomId`, `conversationId` | Bajo | Identificadores | Igual |
| | `fullName` | **Alto** (menor identificado) | **Nombre completo del niño** | Igual |
| | `attendance` | **Alto** | Asistencia de hoy | Igual |
| | `hasUnreadChat`, `hasNoteToday` | Medio | Si hay chat sin leer o nota de la familia hoy | Igual |

**Cómo se protege hoy:** almacenamiento privado de la app (otras apps no lo leen sin permisos de superusuario) y `allowBackup="false"`. **La base no está cifrada** (los tokens sí). La caché con nombres de niños se borra sola cuando la sesión queda sin usuario, es decir, al cerrar sesión o cuando la sesión termina (`di/AppContainer.kt`, commit `1f2d2a0`). La cola `pending_attendance` **no** se borra al cerrar sesión, para no perder marcas sin enviar.

**Límites que hay que decir con honestidad:**

1. El borrado de la caché **no tiene prueba automática**. Sí se verificó a mano leyendo la base del teléfono (`run-as` y `sqlite3` desde la computadora): quedan 0 filas de `cached_child` al cerrar sesión (`docs/BITACORA.md`, «Cierre del 18 de octubre»).
2. Mientras haya sesión, quien tenga el teléfono desbloqueado, un teléfono con superusuario o una compilación `debug` con `adb run-as` puede leer la base: los **nombres de los niños de una sala** y su asistencia.
3. La cola de una persona no se borra al cerrar sesión, pero **desde la versión 2 va ligada a su dueña** (`userId`): si otra docente inicia sesión en el mismo teléfono, las marcas de la primera no se envían ni se cuentan con la sesión de la segunda. Verificado en el teléfono entre dos docentes (`docs/BITACORA.md`, «Cierre del 18 de octubre»).
4. Aún no hay una política de caducidad: una caché nunca cargada de nuevo y con sesión abierta se conserva indefinidamente (`savedAt` existe, no se usa para caducar).

**Riesgo: MEDIO** mientras haya sesión (copia local de nombres, sin cifrar, en un teléfono robado y desbloqueado) y **BAJO** después de cerrar sesión (queda solo la cola de números, estados y el identificador de su dueña). Con el borrado verificado a mano y la cola por persona, ya no se considera ALTO por teléfono compartido; sigue pendiente cifrar la base.

**Mitigaciones recomendadas dentro del marco técnico** (Room, DataStore y Android Keystore son del marco; SQLCipher **no** lo es y exigiría una fila en `DECISIONES-PENDIENTES.md`):

1. ~~Probar el borrado al cerrar sesión~~ **Hecho a mano** (ver arriba). Falta una prueba automática (instrumentada).
2. Cifrar los campos sensibles antes de guardarlos (`fullName`, y `teacherFullName`) con el `KeystoreTokenCipher` que ya existe para los tokens (AES-GCM, llave dentro del Keystore). Ojo: la consulta `ORDER BY fullName` del DAO ya no serviría; habría que ordenar en memoria.
3. Caducar la caché (por ejemplo, descartar lo que tenga más de 24 horas, con `savedAt`).
4. ~~Ligar cada marca pendiente a la persona que la hizo~~ **Hecho** (columna `userId`, versión 2).
5. Evaluar `FLAG_SECURE` para bloquear capturas de pantalla (es una API de Android, sin dependencias; "bloquear capturas" aparece en los buckets extras del tablero).
6. ~~Escribir la migración~~ **Hecho para la versión 1 → 2** (`MIGRATION_1_2`). Cada cambio futuro del esquema necesita la suya (no hay `fallbackToDestructiveMigration`); ver [`plan-implantacion.md`](plan-implantacion.md).

### 4.3 En el servidor (variables de entorno)

| Dato | Nivel | Cómo se protege | Riesgo conocido |
|---|---|---|---|
| `JWT_SECRET` | **Crítico** | La API no arranca sin él (mínimo 32 caracteres); `.env` está ignorado por git (`.gitignore`) y por Docker (`.dockerignore`) | Sin rotación (`kid`) ni gestor de secretos (pendiente) |
| `DATABASE_URL` / `POSTGRES_PASSWORD` | **Crítico** | Igual | `.env.example` trae `POSTGRES_PASSWORD=123456` y `S3_SECRET_KEY=cambia-esta-clave` como valores de ejemplo (`PENDIENTES.md`) |
| `S3_ACCESS_KEY` / `S3_SECRET_KEY` | **Crítico** | Igual | `config.py` trae valores por omisión (`guarderia`, `guarderia-secret`) que no deben usarse fuera de desarrollo |
| `SEED_PASSWORD` | **Crítico** en producción | Solo vale con `ENV=dev`; fuera de `dev` el seed reparte códigos en lugar de contraseñas | Tiene valor por omisión (`codea1234`) y es la contraseña de las cuentas de prueba de la base de Aiven |

### 4.4 Registros del servidor

El servidor escribe en su salida estándar (Docker) el aviso a coordinación con el **nombre completo de la docente** y el motivo. La bitácora estructurada «sin datos personales» está pendiente (`app/core/README.md`). No hay política de cuánto tiempo guarda Docker esa salida. `[decidir]`

## 5. Resumen para el equipo

| Hallazgo | Dónde |
|---|---|
| No hay política de retención escrita ni borrado automático, salvo `auth_attempt` y el borrado de personas autorizadas. | Secciones 3 y 4 |
| Las solicitudes de borrado de fotos ya las resuelve el administrador y, al aprobar, se borra el archivo del bucket. Pero no hay plazo de atención definido, no aplica a documentos y no existe forma de quitar solo la etiqueta de un niño en una foto de grupo (aprobar la borra para todas las familias). | 3.8 y 4.1 |
| Los dos textos falsos que había en la app (ubicación «no se guarda» y salida «automática») ya se corrigieron; la fecha de nacimiento ya se captura real, pero los niños inscritos antes conservan una fecha inventada. | 3.4 y 3.5 |
| Cifrado en reposo, respaldos y almacén de producción: sin definir. | 2 y 4.1 |
| La base local `codea.db` del modo sin conexión **no está cifrada** y guarda nombres de niños; la caché se borra al terminar la sesión (verificado a mano, sin prueba automática) y la cola de marcas, ligada a su dueña, no. Riesgo MEDIO con sesión, BAJO sin ella. | 4.2.1 |
| Las lecturas de fotos, documentos, reportes y mensajes no quedan en la bitácora de auditoría. | 2 |
| Los mensajes no se cifran de extremo a extremo; la privacidad frente al administrador es una regla de la API. | 3.10 |
