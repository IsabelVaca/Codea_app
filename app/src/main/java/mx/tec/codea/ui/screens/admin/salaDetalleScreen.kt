package mx.tec.codea.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.domain.Infante
import mx.tec.codea.domain.Sala
import mx.tec.codea.domain.SalaValidator
import mx.tec.codea.ui.theme.AvatarBg
import mx.tec.codea.ui.theme.AvatarFg
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.salaColor

@Composable
fun SalaAdminDetalleScreen(
    sala: Sala,
    ninos: List<Infante>,
    onQuitarInfante: (Infante) -> Unit,
    infantesDisponibles: List<Infante>,
    onAgregarInfante: (Infante) -> Unit,
    asistentes: List<String>,
    onAsistenteSeleccionada: (String) -> Unit,
    onEditarCupo: (String) -> Unit,
    onEliminar: () -> Unit,
    onBack: () -> Unit
) {
    val ocupacion = if (sala.cupoMaximo > 0) ninos.size / sala.cupoMaximo.toFloat() else 0f

    // estado solo de esta pantalla, se pierde si sales sin guardar.
    var nuevoCupo by remember(sala.id) { mutableStateOf(sala.cupoMaximo.toString()) }
    val cupoError = if (nuevoCupo.isEmpty()) null else SalaValidator.validateCupo(nuevoCupo)
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
                sala.nombre,
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(26.dp),
                        ambientColor = Color.Black.copy(alpha = 0.15f),
                        spotColor = Color.Black.copy(alpha = 0.15f)
                    )
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFFE8FBF6))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(sala.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140))
                    Text("${ninos.size}/${sala.cupoMaximo} niños", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F7A6C))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(ocupacion)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(999.dp))
                            .background(salaColor(sala.id))
                    )
                }
            }

            SeccionLabel("Información")
            InfoCard(titulo = "Asistente asignada", valor = sala.asistente)

            SeccionLabel("Editar cupo")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nuevoCupo,
                    onValueChange = { nuevoCupo = it },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = cupoError != null,
                    supportingText = {
                        if (cupoError != null) Text("El cupo debe ser un número mayor a 0")
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF4F1FB),
                        unfocusedContainerColor = Color(0xFFF4F1FB),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
                Button(
                    onClick = { onEditarCupo(nuevoCupo) },
                    enabled = cupoError == null && nuevoCupo.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF12A594)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Guardar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            SeccionLabel("Niños en esta sala")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ninos.forEach { n ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(13.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(24.dp),
                                ambientColor = Color.Black.copy(alpha = 0.10f),
                                spotColor = Color.Black.copy(alpha = 0.10f)
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .padding(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(AvatarBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(n.iniciales, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AvatarFg)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(n.nombre, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140))
                            Text("${n.edad} años · ${n.tutor}", fontSize = 12.sp, color = Color(0xFF8C7FA8))
                        }
                        IconButton(onClick = { onQuitarInfante(n) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Sacar de la sala", tint = Color(0xFFC4432A))
                        }
                    }
                }
            }

            SeccionLabel("Agregar infante a esta sala")
            if (infantesDisponibles.isEmpty()) {
                Text(
                    "No hay infantes en otras salas para agregar.",
                    fontSize = 12.sp,
                    color = Color(0xFF8C7FA8)
                )
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    infantesDisponibles.forEach { infante ->
                        Button(
                            onClick = { onAgregarInfante(infante) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF4F1FB),
                                contentColor = Color(0xFF6B6183)
                            ),
                            shape = RoundedCornerShape(999.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text(infante.nombre, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            SeccionLabel("Cambiar asistente asignada")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                asistentes.forEach { nombreAsist ->
                    val activo = sala.asistente.startsWith(nombreAsist)
                    Button(
                        onClick = { onAsistenteSeleccionada(nombreAsist) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activo) Color(0xFF12A594) else Color(0xFFF4F1FB),
                            contentColor = if (activo) Color(0xFFFFFDFA) else Color(0xFF6B6183)
                        ),
                        shape = RoundedCornerShape(999.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(nombreAsist, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            SeccionLabel("Acciones")
            AccionButton("Eliminar esta sala", Color(0xFFFFEDE7), Color(0xFFC4432A), onEliminar)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SalaAdminDetalleScreenPreview() {
    CodeaTheme {
        SalaAdminDetalleScreen(
            sala = Sala(
                id = "sala-3",
                nombre = "Preescolar 2",
                cupoMaximo = 12,
                asistente = "Paola Sánchez"
            ),
            ninos = listOf(
                Infante(
                    id = "infante-1",
                    nombre = "Mateo Iglesias",
                    iniciales = "MI",
                    edad = 4,
                    tutor = "Laura Iglesias",
                    tituloTutor = "mamá",
                    sala = "Preescolar 2"
                ),
                Infante(
                    id = "infante-4",
                    nombre = "Valentina Ruiz",
                    iniciales = "VR",
                    edad = 4,
                    tutor = "Sofía Ruiz",
                    tituloTutor = "tía",
                    sala = "Preescolar 2"
                )
            ),
            infantesDisponibles = listOf(
                Infante(
                    id = "infante-2",
                    nombre = "Renata Gómez",
                    iniciales = "RG",
                    edad = 3,
                    tutor = "Carlos Gómez",
                    tituloTutor = "papá",
                    sala = "Maternal"
                )
            ),
            onQuitarInfante = {},
            onAgregarInfante = {},
            asistentes = listOf("Paola Sánchez", "Luis Medina"),
            onAsistenteSeleccionada = {},
            onEditarCupo = {},
            onEliminar = {},
            onBack = {}
        )
    }
}
