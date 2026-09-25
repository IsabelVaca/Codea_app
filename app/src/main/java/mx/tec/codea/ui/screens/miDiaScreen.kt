package mx.tec.codea.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.ui.theme.CodeaTheme

data class RutinaUi(
    val nombre: String,
    val resumen: String,
    val chips: List<String>,
    val color: Color,
    val forma: String, // "50%" o "16px"
    val seleccionada: Boolean,
    val marca: String?,
    val onElegir: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiDiaScreen(
    saludo: String = "Buen día, miss Karla",
    rutinas: List<RutinaUi>,
    onAjustarYComenzar: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Rutina de hoy") })
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
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFFFFEDE7))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(saludo, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2A2140))
                Text(
                    "Elige la rutina de hoy y la app te va acompañando. Tú solo confirmas cuando cada momento termina.",
                    fontSize = 14.sp, lineHeight = 21.sp, color = Color(0xFF9A6A5C)
                )
            }

            SeccionLabel("Rutinas precargadas")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rutinas.forEach { r ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color.White)
                            .border(
                                width = if (r.seleccionada) 2.5.dp else 0.dp,
                                color = if (r.seleccionada) Color(0xFF6C4BF6) else Color.Transparent,
                                shape = RoundedCornerShape(26.dp)
                            )
                            .clickable(onClick = r.onElegir)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(if (r.forma == "50%") CircleShape else RoundedCornerShape(4.dp))
                                    .background(r.color)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(r.nombre, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140))
                                Text(r.resumen, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B6183))
                            }
                            if (r.marca != null) {
                                Text(r.marca, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6C4BF6))
                            }
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            r.chips.forEach { chip ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Color(0xFFF4F1FB))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(chip, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B6183))
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onAjustarYComenzar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C4BF6), contentColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text("Ajustar y comenzar el día", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun MiDiaScreenPreview() {
    CodeaTheme {
        MiDiaScreen(
            rutinas = listOf(
                RutinaUi(
                    nombre = "Rutina Mañana",
                    resumen = "Bienvenida · Desayuno · Actividad libre",
                    chips = listOf("7:00 - 9:00", "Interior"),
                    color = Color(0xFF6C4BF6),
                    forma = "50%",
                    seleccionada = true,
                    marca = "Sugerida",
                    onElegir = {}
                ),
                RutinaUi(
                    nombre = "Rutina Exterior",
                    resumen = "Juego libre · Snack · Cuentos",
                    chips = listOf("9:00 - 11:00", "Patio"),
                    color = Color(0xFF12A594),
                    forma = "16px",
                    seleccionada = false,
                    marca = null,
                    onElegir = {}
                )
            ),
            onAjustarYComenzar = {}
        )
    }
}
