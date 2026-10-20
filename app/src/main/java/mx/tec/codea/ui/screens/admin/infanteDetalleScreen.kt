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
import mx.tec.codea.domain.Infante
import mx.tec.codea.domain.Padre
import mx.tec.codea.ui.theme.AvatarBg
import mx.tec.codea.ui.theme.AvatarFg
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.components.BackHeader

@Composable
fun InfanteDetalleScreen(
    infante: Infante,
    salas: List<String>,
    padresVinculados: List<Padre>,
    onAbrirPadre: (String) -> Unit,
    onSalaSeleccionada: (String) -> Unit,
    onDarBaja: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDFA))
            .padding(horizontal = 22.dp)
    ) {
        BackHeader(
            title = infante.nombre,
            onBackClick = onBack,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        )

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
                    Text(infante.iniciales, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AvatarFg)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(infante.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140))
                    Text("${infante.edad} años · ${infante.sala}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5B4B8A))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (infante.baja) Color(0xFFFFEDE7) else Color(0xFFE8FBF6))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (infante.baja) "Baja" else "Activo",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (infante.baja) Color(0xFFC4432A) else Color(0xFF0F7A6C)
                    )
                }
            }

            SeccionLabel("Información")
            InfoCard(titulo = "Sala actual", valor = infante.sala)

            SeccionLabel("Padre / tutor")
            if (padresVinculados.isEmpty()) {
                InfoCard(titulo = "Padre / tutor", valor = "Sin vincular todavía")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    padresVinculados.forEach { padre ->
                        PadreVinculadoRow(padre = padre, onConsultar = { onAbrirPadre(padre.id) })
                    }
                }
            }

            SeccionLabel("Cambiar de sala")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                salas.forEach { nombre ->
                    val activo = nombre == infante.sala
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AccionButton(
                    texto = if (infante.baja) "Reactivar su inscripción" else "Dar de baja su inscripción",
                    bg = if (infante.baja) Color(0xFFE8FBF6) else Color(0xFFFFEDE7),
                    fg = if (infante.baja) Color(0xFF0F7A6C) else Color(0xFFC4432A),
                    onClick = onDarBaja
                )


            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SeccionLabel(texto: String) {
    Text(
        text = texto.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = Color(0xFFA79BBF)
    )
}

@Composable
fun PadreVinculadoRow(padre: Padre, onConsultar: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color.Black.copy(alpha = 0.12f),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(padre.nombre, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140))
            Text(padre.parentesco, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5B4B8A))
        }
        Button(
            onClick = onConsultar,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4F1FB), contentColor = Color(0xFF4A4066)),
            shape = RoundedCornerShape(999.dp),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Text("Consultar info", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InfoCard(titulo: String, valor: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color.Black.copy(alpha = 0.12f),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .padding(14.dp)
    ) {
        Text(titulo.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFFA79BBF))
        Text(valor, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140))
    }
}

@Composable
fun AccionButton(texto: String, bg: Color, fg: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
        shape = RoundedCornerShape(20.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(texto, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
fun InfanteDetalleScreenPreview() {
    CodeaTheme {
        InfanteDetalleScreen(
            infante = Infante(
                id = "infante-1",
                nombre = "Mateo Iglesias",
                iniciales = "MI",
                edad = 4,
                sala = "Preescolar 2"
            ),
            salas = listOf("Maternal", "Preescolar 1", "Preescolar 2"),
            padresVinculados = listOf(
                Padre(
                    id = "padre-1",
                    nombre = "Laura Iglesias",
                    iniciales = "LI",
                    parentesco = "Mamá",
                    hijoIds = listOf("infante-1"),
                    telefono = "55 1234 5678",
                    correo = "laura.iglesias@correo.com"
                )
            ),
            onAbrirPadre = {},
            onSalaSeleccionada = {},
            onDarBaja = {},
            onBack = {}
        )
    }
}
