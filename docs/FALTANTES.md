# Qué falta de la app

Foto a **2026-10-18**, armada leyendo [`PLAN-ACCION.md`](PLAN-ACCION.md), [`REQUISITOS.md`](REQUISITOS.md), [`RECORRIDO.md`](RECORRIDO.md), [`BITACORA.md`](BITACORA.md), [`plan/01-app-pantallas.md`](plan/01-app-pantallas.md) y los commits del día. **No se corrió la app ni las pruebas para esta lista**, así que "falta" significa "los documentos o el código dicen que no está".

Entrega final: **jueves 22 de octubre de 2026, 23:59**. Congelamiento de funciones nuevas: **lunes 19 por la noche**; martes y miércoles solo se corrige y se documenta.

Desde el 18/oct el checador ya no usa GPS, radio ni mapa (decisión del equipo), y el admin configura el horario del centro en Ajustes → Horario. Esas cosas ya no son pendientes.

Actualizado después de traer el commit `5b70069` de `main` (limpieza de la app). Varios documentos de plan van atrasados respecto al código, así que lo de la app se comprobó también leyendo el código: **Mi perfil → Mi cuenta con cambio de contraseña** y el **diálogo de agregar persona autorizada** ya existen, y los pendientes que decían lo contrario se quitaron de esta lista.

## 1. Requisitos sin cumplir (❌, 6 de 32)

| Requisito | Qué pasa |
|---|---|
| CRF-01 Bitácora de pagos | Fuera de alcance por decisión del equipo; se documenta como desviación. No se integra `feature/pagos` |
| RF-13 Desbloqueo con huella | Falta `BiometricPrompt`; la [decisión #4](DECISIONES-PENDIENTES.md) sigue pendiente |
| RNF04 Disponibilidad del 99 % | Sin despliegue ni monitoreo (existen `/health/live` y `/health/ready`) |
| HN1 Imprimir reportes para coordinación | Falta exportar a PDF o usar `PrintManager` |
| HN2 La asistente ve la llegada de los padres | Sin modelo ni pantalla |
| HN3 El padre ve el menú del día | No existe en la API ni en la app |

## 2. Requisitos parciales (🟡, 10 de 32)

| Requisito | Qué falta |
|---|---|
| CRF-03 Calendario de eventos y citas | Solo muestra asistencia del mes; faltan los modelos de eventos y citas |
| CRF-04 Dictado y consulta con IA | El dictado a texto sí funciona; la consulta con IA no existe ([decisión #10](DECISIONES-PENDIENTES.md)) |
| RF-03 Entrada y salida del niño | Solo hay `PRESENT`/`ABSENT` por día; falta la hora de entrada y de salida |
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
- P2: ajustar rutina (la maqueta `ajustarRutinaScreen.kt` se borró en la limpieza; no había ruta ni endpoint), salida de un niño con persona autorizada y notificaciones.

### Admin

- **Menú del día**: no existe.
- **Avisos con imágenes** (HN5).
- Los **turnos** (`shift`) siguen en la base y en la API, aunque el alta de la docente ya no los pide; decidir si se quitan del backend.
- Leer el **registro de auditoría** (`GET /audit-log`) desde la app: la API existe, no hay pantalla.
- P2: editar los datos de una docente, un infante o un padre (hoy solo cambian sala y baja), borrar avisos del historial y pedir un documento a la familia.

### Padre

- **Selector de hijo** en las tabs (HN4); la API ya devuelve la lista en `GET /me`.
- ~~Probar el diálogo de agregar persona autorizada~~: ya no aplica, porque las personas autorizadas pasan al admin (ver "Cambios pedidos por el equipo").
- **Paginación de fotos** y probar la galería y la solicitud de borrado con una foto real (falla #10 de [`RECORRIDO.md`](RECORRIDO.md)).
- Calendario: cambiar de mes y que tocar un día abra Hoy de ese día.
- Chat y menú del día (ver HN3).

## Cambios pedidos por el equipo (18/oct)

Son cambios nuevos a lo que ya existe. Varios mueven requisitos que hoy figuran como ✅, así que al hacerlos hay que actualizar [`REQUISITOS.md`](REQUISITOS.md) y los documentos de `docs/entrega/`.

### Docente

- **Título del chat con el niño y su familia.** Cada conversación debe mostrar el nombre del niño y el del padre o tutor, no solo un nombre genérico.
- **Pase de salida por alumno.** Cuando la docente termina todos los momentos de la rutina y finaliza el día, la app genera el pase de salida de cada alumno.
  - Por confirmar con el equipo: qué contiene el pase (hora, quién recoge) y quién lo ve. Hoy el plan solo menciona la "salida con persona autorizada" como P2 y marcada "por validar".
  - Si el pase debe nombrar a quién recoge, depende de que las personas autorizadas pasen al admin (ver más abajo).

### Padre

- **Tab "Documentos" pasa a llamarse "Mi hijo".** Debe mostrar información del niño que el padre pueda llenar y editar, por ejemplo alergias. Hoy el tab solo lista los documentos solicitados, sin datos del niño. Hace falta definir los campos y dónde se guardan (API y base).
- **Fotos: el padre las elimina directamente.** Se quita la solicitud de borrado (`POST /media/{id}/deletion-request`) y el padre borra por su cuenta.
  - Afecta RF-10: hoy se cumple con la solicitud y la revisión del admin. Con este cambio desaparece la pantalla de admin "Fotos que piden borrar" (`SolicitudesBorradoScreen`) y el endpoint de revisión.
  - Definir si el borrado afecta a las demás familias cuando la foto sale con varios niños: hoy el admin decide eso.
- **Personas autorizadas para recoger pasan al rol de admin.** Se quita esa parte de la pantalla del padre (incluido `AddAuthorizedDialog`) y el admin las administra por niño.
  - Falta pantalla del admin y confirmar que los endpoints `/authorized-pickups` aceptan al admin.

### Registro de usuarios nuevos

- **Alta por enlace de invitación.** ~~En vez de que el admin comparta un código…~~ **Hecho (rama `invitacion-whatsapp`), falta la URL pública.** El admin toca «Enviar por WhatsApp» y la persona recibe un enlace que abre la app con su correo y su código puestos; solo crea su contraseña. Sin correos: la prueba de identidad es que solo ella recibió el enlace.
  - Falta para que el enlace https abra la app: una **URL pública con HTTPS** que llegue a la API (`PUBLIC_BASE_URL`, decisión #7; para la demostración basta un túnel) y, para que Android la abra sin pasar por el navegador, la huella del certificado (`ANDROID_CERT_SHA256`) y el mismo dominio en la app (`INVITE_HOST`). Sin eso el mensaje lleva el código escrito y funciona igual a mano.

### Admin

- **Pantalla de Horario (Ajustes → Horario): mejorar el diseño y la experiencia de uso.** Hoy son tres horas con selector y siete chips de días. Falta decidir qué se mejora: claridad de las horas, validación visible, mostrar el efecto del cambio (retardo y cierre automático) y confirmar el guardado.

## 4. Deuda técnica

- `CodeaNavHost.kt` tiene unas 1,090 líneas y genera conflictos de merge.
- `CodeaViewModel` y `ParentViewModel` usan `mutableStateOf`; el marco pide `StateFlow`.
- Casi no hay pruebas instrumentadas (el commit de limpieza reporta 77 pruebas JVM en verde).
- Hecho en la limpieza (`5b70069`): se borraron los 7 repositorios en memoria, las 5 pantallas sin ruta y los `*SampleData.kt` del padre.
- Cambiar los valores de `guarderia-api/.env.example` (`POSTGRES_PASSWORD=123456`, `S3_SECRET_KEY=cambia-esta-clave`).
- El backend conserva `POST /staff-attendance/notify-coordination` y la tabla `coordination_notice` sin quien los use.
- `core/rules.py` duplica reglas de la app: si cambia una, cambiar las dos.
- Sin pruebas de carga ni de tiempos (RNF05).

## 5. Infraestructura y entrega

- **Aiven:** aplicar las migraciones `e74f0a5b3d43` y `f85a1b6c4e54` (necesita aprobación). La primera borra las coordenadas ya guardadas.
- Despliegue público con HTTPS ([decisión #7](DECISIONES-PENDIENTES.md)) y respaldos de la base.
- `API_BASE_URL_RELEASE` con HTTPS y un APK de release firmado (necesita un keystore y sus contraseñas).
- ~~Correo real con el código de invitación~~: ya no aplica; se manda por WhatsApp (ver «Alta por enlace de invitación»).
- Correr `pytest` de la API y las pruebas JVM de la app, y anotar lo que falle.
- Recorrer en el teléfono los tres roles con los cambios del 18/oct: el checador nuevo, Familias, Ajustes y Horario.
- Guion de la presentación de 15 minutos, video de respaldo, captura del Planner y entrega en Canvas.
- Poner al día [`ESTADO.md`](ESTADO.md) y [`../guarderia-api/PENDIENTES.md`](../guarderia-api/PENDIENTES.md) (foto del 16/oct) y marcar el bloque 4 de [`PLAN-ACCION.md`](PLAN-ACCION.md), cuyos documentos de `docs/entrega/` ya existen.

## 6. Decisiones sin aprobar

Las decisiones #2, #4, #5, #6, #7, #8, #9 y #10 de [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md) siguen en "Pendiente" y no se implementan hasta que el responsable las apruebe. La #1 (login con JWT) está implementada, pero le falta la aprobación formal.

## Orden sugerido antes del congelamiento

1. Selector de hijo del padre (HN4).
2. Los cambios pedidos por el equipo. Los más baratos son el título del chat y el nombre del tab "Mi hijo"; los más grandes son el enlace de invitación, mover las personas autorizadas al admin y el pase de salida. Con el congelamiento el lunes 19, conviene que el equipo decida cuáles entran y deje el resto como desviación documentada.
3. Migrar Aiven.
4. Recorrido completo en el teléfono y anotar fallas en [`RECORRIDO.md`](RECORRIDO.md).
5. Documentos y presentación.
