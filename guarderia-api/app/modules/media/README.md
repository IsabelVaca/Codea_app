# `media`: fotos y archivos

Prefijo `/media`. Tablas: `media` (clave en el bucket, tipo de contenido, tamaño, estado), `media_child` (qué niños aparecen en la foto), `media_deletion_request` (solicitudes de antes del 19/oct; sin uso).
Archivos y bucket: `core/storage.py`.

**Los archivos nunca pasan por la API:**
1. `POST /media/uploads` devuelve `url` y `fields` (permiso de 5 minutos).
2. La app manda un POST multipart a esa `url` con todos los `fields` y el archivo en el campo `file`.
3. `POST /media/{id}/complete` avisa que terminó. El `media_id` se liga a un reporte o a un documento.
4. Para leer, la API devuelve URLs firmadas de 10 minutos, firmadas con `S3_PUBLIC_ENDPOINT_URL` (lo que ve el teléfono: `http://10.0.2.2:9000` en el emulador, `https://archivos.codeaapp.site` en el servidor compartido).

| Endpoint | Rol | Qué hace |
|---|---|---|
| `POST /media/uploads` | admin (fotos y documentos), docente (`PHOTO`), padre (`DOCUMENT`) | Pide permiso de subida. Valida tipo y tamaño (`test_upload_rules`). Otro rol u otro tipo: 403 `FORBIDDEN` |
| `POST /media/{id}/complete` | cualquiera con cuenta | Marca el archivo como subido |
| `GET /media?child_id=` | quien tenga alcance sobre el niño | Galería paginada y filtrada por categoría, solo donde aparece ese niño |
| `DELETE /media/{id}` | padre | **"Borrar"** (19/oct): borra la foto del bucket y de las galerías al momento y deja sus reportes sin foto, **sin aprobación de nadie**. Basta con que salga un hijo suyo; si salen niños de otras familias, se borra también para ellas. Queda en la bitácora (`MEDIA_DELETED_BY_FAMILY`). `502 STORAGE_UNAVAILABLE` si el bucket falla (y entonces no se marca nada) |

## Estado

| Hecho | Pendiente |
|---|---|
| Subida con URL firmada, galería con paginación por cursor, **borrado directo por la familia** (la solicitud y la revisión del admin se quitaron el 19/oct) | ~~Que la familia vea la respuesta a su solicitud~~ (ya no hay solicitudes) |
| | **Medir** la consulta de fotos en menos de 4 s (RNF05) |
| | Compresión y miniaturas del lado del servidor, si la app no comprime bien |
| Fotos en el chat, de la docente y del padre (el padre la sube como `DOCUMENT`; ver [`messaging`](../messaging/README.md)) | |
| | Almacén: SeaweedFS en desarrollo y en producción (`archivos.codeaapp.site`); RustFS es el que nombra el marco ([decisión #9](../../../../docs/DECISIONES-PENDIENTES.md), sin aprobar) |
| | En producción, el bucket debe existir de antemano (`_ensure_bucket` solo corre con `ENV=dev`) |

Pruebas: `tests/test_media_documents.py` y `tests/test_media_deletion.py` (5: la familia borra directo, foto con niños de otras familias, solo el padre del niño puede, bucket caído no marca nada, las rutas de solicitud ya no existen).
