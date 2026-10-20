# Pruebas del backend

pytest contra un **Postgres real**: la base `guarderia_test`, en el mismo contenedor `db`, creada sola por `conftest.py`.

```bash
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt -r requirements-dev.txt
.venv/bin/pytest
# o dentro del contenedor api de desarrollo (así se corrieron el 20/oct). La imagen no trae tests/ ni pytest:
for f in tests pytest.ini requirements-dev.txt; do docker compose cp $f api:/code/; done
docker compose exec api pip install -r requirements-dev.txt
docker compose exec -e TEST_DB_HOST=db api pytest   # la base se llama db en la red de Compose
```

343 pruebas, todas en verde (conteo y corrida del 2026-10-20):

| Archivo | Pruebas | Qué cubre |
|---|---|---|
| `test_auth.py` | 33 | Token (falta, basura, vencido, de otra persona, refresh usado como access), `/me` por rol, login (bien, mal, sin activar, de baja), activar con código (uso único, vencido, reglas de contraseña, código nuevo, recuperación, texto copiado de WhatsApp con guion e invisibles, correo guardado limpio), refresh con rotación y reuso, logout, baja que corta el acceso, cambio de contraseña, cerrar sesiones y reenviar invitación (código nuevo; no a cuentas activas) |
| `test_admin.py` | 13 | Reglas de salas, ocupación y asistentes, bajas, inscripción de infantes con y sin tutor, alcance, padres, ligar más hijos a un padre (sin código nuevo y sin niños de baja), aviso |
| `test_admin_extras.py` | 17 | Centro (lectura, edición, validaciones, solo admin, auditoría de cambios), horario del centro (valores por omisión, validaciones, solo admin, auditoría), invitaciones, plantillas de rutina |
| `test_teacher_day.py` | 26 | Check-in a tiempo y tarde (contra la hora de entrada del centro, sin ubicación), día que no es de trabajo; aviso a coordinación; **salida** de la docente (necesita entrada, repetida no pisa la hora, por docente) y cierre automático a la hora configurada; arranque de la rutina; confirmar momentos; Mi sala y asistencia; niños con nota de la familia hoy y con resumen ya guardado; reportes para varios niños (RF-05) y no de otra sala; reglas de reportes |
| `test_arrival_time.py` | 8 | Hora de entrada del niño: se guarda al marcar presente, se conserva la primera, ausente la borra, la hora de la marca sin conexión (futura o sin zona horaria no), Mi sala y Hoy la muestran |
| `test_departures_and_health.py` | 10 | Salud del niño (la escribe la familia, privada, con límite, auditada sin el contenido); pase de salida (solo presentes, quién puede recogerlo, reglas, guarda el nombre, alcance por rol y sala); la familia ve a qué hora salió y con quién; la bandeja dice de quién es cada chat |
| `test_media_documents.py` | 4 | Reglas de subida, foto en un reporte que llega a la familia, galería paginada, documentos y personas autorizadas |
| `test_media_deletion.py` | 5 | RF-10 (desde el 19/oct): la familia borra directo, foto con niños de otras familias, solo el padre de un niño de la foto, bucket caído no marca nada, las rutas de solicitud ya no existen |
| `test_messaging.py` | 4 | Chat docente-familia y su alcance; foto en el chat y sus reglas |
| `test_announcement_images.py` | 6 | Avisos con imagen: texto e imagen, solo imagen, sin nada se rechaza, la imagen debe ser propia y estar lista, solo el admin publica, imagen borrada ya no sale |
| `test_daily_menu.py` | 8 | Menú del día: guardar y reemplazar, al menos una comida, todos lo ven y solo el admin escribe, rango, quitar, menú en el día del niño, auditoría |
| `test_parent_views.py` | 3 | Pantalla Hoy, día con rutina sin pase de lista cuenta como ausente, calendario |
| `test_weekly_summary.py` | 7 | RF-12: semana completa, día con rutina sin pase de lista es ausencia, cualquier fecha da el mismo lunes, ausencias de semanas pasadas, semana vacía o futura, alcance, fecha inválida |
| `test_invitations.py` | 7 | Enlace de invitación: `access_code.link` (y sin `PUBLIC_BASE_URL` no hay), `/invitar` no gasta el código, acepta el código sin guion y en minúsculas, no refleja nada raro; `assetlinks.json` vacío o con la huella |
| `test_security.py` | 23 | Límite de intentos (por correo, por IP, activación, ventana), cabeceras de seguridad, HSTS fuera de desarrollo, CORS cerrado, bitácora de auditoría (qué registra, quién la lee, que no guarda secretos, paginación) |
| `test_access_matrix.py` | 169 | Matriz de roles leída de las rutas reales (sin token `401`, rol no permitido `403`), IDOR por registro (padre, docente, chat, fotos), inyección en login, parámetros y texto guardado |

Con el login propio, `as_user(correo)` fabrica un access token real de las personas de `world` (que ya tienen contraseña, con el hash calculado una sola vez). Los access tokens de las pruebas duran 30 días para que las que mueven el reloj no los venzan; la de expiración fija su propio TTL.

Las reglas de `app/core/rules.py` usan los mismos casos que las pruebas de `domain/` de la app.

## Cómo escribir una prueba

- Usa los fixtures de `conftest.py`: `client` (cliente HTTP), `db`, `world` (datos de ejemplo ya creados), `storage` (almacén falso, sin S3 real) y `fixed_clock` (reloj fijo).
- Para actuar como alguien, `as_user(correo)` arma el encabezado `Authorization`.
- Para mover la hora, usa `set_clock(...)` del fixture `fixed_clock`.
- Una regla de alcance se prueba esperando **404**, no 403.

## Pendiente

- ~~Pruebas de seguridad para la rúbrica (IDOR, inyección, archivos subidos, tokens vencidos o manipulados)~~: hechas (`test_access_matrix.py`, `test_auth.py`, `test_security.py`).
- ~~Pruebas de los endpoints nuevos (auditoría, avisos, resumen semanal)~~: hechas. La revisión de borrado se quitó el 19/oct.
- Pruebas de tiempo para fotos (RNF05, menos de 4 s) y de carga.
- Alimentar la **matriz de trazabilidad** con el resultado de cada prueba ([`docs/ENTREGA-3.md`](../../docs/ENTREGA-3.md)).
