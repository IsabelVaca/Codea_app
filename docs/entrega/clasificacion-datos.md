# Clasificación de datos por campo

Parte 6 de la rúbrica (ética y legal), punto 3. Estado a **2026-10-20**, puesto al día contra `main` (commit `043c2d6`, 13 migraciones hasta `d74a0f6c4e15`): datos de salud del niño, `child_departure`, foto del chat, hora de entrada del niño (`attendance.arrived_at`), imagen de los avisos (`announcement.media_id`) y menú del día (`daily_menu`); las personas autorizadas pasan al administrador y la familia borra sus fotos directamente. Incluye la base **local** del teléfono (sección 4.2.1, versión 5, con la caché de respuestas de los tres roles) y los **respaldos** diarios del servidor compartido (sección 4.5).

Cada campo de cada tabla sale de los `models.py` de `guarderia-api/app/modules/*` (las 34 tablas de las 13 migraciones de `guarderia-api/migrations/versions/`). Lo que vive fuera de Postgres (bucket de archivos, teléfono, variables de entorno, registros del servidor, respaldos) está en la sección 4. Desde el 19/oct hay un servidor compartido (`api.codeaapp.site`, ver [`plan-implantacion.md`](plan-implantacion.md)); en él solo se comprobaron HTTPS, cabeceras y que el almacén no entrega archivos sin firma. Lo demás se lee del código.

## 1. Cómo leer las tablas

### Niveles de sensibilidad

| Nivel | Qué es | Ejemplos en este proyecto |
|---|---|---|
| **Bajo** | No identifica a nadie por sí solo. | Estados, categorías, nombre de una sala, plantillas de rutina. |
| **Medio** | Dato personal ordinario de una persona adulta o dato laboral. | Nombre, correo y teléfono de una docente o de un tutor; hora de checada; dirección IP. |
| **Alto** | Dato de un **menor**, dato de **salud**, **ubicación** precisa de una persona, o archivo (foto o documento). La ley mexicana trata los datos de salud como sensibles. | Nombre, fecha de nacimiento y alergias del infante; reportes; fotos; documentos; mensajes sobre el menor; coordenadas del checador. |
| **Crítico** | Si se filtra, permite hacerse pasar por alguien o abrir todo lo demás. | Hash de contraseña, hash de tokens y de códigos, `JWT_SECRET`, llaves del bucket, contraseña de la base. |

### Columnas

- **Dónde vive:** `PG` = Postgres (en el servidor compartido, la base `codea_prod` alojada en Aiven; en desarrollo, el contenedor `db` de `docker-compose.yml`) · `S3` = bucket compatible con S3 (SeaweedFS, en desarrollo y en la Mac que sirve `codeaapp.site`) · `Tel` = teléfono · `Env` = variables de entorno del servidor. **Todo lo de `PG` y `S3` se copia también a los respaldos diarios** (sección 4.5).
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
| Límite de intentos | 5 fallos por correo o 30 por IP en 15 minutos bloquean con `429`. | `auth/throttle.py`, `tests/test_security.py` | Detrás del túnel, uvicorn toma la IP real de `X-Forwarded-For` y solo confía en la red interna (`--proxy-headers`, `docker-compose.prod.yml`). |
| Cabeceras | `X-Content-Type-Options`, `X-Frame-Options: DENY`, `Referrer-Policy`, `Permissions-Policy`, CSP y `Cache-Control: no-store` en `/api/`; HSTS cuando `ENV` no es `dev`. | `app/main.py` | Comprobadas en `api.codeaapp.site` el 19/oct, con HTTPS de Cloudflare delante. |
| CORS cerrado | Sin orígenes por omisión. | `app/main.py`, `config.py` | — |
| Bitácora de auditoría | Registra inicios de sesión, altas, bajas y cambios del administrador; solo se agrega (la API no edita ni borra). | `app/modules/audit` | Registra también los cambios al centro (`CENTER_UPDATED`) y las decisiones sobre fotos. **No** registra lecturas de fotos, documentos, reportes ni mensajes. Quien tenga acceso directo a la base sí podría modificarla. |
| Archivos sin pasar por la API | Subida con URL firmada de 5 minutos (máx. 10 MB; JPEG, PNG, WebP y, solo en documentos, PDF); lectura con URL firmada de 10 minutos; la llave del objeto no lleva datos personales. | `app/core/storage.py`, `media/service.py` | Quien tenga la URL firmada puede ver el archivo durante esos 10 minutos. |
| Contraseñas | Argon2id; entre 9 y 128 caracteres; no puede ser el correo. | `app/core/passwords.py`, `app/core/rules.py` | — |
| Base local del teléfono (sin conexión y caché) | Almacenamiento privado de la app, `allowBackup="false"`; las cachés (Mi sala y respuestas de la API de los tres roles) se borran al terminar la sesión | `di/AppContainer.kt`, `data/offline/` | **No está cifrada** y desde el 20/oct guarda datos de salud; el borrado de Mi sala se verificó a mano y ninguno tiene prueba automática (ver 4.2.1) |
| Tokens en el teléfono | Access y refresh cifrados con AES-GCM; la llave vive en el Android Keystore. | `app/.../data/local/TokenCipher.kt` | El nombre, el id y los roles se guardan sin cifrar. |
| Tráfico de la app | Release: solo HTTPS. Debug: texto plano únicamente hacia `10.0.2.2` y `localhost`. | `network_security_config.xml` (main y debug) | La dirección de release es `https://api.codeaapp.site/api/v1/` (túnel de Cloudflare, con certificado emitido por Cloudflare). Cloudflare termina el HTTPS en sus servidores, así que el tráfico pasa descifrado por ellos antes de entrar al túnel. |
| Cifrado en reposo (Postgres, bucket y respaldos) | **No lo configura el proyecto.** Depende del proveedor y de la máquina. | — | **Por confirmar** con la cuenta de Aiven y en el disco de la Mac servidor, donde viven las fotos y los respaldos. |
| Respaldos | Desde el 20/oct, cada día a las 3:00: `pg_dump` de la base y copia del volumen de fotos, 14 días, en una carpeta solo del usuario (modo 700). Restauración de la base probada. | `guarderia-api/scripts/respaldo.sh`, `guarderia-api/README.md` («Respaldos») | **No están cifrados** y viven en la misma Mac que el servidor. Conservan hasta 14 días lo que se borra o se da de baja, incluidas las fotos que borra una familia (ver 4.5). |

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
| `latitude`, `longitude` | Bajo | Siguen en `daycare_center` y las edita el administrador por la API (`PATCH /center`), pero **el checador ya no las usa** y la app ya no las pide ni las muestra | Sin borrado | — |
| `check_in_radius_m`, `timezone` | Bajo | Validadas (`RADIUS_INVALID`, `TIMEZONE_INVALID`) | Sin borrado | — |
| `entry_time`, `exit_time`, `auto_close_time`, `work_days` | Bajo | Solo el administrador las edita (`PATCH /center`); la API valida que la entrada vaya antes de la salida, que el cierre no sea antes de la salida y que haya al menos un día (`SCHEDULE_INVALID`, `WORKDAYS_INVALID`); cada cambio queda en la bitácora de auditoría (`CENTER_UPDATED`, con el horario nuevo) | Sin borrado | — |

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
| `allergies` | **Alto** (salud) | Igual. La puede editar el tutor desde «Mi hijo» (`PATCH /children/{id}/health`) | Igual | Igual |
| `medical_conditions`, `intolerances`, `medications`, `other_notes` | **Alto** (salud) | Texto libre de hasta 1,000 caracteres que escribe el tutor o el administrador; la docente de la sala los ve en la ficha. La bitácora de auditoría guarda **qué campos** cambiaron, no el contenido. Texto en claro en la base | Igual | Igual |
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
| `checked_out_at` | Medio | La hora de la salida: la pone el servidor cuando la docente registra su salida (`POST /staff-attendance/check-out`) o el cierre automático de las 21:00; vacía hasta entonces | Sin borrado | Como la fila |
| `auto_closed` | Bajo | `true` si la salida la puso el cierre automático y no la docente | Sin borrado | Como la fila |
| `status`, `minutes_late` | Medio | El servidor calcula el retardo; no acepta el que mande el teléfono | Sin borrado | Como la fila |
| `late_reason` | Medio | Texto libre opcional de la docente | Sin borrado | Igual |

> **Ubicación (18/oct).** El checador ya no guarda ubicación: la migración `e74f0a5b3d43` quita `latitude`, `longitude`, `distance_m` y las columnas `out_*` de `staff_check_in`. La base del servidor compartido se creó con ella, así que no tiene esas columnas.
>
> **Nota histórica (commit `f814763`).** El permiso de ubicación del checador decía «no se guarda»; ahora dice que se compara con la ubicación de la estancia y que, al registrar la entrada o la salida, «queda guardada junto con la hora», y que no se sigue a la persona. Coincide con lo que hace el servidor.

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
| `arrived_at` | **Alto** | Hora de entrada del niño (migración `b18e4d7a2c93`): la pone el servidor al pasar lista, o la del teléfono si la marca se hizo sin conexión; se borra al marcar falta. La ve su familia en Hoy | Sin borrado | Como `child` |
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
| `status` | Bajo | `PENDING` hasta que el archivo llega al bucket; `READY`; `DELETED` cuando la familia la borra (el archivo sale del bucket y la foto de todas las galerías) | Sin borrado de la fila. **Los `PENDING` que nunca se completan quedan** | Borrar los `PENDING` de más de 1 día `[decidir]` |
| `uploaded_by` | Medio | Solo quien subió el archivo puede completarlo | Sin borrado | Como el archivo |

#### `media_child`: a quién se etiqueta en una foto

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `media_id`, `child_id` | **Alto** | Decide qué familias ven la foto: **una foto con varios niños la ven las familias de todos ellos** | Sin borrado | Como la foto |

#### `media_deletion_request`: solicitudes de borrado (sin uso desde el 19/oct)

La familia ya no pide el borrado: lo hace directamente (`DELETE /media/{id}`) y queda en `audit_log` como `MEDIA_DELETED_BY_FAMILY`. La tabla se conserva con las solicitudes que se hicieron antes, sin que nada la lea ni la escriba; hay que decidir si se elimina o cuánto tiempo se conserva **[decidir]**. Sus campos (`media_id`, `requested_by`, `reason`, `status`, `decided_by`, `decided_at`, `decision_note`) eran de nivel Medio a Alto.

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
| `child_id` | **Alto** | Alcance por registro; la docente también puede leer la lista. **Agregar y quitar son solo del administrador** desde el 18/oct | **Se borra de verdad** cuando el administrador la quita (`db.delete`); las salidas ya registradas conservan su nombre como texto | Ya cumplido en el borrado manual |
| `full_name`, `relationship`, `phone` | **Alto** | Es un **tercero que no se registró ni aceptó nada**: el administrador lo dio de alta. Teléfono en texto claro | Igual | Igual |
| `added_by` | Medio | — | Igual | Igual |

#### `child_departure`: el pase de salida (módulo `departures`, migración `a07c3d8e6f76`)

Una fila por niño y por día: a qué hora se fue y con quién.

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `child_id`, `date` | **Alto** | Alcance por registro: la docente de la sala y el administrador; los tutores ven la hora y quién lo recogió como último evento de Hoy («Salida · Lo recogió …») | Sin borrado | Como `child` |
| `departed_at` | **Alto** | La hora la pone el servidor | Sin borrado | Igual |
| `picked_up_by` | **Alto** | Nombre de quien lo recogió, guardado como texto aunque después quiten a esa persona de la lista. Es un dato de un **tercero** | Sin borrado | Igual |
| `pickup_id`, `guardian_id` | Medio | FK (`ON DELETE SET NULL`); solo una de las dos | Sin borrado | Igual |
| `recorded_by` | Medio | Quién registró la salida | Sin borrado | Igual |

### 3.10 Comunicación (`messaging`)

#### `conversation`, `message` y `conversation_read`

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `conversation.child_id` | **Alto** | Una conversación por niño | Sin borrado | Como `child` |
| `message.text` | **Alto** | Solo los tutores del niño y las docentes de su sala la leen. **El administrador no puede leerla por la API**: `GET /conversations` le devuelve lista vacía y los mensajes responden 404 | Sin borrado | Como `child` `[decidir]` |
| `message.media_id` | **Alto** | La foto del mensaje (migración `a96b2c7d5f65`); vive en el bucket como las demás fotos y solo la ven quienes ven la conversación. Puede ser la única carga del mensaje (texto vacío) | Sin borrado | Como `child` `[decidir]` |
| `message.author_id`, `conversation_id` | Medio | — | Sin borrado | Igual |
| `message.client_id` | Bajo | Evita duplicados en reintentos | Sin borrado | — |
| `conversation_read.conversation_id`, `user_id`, `last_read_message_id` | Bajo | Sirve para contar no leídos | Sin borrado | — |

> Los mensajes **no están cifrados de extremo a extremo**: quien administre la base (Aiven o el servidor) puede leerlos. Que el administrador de la app no los vea es una regla de la API, no una barrera técnica.

#### `announcement`

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `text` | Bajo | Lo escribe el administrador; lo ven todo el centro o solo una sala. Puede ir vacío si el aviso es solo una imagen | Sin borrado. **No se pueden borrar avisos** (`docs/plan/01-app-pantallas.md`) | `[decidir]` |
| `media_id` | Bajo a **Alto** | La imagen del aviso (migración `c63f9e5b3d04`); vive en el bucket y no entra en la galería de ningún niño. Es Alto si la imagen muestra a menores | Sin borrado | `[decidir]` |
| `daycare_center_id`, `classroom_id`, `author_id` | Bajo | — | Sin borrado | — |

#### `parent_note`: nota del tutor para la asistente

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `child_id`, `date` | **Alto** | Alcance por registro | Sin borrado | Como `child` |
| `text` | **Alto** | Texto libre del tutor sobre su hijo | Sin borrado | Igual |
| `author_id`, `read_at` | Medio | — | Sin borrado | Igual |

### 3.11 Menú del día (`menus`)

#### `daily_menu`: desayuno, comida y colación de un día (uno por centro y día, migración `d74a0f6c4e15`)

| Campo | Nivel | Protección propia | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|
| `daycare_center_id`, `date` | Bajo | Única por centro y día | Sin borrado; «Quitar menú» borra la fila | — |
| `breakfast`, `lunch`, `snack` | Bajo | Texto libre de hasta 500 caracteres; solo el administrador lo escribe y lo ven todos los roles | Igual | — |
| `updated_by` | Medio | Quién lo capturó | Igual | — |

## 4. Fuera de las tablas

### 4.1 Archivos en el bucket

| Objeto | Nivel | Dónde vive | Cómo se protege | Se conserva hoy | Retención propuesta |
|---|---|---|---|---|---|
| Foto de una actividad, del chat o de un aviso (`kind = PHOTO`) | **Alto** (imagen de menores) | `S3`: SeaweedFS, en desarrollo y en el servidor compartido (`archivos.codeaapp.site`, en el disco de la Mac servidor); la decisión #9 sigue sin aprobar | La API solo entrega el archivo con una URL firmada de 10 minutos, y solo a quien tiene alcance sobre el niño etiquetado (o sobre la conversación o el aviso); llave sin datos personales | **Se borra del bucket cuando la familia la borra** (`DELETE /media/{id}`, `Storage.delete`); si nadie la borra, **no se borra nunca**. Sigue en los respaldos diarios hasta 14 días (4.5). Los `PENDING` abandonados quedan | Mientras el niño esté inscrito + `[decidir]` |
| Documento del niño (`kind = DOCUMENT`) | **Alto** (puede contener salud o discapacidad) | `S3` | Igual | **Sin borrado**: el borrado de la familia es solo de fotos. Reemplazar el documento deja el objeto anterior huérfano en el bucket (el código solo cambia `media_id`) | Igual |

En `docker/seaweedfs-s3.sh` hay una sola identidad, la de la API, con permisos de administración. En el servidor compartido se comprobó el 19/oct que `archivos.codeaapp.site` responde **403** a una petición sin firma (`docs/BITACORA.md`), y que la URL de una foto borrada también responde 403 (`docs/REQUISITOS.md`, RF-10). Desde el 20/oct el almacén arranca con hasta 100 volúmenes de 1 GB.

### 4.2 En el teléfono

| Dato | Nivel | Dónde | Cómo se protege | Se conserva |
|---|---|---|---|---|
| `access_token` y `refresh_token` | **Crítico** | DataStore (`SessionStore`) | AES-GCM de 256 bits con llave en el Android Keystore; si la llave se pierde, la sesión se descarta | Hasta cerrar sesión (se borran) o hasta que vence el refresh (7 días) |
| `user_id`, nombre y roles | Medio | DataStore, **sin cifrar** | Almacenamiento privado de la app; `allowBackup="false"` | Hasta cerrar sesión |
| Avisos ya leídos | Bajo | SharedPreferences `leidos` (`LeidosStore`) | Almacenamiento privado de la app | **No se borra al cerrar sesión** (solo se separa por usuario); cambiar de teléfono los vuelve «nuevos» |
| Ubicación del teléfono (checador) | — | **Ya no se usa:** el checador no pide ubicación | — | — |
| Fotos e imágenes de avisos ya vistas | **Alto** | Caché en memoria y en disco de Coil, dentro del almacenamiento privado de la app | Desde el 20/oct `ui/components/ImagenEstable.kt` usa la URL sin firma como llave, para que una imagen ya vista salga del disco y no se vuelva a bajar | **No se borra al cerrar sesión** (el código no limpia la caché de Coil); queda hasta que el sistema o Coil la desaloje |
| Cámara y dictado | Alto | La app usa la cámara y el reconocedor de voz **del sistema** (`TakePicturePreview`, `RecognizerIntent`) | El audio lo procesa el reconocedor del teléfono (la decisión #10 dice que usa el de Google); la app solo recibe el texto | La app no guarda el audio |

### 4.2.1 Base local `codea.db` (modo sin conexión)

Desde el commit `99fac75` la app guarda una base **Room** en el teléfono (`codea.db`, **versión 5**; el esquema de cada versión se exporta a `app/schemas/mx.tec.codea.data.offline.AppDatabase/1.json` a `5.json`) para que la docente pueda marcar asistencia sin red y, desde el 20/oct, para que las pantallas de los tres roles muestren al instante lo último que cargaron. Son tablas **locales**: no existen en Postgres.

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
| | `notesJson` | **Alto** (texto que escribió una familia sobre un menor) | Los avisos de casa de hoy con su texto, para «De casa hoy» sin conexión (versión 3, `MIGRATION_2_3`) | Igual |
| | `hasSummaryToday` | Bajo | Si el niño ya tiene resumen del día | Igual |
| | `arrivedAt` | **Alto** | Hora de entrada del niño hoy (versión 4, `MIGRATION_3_4`) | Igual |
| `cached_response` | `key`, `userId`, `savedAt` | Medio | La llave es «usuario\|pantalla» (por ejemplo, el día de un niño en una fecha); cada persona solo lee lo suyo (versión 5, `MIGRATION_4_5`, 20/oct) | Se reemplaza en cada respuesta nueva; **se borra cuando la sesión queda sin usuario** |
| | `json` | **Alto** | La última respuesta de la API, como JSON. **Docente:** su perfil, su checada, la rutina de hoy, la ficha de cada niño que abrió (con alergias, condiciones médicas, intolerancias, medicamentos y otras indicaciones), quién lo recoge (nombre, parentesco y teléfono de terceros) y el pase de salida. **Padre:** su perfil, el día de su hijo (resumen, reportes, menú, entrada y salida con el nombre de quien lo recogió), el calendario, el resumen semanal, «Mi hijo» con los datos de salud y la lista de fotos (con URLs firmadas que vencen en 10 minutos). **Administrador:** las listas de docentes, infantes y padres (con correos, teléfonos y alergias), salas, rutinas y avisos | Igual |

Además, la app guarda con DataStore (`SeenNotesStore`) **solo los identificadores** de los avisos que la docente marcó «Ya lo vi», por usuario. No viaja al servidor.

**Cómo se protege hoy:** almacenamiento privado de la app (otras apps no lo leen sin permisos de superusuario) y `allowBackup="false"`. **La base no está cifrada** (los tokens sí). La caché de Mi sala (`cached_room`, `cached_child`) y la de respuestas (`cached_response`) se borran solas cuando la sesión queda sin usuario, es decir, al cerrar sesión o cuando la sesión termina (`di/AppContainer.kt`, commits `1f2d2a0` y `da38d39`). La cola `pending_attendance` **no** se borra al cerrar sesión, para no perder marcas sin enviar.

**Límites que hay que decir con honestidad:**

1. El borrado de las cachés **no tiene prueba automática**. El de `cached_child` se verificó a mano leyendo la base del teléfono (`run-as` y `sqlite3` desde la computadora): quedan 0 filas al cerrar sesión (`docs/BITACORA.md`, «Cierre del 18 de octubre»). El de `cached_response` solo está en el código: no se ha verificado en un teléfono.
2. Mientras haya sesión, quien tenga el teléfono desbloqueado, un teléfono con superusuario o una compilación `debug` con `adb run-as` puede leer la base: los **nombres de los niños de una sala** y su asistencia y, desde el 20/oct, **datos de salud** de menores, teléfonos de terceros y, en el teléfono del administrador, las listas completas de personas.
3. La cola de una persona no se borra al cerrar sesión, pero **desde la versión 2 va ligada a su dueña** (`userId`): si otra docente inicia sesión en el mismo teléfono, las marcas de la primera no se envían ni se cuentan con la sesión de la segunda. Verificado en el teléfono entre dos docentes (`docs/BITACORA.md`, «Cierre del 18 de octubre»).
4. Aún no hay una política de caducidad: una caché nunca cargada de nuevo y con sesión abierta se conserva indefinidamente (`savedAt` existe, no se usa para caducar).
5. La caché de imágenes de Coil (4.2) no está en esta base y **no se borra al cerrar sesión**.

**Riesgo: MEDIO** mientras haya sesión (copia local sin cifrar de nombres y, desde el 20/oct, de datos de salud, en un teléfono robado y desbloqueado) y **BAJO** después de cerrar sesión (queda la cola de números, estados y el identificador de su dueña, más las imágenes ya vistas en la caché de Coil). Con el borrado de Mi sala verificado a mano y la cola por persona, ya no se considera ALTO por teléfono compartido; sigue pendiente cifrar la base.

**Mitigaciones recomendadas dentro del marco técnico** (Room, DataStore y Android Keystore son del marco; SQLCipher **no** lo es y exigiría una fila en `DECISIONES-PENDIENTES.md`):

1. ~~Probar el borrado al cerrar sesión~~ **Hecho a mano** (ver arriba). Falta una prueba automática (instrumentada).
2. Cifrar los campos sensibles antes de guardarlos (`fullName`, `teacherFullName` y el `json` de `cached_response`) con el `KeystoreTokenCipher` que ya existe para los tokens (AES-GCM, llave dentro del Keystore). Ojo: la consulta `ORDER BY fullName` del DAO ya no serviría; habría que ordenar en memoria. Otra opción para `cached_response`: no guardar las respuestas con datos de salud.
3. Caducar la caché (por ejemplo, descartar lo que tenga más de 24 horas, con `savedAt`).
4. ~~Ligar cada marca pendiente a la persona que la hizo~~ **Hecho** (columna `userId`, versión 2).
5. Evaluar `FLAG_SECURE` para bloquear capturas de pantalla (es una API de Android, sin dependencias; "bloquear capturas" aparece en los buckets extras del tablero).
6. ~~Escribir la migración~~ **Hecho de la versión 1 a la 5** (`MIGRATION_1_2` a `MIGRATION_4_5`). Cada cambio futuro del esquema necesita la suya (no hay `fallbackToDestructiveMigration`); ver [`plan-implantacion.md`](plan-implantacion.md).
7. Borrar la caché de imágenes de Coil al cerrar sesión, junto con las de Room.

### 4.3 En el servidor (variables de entorno)

| Dato | Nivel | Cómo se protege | Riesgo conocido |
|---|---|---|---|
| `JWT_SECRET` | **Crítico** | La API no arranca sin él (mínimo 32 caracteres); `.env` está ignorado por git (`.gitignore`) y por Docker (`.dockerignore`) | Sin rotación (`kid`) ni gestor de secretos (pendiente) |
| `DATABASE_URL` / `POSTGRES_PASSWORD` | **Crítico** | Igual | `.env.example` trae `POSTGRES_PASSWORD=123456` y `S3_SECRET_KEY=cambia-esta-clave` como valores de ejemplo (`PENDIENTES.md`) |
| `S3_ACCESS_KEY` / `S3_SECRET_KEY` | **Crítico** | Igual | `config.py` trae valores por omisión (`guarderia`, `guarderia-secret`) que no deben usarse fuera de desarrollo |
| `SEED_PASSWORD` | **Crítico** en producción | Solo vale con `ENV=dev`; fuera de `dev` el seed reparte códigos en lugar de contraseñas | Tiene valor por omisión (`codea1234`) y es la contraseña de las cuentas de prueba de la base de Aiven |

### 4.4 Registros del servidor

El servidor escribe en su salida estándar (Docker) el aviso a coordinación con el **nombre completo de la docente** y el motivo. La bitácora estructurada «sin datos personales» está pendiente (`app/core/README.md`). No hay política de cuánto tiempo guarda Docker esa salida. `[decidir]`

### 4.5 Respaldos

| Qué | Nivel | Dónde vive | Protección | Se conserva hoy |
|---|---|---|---|---|
| Copia de la base (`pg_dump`) y del volumen de fotos | **Alto** (todo lo de `PG` y `S3`) | `~/respaldos-codea/` en la Mac servidor | Carpeta solo del usuario (modo 700); sin cifrar | 14 días (`scripts/respaldo.sh` borra los diarios más viejos); conserva ese tiempo lo que se borra o se da de baja |

## 5. Resumen para el equipo

| Hallazgo | Dónde |
|---|---|
| No hay política de retención escrita ni borrado automático, salvo `auth_attempt` y el borrado de personas autorizadas. | Secciones 3 y 4 |
| La familia borra las fotos directamente (desde el 19/oct) y el archivo sale del bucket al momento. No aplica a documentos y no existe forma de quitar solo la etiqueta de un niño en una foto de grupo: borrarla la quita para todas las familias, sin avisarles. | 3.8 y 4.1 |
| Los dos textos falsos que había en la app (ubicación «no se guarda» y salida «automática») ya se corrigieron; la fecha de nacimiento ya se captura real, pero los niños inscritos antes conservan una fecha inventada. | 3.4 y 3.5 |
| Cifrado en reposo sin definir. Los respaldos diarios (desde el 20/oct) no están cifrados y viven en la misma Mac que el servidor. | 2, 4.1 y 4.5 |
| La base local `codea.db` del modo sin conexión **no está cifrada** y guarda nombres de niños; la caché se borra al terminar la sesión (verificado a mano, sin prueba automática) y la cola de marcas, ligada a su dueña, no. Riesgo MEDIO con sesión, BAJO sin ella. | 4.2.1 |
| Las lecturas de fotos, documentos, reportes y mensajes no quedan en la bitácora de auditoría. | 2 |
| Los mensajes no se cifran de extremo a extremo; la privacidad frente al administrador es una regla de la API. | 3.10 |
