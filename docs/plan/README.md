# Plan para llegar al mínimo funcional

Revisión del 16 de octubre de 2026 sobre la rama `backend` (commit `12da735`), puesta al día el 17 de octubre. **Este archivo es histórico:** el login, la conexión con la API y las pantallas de los tres roles ya están en `main` (ver [Estado al 20 de octubre](#estado-al-20-de-octubre)). Lo que falta está en [`../FALTANTES.md`](../FALTANTES.md). La app ya tiene las pantallas principales de los tres roles y buena parte ya habla con la API. La API tiene todos los endpoints de la primera entrega y los de avisos, plantillas de rutina, centro e invitaciones. Lo que falta es **terminar de unir las dos partes**, **agregar las pantallas que faltan** (login, chat del padre, bandeja de chats, cuenta y cierre de sesión) y **cambiar Firebase por un login propio con correo, contraseña y JWT**.

La seguridad a fondo (rate limiting, bloqueo de cuentas, auditoría, etc.) se deja para una fase aparte. Aquí solo se diseña lo necesario para que el login funcione bien, con lo que el curso ya enseña como base (tokens cifrados en el teléfono, refresh rotado). Todo sigue las convenciones de [`../estilo.md`](../estilo.md).

## Documentos

| Archivo | Qué contiene |
| --- | --- |
| [../estilo.md](../estilo.md) | Las convenciones de las prácticas del curso que sigue todo el código: capas, nombres, estado, red, sesión, Hilt, pruebas |
| [01-app-pantallas.md](01-app-pantallas.md) | Las pantallas que existen hoy, las que faltan para el mínimo (por prioridad y por rol) y lo que le falta a las que ya existen |
| [02-app-plan-tecnico.md](02-app-plan-tecnico.md) | Cómo conectar la app a la API: cliente HTTP, sesión, repositorios, errores, fotos y orden de trabajo |
| [03-api-estado.md](03-api-estado.md) | Diagnóstico de la API: qué está hecho, qué está a medias, qué falta y en qué difiere de la propuesta de arquitectura |
| [04-api-login-jwt.md](04-api-login-jwt.md) | Diseño del login con correo y contraseña y JWT: tablas, endpoints, flujos, cambios al código y pruebas |
| [05-api-plan.md](05-api-plan.md) | Las tareas de la API en orden, incluidos los endpoints nuevos que piden las pantallas faltantes |
| [06-mi-sala-propuesta.md](06-mi-sala-propuesta.md) | Propuesta de rediseño de Mi sala (docente): pantallas, subpantallas, datos que faltan y decisiones por tomar |
| [08-seguridad-minimos.md](08-seguridad-minimos.md) | Checklist de la rúbrica de seguridad del RETO_ENTREGA_3: qué ya está hecho (con evidencia) y qué falta, punto por punto |

La lista vigente de endpoints está en [`../api/endpoints.md`](../api/endpoints.md). Referencias anteriores: [endpoints de la primera entrega (PDF, histórico)](../api/endpoints-api-codea.pdf) y la [propuesta de arquitectura](https://claude.ai/artifact/K4CqXjp82tkRLfpKDRdV5H).

## Estado al 20 de octubre

Todas las fases de abajo se hicieron, salvo lo que se dice aquí. Detalle en [`../ESTADO.md`](../ESTADO.md) y [`../BITACORA.md`](../BITACORA.md).

| Fase | Resultado |
|---|---|
| 1. Login | Hecho: login con JWT, activar cuenta con código **o con el enlace de WhatsApp**, Mi cuenta y cerrar sesión. El selector de rol solo sale a quien tiene varios roles |
| 2. Docente | Hecho y ampliado: checador sin GPS, Mi día con **pase de lista**, rutina, reportes, **Mi sala rediseñada** ([06](06-mi-sala-propuesta.md)), **pase de salida**, bandeja de chats con tutores y fotos |
| 3. Padre | Hecho: Hoy (con menú del día), Calendario con resumen semanal, Fotos (borrado directo), Chat con fotos, **Mi hijo** (reemplazó a «Documentos») y **selector de hijo** |
| 4. Admin | Hecho: Avisos (antes «Centro», con imagen), Docentes (con Salas), Familias (vincular un padre a más niños), Ajustes (horario, rutinas, menú del día, personas autorizadas) e invitación por WhatsApp |
| 5. Completar (P1) | Hecho: Día del niño (Mi sala → Resumen), Cambiar contraseña, selector de hijo. Cambió: las personas autorizadas las administra el admin; recuperar la contraseña es pedir un código nuevo al admin (`POST /users/{id}/access-code`; la app todavía no tiene botón para generarlo) |
| 6. Seguridad | Hecho en gran parte: límite de intentos, auditoría, cabeceras, CORS, matriz de roles, HTTPS público en `api.codeaapp.site` (19/oct) y respaldo diario (20/oct). Falta el release firmado |

Cifras (20/oct): 84 rutas, 13 migraciones, 343 pruebas de la API y 140 JVM.

## Resumen (16 de octubre)

| Parte | Hoy | Para el mínimo |
| --- | --- | --- |
| App | Pantallas de los tres roles. Ya tiene cliente HTTP (Retrofit) y habla con la API: admin completo (salas, docentes, infantes, padres, rutinas, avisos), docente (checador, rutina, reportes, Mi sala, chat, avisos) y padre (Hoy, calendario, fotos, documentos, avisos). Sigue con un selector de rol en lugar de login y manda `Bearer dev:<correo>` | 6 pantallas nuevas indispensables (P0, falta la de avisos que ya se hizo) y 6 más para entregar completo (P1). Terminar de conectar lo que sigue en memoria (ver el avance) |
| API | 13 módulos, 65 endpoints, 43 pruebas escritas. Login con Firebase o con `dev:<correo>` | Login propio con JWT (8 endpoints), código de invitación en las respuestas del admin y 2 ajustes pequeños. `GET /announcements` ya está |

## Orden de trabajo

Cada fase se puede probar sola antes de pasar a la siguiente.

| Fase | API | App | Se termina cuando |
| --- | --- | --- | --- |
| 1. Login | Login con JWT completo ([04](04-api-login-jwt.md)) y el seed con contraseñas | Cliente HTTP, sesión, Arranque, Iniciar sesión, Activar cuenta, Mi cuenta y cerrar sesión | Los 3 usuarios del seed entran con su correo y contraseña, ven sus tabs y pueden cerrar sesión |
| 2. Docente | Sin cambios grandes; `has_note_today` en Mi sala | Mi día, checador, reportes, Mi sala, bandeja de chats y conversación conectados a la API | Una docente checa, sigue la rutina, reporta con foto, pasa lista y chatea |
| 3. Padre | Sin cambios grandes | Hoy, Calendario, Fotos, Documentos y el tab nuevo de Chat conectados a la API | Un padre ve el día de su hijo, sus fotos y documentos, y le contesta a la docente |
| 4. Admin | Código de invitación en las respuestas y `resend-invitation` para docentes | Centro, Docentes, Infantes y Padres conectados a la API; diálogo con el código de invitación | El admin da de alta a una docente y a un padre, y los dos activan su cuenta con el código |
| 5. Completar (P1) | ~~`GET /announcements`~~ (hecho), cambiar y recuperar contraseña, documentos por defecto | ~~Avisos~~ (hecho), Día del niño, Agregar autorizado, Cambiar contraseña, selector de hijo | Todas las pantallas del mínimo funcionan contra la API |
| 6. Seguridad | Fase aparte | Fase aparte | Se planea después |

## Avance al 17 de octubre

Hecho después de la revisión del 16 de octubre:

| Qué | Dónde |
| --- | --- |
| App conectada a la API con Retrofit (`Authorization: Bearer dev:<correo>` mientras no haya login) y los repositorios de admin, docente y padre en `data/` | App |
| **Rutinas del admin:** lista, crear, reordenar y guardar contra `GET/POST/PUT /routine-templates`, y asignar una rutina a una sala con `PUT /classrooms/{id}/routine-template`. La docente ve la plantilla de su sala | App y API |
| **Rutina de hoy (docente):** pantalla de solo lectura que aparece justo después de registrar la entrada y antes de Mi día | App |
| **Avisos:** el admin publica a todo el centro o a una sala (menú desplegable) y ve el historial en Centro; `GET /announcements` con alcance por rol; la docente los ve en Menú → Avisos de la fundación con contador de nuevos; el padre los ve en la campana de Hoy | App y API |
| **Centro:** `GET/PATCH /center` y `GET/POST /invitations` en el backend | API |
| 43 pruebas escritas (antes 30) | API |

Sigue **sin hacerse** (y es lo que más pesa para el mínimo):

- Login propio con JWT y todo lo de la fase 1 (Arranque, Iniciar sesión, Activar cuenta, Mi cuenta, cerrar sesión). La app todavía elige el rol en `RoleSelectionScreen`.
- Código de invitación en las respuestas del admin y su diálogo.
- Bandeja de chats de la docente y el tab de Chat del padre.
- Día del niño, Agregar persona autorizada, Cambiar contraseña y selector de hijo.
- El estado de "avisos leídos" vive solo en el teléfono (`LeidosStore`); si se cambia de teléfono, vuelven a salir como nuevos. Un `POST /announcements/{id}/read` en el servidor lo resolvería.
- Las pruebas nuevas (avisos con alcance, plantillas) no se han corrido contra Postgres en esta revisión.

## Decisiones pendientes

Las que cambian el trabajo de las fases 1 a 5. Cada una trae una recomendación.

- [x] **¿Cómo le llega a la persona su código de invitación o de recuperación?** Resuelto el 18/oct: el admin lo manda por **WhatsApp** con un enlace que abre la app (sin correos). Para recuperar la contraseña, el admin genera un código nuevo desde la API. Pendiente original: Recomendado para el mínimo: el admin ve el código en pantalla y lo comparte (WhatsApp, en persona); el correo automático entra después. Así no hace falta un servidor de correo para la primera entrega, y "olvidé mi contraseña" se resuelve con un código nuevo que genera el admin.
- [x] **¿El admin puede leer los chats?** Resuelto: no puede (regla de la API); el botón se quitó el 18/oct. Pendiente original: Hoy `PadreDetalleScreen` tiene un botón "Abrir chat" que lleva a un tab que el admin no tiene. Recomendado: quitar el botón en el mínimo y decidirlo junto con el aviso de privacidad.
- [x] **¿Un padre puede tener varios hijos en la guardería?** Resuelto: sí; las pestañas del padre tienen selector de hijo y el admin puede vincular un padre a más niños (`POST /parents/{id}/children`). Original: Si sí, hace falta el selector de hijo en las tabs del padre (P1). `GET /me` ya regresa la lista.
- [x] **¿Dónde ve el padre los avisos del centro?** Resuelto: la campana en el encabezado de Hoy abre un panel de notificaciones (por ahora solo avisos; fotos, momentos y documentos quedan en el enum para después).
- [x] **Chat en el mínimo:** hecho con consulta cada 10 s (sin WebSocket). Original: recomendado preguntar por mensajes nuevos cada 10 segundos con el chat abierto (la API ya tiene `?after=`). Después: WebSocket como la práctica 12 y notificaciones con WorkManager como la práctica 8; el curso descarta FCM.
- [x] **Cliente Kotlin a mano o generado del OpenAPI.** Se hizo a mano con Retrofit. Original: Recomendado: a mano con Retrofit en el mínimo (son pocos endpoints y se aprende más); generarlo cuando la API se estabilice.
- [x] **Centro, ubicación, radio del checador y turnos:** el checador ya no usa ubicación y el horario del centro se configura en Ajustes → Horario. Original: recomendado dejarlos en el seed para el mínimo; la pantalla de configuración entra después.

## Lo que hay que actualizar en la propuesta de arquitectura

La propuesta del 9 de octubre se escribió antes del diagrama ER actual y antes de decidir el login propio. Hoy el código y el PDF de endpoints mandan. Las diferencias están en [03-api-estado.md](03-api-estado.md#diferencias-con-la-propuesta-de-arquitectura).
