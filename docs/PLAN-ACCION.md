# Plan de acción hasta la entrega (18 al 22 de octubre de 2026)

**Puesto al día el 18/oct por la noche.** El trabajo de `integracion` y de las demás ramas ya está en `main`. Qué falta, por rol: [`FALTANTES.md`](FALTANTES.md). Cada bloque termina con pruebas en verde y un commit propio. Complementa a [`PLAN.md`](PLAN.md) (calendario) y [`REQUISITOS.md`](REQUISITOS.md) (qué cubre cada requisito).

## Reglas de este plan

- **Sin pagos.** El equipo decidió que no hay módulo de pagos (CRF-01 queda fuera de alcance; se documenta como desviación). La rama `feature/pagos` no se integra.
- **Nada de [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md)** se implementa mientras no esté aprobado: huella, Redis, Grafana, Cloudflare tunnel, etc. Se documentan como no cumplidos.
- Commits sin coautor de Claude.
- La base de Aiven es la de pruebas manuales (15 usuarios con contraseña `codea1234`). Las pruebas automáticas usan el Postgres local (`guarderia_test`), nunca Aiven.
- Congelamiento de funciones nuevas: **lunes 19 por la noche**. Martes y miércoles: solo corregir y documentar.

## Bloque 0: línea base (domingo)

- [x] Correr `pytest` de la API (318 en verde, 18/oct). Las pruebas JVM de la app (125) se contaron pero no se corrieron en la última revisión.
- [ ] Recorrer en el teléfono los tres roles (admin, docente, padre) con `adb` y anotar cada falla en [`RECORRIDO.md`](RECORRIDO.md). Se hizo una vez en la mañana del 18/oct; **falta repetirlo** con todo lo nuevo (Mi sala, pase de lista, pase de salida, Mi hijo, chat con fotos, invitación).
- [x] Arreglar lo que bloquee el recorrido (por ejemplo, el título "Inicia sesión" casi invisible).

## Bloque 1: terminar el mínimo funcional

- [x] `has_note_today` en `GET /classrooms/{id}/children` y marca en Mi sala (18/oct; falta verlo en el teléfono).
- [ ] Selector de hijo en las tabs del padre (HN4); la API ya devuelve la lista en `GET /me`.
- [x] Salida de la docente en el checador (la app prometía una salida automática que no existía; 18/oct).
- [x] Día del niño: la docente captura el resumen del día (P1 #12; 18/oct).
- [ ] Pantallas P1 que falten de [`plan/01-app-pantallas.md`](plan/01-app-pantallas.md): queda el selector de hijo. Agregar persona autorizada se hizo, pero en la app del **admin** (Ajustes → Personas autorizadas) por pedido del equipo.
- [ ] Documentos por defecto al inscribir a un infante. *Pierde sentido por ahora:* la pestaña del padre ya no sube documentos (18/oct); decidir con el equipo si se retoma.
- [x] Quitar de la app los repositorios y datos de ejemplo que ya no se usan (18/oct, limpieza `5b70069`).

## Bloque 2: requisitos que faltan en la API (y su pantalla mínima)

| Prioridad | Requisito | Qué |
|---|---|---|
| 1 ✅ | RF-14 | Tabla `audit_log` y escritura desde los `service.py` (logins, activaciones, códigos, altas, bajas y cambios). Hecho el 18/oct; falta la lectura de fotos y documentos. |
| 2 ✅ | Rúbrica, parte 2 | Límite de intentos y bloqueo temporal en login y activación, cabeceras de seguridad, CORS explícito. Hecho el 18/oct. |
| 3 ✅ | RF-10 | Se hizo la revisión del admin (18/oct) y **se reemplazó el 19/oct** por el borrado directo del padre, sin aprobación de nadie. |
| 4 ✅ | RF-03 | Hora de entrada y de salida en la asistencia. La **salida** del niño se hizo como pase de salida (18/oct) y la hora de **entrada** el 19/oct (`attendance.arrived_at`). Falta verlo en un teléfono. |
| 5 ✅ | RF-12 | Resumen semanal por niño (endpoint y vista del padre). Hecho el 18/oct (API con 6 pruebas y tarjeta en Calendario). |
| 6 ✅ | HN5 | `media_id` en los avisos (19/oct). Falta verlo en un teléfono. |
| 7 ✅ | HN3 | Menú del día: API, captura del admin, Hoy de la familia y Mi día de la docente (19/oct). Falta verlo en un teléfono. |
| 7 ✅ | Rúbrica, parte 3 | Pruebas explícitas de IDOR, inyección y matriz de roles. Hecho el 18/oct (los límites de subida ya estaban en `test_upload_rules`). |

## Bloque 3: sin conexión

- [x] Room como caché de Mi sala y cola de marcas de asistencia que se envía al recuperar la red (WorkManager). Alcance recortado a asistencia, como dice [`PLAN.md`](PLAN.md). Hecho el 18/oct y probado en el teléfono con la API apagada.
- [ ] Reportes sin conexión (no se hizo: llevan fotos y son más caros de sincronizar).

## Bloque 4: documentos de la entrega (carpeta `docs/entrega/`)

- [x] Matriz de trazabilidad honesta, a partir de [`REQUISITOS.md`](REQUISITOS.md) y con las pruebas reales (`entrega/matriz-trazabilidad.md`; repasarla al final).
- [x] Ética y legal: criterios verificables, aviso de privacidad, clasificación de datos por campo, tres dilemas éticos (borradores; el responsable y el canal del aviso quedan por completar).
- [x] Manual de usuario, manual de instalación y plan de implantación (puestos al día el 18/oct).
- [ ] **Arquitectura (diagrama de capas y relaciones): no existe.** Es parte de la rúbrica.
- [x] Administración: bitácora, desviaciones (D1 a D18), riesgos, distribución y retrospectiva (`entrega/administracion.md`; el equipo debe llenar 4.2 y 5.2).
- [ ] Guion de la presentación de 15 minutos.
- [x] Poner al día [`REQUISITOS.md`](REQUISITOS.md), [`ESTADO.md`](ESTADO.md), [`plan/`](plan/README.md) y [`api/endpoints.md`](api/endpoints.md) (hecho el 18/oct por la noche).

## Fuera de lo que puedo hacer solo (queda para el equipo)

- Despliegue público con HTTPS y respaldos (dependen de la decisión #7 y de una cuenta).
- APK de release firmado: necesita un keystore y sus contraseñas, que no debo inventar.
- Video de respaldo, captura del Planner y entrega en Canvas.
- Aprobar las decisiones de [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md).
