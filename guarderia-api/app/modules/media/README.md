# `media`: fotos y archivos

Prefijo `/media`. Tablas: `media` (clave en el bucket, tipo de contenido, tamaño, estado), `media_child` (qué niños aparecen en la foto), `media_deletion_request` (solicitudes de antes del 19/oct; sin uso).
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
| `DELETE /media/{id}` | padre | **"Borrar"** (19/oct): borra la foto del bucket y de las galerías al momento y deja sus reportes sin foto, **sin aprobación de nadie**. Basta con que salga un hijo suyo; si salen niños de otras familias, se borra también para ellas. Queda en la bitácora (`MEDIA_DELETED_BY_FAMILY`). `502 STORAGE_UNAVAILABLE` si el bucket falla (y entonces no se marca nada) |

## Estado

| Hecho | Pendiente |
|---|---|
| Subida con URL firmada, galería con paginación por cursor, **borrado directo por la familia** (la solicitud y la revisión del admin se quitaron el 19/oct) | Que la familia vea la respuesta a su solicitud (hoy solo el admin ve el estado) |
| | **Medir** la consulta de fotos en menos de 4 s (RNF05) |
| | Compresión y miniaturas del lado del servidor, si la app no comprime bien |
| | Enviar fotos desde el padre o la asistente por el chat (tarea abierta) |
| | Almacén: SeaweedFS en desarrollo; RustFS es el que nombra el marco ([decisión #9](../../../../docs/DECISIONES-PENDIENTES.md)) |
| | En producción, el bucket debe existir de antemano (`_ensure_bucket` solo corre con `ENV=dev`) |

Pruebas: `tests/test_media_documents.py` y `tests/test_media_deletion.py` (5: la familia borra directo, foto con niños de otras familias, solo el padre del niño puede, bucket caído no marca nada, las rutas de solicitud ya no existen).
