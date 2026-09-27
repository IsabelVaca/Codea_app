package mx.tec.codea.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.model.Docente
import mx.tec.codea.ui.theme.CodeaTheme

data class AccionItem(
    val texto: String,
    val bg: Color,
    val fg: Color,
    val onClick: () -> Unit
)

@Composable
fun DocenteDetalleScreen(
    docente: Docente,
    acciones: List<AccionItem>,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDFA))
            .padding(horizontal = 22.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 14.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(red = 244, green = 241, blue = 250))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color(0xFF2A2140))
            }
            Text(
                text = docente.nombre,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2A2140),
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            // Encabezado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(26.dp),
                        ambientColor = Color.Black.copy(alpha = 0.15f),
                        spotColor = Color.Black.copy(alpha = 0.15f)
                    )
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFFF0F0F0))
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(docente.avatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(docente.iniciales, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = docente.avatarFg)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(docente.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140))
                    Text("${docente.sala} · Checó ${docente.horaChecada}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5B4B8A))
                }
            }

            SeccionLabel("Permisos y asignación")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                acciones.forEach { accion ->
                    AccionButton(accion.texto, accion.bg, accion.fg, accion.onClick)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DocenteDetalleScreenPreview() {
    CodeaTheme {
        DocenteDetalleScreen(
            docente = Docente(
                nombre = "Paola Sánchez",
                iniciales = "PS",
                sala = "Preescolar 2",
                ninosEnSala = 12,
                horaChecada = "7:52"
            ),
            acciones = listOf(
                AccionItem("Cambiar de sala", Color(0xFFF4F1FB), Color(0xFF4A4066), {}),
                AccionItem("Dar de baja", Color(0xFFFFEDE7), Color(0xFFC4432A), {}),
                AccionItem("Ver expediente", Color(0xFFEFE9FF), Color(0xFF5B3FE0), {})
            ),
            onBack = {}
        )
    }
}
