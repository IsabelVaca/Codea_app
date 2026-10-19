# Estado del proyecto

Foto a **2026-10-18 por la noche**, sobre `main` (después de los PR #7 a #9 y la invitación por WhatsApp). Entrega final: **jueves 22 de octubre de 2026, 23:59** (RETO_ENTREGA_3). Congelamiento de funciones nuevas: **lunes 19 por la noche**.
Método: lectura del código, `pytest` de la API (**324 pruebas en verde**, corridas hoy) y conteo de las pruebas JVM de la app (**125**, corridas con Gradle: 0 fallas). Lo que se ha visto en un teléfono o emulador se dice en [`FALTANTES.md`](FALTANTES.md) y en [`BITACORA.md`](BITACORA.md); lo demás es "existe en el código".

Documentos hermanos: [`FALTANTES.md`](FALTANTES.md) (qué falta) · [`REQUISITOS.md`](REQUISITOS.md) · [`ENTREGA-3.md`](ENTREGA-3.md) · [`PLAN.md`](PLAN.md) · [`PLAN-ACCION.md`](PLAN-ACCION.md) · [`MARCO-TECNICO.md`](MARCO-TECNICO.md) · [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md) · [`GUIA-CONTINUAR.md`](GUIA-CONTINUAR.md).

## Resumen en una tabla

| Área | Avance | Hecho | Falta |
|---|---|---|---|
| Requisitos (30 % de la app) | ~64 % | 15 de 32 completos, 11 parciales y 6 sin hacer | Ver [`REQUISITOS.md`](REQUISITOS.md) |
| App: docente | Alto | Checador (entrada y salida, sin GPS), **pase de lista en Mi día** (sin registro = ausente), rutina, reportes (escrito, dictado, foto), **Mi sala rediseñada** (avisos de casa, ficha del niño, resumen guiado), **pase de salida y «Finalizar día»**, chats con tutores y fotos | Salida sin conexión; reportes sin conexión; tiempo real; "Mis documentos" (se quitó); probar en teléfono el pase de salida y los chats nuevos |
| App: admin | Alto | Centro, Docentes, **Familias** (infantes y padres), **Ajustes** (horario, rutinas, catálogo, **personas autorizadas**, Mi cuenta); salas, avisos (texto), **invitación por WhatsApp** | Menú del día; avisos con imágenes; ver la bitácora de auditoría; revisión de fotos (sin pantalla); mejorar Horario |
| App: padre | Medio | Hoy (con la hora de salida del niño), calendario con resumen semanal, fotos (con **Borrar** directo), chat con fotos, **Mi hijo** (datos y salud), **selector de hijo** (HN4), nota a la asistente, avisos | Paginación de fotos, menú del día, probar en teléfono el selector de hijo, el borrado directo y la salida en Hoy |
| Backend | Alto | 15 módulos, 84 endpoints, 11 migraciones, 33 tablas, seed, **324 pruebas**, login JWT, auditoría, resumen semanal, página `/invitar` | HTTPS público, entrada del niño con hora, avisos con imagen, endpoint de borrado directo de fotos |
| Seguridad | Medio-alto | RBAC con matriz de roles comprobada por prueba, alcance por registro (404), ORM parametrizado, login JWT (Argon2id, refresh rotado con detección de reuso), tokens cifrados con Keystore, límite de intentos, cabeceras, CORS cerrado, auditoría | HTTPS, escaneo ZAP, base local del teléfono sin cifrar |
| Sin conexión | Parcial | Room (versión 3) + WorkManager para la asistencia y la última Mi sala | Reportes, fotos, chat, avisos y pase de salida |
| Infraestructura | Bajo | Docker Compose para desarrollo, base en Aiven configurable | Migrar Aiven (`a96b2c7d5f65`, `a07c3d8e6f76`), despliegue público con HTTPS, APK firmado, respaldos, monitoreo |
| Documentación de la entrega | Medio | READMEs técnicos y los ocho documentos de `docs/entrega/` (puestos al día el 18/oct; con partes **[completar por el equipo]**) | Guion de la presentación, video de respaldo, captura del Planner, entrega en Canvas |

## Qué existe, por capa

- **App** (`app/`): Compose + Material 3 + Navigation, tres roles, Retrofit/OkHttp contra la API, Coil para fotos, Room + WorkManager para la asistencia, intent-filters para el enlace de invitación (`https` con `autoVerify` y `codea://`). Ver [`app/README.md`](../app/README.md).
- **Backend** (`guarderia-api/`): FastAPI + SQLAlchemy asíncrono + Postgres + almacén S3. Ver [`guarderia-api/README.md`](../guarderia-api/README.md) y [`guarderia-api/PENDIENTES.md`](../guarderia-api/PENDIENTES.md).
- **Prototipo de diseño** (`docs/prototipos/`): HTML de referencia visual.

## Deuda técnica conocida

| Tema | Detalle |
|---|---|
| HTTP sin cifrar | Solo en debug y solo a `10.0.2.2`/`localhost` (`network_security_config`); falta definir `API_BASE_URL_RELEASE` con HTTPS. |
| Release sin firmar | `app/build.gradle.kts` tiene `optimization.enable = false` y no hay `signingConfig`. |
| Código sin salida | `SolicitudesBorradoScreen`, su ViewModel, su ruta y el repositorio de solicitudes de borrado no se pueden abrir (Centro ya no los muestra). Del backend, `POST /staff-attendance/notify-coordination` y la tabla `coordination_notice` no los usa la app. |
| `CodeaNavHost.kt` | 1,218 líneas; todos los frentes lo tocan y genera conflictos de merge. |
| Estado con `mutableStateOf` | `CodeaViewModel` y `ParentViewModel`; el marco pide StateFlow. |
| Contraseñas por defecto | `guarderia-api/.env.example` trae valores de ejemplo (`POSTGRES_PASSWORD=123456`, `S3_SECRET_KEY=cambia-esta-clave`); cambiarlos antes de desplegar. |
| Pruebas de la app | 125 pruebas JVM (dominio, sesión, reglas de Mi sala, pase de salida, invitación); una sola prueba instrumentada (`ExampleInstrumentedTest`). |
| Reglas duplicadas | `core/rules.py` repite reglas de la app; si cambia una, cambiar las dos. |
| Turnos (`shift`) | Siguen en la base y en la API aunque el alta de la docente ya no los pide. |

## Cómo se calculó el avance de requisitos

Cada requisito vale lo mismo: ✅ = 1, 🟡 = 0.5, ❌ = 0. (15 + 5.5) / 32 ≈ 64 %.
