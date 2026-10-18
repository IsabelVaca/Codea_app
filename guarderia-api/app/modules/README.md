# `app/modules/`

Un módulo por área. Cada uno separa:

- `router.py`: HTTP y qué rol puede llamar cada endpoint.
- `service.py`: reglas, consultas y permiso por registro. Aquí vive la lógica.
- `schemas.py`: modelos Pydantic de entrada y salida.
- `models.py`: tablas SQLAlchemy.

Se registran en `app/main.py`. Todo cuelga de `/api/v1`.

| Módulo | Para qué | Tablas |
|---|---|---|
| [`auth`](auth/README.md) | Perfil (`/me`), invitaciones | `app_user`, `invitation` |
| [`admins`](admins/README.md) | Datos del centro y del checador | `admin` |
| [`classrooms`](classrooms/README.md) | Salas, cupo y asistentes | `daycare_center`, `classroom` |
| [`teachers`](teachers/README.md) | Docentes y turnos | `teacher`, `shift` |
| [`children`](children/README.md) | Infantes | `child` |
| [`parents`](parents/README.md) | Padres y su relación con los hijos | `parent`, `parent_child_rel` |
| [`staff_attendance`](staff_attendance/README.md) | Checador de la docente | `staff_check_in`, `coordination_notice` |
| [`attendance`](attendance/README.md) | Asistencia del niño y calendario | `attendance` |
| [`routines`](routines/README.md) | Plantillas y rutina diaria por sala | `routine_template`, `routine_template_moment`, `routine_moment` |
| [`reports`](reports/README.md) | Reportes, resumen del día | `report`, `report_child`, `daily_summary` |
| [`media`](media/README.md) | Fotos: subida, galería, borrado | `media`, `media_child`, `media_deletion_request` |
| [`documents`](documents/README.md) | Documentos del niño y personas autorizadas | `child_document`, `authorized_pickup` |
| [`messaging`](messaging/README.md) | Chat, avisos, notas | `conversation`, `message`, `conversation_read`, `announcement`, `parent_note` |

## Cómo agregar un módulo

1. Crea la carpeta con `__init__.py`, `models.py`, `schemas.py`, `service.py` y `router.py`.
2. Importa el modelo en `app/core/registry.py` para que Alembic lo vea.
3. `alembic revision --autogenerate -m "..."` y **revisa** la migración antes de aplicarla.
4. Registra el router en `app/main.py`.
5. Pruebas en `tests/`. Fuera de alcance responde 404 (`ApiError(404, ...)`), no 403.
6. Agrega un `README.md` aquí con el mismo formato.

## Pendiente transversal

Auditoría (`audit_log`), JWT propio, rate limiting y cabeceras: ver [`../../PENDIENTES.md`](../../PENDIENTES.md).
