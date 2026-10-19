package mx.tec.codea.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.domain.MomentoCatalogo
import mx.tec.codea.ui.theme.Poppins
import mx.tec.codea.ui.components.BackHeader

@Composable
fun CatalogoMomentosScreen(
    catalogo: List<MomentoCatalogo>,
    seleccionados: List<String>,
    onAlternar: (String) -> Unit,
    creando: Boolean,
    onAbrirCrear: () -> Unit,
    nombrePropio: String, onNombrePropioChange: (String) -> Unit,
    registroPropio: String, onRegistroPropioChange: (String) -> Unit,
    onGuardarPropio: () -> Unit,
    onContinuar: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textoBoton = if (seleccionados.isNotEmpty()) "Continuar con ${seleccionados.size} momentos" else "Elige momentos"

    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BackHeader(overline = "Ajustes · Rutinas", title = "Nueva rutina", onBackClick = onBack)
            SeccionLabel("PASO 2 DE 3")
            Text(
                "Toca para incluir. Cada momento trae su registro rápido.",
                fontFamily = Poppins, fontSize = 13.sp, lineHeight = 19.sp, color = Color(0xFF8C7FA8)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                catalogo.forEach { m ->
                    val on = m.nombre in seleccionados
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(if (on) Color(0xFFEFE9FF) else Color(0xFFFBF8FF))
                            .border(2.dp, if (on) Color(0xFFD9CEFF) else Color.Transparent, RoundedCornerShape(22.dp))
                            .clickable { onAlternar(m.nombre) }
                            .padding(horizontal = 16.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(m.nombre, fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140))
                            Text(m.registro, fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF8C7FA8))
                        }
                        CheckCirculo(on)
                    }
                }
            }

            if (creando) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFFFBF8FF))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CampoTexto(nombrePropio, onNombrePropioChange, "Nombre del momento")
                    CampoTexto(registroPropio, onRegistroPropioChange, "Qué se registra (ej. Foto + nota)")
                    Button(
                        onClick = onGuardarPropio,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFE9FF), contentColor = Color(0xFF5B3FE0)),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        elevation = null
                    ) {
                        Text("Agregar al catálogo", fontFamily = Poppins, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            } else {
                BotonPunteado("+ Crear momento propio", onAbrirCrear)
            }
        }
        Spacer(Modifier.height(14.dp))
        BotonPrincipal(textoBoton, Color(0xFF6C4BF6), onContinuar)
    }
}

@Composable
private fun CheckCirculo(on: Boolean) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .then(
                if (on) Modifier.background(Color(0xFF6C4BF6))
                else Modifier.border(2.dp, Color(0xFFD8D0E8), CircleShape)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (on) Text("✓", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
    }
}

@Composable
private fun CampoTexto(value: String, onChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text(placeholder, fontFamily = Poppins) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF4F1FB),
            unfocusedContainerColor = Color(0xFFF4F1FB),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = Color(0xFF2A2140),
            unfocusedTextColor = Color(0xFF2A2140)
        ),
        textStyle = LocalTextStyle.current.copy(fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
        singleLine = true
    )
}
