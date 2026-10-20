package mx.tec.codea.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.ui.components.AvisoChip
import mx.tec.codea.ui.components.AvisoImagen
import mx.tec.codea.ui.components.BackHeader
import mx.tec.codea.ui.components.AvisoColores
import mx.tec.codea.ui.components.CodeaColores
import mx.tec.codea.ui.theme.Poppins

data class AvisoFundacionUi(
    val id: String,
    val destino: String?,     // null = toda la fundación, si no nombre de sala
    val fecha: String,
    val texto: String,
    val imagenUrl: String? = null,
    val nuevo: Boolean        // true solo durante la visita en la que se vieron por primera vez
)

// pantalla "Avisos de la fundación" de la asistente.
// quien la abre marca todo como leído (AvisosViewModel.alAbrir); lo que era nuevo lleva la pastilla "Nuevo"
// durante esa visita. todos en tarjeta blanca, como en Avisos del admin, y sin etiquetas de sección.
@Composable
fun AvisosFundacionScreen(
    avisos: List<AvisoFundacionUi>,   // más reciente primero
    onBackClick: () -> Unit,          // regresa al menú
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
    BackHeader(
        title = "Avisos de la fundación",
        onBackClick = onBackClick,
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (avisos.isEmpty()) {
            item {
                Text(
                    "Todavía no hay avisos.",
                    modifier = Modifier.padding(vertical = 24.dp),
                    fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp, color = CodeaColores.Gris
                )
            }
        }
        // los nuevos primero; dentro de cada grupo, el más reciente arriba.
        items(avisos.sortedByDescending { it.nuevo }, key = { it.id }) { AvisoItem(it) }
    }
    }
}

@Composable
private fun AvisoItem(aviso: AvisoFundacionUi) {
    val shape = RoundedCornerShape(22.dp)
    val paraTodos = aviso.destino == null
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, ambientColor = Color(0x1A2A2140), spotColor = Color(0x1A2A2140))
            .clip(shape)
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AvisoChip(
                    aviso.destino ?: "Toda la fundación",
                    bg = if (paraTodos) AvisoColores.VioletaSuave else AvisoColores.VerdeSuave,
                    fg = if (paraTodos) AvisoColores.Violeta else AvisoColores.Verde
                )
                if (aviso.nuevo) AvisoChip("Nuevo", bg = CodeaColores.Violeta, fg = Color.White)
            }
            Text(
                aviso.fecha, fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp, color = CodeaColores.Gris
            )
        }
        if (aviso.texto.isNotBlank()) {
            Text(
                aviso.texto, fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp, lineHeight = 21.sp, color = CodeaColores.Ink
            )
        }
        aviso.imagenUrl?.let { AvisoImagen(it) }
    }
}
