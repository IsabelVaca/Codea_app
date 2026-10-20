# `app/modules/`

Un módulo por área. Cada uno separa:

- `router.py`: HTTP y qué rol puede llamar cada endpoint.
- `service.py`: reglas, consultas y permiso por registro. Aquí vive la lógica.
- `schemas.py`: modelos Pydantic de entrada y salida.
- `models.py`: tablas SQLAlchemy.

Se registran en `app/main.py`. Todo cuelga de `/api/v1`.

| Módulo | Para qué | Tablas |
|---|---|---|
| [`auth`](auth/README.md) | Login con JWT, perfil (`/me`), códigos de acceso e invitaciones | `app_user`, `invitation`, `refresh_token`, `auth_attempt` |
| [`audit`](audit/README.md) | Bitácora de quién hizo qué (RF-14) | `audit_log` |
| [`admins`](admins/README.md) | Datos del centro: ubicación, zona horaria y horario del checador | `admin` |
| [`classrooms`](classrooms/README.md) | Salas, cupo y asistentes | `daycare_center`, `classroom` |
| [`teachers`](teachers/README.md) | Docentes y turnos | `teacher`, `shift` |
| [`children`](children/README.md) | Infantes | `child` |
| [`parents`](parents/README.md) | Padres y su relación con los hijos | `parent`, `parent_child_rel` |
| [`staff_attendance`](staff_attendance/README.md) | Checador de la docente | `staff_check_in`, `coordination_notice` |
| [`attendance`](attendance/README.md) | Asistencia del niño y calendario | `attendance` |
| [`routines`](routines/README.md) | Plantillas y rutina diaria por sala | `routine_template`, `routine_template_moment`, `routine_moment` |
| [`reports`](reports/README.md) | Reportes, resumen del día | `report`, `report_child`, `daily_summary` |
| [`media`](media/README.md) | Fotos: subida, galería, borrado directo de la familia | `media`, `media_child`, `media_deletion_request` (sin uso desde el 19/oct) |
| [`documents`](documents/README.md) | Documentos del niño y personas autorizadas | `child_document`, `authorized_pickup` |
| [`departures`](departures/README.md) | Pase de salida: hora de salida de cada niño y con quién se fue | `child_departure` |
| [`menus`](menus/README.md) | Menú del día: desayuno, comida y colación de cada día | `daily_menu` |
| [`messaging`](messaging/README.md) | Chat, avisos, notas | `conversation`, `message`, `conversation_read`, `announcement`, `parent_note` |

## Cómo agregar un módulo

1. Crea la carpeta con `__init__.py`, `models.py`, `schemas.py`, `service.py` y `router.py`.
2. Importa el modelo en `app/core/registry.py` para que Alembic lo vea.
3. `alembic revision --autogenerate -m "..."` y **revisa** la migración antes de aplicarla.
4. Registra el router en `app/main.py`.
5. Pruebas en `tests/`. Fuera de alcance responde 404 (`ApiError(404, ...)`), no 403.
6. Agrega un `README.md` aquí con el mismo formato.

## Pendiente transversal

JWT propio, auditoría (`audit_log`), límite de intentos y cabeceras ya están. Lo que falta: ver [`../../PENDIENTES.md`](../../PENDIENTES.md).
