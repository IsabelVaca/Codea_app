# Cambios pedidos por la profe y la tutora (20 oct 2026)

Pedido textual:

- **Profe:** abrir completa la foto de un aviso, símbolos de carga en las acciones, info completa del niño, las tarjetas blancas se pierden con el fondo, el dictado sale en inglés y debe ser en español.
- **Tutor:** las tarjetas blancas se pierden con el fondo, símbolos de carga o guardado, poder abrir completas las fotos de los avisos, y para el color de las tarjetas no usar colores de mucho contraste, solo que se diferencien del fondo.

Varios puntos son el mismo arreglo visto desde los dos roles (tarjetas, fotos, carga), así que se corrigen una sola vez en el componente compartido y se heredan en ambas pantallas.

## A. Tarjetas blancas que se pierden con el fondo (profe y tutor)

Causa: `TarjetaBlanca`, `NoticeCard` y `MenuCard` pintan la tarjeta con `Color.White` fijo y solo una sombra de 10% de opacidad; sobre el fondo crema (`Cream = 0xFFFFFDFA`, casi idéntico a blanco) la sombra no basta para distinguirlas. El mismo problema se repite donde se puso `.background(Color.White)` a mano en vez de usar el componente.

Arreglo (igual al que ya se usó en el rediseño de "Mi sala", sin agregar colores nuevos): un borde delgado de 1dp con el mismo tono que ya es `outlineVariant` en el tema claro (`Lilac`, `0xFFE6E0F2`). Es un borde sutil, sin contraste fuerte, que solo marca el límite de la tarjeta — exactamente lo que pidió la tutora.

Se agrega esa constante (`Borde`) a `CodeaColores` (EstiloTarjetas.kt) y a `AvisoColores` (AvisoComponentes.kt), y se aplica en:

- `ui/components/EstiloTarjetas.kt` → `TarjetaBlanca`
- `ui/components/NoticeCard.kt` → `NoticeCard` (estado "visto", que hoy no lleva ningún borde)
- `ui/components/MenuCard.kt` → `MenuCard`
- `ui/screens/RutinaDeHoyScreen.kt` (tarjeta de la rutina)
- `ui/screens/AvisosFundacionScreen.kt` (tarjeta de cada aviso)
- `ui/screens/ChatsScreen.kt` (tarjeta de cada conversación en la bandeja)
- `ui/screens/MenuScreen.kt` (las dos tarjetas de opción que no usan `TarjetaBlanca`)
- `ui/screens/parent/notifications/Notificaciones.kt` (cada notificación del panel)

Todas las pantallas que ya usan `TarjetaBlanca`/`NoticeCard` (Mi sala, ficha del niño, Avisos de casa, Menú, Mi hijo, Hoy del padre, Calendario, Mi cuenta) quedan arregladas solo con el cambio en el componente.

## B. Símbolos de carga o guardado (profe y tutor)

Hay botones de guardar que ya tienen una bandera de "ocupado" en el ViewModel pero no la muestran (solo cambian el texto o ni eso), y una acción sin bandera ninguna. Se agrega `CircularProgressIndicator` junto al texto del botón, y se deshabilita mientras dura:

- `ui/screens/parent/child/MyChildScreen.kt`: botón "Guardar" de la salud (`saving` ya existe).
- `ui/screens/ResumenDelDiaScreen.kt`: botón "Guardar resumen" / guiado (`viewModel.guardando` ya existe).
- `ui/screens/account/AccountScreen.kt`: botón de confirmar en "Cambiar contraseña" (`changing` ya existe, hoy solo deshabilita el campo, sin ningún indicador).
- `ui/screens/parent/today/ParentTodayScreen.kt`: "Enviar nota" a la asistente — no existe bandera: se agrega `sendingNote` en `ParentViewModel`.
- `ui/screens/SelectStudentsScreen.kt` (paso final del reporte): "Guardar" — no existe bandera: se agrega `savingReport` en `CodeaViewModel.addReport`.

(El envío de mensajes del chat ya muestra "Enviando…" por burbuja; no se toca.)

## C. Abrir completa la foto de un aviso (profe y tutor)

`AvisoImagen` (ui/components/AvisoComponentes.kt) hoy solo pinta la imagen a tamaño limitado, sin poder tocarla. Se le agrega `clickable` y un visor de pantalla completa (diálogo oscuro, la imagen a su relación de aspecto real, cerrar tocando fuera o la X), con el mismo estilo que ya usa el visor de fotos del padre (`ParentPhotosScreen.VisorFoto`). Por ser el mismo componente, arregla a la vez:

- `ui/screens/AvisosFundacionScreen.kt` (avisos de la fundación, profe)
- `ui/screens/parent/notifications/Notificaciones.kt` (panel de notificaciones, tutor)

## D. El dictado sale en inglés (profe)

`ui/components/DeviceLaunchers.kt` → `rememberDictation()` manda `RecognizerIntent.EXTRA_LANGUAGE = Locale.getDefault()`, que toma el idioma del sistema del teléfono (si está en inglés, el reconocimiento de voz escucha en inglés). La app es en español siempre, así que se fija a `Locale("es", "MX")`, igual que el resto de la app formatea fechas.

## E. Info completa del niño (profe)

Comparando la ficha de la docente (`ChildProfileScreen` + `ChildProfile`) con "Mi hijo" del padre (`MyChildScreen` + `MyChild`): el padre ve fecha de nacimiento exacta y edad calculada; la docente solo ve la edad calculada (`ageText`), nunca la fecha. `TeacherApiRepository.ficha()` descarta `detail.birthDate` en vez de formatearlo.

Se agrega `birthDateText` a `ChildProfile` (igual formato que `ParentViewModel.toMyChild()`: `"d 'de' MMMM 'de' yyyy"`) y se muestra en `ChildProfileScreen`, junto a la edad.

## Qué no se toca

Las pantallas del rol `ADMIN` (directora) no se tocan: no fueron parte del pedido y usan sus propios componentes (`infanteDetalleScreen`, `avisosAdminScreen`, etc.), salvo que compartan un componente ya arreglado (por ejemplo `AvisoImagen`), en cuyo caso heredan la mejora sin cambios propios.
