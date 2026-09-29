package mx.tec.codea.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import mx.tec.codea.ui.screens.admin.SeccionLabel
import mx.tec.codea.ui.theme.CodeaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmarMomentoScreen(
    bloqueHora: String,
    bloqueNombre: String,
    bloquePregunta: String,
    textoTodoBien: String,
    onMarcarNinoPorNino: () -> Unit,
    onDictarLoQuePaso: () -> Unit,
    onNotaDeUnSoloNino: () -> Unit,
    onFotosDeLaActividad: () -> Unit,
    onTodoBien: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Confirmar momento") })
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
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(26.dp),
                        ambientColor = Color.Black.copy(alpha = 0.12f),
                        spotColor = Color.Black.copy(alpha = 0.12f)
                    )
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFFE8FBF6))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    bloqueHora.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp, color = Color(0xFF5E8F87)
                )
                Text(bloqueNombre, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F7A6C))
                Text(bloquePregunta, fontSize = 14.sp, lineHeight = 21.sp, color = Color(0xFF4E7C75))
            }

            SeccionLabel("Confirmar este momento")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OpcionConfirmar("Marcar niño por niño", Color(0xFF6C4BF6), onMarcarNinoPorNino)
                OpcionConfirmar("Dictar lo que pasó", Color(0xFFF0A500), onDictarLoQuePaso)
                OpcionConfirmar("Nota de un solo niño", Color(0xFF5B3FE0), onNotaDeUnSoloNino)
                OpcionConfirmar("Fotos de la actividad", Color(0xFF12A594), onFotosDeLaActividad)
            }

            HorizontalDivider(color = Color(0xFFF0EAFB), thickness = 1.5.dp)

            SeccionLabel("Cuando ya no falte nadie")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(26.dp),
                        ambientColor = Color.Black.copy(alpha = 0.20f),
                        spotColor = Color.Black.copy(alpha = 0.20f)
                    )
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFF12A594))
                    .clickable(onClick = onTodoBien)
                    .padding(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.24f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(textoTodoBien, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text("Cierra el momento y avisa a las familias", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.85f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
        }
    )
}

@Composable
fun OpcionConfirmar(texto: String, color: Color, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.10f),
                spotColor = Color.Black.copy(alpha = 0.10f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(texto, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140))
    }
}

@Preview(showBackground = true)
@Composable
fun ConfirmarMomentoScreenPreview() {
    CodeaTheme {
        ConfirmarMomentoScreen(
            bloqueHora = "7:30 - 8:15",
            bloqueNombre = "Desayuno",
            bloquePregunta = "¿Cómo estuvo este momento para el grupo?",
            textoTodoBien = "Todo salió bien",
            onMarcarNinoPorNino = {},
            onDictarLoQuePaso = {},
            onNotaDeUnSoloNino = {},
            onFotosDeLaActividad = {},
            onTodoBien = {}
        )
    }
}
