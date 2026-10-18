# Endpoints de la API (estado actual)

Lista sacada del código el 16 de octubre de 2026 (rama `backend`, commit `12da735`) y puesta al día el 17 de octubre con lo que se agregó después (centro, invitaciones, plantillas de rutina y avisos): rutas y roles de cada `router.py`, cuerpos y respuestas del OpenAPI que genera FastAPI, y códigos de error de cada `service.py`. Reemplaza a `Endpoints_API_Codea.docx.pdf` y a [`endpoints-api-codea.pdf`](endpoints-api-codea.pdf), que tienen el mismo contenido y ya no coinciden en varios detalles (ver [qué cambió](#qué-cambió-respecto-al-pdf)).

Son **65 endpoints en 13 módulos**. El contrato exacto, con todos los campos, sigue siendo el OpenAPI: con la API prendida, `http://localhost:8000/docs`.

El login con JWT ya está en la rama `feature/login-jwt` (aquí lo reflejan las secciones *Cuenta* y la convención de autenticación). Lo que falta vive en [`../plan/05-api-plan.md`](../plan/05-api-plan.md).

## Convenciones

| Tema | Cómo es hoy |
| --- | --- |
| Base | `http://localhost:8000/api/v1` (emulador: `http://10.0.2.2:8000/api/v1`) |
| Autenticación | `Authorization: Bearer <access_token>`: JWT de 5 minutos que devuelve `POST /auth/login`; se renueva con `POST /auth/refresh` (refresh token de 7 días, rotado en cada uso). Detalle en [`app/modules/auth/README.md`](../../guarderia-api/app/modules/auth/README.md) |
| Roles | `ADMIN`, `TEACHER`, `PARENT`, los mismos del `enum Role` de la app. Una persona puede tener más de uno |
| Alcance | Además del rol, cada consulta se filtra: el `PARENT` solo ve a sus hijos y el `TEACHER` solo a los niños de su sala. Fuera de alcance responde **404**, no 403 |
| Ids | `int` para salas, niños, momentos, reportes, fotos, documentos, conversaciones y mensajes; `uuid` para personas (docentes, padres, usuarios) |
| Nombres | `snake_case`; personas y niños con `name` + `surname`, y `full_name` ya armado |
| Fechas | `date` = `AAAA-MM-DD`; horas = ISO 8601 con zona. El "hoy" lo calcula la API con la zona del centro (`America/Monterrey`) |
| Paginación | Por cursor en Fotos y mensajes: `?limit=&cursor=` → `{items, next_cursor}` |
| Errores | Problem Details (`application/problem+json`) con `code` estable. Ejemplo abajo |
| Repetir una acción | Checada, pedir borrado de una foto y enviar un mensaje con el mismo `client_id` regresan el registro que ya existía con **200** en vez de duplicarlo |

```json
{"type": "about:blank", "title": "Primero registra tu entrada de hoy", "status": 409, "code": "NOT_CHECKED_IN",
 "detail": "Estás a 1401 m; el radio permitido es de 60 m", "distance_m": 1401, "allowed_radius_m": 60}
```

Errores comunes a todos los endpoints con sesión: `401 UNAUTHENTICATED` (sin token), `401 TOKEN_INVALID`, `403 INVITATION_REQUIRED` (cuenta sin invitación aceptada), `403 USER_INACTIVE` (dado de baja), `403 FORBIDDEN` (su rol no puede) y `422 VALIDATION_ERROR` (el JSON no tiene la forma esperada).

## Sistema

| Método | Ruta | Rol | Responde |
| --- | --- | --- | --- |
| GET | `/health/live` | Sin sesión | `{status}` |
| GET | `/health/ready` | Sin sesión | `{status, database}`; `503 DB_UNAVAILABLE` |

## Cuenta

| Método | Ruta | Rol | Envía | Responde | Errores |
| --- | --- | --- | --- | --- | --- |
| GET | `/me` | Todos | — | `MeOut`: datos de la persona, `roles` y un bloque por rol: `admin {center}`, `teacher {classroom, is_primary, shift, center}`, `parent {children[]}` | — |
| POST | `/auth/login` | Sin sesión | `{email, password}` | `TokenOut` | `401 INVALID_CREDENTIALS` (el mismo para correo desconocido y contraseña mal); `403 ACCOUNT_NOT_ACTIVATED`, `USER_INACTIVE`; `429 TOO_MANY_ATTEMPTS` (con `retry_after_s` y `Retry-After`) tras 5 fallos seguidos del mismo correo |
| POST | `/auth/activate` | Sin sesión | `{email, code, password}`: el código lo da el administrador | `TokenOut` | `403 CODE_INVALID`, `CODE_EXPIRED`, `USER_INACTIVE`; `422 PASSWORD_TOO_SHORT` (mínimo 9), `PASSWORD_TOO_LONG` (máximo 128), `PASSWORD_IS_EMAIL`; `429 TOO_MANY_ATTEMPTS` |
| POST | `/auth/refresh` | Sin sesión | `{refresh_token}` | `TokenOut` sin `me` (el viejo deja de servir) | `401 REFRESH_INVALID` |
| POST | `/auth/logout` | Sin sesión | `{refresh_token}` | 204 (también si ya no servía) | — |
| POST | `/me/password` | Todos | `{current_password, new_password}` | `TokenOut`: cierra todas las sesiones y da tokens nuevos para esta | `403 PASSWORD_INCORRECT`; `422 PASSWORD_TOO_SHORT`, `PASSWORD_TOO_LONG`, `PASSWORD_IS_EMAIL` |
| DELETE | `/me/sessions` | Todos | — | `{revoked}`: cierra todas mis sesiones | — |
| POST | `/users/{id}/access-code` | `ADMIN` | — | 201 `{code, purpose, expires_at}`: `INVITATION` si la persona nunca entró, `PASSWORD_RESET` si ya entró | `404 USER_NOT_FOUND`; `409 USER_INACTIVE` |

`TokenOut`: `{access_token, token_type, expires_in, refresh_token, refresh_expires_in, me?}`. Errores del token en cualquier endpoint: `401 UNAUTHENTICATED` (falta el encabezado), `TOKEN_EXPIRED` (la app renueva y repite) y `TOKEN_INVALID` (firma, tipo o `token_version` no coinciden: la app cierra sesión); `403 USER_INACTIVE` (de baja).

`center` trae `timezone` y el horario del centro para el checador: `entry_time`, `exit_time`, `auto_close_time` ("08:00:00") y `work_days` (1 = lunes … 7 = domingo). También sigue trayendo `latitude`, `longitude` y `check_in_radius_m`, que el checador ya no usa.

## Salas

| Método | Ruta | Rol | Envía | Responde | Errores |
| --- | --- | --- | --- | --- | --- |
| GET | `/classrooms` | `ADMIN` | — | `ClassroomOut[]`: `capacity`, `enrolled`, `available`, `primary_teacher` | — |
| POST | `/classrooms` | `ADMIN` | `{name, capacity, school_grade?, teacher_id?}` | 201 `ClassroomDetail` | `422 NAME_REQUIRED`, `CAPACITY_INVALID`; `404 TEACHER_NOT_FOUND`; `409 CENTER_MISSING` |
| GET | `/classrooms/{id}` | `ADMIN` | — | `ClassroomDetail`: lo de la lista + `children[]` y `teachers[]` | `404 CLASSROOM_NOT_FOUND` |
| PATCH | `/classrooms/{id}` | `ADMIN` | `{name?, capacity?, school_grade?}` | `ClassroomDetail` | `422 NAME_REQUIRED`, `CAPACITY_INVALID` |
| PUT | `/classrooms/{id}/staff` | `ADMIN` | `{teacher_ids[], primary_teacher_id?}` | `ClassroomDetail` | `404 TEACHER_NOT_FOUND`; `422 PRIMARY_NOT_IN_STAFF` |
| DELETE | `/classrooms/{id}` | `ADMIN` | — | 204; sus asistentes quedan sin sala | `409 CLASSROOM_NOT_EMPTY` |
| GET | `/classrooms/{id}/children?date=` | `ADMIN`, `TEACHER` (su sala) | — | `RoomChild[]`: `attendance`, `conversation_id`, `has_unread_chat`, `unread_count`, `notes_today`, `has_note_today` (notas del papá de hoy) | `404 CLASSROOM_NOT_FOUND` |

## Docentes

| Método | Ruta | Rol | Envía | Responde | Errores |
| --- | --- | --- | --- | --- | --- |
| GET | `/teachers` | `ADMIN` | — | `TeacherOut[]`: `classroom`, `is_primary`, `shift`, `checked_in_at` (hoy), `active`, `has_account`, `children_count` | — |
| POST | `/teachers` | `ADMIN` | `{name, surname?, phone, email, classroom_id?, shift_id?}` | 201 `TeacherOut`; crea su invitación | `422 NAME_REQUIRED`, `PHONE_INVALID`, `EMAIL_INVALID`; `409 EMAIL_TAKEN`; `404 SHIFT_NOT_FOUND` |
| GET | `/teachers/{id}` | `ADMIN` | — | `TeacherOut` | `404 TEACHER_NOT_FOUND` |
| PATCH | `/teachers/{id}` | `ADMIN` | `{classroom_id?, shift_id?}` (`classroom_id: null` = sin sala) | `TeacherOut` | `404 TEACHER_NOT_FOUND`, `SHIFT_NOT_FOUND` |
| POST | `/teachers/{id}/deactivate` | `ADMIN` | — | `TeacherOut`; le quita el acceso | `404 TEACHER_NOT_FOUND` |
| POST | `/teachers/{id}/reactivate` | `ADMIN` | — | `TeacherOut` | `404 TEACHER_NOT_FOUND` |
| GET | `/shifts` | `ADMIN` | — | `ShiftOut[]`: `name`, `start_time`, `end_time` | — |

## Infantes

| Método | Ruta | Rol | Envía | Responde | Errores |
| --- | --- | --- | --- | --- | --- |
| GET | `/children` | `ADMIN` | — | `ChildOut[]`: `birth_date`, `age`, `allergies`, `classroom`, `status`, `guardian` | — |
| POST | `/children` | `ADMIN` | `{name, surname?, birth_date, allergies?, classroom_id?, guardian: {name, surname?, relationship, phone, email}}` | 201 `ChildDetail`; registra al tutor y crea su invitación | `422 NAME_REQUIRED`, `GUARDIAN_REQUIRED`, `AGE_INVALID`, `PHONE_INVALID`, `EMAIL_INVALID` |
| GET | `/children/{id}` | Todos (en su alcance) | — | `ChildDetail`: lo de la lista + `guardians[]` | `404 CHILD_NOT_FOUND` |
| PATCH | `/children/{id}` | `ADMIN` | `{classroom_id}` (`null` = sin sala) | `ChildDetail` | `404 CHILD_NOT_FOUND`, `CLASSROOM_NOT_FOUND` |
| POST | `/children/{id}/deactivate` | `ADMIN` | — | `ChildDetail` | `404 CHILD_NOT_FOUND` |
| POST | `/children/{id}/reactivate` | `ADMIN` | — | `ChildDetail` | `404 CHILD_NOT_FOUND` |

`PATCH /children/{id}` sirve para tres botones de la app: cambiar de sala, "Agregar a esta sala" y "Sacar de la sala".

## Padres

| Método | Ruta | Rol | Envía | Responde | Errores |
| --- | --- | --- | --- | --- | --- |
| GET | `/parents` | `ADMIN` | — | `ParentOut[]`: `relationship`, `child_ids`, `children[]`, `has_access`, `has_account` | — |
| POST | `/parents` | `ADMIN` | `{name, surname?, phone, email, relationship, child_ids[]}` | 201 `ParentOut`; crea su invitación | `422 NAME_REQUIRED`, `PHONE_INVALID`, `EMAIL_INVALID`, `CHILDREN_REQUIRED`; `409 EMAIL_TAKEN`; `404 CHILD_NOT_FOUND` |
| GET | `/parents/{id}` | `ADMIN` | — | `ParentOut` | `404 PARENT_NOT_FOUND` |
| POST | `/parents/{id}/deactivate` | `ADMIN` | — | `ParentOut` | `404 PARENT_NOT_FOUND` |
| POST | `/parents/{id}/reactivate` | `ADMIN` | — | `ParentOut` | `404 PARENT_NOT_FOUND` |
| POST | `/parents/{id}/resend-invitation` | `ADMIN` | — | `ParentOut`; hoy solo renueva la vigencia | `409 INVITATION_ALREADY_ACCEPTED` |

## Checador

| Método | Ruta | Rol | Envía | Responde | Errores |
| --- | --- | --- | --- | --- | --- |
| GET | `/staff-attendance/today` | `TEACHER` | — | `CheckInOut` o `null` (con `checked_out_at` y `auto_closed` cuando ya hay salida). Cierra antes las entradas abiertas pasadas de las 9 pm | — |
| POST | `/staff-attendance/check-in` | `TEACHER` | `{late_reason?}` (sin ubicación) | 201 `CheckInOut`: `status` (`ON_TIME`, `LATE`), `minutes_late`, `place_name`, `auto_closed`. Repetida el mismo día: 200 con la misma | `409 NOT_WORKING_DAY` si hoy no es uno de los `work_days` del centro |
| POST | `/staff-attendance/check-out` | `TEACHER` | — (sin cuerpo) | 201 `CheckInOut` con `checked_out_at`. Repetida, o ya cerrada a las 9 pm: 200 con la misma salida | `409 NOT_CHECKED_IN` |
| POST | `/staff-attendance/notify-coordination` | `TEACHER` | `{reason: LATE, message?}` | 201 `NotifyOut`; hoy solo se guarda y se anota en el log | — |

La API calcula distancia, radio y retardo con su propio reloj; nunca acepta el estado que calculó el teléfono. La checada también arranca la rutina del día.

## Asistencia

| Método | Ruta | Rol | Envía | Responde | Errores |
| --- | --- | --- | --- | --- | --- |
| PUT | `/attendance/{child_id}` | `ADMIN`, `TEACHER` (su sala) | `{status: PRESENT \| ABSENT, date?}` | `AttendanceOut` | `404 CHILD_NOT_FOUND` |
| GET | `/children/{id}/attendance?month=AAAA-MM` | Todos (en su alcance) | — | `CalendarOut`: `days[]` con `status` (`FULL_DAY`, `WITH_NOTE`, `ABSENT`, `NO_RECORD`) y `summary` | `422 MONTH_INVALID`; `404 CHILD_NOT_FOUND` |

## Rutina

| Método | Ruta | Rol | Envía | Responde | Errores |
| --- | --- | --- | --- | --- | --- |
| GET | `/classrooms/{id}/routine?date=` | `ADMIN`, `TEACHER` (su sala) | — | `RoutineOut`: `moments[]` (con `status`, `started_at`, `confirmed_at`, `report_count`), `current_step`, `total_steps`, `active_moment_id` | `404 CLASSROOM_NOT_FOUND` |
| POST | `/routine-moments/{id}/confirm` | `TEACHER` | — | `RoutineOut` ya avanzada | `409 MOMENT_NOT_ACTIVE`; `404 MOMENT_NOT_FOUND` |
| GET | `/routine-templates` | `ADMIN` | — | `TemplateOut[]`: `id`, `name`, `is_default`, `moments[]` (`number`, `name`, `category`) y `classrooms[]` que la tienen asignada. La predeterminada va primero | — |
| POST | `/routine-templates` | `ADMIN` | `{name, moments[{name, category}], is_default?}` | 201 `TemplateOut` | `422 NAME_REQUIRED`, `MOMENTS_REQUIRED` |
| PUT | `/routine-templates/{id}` | `ADMIN` | Igual que el POST: la plantilla se manda entera y reemplaza sus momentos, en ese orden | `TemplateOut` | `404 TEMPLATE_NOT_FOUND`; `422 NAME_REQUIRED`, `MOMENTS_REQUIRED` |
| PUT | `/classrooms/{id}/routine-template` | `ADMIN` | `{template_id}` (`null` = vuelve a la predeterminada) | `{classroom, template_id}` | `404 CLASSROOM_NOT_FOUND`, `TEMPLATE_NOT_FOUND` |

`category` es una de `ARRIVAL`, `MEAL`, `ACTIVITY`, `NAP`, `HYGIENE` o `PICKUP`. La plantilla de la sala (la asignada o, si no tiene, la predeterminada) se copia a los momentos del día **al checar la docente**: cambiar una plantilla no modifica un día que ya empezó.

## Reportes y día del niño

| Método | Ruta | Rol | Envía | Responde | Errores |
| --- | --- | --- | --- | --- | --- |
| POST | `/reports` | `TEACHER` | `{routine_moment_id, type, method, description, child_ids[], media_id?}` | 201 `ReportOut` | `422 DESCRIPTION_TOO_SHORT`, `CHILDREN_REQUIRED`; `409 MOMENT_NOT_STARTED`, `CHILD_ABSENT`, `MEDIA_NOT_READY`; `404 CHILD_NOT_FOUND`, `MEDIA_NOT_FOUND`, `MOMENT_NOT_FOUND` |
| GET | `/reports?moment_id=` | `ADMIN`, `TEACHER` | — | `ReportOut[]` | `404 MOMENT_NOT_FOUND` |
| POST | `/children/{id}/daily-entries` | `ADMIN`, `TEACHER` | `{date?, mood?, meals?, diaper_changes?, nap_minutes?, general_note?}`; solo cambia lo que se manda | `DailyEntryOut` | `404 CHILD_NOT_FOUND` |
| GET | `/children/{id}/weekly-summary?week_start=` | Todos (en su alcance) | — | `WeeklySummaryOut`: lunes a viernes de esa semana (sin fecha, la de hoy): `days_attended`, `days_absent`, `moods`, `meals`, `nap_minutes_average`, `diaper_changes_total`, `reports_by_type`, `achievements`, `family_notes` y `days[]` | `404 CHILD_NOT_FOUND`; `422` si la fecha no es válida |
| GET | `/children/{id}/day?date=` | Todos (en su alcance) | — | `DayOut`: `attendance`, `summary`, `timeline[]` y `notes[]` (las notas del papá) | `404 CHILD_NOT_FOUND` |

## Fotos

| Método | Ruta | Rol | Envía | Responde | Errores |
| --- | --- | --- | --- | --- | --- |
| POST | `/media/uploads` | `TEACHER` (`PHOTO`), `PARENT` (`DOCUMENT`) | `{content_type, size_bytes, kind?: PHOTO \| DOCUMENT}` | 201 `{media_id, url, fields, expires_in, max_bytes}`: un permiso de 5 minutos, máximo 10 MB | `422 CONTENT_TYPE_NOT_ALLOWED`, `FILE_TOO_LARGE`; `403 FORBIDDEN` (otro rol u otro tipo) |
| POST | `/media/{id}/complete` | Quien la pidió | — | `MediaOut` con `status: READY` | `409 UPLOAD_MISSING`; `404 MEDIA_NOT_FOUND` |
| GET | `/media?child_id=&category=&cursor=&limit=` | Todos (en su alcance) | — | `{items: FeedItem[], next_cursor}`; cada foto con `url` firmada de 10 minutos, `category`, `teacher_name`, `note` | `404 CHILD_NOT_FOUND` |
| POST | `/media/{id}/deletion-request` | `PARENT` | `{reason?}` | 201 `DeletionRequestOut`; repetida: 200 con la misma | `404 MEDIA_NOT_FOUND` |
| GET | `/media/deletion-requests?status=` | `ADMIN` | — | `DeletionReviewOut[]` (por omisión `PENDING`): `reason`, `requested_by_name`, `child_names`, `photo_url` (firmada, 10 min; vacía si ya se borró), `decided_at`, `decision_note` | — |
| POST | `/media/deletion-requests/{id}/approve` | `ADMIN` | `{note?}` | `DeletionReviewOut`: borra la foto del bucket y de todas las galerías | `404 DELETION_REQUEST_NOT_FOUND`, `409 REQUEST_ALREADY_DECIDED`, `502 STORAGE_UNAVAILABLE` |
| POST | `/media/deletion-requests/{id}/reject` | `ADMIN` | `{note?}` | `DeletionReviewOut`: la foto se queda | `404`, `409 REQUEST_ALREADY_DECIDED` |

Subida: después de `/media/uploads`, la app manda un POST multipart a `url` con todos los `fields` y el archivo en el campo `file`, y luego llama `/complete`.

## Documentos y personas autorizadas

| Método | Ruta | Rol | Envía | Responde | Errores |
| --- | --- | --- | --- | --- | --- |
| GET | `/children/{id}/documents` | `ADMIN`, `PARENT` (sus hijos) | — | `DocumentOut[]`: `title`, `due_date`, `status` (`CURRENT`, `MISSING`), `url` firmada | `404 CHILD_NOT_FOUND` |
| PUT | `/children/{id}/documents/{doc_id}` | `ADMIN`, `PARENT` | `{media_id}` | `DocumentOut` | `404 DOCUMENT_NOT_FOUND`, `MEDIA_NOT_FOUND`; `409 MEDIA_NOT_READY` |
| GET | `/children/{id}/authorized-pickups` | Todos (en su alcance) | — | `PickupOut[]` | `404 CHILD_NOT_FOUND` |
| POST | `/children/{id}/authorized-pickups` | `ADMIN`, `PARENT` | `{full_name, relationship?, phone?}` | 201 `PickupOut` | `404 CHILD_NOT_FOUND` |
| DELETE | `/children/{id}/authorized-pickups/{person_id}` | `ADMIN`, `PARENT` | — | 204 | `404 PICKUP_NOT_FOUND` |

Hoy no hay endpoint para pedir documentos: la lista de cada niño la crea el seed.

## Comunicación

| Método | Ruta | Rol | Envía | Responde | Errores |
| --- | --- | --- | --- | --- | --- |
| POST | `/announcements` | `ADMIN` | `{text, classroom_id?}` (sin sala = todo el centro) | 201 `AnnouncementOut`: `id`, `text`, `classroom_id`, `classroom_name`, `created_at` | `422 TEXT_REQUIRED`; `404 CLASSROOM_NOT_FOUND` |
| GET | `/announcements?limit=` | Todos | — | `AnnouncementOut[]`, el más reciente primero (50 por omisión, máximo 200). `ADMIN`: todos los del centro. `TEACHER`: los del centro y los de su sala. `PARENT`: los del centro y los de las salas de sus hijos | — |
| GET | `/conversations` | `TEACHER`, `PARENT` (al `ADMIN` le regresa lista vacía) | — | `ConversationOut[]`: una por niño, con `child`, `unread_count` y `last_message` | — |
| GET | `/conversations/{id}/messages?cursor=&after=&limit=` | Participantes | — | `{items: MessageOut[], next_cursor}`; con `after=<id>` solo los nuevos. Cada mensaje trae `author_name`, `is_mine` e `is_read` | `404 CONVERSATION_NOT_FOUND` |
| POST | `/conversations/{id}/messages` | Participantes | `{text, client_id?}` | 201 `MessageOut`; mismo `client_id`: 200 con el que ya existía | `422 TEXT_REQUIRED`; `404 CONVERSATION_NOT_FOUND` |
| POST | `/conversations/{id}/read` | Participantes | — | 204 | `404 CONVERSATION_NOT_FOUND` |
| POST | `/children/{id}/notes` | `PARENT` | `{text}` | 201 `NoteOut` (nota para hoy) | `422 TEXT_REQUIRED`; `404 CHILD_NOT_FOUND` |

Participantes de una conversación: los padres ligados al niño y las docentes de su sala. El servidor no lleva la cuenta de avisos leídos: la app guarda en el teléfono cuáles ya se vieron.

## Centro e invitaciones

| Método | Ruta | Rol | Envía | Responde | Errores |
| --- | --- | --- | --- | --- | --- |
| GET | `/center` | `ADMIN` | — | `CenterOut`: `name`, `latitude`, `longitude`, `check_in_radius_m`, `timezone`, `entry_time`, `exit_time`, `auto_close_time`, `work_days` | `409 CENTER_MISSING` |
| PATCH | `/center` | `ADMIN` | `{name?, latitude?, longitude?, check_in_radius_m?, timezone?, entry_time?, exit_time?, auto_close_time?, work_days?}` (solo cambia lo que se manda; latitud y longitud en `null` quitan la ubicación). Horario: la entrada va antes de la salida y la salida no pasa del cierre | `CenterOut` | `422 LOCATION_INCOMPLETE`, `LOCATION_INVALID`, `RADIUS_INVALID`, `TIMEZONE_INVALID`, `SCHEDULE_INVALID`, `WORKDAYS_INVALID` |
| POST | `/invitations` | `ADMIN` | `{name, surname?, phone, email, role}` | 201 `InvitationOut` con `access_code {code, purpose, expires_at}` (se muestra una sola vez) | `409 EMAIL_TAKEN`, `ALREADY_HAS_ACCOUNT` |
| GET | `/invitations?status=&role=` | `ADMIN` | — | `InvitationOut[]`: `status` (`PENDING`, `ACCEPTED` = código usado, `EXPIRED`), `expires_at`, `accepted_at` | — |

## Auditoría

| Método | Ruta | Rol | Respuesta | Errores |
| --- | --- | --- | --- | --- |
| GET | `/audit-log?action=&actor_id=&limit=&cursor=` | `ADMIN` | `Page<AuditOut>`: `id`, `created_at`, `action`, `actor_id`, `actor_name`, `actor_email`, `entity_type`, `entity_id`, `detail`, `ip`; lo más nuevo primero, `limit` de 1 a 200 | `403` si no es admin; `422` si `limit` está fuera de rango |

Acciones y qué datos guarda cada una: [`app/modules/audit/README.md`](../../guarderia-api/app/modules/audit/README.md). Solo se agrega; no hay forma de editar ni borrar.

## Enums

| Enum | Valores |
| --- | --- |
| `Role` | `ADMIN`, `TEACHER`, `PARENT` |
| `Relationship` | `MOTHER`, `FATHER`, `LEGAL_GUARDIAN`, `OTHER` |
| `ChildStatus` | `ACTIVE`, `INACTIVE` |
| `CheckInStatus` | `ON_TIME`, `LATE` |
| `CoordinationReason` | `LATE` |
| `AttendanceStatus` | `PRESENT`, `ABSENT` |
| `DayAttendanceStatus` | `FULL_DAY`, `WITH_NOTE`, `ABSENT`, `NO_RECORD` |
| `MomentCategory` | `ARRIVAL`, `MEAL`, `ACTIVITY`, `NAP`, `HYGIENE`, `PICKUP` |
| `MomentStatus` | `DONE`, `ACTIVE`, `PENDING` |
| `ReportType` | `INCIDENT`, `ACHIEVEMENT`, `HEALTH`, `BEHAVIOR` |
| `ReportMethod` | `WRITTEN`, `DICTATION`, `PHOTO` |
| `Mood` | `HAPPY`, `CALM`, `TIRED`, `SAD`, `UPSET` |
| `MealAmount` | `ALL`, `MOST`, `HALF`, `LITTLE`, `NONE` |
| `TimelineType` | `CHECK_IN`, `MEAL`, `ACTIVITY`, `NAP` |
| `MediaKind` | `PHOTO`, `DOCUMENT` |
| `MediaStatus` | `PENDING`, `READY` |
| `PhotoCategory` | `ACTIVITIES`, `MEALS`, `NAP`, `INCIDENTS` |
| `DocumentStatus` | `CURRENT`, `MISSING` |
| `DeletionStatus` | `PENDING`, `APPROVED`, `REJECTED` |

## Qué cambió respecto al PDF

| Tema | El PDF dice | El código hace |
| --- | --- | --- |
| Rutas viejas | Cada endpoint trae su "Antes: /docente/…, /admin/…, /padre/…" | Ya no existen; solo las rutas de esta lista |
| Turnos | No aparece | `GET /shifts` para el formulario de alta de asistente |
| Alta de docente | `{…, shift}` | `{…, surname?, shift_id}`; `PATCH` usa `shift_id` |
| Crear y editar sala | Crear: `{name, capacity, teacher_id?}`; editar: `{capacity}` | Crear también acepta `school_grade`; editar acepta `name`, `capacity` y `school_grade` |
| Eliminar sala | "Falta decidir qué pasa con los niños" | `409 CLASSROOM_NOT_EMPTY` si tiene niños; las asistentes quedan sin sala |
| Nombres | Un solo `name` | `name` + `surname` en docentes, padres, tutores e infantes |
| Checador | — | Repetir la checada el mismo día regresa la misma con 200; `notify-coordination` pide `reason` |
| Asistencia | Solo `{status}` | También acepta `date`; la puede marcar el `ADMIN` |
| Fotos | `POST /media/uploads` sin cuerpo descrito | Pide `content_type` y `size_bytes`; máximo 10 MB; también sirve para documentos (`kind: DOCUMENT`) |
| Chat | — | `?after=<id>` para traer solo lo nuevo, `client_id` contra duplicados, `is_mine` e `is_read` en cada mensaje |
| Día del niño | Solo el padre | Cualquier rol en su alcance: la docente también lo lee (incluye las notas del papá) |
| Resumen del día | Solo la docente | También el `ADMIN` |
| Autorizados | Padre y admin | Leer: cualquier rol en su alcance (la docente también) |
| Reenviar invitación | "Hay que construirlo completo" | Solo renueva la vigencia; no hay código ni correo todavía |
