package mx.tec.codea.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import mx.tec.codea.ui.theme.Poppins

// los colores y la etiquetita que comparten las pantallas de avisos
// (la lista de la asistente y el panel de notificaciones del papá).
object AvisoColores {
    val Ink = Color(0xFF2A2140)
    val Violeta = Color(0xFF5B3FE0)
    val VioletaSuave = Color(0xFFEFE9FF)
    val Gris = Color(0xFFA79BBF)
    val GrisTexto = Color(0xFF8C7FA8)
    val Coral = Color(0xFFFF7A5A)
    val Verde = Color(0xFF0F7A6C)
    val VerdeSuave = Color(0xFFE8FBF6)
    val Fondo = Color(0xFFF9F7FD)
    val Campo = Color(0xFFF4F1FB)
}

@Composable
fun AvisoChip(texto: String, bg: Color, fg: Color) {
    Text(
        texto,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 11.dp, vertical = 5.dp),
        fontFamily = Poppins, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = fg
    )
}

// la imagen de un aviso con su relación de aspecto y un tope de alto: completa, sin recortar.
// una horizontal llena el ancho; una vertical llega al tope y queda centrada.
@Composable
fun AvisoImagen(model: Any, descripcion: String = "Imagen del aviso", modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        AsyncImage(
            model = imagenEstable(model),
            contentDescription = descripcion,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .heightIn(max = AvisoImagenAltoMax)
                .clip(RoundedCornerShape(14.dp)),
        )
    }
}

val AvisoImagenAltoMax = 320.dp
