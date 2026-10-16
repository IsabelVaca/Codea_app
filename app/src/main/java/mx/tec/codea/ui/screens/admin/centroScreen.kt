package mx.tec.codea.ui.screens.admin

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.data.InfanteRepository
import mx.tec.codea.data.SalaRepository
import mx.tec.codea.domain.Aviso
import mx.tec.codea.domain.Infante
import mx.tec.codea.domain.Sala
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Poppins
import mx.tec.codea.ui.theme.salaColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CentroScreen(
    salas: List<Sala>,
    infantes: List<Infante>,
    onSalaClick: (Sala) -> Unit = {},
    avisoTexto: String,
    onAvisoTextoChange: (String) -> Unit,
    onPublicarAviso: () -> Unit,
    onCrearSala: () -> Unit,
    canPublicarAviso: Boolean,
    destinoSalaId: String? = null,           // null = todas las salas
    onDestinoChange: (String?) -> Unit = {},
    avisos: List<Aviso> = emptyList(),       // el historial, el más reciente primero
) {
    // si la sala elegida deja de existir, el aviso vuelve a ser para todas.
    val destino = salas.firstOrNull { it.id == destinoSalaId }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Centro", fontWeight = FontWeight.Bold) })
        },
        content = { paddingValues ->
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .background(Color(0xFFFFFDFA))
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SeccionLabel("Ocupación de salas")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                salas.forEach { sala ->
                    val ninosActuales = infantes.count { it.sala == sala.nombre }
                    val ocupacion = if (sala.cupoMaximo > 0) ninosActuales / sala.cupoMaximo.toFloat() else 0f
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
                            .clickable { onSalaClick(sala) }
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
            }

            CrearSalaButton(
                onClick = onCrearSala,
                modifier = Modifier.padding(top = 2.dp)
            )

            SeccionLabel("Publicar aviso del centro")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DestinoDropdown(
                    salas = salas,
                    seleccionId = destino?.id,
                    onSeleccion = onDestinoChange
                )
                CampoAviso(
                    valor = avisoTexto,
                    onCambio = onAvisoTextoChange,
                    placeholder = if (destino == null) "Escribe el aviso para todas las salas…"
                    else "Escribe el aviso para ${destino.nombre}…"
                )
                Button(
                    onClick = onPublicarAviso,
                    enabled = canPublicarAviso,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFE9FF), contentColor = Color(0xFF5B3FE0)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Publicar aviso", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            SeccionLabel("Historial de avisos")
            if (avisos.isEmpty()) {
                Text(
                    "Todavía no se ha publicado ningún aviso.",
                    fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CentroColores.Gris
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                avisos.forEach { aviso -> AvisoCard(aviso) }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
        }
    )
}

private object CentroColores {
    val Ink = Color(0xFF2A2140)
    val Violeta = Color(0xFF5B3FE0)
    val VioletaSuave = Color(0xFFEFE9FF)
    val Campo = Color(0xFFF4F1FB)
    val Gris = Color(0xFFA79BBF)
    val Verde = Color(0xFF0F7A6C)
    val VerdeSuave = Color(0xFFE8FBF6)
}

// elige a quién va el aviso: a todas las salas o solo a una.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DestinoDropdown(
    salas: List<Sala>,
    seleccionId: String?,
    onSeleccion: (String?) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    val etiqueta = salas.firstOrNull { it.id == seleccionId }?.let { "Solo ${it.nombre}" }
        ?: "Para todos · todas las salas"

    ExposedDropdownMenuBox(expanded = abierto, onExpandedChange = { abierto = it }) {
        Row(
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CentroColores.Campo)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(etiqueta, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CentroColores.Ink)
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = CentroColores.Violeta)
        }
        ExposedDropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false },
            modifier = Modifier.background(Color.White)
        ) {
            DropdownMenuItem(
                text = { Text("Para todos · todas las salas", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CentroColores.Ink) },
                onClick = { onSeleccion(null); abierto = false }
            )
            salas.forEach { sala ->
                DropdownMenuItem(
                    text = { Text("Solo ${sala.nombre}", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CentroColores.Ink) },
                    onClick = { onSeleccion(sala.id); abierto = false }
                )
            }
        }
    }
}

@Composable
private fun CampoAviso(valor: String, onCambio: (String) -> Unit, placeholder: String) {
    BasicTextField(
        value = valor,
        onValueChange = onCambio,
        textStyle = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = CentroColores.Ink),
        cursorBrush = SolidColor(CentroColores.Violeta),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { interno ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 84.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(CentroColores.Campo)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                if (valor.isEmpty()) {
                    Text(placeholder, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = CentroColores.Gris)
                }
                interno()
            }
        }
    )
}

// una fila del historial: etiqueta de destino + fecha, y el texto del aviso.
@Composable
private fun AvisoCard(aviso: Aviso) {
    val paraTodos = aviso.destino == null
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(22.dp), ambientColor = Color.Black.copy(alpha = 0.12f), spotColor = Color.Black.copy(alpha = 0.12f))
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                aviso.destino ?: "Todas las salas",
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (paraTodos) CentroColores.VioletaSuave else CentroColores.VerdeSuave)
                    .padding(horizontal = 11.dp, vertical = 5.dp),
                fontFamily = Poppins, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp,
                color = if (paraTodos) CentroColores.Violeta else CentroColores.Verde
            )
            Text(aviso.fecha, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = CentroColores.Gris)
        }
        Text(
            aviso.texto, fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp, lineHeight = 21.sp, color = CentroColores.Ink
        )
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

@Preview(showBackground = true)
@Composable
fun CentroScreenPreview() {
    CodeaTheme {
        CentroScreen(
            salas = SalaRepository().getAll(),
            infantes = InfanteRepository().getAll(),
            avisoTexto = "",
            onAvisoTextoChange = {},
            onPublicarAviso = {},
            onCrearSala = {},
            canPublicarAviso = true,
            avisos = listOf(
                Aviso("2", "Maternal", "16 oct · 09:30", "Mañana no hay servicio."),
                Aviso("1", null, "15 oct · 14:00", "Junta de padres el viernes."),
            )
        )
    }
}
