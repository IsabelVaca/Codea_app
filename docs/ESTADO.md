# Estado del proyecto

Foto a **2026-10-20**, sobre `main` (después de los PR #11 a #17) y el servidor compartido. Entrega final: **jueves 22 de octubre de 2026, 23:59** (RETO_ENTREGA_3). El congelamiento de funciones nuevas fue el **lunes 19 por la noche**; el 20/oct se hicieron ajustes de diseño, la caché en Room, los respaldos y el almacén.
Método: lectura del código, `pytest` de la API (**343 pruebas en verde**, corridas el 20/oct) y las pruebas JVM de la app (**140**, corridas con Gradle el 20/oct: 0 fallas). Lo que se ha visto en un teléfono o emulador se dice en [`FALTANTES.md`](FALTANTES.md), [`RECORRIDO.md`](RECORRIDO.md) y [`BITACORA.md`](BITACORA.md); lo demás es "existe en el código".

Documentos hermanos: [`FALTANTES.md`](FALTANTES.md) (qué falta) · [`REQUISITOS.md`](REQUISITOS.md) · [`ENTREGA-3.md`](ENTREGA-3.md) · [`PLAN.md`](PLAN.md) · [`PLAN-ACCION.md`](PLAN-ACCION.md) · [`MARCO-TECNICO.md`](MARCO-TECNICO.md) · [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md) · [`GUIA-CONTINUAR.md`](GUIA-CONTINUAR.md) · [`INCIDENTE-2026-10-19.md`](INCIDENTE-2026-10-19.md).

## Resumen en una tabla

| Área | Avance | Hecho | Falta |
|---|---|---|---|
| Requisitos (30 % de la app) | ~72 % | 19 de 32 completos, 8 parciales y 5 sin hacer | Ver [`REQUISITOS.md`](REQUISITOS.md) |
| App: docente | Alto | Checador (entrada y salida, sin GPS), **pase de lista en Mi día** (sin registro = ausente), rutina, reportes (escrito, dictado, foto), **Mi sala rediseñada** (cuadrícula, avisos de casa, ficha del niño, resumen guiado), **pase de salida y «Finalizar día»**, chats con tutores y fotos. Desde el 20/oct, con el estilo del admin y lo último guardado en el teléfono (perfil, checada, rutina, ficha, pase de salida) | Escrituras sin conexión (salvo asistencia); tiempo real del chat; "Mis documentos" (se quitó); probar en teléfono la selección de varios niños en un reporte y la cámara |
| App: admin | Alto | **Avisos** (texto e imagen), **Docentes** (con **Salas**), **Familias** (infantes y padres, **vincular a un padre con más niños**), **Ajustes** (horario, menú del día, rutinas, catálogo, **personas autorizadas**, Mi cuenta); filtros Activos/Todos; **invitación por WhatsApp**; listas que salen al instante con lo guardado | Ver la bitácora de auditoría; mejorar Horario; editar los datos de una persona |
| App: padre | Medio-alto | Hoy (menú, entrada y salida del niño), calendario con resumen semanal, fotos (con **Borrar** directo), chat con fotos, **Mi hijo** (datos y salud), **selector de hijo** (HN4), nota a la asistente, avisos. Desde el 20/oct, con el estilo del admin y sin datos inventados | Paginación de fotos; probar el selector de hijo con una familia de dos hijos |
| Backend | Alto | 16 módulos, **84 rutas**, 13 migraciones (última `d74a0f6c4e15`), 34 tablas, seed, **343 pruebas**, login JWT, auditoría, resumen semanal, página `/invitar`, avisos con imagen, borrado directo de fotos | Candado al `seed_demo.py --borrar-todo`; quitar lo que ya no usa la app (turnos, aviso a coordinación) |
| Seguridad | Medio-alto | RBAC con matriz de roles comprobada por prueba, alcance por registro (404), ORM parametrizado, login JWT (Argon2id, refresh rotado con detección de reuso), tokens cifrados con Keystore, límite de intentos, cabeceras, CORS cerrado, auditoría, **HTTPS con HSTS** en el servidor compartido | Escaneo ZAP; la base local del teléfono (cola y caché) sin cifrar |
| Sin conexión | Parcial | Room (versión 5) + WorkManager: la asistencia se marca sin red y se manda sola; **los tres roles ven lo último que cargaron** (`cached_response`) | Reportes, fotos, chat, avisos y pase de salida necesitan red para escribir |
| Infraestructura | Medio | **Servidor compartido** en `https://api.codeaapp.site/api/v1/` (túnel de Cloudflare desde una Mac, proyecto `codea-prod`), base en Aiven (`codea_prod`, Postgres 18), fotos en SeaweedFS (`archivos.codeaapp.site`, hasta 100 volúmenes desde el 20/oct), **respaldo diario** de la base y las fotos (20/oct, restauración probada), `.env.dev` sin secretos para el equipo | APK firmado; monitoreo; la Mac no se debe dormir; el valor por omisión de `API_BASE_URL` en `app/build.gradle.kts` sigue siendo el emulador |
| Documentación de la entrega | Medio | READMEs técnicos al día (20/oct) y los ocho documentos de `docs/entrega/` (con partes **[completar por el equipo]**); [`INCIDENTE-2026-10-19.md`](INCIDENTE-2026-10-19.md) | Documento de arquitectura; guion de la presentación, video de respaldo, captura del Planner y entrega en Canvas |

## Qué existe, por capa

- **App** (`app/`): Compose + Material 3 + Navigation, tres roles, Retrofit/OkHttp contra la API, Coil para fotos, Room (versión 5: cola de asistencia, caché de Mi sala y caché de respuestas de la API) + WorkManager, intent-filters para el enlace de invitación (`https` con `autoVerify` y `codea://`). Ver [`app/README.md`](../app/README.md).
- **Backend** (`guarderia-api/`): FastAPI + SQLAlchemy asíncrono + Postgres + almacén S3. Ver [`guarderia-api/README.md`](../guarderia-api/README.md) (cómo levantarla, el servidor compartido y los respaldos) y [`guarderia-api/PENDIENTES.md`](../guarderia-api/PENDIENTES.md).
- **Servidor compartido**: Docker Compose en la Mac del equipo (`codea-prod`: `api`, `storage`, `tunnel`; la base en Aiven). Respaldo diario a las 3:00 con `launchd` (`guarderia-api/scripts/respaldo.sh`).
- **Prototipo de diseño** (`docs/prototipos/`): HTML de referencia visual.

## Deuda técnica conocida

| Tema | Detalle |
|---|---|
| HTTP sin cifrar | Solo en debug y solo a `10.0.2.2`/`localhost` (`network_security_config`). La app del equipo apunta a `https://api.codeaapp.site/api/v1/` (en `local.properties`); el valor por omisión de `app/build.gradle.kts` sigue siendo `10.0.2.2`. |
| Release sin firmar | `app/build.gradle.kts` tiene `optimization.enable = false` y no hay `signingConfig`. |
| Seed peligroso | `seed_demo.py --borrar-todo` vacía todas las tablas y el bucket sin pedir confirmación; así se borraron los datos de producción el 19/oct ([incidente](INCIDENTE-2026-10-19.md)). Falta que se niegue fuera de desarrollo. |
| Un solo servidor | La API, las fotos y el túnel corren en una Mac: si se duerme o se apaga, nadie entra. Sin monitoreo (RNF04). |
| Código sin salida | La tabla `media_deletion_request` ya no la usa nadie; se conserva sin migración destructiva. Del backend, `POST /staff-attendance/notify-coordination` y la tabla `coordination_notice` no los usa la app. Los turnos (`shift`) siguen en la base y en la API aunque el alta de la docente ya no los pide. |
| `CodeaNavHost.kt` | 1,272 líneas; todos los frentes lo tocan y genera conflictos de merge. |
| Estado con `mutableStateOf` | `CodeaViewModel` y `ParentViewModel`; el marco pide StateFlow. |
| Contraseñas por defecto | `guarderia-api/.env.example` trae valores de ejemplo (`POSTGRES_PASSWORD=123456`, `S3_SECRET_KEY=cambia-esta-clave`); `.env.dev` es solo para desarrollo. Producción usa `.env.prod`, que no está en el repo. |
| Pruebas de la app | 140 pruebas JVM (dominio, sesión, reglas de Mi sala, pase de salida, invitación, duraciones); una sola prueba instrumentada (`ExampleInstrumentedTest`). |
| Reglas duplicadas | `core/rules.py` repite reglas de la app; si cambia una, cambiar las dos. |

## Cómo se calculó el avance de requisitos

Cada requisito vale lo mismo: ✅ = 1, 🟡 = 0.5, ❌ = 0. (19 + 4) / 32 ≈ 72 %.
