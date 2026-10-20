# `ui/components/`

Piezas reutilizables entre pantallas.

| Archivo | Qué es |
|---|---|
| `CodeaBottomBar.kt` | Barra inferior; recibe las pestañas ya filtradas por rol |
| `ScreenHeader.kt`, `BackHeader.kt` | Encabezados de pantalla (con y sin botón de regreso) |
| `EstiloTarjetas.kt` | El estilo del admin para los tres roles: `CodeaColores` (lila `EDE9FE` con morado `6D28D9`, gris para lo secundario, violeta `5B3FE0` para el botón principal, rojo para cerrar sesión), `TarjetaBlanca` (tarjeta blanca con sombra suave), `DatoTarjeta` (etiqueta gris en mayúsculas y valor en negritas) y `Pastilla`. Antes era `parent/ParentStyle.kt` (`PadreColores`, `TarjetaPadre`, `DatoPadre`, `PastillaPadre`) |
| `InfoBox.kt` | Recuadro informativo (Poppins; el color lo pone quien lo usa: lila, rojo suave o ámbar para lo pendiente) |
| `InitialsAvatar.kt` | Cuadro redondeado con iniciales: **un solo color**, lila con iniciales moradas, como en el admin (desde el 20/oct; antes ámbar, coral, violeta y turquesa). `background = Color.White` sobre una tarjeta lila |
| `NoticeCard.kt` | Aviso que dejó la familia: tarjeta blanca, borde ámbar mientras falta verlo, «Responder» lila y «Ya lo vi» violeta |
| `PersonaDropdown.kt` | Selector de personas (docente, padre, infante) usado por los formularios de admin |
| `AccessCodeDialog.kt` | El código de acceso de alguien recién dado de alta, con botón de compartir. La API lo da una sola vez, así que es la única oportunidad de copiarlo |
| `DeviceLaunchers.kt` | `rememberTakePhoto` (cámara) y `rememberDictation` (voz a texto), con sus permisos |

## Regla de los encabezados

Toda pantalla empieza igual, en los tres perfiles:

- **Pestañas** (la barra inferior): `ScreenHeader(title)`.
- **Pantallas que abren encima de una pestaña:** `BackHeader(title, onBackClick)`, con el botón redondo de regreso.
- **Sin sobretítulo** (la línea gris de arriba, «Estancia La Oruga · Administración»): se quitó de todas las pantallas el 19/oct, y el 20/oct también de las de la docente que lo usaban para un dato. Ese dato pasó a una `Pastilla` abajo del título (el resumen guiado, «Resumen 2 de 5», y los papás en el chat de la docente) o se quitó (el reporte y elegir niños). `overline` sigue en `BackHeader`/`ScreenHeader`, pero ninguna pantalla lo usa.
- No se escriben encabezados a mano ni con `TopAppBar`. Desde el 18/oct las pantallas del admin (y la bandeja de Chats) siguen esta regla.

## Notas sobre `DeviceLaunchers.kt`

- **Cámara:** usa `ActivityResultContracts.TakePicturePreview`, que devuelve **una miniatura** (`Bitmap`), no la foto completa. Sirve para el prototipo, pero para fotos reales de actividades habría que usar `TakePicture` con `FileProvider`, comprimir y corregir la orientación con EXIF (el marco lo pide así) y subir por `MediaUploader`.
- **Dictado:** usa `RecognizerIntent` del sistema (reconocedor de Google). No agrega dependencias, pero conviene confirmar que está permitido ([decisión #10](../../../../../../../../../docs/DECISIONES-PENDIENTES.md)). No hay "consulta con IA" (CRF-04).
- Permisos que pide la app: `CAMERA` y `RECORD_AUDIO` (manifiesto).

## Pendiente

- **Selector de foto de la galería** con Photo Picker (no pide permisos; está en el marco).
- **Selector de hijo** para el padre (HN4).
- **Indicador de carga y de error** reutilizable: hoy cada pantalla improvisa o solo usa el snackbar.
- **Estado vacío** reutilizable para listas sin datos.

## Imágenes con URL firmada

`ImagenEstable.kt` (`imagenEstable(url)`): las fotos y las imágenes de los avisos llegan con una URL firmada que cambia en cada carga. Le da a Coil la URL sin la firma como llave de memoria y de disco, para que una imagen que ya se vio no se vuelva a bajar. La usan `AvisoImagen` y las fotos del padre.
