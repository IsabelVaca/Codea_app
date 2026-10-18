# `messaging`: chat, avisos y notas

Tablas: `conversation` (una por niño), `message`, `conversation_read`, `announcement` (texto, sala opcional, autor), `parent_note` (nota del padre a la asistente para hoy).

| Endpoint | Rol | Qué hace |
|---|---|---|
| `GET /conversations` | cualquiera con cuenta | Conversaciones con conteo de no leídos |
| `GET /conversations/{id}/messages` | quien participe | Mensajes paginados; con `?after=<id>` trae solo los nuevos |
| `POST /conversations/{id}/messages` | quien participe | Envía. Si el `client_id` ya se usó, devuelve el existente con 200 (reintentos seguros) |
| `POST /conversations/{id}/read` | cualquiera con cuenta | Marca como leída |
| `POST /children/{id}/notes` | padre | Nota a la asistente para hoy |
| `GET /announcements` | cualquiera con cuenta | Avisos con alcance por rol |
| `POST /announcements` | admin | Aviso. Sin `classroom_id` es para todo el centro; con él, solo para esa sala |

El `client_id` del mensaje hace que enviar sin conexión y reintentar después no duplique mensajes: sirve para la futura cola de sincronización.

## Estado

| Hecho | Pendiente |
|---|---|
| Chat docente-familia con no leídos, notas y publicación de avisos | Marcar avisos como leídos en el servidor (hoy el "leído" vive en el teléfono) |
| | **Avisos con imágenes** (HN5, `media_id` en `announcement`) y **historial** para el admin |
| | **Mensajes en tiempo real** ("Agregar funcionalidad mensajes tiempo real", tarea abierta): hoy se consulta con `?after=`; el marco permite SSE o WebSocket, no Firebase |
| | **Mandar fotos** por el chat (rol padre y asistente, tarea abierta) |
| | **Notificaciones**: sin Firebase; WorkManager + notificaciones locales en la app |
| | Chat del lado del padre en la app (la API ya lo permite) |

Pruebas: `tests/test_messaging.py`, `tests/test_admin.py` (`test_announcement`).
