# Aviso de privacidad de Codea

**Estado: borrador para revisión del equipo.** Describe lo que el código de la rama `integracion` (commit `22c0008`, 2026-10-18) hace con los datos. Lo que el código no hace se dice como no hecho. Lo que solo el equipo o el centro pueden llenar está marcado con **«[completar por el equipo]»**. Antes de publicarlo a familias reales debe revisarlo quien tenga responsabilidad legal sobre el centro: aquí no se da asesoría jurídica.

Versión del aviso: 0.1 · Fecha: 18 de octubre de 2026.

> **Nota de estado (18-oct):** la revisión de solicitudes de borrado de fotos ya existe en la API y en la pantalla del administrador (commits `79deb06` y `8af6958`). No se ha probado en un teléfono con una foto real (`docs/RECORRIDO.md`, falla 10), y no define todavía un plazo de respuesta.

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
| Asistencia diaria (presente o falta). **Sin pase de lista, el niño cuenta como ausente** | La docente o el administrador |
| **Salida del niño:** hora, y quién lo recogió (nombre de la persona autorizada o del tutor, guardado como texto) | La docente, en el pase de salida; la hora la pone el servidor |
| Resumen del día: ánimo, cuánto comió, cambios de pañal, minutos de siesta y nota general | La docente |
| **Reportes** de incidente, logro, salud o conducta, con su descripción | La docente |
| **Fotografías** de las actividades en las que aparece | La docente, con la cámara del teléfono |
| **Documentos** que pide el centro (por ejemplo, certificado de alergias o informe de discapacidad, que son los que trae el ejemplo del sistema) | Antes el tutor los subía desde la app; **desde el 18/oct la app ya no tiene esa pantalla**. La API de documentos sigue en el servidor y conserva los que ya existían |
| Mensajes de la familia sobre el niño, y **fotos que se mandan por el chat** | El tutor y la docente |

Los datos de **salud** (alergias, condiciones médicas, medicamentos, reportes de salud, certificados) se tratan como **sensibles**. Los datos de **menores** se tratan con el nivel de protección más alto que define este proyecto.

### 2.3 Lo que la app no recaba

Se comprobó en el manifiesto (`app/src/main/AndroidManifest.xml`) y en las dependencias (`app/build.gradle.kts`):

- **No hay rastreo de ubicación en segundo plano.** Solo se piden los permisos de ubicación aproximada y precisa, que se usan con la app abierta, y no existe el permiso de ubicación en segundo plano.
- **No hay herramientas de analítica, publicidad ni Firebase** en las dependencias.
- **No se guarda audio.** El dictado usa el reconocedor de voz del teléfono y la app solo recibe el texto.
- **No se pide acceso a contactos, SMS ni almacenamiento general.** Para subir un documento se usa el selector de archivos del sistema.

### 2.4 El checador y la ubicación

Desde el 18 de octubre de 2026 el checador **no usa la ubicación del teléfono**:

1. La docente abre el checador y toca «Registrar mi entrada». La app **no pide el permiso de ubicación** (ya no está declarado en el manifiesto) ni usa mapas.
2. El **servidor** guarda la hora (la del servidor, no la del teléfono), si hubo retardo y el motivo que escribió la docente. Hay una fila por docente y por día.
3. La docente registra su salida con otro toque. Si no la registra, a las **21:00** (hora del centro) el servidor **cierra el día solo**: guarda esa hora como salida y marca el registro como automático.
4. Las coordenadas y distancias que se guardaron antes de este cambio se **borran** con la migración `e74f0a5b3d43`. Mientras esa migración no se aplique en la base de Aiven, esa base todavía conserva esas columnas.
5. Como el checador ya no verifica dónde está la docente, el registro prueba la hora de la entrada, no el lugar.


### 2.5 Copia local en el teléfono (modo sin conexión)

Para que la docente pueda marcar la asistencia aunque no haya red, la app guarda **en el teléfono** una base local (`codea.db`):

| Qué se guarda | Dónde y cuánto tiempo |
|---|---|
| **Nombres de los niños de la sala**, su asistencia de hoy y si tienen chat sin leer o nota de la familia; nombre de la sala y de la docente | Es una copia de lo último que se vio en Mi sala. Se reemplaza en cada carga y **se borra cuando se cierra la sesión o la sesión termina** |
| **Marcas de asistencia pendientes**: número del niño (sin nombre), presente o falta, fecha y quién las hizo (identificador de la docente) | Hasta que se envían al servidor. **No se borran al cerrar sesión**, para no perder marcas sin enviar, pero solo se envían con la sesión de la docente que las hizo |

Esa base **no está cifrada** (los tokens de sesión sí, con una llave del Android Keystore). Está en el almacenamiento privado de la app y no se incluye en copias de seguridad (`allowBackup="false"`). El borrado de los nombres al terminar la sesión está en el código y se comprobó a mano leyendo la base del teléfono (queda sin filas de niños al cerrar sesión); no tiene prueba automática. Mientras haya una sesión abierta, quien tenga el teléfono desbloqueado podría ver los nombres de los niños de esa sala. Esto solo aplica al perfil de docente.

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
| Marcar la asistencia de los niños aunque no haya conexión y enviarla sola al volver la red | Copia local descrita en 2.5 |
| Verificar la asistencia y puntualidad del personal | Ubicación, hora y retardo del checador (entrada y salida) |
| Resumir a la familia la semana del niño (asistencia, ánimo, comidas, siesta, logros y notas) | Se calcula al consultar a partir del resumen del día, los reportes y las notas; no guarda datos nuevos |
| Mantener una bitácora de quién hizo qué, para investigar incidentes y errores | Cuenta, IP, acciones del administrador |
| Frenar el adivinado de contraseñas y códigos | Correo e IP de los intentos fallidos |

### 3.2 Finalidades que **no** existen

El sistema no usa los datos para publicidad ni perfiles comerciales, y no los vende. No hay en el código ninguna función que lo haga. **[El equipo debe confirmar que esto es lo que quiere declarar.]**

## 4. Con quién se comparten y dónde se guardan

### 4.1 Dentro del centro (quién ve qué)

| Quién | Qué puede ver |
|---|---|
| **Tutor (padre o madre)** | Solo a **sus** hijos: resumen del día, calendario de asistencia, fotos donde aparecen, documentos, personas autorizadas, mensajes con las docentes de la sala y avisos de sus salas o del centro. **Una foto etiquetada con varios niños la ven las familias de todos ellos** |
| **Docente** | Solo a los niños de **su** sala: lista, asistencia, rutina, reportes, los datos de salud que la familia escribió, las salidas del día, mensajes con sus familias y la lista de personas autorizadas. Ve los avisos del centro y de su sala |
| **Administrador** | Todos los niños, las salas, las docentes, los tutores, los documentos, las fotos, los avisos, la bitácora de auditoría y el centro. **No puede leer los mensajes** entre familias y docentes a través de la API: es una regla del sistema, no un cifrado |
| **Persona con acceso técnico a la base de datos o al almacén** | Todo lo que no esté cifrado: es decir, casi todo (ver 6) |

### 4.2 Proveedores y terceros

| Tercero | Para qué | Qué datos le llegan | Estado |
|---|---|---|---|
| **Aiven** (Postgres alojado) | Guardar la base de datos. Hoy se usa para las **pruebas manuales** del equipo, con datos de ejemplo | Todas las tablas | En uso para pruebas. La base de producción **no está definida** **[completar por el equipo]** |
| **Almacén de archivos compatible con S3** | Guardar fotos y documentos | Los archivos | En desarrollo corre SeaweedFS en un contenedor local. El de producción **no está decidido** (decisión #9 y plan de implantación) **[completar por el equipo]** |
| **Google (reconocedor de voz del teléfono)** | Convertir la voz del dictado en texto | La voz que dicta la docente, procesada por el servicio de voz del sistema Android, según su configuración; la decisión #10 de `docs/DECISIONES-PENDIENTES.md` dice que el reconocedor es el de Google y sigue sin confirmarse | Solo si la docente elige «Dictado» |

No se transfieren datos a otros terceros desde el código. No hay envío de correos desde el sistema: los códigos de acceso los entrega el administrador en persona o por el medio que elija.

## 5. Cuánto tiempo se conservan

**Hoy el sistema no borra nada por su cuenta**, con estas excepciones:

| Dato | Qué pasa |
|---|---|
| Intentos fallidos de entrar (correo e IP) | Se borran los de más de 1 día; los de un correo se borran cuando ese correo entra bien |
| Persona autorizada para recoger | Se borra de verdad cuando el tutor o el administrador la quita |
| Copia local de Mi sala en el teléfono de la docente | Se borra al terminar la sesión (comprobado a mano en un teléfono, sin prueba automática). Las marcas pendientes se borran al enviarse |
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
| Archivos de solicitudes de borrado atendidas | De inmediato, en el bucket |

## 6. Cómo se protegen

Medidas que existen en el código:

- Contraseñas con Argon2id; tokens y códigos de acceso solo como hash SHA-256.
- Sesión con un token de acceso de 5 minutos y renovación rotada; si alguien reutiliza un token ya usado, se cierran todas sus sesiones.
- Bloqueo temporal tras 5 fallos seguidos por correo o 30 por IP en 15 minutos.
- Permisos por rol y por registro: un recurso fuera del alcance responde «no existe» (404).
- Dar de baja a una persona le corta el acceso en la siguiente petición.
- Fotos y documentos no pasan por la API: se suben y se leen con direcciones firmadas de 5 y 10 minutos.
- En el teléfono, los tokens se guardan cifrados con una llave del Android Keystore. La copia local de Mi sala se borra al terminar la sesión.
- Cabeceras de seguridad y `Cache-Control: no-store` en la API; HSTS cuando el servidor no está en modo desarrollo.
- Bitácora de auditoría de inicios de sesión y de altas, bajas, cambios del administrador, cambios a la ubicación del centro y decisiones sobre solicitudes de borrado de fotos.

Medidas que **no** existen todavía (se dicen para no prometer de más):

- **No hay HTTPS desplegado:** la API no está publicada en ninguna dirección pública. La app de release exige HTTPS, pero la dirección de producción no existe aún.
- **No se verificó el cifrado en reposo** de la base ni del almacén de archivos: depende del proveedor.
- **No hay respaldos** definidos.
- **La base local del teléfono (`codea.db`) no está cifrada** y guarda nombres de niños mientras haya sesión (2.5).
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
| **Acceso** | El tutor ve en la app el resumen del día, el calendario, las fotos, los documentos y las personas autorizadas de sus hijos. El administrador ve los expedientes | No hay una exportación de «todos mis datos». Hay que pedirla al administrador, que no tiene una función para generarla |
| **Rectificación** | El administrador puede cambiar de sala a un niño y administra las personas autorizadas; el tutor puede corregir las alergias y demás datos de salud de su hijo desde «Mi hijo» | **No hay pantalla ni endpoint para corregir** nombre, fecha de nacimiento, alergias, correo o teléfono de una cuenta (está como «editar datos» en `docs/plan/01-app-pantallas.md`, prioridad P2). Hoy solo se corrige con acceso directo a la base |
| **Cancelación** | El administrador puede **dar de baja** una cuenta o un niño (se desactiva el acceso). El tutor puede **pedir que se borre una foto** desde la pantalla Fotos; el administrador la ve en «Fotos que piden borrar» y puede **borrarla** (se elimina del almacén y de las galerías de todas las familias etiquetadas) o **conservarla** | **La baja no borra datos.** No hay forma de borrar un expediente completo desde la app. No se pueden borrar documentos por esa vía (solo fotos). En una foto de grupo no se puede quitar solo la etiqueta de un niño: al aprobar, desaparece para todos. No hay plazo de atención definido |
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
| ~~Corregir el texto de permiso de ubicación («no se guarda»)~~ **Hecho** en `f814763`. Sigue abierto decidir si se dejan de guardar las coordenadas | `staff_check_in` |
| Mostrar y registrar la aceptación del aviso al activar la cuenta | App (no existe) |
| Probar la revisión de solicitudes de borrado en un teléfono con una foto real y definir el plazo de atención | `media` (RF-10) |
| Plazos de retención aprobados e implementados | Tareas de limpieza en el servidor |
| Responsable, domicilio y canal de contacto | Este documento |
| HTTPS público, respaldos y proveedor del almacén de archivos | [`plan-implantacion.md`](plan-implantacion.md) |
