# Estado del proyecto

Foto a **2026-10-16**. Entrega final: **jueves 22 de octubre de 2026, 23:59** (RETO_ENTREGA_3).
Método: lectura del código y de los tableros de Teams. **No se corrió la app ni el backend para esta foto**, así que "hecho" significa "existe en el código".

Documentos hermanos: [`REQUISITOS.md`](REQUISITOS.md) · [`ENTREGA-3.md`](ENTREGA-3.md) · [`PLAN.md`](PLAN.md) · [`MARCO-TECNICO.md`](MARCO-TECNICO.md) · [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md) · [`GUIA-CONTINUAR.md`](GUIA-CONTINUAR.md).

## Resumen en una tabla

| Área | Avance | Hecho | Falta |
|---|---|---|---|
| Requisitos (30 % de la app) | ~53 % | 12 de 32 completos y 10 parciales | Ver [`REQUISITOS.md`](REQUISITOS.md) |
| App: docente | Alto | Check-in con GPS, rutina, reportes (escrito, dictado, foto), Mi sala, chat | Documentos y perfil (`ComingSoon`); tiempo real; sin conexión |
| App: admin | Alto | Salas, docentes, infantes, padres, rutinas y catálogo de momentos, centro y avisos (publicar e historial) | Solicitudes de borrado, pagos, menú del día |
| App: padre | Medio | Hoy, calendario, fotos, documentos, personas autorizadas, nota a la asistente | Chat, varios hijos, paginación de fotos, menú del día |
| Backend | Medio-alto | 13 módulos, migraciones, seed, 43 pruebas | Login JWT (diseñado en `plan/04`), auditoría, resumen semanal, pagos, endurecimiento |
| Seguridad | Bajo | RBAC, alcance por registro (404), ORM parametrizado, invitaciones | Login real, HTTPS, rate limiting, cabeceras, escaneo |
| Sin conexión | Nulo | — | Room + WorkManager |
| Infraestructura | Bajo | Docker Compose para desarrollo, base en Aiven configurable | Despliegue público con HTTPS, APK firmado, respaldos, monitoreo |
| Documentación de la entrega | Bajo | READMEs técnicos (esta carpeta) | Matriz de trazabilidad, manuales, ética y legal, administración final, presentación |

## Qué existe, por capa

- **App** (`app/`): Compose + Material 3 + Navigation, tres roles, Retrofit/OkHttp contra la API, Coil para fotos. Ver [`app/README.md`](../app/README.md).
- **Backend** (`guarderia-api/`): FastAPI + SQLAlchemy asíncrono + Postgres + almacén S3. Ver [`guarderia-api/README.md`](../guarderia-api/README.md) y [`guarderia-api/PENDIENTES.md`](../guarderia-api/PENDIENTES.md).
- **Prototipo de diseño** (`docs/prototipos/`): HTML de referencia visual.

## Deuda técnica conocida

| Tema | Detalle |
|---|---|
| Login falso | La app manda `Bearer dev:<correo>` y el rol se elige a mano (`CodeaApp.kt`, `ApiConfig.kt`). |
| HTTP sin cifrar | `BASE_URL = http://10.0.2.2:8000/` y `usesCleartextTraffic="true"` en el manifiesto. |
| Release sin firmar | `app/build.gradle.kts` tiene `optimization.enable = false` y no hay `signingConfig`. |
| Repositorios en memoria olvidados | En `data/` quedan clases de la etapa sin backend (`ChildRepository`, `TeacherRepository`, …); ver [`app/src/main/java/mx/tec/codea/data/README.md`](../app/src/main/java/mx/tec/codea/data/README.md). |
| Datos de ejemplo del padre | `ui/screens/parent/*SampleData.kt` siguen en el repo; las pantallas leen del `ParentViewModel`. |
| Dos pantallas casi iguales | Por ejemplo `CheckerScreen.kt` y `checadorScreen.kt`, `MyDayScreen.kt` y `miDiaScreen.kt`; ver el README de `ui/screens/`. |
| `CodeaNavHost.kt` | 849 líneas; todos los frentes lo tocan y genera conflictos de merge. |
| Estado con `mutableStateOf` | `CodeaViewModel` y `ParentViewModel`; el marco pide StateFlow. |
| Contraseñas por defecto | `guarderia-api/.env.example` trae valores de ejemplo; cambiarlos antes de desplegar. |
| Pruebas de la app | Solo 11 pruebas JVM de dominio; casi no hay pruebas instrumentadas. |

## Cómo se calculó el avance de requisitos

Cada requisito vale lo mismo: ✅ = 1, 🟡 = 0.5, ❌ = 0. (12 + 5) / 32 ≈ 53 %.
