# `media`: fotos y archivos

Prefijo `/media`. Tablas: `media` (clave en el bucket, tipo de contenido, tamaño, estado), `media_child` (qué niños aparecen en la foto), `media_deletion_request` (solicitud de borrado de la familia).
Archivos y bucket: `core/storage.py`.

**Los archivos nunca pasan por la API:**
1. `POST /media/uploads` devuelve `url` y `fields` (permiso de 5 minutos).
2. La app manda un POST multipart a esa `url` con todos los `fields` y el archivo en el campo `file`.
3. `POST /media/{id}/complete` avisa que terminó. El `media_id` se liga a un reporte o a un documento.
4. Para leer, la API devuelve URLs firmadas de 10 minutos, firmadas con `S3_PUBLIC_ENDPOINT_URL` (lo que ve el teléfono; `http://10.0.2.2:9000` en el emulador).

| Endpoint | Rol | Qué hace |
|---|---|---|
| `POST /media/uploads` | cualquiera con cuenta | Pide permiso de subida. Valida tipo y tamaño (`test_upload_rules`) |
| `POST /media/{id}/complete` | cualquiera con cuenta | Marca el archivo como subido |
| `GET /media` | padre | Galería paginada y filtrada por categoría, solo donde aparece su hijo |
| `POST /media/{id}/deletion-request` | padre | "Pedir que se borre"; si ya la pidió, devuelve la misma con 200 |
| `GET /media/deletion-requests?status=` | admin | Las solicitudes (por omisión `PENDING`): quién la pidió, el motivo, de qué niños es la foto y una URL firmada para verla |
| `POST /media/deletion-requests/{id}/approve` | admin | Borra el archivo del bucket y marca la foto `DELETED` (sale de todas las galerías); los reportes conservan su texto sin foto; cierra las demás solicitudes de la misma foto. `409 REQUEST_ALREADY_DECIDED`; `502 STORAGE_UNAVAILABLE` si el bucket falla (y entonces no se marca nada) |
| `POST /media/deletion-requests/{id}/reject` | admin | La foto se queda; la solicitud queda resuelta. Ambas aceptan `{note?}` para explicar la decisión |

## Estado

| Hecho | Pendiente |
|---|---|
| Subida con URL firmada, galería con paginación por cursor, solicitud de borrado, **revisión por el admin** (listar, aprobar con borrado real del bucket, rechazar; auditada) | Que la familia vea la respuesta a su solicitud (hoy solo el admin ve el estado) |
| | **Medir** la consulta de fotos en menos de 4 s (RNF05) |
| | Compresión y miniaturas del lado del servidor, si la app no comprime bien |
| | Enviar fotos desde el padre o la asistente por el chat (tarea abierta) |
| | Fotos en avisos (HN5) |
| | Almacén: SeaweedFS en desarrollo; RustFS es el que nombra el marco ([decisión #9](../../../../docs/DECISIONES-PENDIENTES.md)) |
| | En producción, el bucket debe existir de antemano (`_ensure_bucket` solo corre con `ENV=dev`) |

Pruebas: `tests/test_media_documents.py` y `tests/test_media_deletion.py` (9: listado, solo admin, aprobar borra, rechazar conserva, una sola decisión, solicitudes repetidas, falla del bucket, auditoría).
