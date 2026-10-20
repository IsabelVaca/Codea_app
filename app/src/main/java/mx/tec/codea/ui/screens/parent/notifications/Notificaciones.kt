package mx.tec.codea.ui.screens.parent.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import mx.tec.codea.ui.components.AvisoChip
import mx.tec.codea.ui.components.AvisoImagen
import mx.tec.codea.ui.components.AvisoColores
import mx.tec.codea.ui.theme.Poppins

// ícono de campana (24×24, línea de 2dp)
private val IconoCampana: ImageVector by lazy {
    ImageVector.Builder(
        name = "Campana", defaultWidth = 22.dp, defaultHeight = 22.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        listOf(
            "M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9",
            "M10.3 21a1.9 1.9 0 0 0 3.4 0"
        ).forEach { d ->
            addPath(
                pathData = addPathNodes(d),
                stroke = SolidColor(AvisoColores.Violeta), strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round
            )
        }
    }.build()
}

// hoy solo hay avisos; foto, momento y documento quedan listos para cuando
// la API tenga notificaciones de esos tipos.
enum class TipoNotif(val etiqueta: String, val bg: Color, val fg: Color) {
    AVISO("Aviso", Color(0xFFEFE9FF), Color(0xFF5B3FE0)),
    FOTO("Foto", Color(0xFFFFF4D6), Color(0xFFB37F09)),
    MOMENTO("Momento", Color(0xFFE8FBF6), Color(0xFF0F7A6C)),
    DOCUMENTO("Documento", Color(0xFFF4F1FB), Color(0xFF5B4B8A))
}

data class NotifUi(
    val id: String,
    val tipo: TipoNotif,
    val fecha: String,
    val texto: String,
    val imagenUrl: String? = null,
    val nueva: Boolean    // true solo durante la visita en la que se abrió el panel
)

// encabezado de la pantalla Hoy: título a la izquierda, campana a la derecha.
@Composable
fun HoyHeader(
    titulo: String,           // "El día de Mateo"
    pendientes: Int,
    onAbrirNotif: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 22.dp,
    // si viene, se dibuja el botón de Mi cuenta junto a la campana.
    onCuentaClick: (() -> Unit)? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(start = horizontalPadding, end = horizontalPadding, top = 6.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                titulo, fontFamily = Poppins, fontWeight = FontWeight.Bold,
                fontSize = 22.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground, maxLines = 1
            )
        }
        BotonCampana(pendientes, onAbrirNotif)
        if (onCuentaClick != null) BotonCuenta(onCuentaClick)
    }
}

@Composable
private fun BotonCuenta(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AvisoColores.VioletaSuave)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Person, contentDescription = "Mi cuenta", tint = AvisoColores.Ink)
    }
}

@Composable
private fun BotonCampana(pendientes: Int, onClick: () -> Unit) {
    Box {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AvisoColores.VioletaSuave)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(IconoCampana, contentDescription = "Notificaciones", tint = Color.Unspecified)
        }
        if (pendientes > 0) {
            Text(
                pendientes.toString(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                    .border(2.dp, Color(0xFFFFFDFA), RoundedCornerShape(50))
                    .clip(RoundedCornerShape(50))
                    .background(AvisoColores.Coral)
                    .padding(horizontal = 5.dp),
                fontFamily = Poppins, fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp, color = AvisoColores.Ink, textAlign = TextAlign.Center
            )
        }
    }
}

// panel de notificaciones: tarjeta desde arriba con fondo oscurecido.
@Composable
fun PanelNotificaciones(
    notifs: List<NotifUi>,          // más reciente primero
    onCerrar: () -> Unit
) {
    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(AvisoColores.Ink.copy(alpha = .35f))
                .clickable(onClick = onCerrar)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(.88f)
                    .shadow(24.dp, RoundedCornerShape(26.dp))
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFFFFFDFA))
                    .clickable(enabled = false) {}   // evita cerrar al tocar dentro
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 14.dp, top = 18.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Notificaciones", fontFamily = Poppins, fontWeight = FontWeight.Bold,
                        fontSize = 17.sp, color = AvisoColores.Ink
                    )
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AvisoColores.Campo)
                            .clickable(onClick = onCerrar),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "✕", fontFamily = Poppins, fontWeight = FontWeight.Bold,
                            fontSize = 16.sp, color = Color(0xFF6B6183)
                        )
                    }
                }
                if (notifs.isEmpty()) {
                    Text(
                        "Todavía no tienes notificaciones.",
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                        fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp, color = AvisoColores.GrisTexto
                    )
                }
                LazyColumn(
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifs, key = { it.id }) { NotifItem(it) }
                }
            }
        }
    }
}

@Composable
private fun NotifItem(n: NotifUi) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (n.nueva)
                    Modifier.shadow(
                        4.dp, shape,
                        ambientColor = AvisoColores.Violeta.copy(alpha = .16f),
                        spotColor = AvisoColores.Violeta.copy(alpha = .16f)
                    )
                else Modifier
            )
            .clip(shape)
            .background(if (n.nueva) Color.White else AvisoColores.Fondo)
            .padding(horizontal = 14.dp, vertical = 12.dp),
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
                AvisoChip(n.tipo.etiqueta, bg = n.tipo.bg, fg = n.tipo.fg)
                if (n.nueva) Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(AvisoColores.Coral))
            }
            Text(
                n.fecha, fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp, color = AvisoColores.GrisTexto
            )
        }
        if (n.texto.isNotBlank()) {
            Text(
                n.texto, fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp, lineHeight = 19.5.sp, color = AvisoColores.Ink
            )
        }
        n.imagenUrl?.let { AvisoImagen(it) }
    }
}
