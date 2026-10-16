package mx.tec.codea.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import mx.tec.codea.ui.components.BackHeader
import mx.tec.codea.ui.components.AvisoColores
import mx.tec.codea.ui.theme.Poppins

data class AvisoFundacionUi(
    val id: String,
    val destino: String?,     // null = toda la fundación, si no nombre de sala
    val fecha: String,
    val texto: String,
    val nuevo: Boolean        // true solo durante la visita en la que se vieron por primera vez
)

// pantalla "Avisos de la fundación" de la asistente.
// quien la abre marca todo como leído (AvisosViewModel.alAbrir), y aquí lo que era nuevo
// se ve en "Nuevos" durante esa visita.
@Composable
fun AvisosFundacionScreen(
    avisos: List<AvisoFundacionUi>,   // más reciente primero
    onBackClick: () -> Unit,          // regresa al menú
    modifier: Modifier = Modifier,
) {
    val nuevos = avisos.filter { it.nuevo }
    val anteriores = avisos.filterNot { it.nuevo }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
    BackHeader(
        overline = "Menú",
        title = "Avisos de la fundación",
        onBackClick = onBackClick,
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Etiqueta("Nuevos") }

        if (nuevos.isEmpty()) {
            item {
                Text(
                    "No tienes avisos nuevos.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(AvisoColores.Campo)
                        .padding(16.dp),
                    fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp, color = Color(0xFF6B6183)
                )
            }
        } else {
            items(nuevos, key = { it.id }) { AvisoItem(it) }
        }

        if (anteriores.isNotEmpty()) {
            item(key = "anteriores") {
                Column {
                    Spacer(Modifier.height(4.dp))
                    Etiqueta("Anteriores")
                }
            }
            items(anteriores, key = { it.id }) { AvisoItem(it) }
        }
    }
    }
}

@Composable
private fun Etiqueta(texto: String) {
    Text(
        texto.uppercase(), fontFamily = Poppins, fontWeight = FontWeight.Bold,
        fontSize = 12.sp, letterSpacing = 1.4.sp, color = AvisoColores.Gris
    )
}

@Composable
private fun AvisoItem(aviso: AvisoFundacionUi) {
    val shape = RoundedCornerShape(22.dp)
    val paraTodos = aviso.destino == null
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (aviso.nuevo)
                    Modifier.shadow(
                        6.dp, shape,
                        ambientColor = AvisoColores.Violeta.copy(alpha = .16f),
                        spotColor = AvisoColores.Violeta.copy(alpha = .16f)
                    )
                else Modifier
            )
            .clip(shape)
            .background(if (aviso.nuevo) Color.White else AvisoColores.Fondo)
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
                if (aviso.nuevo) AvisoChip("Nuevo", bg = AvisoColores.Coral, fg = AvisoColores.Ink)
            }
            Text(
                aviso.fecha, fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp, color = AvisoColores.GrisTexto
            )
        }
        Text(
            aviso.texto, fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp, lineHeight = 21.sp, color = AvisoColores.Ink
        )
    }
}
