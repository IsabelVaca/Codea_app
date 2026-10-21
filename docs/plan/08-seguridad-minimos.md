# Mínimos de seguridad de la API (RETO_ENTREGA_3)

Checklist de trabajo para la rúbrica de seguridad de la entrega final (**jueves 22 de octubre de 2026, 23:59**), la misma que resume la tabla "Parte 2: Seguridad" de [`../ENTREGA-3.md`](../ENTREGA-3.md). Se va punto por punto, no todos a la vez: cada fila se cierra (código + pruebas en verde + esta tabla actualizada) antes de pasar a la siguiente.

Antes de tocar nada se revisó el código real de `guarderia-api/app` para no reimplementar lo que ya existe. La mayoría de la rúbrica ya estaba hecha y probada; lo que falta de verdad es más acotado de lo que parece a primera vista.

## Estado por punto de la rúbrica

### Controles, protecciones y contramedidas en la API

| # | Punto | Estado | Evidencia | Pendiente |
|---|---|---|---|---|
| 1 | Sesiones e identidad (nada en texto plano) | ✅ hecho | JWT HS256 de 5 min (`app/core/tokens.py:19-51`), secret por variable de entorno sin valor por omisión (`app/core/config.py:15`), contraseñas con Argon2id vía `pwdlib` (`app/core/passwords.py`), refresh tokens rotados y hasheados SHA-256 con detección de reuso (`app/modules/auth/service.py:228-258`), `token_version` para cortar sesiones al instante | Falta aprobar formalmente la [decisión #1](../DECISIONES-PENDIENTES.md) (ya implementada) |
| 2 | Autorización estricta por rol / anti-IDOR (BOLA) | ✅ hecho | `require_role()` + `can_see_child()` / `get_child_in_scope()` / `ensure_classroom_access()`, 404 fuera de alcance (`app/core/security.py:82-131`), usado en children/messaging/media/attendance/departures/reports. Sin huecos de IDOR encontrados | Ninguno |
| 3 | Validaciones extra antes de modificar/eliminar | ✅ en su mayoría | No hay borrado físico (solo `deactivate`/`reactivate` tras `AdminUser`); `DELETE /media/{id}` valida que el recurso sea del propio usuario | Ninguno |
| 4 | Consultas parametrizadas / sanitización de entrada | ✅ SQLi cubierto / ⚠️ falta lo de esquema | SQLAlchemy ORM en todo el código (el único `text()` con f-string es un script de seed de desarrollo con nombres de tabla fijos, no es endpoint) | Los esquemas Pydantic de entrada no usan `extra="forbid"` ni límites de longitud; la validación de negocio vive en `app/core/rules.py` pero no a nivel de esquema |
| 5 | Rate limiting / bloqueo en endpoints críticos | 🟡 parcial | Throttling propio en Postgres para `/auth/login` y `/auth/activate` (`app/modules/auth/throttle.py`: 5 fallos/correo, 30/IP, ventana 15 min; pruebas en `tests/test_security.py`) | El propio equipo ya anotó "pendiente: límite en endpoints que no son de entrada"; `/auth/refresh` no tiene ningún límite y el resto de la API tampoco |
| 6 | Tráfico cifrado + cabeceras de seguridad | ✅ hecho y confirmado en producción | HSTS, CSP, `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`, `Cache-Control: no-store`, CORS cerrado por omisión — middleware en `app/main.py:74-97`, verificado contra `api.codeaapp.site` el 20/oct | Ninguno |

### Infraestructura y despliegue seguro

| # | Punto | Estado | Evidencia | Pendiente |
|---|---|---|---|---|
| 7 | Contenedores con buenas prácticas | 🟡 parcial | Secretos vía variables de entorno (`.env` fuera de git; `.env.example`/`.env.prod.example` con placeholders vacíos) | `guarderia-api/Dockerfile` no tiene `USER` (corre como root) y no es multi-stage; `.dockerignore` existe pero es mínimo |
| 8 | Túnel seguro para exponer el servicio | ✅ hecho y aprobado | Cloudflare Tunnel en `docker-compose.prod.yml`, [decisión #7](../DECISIONES-PENDIENTES.md), en producción desde el 19/oct | Ninguno |

### Validación de seguridad

| # | Punto | Estado | Evidencia | Pendiente |
|---|---|---|---|---|
| 9 | Pruebas que rechazan ataques | ✅ ya existen | Inyección SQL (`tests/test_access_matrix.py`), tokens expirados/manipulados y reuso de refresh (`tests/test_auth.py`), IDOR (`tests/test_access_matrix.py`), rate limit/bloqueo (`tests/test_security.py`) | Faltan las pruebas de los puntos 5 y 7 una vez que se implementen |
| 10 | Escaneo dinámico (OWASP ZAP) | ❌ pendiente | — | Correrlo contra la API **local** (nunca contra `api.codeaapp.site`: la propia rúbrica dice que el túnel es solo para demostrar acceso por internet, no para pruebas de seguridad) y documentar hallazgos + mitigación |

## Orden de trabajo

- [ ] 1. Rate limiting ampliado: extender `app/modules/auth/throttle.py` o agregar un límite genérico por IP para toda la API (middleware en `app/main.py`), cubriendo explícitamente `/auth/refresh`. Nuevas settings en `app/core/config.py`. Pruebas nuevas en `tests/test_security.py`.
- [ ] 2. Validación estricta de entrada: esquema base con `extra="forbid"` en `app/core/schemas.py`, aplicado primero a `auth`, `children` y `messaging` (los más sensibles), con límites de longitud. Ampliable a los demás módulos después. Pruebas de "campo extra rechazado" y "texto demasiado largo rechazado".
- [ ] 3. `Dockerfile` sin root: agregar `USER` no privilegiado, revisar si conviene multi-stage, reforzar `.dockerignore`.
- [ ] 4. Revisar `.env.example` / `.env.prod.example`: placeholders claros, nada real.
- [ ] 5. Correr toda la suite (343 pruebas de la API + las nuevas) contra Postgres local para confirmar verde.
- [ ] 6. Dejar documentada en esta tabla la evidencia de los puntos ya ✅, lista para citar en la entrega.
- [ ] 7. Escaneo DAST con OWASP ZAP contra la API corriendo localmente vía `docker compose` (Postgres de prueba, igual que pytest). Imagen `zaproxy/zap-stable`, baseline scan o `zap-api-scan.py` contra `/openapi.json`. Documentar hallazgos y su justificación/mitigación.
- [ ] 8. Cerrar documentación: actualizar `../ENTREGA-3.md` (Parte 2 y 3) y `../FALTANTES.md` (quitar ZAP de la sección 5 y del "orden sugerido"), marcar esta tabla como completa.

## Archivos críticos

- `guarderia-api/app/modules/auth/throttle.py`, `app/main.py`, `app/core/config.py` — rate limiting.
- `guarderia-api/app/core/schemas.py` + esquemas de `auth/`, `children/`, `messaging/` — validación estricta.
- `guarderia-api/Dockerfile`, `.dockerignore` — contenedor.
- `guarderia-api/.env.example`, `.env.prod.example`.
- `guarderia-api/tests/test_security.py` y los módulos de prueba que correspondan a cada cambio.
- `../ENTREGA-3.md`, `../FALTANTES.md` (actualizar al cerrar).

## Cómo se verifica

- `cd guarderia-api && POSTGRES_PASSWORD=<la del contenedor local> .venv/bin/pytest` → debe seguir en verde después de cada punto de código.
- `curl -i` contra la instancia local para confirmar el `429` con `retry_after_s` y las cabeceras de seguridad.
- ZAP: `docker compose up -d` localmente (DB de prueba, no Aiven), correr el contenedor de ZAP contra `http://localhost:8000`, revisar el reporte generado y anexarlo a la documentación de entrega.
