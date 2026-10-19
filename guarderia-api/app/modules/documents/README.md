# `documents`: documentos del niño y personas autorizadas

Prefijo `/children/{child_id}`. Tablas: `child_document` (título, vencimiento, `media_id`, fecha de subida), `authorized_pickup` (nombre, parentesco, teléfono, quién la agregó).

| Endpoint | Rol | Qué hace |
|---|---|---|
| `GET /children/{id}/documents` | admin o padre | Documentos pedidos, cada uno vigente o faltante |
| `PUT /children/{id}/documents/{doc}` | ver `router.py` | Sube o reemplaza un documento con el mismo permiso temporal que las fotos |
| `GET /children/{id}/authorized-pickups` | cualquiera con alcance | Personas autorizadas a recoger |
| `POST /children/{id}/authorized-pickups` | **solo admin** | Agrega una persona (queda en la auditoría) |
| `DELETE /children/{id}/authorized-pickups/{person}` | **solo admin** | Quita una persona (queda en la auditoría) |

## Estado

| Hecho | Pendiente |
|---|---|
| Documentos con vigencia, personas autorizadas | Que el admin defina qué documentos se piden (hoy salen del seed) |
| | Aviso cuando un documento esté por vencer |
| | Datos sensibles de menores: clasificarlos y definir cuánto tiempo se conservan (rúbrica, parte 6) |
| Las personas autorizadas las administra el admin; la docente las elige en el pase de salida ([`departures`](../departures/README.md)) | |

Pruebas: `tests/test_media_documents.py` (`test_documents_and_pickups`) y `tests/test_departures_and_health.py`.
