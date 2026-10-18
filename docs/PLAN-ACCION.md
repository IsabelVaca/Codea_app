# Plan de acción hasta la entrega (18 al 22 de octubre de 2026)

Se trabaja en la rama `integracion` (login JWT + chat sobre `main`). Cada bloque termina con pruebas en verde y un commit propio. Complementa a [`PLAN.md`](PLAN.md) (calendario) y [`REQUISITOS.md`](REQUISITOS.md) (qué cubre cada requisito).

## Reglas de este plan

- **Sin pagos.** El equipo decidió que no hay módulo de pagos (CRF-01 queda fuera de alcance; se documenta como desviación). La rama `feature/pagos` no se integra.
- **Nada de [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md)** se implementa mientras no esté aprobado: huella, Redis, Grafana, Cloudflare tunnel, Google Maps, etc. Se documentan como no cumplidos.
- Commits sin coautor de Claude. Sin `push` ni cambios a `main` hasta que el equipo revise `integracion`.
- La base de Aiven es la de pruebas manuales (15 usuarios con contraseña `codea1234`). Las pruebas automáticas usan el Postgres local (`guarderia_test`), nunca Aiven.
- Congelamiento de funciones nuevas: **lunes 19 por la noche**. Martes y miércoles: solo corregir y documentar.

## Bloque 0: línea base (domingo)

- [ ] Correr `pytest` de la API y las pruebas JVM de la app; anotar lo que falle.
- [ ] Recorrer en el teléfono los tres roles (admin, docente, padre) con `adb` y anotar cada falla en [`RECORRIDO.md`](RECORRIDO.md).
- [ ] Arreglar lo que bloquee el recorrido (por ejemplo, el título "Inicia sesión" casi invisible).

## Bloque 1: terminar el mínimo funcional

- [x] `has_note_today` en `GET /classrooms/{id}/children` y marca en Mi sala (18/oct; falta verlo en el teléfono).
- [ ] Selector de hijo en las tabs del padre (HN4); la API ya devuelve la lista en `GET /me`.
- [x] Salida de la docente en el checador (la app prometía una salida automática que no existía; 18/oct).
- [x] Día del niño: la docente captura el resumen del día (P1 #12; 18/oct).
- [ ] Pantallas P1 que falten de [`plan/01-app-pantallas.md`](plan/01-app-pantallas.md): selector de hijo, agregar persona autorizada (formulario).
- [ ] Documentos por defecto al inscribir a un infante.
- [ ] Quitar de la app los repositorios y datos de ejemplo que ya no se usan.

## Bloque 2: requisitos que faltan en la API (y su pantalla mínima)

| Prioridad | Requisito | Qué |
|---|---|---|
| 1 ✅ | RF-14 | Tabla `audit_log` y escritura desde los `service.py` (logins, activaciones, códigos, altas, bajas y cambios). Hecho el 18/oct; falta la lectura de fotos y documentos. |
| 2 ✅ | Rúbrica, parte 2 | Límite de intentos y bloqueo temporal en login y activación, cabeceras de seguridad, CORS explícito. Hecho el 18/oct. |
| 3 ✅ | RF-10 | Listar, aprobar y rechazar solicitudes de borrado de fotos; pantalla del admin. Hecho el 18/oct (API con 9 pruebas y pantalla en Centro). |
| 4 | RF-03 | Hora de entrada y de salida en la asistencia. |
| 5 ✅ | RF-12 | Resumen semanal por niño (endpoint y vista del padre). Hecho el 18/oct (API con 6 pruebas y tarjeta en Calendario). |
| 6 | HN5 | `media_id` en los avisos. |
| 7 ✅ | Rúbrica, parte 3 | Pruebas explícitas de IDOR, inyección y matriz de roles. Hecho el 18/oct (los límites de subida ya estaban en `test_upload_rules`). |

## Bloque 3: sin conexión

- [x] Room como caché de Mi sala y cola de marcas de asistencia que se envía al recuperar la red (WorkManager). Alcance recortado a asistencia, como dice [`PLAN.md`](PLAN.md). Hecho el 18/oct y probado en el teléfono con la API apagada.
- [ ] Reportes sin conexión (no se hizo: llevan fotos y son más caros de sincronizar).

## Bloque 4: documentos de la entrega (carpeta `docs/entrega/`)

- [ ] Matriz de trazabilidad honesta, a partir de [`REQUISITOS.md`](REQUISITOS.md) y con las pruebas reales.
- [ ] Ética y legal: criterios verificables, aviso de privacidad, clasificación de datos por campo, dos dilemas éticos.
- [ ] Manual de usuario, manual de instalación, arquitectura (diagrama), plan de implantación.
- [ ] Administración: bitácora, desviaciones, riesgos, distribución (con el historial de git) y retrospectiva.
- [ ] Guion de la presentación de 15 minutos.
- [ ] Poner al día [`REQUISITOS.md`](REQUISITOS.md), [`ESTADO.md`](ESTADO.md), [`plan/`](plan/README.md) y [`api/endpoints.md`](api/endpoints.md) (los pagos ya se quitaron de los documentos el 18/oct).

## Fuera de lo que puedo hacer solo (queda para el equipo)

- Despliegue público con HTTPS y respaldos (dependen de la decisión #7 y de una cuenta).
- APK de release firmado: necesita un keystore y sus contraseñas, que no debo inventar.
- Video de respaldo, captura del Planner y entrega en Canvas.
- Aprobar las decisiones de [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md).
