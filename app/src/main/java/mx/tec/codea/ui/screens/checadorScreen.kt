package mx.tec.codea.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.ui.theme.CodeaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecadorScreen(
    fecha: String = "Miércoles 19 de agosto",
    hora: String = "07:52",
    nombreDocente: String = "Miss Karla Robles",
    sala: String = "Sala Preescolar 2",
    centro: String = "Estancia La Oruga",
    turno: String = "turno de 8:00 a 16:00",
    notaLlegada: String = "Llegas 8 minutos antes",
    onRegistrarEntrada: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Checador") })
        },
        content = { paddingValues ->
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .background(Color(0xFFFFFDFA))
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF2A2140))
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                fecha.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = Color(0xFFBDB0DC)
            )
            Text(
                hora,
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                lineHeight = 44.sp,
                color = Color(0xFFFFFDFA)
            )
            Text(
                "$nombreDocente · $sala\n$centro · $turno",
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = Color(0xFFCFC5E6)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF12A594))
                .clickable(onClick = onRegistrarEntrada)
                .padding(22.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.24f))
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Registrar mi entrada", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text(notaLlegada, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.88f))
            }
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EtiquetaEstado("Ubicación verificada", Color(0xFFE8FBF6), Color(0xFF0F7A6C))
            EtiquetaEstado("Sin papel ni firma", Color(0xFFEFE9FF), Color(0xFF5B3FE0))
        }
    }
        }
    )
}

@Composable
fun EtiquetaEstado(texto: String, bg: Color, fg: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 13.dp, vertical = 9.dp)
    ) {
        Text(texto, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}

@Preview(showBackground = true)
@Composable
fun ChecadorScreenPreview() {
    CodeaTheme {
        ChecadorScreen(
            onRegistrarEntrada = {}
        )
    }
}
