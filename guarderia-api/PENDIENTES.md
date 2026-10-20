# Backend: estado y pendientes

Foto a 2026-10-18 por la noche, sobre `main`. Las pruebas sí se corrieron para esta foto: `POSTGRES_PASSWORD=123456 .venv/bin/pytest` → **324 pasaron**. Contexto general: [`../docs/ESTADO.md`](../docs/ESTADO.md). Qué falta en toda la app: [`../docs/FALTANTES.md`](../docs/FALTANTES.md). Marco técnico: [`../docs/MARCO-TECNICO.md`](../docs/MARCO-TECNICO.md).

## Qué hay

- 15 módulos en `app/modules/` (ver [`app/modules/README.md`](app/modules/README.md)), 11 migraciones Alembic, 33 tablas, 84 endpoints (matriz en [`../docs/api/matriz-de-roles.md`](../docs/api/matriz-de-roles.md), comprobada por `tests/test_access_matrix.py`) y seed con datos de ejemplo.
- Errores uniformes (Problem Details con `code` estable), paginación por cursor, reloj reemplazable en pruebas, subida de archivos con URL firmada.
- Permisos por rol (`require_role`) y por registro (404 fuera de alcance).
- Login propio con JWT, límite de intentos, auditoría (`audit_log`), cabeceras de seguridad y CORS cerrado.
- Regla de asistencia: **sin registro de asistencia = ausente** (`attendance/queries.py::present_child_ids`); la usan Mi sala, los reportes (`409 CHILD_ABSENT`), el calendario del padre y el resumen semanal.
- Pase de salida (`departures`), datos de salud del niño que edita la familia (`PATCH /children/{id}/health`), foto en el chat y personas autorizadas administradas solo por el admin.
- Invitación por enlace: `access_code.link`, página pública `GET /invitar` y `GET /.well-known/assetlinks.json`.

## Hecho (ya no es pendiente)

| Qué | Requisito |
|---|---|
| Auth propia con JWT; límite de intentos; invitación por enlace de WhatsApp (el correo real ya no aplica) | RF-01, RNF01, RNF03 |
| Bitácora de auditoría (`modules/audit`) | RF-14 (falta registrar lectura de fotos y documentos, y `PATCH /center`) |
| Resumen semanal (`GET /children/{id}/weekly-summary`) | RF-12 |
| Revisión de solicitudes de borrado de fotos (`media/deletion-requests`) | RF-10 (ver abajo: el equipo pidió cambiarla) |
| Hora de **salida** del niño y con quién (pase de salida); la familia la ve como último evento de `GET /children/{id}/day` (`DEPARTURE`) | RF-03, solo la salida |
| Borrado directo de fotos por la familia (`DELETE /media/{id}`) | RF-10 (cambio del 18/oct) |
| Migraciones `a96b2c7d5f65` y `a07c3d8e6f76` aplicadas en Aiven (18/oct) | — |
| Endurecimiento: límite de intentos, cabeceras, CORS cerrado, pruebas de IDOR, inyección y matriz de roles | Rúbrica, partes 2 y 3 |

## Pendiente, por prioridad

### Alta (rúbrica, requisitos y pedidos del equipo)

| Qué | Requisito | Cómo, dentro del marco |
|---|---|---|
| **Foto con niños de varias familias** | RF-10 | `DELETE /media/{id}` ya existe (el padre borra directo si solo salen hijos suyos); con niños de otras familias responde `409 MEDIA_SHARED` y **nadie puede resolverlo desde la app**, porque la revisión del admin no tiene pantalla. Decidir con el equipo qué hacer (devolver la pantalla del admin, o que cada familia la oculte) y si se quitan los endpoints `…/deletion-requests`. |
| **Hora de entrada del niño** | RF-03 | `attendance` guarda solo `PRESENT`/`ABSENT`; agregar hora de entrada. La salida ya está |
| **Avisos con imágenes** | HN5 | `media_id` en `announcement` (hoy solo lo tiene `message`); marcar como leído en el servidor (`POST /announcements/{id}/read`); hoy el "leído" vive en el teléfono |
| **Despliegue con HTTPS y URL pública** | RNF04 y enlace de invitación | `PUBLIC_BASE_URL=https://api.codeaapp.site` ya va en `.env.prod.example`; falta `ANDROID_CERT_SHA256` (necesita el keystore de release) para que el enlace abra la app sin pasar por el navegador |

### Media

| Qué | Requisito |
|---|---|
| Eventos y citas (calendario) | CRF-03 |
| Menú del día | HN3 |
| Llegada de los padres | HN2 |
| Exportar reportes (PDF/CSV) | HN1 |
| Varios hijos por padre: `GET /me` devuelve la lista y la app ya tiene el selector (falta verlo en un teléfono) | HN4 |
| Chat en tiempo real | SSE o WebSocket (el marco los permite); hoy se consulta con `?after=` cada 10 s |
| Decidir si se quitan los turnos (`shift`), `POST /staff-attendance/notify-coordination` y `coordination_notice`, que la app ya no usa | — |
| Documentos al inscribir (la API de documentos sigue, pero la app ya no tiene pantalla) | — |

### Infra y operación

| Qué | Nota |
|---|---|
| Despliegue público con HTTPS | Configurado para `codeaapp.site` con túnel de Cloudflare ([decisión #7](../docs/DECISIONES-PENDIENTES.md), aprobada 19/oct; pasos en el [README](README.md#producción-en-codeaappsite)). Falta crear el túnel en el panel, levantarlo y verificarlo |
| Respaldos de la base | Tarea abierta |
| Monitoreo | `/health/live` y `/health/ready` ya existen; Grafana en duda ([decisión #6](../docs/DECISIONES-PENDIENTES.md)) |
| Redis | Fuera del marco ([decisión #5](../docs/DECISIONES-PENDIENTES.md)) |
| Documentar la API | El contrato vivo es `/docs`. Referencia vigente: [`docs/api/endpoints.md`](../docs/api/endpoints.md) |
| Cambiar contraseñas de `.env.example` | `POSTGRES_PASSWORD=123456`, `S3_SECRET_KEY=cambia-esta-clave` |
| Almacén | SeaweedFS en lugar de RustFS ([decisión #9](../docs/DECISIONES-PENDIENTES.md)) |

### Deuda técnica

- `app/main.py` tiene un `_ensure_bucket` que reintenta 30 veces en arranque (solo con `ENV=dev`); en producción el bucket debe existir de antemano.
- `core/rules.py` duplica reglas de la app: si cambia una, cambiar las dos.
- No hay índices más allá de los de las migraciones; revisar con datos reales (fotos, mensajes).
- No hay pruebas de carga ni de tiempos (RNF05: consultas de fotos en menos de 4 s).
- Hay pruebas de IDOR, de inyección y de límites de subida (`test_access_matrix.py`, `test_upload_rules`), pero no un escaneo automático (ZAP).
- Tras un proxy, el límite de intentos necesita la IP real del cliente (`uvicorn --proxy-headers`; ver `app/modules/auth/README.md`).
