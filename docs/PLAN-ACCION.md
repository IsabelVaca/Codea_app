# Plan de acción hasta la entrega (18 al 22 de octubre de 2026)

**Puesto al día el 20/oct.** Todo lo integrado está en `main` (hasta el PR #17). Qué falta, por rol: [`FALTANTES.md`](FALTANTES.md). Cada bloque termina con pruebas en verde y un commit propio. Complementa a [`PLAN.md`](PLAN.md) (calendario) y [`REQUISITOS.md`](REQUISITOS.md) (qué cubre cada requisito).

## Reglas de este plan

- **Sin pagos.** El equipo decidió que no hay módulo de pagos (CRF-01 queda fuera de alcance; se documenta como desviación). La rama `feature/pagos` no se integra.
- **Nada de [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md)** se implementa mientras no esté aprobado: huella, Redis, Grafana, Cloudflare tunnel, etc. Se documentan como no cumplidos.
- Commits sin coautor de Claude.
- La base de producción es `codea_prod` en Aiven (la del servidor compartido, desde el 19/oct). Las pruebas automáticas usan el Postgres local (`guarderia_test`), nunca Aiven, y el seed nunca se corre contra producción sin permiso del equipo ([incidente del 19/oct](INCIDENTE-2026-10-19.md)).
- Congelamiento de funciones nuevas: **lunes 19 por la noche**. Martes y miércoles: solo corregir y documentar.

## Bloque 0: línea base (domingo)

- [x] Correr `pytest` de la API (343 en verde, 20/oct) y las pruebas JVM de la app (140 en verde, 20/oct).
- [ ] Recorrer en el teléfono los tres roles (admin, docente, padre) y anotar cada falla en [`RECORRIDO.md`](RECORRIDO.md). Se hizo el 18/oct y el 19/oct (pase de lista, pase de salida, Mi hijo, menú, avisos con imagen, borrado de fotos); **falta uno final** contra el servidor compartido con la versión que se entrega (rediseños y caché del 20/oct, selección de varios niños, cámara, selector de hijo).
- [x] Arreglar lo que bloquee el recorrido (por ejemplo, el título "Inicia sesión" casi invisible).

## Bloque 1: terminar el mínimo funcional

- [x] `has_note_today` en `GET /classrooms/{id}/children` y marca en Mi sala (18/oct; falta verlo en el teléfono).
- [x] Selector de hijo en las tabs del padre (HN4; 18/oct, falta probarlo con dos hijos).
- [x] Salida de la docente en el checador (la app prometía una salida automática que no existía; 18/oct).
- [x] Día del niño: la docente captura el resumen del día (P1 #12; 18/oct).
- [x] Pantallas P1 de [`plan/01-app-pantallas.md`](plan/01-app-pantallas.md), con el selector de hijo. Agregar persona autorizada se hizo, pero en la app del **admin** (Ajustes → Personas autorizadas) por pedido del equipo.
- [ ] Documentos por defecto al inscribir a un infante. *Pierde sentido por ahora:* la pestaña del padre ya no sube documentos (18/oct); decidir con el equipo si se retoma.
- [x] Quitar de la app los repositorios y datos de ejemplo que ya no se usan (18/oct, limpieza `5b70069`).

## Bloque 2: requisitos que faltan en la API (y su pantalla mínima)

| Prioridad | Requisito | Qué |
|---|---|---|
| 1 ✅ | RF-14 | Tabla `audit_log` y escritura desde los `service.py` (logins, activaciones, códigos, altas, bajas y cambios). Hecho el 18/oct; falta la lectura de fotos y documentos. |
| 2 ✅ | Rúbrica, parte 2 | Límite de intentos y bloqueo temporal en login y activación, cabeceras de seguridad, CORS explícito. Hecho el 18/oct. |
| 3 ✅ | RF-10 | Se hizo la revisión del admin (18/oct) y **se reemplazó el 19/oct** por el borrado directo del padre, sin aprobación de nadie. |
| 4 ✅ | RF-03 | Hora de entrada y de salida en la asistencia. La **salida** del niño se hizo como pase de salida (18/oct) y la hora de **entrada** el 19/oct (`attendance.arrived_at`). Probado de punta a punta en el teléfono el 19/oct. |
| 5 ✅ | RF-12 | Resumen semanal por niño (endpoint y vista del padre). Hecho el 18/oct (API con 6 pruebas y tarjeta en Calendario). |
| 6 ✅ | HN5 | `media_id` en los avisos (19/oct). Probado en el teléfono el 19/oct. |
| 7 ✅ | HN3 | Menú del día: API, captura del admin, y Hoy de la familia (el 19/oct también se mostraba en Mi día de la docente; el 20/oct se quitó de ahí). Probado en el teléfono el 19/oct. |
| 7 ✅ | Rúbrica, parte 3 | Pruebas explícitas de IDOR, inyección y matriz de roles. Hecho el 18/oct (los límites de subida ya estaban en `test_upload_rules`). |

## Bloque 3: sin conexión

- [x] Room como caché de Mi sala y cola de marcas de asistencia que se envía al recuperar la red (WorkManager). Alcance recortado a asistencia, como dice [`PLAN.md`](PLAN.md). Hecho el 18/oct y probado en el teléfono con la API apagada.
- [x] Ver sin red lo último que se cargó, en los tres roles: Room guarda la última respuesta de la API (`cached_response`, 20/oct).
- [ ] Reportes sin conexión (no se hizo: llevan fotos y son más caros de sincronizar).

## Bloque 4: documentos de la entrega (carpeta `docs/entrega/`)

- [x] Matriz de trazabilidad honesta, a partir de [`REQUISITOS.md`](REQUISITOS.md) y con las pruebas reales (`entrega/matriz-trazabilidad.md`; repasarla al final).
- [x] Ética y legal: criterios verificables, aviso de privacidad, clasificación de datos por campo, tres dilemas éticos (borradores; el responsable y el canal del aviso quedan por completar).
- [x] Manual de usuario, manual de instalación y plan de implantación (puestos al día el 18/oct).
- [ ] **Arquitectura (diagrama de capas y relaciones): no existe.** Es parte de la rúbrica.
- [x] Administración: bitácora, desviaciones (D1 a D18), riesgos, distribución y retrospectiva (`entrega/administracion.md`; el equipo debe llenar 4.2 y 5.2).
- [ ] Guion de la presentación de 15 minutos.
- [x] Poner al día [`REQUISITOS.md`](REQUISITOS.md), [`ESTADO.md`](ESTADO.md), [`FALTANTES.md`](FALTANTES.md), [`plan/`](plan/README.md) y [`api/endpoints.md`](api/endpoints.md) (18/oct por la noche y de nuevo el 20/oct).
- [ ] Escaneo dinámico con OWASP ZAP contra `api.codeaapp.site` y sus hallazgos.

## Fuera de lo que puedo hacer solo (queda para el equipo)

- ~~Despliegue público con HTTPS y respaldos~~: hechos. Servidor compartido con HTTPS desde el 19/oct (decisión #7) y respaldo diario de la base y las fotos desde el 20/oct (`guarderia-api/README.md`, «Respaldos»). Queda el candado al `seed_demo.py --borrar-todo`.
- APK de release firmado: necesita un keystore y sus contraseñas, que no debo inventar.
- Video de respaldo, captura del Planner y entrega en Canvas.
- Aprobar las decisiones de [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md).
