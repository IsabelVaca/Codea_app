# Qué falta de la app

Foto a **2026-10-18 por la noche**, armada leyendo [`PLAN-ACCION.md`](PLAN-ACCION.md), [`REQUISITOS.md`](REQUISITOS.md), [`RECORRIDO.md`](RECORRIDO.md), [`BITACORA.md`](BITACORA.md), los commits del día y **el código de `main`** (reconocimiento hecho leyendo el código, sin teléfono). Lo que dice "hecho" está compilado y con pruebas (**324 de la API corridas hoy en verde** y 125 JVM contadas); lo que dice "visto en el emulador" se abrió en pantalla. **Lo demás no se ha recorrido en un teléfono**, así que "falta" significa "los documentos o el código dicen que no está".

Entrega final: **jueves 22 de octubre de 2026, 23:59**. Congelamiento de funciones nuevas: **lunes 19 por la noche**; martes y miércoles solo se corrige y se documenta.

Desde el 18/oct el checador ya no usa GPS, radio ni mapa (decisión del equipo), y el admin configura el horario del centro en Ajustes → Horario. Esas cosas ya no son pendientes.

Puesto al día después del PR #9 (`e2e5564`). **Los documentos ya se pusieron al día contra el código** el 18/oct por la noche ([`ESTADO.md`](ESTADO.md), [`REQUISITOS.md`](REQUISITOS.md), [`PLAN-ACCION.md`](PLAN-ACCION.md), [`ENTREGA-3.md`](ENTREGA-3.md), [`RECORRIDO.md`](RECORRIDO.md), [`plan/`](plan/README.md), `guarderia-api/PENDIENTES.md` y los ocho documentos de `docs/entrega/`). Lo que se comprobó leyendo el código: **Mi perfil → Mi cuenta con cambio de contraseña** existe, `ParentViewModel` sigue con el primer hijo, el padre conserva «Pedir que se borre» y la pantalla del admin para resolverlo no tiene ruta, la bitácora de auditoría no tiene pantalla, y la matriz de roles (84 rutas) coincide con el código.

## 1. Requisitos sin cumplir (❌, 6 de 32; en total 15 ✅ y 11 🟡, ~64 %)

| Requisito | Qué pasa |
|---|---|
| CRF-01 Bitácora de pagos | Fuera de alcance por decisión del equipo; se documenta como desviación. No se integra `feature/pagos` |
| RF-13 Desbloqueo con huella | Falta `BiometricPrompt`; la [decisión #4](DECISIONES-PENDIENTES.md) sigue pendiente |
| RNF04 Disponibilidad del 99 % | Sin despliegue ni monitoreo (existen `/health/live` y `/health/ready`) |
| HN1 Imprimir reportes para coordinación | Falta exportar a PDF o usar `PrintManager` |
| HN2 La asistente ve la llegada de los padres | Sin modelo ni pantalla |
| HN3 El padre ve el menú del día | No existe en la API ni en la app |

## 2. Requisitos parciales (🟡, 11 de 32)

| Requisito | Qué falta |
|---|---|
| CRF-03 Calendario de eventos y citas | Solo muestra asistencia del mes; faltan los modelos de eventos y citas |
| CRF-04 Dictado y consulta con IA | El dictado a texto sí funciona; la consulta con IA no existe ([decisión #10](DECISIONES-PENDIENTES.md)) |
| RF-03 Entrada y salida del niño | La **salida** ya se registra (pase de salida: hora y quién lo recogió); falta la hora de **entrada** y que la familia vea la salida |
| RF-10 Borrar una foto | **Bajó de ✅ a 🟡 el 18/oct:** el padre sigue pidiendo el borrado y el admin ya no tiene dónde resolverlo (ver «Cambios pedidos por el equipo») |
| RF-05 Reporte para varios niños | La API está probada; la pantalla de selección no se ha recorrido en el teléfono |
| RF-09 Fotografía de la actividad | La captura con la cámara no se ha probado; usa `TakePicturePreview` (miniatura) |
| RF-11 y RNF07 Sin conexión | Solo funciona la asistencia de Mi sala. Faltan reportes, fotos, chat, documentos y avisos |
| RNF05 Consulta de fotos en menos de 4 s | URLs firmadas y paginación por cursor, pero nadie lo ha medido |
| HN4 Varios hijos por padre | El modelo lo permite; `ParentViewModel` usa solo el primer hijo |
| HN5 Avisos con imágenes | Falta `media_id` en los avisos, en la API y en la pantalla de publicar |

## 3. Por rol

### Docente

- **"Mis documentos"** se quitó del Menú en la limpieza (ya no dice "Próximamente"); la función no existe. Si el cliente la pide, hay que hacerla desde cero.
- **Editar el perfil** (nombre, teléfono, foto, `PATCH /me`): Menú → Mi perfil abre Mi cuenta, que sirve para ver los datos y cambiar la contraseña, pero no para editarlos (P2).
- **Reportes sin conexión**: no se hicieron porque llevan fotos.
- **Chat en tiempo real**: hoy se consulta con `?after=`; el marco permite SSE o WebSocket.
- Recorrer el flujo de **varios niños a la vez** y la **cámara** en el teléfono.
- Probar de punta a punta el **pase de salida**: registrar la salida de cada niño, «Corregir», «Finalizar día» y la pantalla «Día finalizado». En el emulador solo se vieron la tarjeta de Mi día y la lista del pase.
- P2: ajustar rutina (la maqueta `ajustarRutinaScreen.kt` se borró en la limpieza; no había ruta ni endpoint), salida de un niño con persona autorizada y notificaciones.

### Admin

- **Menú del día**: no existe.
- **Avisos con imágenes** (HN5).
- **Centro ya no muestra «Fotos que piden borrar»** (se quitó el 18/oct, a petición del equipo). La pantalla de revisión (`SolicitudesBorradoScreen`, su ruta, su ViewModel y los endpoints de `media/deletion-requests`) sigue en el código sin forma de llegar a ella. Mientras el padre conserve «Pedir que se borre», sus solicitudes se guardan y nadie las resuelve.
- Hay un **dilema ético** ([`entrega/etica-legal.md`](entrega/etica-legal.md), dilema 1: fotos y derecho al borrado) que se escribió con la revisión del admin; si el padre borra directo hay que reescribirlo.
- Los **turnos** (`shift`) siguen en la base y en la API, aunque el alta de la docente ya no los pide; decidir si se quitan del backend.
- Leer el **registro de auditoría** (`GET /audit-log`) desde la app: la API existe, no hay pantalla.
- P2: editar los datos de una docente, un infante o un padre (hoy solo cambian sala y baja), borrar avisos del historial y pedir un documento a la familia.

### Padre

- ~~Selector de hijo en las tabs (HN4)~~ **Hecho el 18/oct, sin verlo en un teléfono.** Tira con los nombres arriba de Hoy, Calendario, Fotos y Mi hijo, solo con más de un hijo. Falta probarlo con una familia de dos hijos (el seed trae uno por padre).
- ~~Probar el diálogo de agregar persona autorizada~~: ya no aplica, porque las personas autorizadas pasan al admin (ver "Cambios pedidos por el equipo").
- **Probar «Mi hijo» en el teléfono**: guardar alergias y demás datos y ver el cambio en la ficha de la docente.
- **Paginación de fotos** y probar la galería con una foto real (falla #10 de [`RECORRIDO.md`](RECORRIDO.md)). La solicitud de borrado está por quitarse (ver abajo).
- Calendario: cambiar de mes y que tocar un día abra Hoy de ese día.
- Chat y menú del día (ver HN3).

## Cambios pedidos por el equipo (18/oct)

Son cambios nuevos a lo que ya existe. Varios mueven requisitos que hoy figuran como ✅, así que al hacerlos hay que actualizar [`REQUISITOS.md`](REQUISITOS.md) y los documentos de `docs/entrega/`.

### Docente

- ✅ **Chats con el niño y su familia (hecho el 18/oct; compila y con pruebas, sin verlo en pantalla).** En la bandeja de la docente cada chat muestra el nombre del niño y, debajo, sus tutores («Mamá: Laura Iglesias · Papá: Jorge Flores»); al abrir el chat, el niño va de título y los tutores arriba. El padre no ve esa línea. La API manda `child.guardians[]` en `GET /conversations`. Si el niño no tiene tutor registrado no sale la línea (en el seed solo Mateo Iglesias y Valentina Ruiz tienen uno).
- ✅ **Pase de salida por alumno (hecho el 18/oct; visto en el emulador, sin registrar salidas de punta a punta).** El pase es el último paso de la rutina en Mi día: antes de terminar es una fila gris con candado y, al confirmar el último momento, pasa a una tarjeta violeta «SIGUIENTE PASO» y la pantalla baja sola hasta ella. La docente registra la hora de salida de cada niño y elige quién lo recoge (las personas autorizadas que registró el admin y los tutores); con todos fuera aparece «Finalizar día», que registra su salida y termina su turno. Ver [`guarderia-api/app/modules/departures/README.md`](../guarderia-api/app/modules/departures/README.md).
  - Decisiones que tomé y conviene confirmar con el equipo: solo salen los niños con pase de lista de hoy; los tutores del niño aparecen siempre como opción (si no, un niño sin personas autorizadas bloquearía el «Finalizar día»); la hora la pone el servidor; hay «Corregir» por si se eligió a la persona equivocada; «Finalizar día» pide confirmación; el pase necesita conexión (no hay cola sin conexión).
  - ✅ La familia ve la hora de salida y quién lo recogió como último evento de Hoy (hecho el 18/oct; API probada, sin verlo en un teléfono).

### Padre

- ✅ **Tab «Documentos» ahora es «Mi hijo» (hecho el 18/oct, sin probar en el teléfono).** Muestra los datos del niño (nombre, fecha de nacimiento, edad, sala y tutores), deja escribir y editar alergias, condiciones médicas, intolerancias, medicamentos y otras indicaciones (hasta 1000 caracteres cada una; `PATCH /children/{id}/health`) . Ya no tiene subida de documentos (se quitó a petición del equipo; la API de documentos sigue en el backend sin pantalla). La docente ve lo que escribe la familia en la ficha del niño. Los datos del niño (nombre, nacimiento, sala) no los edita la familia: los corrige la dirección.
- **Fotos: el padre las elimina directamente. *(Casi hecho.)*** `DELETE /media/{id}` y el botón «Borrar» con confirmación en Fotos (18/oct; API con pruebas, sin verlo en un teléfono).
  - Hecho: Centro ya no muestra «Fotos que piden borrar», el endpoint y el botón. **Falta:** decidir qué pasa con la foto que sale con niños de otras familias (hoy `409 MEDIA_SHARED` y nadie puede resolverlo desde la app) y quitar o reutilizar el código de la revisión del admin.
  - Afecta RF-10: hoy se cumple con la solicitud y la revisión del admin, y con lo ya quitado queda sin pantalla de admin. Hay que actualizar [`REQUISITOS.md`](REQUISITOS.md).
  - Regla que apliqué (confirmar con el equipo): el padre solo borra si todos los niños de la foto son hijos suyos.
- ✅ **Personas autorizadas para recoger pasan al rol de admin (hecho el 18/oct, sin probar en el teléfono).** Se quitó esa parte de la pantalla del padre (y `AddAuthorizedDialog`). El admin las administra por niño en Ajustes → Personas autorizadas (nombre, parentesco y teléfono). En la API, agregar y quitar son solo del admin; consultar sigue abierto a quien ve al niño. Se registran por niño, no por padre: con dos hijos, se capturan en cada uno.

### Registro de usuarios nuevos

- **Alta por enlace de invitación.** ~~En vez de que el admin comparta un código…~~ **Hecho y ya en `main`; falta la URL pública y probarlo en el teléfono.** El admin toca «Enviar por WhatsApp» y la persona recibe un enlace que abre la app con su correo y su código puestos; solo crea su contraseña. Sin correos: la prueba de identidad es que solo ella recibió el enlace.
  - Falta para que el enlace https abra la app: la URL pública ya está configurada (`PUBLIC_BASE_URL=https://api.codeaapp.site`, túnel aprobado en la decisión #7; falta levantarlo) y, para que Android la abra sin pasar por el navegador, la huella del certificado (`ANDROID_CERT_SHA256`) y el mismo dominio en la app (`INVITE_HOST`). Sin eso el mensaje lleva el código escrito y funciona igual a mano.

### Admin

- **Pantalla de Horario (Ajustes → Horario): mejorar el diseño y la experiencia de uso.** Hoy son tres horas con selector y siete chips de días. Falta decidir qué se mejora: claridad de las horas, validación visible, mostrar el efecto del cambio (retardo y cierre automático) y confirmar el guardado.

## 4. Deuda técnica

- `CodeaNavHost.kt` tiene unas 1,090 líneas y genera conflictos de merge.
- `CodeaViewModel` y `ParentViewModel` usan `mutableStateOf`; el marco pide `StateFlow`.
- Casi no hay pruebas instrumentadas (106 pruebas JVM en verde).
- Código de las solicitudes de borrado sin forma de llegar a él (ver Admin): `SolicitudesBorradoScreen`, `SolicitudesBorradoViewModel`, `AdminSolicitudesBorradoRoute` y `SolicitudBorradoApiRepository`.
- Hecho en la limpieza (`5b70069`): se borraron los 7 repositorios en memoria, las 5 pantallas sin ruta y los `*SampleData.kt` del padre.
- Cambiar los valores de `guarderia-api/.env.example` (`POSTGRES_PASSWORD=123456`, `S3_SECRET_KEY=cambia-esta-clave`).
- El backend conserva `POST /staff-attendance/notify-coordination` y la tabla `coordination_notice` sin quien los use.
- `core/rules.py` duplica reglas de la app: si cambia una, cambiar las dos.
- Sin pruebas de carga ni de tiempos (RNF05).

## 5. Infraestructura y entrega

- ~~**Aiven:** aplicar las migraciones pendientes~~ **Hecho el 18/oct por la noche** (con permiso): Aiven está en `a07c3d8e6f76`, la última.
- Despliegue público con HTTPS ([decisión #7](DECISIONES-PENDIENTES.md)) y respaldos de la base.
- Un APK de release firmado (necesita un keystore y sus contraseñas). La dirección HTTPS ya está: `https://api.codeaapp.site/api/v1/`; falta levantar el túnel (`guarderia-api/README.md`).
- ~~Correo real con el código de invitación~~: ya no aplica; se manda por WhatsApp (ver «Alta por enlace de invitación»).
- Recorrer en el teléfono los tres roles con los cambios del 18/oct: el checador nuevo, Familias, Ajustes, Horario, Personas autorizadas, Mi hijo, el pase de lista, Mi sala, el pase de salida, el chat con fotos y la invitación. La lista concreta está al final de [`RECORRIDO.md`](RECORRIDO.md).
- **Documento de arquitectura (diagrama de capas y relaciones): no existe** y la rúbrica ([`ENTREGA-3.md`](ENTREGA-3.md), parte 4) lo pide. Hay texto en los READMEs y en [`MARCO-TECNICO.md`](MARCO-TECNICO.md), pero no hay un entregable.
- Huecos **[completar por el equipo]** en `docs/entrega/`: responsable y canal del aviso de privacidad, distribución del trabajo (4.2), retrospectiva (5.2) y las filas de `plan-implantacion.md`.
- Guion de la presentación de 15 minutos, video de respaldo, captura del Planner y entrega en Canvas.
- ~~Poner al día `ESTADO.md`, `PENDIENTES.md` y `PLAN-ACCION.md`~~ Hecho el 18/oct por la noche.

## 6. Decisiones sin aprobar

Las decisiones #2, #4, #5, #6, #7, #8, #9 y #10 de [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md) siguen en "Pendiente" y no se implementan hasta que el responsable las apruebe. La #1 (login con JWT) está implementada, pero le falta la aprobación formal.

## Orden sugerido antes del congelamiento

1. ~~Selector de hijo del padre (HN4)~~ Hecho; falta probarlo.
2. Los cambios pedidos que quedan: la foto con niños de varias familias, la URL pública del enlace de invitación y mejorar la pantalla de Horario. Con el congelamiento el lunes 19, conviene que el equipo decida cuáles entran y deje el resto como desviación documentada.
3. ~~Migrar Aiven~~ Hecho el 18/oct: Aiven está en `a07c3d8e6f76`.
4. Recorrido completo en el teléfono y anotar fallas en [`RECORRIDO.md`](RECORRIDO.md).
5. Escribir el documento de arquitectura, completar los huecos de `docs/entrega/`, repasar la matriz de trazabilidad con el estado final y preparar la presentación.
