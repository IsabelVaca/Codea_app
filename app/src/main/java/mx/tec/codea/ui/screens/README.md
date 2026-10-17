# `ui/screens/` (docente y comunes)

Aquí viven las pantallas de la docente. Las del admin están en [`admin/`](admin/README.md), las del padre en [`parent/`](parent/README.md) y la selección de rol en `roleselection/`.
Las pantallas copian el diseño y el texto del prototipo (`docs/prototipos/`) y **solo dibujan**: reciben datos y callbacks, y llaman funciones del ViewModel.

## En uso (las que registra `CodeaNavHost.kt`)

| Archivo | Pantalla | Estado |
|---|---|---|
| `MyDayScreen.kt` | Mi día: rutina y momento elegido | ✅ |
| `DayNotStartedScreen.kt` | "Aún el día no comienza" (antes del check-in) | ✅ |
| `SelectReportMethodScreen.kt` | Elegir método de reporte (escrito, dictado, foto) | ✅ |
| `ReportScreen.kt` | Formulario de reporte | ✅ Dictado con `RecognizerIntent`; la foto usa `TakePicturePreview` |
| `SelectStudentsScreen.kt` | Elegir niños del reporte | ✅ |
| `MyRoomScreen.kt` | Mi sala y asistencia | ✅ |
| `ChatsScreen.kt` | Conversaciones con las familias | ✅ Envía y lee por la API; sin tiempo real |
| `MenuScreen.kt` | Menú: perfil, checador y otras opciones | 🟡 Documentos y perfil abren `ComingSoon`; Avisos ya abre `AvisosFundacionScreen` |
| `CheckerScreen.kt` | Checador (GPS y mapa) | ✅ Mapa con Google Maps |
| `CheckInDoneScreen.kt` | "Entrada registrada" | ✅ |
| `ComingSoonScreen.kt` | Marcador de lo no construido | — |

## Posiblemente muertas (no las registra el grafo; revisar antes de borrar)

| Archivo | Observación |
|---|---|
| `checadorScreen.kt` (`ChecadorScreen`) | Versión paralela de `CheckerScreen.kt` |
| `miDiaScreen.kt` (`MiDiaScreen`), `miDiaEnCursoScreen.kt` | Versiones paralelas de `MyDayScreen.kt` |
| `ajustarRutinaScreen.kt`, `confirmarMomentoScreen.kt` | Solo aparecen en sus propios `@Preview` |

Comprobado el 2026-10-16 con búsqueda de texto. Si se confirma que nada las usa, se pueden eliminar; la convención de nombres del repo es "igual que el archivo vecino", así que no las renombres.

## Pendiente

- Las dos opciones `ComingSoon` del menú: **documentos** y **perfil** (los avisos ya existen).
- **"Finalizar día"** al terminar la rutina (tarea abierta en Teams).
- **Mandar fotos** desde el chat (tarea abierta en Teams).
- **Foto de calidad:** `TakePicturePreview` devuelve una miniatura; para fotos de verdad usar `TakePicture` con `FileProvider`, y comprimir y orientar según EXIF como pide el marco.
- **Sin conexión:** que Mi día, Mi sala y el reporte funcionen sin internet (RF-11).
- **Huella al abrir la app** (RF-13).
- **Pruebas de interfaz** con Compose UI Test (el marco las pide).
