# Checklist del RETO_ENTREGA_3 (rúbrica final)

Entrega: **jueves 22 de octubre de 2026, 23:59**, en Canvas, un solo PDF por grupo. Vale 100 puntos.
Estado a 2026-10-16. Los entregables nuevos se guardan en `docs/entrega/` (carpeta por crear).

## Parte 1: Sistema funcionando

| Elemento | Estado | Pendiente |
|---|---|---|
| App Android (build release firmado) | 🟡 | Crear keystore, `signingConfig` y activar minificación. |
| Backend propio | 🟡 | Desplegarlo en una URL pública. |
| Integración completa | 🟡 | Probar de punta a punta los tres roles contra el backend desplegado. |
| Persistencia | ✅ | Postgres. |
| Cámara, multimedia, sensores | ✅ | Cámara, micrófono (dictado), GPS. |
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
| Pruebas del backend | ✅ 68 pruebas con pytest contra Postgres real (31 son de autenticación). |
| Pruebas JVM de la app | ✅ 33 pruebas (dominio, autenticador de tokens y sesión, con servidor falso). |
| Pruebas de interfaz | ❌ Solo `ExampleInstrumentedTest`. |
| Matriz de trazabilidad (ID, descripción, criterio, evidencia, resultado, estado) | ❌ Partir de [`REQUISITOS.md`](REQUISITOS.md). Debe ser honesta: lo no cumplido se marca como no cumplido. |

## Parte 4: Documentación

| Documento | Estado |
|---|---|
| Manual de usuario | ❌ |
| Manual de instalación | 🟡 (base en `guarderia-api/README.md` y `docs/GUIA-CONTINUAR.md`) |
| Arquitectura (capas y relaciones) | 🟡 (texto en los READMEs; falta diagrama) |
| Decisiones técnicas principales | 🟡 ([`MARCO-TECNICO.md`](MARCO-TECNICO.md) y [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md)) |
| Plan de implantación (canal, versión, configuración, despliegue, migración, rollback) | ❌ |

## Parte 5: Administración del proyecto

| Elemento | Estado |
|---|---|
| Backlog final y estado de requisitos | 🟡 ([`REQUISITOS.md`](REQUISITOS.md)) |
| Bitácora de hitos | ❌ |
| Desviaciones respecto al plan | ❌ (aquí van las excepciones al marco) |
| Riesgos | ❌ |
| Distribución del trabajo | ❌ (usar el historial de git y el tablero) |
| Retrospectiva | ❌ |

## Parte 6: Consideraciones éticas y legales

| Elemento | Estado |
|---|---|
| 1. Criterios éticos y legales como requisitos verificables | ❌ |
| 2. Aviso de privacidad (qué datos, para qué, dónde, con quién, cuánto tiempo, cómo ejercer derechos) | ❌ |
| 3. Clasificación de datos por campo (nivel, ubicación, protección, retención) | ❌ |
| 4. Dilemas éticos (al menos dos) | ❌ |

Punto de partida: la app maneja datos de menores, fotos y documentos. Las tablas del backend ya dan la lista de campos a clasificar.

## Parte 7: Presentación (15 min)

Guion de tiempos: 2 min problema y alcance · 5 min demo en dispositivo físico · 2 min pruebas y matriz · 2 min seguridad · 1 min capacidad diferenciadora · 3 min documentación e implantación y dilemas éticos.
Pendiente: guion, video de respaldo de ~3 min, APK de release.

## Contenido del PDF final (15 puntos)

1. Portada. 2. Resumen del alcance. 3. Arquitectura. 4. Matriz y evidencia de pruebas. 5. Evidencia de seguridad. 6. Manual de usuario. 7. Manual de instalación. 8. Plan de implantación. 9. Administración final. 10. Ética y legal. 11. Enlace al repositorio con tag de la versión entregada. 12. Enlace al APK de release. 13. Instrucciones para levantar el backend o enlace al servicio desplegado. 14. Enlace al video de respaldo. 15. Enlace al Planner y captura del tablero.
