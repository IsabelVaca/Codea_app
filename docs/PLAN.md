# Calendario de la entrega final (16 al 22 de octubre de 2026)

Este archivo **solo** reparte el tiempo hasta el jueves 22 a las 23:59. El *qué* y el *cómo* técnico ya están en los planes del equipo, y no se repiten aquí:

| Necesitas… | Ve a |
|---|---|
| Orden de trabajo por fases, pantallas que faltan, decisiones | [`plan/README.md`](plan/README.md) |
| Pantallas que existen y las que faltan | [`plan/01-app-pantallas.md`](plan/01-app-pantallas.md) |
| Cómo conectar la app a la API (cliente, sesión, errores, fotos) | [`plan/02-app-plan-tecnico.md`](plan/02-app-plan-tecnico.md) |
| Estado de la API | [`plan/03-api-estado.md`](plan/03-api-estado.md) |
| Login con JWT | [`plan/04-api-login-jwt.md`](plan/04-api-login-jwt.md) |
| Tareas de la API en orden | [`plan/05-api-plan.md`](plan/05-api-plan.md) |
| Convenciones de código | [`estilo.md`](estilo.md) |
| Endpoints | [`api/endpoints.md`](api/endpoints.md) |
| Requisitos del cliente y su estado | [`REQUISITOS.md`](REQUISITOS.md) |
| Rúbrica de la entrega | [`ENTREGA-3.md`](ENTREGA-3.md) |

Regla: todo dentro de [`MARCO-TECNICO.md`](MARCO-TECNICO.md); lo que dependa de [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md) no se empieza hasta que se apruebe.
`plan/` termina en el "mínimo funcional" (con la seguridad a fondo como fase aparte). **La rúbrica pide más**: seguridad (parte 2), sin conexión, pruebas y matriz, documentación, ética y presentación. Este calendario cubre esa diferencia.

## Frentes

| Frente | Contenido | Se apoya en |
|---|---|---|
| A. Backend | Login JWT, auditoría, resumen semanal, revisión de borrado de fotos, endurecimiento | `plan/04`, `plan/05` |
| B. App | Sesión y login, pantallas P0/P1 faltantes, sin conexión | `plan/01`, `plan/02` |
| C. Infra | Despliegue con HTTPS, APK firmado, respaldos, escaneo | `ENTREGA-3.md` partes 1 y 2 |
| D. Documentos | Matriz, manuales, ética, administración, guion y video | `ENTREGA-3.md` partes 3 a 7 |

## Calendario

**Congelamiento de funciones nuevas: lunes 19 por la noche.**

| Día | A. Backend | B. App | C. Infra | D. Documentos |
|---|---|---|---|---|
| Vie 16 y sáb 17 (hecho o en curso) | Avisos con `GET` | Avisos, rutinas, conexión a la API | — | Esta documentación |
| Dom 18 | ✅ **Login JWT hecho** (rama `feature/login-jwt`, sin subir) | ✅ **Sesión, Arranque, Iniciar sesión, Activar cuenta, Mi cuenta y código de acceso hechos** | Elegir dónde se despliega; keystore de firma | Matriz de trazabilidad (borrador); clasificación de datos |
| Lun 19 | Auditoría (`audit_log`); resumen semanal; rate limiting y cabeceras | Fase 2 y 3 de `plan/README.md` (docente y padre conectados, chat); Room para asistencia y reportes | Despliegue con HTTPS | Aviso de privacidad; criterios éticos y legales; manual de usuario |
| Mar 20 | **Congelamiento.** Escaneo ZAP, pruebas de IDOR e inyección; revisión de borrado de fotos | Fase 4 (admin); WorkManager para sincronizar; pruebas de interfaz | APK de release firmado; probarlo contra el servidor desplegado | Dilemas éticos; arquitectura (diagrama); manual de instalación; plan de implantación |
| Mié 21 | Corregir hallazgos; documentar la API | Corregir; medir fotos (RNF05) | Ensayo completo en dispositivo físico | Matriz final con resultados reales; administración final; guion de 15 min |
| Jue 22 | Solo críticos | Solo críticos | Video de respaldo de 3 min | PDF final; **entregar antes de las 18:00** |

## Qué se recorta (y se documenta como desviación)

Consulta con IA (CRF-04), Redis, Grafana, bloqueo de capturas y objetos perdidos. Si el tiempo falta: en la app, sin conexión se reduce a solo asistencia; los eventos y citas (CRF-03) quedan en versión mínima o fuera. Los pagos (CRF-01) quedaron fuera por decisión del equipo.

## Riesgos

1. El modo sin conexión es lo más caro; reducirlo a asistencia si el lunes no está.
2. El despliegue con HTTPS puede consumir un día.
3. La documentación se deja para el final y no alcanza; por eso el frente D arranca ya.
4. Todos modifican `CodeaNavHost.kt`; ramas cortas y merges frecuentes.

Las personas por frente no están asignadas.
