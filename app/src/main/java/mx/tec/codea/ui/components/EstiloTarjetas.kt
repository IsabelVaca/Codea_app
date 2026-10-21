package mx.tec.codea.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.luminance
import mx.tec.codea.ui.theme.CoralLight
import mx.tec.codea.ui.theme.CoralNight
import mx.tec.codea.ui.theme.Cream
import mx.tec.codea.ui.theme.Dusk
import mx.tec.codea.ui.theme.Mist
import mx.tec.codea.ui.theme.Night
import mx.tec.codea.ui.theme.NightCard
import mx.tec.codea.ui.theme.NightRaised
import mx.tec.codea.ui.theme.Poppins
import mx.tec.codea.ui.theme.Sun
import mx.tec.codea.ui.theme.TealLight
import mx.tec.codea.ui.theme.TealNight
import mx.tec.codea.ui.theme.VioletLight
import mx.tec.codea.ui.theme.VioletNight
import mx.tec.codea.ui.theme.muted

// true con el tema oscuro: el fondo del tema es oscuro. se lee del tema (no del teléfono) para que las vistas
// previas y CodeaTheme(darkTheme = ...) también lo respeten.
private val esOscuro: Boolean
    @Composable @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background.luminance() < 0.5f

// el estilo común de la app, el mismo del admin: tarjetas blancas con sombra, lila con morado para lo que resalta
// y gris para lo secundario. lo usan todas las pantallas (admin, docente y padre).
// en claro cada color es el fijo de siempre (el del prototipo); en oscuro toma el papel equivalente del tema,
// el mismo que usa "Mi sala" de la docente (tarjetas surfaceContainerLowest, texto onSurface, lila primaryContainer).
object CodeaColores {
    // ----- texto -----
    val Ink: Color @Composable @ReadOnlyComposable get() = if (esOscuro) Cream else Color(0xFF2A2140)
    // los nombres en negro de las tarjetas (niños, chats, la docente en el Menú).
    val Titulo: Color @Composable @ReadOnlyComposable get() = if (esOscuro) Cream else Color.Black
    // títulos morados oscuros de los formularios del admin («Nueva inscripción»).
    val InkMorado: Color @Composable @ReadOnlyComposable get() = if (esOscuro) Cream else Color(0xFF4A3A80)
    // texto de un botón sobre Campo («Reenviar invitación»).
    val InkSuave: Color @Composable @ReadOnlyComposable get() = if (esOscuro) Cream else Color(0xFF4A4066)
    val Morado: Color @Composable @ReadOnlyComposable get() = if (esOscuro) VioletLight else Color(0xFF6D28D9)
    // texto, íconos y enlaces en violeta («Tocar la foto…», el circulito de carga). para rellenos va Violeta.
    // texto o ícono encima de un relleno Morado (la palomita, el número de una insignia, el día elegido):
    // blanco en claro; en oscuro el morado se aclara y encima va la tinta, como el botón de "Mi sala".
    val SobreMorado: Color @Composable @ReadOnlyComposable get() = if (esOscuro) Night else Color.White
    val VioletaTexto: Color @Composable @ReadOnlyComposable get() = if (esOscuro) VioletLight else Color(0xFF5B3FE0)
    val Gris: Color @Composable @ReadOnlyComposable get() = Color(0xFFA79BBF)
    val GrisMedio: Color @Composable @ReadOnlyComposable get() = if (esOscuro) Mist else Color(0xFF8C7FA8)
    val GrisTexto: Color @Composable @ReadOnlyComposable get() = if (esOscuro) MaterialTheme.colorScheme.muted else Color(0xFF8A80A5)
    // el gris oscuro de los chips sin elegir y de los subtítulos del admin.
    val GrisOscuro: Color @Composable @ReadOnlyComposable get() = if (esOscuro) MaterialTheme.colorScheme.muted else Color(0xFF6B6183)
    val MoradoGris: Color @Composable @ReadOnlyComposable get() = if (esOscuro) MaterialTheme.colorScheme.muted else Color(0xFF5B4B8A)
    val Verde: Color @Composable @ReadOnlyComposable get() = if (esOscuro) TealLight else Color(0xFF0F7A6C)
    val VerdeGris: Color @Composable @ReadOnlyComposable get() = if (esOscuro) TealLight.copy(alpha = 0.85f) else Color(0xFF3E7A70)
    val Rojo: Color @Composable @ReadOnlyComposable get() = if (esOscuro) CoralLight else Color(0xFFC4432A)
    val CafeRojo: Color @Composable @ReadOnlyComposable get() = if (esOscuro) CoralLight else Color(0xFF9A4A32)
    val CafeSuave: Color @Composable @ReadOnlyComposable get() = if (esOscuro) CoralLight.copy(alpha = 0.85f) else Color(0xFF9A6A5C)
    val Ambar: Color @Composable @ReadOnlyComposable get() = if (esOscuro) Sun else Color(0xFFB37F09)

    // ----- rellenos -----
    // la pantalla (crema en claro).
    val Fondo: Color @Composable @ReadOnlyComposable get() = if (esOscuro) Night else Color(0xFFFFFDFA)
    // la tarjeta blanca: en oscuro, la misma tarjeta elevada de los niños de "Mi sala".
    val Tarjeta: Color @Composable @ReadOnlyComposable get() = if (esOscuro) NightCard else Color.White
    val Lila: Color @Composable @ReadOnlyComposable get() = if (esOscuro) VioletNight else Color(0xFFEDE9FE)
    val Lavanda: Color @Composable @ReadOnlyComposable get() = if (esOscuro) VioletNight else Color(0xFFEFE9FF)
    val Campo: Color @Composable @ReadOnlyComposable get() = if (esOscuro) NightRaised else Color(0xFFF4F1FB)
    // casi blanco: las opciones sin elegir del catálogo de momentos, el fondo de un aviso leído.
    val CampoSuave: Color @Composable @ReadOnlyComposable get() = if (esOscuro) NightCard else Color(0xFFFBF8FF)
    val FondoAviso: Color @Composable @ReadOnlyComposable get() = if (esOscuro) NightCard else Color(0xFFF9F7FD)
    val GrisClaro: Color @Composable @ReadOnlyComposable get() = if (esOscuro) NightRaised else Color(0xFFF0F0F0)
    // el botón principal (Enviar, Guardar), como «Publicar» en Avisos. el texto encima es blanco en los dos temas.
    val Violeta: Color @Composable @ReadOnlyComposable get() = Color(0xFF5B3FE0)
    val VioletaMarca: Color @Composable @ReadOnlyComposable get() = Color(0xFF6C4BF6)
    val VerdeSuave: Color @Composable @ReadOnlyComposable get() = if (esOscuro) TealNight else Color(0xFFE8FBF6)
    val RojoSuave: Color @Composable @ReadOnlyComposable get() = if (esOscuro) CoralNight else Color(0xFFFFEDE7)
    val AmbarSuave: Color @Composable @ReadOnlyComposable get() = if (esOscuro) Color(0xFF3A2E12) else Color(0xFFFFF4D6)

    // ----- líneas -----
    // línea delgada para que la tarjeta blanca no se pierda en el fondo crema: el mismo tono que
    // "outlineVariant" del tema claro (Lilac), sin agregar un color nuevo ni subir el contraste.
    val Borde: Color @Composable @ReadOnlyComposable get() = if (esOscuro) NightRaised else Color(0xFFE6E0F2)
    val BordeFuerte: Color @Composable @ReadOnlyComposable get() = if (esOscuro) Dusk else Color(0xFFD8D0E8)
    val BordeLavanda: Color @Composable @ReadOnlyComposable get() = if (esOscuro) VioletLight.copy(alpha = 0.5f) else Color(0xFFD9CEFF)
    val Divisor: Color @Composable @ReadOnlyComposable get() = if (esOscuro) NightRaised else Color(0xFFF1EEF8)
    val Asa: Color @Composable @ReadOnlyComposable get() = if (esOscuro) Dusk else Color(0xFFC4BAD8)
    val Flecha: Color @Composable @ReadOnlyComposable get() = if (esOscuro) Dusk else Color(0xFFB9B0D0)
}

// la tarjeta blanca de siempre (como InfoTarjeta en el detalle del admin).
@Composable
fun TarjetaBlanca(
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    espacio: Dp = 10.dp,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    val forma = RoundedCornerShape(22.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, forma, ambientColor = Color(0x1A2A2140), spotColor = Color(0x1A2A2140))
            .clip(forma)
            .background(CodeaColores.Tarjeta)
            .border(1.dp, CodeaColores.Borde, forma)
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(espacio),
        content = contenido,
    )
}

// un dato: la etiqueta chica en gris y el valor en negritas, igual que en el detalle del admin.
@Composable
fun DatoTarjeta(etiqueta: String, valor: String, modifier: Modifier = Modifier, colorValor: Color = CodeaColores.Ink) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            etiqueta.uppercase(), fontFamily = Poppins, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.1.sp, color = CodeaColores.Gris,
        )
        Text(valor, fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorValor)
    }
}

// la pastilla de la derecha de las tarjetas: lila con morado, o gris si es algo apagado.
@Composable
fun Pastilla(texto: String, modifier: Modifier = Modifier, apagada: Boolean = false) {
    Text(
        texto,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (apagada) CodeaColores.Campo else CodeaColores.Lila)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold,
        color = if (apagada) CodeaColores.Gris else CodeaColores.Morado,
    )
}
