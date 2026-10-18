# Pruebas del backend

pytest contra un **Postgres real**: la base `guarderia_test`, en el mismo contenedor `db`, creada sola por `conftest.py`.

```bash
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt -r requirements-dev.txt
.venv/bin/pytest
```

274 pruebas (conteo del 2026-10-18):

| Archivo | Pruebas | Qué cubre |
|---|---|---|
| `test_auth.py` | 31 | Token (falta, basura, vencido, de otra persona, refresh usado como access), `/me` por rol, login (bien, mal, sin activar, de baja), activar con código (uso único, vencido, reglas de contraseña, código nuevo, recuperación), refresh con rotación y reuso, logout, baja que corta el acceso, cambio de contraseña y cerrar sesiones |
| `test_admin.py` | 10 | Reglas de salas, ocupación y asistentes, bajas, inscripción de infantes con y sin tutor, alcance, padres, aviso |
| `test_admin_extras.py` | 13 | Centro (lectura, edición, validaciones, solo admin, auditoría de cambios), invitaciones, plantillas de rutina |
| `test_teacher_day.py` | 14 | Check-in a tiempo, tarde y fuera de radio; **salida** (necesita entrada, mismo radio, repetida no pisa la hora); arranque de la rutina; confirmar momentos; Mi sala y asistencia; niños con nota de la familia hoy; reglas de reportes |
| `test_media_documents.py` | 4 | Reglas de subida, foto en un reporte que llega a la familia, galería paginada, documentos y personas autorizadas |
| `test_messaging.py` | 2 | Chat docente-familia y su alcance |
| `test_parent_views.py` | 2 | Pantalla Hoy y calendario |
| `test_security.py` | 23 | Límite de intentos (por correo, por IP, activación, ventana), cabeceras de seguridad, CORS cerrado, bitácora de auditoría (qué registra, quién la lee, que no guarda secretos) |
| `test_media_deletion.py` | 9 | RF-10: el admin lista, aprueba (borra del bucket y de las galerías) y rechaza solicitudes de borrado; una sola decisión; falla del bucket; auditoría |
| `test_weekly_summary.py` | 6 | RF-12: semana completa, cualquier fecha da el mismo lunes, ausencias de semanas pasadas, semana vacía o futura, alcance, fecha inválida |
| `test_access_matrix.py` | 160 | Matriz de roles leída de las rutas reales (sin token `401`, rol no permitido `403`), IDOR por registro (padre, docente, chat, fotos), inyección en login, parámetros y texto guardado |

Con el login propio, `as_user(correo)` fabrica un access token real de las personas de `world` (que ya tienen contraseña, con el hash calculado una sola vez). Los access tokens de las pruebas duran 30 días para que las que mueven el reloj no los venzan; la de expiración fija su propio TTL.

Las reglas de `app/core/rules.py` usan los mismos casos que las pruebas de `domain/` de la app.

## Cómo escribir una prueba

- Usa los fixtures de `conftest.py`: `client` (cliente HTTP), `db`, `world` (datos de ejemplo ya creados), `storage` (almacén falso, sin S3 real) y `fixed_clock` (reloj fijo).
- Para actuar como alguien, `as_user(correo)` arma el encabezado `Authorization`.
- Para mover la hora, usa `set_clock(...)` del fixture `fixed_clock`.
- Una regla de alcance se prueba esperando **404**, no 403.

## Pendiente

- Pruebas de **seguridad** para la rúbrica: IDOR entre familias y entre salas, inyección, tamaño y tipo de archivos subidos, tokens vencidos o manipulados (cuando exista el JWT propio).
- Pruebas de los endpoints nuevos (auditoría, avisos, resumen semanal, revisión de borrado).
- Pruebas de tiempo para fotos (RNF05, menos de 4 s) y de carga.
- Alimentar la **matriz de trazabilidad** con el resultado de cada prueba ([`docs/ENTREGA-3.md`](../../docs/ENTREGA-3.md)).
