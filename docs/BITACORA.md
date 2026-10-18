# Bitácora de trabajo

Registro cronológico de lo que se hizo, por qué y cómo se verificó. Lo nuevo va arriba. El plan está en [`PLAN-ACCION.md`](PLAN-ACCION.md) y las fallas encontradas en [`RECORRIDO.md`](RECORRIDO.md).

## 2026-10-18 (domingo)

### Integración de ramas
- Rama local `integracion` desde `main`: se mergearon `feature/login-jwt` y `feature/chat` (sin conflictos).
- `feature/pagos` **no** se integró: el equipo decidió que no hay módulo de pagos.
- Verificación: `./gradlew :app:testDebugUnitTest` en verde.

### Entorno de pruebas
- `guarderia-api/.env` actualizado con la base de Aiven; se conservaron `JWT_SECRET` (generado) y `SEED_PASSWORD`. `S3_PUBLIC_ENDPOINT_URL=http://localhost:9000` porque el teléfono es físico y usa `adb reverse`. El `.env` anterior se guardó fuera del repositorio.
- Aiven estaba en la migración `caf6344f99d5`. Se corrió `alembic upgrade head` (`98306224d8b5`, login con JWT; elimina `firebase_uid`) y a los 15 usuarios se les puso la contraseña del seed (`codea1234`). El equipo confirmó que nadie estaba trabajando sobre la base.
- Pruebas de la API: 68 en verde con el Postgres local (`guarderia_test`). El `venv` no tenía `pwdlib`; se reinstaló con `requirements.txt`.
- Teléfono físico (modo oscuro) por USB: `adb reverse tcp:8000` y `tcp:9000`; la app se instala con `./gradlew :app:installDebug -PapiBaseUrl=http://localhost:8000/api/v1/`.

### Correcciones de la app (recorrido en teléfono)
| Falla | Arreglo | Verificado en el teléfono |
|---|---|---|
| La tab Documentos del padre cerraba la app (clave duplicada en `LazyColumn`) | Claves con prefijo `documento-` y `autorizado-` | Sí: abre y muestra documentos y autorizados |
| Login ilegible en modo oscuro | Fondo del tema en `AuthFrame` | Pendiente de ver (hay que cerrar sesión) |
| "Hoy" del padre ilegible en modo oscuro | Título con `onBackground`; tarjeta pastel con texto `Ink` | Sí |
| Mi sala decía "Preescolar 2" para todas | Título con `roomName` de `GET /me` | Pendiente de ver con la docente |
| Calendario decía "Agosto" | Mes tomado de los datos del calendario | Sí: "Octubre en una línea" |
| "Documentos" se partía en dos líneas | 11 sp, una línea y sin salto en la barra con 5 tabs | Sí, al abrir la tab |

### Documentación
- Se crearon [`PLAN-ACCION.md`](PLAN-ACCION.md), [`RECORRIDO.md`](RECORRIDO.md) y esta bitácora.

### Decisiones de trabajo
- Commits en `integracion`, sin coautor de Claude y sin `push` hasta que el equipo revise.
- Lo que depende de [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md) no se implementa.
