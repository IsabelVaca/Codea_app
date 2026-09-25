package mx.tec.codea.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.ui.theme.CodeaTheme

data class MomentoConfigUi(
    val nombre: String,
    val nota: String,
    val activo: Boolean,
    val onAlternar: () -> Unit
)

@Composable
fun AjustarRutinaScreen(
    tituloRutina: String,
    subtitulo: String,
    momentos: List<MomentoConfigUi>,
    resumen: String,
    onAgregarMomento: () -> Unit,
    onComenzarDia: () -> Unit,
    textoComenzar: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDFA))
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFEFE9FF))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(tituloRutina, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4A3A80))
                Text(subtitulo, fontSize = 13.sp, lineHeight = 19.sp, color = Color(0xFF5B4B8A))
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                momentos.forEachIndexed { index, m ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (m.activo) Color.White else Color(0xFFF7F5FB))
                            .padding(14.dp)
                    ) {
                        // Handle de arrastre — usa una librería de reorder (p. ej. reorderable) para drag real
                        Column(
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .pointerInput(index) { detectDragGestures { _, _ -> /* reordenar */ } },
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            repeat(3) {
                                Box(
                                    modifier = Modifier
                                        .width(13.dp)
                                        .height(2.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFFC4BAD8))
                                )
                            }
                        }
                        Text(
                            "${index + 1}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (m.activo) Color(0xFF6C4BF6) else Color(0xFFC4BAD8)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                m.nombre, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                                color = if (m.activo) Color(0xFF2A2140) else Color(0xFFA79BBF),
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                m.nota, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFA79BBF),
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                        Switch(
                            checked = m.activo,
                            onCheckedChange = { m.onAlternar() },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = Color(0xFF6C4BF6),
                                uncheckedTrackColor = Color(0xFFE2DCF0)
                            )
                        )
                    }
                }
            }

            Text(
                resumen, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8C7FA8),
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = onAgregarMomento,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, Color(0xFFD8D0E8)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6B6183))
            ) {
                Text("+ Agregar un momento de hoy", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onComenzarDia,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C4BF6), contentColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(textoComenzar, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AjustarRutinaScreenPreview() {
    CodeaTheme {
        AjustarRutinaScreen(
            tituloRutina = "Rutina Mañana",
            subtitulo = "Bienvenida · Desayuno · Actividad libre",
            momentos = listOf(
                MomentoConfigUi("Bienvenida", "7:00 - 7:30", activo = true, onAlternar = {}),
                MomentoConfigUi("Desayuno", "7:30 - 8:15", activo = true, onAlternar = {}),
                MomentoConfigUi("Actividad libre", "8:15 - 9:00", activo = false, onAlternar = {})
            ),
            resumen = "2 de 3 momentos activos",
            onAgregarMomento = {},
            onComenzarDia = {},
            textoComenzar = "Comenzar el día"
        )
    }
}
