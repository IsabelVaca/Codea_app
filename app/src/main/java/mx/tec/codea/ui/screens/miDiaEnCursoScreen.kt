package mx.tec.codea.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import mx.tec.codea.ui.theme.CodeaTheme

data class BloqueActivoUi(
    val tituloAhora: String,
    val nombre: String,
    val nota: String,
    val notaRapidaTexto: String?,
    val chipCurso: String,
    val chipCursoBg: Color,
    val chipCursoFg: Color,
    val enCurso: Boolean,
    val textoAccionAhora: String,
    val onAccionAhora: () -> Unit,
    val onNotaRapida: () -> Unit,
    val onFotoDelMomento: () -> Unit
)

data class BloqueUi(
    val ordinal: String,
    val ordinalColor: Color,
    val nombre: String,
    val estado: String,
    val color: Color,
    val forma: String,
    val fondo: Color,
    val tituloColor: Color,
    val onAbrir: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiDiaEnCursoScreen(
    rutinaActual: String,
    progresoTexto: String,
    progreso: Float, // 0f..1f
    bloqueActivo: BloqueActivoUi?,
    bloques: List<BloqueUi>,
    onCambiarRutina: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mi día") })
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
            // Encabezado con progreso
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFFEFE9FF))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(rutinaActual, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2A2140))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFFFFDFA))
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(progresoTexto, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5B3FE0))
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFFFFDFA))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progreso)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFF6C4BF6))
                    )
                }
            }

            // Momento activo
            if (bloqueActivo != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(26.dp),
                            ambientColor = Color.Black.copy(alpha = 0.25f),
                            spotColor = Color.Black.copy(alpha = 0.25f)
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color(0xFF2A2140))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            bloqueActivo.tituloAhora.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp, color = Color(0xFFBDB0DC)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(bloqueActivo.chipCursoBg)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(bloqueActivo.chipCurso, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = bloqueActivo.chipCursoFg)
                        }
                    }
                    Text(bloqueActivo.nombre, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text(bloqueActivo.nota, fontSize = 13.sp, lineHeight = 19.sp, color = Color(0xFFCFC5E6))
                    if (bloqueActivo.notaRapidaTexto != null) {
                        Text(bloqueActivo.notaRapidaTexto, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFC53D))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                        Button(
                            onClick = bloqueActivo.onAccionAhora,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C4BF6), contentColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text(bloqueActivo.textoAccionAhora, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        if (bloqueActivo.enCurso) {
                            Button(
                                onClick = bloqueActivo.onNotaRapida,
                                modifier = Modifier.height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.14f), contentColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) {
                                Text("Nota de un niño", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = bloqueActivo.onFotoDelMomento,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.14f))
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = "Foto del momento", tint = Color(0xFFFFC53D))
                            }
                        }
                    }
                }
            }

            SeccionLabel("Rutina de hoy")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                bloques.forEach { b ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(b.fondo)
                            .clickable(onClick = b.onAbrir)
                            .padding(14.dp)
                    ) {
                        Text(b.ordinal, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = b.ordinalColor)
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(if (b.forma == "50%") CircleShape else RoundedCornerShape(4.dp))
                                .background(b.color)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(b.nombre, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = b.tituloColor)
                            Text(b.estado, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFA79BBF))
                        }
                    }
                }
            }

            Button(
                onClick = onCambiarRutina,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4F1FB), contentColor = Color(0xFF6B6183)),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text("Cambiar la rutina de hoy", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun MiDiaEnCursoScreenPreview() {
    CodeaTheme {
        MiDiaEnCursoScreen(
            rutinaActual = "Rutina Mañana",
            progresoTexto = "2/5 momentos",
            progreso = 0.4f,
            bloqueActivo = BloqueActivoUi(
                tituloAhora = "Ahora",
                nombre = "Desayuno",
                nota = "Ayuda a los niños a lavarse las manos antes de sentarse.",
                notaRapidaTexto = "Recuerda tomar fotos del momento",
                chipCurso = "En curso",
                chipCursoBg = Color(0xFF12A594),
                chipCursoFg = Color.White,
                enCurso = true,
                textoAccionAhora = "Terminar momento",
                onAccionAhora = {},
                onNotaRapida = {},
                onFotoDelMomento = {}
            ),
            bloques = listOf(
                BloqueUi(
                    ordinal = "1",
                    ordinalColor = Color(0xFFC4BAD8),
                    nombre = "Bienvenida",
                    estado = "Completado",
                    color = Color(0xFF6C4BF6),
                    forma = "50%",
                    fondo = Color(0xFFF7F5FB),
                    tituloColor = Color(0xFFA79BBF),
                    onAbrir = {}
                ),
                BloqueUi(
                    ordinal = "2",
                    ordinalColor = Color(0xFF6C4BF6),
                    nombre = "Desayuno",
                    estado = "En curso",
                    color = Color(0xFF12A594),
                    forma = "50%",
                    fondo = Color.White,
                    tituloColor = Color(0xFF2A2140),
                    onAbrir = {}
                ),
                BloqueUi(
                    ordinal = "3",
                    ordinalColor = Color(0xFFC4BAD8),
                    nombre = "Actividad libre",
                    estado = "Pendiente",
                    color = Color(0xFFFFC53D),
                    forma = "16px",
                    fondo = Color(0xFFF7F5FB),
                    tituloColor = Color(0xFFA79BBF),
                    onAbrir = {}
                )
            ),
            onCambiarRutina = {}
        )
    }
}
