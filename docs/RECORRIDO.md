# Recorrido en teléfono físico (18 de octubre de 2026)

App de la rama `integracion` en un teléfono Android real con **modo oscuro activado**, por USB (`adb reverse` 8000 y 9000), contra la API local y la base de Aiven migrada. Cuentas del seed con contraseña `codea1234`. Complementa a [`PLAN-ACCION.md`](PLAN-ACCION.md).

## Lo que funciona

| Rol | Resultado |
|---|---|
| Login | Los tres roles entran con correo y contraseña; "Mi cuenta" → "Cerrar sesión" pide confirmación y regresa al login. |
| Admin | Centro (ocupación de salas, avisos), Docentes (8), Infantes (12), Padres (7) y Rutinas (2) cargan datos reales. |
| Docente | Mi día, Mi sala (con Presente/Falta y chat), Chats, Menú y Avisos de la fundación (contador de 2 nuevos). |
| Padre | Hoy (con campana de avisos), Calendario, Fotos y Chat con la asistente. |

## Fallas encontradas

| # | Dónde | Falla | Gravedad | Estado |
|---|---|---|---|---|
| 1 | Padre → Documentos | **La app se cierra** al abrir la tab: `Key "1" was already used` en el `LazyColumn` (documentos y personas autorizadas usan el mismo `id` como clave). | Alta | Corregido: claves con prefijo (`ParentDocumentsScreen.kt`) |
| 2 | Login, Activar cuenta | Con el teléfono en modo oscuro, el título "Inicia sesión" y los iconos de la barra de estado salen blancos sobre fondo claro: casi no se leen. | Media | Corregido: `AuthFrame` pinta el fondo del tema (`AuthComponents.kt`) |
| 3 | Padre → Hoy | En modo oscuro, "El día de Mateo" (oscuro sobre oscuro) y el nombre del niño y "Todavía no hay notas" (blanco sobre durazno) no se leen. | Media | Corregido: título con `onBackground` y tarjeta con texto `Ink` (`Notificaciones.kt`, `ParentTodayScreen.kt`) |
| 4 | Docente → Checador | **Era un prototipo:** botones "Simular llegada", fecha fija ("19 de agosto"), hora fija 07:52 y distancia fija ("12 m"); además `registrar()` mandaba siempre las coordenadas del centro, así que el servidor nunca podía rechazar por distancia. | Alta (CRF-05 figuraba como cumplido) | Corregido: GPS y hora reales (`DeviceLocation`, `CheckerViewModel`); probado en el teléfono: "Domingo 18 de octubre", 13:29, "Estás a 842 m", fuera del perímetro y botón bloqueado. Los casos simulados solo salen en compilaciones debug. **El mapa de Google sigue en blanco:** falta `MAPS_API_KEY` en `local.properties` (decisión #3 abierta). |
| 5 | Docente → Mi sala | El título dice "Sala · Preescolar 2", pero Karla Robles es de Maternal (Menú y admin). | Media | Corregido: usa la sala real de `GET /me` (`MyRoomScreen.kt`) |
| 6 | Padre → Calendario | El resumen dice "Agosto en una línea" aunque el mes es octubre. | Baja | Corregido: usa el mes de los datos (`ParentCalendarScreen.kt`) |
| 7 | Docente → Menú | "Mis documentos" y "Mi perfil" son pantallas de "Próximamente". | Media | Pendiente |
| 8 | Padre | La tab "Documentos" se corta en el borde derecho de la barra inferior. | Baja | Corregido: etiquetas en una línea y 11 sp con 5 tabs (`CodeaBottomBar.kt`) |
| 9 | Admin → Rutinas | La pantalla no tiene título arriba. | Baja | Pendiente |
| 10 | Padre → Fotos | Sin fotos en la base de pruebas, así que no se pudo probar la galería ni la solicitud de borrado. Fondo claro fijo con barra superior oscura. | Media | Pendiente de probar con una foto subida |

## Pendientes que salieron de las correcciones

- **Ubicación del centro para la demo:** la base trae las coordenadas del seed (Monterrey, sin relación con el lugar real). Para registrar una entrada de verdad, el admin debe poner la ubicación donde se hará la demo con `PATCH /center` (`latitude`, `longitude`, `check_in_radius_m`). La app todavía no tiene pantalla para eso (en [`PLAN-ACCION.md`](PLAN-ACCION.md), bloque 1).
- La entrada y la salida **sí registradas** se probaron en el teléfono con el simulador de pruebas (el servidor recibe coordenadas del centro y las valida): entrada con retardo a las 13:59 y salida a las 14:01. Con la ubicación real del teléfono (a 842 m del centro del seed) la app bloquea la entrada y la salida como debe.

## Cosas del entorno (no son fallas de la app)

- El `venv` de `guarderia-api/` no tenía las dependencias del login (`pwdlib`); se reinstalaron con `requirements.txt`.
- Las pruebas de la API (68, todas pasan) usan el Postgres local (`guarderia_test`), cuya contraseña es la anterior del `.env`. Se corren con `POSTGRES_PASSWORD=<la del contenedor local> .venv/bin/pytest`.
- La base de Aiven estaba en la migración anterior al login; se migró y los 15 usuarios quedaron con la contraseña del seed.
