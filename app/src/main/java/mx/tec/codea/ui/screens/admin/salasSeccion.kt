package mx.tec.codea.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.domain.Infante
import mx.tec.codea.domain.Sala
import mx.tec.codea.ui.theme.Poppins
import mx.tec.codea.ui.theme.salaColor

// la pestaña "Salas" de Docentes: "+ Crear sala" arriba y la ocupación de cada sala (la que antes estaba en Centro).
@Composable
fun SalasSeccion(
    salas: List<Sala>,
    infantes: List<Infante>,
    onSalaClick: (Sala) -> Unit,
    onCrearSala: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // el botón de agregar va arriba de la lista, igual que en Docentes.
        CrearSalaButton(onClick = onCrearSala)
        SeccionLabel("OCUPACIÓN DE SALAS")
        if (salas.isEmpty()) {
            Text(
                "Todavía no hay salas.",
                fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFA79BBF)
            )
        }
        salas.forEach { sala ->
            SalaOcupacionCard(
                sala = sala,
                ninosActuales = infantes.count { it.sala == sala.nombre },
                onClick = { onSalaClick(sala) }
            )
        }
    }
}

// nombre, niños/cupo, la barra de ocupación con el color de la sala y su asistente.
@Composable
private fun SalaOcupacionCard(sala: Sala, ninosActuales: Int, onClick: () -> Unit) {
    val ocupacion = if (sala.cupoMaximo > 0) (ninosActuales / sala.cupoMaximo.toFloat()).coerceIn(0f, 1f) else 0f
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
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(sala.nombre, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140))
            Text("$ninosActuales/${sala.cupoMaximo} niños", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5B3FE0))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xFFF4F1FB))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ocupacion)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(999.dp))
                    .background(salaColor(sala.id))
            )
        }
        Text(sala.asistente, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFA79BBF))
    }
}

@Composable
fun CrearSalaButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(2.dp, Color(0xFFEDE9FE)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFEDE9FE),
            contentColor = Color(0xFF6D28D9)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = "+ Crear sala",
            fontSize = 15.sp,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold
        )
    }
}
