# Plan para llegar al mínimo funcional

Revisión del 16 de octubre de 2026 sobre la rama `backend` (commit `12da735`), **puesta al día el 17 de octubre** con lo que se hizo después (ver [Avance al 17 de octubre](#avance-al-17-de-octubre)). La app ya tiene las pantallas principales de los tres roles y buena parte ya habla con la API. La API tiene todos los endpoints de la primera entrega y los de avisos, plantillas de rutina, centro e invitaciones. Lo que falta es **terminar de unir las dos partes**, **agregar las pantallas que faltan** (login, chat del padre, bandeja de chats, cuenta y cierre de sesión) y **cambiar Firebase por un login propio con correo, contraseña y JWT**.

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

La lista vigente de endpoints está en [`../api/endpoints.md`](../api/endpoints.md). Referencias anteriores: [endpoints de la primera entrega (PDF, histórico)](../api/endpoints-api-codea.pdf) y la [propuesta de arquitectura](https://claude.ai/artifact/K4CqXjp82tkRLfpKDRdV5H).

## Resumen

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

- [ ] **¿Cómo le llega a la persona su código de invitación o de recuperación?** Recomendado para el mínimo: el admin ve el código en pantalla y lo comparte (WhatsApp, en persona); el correo automático entra después. Así no hace falta un servidor de correo para la primera entrega, y "olvidé mi contraseña" se resuelve con un código nuevo que genera el admin.
- [ ] **¿El admin puede leer los chats?** Hoy `PadreDetalleScreen` tiene un botón "Abrir chat" que lleva a un tab que el admin no tiene. Recomendado: quitar el botón en el mínimo y decidirlo junto con el aviso de privacidad.
- [ ] **¿Un padre puede tener varios hijos en la guardería?** Si sí, hace falta el selector de hijo en las tabs del padre (P1). `GET /me` ya regresa la lista.
- [x] **¿Dónde ve el padre los avisos del centro?** Resuelto: la campana en el encabezado de Hoy abre un panel de notificaciones (por ahora solo avisos; fotos, momentos y documentos quedan en el enum para después).
- [ ] **Chat en el mínimo:** recomendado preguntar por mensajes nuevos cada 10 segundos con el chat abierto (la API ya tiene `?after=`). Después: WebSocket como la práctica 12 y notificaciones con WorkManager como la práctica 8; el curso descarta FCM.
- [ ] **Cliente Kotlin a mano o generado del OpenAPI.** Recomendado: a mano con Retrofit en el mínimo (son pocos endpoints y se aprende más); generarlo cuando la API se estabilice.
- [ ] **Centro, ubicación, radio del checador y turnos:** recomendado dejarlos en el seed para el mínimo; la pantalla de configuración entra después.

## Lo que hay que actualizar en la propuesta de arquitectura

La propuesta del 9 de octubre se escribió antes del diagrama ER actual y antes de decidir el login propio. Hoy el código y el PDF de endpoints mandan. Las diferencias están en [03-api-estado.md](03-api-estado.md#diferencias-con-la-propuesta-de-arquitectura).
