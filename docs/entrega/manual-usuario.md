# Manual de usuario de Codea

Estado a **2026-10-18**, rama `integracion`, commit `22c0008`. Está escrito a partir de [`docs/plan/01-app-pantallas.md`](../plan/01-app-pantallas.md), de los textos de la app (`app/src/main/res/values/strings.xml`), de los archivos de `app/src/main/java/mx/tec/codea/ui/screens/` y del recorrido en teléfono físico de [`RECORRIDO.md`](../RECORRIDO.md). **No se ejecutó la app para escribirlo**: si una pantalla cambió después, manda lo que se vea en el teléfono.

Lo que todavía no existe se marca como **«Próximamente»** (la propia app muestra «Muy pronto» en lo que no está construido) o como **«No disponible hoy»** cuando ni siquiera hay una pantalla que lo anuncie.

Codea es la aplicación de la estancia infantil **La Oruga** para llevar el día de los niños y comunicar al centro, a las docentes y a las familias. Tiene **tres perfiles**: administrador, docente y padre o tutor. Cada persona entra con su correo y su contraseña y ve únicamente las pantallas de su perfil.

---

## 1. Antes de empezar

| Qué necesitas | Detalle |
|---|---|
| Un teléfono Android | Versión 7.0 (nivel de API 24) o más nueva |
| Conexión a internet | **Casi todo necesita conexión.** La única excepción es la **asistencia de Mi sala** (docente), que funciona sin conexión y se envía sola al volver la red (ver 2.4). Reportes, fotos, chat, checador, documentos y avisos necesitan red |
| Una cuenta | La crea el administrador del centro; no te puedes registrar solo |
| Un código de acceso | Lo recibes del administrador la primera vez. Tiene el formato `K7QX-M4PZ` |

### 1.1 Primera vez: activar tu cuenta

1. Abre Codea. Aparece «Inicia sesión».
2. Toca **«Es mi primera vez: activar mi cuenta»**.
3. Escribe tu **correo** (el que registró la dirección), el **código** que te dieron y la **contraseña** que quieras usar. Repítela.
4. Toca **«Activar y entrar»**. Quedas dentro con tu perfil.

El código se puede escribir con o sin guion y en mayúsculas o minúsculas. **Solo se puede usar una vez y dura 7 días.** Si venció, pide otro a la dirección.

Reglas de la contraseña: de **9 a 128 caracteres** y no puede ser igual a tu correo. Con 8 no pasa.

### 1.2 Entrar cada día

1. Escribe tu correo y tu contraseña y toca **«Entrar»**.
2. Si tienes más de un perfil (por ejemplo, eres docente y también tutora de un niño), la app te pregunta **«¿Qué tipo de usuario eres?»**. Si tu cuenta todavía no tiene perfil, te lo dice: «Pide ayuda en la dirección».

Si fallas varias veces seguidas (5 con el mismo correo), el sistema te bloquea unos minutos. Espera y vuelve a intentar.

La sesión se renueva sola mientras uses la app. Si pasan **7 días sin abrirla**, tendrás que escribir tu contraseña otra vez.

### 1.3 Olvidé mi contraseña

La app no manda correos. Pide a la dirección un **código nuevo** y úsalo en «Activar mi cuenta» con tu correo y una contraseña nueva. Así se cierran tus sesiones anteriores.
> Hoy el administrador no tiene un botón en la app para generar ese código de recuperación a alguien que ya entró; solo puede hacerlo desde la API (`POST /users/{id}/access-code`) **[no disponible hoy desde la pantalla]**.

### 1.4 Mi cuenta (los tres perfiles)

| Perfil | Cómo se llega |
|---|---|
| Administrador | Toca el avatar en el encabezado de **Centro** |
| Padre o tutor | Toca el botón junto a la campana en **Hoy** |
| Docente | **Menú → Mi perfil** |

Ahí ves tu nombre y correo (el padre también ve «Mis hijos») y puedes:

- **Cambiar contraseña:** escribe la actual y la nueva dos veces. Al cambiarla se **cierran tus sesiones en otros dispositivos**.
- **Cerrar sesión:** pide confirmación («Tendrás que volver a entrar con tu correo y tu contraseña»).

Editar tu nombre, teléfono o foto: **Próximamente**.

### 1.5 Si algo sale mal

| Mensaje | Qué hacer |
|---|---|
| «Tu acceso a la app está desactivado» | La dirección te dio de baja. Habla con ella |
| «Demasiados intentos; espera unos minutos» | Espera unos 15 minutos |
| La app te regresa a «Inicia sesión» sola | Tu sesión terminó (cambio de contraseña, baja o más de 7 días). Entra de nuevo |
| «Muy pronto» con un candado | Esa sección no está construida |

---

## 2. Docente

Barra inferior: **Mi día · Mi sala · Chats · Menú**.

### 2.1 Antes de empezar el día: Mi día y el Checador

1. **Mi día** dice «Aún el día no comienza»: «Ve al checador a registrar tu entrada cuando estés en la estancia». Toca **Checador** (también está en **Menú**).
2. La pantalla «Registrar entrada» muestra la fecha y la hora del teléfono y tu turno. **No pide ubicación ni permisos**: el checador no usa GPS ni mapa.
   - **A tiempo:** toca **«Registrar mi entrada»**.
   - **Tarde:** la app te avisa «Llegaste después de tu hora»; se registra igual con **retardo**. Puedes escribir el **motivo del retardo** (opcional) y luego tocar «Registrar con retardo». El retardo queda marcado en tu registro; la app no tiene aviso a coordinación.
3. La hora que queda guardada es la del **servidor**, no la del teléfono.
4. Al registrar te sale «¡Entrada registrada!» y la app te lleva sola a **«Rutina de hoy»**, de solo lectura. Toca **«Comenzar el día»**.

**Importante sobre tu privacidad:** el checador guarda solo la **hora de entrada, la hora de salida, el retardo y el motivo** que escribas. **No guarda tu ubicación.** Ver [`aviso-privacidad.md`](aviso-privacidad.md).

**Salida.** Al terminar tu turno vuelve al checador: la pantalla se llama **«Tu jornada de hoy»** y muestra el botón **«Registrar mi salida»**. Al registrarla dice «Salida registrada a las …». Una salida repetida no cambia la hora ya registrada. **Si se te olvida, a las 9:00 pm el servidor cierra tu día solo:** la salida queda a las 9:00 pm y la pantalla dice «Salida automática a las 21:00». En **Menú**, la tarjeta del checador dice «Entrada 13:59 · falta registrar tu salida» y, después, «Entrada 13:59 · Salida 14:01».

### 2.2 Mi día: la rutina

La pantalla muestra los **subprocesos (momentos) de la rutina** de tu sala con su avance («2 de 9»). Cada uno puede estar terminado, **en curso** o pendiente. La rutina sale de la plantilla que el administrador asignó a tu sala (o la predeterminada).

- Toca un momento para ver cuántos **reportes** tiene.
- **«Realizar reporte»** (solo en el momento en curso): ver 2.3.
- **«Confirmar subproceso»**: cierra el momento en curso y arranca el siguiente.

### 2.3 Hacer un reporte

1. En el momento en curso toca **«Realizar reporte»**.
2. Elige cómo levantarlo: **Escrito**, **Dictado** o **Con foto**. Las tres guardan lo mismo y puedes cambiar después.
3. Elige el **tipo**: Incidencia, Logro, Salud o Conducta.
4. Escribe **«¿Qué pasó?»** (mínimo **10 caracteres**) o pulsa **«Dictar en vez de escribir»**. El dictado usa el reconocedor de voz del teléfono y pide permiso de micrófono; si tu teléfono no tiene servicio de voz, la app te lo dice.
5. Con foto: toma la foto con la cámara (la app pide permiso de cámara). Puedes cambiarla o quitarla.
6. Toca **«Continuar · elegir niños»**.
7. En **«¿A quién le pasó?»** marca uno o varios niños de tu sala. Verás quién lo verá: «Familia de … · coordinación de la estancia. El grupo no lo ve».
8. Toca **«Guardar reporte · N niños»**.

> **Cuidado al redactar:** la descripción es **una sola** para todos los niños que marques, y cada familia la leerá. No escribas el nombre de otro niño en el texto. Los niños que estén **ausentes** no se pueden elegir (el sistema lo rechaza). Un reporte guardado **no se puede editar ni anular** hoy.

La foto del reporte se ve como una miniatura de baja calidad (la app usa la vista previa de la cámara); fotos de más calidad: **Próximamente**.

### 2.4 Mi sala

Muestra el nombre de **tu sala**, cuántos niños están presentes y cuántos chats tienes sin leer. Para cada niño:

- **Presente / Falta:** marca la asistencia del día. Sin registro, el niño cuenta como presente.
- **Chat:** abre la conversación con su familia.

**Mi sala sin conexión (solo la asistencia).** Si no hay red, puedes seguir marcando **Presente / Falta**:

1. La marca se ve de inmediato y aparece el aviso «Sin conexión: la marca se enviará sola cuando vuelva la red».
2. En lo alto de Mi sala sale un banner: **«1 cambio sin enviar · se manda solo cuando vuelva la conexión»** (o «N cambios sin enviar · se mandan solos…»).
3. Puedes cerrar la app: las marcas pendientes se conservan en el teléfono. Al abrirla sin conexión, Mi sala se muestra con **lo último que se guardó** y el aviso «Sin conexión: se muestra lo último que se guardó en el teléfono».
4. Cuando vuelve la red y la docente que las hizo tiene sesión, la app las envía **sola**, sin que la abras (en el caso probado tardó menos de un minuto); el banner desaparece. Si marcas varias veces al mismo niño el mismo día, solo se manda la última.
5. Una marca que el servidor rechaza de forma definitiva (por ejemplo, un niño que ya no está en tu sala) se avisa y no se reintenta.

**Qué sí necesita red:** reportes, fotos, chats, el checador (entrada y salida), documentos, avisos y todas las pantallas del administrador y del padre. Si abres Mi sala sin red por primera vez y no hay nada guardado, no se puede cargar.

**Privacidad en el teléfono:** para mostrar Mi sala sin conexión la app guarda en el teléfono los **nombres de los niños de tu sala** y su asistencia; esa copia **no está cifrada** y se borra cuando cierras sesión o la sesión termina. Las marcas pendientes (solo número de niño, estado, fecha y quién las hizo) no se borran al cerrar sesión: se conservan y **solo se envían con la sesión de la docente que las hizo**; si entra otra docente en el mismo teléfono no se mandan ni se cuentan con la suya. Detalle en [`aviso-privacidad.md`](aviso-privacidad.md), 2.5.
- **Resumen del día** (en la tarjeta de cada niño, «Resumen del día ›»): la docente captura lo que verá la familia en «Hoy». La pantalla se llama «Resumen de [niño]» y tiene **Ánimo**, **Comida** (por ejemplo «Casi todo»), **Cambios de pañal** (con − y +), **Siesta** (de 15 en 15 minutos, hasta 6 horas) y **Nota para la familia** («Cómo estuvo su día», opcional); toca **«Guardar resumen»**. Solo se manda lo que llenaste. Si la familia dejó una nota para hoy, arriba aparece «La familia te dejó una nota». En la tarjeta del niño también ves la etiqueta **«Nota de la familia hoy»**. Se probó de punta a punta en el teléfono: la mamá lo vio en Hoy.

### 2.5 Chats

La **bandeja** lista una conversación por niño (familia de …), con no leídos y el último mensaje. Abre una, escribe y toca enviar. Si falla, aparece «No se envió · toca para reintentar»; reintentar **no duplica** el mensaje. Aparece «leído» cuando la otra persona lo vio.

- La app consulta mensajes nuevos **cada 10 segundos**; no es tiempo real (Próximamente).
- El botón **«+» para adjuntar** no hace nada todavía **[Próximamente]**.
- Solo ves las conversaciones de los niños de tu sala.

### 2.6 Menú

| Opción | Qué hace hoy |
|---|---|
| Tu tarjeta | Nombre, turno y si ya registraste entrada |
| **Checador** | Abre el checador (entrada, y después salida; si no la registras, se cierra sola a las 9 pm). Su tarjeta muestra la entrada y, si ya la hay, la salida |
| **Mis documentos** (contrato, constancias) | **Próximamente.** Abre «Muy pronto» |
| **Mi perfil** | Abre Mi cuenta (ver 1.4) |
| **Avisos de la fundación** | Lista los avisos del centro y de tu sala, en «Nuevos» y «Anteriores». Muestra un contador de nuevos |
| **Cerrar sesión** | Pide confirmación |

---

## 3. Administrador

Barra inferior: **Centro · Docentes · Familias · Ajustes**.

> El administrador **ve a todos los niños y a todo el personal**. No puede leer los chats entre familias y docentes (no tiene pestaña de chat).

### 3.1 Centro

- **Salas con su ocupación** («8/12 niños»). Toca una sala para su **detalle**: ver sus niños y asistentes, cambiar el **cupo**, asignar asistentes, mover niños y **«Eliminar esta sala»** (si tiene niños, el sistema lo rechaza; sus asistentes quedan sin sala).
- **Crear sala:** «Define su nombre, cupo y quién la atiende» (se elige una asistente).
- **Publicar aviso:** escribe el texto y elige a quién va: **«Para todos · todas las salas»** o **«Solo» una sala**. Debajo ves el **historial** de avisos. Los avisos **no se pueden borrar** ni llevar imágenes **[Próximamente]**.
- **Fotos que piden borrar:** una tarjeta que dice cuántas solicitudes de las familias esperan decisión («N por revisar» o «No hay solicitudes pendientes»). Toca **«Ver»**: para cada solicitud ves la foto, los niños etiquetados, quién la pidió, la fecha y el motivo (si lo escribió). Elige **«Borrar la foto»** (pide confirmación: «Se borra del servidor y desaparece de las galerías de todas las familias. No se puede deshacer») o **«Conservar»**. Si la foto sale con varios niños, al borrarla desaparece para **todas** esas familias. Esta pantalla es nueva y no se ha probado en un teléfono con una foto real.

### 3.2 Docentes

1. Lista de docentes con sala, hora de checada del día y número de niños.
2. **«+ Dar de alta a una asistente»:** nombre, teléfono de contacto, correo electrónico, sala y turno (Matutino o Vespertino).
3. La app muestra un **código de acceso** en una ventana: **solo se ve ahora**. Toca **«Compartir»** (WhatsApp, en persona…) y **«Listo»**. La persona lo usa en «Activar mi cuenta». La app **no manda correos**.
4. **Detalle de una docente:** cambiar de sala, **dar de baja** (le corta el acceso al instante) o **reactivar**.

### 3.3 Familias

Una sola pestaña para los niños y para quienes los recogen. Arriba hay un resumen («N infantes · N padres y tutores») y un selector: **Infantes** o **Padres y tutores**.

**Infantes**

1. Lista de infantes inscritos.
2. **«+ Inscribir a un infante»:** nombre(s), apellido(s) (opcional), **fecha de nacimiento real en formato dd/mm/aaaa** (por ejemplo 14/03/2022; la app valida que exista, que no sea futura y que el niño tenga de 0 a 12 años), alergias o notas médicas (opcional) y **sala asignada**. Al guardar queda inscrito y «listo para vincular» a un tutor.
3. **Detalle:** cambiar de sala (también «Sacar de la sala»), **dar de baja** o **reactivar la inscripción**, y ver los datos del tutor con **«Consultar info»**.

Corregir el nombre, la fecha de nacimiento o las alergias de un infante ya inscrito: **No disponible hoy** (solo cambia la sala).

**Padres y tutores**

1. Lista de padres y tutores («familias con acceso a la app»).
2. **«+ Inscribir a un padre o tutor»:** nombre, teléfono de contacto, correo electrónico, parentesco (Mamá, Papá, Tutor o Tutora) y uno o más hijos que ya estén inscritos. Se muestra su código de acceso.
3. **Detalle:** **quitar o devolver el acceso**, y **«Reenviar invitación»**, que genera un **código nuevo** y lo muestra.
4. El botón **«Abrir chat»** del detalle apunta a la pestaña Chats, que el administrador no tiene: **no funciona para este perfil** (según la lectura del código; no se probó).

### 3.4 Ajustes

El menú de lo que el administrador configura. Cada fila abre su pantalla:

- **Rutinas:** lista de **plantillas de rutina** con su número de momentos. **«+ Nueva rutina»** pide nombre y momentos; cada momento tiene una categoría (llegada, comida, actividad, siesta, higiene, entrega) y hay un **catálogo de momentos** (también se puede agregar uno propio). Se pueden **ordenar** los momentos (la pantalla de arrastre usa una librería pendiente de aprobar; ver `DECISIONES-PENDIENTES.md`, decisión #8) y **asignar una plantilla a una sala**. Cambiar una plantilla **no modifica un día que ya empezó**: la rutina del día se copia cuando la docente checa.
- **Mi cuenta:** perfil, cambiar contraseña y cerrar sesión (ver 1.4).

El checador **ya no tiene ajustes**: no usa ubicación ni radio.

### 3.5 Lo que el administrador todavía no puede hacer desde la app

| Función | Estado |
|---|---|
| Quitar solo a un niño de una foto de grupo; revisar solicitudes de borrado de **documentos** | **Próximamente** (la revisión de solicitudes de fotos ya existe, ver 3.1) |
| Ver la **bitácora de auditoría** | No hay pantalla; existe `GET /audit-log` en la API |
| Editar datos de docentes, infantes y padres; cambiar el nombre del centro y los turnos | **Próximamente** |
| Pedir documentos a una familia (la lista de cada niño sale hoy del seed) | **Próximamente** |
| Menú del día, llegada de padres, eventos y citas, imprimir reportes | **Próximamente** |

---

## 4. Padre o tutor

Barra inferior: **Hoy · Calendario · Fotos · Chat · Documento** (la última pestaña se llama «Documento» en la barra).

> Solo ves a **tus** hijos. Si tienes más de uno, hoy la app muestra **solo el primero** y no hay selector de hijo **[Próximamente]**.

### 4.1 Hoy

- **Resumen del día** de tu hijo: ánimo, cuánto comió, cambios de pañal, minutos de siesta y una nota general, y la **línea de tiempo «Su día, paso a paso»**. Si la docente aún no capturó nada, verás «Todavía no hay notas».
- **«Ver el mes»** lleva al calendario.
- **Aviso desde casa:** escribe una nota para la asistente («Cuéntale a la asistente cómo amaneció …») y envíala; la docente la ve en el día de hoy.
- La **campana** abre los **avisos** del centro y de la sala de tu hijo, con contador de nuevos. «Leído» se guarda solo en tu teléfono: al cambiar de teléfono vuelven a salir como nuevos.
- El botón junto a la campana abre **Mi cuenta**.

### 4.2 Calendario

Un mes con la **asistencia** de cada día: día completo, con nota, falta o sin registro. Al tocar un día se abre **Hoy** de ese día. No hay eventos, citas ni fiestas **[Próximamente]**. No encontré un control para cambiar de mes en el código de la pantalla: muestra el mes con los datos que entrega la API.

**Resumen de la semana.** En la pestaña Calendario hay una tarjeta «Resumen de la semana» con el rango («12 al 16 de octubre»), los cinco días de lunes a viernes marcados ✓ (asistió), ✗ (faltó) o · (sin registro), cuántos días asistió y faltó, el **ánimo**, las **comidas**, la **siesta promedio**, los cambios de pañal, cuántas **notas** le enviaste a la asistente y los **logros** que la asistente escribió. Si la docente no capturó ánimo, comida o siesta, salen con «—». Se calcula al abrir la pantalla; no se envía ni se imprime.

### 4.3 Fotos

- Galería de las fotos donde **aparece tu hijo**, por categoría: **Actividades, Comidas, Siesta e Incidentes**, con la maestra, fecha, hora y nota.
- Una foto en la que aparezcan varios niños **la ven las familias de todos ellos**.
- **Descargar:** abre la foto en el navegador, desde donde la puedes guardar. La dirección vale 10 minutos.
- **Pedir que se borre:** envía una solicitud al centro y el botón cambia a «Solicitud enviada». El administrador la revisa en su pantalla «Fotos que piden borrar» y decide borrarla o conservarla. No hay un plazo de respuesta definido: si necesitas que se atienda pronto, habla con la dirección. La app no te avisa cuando se resuelve.
- La galería carga una primera página; seguir cargando más: **Próximamente**.

### 4.4 Chat

Conversación con las asistentes de la sala de tu hijo. Si tienes varios hijos, primero ves la bandeja. Escribe y envía; si falla, toca para reintentar. El chat consulta mensajes nuevos cada 10 segundos. Adjuntar fotos o archivos: **Próximamente**.

### 4.5 Documentos

- **Documentos de [hijo]:** lista de lo que pide el centro, cada uno **Vigente** o **Faltante** (con fecha límite).
- Toca un documento para **subir o reemplazar** el archivo con el selector de archivos del teléfono (PDF o imagen, hasta 10 MB).
- **Autorizados para recoger:** personas que pueden recoger a tu hijo. **«Agregar»** pide el **nombre completo** y la agrega; **«(eliminar)»** la quita.
- Si no hay documentos pedidos, la lista aparece vacía: hoy el sistema **no crea** una lista de documentos al inscribir a un infante (está en el Bloque 1 de [`PLAN-ACCION.md`](../PLAN-ACCION.md)); los que se ven en la base de pruebas salen del seed.

### 4.6 Lo que el padre todavía no puede hacer

| Función | Estado |
|---|---|
| Cambiar entre varios hijos | **Próximamente** |
| Ver el menú del día | **Próximamente** |
| Imprimir o enviar el resumen semanal (hoy solo se ve en la app) | **Próximamente** |
| Eventos y citas en el calendario | **Próximamente** |
| Mandar fotos por el chat | **Próximamente** |
| Notificaciones del teléfono (push) | **Próximamente** |
| Usar la app sin internet | **Próximamente** para el padre y el administrador; para la docente solo existe la asistencia de Mi sala |
| Desbloqueo con huella | **Próximamente** (pendiente de aprobación) |
| Corregir mis datos o pedir una copia de ellos | **No disponible hoy**: pídelo a la dirección |

---

## 5. Resumen: ¿qué sirve hoy y qué no?

| Área | Docente | Administrador | Padre |
|---|---|---|---|
| Entrar, activar cuenta, cambiar contraseña, cerrar sesión | ✅ | ✅ | ✅ |
| Checador (entrada y salida, sin ubicación; cierre automático a las 9 pm) | ✅ | — | — |
| Rutina del día | ✅ | ✅ (plantillas) | ✅ (resumen) |
| Reportes con texto, dictado y foto a varios niños | ✅ | — | ✅ (los ve) |
| Asistencia del día | ✅ (también sin conexión) | — | ✅ (calendario) |
| Chat con la familia o la docente | ✅ (sin tiempo real) | — | ✅ (sin tiempo real) |
| Avisos | ✅ (lee) | ✅ (publica) | ✅ (lee) |
| Fotos y solicitud de borrado | — | ✅ (revisa) | ✅ (pide el borrado) |
| Documentos y autorizados | — | — | ✅ |
| Altas, bajas y salas | — | ✅ | — |
| Varios hijos, menú del día, eventos, huella, notificaciones (push), y todo lo demás sin conexión | ❌ | ❌ | ❌ |
