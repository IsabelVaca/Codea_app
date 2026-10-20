# Manual de usuario de Codea

Estado a **2026-10-20** (puesto al día contra `main` el 20/oct: la pestaña **Avisos** del administrador, antes «Centro»; las salas en **Docentes → Salas**; filtros Activos / Todos; vincular a un padre con otro niño; Mi día sin menú; Mi sala con su nueva disposición; borrado directo de fotos; avisos con imagen; menú del día; y lo que la app guarda para mostrarlo al instante). La app se conecta al servidor del equipo en `https://api.codeaapp.site`. Está escrito a partir de [`docs/plan/01-app-pantallas.md`](../plan/01-app-pantallas.md), de los textos de la app (`app/src/main/res/values/strings.xml`), de los archivos de `app/src/main/java/mx/tec/codea/ui/screens/` y del recorrido en teléfono físico de [`RECORRIDO.md`](../RECORRIDO.md). **No se ejecutó la app para escribirlo**: si una pantalla cambió después, manda lo que se vea en el teléfono.

Lo que todavía no existe se marca como **«Próximamente»** o como **«No disponible hoy»** cuando ni siquiera hay una pantalla que lo anuncie.

Codea es la aplicación de la estancia infantil **La Oruga** para llevar el día de los niños y comunicar al centro, a las docentes y a las familias. Tiene **tres perfiles**: administrador, docente y padre o tutor. Cada persona entra con su correo y su contraseña y ve únicamente las pantallas de su perfil.

---

## 1. Antes de empezar

| Qué necesitas | Detalle |
|---|---|
| Un teléfono Android | Versión 7.0 (nivel de API 24) o más nueva |
| Conexión a internet | **Casi todo necesita conexión.** Sin red, la app muestra **lo último que cargó** en cada pantalla (los tres perfiles), pero no guarda cambios. La única excepción es la **asistencia de Mi sala** (docente), que sí se guarda sin conexión y se envía sola al volver la red (ver 2.4). Reportes, fotos, chat, checador, pase de salida y avisos necesitan red |
| Una cuenta | La crea el administrador del centro; no te puedes registrar solo |
| Una invitación | La recibes del administrador por **WhatsApp**: un enlace y, por si el enlace no abre la app, tu correo y un código con el formato `K7QX-M4PZ` |

### 1.1 Primera vez: activar tu cuenta

**Con el enlace de WhatsApp (lo normal).** Instala Codea y toca el enlace del mensaje (`https://api.codeaapp.site/invitar…`): la app se abre en «Activar mi cuenta» con tu **correo y tu código ya escritos**. Solo escribe y repite tu contraseña y toca **«Activar y entrar»**. Si la app ya tiene una sesión abierta, ignora el enlace. Para que el enlace abra la app directamente, el servidor tiene que conocer la huella del certificado con que se firmó la app; si se abre el navegador en lugar de la app, usa el procedimiento de abajo (que el enlace abra la app no se ha probado en un teléfono: `docs/RECORRIDO.md`).

**A mano (si el enlace no abre la app):**

1. Abre Codea. Aparece «Inicia sesión».
2. Toca **«Es mi primera vez: activar mi cuenta»**.
3. Escribe tu **correo** (el que registró la dirección), el **código** que te dieron y la **contraseña** que quieras usar. Repítela.
4. Toca **«Activar y entrar»**. Quedas dentro con tu perfil.

Escribe solo las letras y los números del código: **la app pone el guion sola** y lo pasa a mayúsculas; también puedes pegarlo copiado de WhatsApp. **Solo se puede usar una vez y dura 7 días.** Si venció, pide otro a la dirección. Quien no esté dado de alta por el administrador no tiene código y no puede crear una cuenta.

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
| Administrador | **Ajustes → Mi cuenta** |
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

---

## 2. Docente

Barra inferior: **Mi día · Mi sala · Chats · Menú**.

### 2.1 Antes de empezar el día: Mi día y el Checador

1. **Mi día** dice «Aún el día no comienza»: «Ve al checador a registrar tu entrada cuando estés en la estancia». Toca **Checador** (también está en **Menú**).
2. La pantalla «Registrar entrada» muestra la fecha y la hora del teléfono. Si hoy no es uno de los días de trabajo que el administrador definió, dice «Hoy no es día de trabajo» y no deja registrar. **No pide ubicación ni permisos**: el checador no usa GPS ni mapa.
   - **A tiempo:** toca **«Registrar mi entrada»**.
   - **Tarde:** la app te avisa «Llegaste después de tu hora»; se registra igual con **retardo**. Puedes escribir el **motivo del retardo** (opcional) y luego tocar «Registrar con retardo». El retardo queda marcado en tu registro; la app no tiene aviso a coordinación.
3. La hora que queda guardada es la del **servidor**, no la del teléfono.
4. Al registrar te sale «¡Entrada registrada!» y **se queda en pantalla** hasta que toques **«Ir a Mi día»**, que te lleva a **«Rutina de hoy»**, de solo lectura. Toca **«Comenzar el día»**.

**Importante sobre tu privacidad:** el checador guarda solo la **hora de entrada, la hora de salida, el retardo y el motivo** que escribas. **No guarda tu ubicación.** Ver [`aviso-privacidad.md`](aviso-privacidad.md).

**Salida.** Al terminar tu turno vuelve al checador: la pantalla se llama **«Tu jornada de hoy»** y muestra el botón **«Registrar mi salida»**. Al registrarla dice «Salida registrada a las …». Una salida repetida no cambia la hora ya registrada. **Si se te olvida, a la hora de cierre del centro (21:00 por omisión) el servidor cierra tu día solo:** la salida queda en esa hora y la pantalla dice «Salida automática a las 21:00». En **Menú**, la tarjeta del checador dice «Entrada 13:59, falta tu salida» y, después, «Entrada 13:59, salida 14:01».

### 2.2 Mi día: la rutina

Bajo el título **«Rutina»** están los **subprocesos (momentos) de la rutina** de tu sala. Cada uno puede estar terminado, **en curso** o pendiente; el momento elegido lo dice en una pastilla («En curso desde 0:03», «Terminó 0:45» o «Pendiente»). La rutina sale de la plantilla que el administrador asignó a tu sala (o la predeterminada). Desde el 20/oct Mi día **ya no muestra el menú del día** (la familia lo sigue viendo en Hoy).

- Toca un momento para ver cuántos **reportes** tiene.
- **«Realizar reporte»** (solo en el momento en curso): ver 2.3.
- **«Confirmar subproceso»**: cierra el momento en curso y arranca el siguiente.

**Pase de lista.** Arriba de la rutina hay una tarjeta «Pase de lista» («1 de 2 presentes»). Ábrela: aparece toda tu sala, un toque por niño (presente o no), y **«Marcar a todos presentes»** para el caso normal. Cada toque se guarda al instante, también sin conexión (ver 2.4). **No hay un tercer estado: un niño al que no le pasas lista cuenta como ausente**; por eso no puedes hacerle reportes ni registrarle salida ese día.

**Pase de salida (último paso del día).** Mientras faltan momentos, el pase es una fila gris con candado («Se abre cuando termines todos los momentos»). Al confirmar el último momento pasa a una tarjeta violeta **«SIGUIENTE PASO»**, a la que la pantalla baja sola. Ábrela:

1. Cuando recojan a un niño, toca **«Registrar salida»** y elige **quién se lo lleva**: las personas autorizadas que registró la dirección y, siempre, sus tutores. Si el niño no tiene a nadie registrado, la pantalla dice que pidas a la dirección que lo agregue en Ajustes → Personas autorizadas.
2. La hora la pone el servidor. La fila queda como «Salió a las … con …». Si te equivocaste de persona, toca **«Corregir»**.
3. Solo aparecen los niños con pase de lista de hoy. Con todos fuera (o si hoy no vino ninguno) aparece **«Finalizar día»**: pide confirmación, registra tu salida del checador y termina tu turno. Después se muestra **«Día finalizado»**.

El pase de salida **necesita conexión**: no hay cola sin conexión para él. La familia ve la hora y quién recogió a su hijo como último evento de «Hoy».

### 2.3 Hacer un reporte

1. En el momento en curso toca **«Realizar reporte»**.
2. Elige cómo levantarlo: **Escrito**, **Dictado** o **Con foto**. Las tres guardan lo mismo y puedes cambiar después.
3. Elige el **tipo**: Incidencia, Logro, Salud o Conducta.
4. Escribe **«¿Qué pasó?»** (mínimo **10 caracteres**) o pulsa **«Dictar en vez de escribir»**. El dictado usa el reconocedor de voz del teléfono y pide permiso de micrófono; si tu teléfono no tiene servicio de voz, la app te lo dice.
5. Con foto: toma la foto con la cámara (la app pide permiso de cámara). Puedes cambiarla o quitarla.
6. Toca **«Continuar y elegir niños»**.
7. En **«¿A quién le pasó?»** marca uno o varios niños de tu sala. Verás quién lo verá: «Familia de … y coordinación de la estancia. El grupo no lo ve.».
8. Toca **«Guardar reporte de N niños»**.

> **Cuidado al redactar:** la descripción es **una sola** para todos los niños que marques, y cada familia la leerá. No escribas el nombre de otro niño en el texto. Los niños que estén **ausentes** no se pueden elegir (el sistema lo rechaza). Un reporte guardado **no se puede editar ni anular** hoy.

La foto del reporte se ve como una miniatura de baja calidad (la app usa la vista previa de la cámara); fotos de más calidad: **Próximamente**.

### 2.4 Mi sala

Muestra el nombre de **tu sala**, cuántos niños hay hoy y a tus niños en una **cuadrícula de tarjetas** (dos o más columnas, según el ancho de la pantalla), sin botones de asistencia (la asistencia se marca en el pase de lista de Mi día, ver 2.2).

- **«De casa hoy»:** los avisos que las familias mandaron hoy, con su texto. Cada uno tiene **Responder** (abre el chat de ese niño) y **«Ya lo vi»**. «Ya lo vi» **se guarda solo en tu teléfono**: no le avisa a la familia ni a otra docente. Se ven hasta tres; si hay más, **«Ver todos (N)»** abre «Avisos de casa», la bandeja completa del día.
- **Filtros y tarjetas:** «Los niños» se filtra con **Todos**, **Con aviso**, **Con chat** y **Sin resumen**; cada tarjeta trae sus etiquetas (por ejemplo, «Aviso de casa» o «Sin resumen»). Los ausentes quedan aparte al final («Ausentes hoy (N)»).
- **«Capturar resúmenes (N)»:** aparece solo si hay niños presentes sin resumen del día. Abre el resumen del primero («Resumen 1 de N») y, al guardar, pasa solo al siguiente; «Saltar este niño» o «Terminar sin guardar» avanzan sin guardar.
- **Ficha del niño** (tocar su renglón): edad, alergias y demás datos de salud que escribió la familia en «Mi hijo», sus avisos, el resumen del día, **«Corregir asistencia»**, la **familia** y **«Quién lo recoge»** (personas autorizadas y tutores) con el botón **Llamar**, que abre el marcador del teléfono. También está el **Chat** con la familia.

**Mi sala sin conexión (solo la asistencia).** Si no hay red, puedes seguir pasando lista y corrigiendo la asistencia (Presente / Falta):

1. La marca se ve de inmediato y aparece el aviso «Sin conexión: la marca se enviará sola cuando vuelva la red».
2. En lo alto de Mi sala sale un banner: **«1 cambio sin enviar. Se manda solo cuando vuelva la conexión.»** (o «N cambios sin enviar. Se mandan solos…»).
3. Puedes cerrar la app: las marcas pendientes se conservan en el teléfono. Al abrirla sin conexión, Mi sala se muestra con **lo último que se guardó** y el aviso «Sin conexión: se muestra lo último que se guardó en el teléfono».
4. Cuando vuelve la red y la docente que las hizo tiene sesión, la app las envía **sola**, sin que la abras (en el caso probado tardó menos de un minuto); el banner desaparece. Si marcas varias veces al mismo niño el mismo día, solo se manda la última.
5. Una marca que el servidor rechaza de forma definitiva (por ejemplo, un niño que ya no está en tu sala) se avisa y no se reintenta.

**Qué sí necesita red:** reportes, fotos, chats, el resumen del día, el pase de salida, el checador (entrada y salida) y avisos. Desde el 20/oct, tu perfil, la checada y la rutina de hoy, la ficha del niño y el pase de salida **se muestran con lo último que se guardó** mientras llega lo nuevo (y si no hay red), pero para registrar algo hace falta conexión. Si abres Mi sala sin red por primera vez y no hay nada guardado, no se puede cargar.

**Privacidad en el teléfono:** para mostrar Mi sala sin conexión la app guarda en el teléfono los **nombres de los niños de tu sala** y su asistencia, y desde el 20/oct también la última ficha de cada niño que abriste (con sus datos de salud y quién lo recoge); esa copia **no está cifrada** y se borra cuando cierras sesión o la sesión termina. Las marcas pendientes (solo número de niño, estado, fecha y quién las hizo) no se borran al cerrar sesión: se conservan y **solo se envían con la sesión de la docente que las hizo**; si entra otra docente en el mismo teléfono no se mandan ni se cuentan con la suya. Detalle en [`aviso-privacidad.md`](aviso-privacidad.md), 2.5.
- **Resumen del día** (en la tarjeta de cada niño, «Resumen del día ›»): la docente captura lo que verá la familia en «Hoy». La pantalla se llama «Resumen de [niño]» y tiene **Ánimo**, **Comida** (por ejemplo «Casi todo»), **Cambios de pañal** (con − y +), **Siesta** (de 15 en 15 minutos, hasta 6 horas) y **Nota para la familia** («Cómo estuvo su día», opcional); toca **«Guardar resumen»**. Solo se manda lo que llenaste. Si la familia dejó una nota para hoy, arriba aparece «La familia te dejó una nota». En la tarjeta del niño también ves la etiqueta **«Nota de la familia hoy»**. Se probó de punta a punta en el teléfono: la mamá lo vio en Hoy.

### 2.5 Chats

La **bandeja** lista una conversación por niño (familia de …), con no leídos y el último mensaje. Abre una, escribe y toca enviar. Si falla, aparece «No se envió, toca para reintentar»; reintentar **no duplica** el mensaje. Aparece «leído» cuando la otra persona lo vio.

- La app consulta mensajes nuevos **cada 10 segundos**; no es tiempo real (Próximamente).
- Cada chat muestra el nombre del niño y, en una pastilla, sus tutores; si el niño no tiene tutor registrado no sale.
- El botón **«+»** adjunta una **foto**: de la galería (selector de fotos del sistema) o con la cámara. La app la comprime y la sube; si falla, toca para reintentar. Tocar la foto la abre completa. Otros archivos: **Próximamente**.
- Solo ves las conversaciones de los niños de tu sala.

### 2.6 Menú

| Opción | Qué hace hoy |
|---|---|
| Tu tarjeta | Nombre, turno y si ya registraste entrada |
| **Checador** | Abre el checador (entrada, y después salida; si no la registras, se cierra sola a las 9 pm). Su tarjeta muestra la entrada y, si ya la hay, la salida |
| **Mi perfil** | Abre Mi cuenta (ver 1.4) |
| **Avisos de la fundación** | Lista los avisos del centro y de tu sala, en «Nuevos» y «Anteriores». Muestra un contador de nuevos |
| **Cerrar sesión** | Pide confirmación |

---

## 3. Administrador

Barra inferior: **Avisos · Docentes · Familias · Ajustes** (la primera se llamaba «Centro» hasta el 19/oct).

> El administrador **ve a todos los niños y a todo el personal**. No puede leer los chats entre familias y docentes (no tiene pestaña de chat).

Las listas que tienen altas y bajas (docentes, infantes y padres) siguen el mismo orden: el selector de la pestaña, el botón grande de agregar arriba («+ …») y el filtro **Activos** (por omisión) o **Todos**, con el número de cada uno; con «Todos», quien está de baja lleva la pastilla «De baja» (en padres, «Sin acceso»). Lo último que se cargó se muestra al instante mientras llega lo nuevo de la API. En Docentes, además, la lista se vuelve a pedir al abrir la pestaña y se puede jalar hacia abajo para recargar.

### 3.1 Avisos

- **«+ Nuevo aviso»** abre el formulario en una sola tarjeta: **Para** (**«Todas las salas»** o **«Solo»** una sala), el texto, **«Imagen»** (opcional; se elige del teléfono, se ve completa en la vista previa y se puede cambiar o quitar), **«Cancelar»** y **«Publicar»**. Un aviso puede ir solo con imagen. Al publicar, el formulario se cierra; al cancelar, lo escrito se queda.
- Debajo está el **historial** de avisos, el más reciente primero, con su destino, fecha, texto e imagen. Los avisos **no se pueden borrar** **[Próximamente]**.
- **Fotos que piden borrar:** ya no existe (se quitó el 18 y 19/oct). La familia borra directamente las fotos de su hijo; el administrador **no** revisa solicitudes.

### 3.2 Docentes

Arriba hay un selector: **Docentes** o **Salas**.

**Docentes**

1. Lista de docentes con su sala, el número de niños y la hora en que checaron hoy («Checó 8:02» o «Sin checar»).
2. **«+ Dar de alta a una asistente»:** nombre, teléfono de contacto, correo electrónico, y sala. El turno ya no se pide: la entrada y la salida son las del centro (Ajustes → Horario).
3. La app muestra el **«Código de acceso»** en una ventana: **solo se ve ahora**. Toca **«Enviar por WhatsApp»** (abre el chat con el mensaje escrito) o **«Compartir»** (cualquier otra app) y **«Listo»**. La persona lo usa en «Activar mi cuenta». La app **no manda correos**.
4. **Detalle de una docente:** cambiar de sala, **dar de baja** (le corta el acceso al instante) o **reactivar**.

**Salas** (antes en «Centro»)

- **«+ Crear sala»:** «Define su nombre, cupo y quién la atiende.» (se elige una asistente).
- Cada sala con su **ocupación** («8/12 niños»). Toca una para su **detalle**: ver la asistente y los niños, **editar el cupo**, **agregar un infante** o **sacarlo de la sala**, **cambiar la asistente asignada** y **«Eliminar esta sala»** (si tiene niños, el sistema lo rechaza; sus asistentes quedan sin sala).

### 3.3 Familias

Una sola pestaña para los niños y para quienes los recogen. Arriba hay un selector: **Infantes** o **Padres y tutores**.

**Infantes**

1. Lista de infantes inscritos.
2. **«+ Inscribir a un infante»:** nombre(s), apellido(s) (opcional), **fecha de nacimiento real en formato dd/mm/aaaa** (por ejemplo 14/03/2022; la app valida que exista, que no sea futura y que el niño tenga de 0 a 12 años), alergias o notas médicas (opcional) y **sala asignada**. Al guardar queda inscrito y «listo para vincular» a un tutor.
3. **Detalle:** cambiar de sala, **dar de baja** o **reactivar la inscripción**, y ver los datos del tutor con **«Consultar info»**. Para dejarlo sin sala, se saca desde el detalle de la sala (3.2).

Corregir el nombre o la fecha de nacimiento de un infante ya inscrito: **No disponible hoy** (solo cambia la sala). Las alergias y demás datos de salud los corrige la familia en «Mi hijo».

**Padres y tutores**

1. Lista de padres y tutores («familias con acceso a la app»).
2. **«+ Inscribir a un padre o tutor»:** nombre, teléfono de contacto, correo electrónico, parentesco (Mamá, Papá, Tutor o Tutora) y uno o más hijos inscritos (la lista no ofrece niños dados de baja). Al guardar se muestra el diálogo **«Código de acceso»** con el código (solo se ve esa vez) y el botón **«Enviar por WhatsApp»**, que abre el chat con el teléfono de la persona y el mensaje ya escrito (el enlace que abre la app y, por si no abre, su correo y su código). Sin teléfono válido o sin WhatsApp, cae al selector de compartir; **«Compartir»** usa cualquier otra app.
3. **Detalle:** teléfono, correo e hijos inscritos; **«Vincular a otro niño»** (desde el 20/oct: elige uno o más niños inscritos que todavía no son suyos y toca «Vincular»; no cambia su código de acceso); **«Suspender su acceso a la app»** o **«Reactivar su acceso a la app»**; y **«Reenviar invitación»**, que genera un **código nuevo** y abre el mismo diálogo (solo para quien todavía no activó su cuenta).

### 3.4 Ajustes

El menú de lo que el administrador configura. Cada fila abre su pantalla:

Las opciones de la estancia van en una tarjeta (Rutinas, Horario, Menú del día y Personas autorizadas) y **Mi cuenta** va aparte, abajo.

- **Rutinas:** lista de **plantillas de rutina** con su número de momentos. **«+ Nueva rutina»** pide nombre y momentos; cada momento tiene una categoría (llegada, comida, actividad, siesta, higiene, entrega) y hay un **catálogo de momentos** (también se puede agregar uno propio). Se pueden **ordenar** los momentos (la pantalla de arrastre usa una librería pendiente de aprobar; ver `DECISIONES-PENDIENTES.md`, decisión #8) y **asignar una plantilla a una sala**. Cambiar una plantilla **no modifica un día que ya empezó**: la rutina del día se copia cuando la docente checa.
- **Horario:** el horario de todo el centro. Se elige la **hora de entrada** (después de ella se marca retardo), la **hora de salida**, la **hora de cierre automático** (a esa hora se cierran solas las entradas que sigan abiertas) y los **días de trabajo** (L M X J V S D). Se toca cada hora para cambiarla; la app avisa si la entrada no es antes de la salida, si el cierre es antes de la salida o si no hay ningún día. Un día que no marques no deja registrar entrada. Por omisión: 8:00 a 16:00, cierre a las 21:00, lunes a viernes.
- **Menú del día:** el menú de la semana, de lunes a viernes (hay botones para ver la semana anterior y la siguiente). Cada día muestra su **desayuno, comida y colación**; **«Agregar»** o **«Editar»** abre un cuadro con las tres comidas (basta con escribir una; hasta 500 caracteres cada una) y **«Quitar menú»** lo borra. Lo ven las familias en Hoy (desde el 20/oct ya no sale en Mi día de la docente).
- **Personas autorizadas:** la lista de niños y, por niño, quién puede recogerlo (nombre, parentesco y teléfono), con agregar y quitar. **Solo la dirección las administra**; la familia ya no las edita. Se registran por niño: con dos hijos, se capturan en cada uno. La docente las ve en la ficha del niño y las usa en el pase de salida.
- **Mi cuenta:** perfil, cambiar contraseña y cerrar sesión (ver 1.4).

El checador **no usa ubicación ni radio**.

### 3.5 Lo que el administrador todavía no puede hacer desde la app

| Función | Estado |
|---|---|
| Borrar una foto, quitar solo a un niño de una foto de grupo o borrar **documentos** | **No disponible** para el administrador: las fotos las borra la familia desde su app (Fotos → «Borrar», al momento y sin aprobación; si salen otros niños, se borra también para sus familias) |
| Generar un código para recuperar la contraseña de alguien que ya activó su cuenta | **No disponible hoy desde la pantalla** (solo `POST /users/{id}/access-code` en la API) |
| Ver la **bitácora de auditoría** | No hay pantalla; existe `GET /audit-log` en la API |
| Editar datos de docentes, infantes y padres; cambiar el nombre del centro | **Próximamente** |
| Pedir documentos a una familia (la lista de cada niño sale hoy del seed) | **Próximamente** |
| Llegada de padres, eventos y citas, imprimir reportes | **Próximamente** |

---

## 4. Padre o tutor

Barra inferior: **Hoy · Calendario · Fotos · Chat · Mi hijo**.

> Solo ves a **tus** hijos. Si tienes más de uno, arriba de Hoy, Calendario, Fotos y Mi hijo aparece una tira con sus nombres: toca uno para ver a ese hijo. La app recuerda cuál elegiste mientras la usas. El Chat tiene una conversación por niño.

### 4.1 Hoy

- **«El día de [niño]»:** ánimo, comidas, pañal o baño y siesta, y una nota general; lo que la docente no llenó no aparece, y sin nota verás «Todavía no hay notas de este día.». Debajo, la **línea de tiempo del día**: empieza con la **Entrada** (la hora de su pase de lista), sigue con los momentos y sus reportes y termina con la **Salida** («Lo recogió …») cuando la maestra la registra en el pase de salida.
- **Menú del día:** una tarjeta con el desayuno, la comida y la colación de ese día. Si la dirección todavía no lo captura, no aparece.
- **«Ver el mes»** lleva al calendario.
- **Aviso desde casa:** escribe una nota para la asistente («Cuéntale a la asistente cómo amaneció …») y envíala; la docente la ve en el día de hoy.
- La **campana** abre los **avisos** del centro y de la sala de tu hijo, con contador de nuevos. «Leído» se guarda solo en tu teléfono: al cambiar de teléfono vuelven a salir como nuevos.
- El botón junto a la campana abre **Mi cuenta**.

### 4.2 Calendario

Un mes con la **asistencia** de cada día: día completo, con nota, falta o sin registro. Al tocar un día se abre **Hoy** de ese día. No hay eventos, citas ni fiestas **[Próximamente]**. No encontré un control para cambiar de mes en el código de la pantalla: muestra el mes con los datos que entrega la API.

**Resumen de la semana.** En la pestaña Calendario hay una tarjeta «Resumen de la semana» con el rango («12 al 16 de octubre»), los cinco días de lunes a viernes marcados ✓ (asistió), ✗ (faltó) o · (sin registro), cuántos días asistió y faltó, el **ánimo**, las **comidas**, la **siesta promedio**, los cambios de pañal, cuántas **notas** le enviaste a la asistente y los **logros** que la asistente escribió. Si la docente no capturó ánimo, comida o siesta, salen con «—». Se calcula al abrir la pantalla; no se envía ni se imprime.

### 4.3 Fotos

- Galería de las fotos donde **aparece tu hijo**, con un filtro por categoría: **Todas, Actividades, Comidas, Siesta e Incidentes**, con la maestra, fecha, hora y nota.
- Una foto en la que aparezcan varios niños **la ven las familias de todos ellos**.
- **Descargar:** abre la foto en el navegador, desde donde la puedes guardar. La dirección vale 10 minutos.
- **Borrar:** abre la foto y toca **«Borrar»**. La app pide confirmación («Se borra para siempre y ya no la verá nadie, ni tú ni la maestra. No se puede deshacer.») y la foto desaparece en el momento, también para la maestra. **Si en la foto salen niños de otras familias, también se borra para ellas** (no se les avisa). Las copias de respaldo del servidor la conservan hasta 14 días.
- La galería carga una primera página; seguir cargando más: **Próximamente**.

### 4.4 Chat

Conversación con las asistentes de la sala de tu hijo. Si tienes varios hijos, primero ves la bandeja. Escribe y envía; si falla, toca para reintentar. El chat consulta mensajes nuevos cada 10 segundos. Con el botón **«+»** puedes mandar una **foto** (de la galería o con la cámara); tocarla la abre completa. Otros archivos: **Próximamente**.

### 4.5 Mi hijo

- **Sus datos:** nombre, fecha de nacimiento, edad, sala y tutores. **No los editas tú**: si hay un error, pide a la dirección que lo corrija.
- **Salud y cuidados:** escribe **alergias, condiciones médicas, intolerancias, medicamentos y otras indicaciones** (hasta 1,000 caracteres cada una) y guarda. La maestra de su sala lo ve en la ficha de tu hijo, y puedes cambiarlo cuando quieras. Son datos de salud de un menor: escribe solo lo necesario para cuidarlo.
- «Mi hijo» **no muestra** las personas autorizadas para recogerlo ni los documentos (se quitaron a petición del equipo el 18/oct). Si quieres agregar o quitar a alguien que lo recoge, pídelo a la dirección.

### 4.6 Lo que el padre todavía no puede hacer

| Función | Estado |
|---|---|
| Imprimir o enviar el resumen semanal (hoy solo se ve en la app) | **Próximamente** |
| Eventos y citas en el calendario | **Próximamente** |
| Agregar o quitar personas autorizadas | **Lo hace la dirección** |
| Notificaciones del teléfono (push) | **Próximamente** |
| Usar la app sin internet | Sin red se ve **lo último que se cargó** (desde el 20/oct), pero no se puede mandar nada (nota de casa, salud, chat ni borrar fotos). Para la docente, solo la asistencia de Mi sala se guarda sin red |
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
| Pase de lista (sin registro = ausente) | ✅ (también sin conexión) | — | ✅ (calendario) |
| Pase de salida y «Finalizar día» | ✅ (con red) | — | ✅ (ve la hora y quién lo recogió en Hoy) |
| Chat con la familia o la docente, con fotos | ✅ (sin tiempo real) | — | ✅ (sin tiempo real) |
| Avisos | ✅ (lee) | ✅ (publica) | ✅ (lee) |
| Fotos y borrado | — | — | ✅ (borra las fotos de su hijo) |
| Mi hijo (datos y salud) | ✅ (lo ve en la ficha) | — | ✅ (escribe la salud) |
| Personas autorizadas para recoger | ✅ (las ve) | ✅ (las administra) | — |
| Invitación por WhatsApp | — | ✅ | ✅ (activa con el enlace) |
| Altas, bajas y salas | — | ✅ | — |
| Menú del día | — | ✅ (lo captura) | ✅ (lo ve en Hoy) |
| Varios hijos | — | ✅ (vincula a un padre con más niños) | ✅ (tira con sus nombres; sin probar con una familia de dos hijos) |
| Ver lo último que se cargó, sin conexión | ✅ | ✅ | ✅ |
| Eventos y citas, huella, notificaciones (push), imprimir reportes y escribir sin conexión (salvo la asistencia) | ❌ | ❌ | ❌ |
