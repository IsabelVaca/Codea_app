package mx.tec.codea.ui.screens.admin

import mx.tec.codea.ui.components.CodeaColores
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.tec.codea.ui.theme.Poppins
import mx.tec.codea.ui.components.BackHeader

@Composable
fun NuevaRutinaScreen(
    nombre: String, onNombreChange: (String) -> Unit,
    etapa: String, onElegirEtapa: (String) -> Unit,
    onElegirMomentos: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BackHeader(title = "Nueva rutina", onBackClick = onBack)
            SeccionLabel("PASO 1 DE 3")
            SeccionLabel("NOMBRE")
            CampoTexto(nombre, onNombreChange, "Ej. Tarde tranquila")
            SeccionLabel("ETAPA")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Maternal", "Lactantes", "Preescolar").forEach { e ->
                    ChipBoton(e, etapa == e, CodeaColores.VioletaMarca) { onElegirEtapa(e) }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        BotonPrincipal("Elegir momentos", CodeaColores.VioletaMarca, onElegirMomentos)
    }
}

@Composable
private fun ChipBoton(nombre: String, activo: Boolean, colorActivo: Color, onClick: () -> Unit) {
    val bg = if (activo) colorActivo else CodeaColores.Campo
    val fg = if (activo) Color.White else CodeaColores.GrisOscuro
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(nombre, fontFamily = Poppins, fontWeight = FontWeight.Bold)
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
            focusedContainerColor = CodeaColores.Campo,
            unfocusedContainerColor = CodeaColores.Campo,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = CodeaColores.Ink,
            unfocusedTextColor = CodeaColores.Ink
        ),
        textStyle = LocalTextStyle.current.copy(fontFamily = Poppins, fontWeight = FontWeight.SemiBold),
        singleLine = true
    )
}
