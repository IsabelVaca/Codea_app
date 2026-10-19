# Checklist del RETO_ENTREGA_3 (rúbrica final)

Entrega: **jueves 22 de octubre de 2026, 23:59**, en Canvas, un solo PDF por grupo. Vale 100 puntos.
Estado a 2026-10-18 por la noche. Los entregables nuevos están en `docs/entrega/` (ya existen ocho documentos, con partes **[completar por el equipo]**).

## Parte 1: Sistema funcionando

| Elemento | Estado | Pendiente |
|---|---|---|
| App Android (build release firmado) | 🟡 | Crear keystore, `signingConfig` y activar minificación. |
| Backend propio | 🟡 | Desplegarlo en una URL pública. |
| Integración completa | 🟡 | Probar de punta a punta los tres roles contra el backend desplegado. |
| Persistencia | ✅ | Postgres. |
| Cámara, multimedia, sensores | ✅ | Cámara (reportes y chat), selector de fotos, micrófono (dictado) y marcador del teléfono (botón Llamar). **Ya no usa GPS** (el checador se simplificó el 18/oct). |
| Operación sin conexión | 🟡 | **Asistencia de Mi sala** sin conexión: Room (cola de marcas y caché de la última Mi sala). Probado en el teléfono con la API apagada. Lo demás (reportes, fotos, chat, check-in) requiere conexión. |
| Sincronización posterior | 🟡 | WorkManager manda la cola solo al volver la red (reintento con espera creciente, colapsa marcas repetidas). Probado de punta a punta; solo cubre la asistencia. |
| Datos realistas | ✅ | `python -m app.seed`. |

## Parte 2: Seguridad

| Elemento | Estado | Pendiente |
|---|---|---|
| Sesiones y tokens | ✅ | JWT de 5 min + refresh rotado (hash SHA-256, detección de reuso), `token_version` para cortar acceso al instante, tokens cifrados con Keystore en la app. Falta aprobar formalmente la [decisión #1](DECISIONES-PENDIENTES.md). |
| Validación de permisos, anti-IDOR | ✅ | RBAC + 404 fuera de alcance. [`api/matriz-de-roles.md`](api/matriz-de-roles.md) y `tests/test_access_matrix.py` (matriz de roles, IDOR de padre, docente, chat y fotos). |
| Consultas parametrizadas | ✅ | SQLAlchemy ORM; pruebas de inyección en login, filtros y texto guardado (`test_access_matrix.py`). |
| Rate limiting y bloqueo | ✅ | 5 fallos por correo y 30 por IP en 15 min en `/auth/login` y `/auth/activate`; `429` con `retry_after_s`. Pruebas en `tests/test_security.py`. Pendiente: límite en endpoints que no son de entrada. |
| HTTPS, HSTS, cabeceras | 🟡 | Cabeceras listas (`nosniff`, `X-Frame-Options`, CSP, `Referrer-Policy`, `Permissions-Policy`, `Cache-Control: no-store`, CORS cerrado) y HSTS fuera de `ENV=dev`. **Falta HTTPS** (despliegue público). |
| Secretos fuera del código | 🟡 | `.env` está ignorado; cambiar valores por defecto. |
| Escaneo dinámico (OWASP ZAP) | ❌ | Correrlo y documentar hallazgos y mitigaciones. |

## Parte 3: Pruebas y matriz de trazabilidad

| Elemento | Estado |
|---|---|
| Pruebas del backend | ✅ 318 pruebas con pytest contra Postgres real (corridas el 18/oct). |
| Pruebas JVM de la app | ✅ 121 pruebas (dominio, reglas de Mi sala y del pase de salida, invitación, sesión con servidor falso); contadas, no corridas en esta revisión. |
| Pruebas de interfaz | ❌ Solo `ExampleInstrumentedTest`. |
| Matriz de trazabilidad (ID, descripción, criterio, evidencia, resultado, estado) | 🟡 [`entrega/matriz-trazabilidad.md`](entrega/matriz-trazabilidad.md) existe; hay que repasarla contra el estado final de [`REQUISITOS.md`](REQUISITOS.md). |

## Parte 4: Documentación

| Documento | Estado |
|---|---|
| Manual de usuario | 🟡 [`entrega/manual-usuario.md`](entrega/manual-usuario.md), al día con `main` (escrito leyendo el código; falta cotejarlo en el teléfono) |
| Manual de instalación | 🟡 [`entrega/manual-instalacion.md`](entrega/manual-instalacion.md) |
| Arquitectura (capas y relaciones) | ❌ **No hay documento ni diagrama** en `docs/entrega/`; solo texto repartido en los READMEs y [`MARCO-TECNICO.md`](MARCO-TECNICO.md) |
| Decisiones técnicas principales | 🟡 ([`MARCO-TECNICO.md`](MARCO-TECNICO.md) y [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md)) |
| Plan de implantación (canal, versión, configuración, despliegue, migración, rollback) | 🟡 [`entrega/plan-implantacion.md`](entrega/plan-implantacion.md) (con huecos **[completar por el equipo]**) |

## Parte 5: Administración del proyecto

| Elemento | Estado |
|---|---|
| Backlog final y estado de requisitos | 🟡 ([`REQUISITOS.md`](REQUISITOS.md)) |
| Bitácora de hitos | 🟡 [`entrega/administracion.md`](entrega/administracion.md), sección 1 |
| Desviaciones respecto al plan | 🟡 [`entrega/administracion.md`](entrega/administracion.md), sección 2 (D1 a D18 y las excepciones al marco) |
| Riesgos | 🟡 [`entrega/administracion.md`](entrega/administracion.md), sección 3 |
| Distribución del trabajo | 🟡 sección 4 del mismo documento (el equipo debe llenar 4.2) |
| Retrospectiva | 🟡 sección 5 del mismo documento (el equipo debe llenar 5.2) |

## Parte 6: Consideraciones éticas y legales

| Elemento | Estado |
|---|---|
| 1. Criterios éticos y legales como requisitos verificables | 🟡 [`entrega/etica-legal.md`](entrega/etica-legal.md) |
| 2. Aviso de privacidad (qué datos, para qué, dónde, con quién, cuánto tiempo, cómo ejercer derechos) | 🟡 [`entrega/aviso-privacidad.md`](entrega/aviso-privacidad.md): borrador, con responsable y canal por completar |
| 3. Clasificación de datos por campo (nivel, ubicación, protección, retención) | 🟡 [`entrega/clasificacion-datos.md`](entrega/clasificacion-datos.md), con las 33 tablas |
| 4. Dilemas éticos (al menos dos) | 🟡 tres en [`entrega/etica-legal.md`](entrega/etica-legal.md); el dilema 1 (fotos y derecho al borrado) hay que reescribirlo si el padre borra directo |

Punto de partida: la app maneja datos de menores, fotos y documentos. Las tablas del backend ya dan la lista de campos a clasificar.

## Parte 7: Presentación (15 min)

Guion de tiempos: 2 min problema y alcance · 5 min demo en dispositivo físico · 2 min pruebas y matriz · 2 min seguridad · 1 min capacidad diferenciadora · 3 min documentación e implantación y dilemas éticos.
Pendiente: guion, video de respaldo de ~3 min, APK de release.

## Contenido del PDF final (15 puntos)

1. Portada. 2. Resumen del alcance. 3. Arquitectura. 4. Matriz y evidencia de pruebas. 5. Evidencia de seguridad. 6. Manual de usuario. 7. Manual de instalación. 8. Plan de implantación. 9. Administración final. 10. Ética y legal. 11. Enlace al repositorio con tag de la versión entregada. 12. Enlace al APK de release. 13. Instrucciones para levantar el backend o enlace al servicio desplegado. 14. Enlace al video de respaldo. 15. Enlace al Planner y captura del tablero.
