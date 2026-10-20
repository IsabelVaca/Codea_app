package mx.tec.codea.ui.screens.parent

import androidx.compose.foundation.background
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
import mx.tec.codea.ui.theme.Poppins

// las pantallas del padre usan el mismo estilo que las del admin: tarjetas blancas con sombra,
// lila con morado para lo que resalta y gris para lo secundario. los colores son fijos, como en el admin.
object PadreColores {
    val Ink = Color(0xFF2A2140)
    val Morado = Color(0xFF6D28D9)
    val Lila = Color(0xFFEDE9FE)
    // el botón principal (Enviar, Guardar), como «Publicar» en Avisos.
    val Violeta = Color(0xFF5B3FE0)
    val Gris = Color(0xFFA79BBF)
    val GrisTexto = Color(0xFF8A80A5)
    val Campo = Color(0xFFF4F1FB)
    val Verde = Color(0xFF0F7A6C)
    val Rojo = Color(0xFFC4432A)
    val RojoSuave = Color(0xFFFFEDE7)
}

// la tarjeta blanca de siempre (como InfoTarjeta en el detalle del admin).
@Composable
fun TarjetaPadre(
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    espacio: Dp = 10.dp,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(22.dp), ambientColor = Color(0x1A2A2140), spotColor = Color(0x1A2A2140))
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(espacio),
        content = contenido,
    )
}

// un dato: la etiqueta chica en gris y el valor en negritas, igual que en el detalle del admin.
@Composable
fun DatoPadre(etiqueta: String, valor: String, modifier: Modifier = Modifier, colorValor: Color = PadreColores.Ink) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            etiqueta.uppercase(), fontFamily = Poppins, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.1.sp, color = PadreColores.Gris,
        )
        Text(valor, fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorValor)
    }
}

// la pastilla de la derecha de las tarjetas: lila con morado, o gris si es algo apagado.
@Composable
fun PastillaPadre(texto: String, modifier: Modifier = Modifier, apagada: Boolean = false) {
    Text(
        texto,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (apagada) PadreColores.Campo else PadreColores.Lila)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold,
        color = if (apagada) PadreColores.Gris else PadreColores.Morado,
    )
}
