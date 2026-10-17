# `ui/components/`

Piezas reutilizables entre pantallas.

| Archivo | Qué es |
|---|---|
| `CodeaBottomBar.kt` | Barra inferior; recibe las pestañas ya filtradas por rol |
| `ScreenHeader.kt`, `BackHeader.kt` | Encabezados de pantalla (con y sin botón de regreso) |
| `InfoBox.kt` | Recuadro informativo |
| `InitialsAvatar.kt` | Círculo con iniciales |
| `PersonaDropdown.kt` | Selector de personas (docente, padre, infante) usado por los formularios de admin |
| `DeviceLaunchers.kt` | `rememberTakePhoto` (cámara) y `rememberDictation` (voz a texto), con sus permisos |

## Notas sobre `DeviceLaunchers.kt`

- **Cámara:** usa `ActivityResultContracts.TakePicturePreview`, que devuelve **una miniatura** (`Bitmap`), no la foto completa. Sirve para el prototipo, pero para fotos reales de actividades habría que usar `TakePicture` con `FileProvider`, comprimir y corregir la orientación con EXIF (el marco lo pide así) y subir por `MediaUploader`.
- **Dictado:** usa `RecognizerIntent` del sistema (reconocedor de Google). No agrega dependencias, pero conviene confirmar que está permitido ([decisión #10](../../../../../../../../../docs/DECISIONES-PENDIENTES.md)). No hay "consulta con IA" (CRF-04).
- Permisos que pide la app: `CAMERA` y `RECORD_AUDIO` (manifiesto).

## Pendiente

- **Selector de foto de la galería** con Photo Picker (no pide permisos; está en el marco).
- **Selector de hijo** para el padre (HN4).
- **Indicador de carga y de error** reutilizable: hoy cada pantalla improvisa o solo usa el snackbar.
- **Estado vacío** reutilizable para listas sin datos.
