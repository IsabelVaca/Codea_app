# Estado del proyecto

Foto a **2026-10-16**. Entrega final: **jueves 22 de octubre de 2026, 23:59** (RETO_ENTREGA_3).
Método: lectura del código y de los tableros de Teams. **No se corrió la app ni el backend para esta foto**, así que "hecho" significa "existe en el código".

Documentos hermanos: [`REQUISITOS.md`](REQUISITOS.md) · [`ENTREGA-3.md`](ENTREGA-3.md) · [`PLAN.md`](PLAN.md) · [`MARCO-TECNICO.md`](MARCO-TECNICO.md) · [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md) · [`GUIA-CONTINUAR.md`](GUIA-CONTINUAR.md).

## Resumen en una tabla

| Área | Avance | Hecho | Falta |
|---|---|---|---|
| Requisitos (30 % de la app) | ~59 % | 15 de 32 completos y 8 parciales | Ver [`REQUISITOS.md`](REQUISITOS.md) |
| App: docente | Alto | Check-in con GPS, rutina, reportes (escrito, dictado, foto), Mi sala, chat | Documentos y perfil (`ComingSoon`); tiempo real; sin conexión |
| App: admin | Alto | Pestañas Centro, Docentes, **Familias** (infantes y padres juntos) y **Ajustes** (rutinas, catálogo de momentos y Mi cuenta); salas, avisos (publicar e historial) y fotos que piden borrar | Turnos, menú del día |
| App: padre | Medio | Hoy, calendario, fotos, documentos, personas autorizadas, nota a la asistente | Chat, varios hijos, paginación de fotos, menú del día |
| Backend | Alto | 13 módulos, migraciones, seed, 68 pruebas, login JWT | Resumen semanal, HTTPS, endurecimiento restante |
| Seguridad | Medio | RBAC, alcance por registro (404), ORM parametrizado, **login JWT** (Argon2id, refresh rotado con detección de reuso, baja que corta el acceso al instante), tokens cifrados con Keystore | HTTPS, rate limiting y bloqueo, cabeceras, escaneo ZAP, auditoría |
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
| HTTP sin cifrar | Solo en debug y solo a `10.0.2.2`/`localhost` (`network_security_config`); falta definir `API_BASE_URL_RELEASE` con HTTPS. |
| Release sin firmar | `app/build.gradle.kts` tiene `optimization.enable = false` y no hay `signingConfig`. |
| Repositorios en memoria olvidados | En `data/` quedan clases de la etapa sin backend (`ChildRepository`, `TeacherRepository`, …); ver [`app/src/main/java/mx/tec/codea/data/README.md`](../app/src/main/java/mx/tec/codea/data/README.md). |
| Datos de ejemplo del padre | `ui/screens/parent/*SampleData.kt` siguen en el repo; las pantallas leen del `ParentViewModel`. |
| Dos pantallas casi iguales | Por ejemplo `CheckerScreen.kt` y `checadorScreen.kt`, `MyDayScreen.kt` y `miDiaScreen.kt`; ver el README de `ui/screens/`. |
| `CodeaNavHost.kt` | 849 líneas; todos los frentes lo tocan y genera conflictos de merge. |
| Estado con `mutableStateOf` | `CodeaViewModel` y `ParentViewModel`; el marco pide StateFlow. |
| Contraseñas por defecto | `guarderia-api/.env.example` trae valores de ejemplo; cambiarlos antes de desplegar. |
| Pruebas de la app | 33 pruebas JVM (dominio, autenticador de tokens y sesión); casi no hay pruebas instrumentadas. |

## Cómo se calculó el avance de requisitos

Cada requisito vale lo mismo: ✅ = 1, 🟡 = 0.5, ❌ = 0. (12 + 5) / 32 ≈ 53 %.
