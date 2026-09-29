package mx.tec.codea.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import mx.tec.codea.domain.Docente
import mx.tec.codea.ui.theme.AvatarBg
import mx.tec.codea.ui.theme.AvatarFg
import mx.tec.codea.ui.theme.CodeaTheme

@Composable
fun DocenteDetalleScreen(
    docente: Docente,
    salas: List<String>,
    onSalaSeleccionada: (String) -> Unit,
    onDarDeBaja: () -> Unit,
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
                        .background(AvatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(docente.iniciales, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AvatarFg)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(docente.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140))
                    Text("${docente.sala} · Checó ${docente.horaChecada}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5B4B8A))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (docente.baja) Color(0xFFFFEDE7) else Color(0xFFE8FBF6))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (docente.baja) "Baja" else "Activa",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (docente.baja) Color(0xFFC4432A) else Color(0xFF0F7A6C)
                    )
                }
            }

            SeccionLabel("Cambiar de sala")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                salas.forEach { nombre ->
                    val activo = nombre == docente.sala
                    Button(
                        onClick = { onSalaSeleccionada(nombre) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activo) Color(0xFF6C4BF6) else Color(0xFFF4F1FB),
                            contentColor = if (activo) Color(0xFFFFFDFA) else Color(0xFF6B6183)
                        ),
                        shape = RoundedCornerShape(999.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(nombre, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            SeccionLabel("Acciones")
            AccionButton(
                texto = if (docente.baja) "Reactivar a la docente" else "Dar de baja a la docente",
                bg = if (docente.baja) Color(0xFFE8FBF6) else Color(0xFFFFEDE7),
                fg = if (docente.baja) Color(0xFF0F7A6C) else Color(0xFFC4432A),
                onClick = onDarDeBaja
            )

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
                id = "docente-1",
                nombre = "Paola Sánchez",
                iniciales = "PS",
                sala = "Preescolar 2",
                horaChecada = "7:52"
            ),
            salas = listOf("Maternal", "Preescolar 1", "Preescolar 2"),
            onSalaSeleccionada = {},
            onDarDeBaja = {},
            onBack = {}
        )
    }
}
