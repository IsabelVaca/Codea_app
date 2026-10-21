package mx.tec.codea.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import mx.tec.codea.R
import mx.tec.codea.ui.theme.Poppins

// los colores y la etiquetita que comparten las pantallas de avisos
// (la lista de la asistente y el panel de notificaciones del papá).
object AvisoColores {
    val Ink: Color @Composable get() = CodeaColores.Ink
    val Violeta: Color @Composable get() = CodeaColores.Violeta
    val VioletaSuave: Color @Composable get() = CodeaColores.Lavanda
    val Gris: Color @Composable get() = CodeaColores.Gris
    val GrisTexto: Color @Composable get() = CodeaColores.GrisMedio
    val Coral = Color(0xFFFF7A5A)
    val Verde: Color @Composable get() = CodeaColores.Verde
    val VerdeSuave: Color @Composable get() = CodeaColores.VerdeSuave
    val Fondo: Color @Composable get() = CodeaColores.FondoAviso
    val Campo: Color @Composable get() = CodeaColores.Campo
    // línea delgada para que la tarjeta del aviso no se pierda en el fondo: mismo tono que CodeaColores.Borde.
    val Borde: Color @Composable get() = CodeaColores.Borde
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
// se puede tocar para abrirla completa en un visor de pantalla entera (igual en avisos de la fundación
// y en el panel de notificaciones del papá, porque ambos usan este mismo componente).
@Composable
fun AvisoImagen(model: Any, descripcion: String = "Imagen del aviso", modifier: Modifier = Modifier) {
    var abierta by remember { mutableStateOf(false) }
    val estable = imagenEstable(model)
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        AsyncImage(
            model = estable,
            contentDescription = descripcion,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .heightIn(max = AvisoImagenAltoMax)
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClickLabel = "Ver imagen completa") { abierta = true },
        )
    }
    if (abierta) {
        VisorAvisoImagen(model = estable, descripcion = descripcion, onCerrar = { abierta = false })
    }
}

// el visor de pantalla completa: fondo casi negro, la imagen entera y una X para cerrar, como el de las fotos del chat.
@Composable
private fun VisorAvisoImagen(model: Any, descripcion: String, onCerrar: () -> Unit) {
    Dialog(onDismissRequest = onCerrar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF15111F))
                .clickable(onClick = onCerrar),
        ) {
            AsyncImage(
                model = model,
                contentDescription = descripcion,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
            IconButton(onClick = onCerrar, modifier = Modifier.statusBarsPadding().padding(8.dp)) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.chats_photo_close),
                    tint = Color.White,
                )
            }
        }
    }
}

val AvisoImagenAltoMax = 320.dp
