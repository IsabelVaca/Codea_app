# Backend: estado y pendientes

Foto a 2026-10-16 (leyendo el código; las pruebas no se corrieron para esta foto). Contexto general: [`../docs/ESTADO.md`](../docs/ESTADO.md). Marco técnico: [`../docs/MARCO-TECNICO.md`](../docs/MARCO-TECNICO.md).

## Qué hay

- 13 módulos en `app/modules/` (ver [`app/modules/README.md`](app/modules/README.md)), migraciones Alembic (2), seed con datos de ejemplo y 43 pruebas con pytest contra Postgres real.
- Errores uniformes (Problem Details con `code` estable), paginación por cursor, reloj reemplazable en pruebas, subida de archivos con URL firmada.
- Permisos por rol (`require_role`) y por registro (404 fuera de alcance).

## Pendiente, por prioridad

### Alta (rúbrica y requisitos)

| Qué | Requisito | Cómo, dentro del marco |
|---|---|---|
| ~~Auth propia con JWT~~ **Hecha** (rama `feature/login-jwt`) | RF-01, RNF01, RNF03 | Ver [`app/modules/auth/README.md`](app/modules/auth/README.md). Falta: límite de intentos, correo real con el código, auditoría de logins |
| **Bitácora de auditoría** | RF-14 | Tabla `audit_log` (quién, qué, sobre qué, cuándo) escrita desde los `service.py`. |
| **Avisos con imágenes** (`GET /announcements` ya existe) | HN5 | `media_id` en el aviso; marcar como leído en el servidor (`POST /announcements/{id}/read`), hoy el "leído" vive en el teléfono. |
| **Resumen semanal** | RF-12 | Endpoint que agregue `daily_summary` por semana y niño. |
| **Revisar solicitudes de borrado de fotos** | RF-10 | Listar y aprobar/rechazar `media_deletion_request`; borrar el objeto del bucket. |
| **Endurecimiento** | Rúbrica parte 2 | Rate limiting, bloqueo temporal en endpoints críticos, cabeceras (HSTS, `X-Content-Type-Options`, CSP), CORS explícito, sin secretos por defecto. |
| **Entrada y salida del niño** | RF-03 | `attendance` guarda solo `PRESENT`/`ABSENT`; agregar hora de entrada y de salida. |

### Media

| Qué | Requisito |
|---|---|
| ~~Módulo de pagos por infante~~ (backend hecho, falta la app) | CRF-01 |
| Eventos y citas (calendario) | CRF-03 |
| Menú del día | HN3 |
| Llegada de los padres | HN2 |
| Exportar reportes (PDF/CSV) | HN1 |
| Varios hijos por padre en las respuestas de la API | HN4 (el modelo ya lo permite) |
| Chat en tiempo real | SSE o WebSocket (el marco los permite); hoy se consulta con `?after=` |
| Correo real de invitación | Hoy solo queda en el log (`auth/service.py`) |

### Infra y operación

| Qué | Nota |
|---|---|
| Despliegue público con HTTPS | Cloudflare tunnel está en duda ([decisión #7](../docs/DECISIONES-PENDIENTES.md)) |
| Respaldos de la base | Tarea abierta |
| Monitoreo | `/health/live` y `/health/ready` ya existen; Grafana en duda ([decisión #6](../docs/DECISIONES-PENDIENTES.md)) |
| Redis | Fuera del marco ([decisión #5](../docs/DECISIONES-PENDIENTES.md)) |
| Documentar la API | Tarea abierta; el contrato vivo es `/docs`.  Referencia vigente: [`docs/api/endpoints.md`](../docs/api/endpoints.md) |
| Cambiar contraseñas de `.env.example` | `POSTGRES_PASSWORD=123456`, `S3_SECRET_KEY=cambia-esta-clave` |
| Almacén | SeaweedFS en lugar de RustFS ([decisión #9](../docs/DECISIONES-PENDIENTES.md)) |

### Deuda técnica

- `app/main.py` tiene un `_ensure_bucket` que reintenta 30 veces en arranque (solo con `ENV=dev`); en producción el bucket debe existir de antemano.
- `core/rules.py` duplica reglas de la app: si cambia una, cambiar las dos.
- No hay índices más allá de los de las migraciones; revisar con datos reales (fotos, mensajes).
- No hay pruebas de carga ni de tiempos (RNF05: consultas de fotos en menos de 4 s).
- No hay pruebas explícitas de IDOR, inyección ni límites de tamaño de subida más allá de `test_upload_rules`.
