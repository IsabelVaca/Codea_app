# Qué falta de la app

Foto a **2026-10-20**, sobre `main` (PR #11 a #17) y el servidor compartido. Lo que dice "hecho" está compilado y con pruebas (**343 de la API** y **140 JVM**, corridas el 20/oct en verde); lo que dice "visto" se abrió en un teléfono o en el emulador ([`RECORRIDO.md`](RECORRIDO.md), [`BITACORA.md`](BITACORA.md)). Lo demás no se ha recorrido en un teléfono.

Entrega final: **jueves 22 de octubre de 2026, 23:59**. El congelamiento de funciones nuevas fue el **lunes 19 por la noche**: martes y miércoles solo se corrige y se documenta.

## 1. Requisitos sin cumplir (❌, 5 de 32; en total 19 ✅ y 8 🟡, ~72 %)

| Requisito | Qué pasa |
|---|---|
| CRF-01 Bitácora de pagos | Fuera de alcance por decisión del equipo; se documenta como desviación. No se integra `feature/pagos` |
| RF-13 Desbloqueo con huella | Falta `BiometricPrompt`; la [decisión #4](DECISIONES-PENDIENTES.md) sigue pendiente |
| RNF04 Disponibilidad del 99 % | Ya hay servidor público con HTTPS, pero en una sola Mac y sin monitoreo (existen `/health/live` y `/health/ready`) |
| HN1 Imprimir reportes para coordinación | Falta exportar a PDF o usar `PrintManager` |
| HN2 La asistente ve la llegada de los padres | Sin modelo ni pantalla |

## 2. Requisitos parciales (🟡, 8 de 32)

| Requisito | Qué falta |
|---|---|
| CRF-03 Calendario de eventos y citas | Solo muestra asistencia del mes; faltan los modelos de eventos y citas |
| CRF-04 Dictado y consulta con IA | El dictado a texto sí funciona; la consulta con IA no existe ([decisión #10](DECISIONES-PENDIENTES.md)) |
| RF-05 Reporte para varios niños | La API está probada; la pantalla de selección no se ha recorrido en el teléfono |
| RF-09 Fotografía de la actividad | La captura con la cámara no se ha probado; usa `TakePicturePreview` (miniatura) |
| RF-11 y RNF07 Sin conexión | Se escribe sin red solo la asistencia. Desde el 20/oct los tres roles **ven** lo último que cargaron (Room), pero reportes, fotos, chat, avisos y el pase de salida necesitan red |
| RNF05 Consulta de fotos en menos de 4 s | URLs firmadas, paginación por cursor y caché de imágenes, pero nadie lo ha medido |
| HN4 Varios hijos por padre | Hecho (selector de hijo); falta probarlo con una familia de dos hijos. El admin ya puede ligar un padre a más niños (20/oct) |

## 3. Por rol

### Docente

- **"Mis documentos"** se quitó del Menú; la función no existe.
- **Editar el perfil** (nombre, teléfono, foto, `PATCH /me`): Mi cuenta sirve para ver los datos y cambiar la contraseña, no para editarlos (P2).
- **Escrituras sin conexión**: reportes, resumen del día y pase de salida necesitan red.
- **Chat en tiempo real**: hoy se consulta con `?after=`; el marco permite SSE o WebSocket.
- Recorrer en el teléfono el reporte para **varios niños** y la **cámara**.

### Admin

- Leer el **registro de auditoría** (`GET /audit-log`) desde la app: la API existe, no hay pantalla.
- **Horario** (Ajustes → Horario): mejorar el diseño (claridad de las horas, validación visible, efecto del cambio).
- P2: editar los datos de una docente, un infante o un padre (hoy solo cambian sala, baja y los niños del padre), borrar avisos del historial.
- Decidir si los **turnos** (`shift`) se quitan del backend.

### Padre

- **Paginación de fotos** y probar la galería con varias fotos reales.
- Probar el **selector de hijo** con una familia de dos hijos (el admin ya puede ligar a un padre con otro niño para armarla).
- Hay un **dilema ético** sin resolver: con el borrado directo, la familia de otro niño que salga en la foto la pierde sin que nadie la consulte ([`entrega/etica-legal.md`](entrega/etica-legal.md), dilema 1).

## 4. Servidor e infraestructura

- ✅ Servidor compartido con HTTPS (`api.codeaapp.site`, túnel de Cloudflare, [decisión #7](DECISIONES-PENDIENTES.md)), base en Aiven y fotos en SeaweedFS.
- ✅ **Respaldos** (20/oct): diario a las 3:00, base y fotos, 14 días en la Mac servidor, restauración probada. Ver [`guarderia-api/README.md`](../guarderia-api/README.md), «Respaldos».
- ✅ **Almacén** (20/oct): SeaweedFS con hasta 100 volúmenes; ya no sale «only 0 volumes left».
- **Candado al seed**: que `seed_demo.py --borrar-todo` se niegue fuera de desarrollo salvo que se escriba el nombre de la base (así se borró producción el 19/oct, [incidente](INCIDENTE-2026-10-19.md)).
- **APK de release firmado**: necesita un keystore y sus contraseñas.
- **La Mac servidor no se debe dormir**; si se duerme, reiniciar el túnel (`guarderia-api/README.md`).
- Cambiar el valor por omisión de `API_BASE_URL` en `app/build.gradle.kts` (hoy el emulador) por la API compartida, y quitar de las guías los pasos de `adb reverse`.
- Volver a dar de alta, desde un admin que apunte a la API, las cuentas reales que se perdieron el 19/oct.

## 5. Entrega

- **Documento de arquitectura (diagrama de capas y relaciones): no existe** y la rúbrica ([`ENTREGA-3.md`](ENTREGA-3.md), parte 4) lo pide.
- **Escaneo dinámico (OWASP ZAP)** contra `api.codeaapp.site` y sus hallazgos.
- Huecos **[completar por el equipo]** en `docs/entrega/`: responsable y canal del aviso de privacidad, distribución del trabajo (4.2), retrospectiva (5.2) y las filas de `plan-implantacion.md`.
- Repasar la matriz de trazabilidad con el estado final de [`REQUISITOS.md`](REQUISITOS.md).
- Guion de la presentación de 15 minutos, video de respaldo, captura del Planner, tag de la versión entregada y entrega en Canvas.

## 6. Decisiones sin aprobar

Las decisiones #2, #4, #5, #6, #8, #9 y #10 de [`DECISIONES-PENDIENTES.md`](DECISIONES-PENDIENTES.md) siguen en "Pendiente". La #1 (login con JWT) está implementada, pero le falta la aprobación formal. La #7 (túnel) y la #11 (respaldo con `launchd`) están aprobadas.

## Orden sugerido para el martes y el miércoles

1. Candado al seed y APK de release firmado.
2. Recorrido completo de los tres roles en un teléfono contra la API compartida; anotar fallas en [`RECORRIDO.md`](RECORRIDO.md) y corregir solo lo que bloquee.
3. Documento de arquitectura y escaneo ZAP.
4. Huecos de `docs/entrega/`, repaso de la matriz de trazabilidad y presentación (guion, video y Planner).

## Lo que salió de esta lista desde el 18/oct

Selector de hijo (HN4, falta probarlo), menú del día (HN3), avisos con imagen (HN5), hora de entrada y de salida del niño (RF-03), borrado directo de fotos (RF-10), «Mi hijo» con datos de salud, personas autorizadas en el admin, invitación por WhatsApp, servidor público con HTTPS, rediseño del admin (Avisos, Salas en Docentes, filtros), vincular a un padre con más niños, estilo del admin en las pantallas del padre y de la docente, caché en Room para los tres roles, respaldos y almacén.
