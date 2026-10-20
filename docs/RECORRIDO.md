# Recorrido en teléfono físico (18 de octubre de 2026)

App de la rama `integracion` en un teléfono Android real con **modo oscuro activado**, por USB (`adb reverse` 8000 y 9000), contra la API local y la base de Aiven migrada. Cuentas del seed con contraseña `codea1234`. Complementa a [`PLAN-ACCION.md`](PLAN-ACCION.md).

## Lo que funciona

| Rol | Resultado |
|---|---|
| Login | Los tres roles entran con correo y contraseña; "Mi cuenta" → "Cerrar sesión" pide confirmación y regresa al login. |
| Admin | Centro (ocupación de salas, avisos), Docentes (8), Familias (infantes 12 y padres 7) y Ajustes (rutinas 2) cargan datos reales. Las pestañas Infantes y Padres se unieron en Familias, y Rutinas pasó a Ajustes (18/oct). |
| Docente | Mi día, Mi sala (con Presente/Falta y chat), Chats, Menú y Avisos de la fundación (contador de 2 nuevos). |
| Padre | Hoy (con campana de avisos), Calendario, Fotos y Chat con la asistente. |

## Fallas encontradas

| # | Dónde | Falla | Gravedad | Estado |
|---|---|---|---|---|
| 1 | Padre → Documentos | **La app se cierra** al abrir la tab: `Key "1" was already used` en el `LazyColumn` (documentos y personas autorizadas usan el mismo `id` como clave). | Alta | Corregido (y la pestaña desapareció: ahora es «Mi hijo») |
| 2 | Login, Activar cuenta | Con el teléfono en modo oscuro, el título "Inicia sesión" y los iconos de la barra de estado salen blancos sobre fondo claro: casi no se leen. | Media | Corregido: `AuthFrame` pinta el fondo del tema (`AuthComponents.kt`) |
| 3 | Padre → Hoy | En modo oscuro, "El día de Mateo" (oscuro sobre oscuro) y el nombre del niño y "Todavía no hay notas" (blanco sobre durazno) no se leen. | Media | Corregido: título con `onBackground` y tarjeta con texto `Ink` (`Notificaciones.kt`, `ParentTodayScreen.kt`) |
| 4 | Docente → Checador | **Era un prototipo:** botones "Simular llegada", fecha fija ("19 de agosto"), hora fija 07:52 y distancia fija ("12 m"); además `registrar()` mandaba siempre las coordenadas del centro, así que el servidor nunca podía rechazar por distancia. | Alta (CRF-05 figuraba como cumplido) | Corregido: GPS y hora reales (`DeviceLocation`, `CheckerViewModel`); probado en el teléfono: "Domingo 18 de octubre", 13:29, "Estás a 842 m", fuera del perímetro y botón bloqueado. Los casos simulados solo salen en compilaciones debug. **El mapa de Google sigue en blanco:** falta `MAPS_API_KEY` en `local.properties` (decisión #3 abierta). |
| 5 | Docente → Mi sala | El título dice "Sala · Preescolar 2", pero Karla Robles es de Maternal (Menú y admin). | Media | Corregido: usa la sala real de `GET /me` (`MyRoomScreen.kt`) |
| 6 | Padre → Calendario | El resumen dice "Agosto en una línea" aunque el mes es octubre. | Baja | Corregido: usa el mes de los datos (`ParentCalendarScreen.kt`) |
| 7 | Docente → Menú | "Mis documentos" y "Mi perfil" son pantallas de "Próximamente". | Media | Corregido: Mi perfil abre Mi cuenta y «Mis documentos» se quitó del Menú (18/oct) |
| 8 | Padre | La tab "Documentos" se corta en el borde derecho de la barra inferior. | Baja | Corregido: etiquetas en una línea y 11 sp con 5 tabs (`CodeaBottomBar.kt`) |
| 9 | Admin → Rutinas | La pantalla no tiene título arriba. | Baja | Corregido: ahora es una opción de Ajustes y lleva título y botón de volver |
| 10 | Padre → Fotos | Sin fotos en la base de pruebas, así que no se pudo probar la galería ni la solicitud de borrado. Fondo claro fijo con barra superior oscura. | Media | Pendiente de probar con una foto subida |

## Pendientes que salieron de las correcciones

- ~~Ubicación del centro para la demo~~ **Ya no aplica (18/oct):** el checador no usa ubicación, así que no hay que fijar el centro para registrar una entrada.
- Historia: la entrada y la salida se probaron en el teléfono con el simulador de pruebas y la ubicación real a 842 m. Desde el 18/oct el checador no usa ubicación; **la versión nueva (sin GPS, con cierre a las 9 pm) se probó contra la API local con `curl` y con las pruebas automáticas, pero no en el teléfono.**

## Cosas del entorno (no son fallas de la app)

- El `venv` de `guarderia-api/` no tenía las dependencias del login (`pwdlib`); se reinstalaron con `requirements.txt`.
- Las pruebas de la API (68, todas pasan) usan el Postgres local (`guarderia_test`), cuya contraseña es la anterior del `.env`. Se corren con `POSTGRES_PASSWORD=<la del contenedor local> .venv/bin/pytest`.
- La base de Aiven estaba en la migración anterior al login; se migró y los 15 usuarios quedaron con la contraseña del seed.

## Falta recorrer (todo lo que llegó después de este recorrido)

El recorrido de arriba es de la mañana del 18/oct. Desde entonces se agregó esto, que **no se ha recorrido en un teléfono** (lo que sí se vio está en [`BITACORA.md`](BITACORA.md)):

| Rol | Qué recorrer |
|---|---|
| Docente | Pase de lista en Mi día («Marcar a todos presentes»); Mi sala nueva (De casa hoy, «Ya lo vi», filtros, ausentes plegados, «Capturar resúmenes» con varios niños); ficha del niño (alergias, Llamar, quién lo recoge, Corregir asistencia); pase de salida de punta a punta (registrar, «Corregir», «Finalizar día», «Día finalizado»); chats con tutores y con fotos; sin conexión con los campos nuevos; modo oscuro |
| Padre | «Mi hijo» (guardar salud y verla en la ficha de la docente); chat con foto (galería y cámara); abrir el enlace de invitación; Fotos con una foto real (falla #10 sigue sin probar) |
| Admin | «Enviar por WhatsApp» (chat abierto con el mensaje); Ajustes → Personas autorizadas; Horario |
| Los tres | `adb shell am start -a android.intent.action.VIEW -d "codea://invitar?email=…&code=…" mx.tec.codea` con la sesión cerrada (debe abrir «Activar mi cuenta» con correo y código puestos) y con la sesión abierta (debe ignorarlo) |

Antes de recorrer: aplicar en Aiven `a96b2c7d5f65` y `a07c3d8e6f76` (sin ellas el login da 500) y compilar con `-PapiBaseUrl=http://localhost:8000/api/v1/` más `adb reverse` 8000 y 9000.

## Recorrido del 19/oct por la noche (teléfono Pixel 8 Pro, modo oscuro, API local con Aiven)

Compilado con `-PapiBaseUrl=http://localhost:8000/api/v1/ --no-configuration-cache` (sin esa bandera Gradle reutiliza la configuración anterior y la app queda con `10.0.2.2`, que fue la causa de «No se pudo conectar con el servidor» al empezar) y `adb reverse` 8000 y 9000.

### Lo que se probó y funcionó

| Rol | Flujo | Resultado |
|---|---|---|
| Docente (Karla) | Mi día con «Menú de hoy»; pase de lista (marcar ausente y presente a Diego) | Muestra «Entró a las 18:59» bajo el nombre; el menú aparece arriba de Mi día |
| Docente | Rutina terminada (momentos confirmados), «Siguiente paso: Pase de salida», registrar salida de Diego (con Marisol) y de Renata (con Carlos), «Corregir», «Finalizar día» y «Día finalizado» | Todo bien; «Han salido 2 de 2», «Salió a las 19:10 con Marisol Hernández», salida registrada a las 19:11 |
| Docente | Menú → Avisos de la fundación | El aviso con imagen se ve con su imagen |
| Padre (Marisol) | Hoy: «Entrada · 18:59» en la línea de tiempo y tarjeta «Menú del día»; campana con el aviso con imagen | Bien |
| Padre | Mi hijo: guardar alergias | «Datos de tu hijo guardados» |
| Padre | Fotos: foto de prueba en un reporte → abrir → «Borrar» → confirmar | «Foto borrada»; la API devuelve 0 fotos y el almacén responde 403 |
| Admin | Centro → «Agregar imagen» (selector del sistema), publicar solo con imagen | Aparece en el historial con su imagen |
| Admin | Ajustes → Menú del día: agregar el lunes (desayuno, comida y colación), guardar | «Menú guardado» y la semana se recarga con el menú |

### Fallas encontradas y corregidas

| # | Dónde | Falla | Estado |
|---|---|---|---|
| 11 | Padre → Fotos | Ignoraba el modo oscuro (fondo claro fijo con la barra oscura) y, sin fotos, quedaba vacía | Corregido: usa los colores del tema y dice «Todavía no hay fotos de Diego…» |
| 12 | Padre → Calendario | Con modo oscuro, los números de los días sin registro casi no se leían; decía «1 días» | Corregido: color legible y plural («1 día con nota») |
| 13 | Admin → Menú del día | El texto «Semana del 19 al 23 de octubre» casi no se veía en modo oscuro | Corregido: usa el color del tema |
| 14 | Padre → Mi hijo | Al escribir en un campo, el teclado lo tapaba y la pantalla no subía | Corregido: `imePadding()` en el contenedor de navegación (`CodeaApp.kt`), para todas las pantallas |
| 15 | Padre → Fotos y Hoy | No se refrescaban al entrar: una foto subida con la app abierta no salía hasta reabrirla | Corregido: se vuelve a pedir al entrar |
| 16 | Padre → visor de fotos | La inicial de la maestra salía «R» (de «Robles») | Corregido: usa la inicial del nombre («K») |
| 17 | Admin → Centro | Al publicar un aviso con imagen el botón solo se apagaba durante varios segundos | Corregido: dice «Publicando…» |

### Notas del entorno (no son fallas de la app)

- En el chat, la foto del 18/oct sale como un cuadro gris: el archivo no está en el almacén de esta máquina (la base de Aiven es compartida y cada quien tiene su propio SeaweedFS local); la URL firmada responde 404.
- La prueba dejó datos en Aiven: un aviso con imagen, el menú del lunes 19, dos fotos de prueba (las dos borradas), la salida de Diego y Renata y el cierre de turno de Karla del 19/oct.

### Falta recorrer

Chat con foto (cámara y galería), el enlace de invitación y «Enviar por WhatsApp», Ajustes → Horario y → Personas autorizadas, ver lo que guarda la familia en la ficha del niño de la docente, el checador sin conexión y las marcas sin conexión.
