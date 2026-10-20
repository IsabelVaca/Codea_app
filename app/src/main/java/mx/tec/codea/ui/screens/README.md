# `ui/screens/` (docente y comunes)

Aquí viven las pantallas de la docente. Las del admin están en [`admin/`](admin/README.md), las del padre en [`parent/`](parent/README.md), la selección de rol en `roleselection/`, **entrar** en `auth/` (`SplashScreen`, `LoginScreen`, `ActivateAccountScreen` y las piezas comunes `AuthComponents`) y **Mi cuenta** en `account/` (`AccountScreen`, para los tres roles).
Las pantallas copian el diseño y el texto del prototipo (`docs/prototipos/`) y **solo dibujan**: reciben datos y callbacks, y llaman funciones del ViewModel.

**Estilo (desde el 20/oct):** el mismo del admin y del padre, con las piezas de `ui/components/EstiloTarjetas.kt`: tarjetas blancas con sombra suave, lila con morado para lo elegido o lo hecho, gris para lo secundario, botón principal violeta y «Cerrar sesión» en rojo suave. Sin etiquetas de sección ni sobretítulos, y sin «·» en los textos de la docente. Las iniciales van en lila con morado (un solo color). El ámbar (`secondary`) queda solo para lo pendiente o tarde (llegada tarde, aviso de la familia sin ver, «nadie está presente»). Mi día, Mi sala y elegir el método de reporte se quedaron con el acomodo de StarLord133; las demás se ajustaron: Menú, Checador, entrada registrada, «Aún el día no comienza», pase de lista, ficha del niño, avisos de casa, reporte, elegir niños, resumen del día, pase de salida, día finalizado, avisos de la fundación, rutina de hoy, Chats (también la del padre) y Mi cuenta (los tres roles).

## En uso (las que registra `CodeaNavHost.kt`)

| Archivo | Pantalla | Estado |
|---|---|---|
| `MyDayScreen.kt` | Mi día: rutina y momento elegido | ✅ |
| `DayNotStartedScreen.kt` | "Aún el día no comienza" (antes del check-in) | ✅ |
| `SelectReportMethodScreen.kt` | Elegir método de reporte (escrito, dictado, foto) | ✅ |
| `ReportScreen.kt` | Formulario de reporte | ✅ Dictado con `RecognizerIntent`; la foto usa `TakePicturePreview` |
| `SelectStudentsScreen.kt` | Elegir niños del reporte | ✅ |
| `MyRoomScreen.kt` | Mi sala y asistencia | ✅ |
| `RollCallScreen.kt` | Pase de lista (desde Mi día) | ✅ |
| `ChildProfileScreen.kt` | Ficha del niño: salud, avisos de hoy, resumen, asistencia y quién lo recoge | ✅ Sale al instante con lo guardado |
| `FamilyNoticesScreen.kt` | Avisos de casa de hoy | ✅ |
| `ResumenDelDiaScreen.kt` | Resumen del día de un niño (ánimo, comida, pañales, siesta, nota), suelto o guiado | ✅ |
| `RutinaDeHoyScreen.kt` | La rutina de hoy, una vez después de checar la entrada | ✅ |
| `AvisosFundacionScreen.kt` | Avisos de la fundación | ✅ |
| `ChatsScreen.kt` | Conversaciones con las familias | ✅ Envía y lee por la API (texto y fotos: cámara con `TakePicture`+`FileProvider`, Photo Picker, EXIF y compresión antes de subir); sin tiempo real |
| `MenuScreen.kt` | Menú: perfil, checador y otras opciones | ✅ Avisos abre `AvisosFundacionScreen`; Mi perfil abre Mi cuenta; Cerrar sesión cierra la sesión de verdad |
| `CheckerScreen.kt` | Checador: entrada y salida con un toque, sin ubicación ni mapa; a las 9 pm la salida se cierra sola | ✅ |
| `CheckInDoneScreen.kt` | "Entrada registrada" | ✅ |
| `DeparturePassScreen.kt` | **Pase de salida:** con la rutina terminada, la docente registra la hora de salida de cada niño y quién lo recoge (personas autorizadas y tutores); con todos fuera aparece «Finalizar día» | 🟡 sin probar en el teléfono; necesita conexión |
| `DayFinishedScreen.kt` | «Día finalizado»: reemplaza a la rutina en Mi día después de finalizar el día (o del cierre automático de las 9 pm) | 🟡 sin probar en el teléfono |

## Pantallas borradas el 18/oct

Se quitaron `checadorScreen.kt`, `miDiaScreen.kt`, `miDiaEnCursoScreen.kt`, `confirmarMomentoScreen.kt` y `ajustarRutinaScreen.kt` (1,010 líneas): ninguna se abría desde la app, solo existían sus `@Preview`. También `ComingSoonScreen.kt`, que solo servía para «Mis documentos». Las pantallas vivas son `CheckerScreen.kt` y `MyDayScreen.kt`.

## Pendiente

- **Foto de calidad:** `TakePicturePreview` devuelve una miniatura; para fotos de verdad usar `TakePicture` con `FileProvider`, y comprimir y orientar según EXIF como pide el marco.
- **Sin conexión:** que Mi día, Mi sala y el reporte funcionen sin internet (RF-11).
- **Huella al abrir la app** (RF-13).
- **Pruebas de interfaz** con Compose UI Test (el marco las pide).
