# Aviso de privacidad de Codea

**Estado: borrador para revisión del equipo.** Describe lo que el código de `main` (commit `043c2d6`, 2026-10-20) y el servidor compartido `codeaapp.site` hacen con los datos. Lo que el código no hace se dice como no hecho. Lo que solo el equipo o el centro pueden llenar está marcado con **«[completar por el equipo]»**. Antes de publicarlo a familias reales debe revisarlo quien tenga responsabilidad legal sobre el centro: aquí no se da asesoría jurídica.

Versión del aviso: 0.2 · Fecha: 20 de octubre de 2026.

> **Nota de estado (20-oct):** la familia borra directamente las fotos de su hijo desde la app, sin aprobación de nadie (`DELETE /media/{id}`); si en la foto salen niños de otras familias, se borra también para ellas. Se probó con pruebas automáticas y en un teléfono (19/oct). Ya no hay solicitud ni plazo de respuesta. Desde el 20/oct hay un **respaldo diario** de la base y de las fotos que se guarda 14 días (ver 5 y 6), y la app guarda en el teléfono la última información que mostró a cada perfil (ver 2.5).

Documentos relacionados: [`clasificacion-datos.md`](clasificacion-datos.md) (campo por campo) y [`etica-legal.md`](etica-legal.md) (requisitos verificables y dilemas).

---

## 1. Quién es el responsable

Codea es la aplicación con la que la estancia infantil **La Oruga** (nombre que usa el sistema para el centro) registra la asistencia, la rutina y la comunicación con las familias.

| Dato | Valor |
|---|---|
| Responsable del tratamiento (nombre o razón social) | **[completar por el equipo]** |
| Domicilio | **[completar por el equipo]** |
| Correo o canal para asuntos de privacidad | **[completar por el equipo]** |
| Persona o área encargada de atender solicitudes | **[completar por el equipo]**. Hoy el único rol con poder de acción dentro del sistema es el administrador (la «dirección» en la app). |

## 2. Qué datos se recaban

### 2.1 De las personas adultas con cuenta (administración, docentes y tutores)

| Dato | De quién | Cómo se obtiene |
|---|---|---|
| Nombre, apellido, correo y teléfono | Todas las cuentas | Los captura el administrador al dar de alta a la persona |
| Contraseña | Todas las cuentas | La elige la propia persona al activar su cuenta. **Se guarda solo como hash Argon2id**: nadie, ni el administrador, puede leerla |
| Fechas de último inicio de sesión y de último cambio de contraseña | Todas las cuentas | Las registra el servidor |
| Dispositivo y aplicación desde donde se inicia sesión (`user_agent`) | Todas las cuentas | Lo manda la app en cada inicio de sesión |
| Dirección IP y correo escrito en cada intento de entrar, correcto o fallido | Quien intenta entrar (aunque no tenga cuenta) | Los registra el servidor para frenar ataques y para la bitácora |
| Sala, turno y relación con el niño | Docentes y tutores | Los captura el administrador |
| Hora de entrada y de salida (la de salida puede ser la del cierre automático de las 9 pm), retardo y motivo del retardo. **No se guarda la ubicación** | Docentes | Al usar el checador (ver 2.4) |
| Avisos a coordinación (motivo y mensaje opcional) | Docentes | **La app ya no los genera** (se quitó el botón el 18/oct); la tabla y el endpoint siguen en el servidor |
| Mensajes, notas para la asistente | Docentes y tutores | Los escriben ellos en la app |
| Persona(s) autorizada(s) para recoger al niño: nombre, parentesco y teléfono | Terceros | **Solo el administrador** las captura y las quita (desde el 18/oct la familia ya no puede). Se registran por niño |

### 2.2 De los menores

| Dato | Cómo se obtiene |
|---|---|
| Nombre, apellido y fecha de nacimiento | Los captura el administrador al inscribir (la app pide la fecha real, dd/mm/aaaa). Los niños inscritos con una versión anterior de la app conservan una fecha aproximada calculada desde su edad |
| **Alergias** (dato de salud) | Las captura el administrador al inscribir y las puede editar el tutor desde «Mi hijo» |
| **Condiciones médicas, intolerancias, medicamentos y otras indicaciones** (datos de salud, hasta 1,000 caracteres cada uno) | El tutor (o el administrador) desde «Mi hijo». La docente de la sala los ve en la ficha del niño. La bitácora de auditoría guarda **qué campos** cambiaron, no su contenido |
| Sala, y si está activo o dado de baja | El administrador |
| Asistencia diaria (presente o falta) y **hora de entrada** del niño (la de su pase de lista). **Sin pase de lista, el niño cuenta como ausente** | La docente o el administrador; la hora la pone el servidor (o el teléfono, si la marca se hizo sin conexión) |
| **Salida del niño:** hora, y quién lo recogió (nombre de la persona autorizada o del tutor, guardado como texto) | La docente, en el pase de salida; la hora la pone el servidor |
| Resumen del día: ánimo, cuánto comió, cambios de pañal, minutos de siesta y nota general | La docente |
| **Reportes** de incidente, logro, salud o conducta, con su descripción | La docente |
| **Fotografías** de las actividades en las que aparece | La docente, con la cámara del teléfono |
| **Documentos** que pide el centro (por ejemplo, certificado de alergias o informe de discapacidad, que son los que trae el ejemplo del sistema) | Antes el tutor los subía desde la app; **desde el 18/oct la app ya no tiene esa pantalla**. La API de documentos sigue en el servidor y conserva los que ya existían |
| Mensajes de la familia sobre el niño, y **fotos que se mandan por el chat** | El tutor y la docente |

Los datos de **salud** (alergias, condiciones médicas, medicamentos, reportes de salud, certificados) se tratan como **sensibles**. Los datos de **menores** se tratan con el nivel de protección más alto que define este proyecto.

### 2.3 Lo que la app no recaba

Se comprobó en el manifiesto (`app/src/main/AndroidManifest.xml`) y en las dependencias (`app/build.gradle.kts`):

- **No se usa la ubicación.** Desde el 18 de octubre el manifiesto no declara ningún permiso de ubicación; solo pide cámara, micrófono e internet.
- **No hay herramientas de analítica, publicidad ni Firebase** en las dependencias.
- **No se guarda audio.** El dictado usa el reconocedor de voz del teléfono y la app solo recibe el texto.
- **No se pide acceso a contactos, SMS ni almacenamiento general.** Para subir un documento se usa el selector de archivos del sistema.

### 2.4 El checador y la ubicación

Desde el 18 de octubre de 2026 el checador **no usa la ubicación del teléfono**:

1. La docente abre el checador y toca «Registrar mi entrada». La app **no pide el permiso de ubicación** (ya no está declarado en el manifiesto) ni usa mapas.
2. El **servidor** guarda la hora (la del servidor, no la del teléfono), si hubo retardo y el motivo que escribió la docente. Hay una fila por docente y por día.
3. La docente registra su salida con otro toque. Si no la registra, a las **21:00** (hora del centro) el servidor **cierra el día solo**: guarda esa hora como salida y marca el registro como automático.
4. Las coordenadas y distancias que se guardaron antes de este cambio se **borran** con la migración `e74f0a5b3d43`. La base del servidor compartido (`codea_prod`) se creó con todas las migraciones, así que no tiene esas columnas.
5. Como el checador ya no verifica dónde está la docente, el registro prueba la hora de la entrada, no el lugar.


### 2.5 Copia local en el teléfono (modo sin conexión y caché)

Para que la docente pueda marcar la asistencia aunque no haya red, y para que las pantallas muestren al instante lo último que se vio, la app guarda **en el teléfono** una base local (`codea.db`, versión 5):

| Qué se guarda | Dónde y cuánto tiempo |
|---|---|
| **Nombres de los niños de la sala**, su asistencia y hora de entrada de hoy, los avisos de casa del día y si tienen chat sin leer, nota de la familia o resumen; nombre de la sala y de la docente | Es una copia de lo último que se vio en Mi sala (perfil de docente). Se reemplaza en cada carga y **se borra cuando se cierra la sesión o la sesión termina** |
| **Última respuesta de la API de cada pantalla** (desde el 20/oct, los tres perfiles). Docente: su perfil, su checada y la rutina de hoy, la ficha del niño con sus **datos de salud** y quién lo recoge (nombre, parentesco y teléfono), y el pase de salida. Padre o tutor: su perfil, el día de su hijo (resumen, reportes, menú, entrada y salida), el calendario, el resumen semanal, «Mi hijo» con los **datos de salud** y la lista de fotos. Administrador: las listas de docentes, infantes, padres (con correo y teléfono), salas, rutinas y avisos | Cada persona tiene lo suyo. Se reemplaza cada vez que llega la respuesta nueva y **se borra cuando se cierra la sesión o la sesión termina** |
| **Marcas de asistencia pendientes**: número del niño (sin nombre), presente o falta, fecha y quién las hizo (identificador de la docente) | Hasta que se envían al servidor. **No se borran al cerrar sesión**, para no perder marcas sin enviar, pero solo se envían con la sesión de la docente que las hizo |

Además, las **fotos y las imágenes de avisos** que ya se vieron quedan en la caché de imágenes de la app (Coil), dentro de su almacenamiento privado, para no descargarlas otra vez. El código **no borra esa caché al cerrar sesión**.

Esa base **no está cifrada** (los tokens de sesión sí, con una llave del Android Keystore). Está en el almacenamiento privado de la app y no se incluye en copias de seguridad (`allowBackup="false"`). El borrado de los nombres de Mi sala al terminar la sesión se comprobó a mano leyendo la base del teléfono (queda sin filas de niños al cerrar sesión); el de las respuestas guardadas está en el código pero no se ha comprobado en un teléfono. Ninguno tiene prueba automática. Mientras haya una sesión abierta, quien tenga el teléfono desbloqueado podría ver lo que la app guardó de esa persona, incluidos datos de salud de menores.

## 3. Para qué se usan los datos

### 3.1 Finalidades necesarias

| Finalidad | Datos que intervienen |
|---|---|
| Crear y proteger las cuentas, iniciar sesión, activar la cuenta con un código, cerrar sesiones | Datos de cuenta, IP, dispositivo |
| Controlar quién puede ver qué: cada familia ve solo a sus hijos y cada docente solo a los niños de su sala | Relaciones entre tutor, niño, sala y docente |
| Llevar la asistencia, la rutina del día, los reportes y el resumen que ve la familia | Datos del menor |
| Compartir fotografías de las actividades con las familias de los niños que aparecen | Fotografías, etiquetas de quién aparece |
| Pedir y conservar los documentos que exige el centro | Documentos |
| Saber quién puede recoger al niño | Personas autorizadas |
| Comunicar a la familia con la docente (chat, notas) y al centro con las familias (avisos) | Mensajes, avisos |
| Marcar la asistencia de los niños aunque no haya conexión y enviarla sola al volver la red, y mostrar al instante lo último que se vio | Copia local descrita en 2.5 |
| Verificar la asistencia y puntualidad del personal | Hora de entrada y de salida, retardo y motivo del checador (sin ubicación) |
| Recuperar la información si falla el servidor o se borra algo por error | Copia diaria de toda la base y de las fotos (ver 5 y 6) |
| Resumir a la familia la semana del niño (asistencia, ánimo, comidas, siesta, logros y notas) | Se calcula al consultar a partir del resumen del día, los reportes y las notas; no guarda datos nuevos |
| Mantener una bitácora de quién hizo qué, para investigar incidentes y errores | Cuenta, IP, acciones del administrador |
| Frenar el adivinado de contraseñas y códigos | Correo e IP de los intentos fallidos |

### 3.2 Finalidades que **no** existen

El sistema no usa los datos para publicidad ni perfiles comerciales, y no los vende. No hay en el código ninguna función que lo haga. **[El equipo debe confirmar que esto es lo que quiere declarar.]**

## 4. Con quién se comparten y dónde se guardan

### 4.1 Dentro del centro (quién ve qué)

| Quién | Qué puede ver |
|---|---|
| **Tutor (padre o madre)** | Solo a **sus** hijos: resumen del día (con la hora de entrada y la de salida, y quién lo recogió), calendario de asistencia, resumen semanal, fotos donde aparecen, sus datos y su salud, mensajes con las docentes de la sala, avisos de sus salas o del centro y el menú del día. La API también le deja leer los documentos y las personas autorizadas de sus hijos, aunque la app ya no los muestra. **Una foto etiquetada con varios niños la ven las familias de todos ellos** |
| **Docente** | Solo a los niños de **su** sala: lista, asistencia, rutina, reportes, los datos de salud que la familia escribió, las salidas del día, mensajes con sus familias y la lista de personas autorizadas. Ve los avisos del centro y de su sala |
| **Administrador** | Todos los niños, las salas, las docentes, los tutores, los documentos, las fotos, los avisos, la bitácora de auditoría y el centro. **No puede leer los mensajes** entre familias y docentes a través de la API: es una regla del sistema, no un cifrado |
| **Persona con acceso técnico a la base de datos, al almacén, a la computadora que sirve la API o a sus respaldos** | Todo lo que no esté cifrado: es decir, casi todo (ver 6) |

### 4.2 Proveedores y terceros

| Tercero | Para qué | Qué datos le llegan | Estado |
|---|---|---|---|
| **Aiven** (Postgres alojado) | Guardar la base de datos | Todas las tablas | **En uso** desde el 19/oct para el servidor compartido `codeaapp.site` (base `codea_prod`, Postgres 18). Hoy tiene datos de prueba; no debe recibir datos reales de familias mientras el servidor sea de pruebas |
| **Cloudflare** (túnel y certificado HTTPS) | Publicar la API (`api.codeaapp.site`) y el almacén de fotos (`archivos.codeaapp.site`) sin abrir puertos | Todo el tráfico entre la app y el servidor, incluidas las fotos: Cloudflare termina el HTTPS en sus servidores y lo reenvía a la computadora por el túnel (que también va cifrado) | En uso desde el 19/oct (decisión #7, aprobada) |
| **Almacén de archivos compatible con S3** (SeaweedFS) | Guardar fotos y documentos | Los archivos | Corre en un contenedor de Docker **en la computadora de un integrante del equipo** que sirve `codeaapp.site`; ahí mismo quedan los respaldos diarios. SeaweedFS sigue pendiente de aprobación (decisión #9). Dónde vivirá en un piloto con familias reales: **[completar por el equipo]** |
| **Google (reconocedor de voz del teléfono)** | Convertir la voz del dictado en texto | La voz que dicta la docente, procesada por el servicio de voz del sistema Android, según su configuración; la decisión #10 de `docs/DECISIONES-PENDIENTES.md` dice que el reconocedor es el de Google y sigue sin confirmarse | Solo si la docente elige «Dictado» |

No se transfieren datos a otros terceros desde el código. No hay envío de correos desde el sistema: los códigos de acceso los entrega el administrador en persona o por el medio que elija.

## 5. Cuánto tiempo se conservan

**Hoy el sistema no borra nada por su cuenta**, con estas excepciones:

| Dato | Qué pasa |
|---|---|
| Intentos fallidos de entrar (correo e IP) | Se borran los de más de 1 día; los de un correo se borran cuando ese correo entra bien |
| Persona autorizada para recoger | Se borra de verdad cuando el tutor o el administrador la quita |
| Copia local en el teléfono (Mi sala y respuestas guardadas de los tres perfiles) | Se borra al terminar la sesión (lo de Mi sala, comprobado a mano en un teléfono; ninguna con prueba automática). Las marcas pendientes se borran al enviarse. La caché de imágenes **no** se borra al cerrar sesión |
| Respaldos diarios de la base y de las fotos | Se guardan **14 días** en la computadora que sirve la API (`~/respaldos-codea/`) y después se borran solos. Mientras duran, conservan también lo que ya se borró o se dio de baja en el sistema, incluidas las fotos que una familia borró |
| Contraseña | Se reemplaza al cambiarla; el hash anterior no se conserva |
| Tokens de sesión | Duran 5 minutos (acceso) y 7 días (renovación); en el teléfono se borran al cerrar sesión. En el servidor el registro vencido **no se borra** |

Todo lo demás (cuentas, expedientes de menores, reportes, fotos, documentos, mensajes, checadas, bitácora) **permanece mientras exista la base de datos**. Dar de baja a una persona o a un niño **solo desactiva** el acceso o la inscripción; **no borra los datos**.

Propuesta de plazos para que el equipo la apruebe o corrija (no está implementada ni aprobada):

| Dato | Plazo propuesto |
|---|---|
| Expediente, reportes, fotos, documentos y mensajes de un niño | Mientras esté inscrito y un periodo adicional de **[completar por el equipo]**; después, borrado definitivo o anonimización |
| Checadas del personal | Mientras dure la relación laboral y el plazo que la ley laboral exija **[verificar con asesoría legal]** |
| Bitácora de auditoría | Entre 1 y 2 años **[completar por el equipo]** |
| Tokens y códigos vencidos | 30 días después de vencer |
| Fotos que la familia borra | De inmediato, en el bucket (ya se hace); en los respaldos, cuando venzan (hoy 14 días) |
| Respaldos de la base y de las fotos | 14 días (lo que ya hace el respaldo diario) |

## 6. Cómo se protegen

Medidas que existen en el código:

- Contraseñas con Argon2id; tokens y códigos de acceso solo como hash SHA-256.
- Sesión con un token de acceso de 5 minutos y renovación rotada; si alguien reutiliza un token ya usado, se cierran todas sus sesiones.
- Bloqueo temporal tras 5 fallos seguidos por correo o 30 por IP en 15 minutos.
- Permisos por rol y por registro: un recurso fuera del alcance responde «no existe» (404).
- Dar de baja a una persona le corta el acceso en la siguiente petición.
- Fotos y documentos no pasan por la API: se suben y se leen con direcciones firmadas de 5 y 10 minutos.
- En el teléfono, los tokens se guardan cifrados con una llave del Android Keystore. La copia local de Mi sala y las respuestas guardadas se borran al terminar la sesión.
- **HTTPS** entre la app y el servidor desde el 19/oct (`api.codeaapp.site`, con certificado emitido por Cloudflare); la app de release solo acepta HTTPS. Cabeceras de seguridad y `Cache-Control: no-store` en la API; HSTS cuando el servidor no está en modo desarrollo (comprobado en el servidor el 19/oct).
- **Respaldo diario** de la base y de las fotos desde el 20/oct, a las 3:00, en una carpeta a la que solo entra el usuario de la computadora servidor; se guardan 14 días y se probó restaurar la base.
- Bitácora de auditoría de inicios de sesión y de altas, bajas, cambios del administrador, cambios del centro y borrados de fotos hechos por la familia.

Medidas que **no** existen todavía (se dicen para no prometer de más):

- **El servidor corre en la computadora de un integrante del equipo**, no en un centro de datos: si se apaga o se duerme, la app deja de funcionar, y las fotos y los respaldos están en ese mismo disco. No hay monitoreo.
- **No se verificó el cifrado en reposo** de la base (Aiven), del almacén de fotos ni de los respaldos (disco de esa computadora). Los respaldos no están cifrados.
- **La base local del teléfono (`codea.db`) no está cifrada** y, mientras haya sesión, guarda nombres de niños y, desde el 20/oct, también datos de salud y teléfonos (2.5).
- **Los mensajes y los archivos no están cifrados de extremo a extremo.**
- **La bitácora no registra quién consulta** una foto, un documento, un reporte o un mensaje.
- **No hay segundo factor** de autenticación para el administrador.

## 7. Tus derechos y cómo ejercerlos

Como titular (o como madre, padre o tutor de un menor) tienes derecho a **conocer** qué datos tenemos (acceso), a **corregirlos** (rectificación), a **pedir que se eliminen** (cancelación) y a **oponerte** a su uso, así como a **revocar** el consentimiento que diste. La base legal en México es la Ley Federal de Protección de Datos Personales en Posesión de los Particulares y su normativa vigente **[verificar con asesoría legal qué versión aplica y qué autoridad corresponde]**.

### 7.1 Cómo solicitarlo

1. Escribe a **[canal de privacidad: completar por el equipo]** o habla con la dirección del centro.
2. Indica quién eres, a qué niño se refiere (si es el caso) y qué derecho quieres ejercer.
3. La respuesta será en **[plazo: completar por el equipo]**.

### 7.2 Qué se puede hacer hoy en el sistema y qué no

| Derecho | Lo que existe hoy | Lo que **falta** |
|---|---|---|
| **Acceso** | El tutor ve en la app el resumen del día, el calendario, el resumen semanal, las fotos y los datos y la salud de sus hijos («Mi hijo»). Los documentos y las personas autorizadas ya no se muestran en la app del tutor. El administrador ve los expedientes | No hay una exportación de «todos mis datos». Hay que pedirla al administrador, que no tiene una función para generarla |
| **Rectificación** | El administrador puede cambiar de sala a un niño y administra las personas autorizadas; el tutor puede corregir las alergias y demás datos de salud de su hijo desde «Mi hijo» | **No hay pantalla ni endpoint para corregir** nombre, fecha de nacimiento, correo o teléfono de una cuenta o de un niño (está como «editar datos» en `docs/plan/01-app-pantallas.md`, prioridad P2). Hoy solo se corrige con acceso directo a la base |
| **Cancelación** | El administrador puede **dar de baja** una cuenta o un niño (se desactiva el acceso). Desde el 19/oct el tutor **borra directamente** las fotos de su hijo desde la pantalla Fotos: se eliminan del almacén y de todas las galerías al momento y queda en la bitácora, **también si en la foto salen niños de otras familias** (para ellas también desaparece, sin aviso) | **La baja no borra datos.** No hay forma de borrar un expediente completo desde la app. No se pueden borrar documentos por esa vía (solo fotos). En una foto de grupo no se puede quitar solo la etiqueta de un niño: se borra para todos. Las fotos borradas siguen en los respaldos diarios hasta 14 días. No hay plazo de atención definido para lo que se pide a la dirección |
| **Oposición** | Se puede pedir la baja de la cuenta | Igual que cancelación |
| **Revocar el consentimiento** | Igual que cancelación | Igual |

El equipo debe decidir cómo se atenderán estos casos mientras no estén construidos. La postura que propone [`etica-legal.md`](etica-legal.md) (dilema 1) es: **procedimiento manual documentado** con registro del hecho en la bitácora, mientras no exista la pantalla.

## 8. Menores de edad

Los datos de los menores los entregan sus madres, padres o tutores, o el centro, con el consentimiento de ellos. Ningún menor tiene cuenta propia en la aplicación. **[El equipo debe definir y conservar el formato de consentimiento expreso para el tratamiento de datos de salud y de imagen. La app no recaba hoy ese consentimiento: no existe ninguna pantalla de aceptación del aviso.]**

## 9. Cambios a este aviso

Cualquier cambio se publicará en **[medio: completar por el equipo]** y se avisará a las familias por la sección de avisos de la app. La versión y la fecha van al inicio de este documento.

---

## Anexo: qué tiene que cambiar para que el aviso sea verdadero sin reservas

| Pendiente | Dónde |
|---|---|
| ~~Corregir el texto de permiso de ubicación («no se guarda»)~~ **Hecho** en `f814763`. ~~Decidir si se dejan de guardar las coordenadas~~ **Resuelto** el 18/oct: el checador ya no usa ubicación | `staff_check_in` |
| Mostrar y registrar la aceptación del aviso al activar la cuenta | App (no existe) |
| ~~Probar el borrado directo en un teléfono~~ **Hecho** el 19/oct. Falta decidir si se avisa a las otras familias de una foto de grupo borrada | `media` (RF-10) |
| Plazos de retención aprobados e implementados (incluido el de los respaldos, hoy 14 días) | Tareas de limpieza en el servidor |
| Responsable, domicilio y canal de contacto | Este documento |
| ~~HTTPS público y respaldos~~ **Hechos** (19 y 20/oct). Falta un servidor que no dependa de la computadora de un integrante, verificar el cifrado en reposo y aprobar el almacén (decisión #9) | [`plan-implantacion.md`](plan-implantacion.md) |
| Cifrar o reducir lo que la app guarda en el teléfono (desde el 20/oct incluye datos de salud) y borrar la caché de imágenes al cerrar sesión | App (`data/offline/`) |
