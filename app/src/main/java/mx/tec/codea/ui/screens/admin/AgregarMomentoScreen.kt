package mx.tec.codea.ui.screens.admin

import mx.tec.codea.ui.components.CodeaColores
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
fun AgregarMomentoScreen(
    catalogo: List<MomentoCatalogo>,          // solo los que la rutina aún no tiene
    elegido: String?,
    onElegir: (String?) -> Unit,
    nombrePropio: String, onNombrePropioChange: (String) -> Unit,
    registroPropio: String, onRegistroPropioChange: (String) -> Unit,
    onAgregar: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textoBoton = when {
        nombrePropio.isNotBlank() -> "Agregar “${nombrePropio.trim()}”"
        elegido != null -> "Agregar $elegido"
        else -> "Agregar a la rutina"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BackHeader(title = "Agregar momento", onBackClick = onBack)
        SeccionLabel("DEL CATÁLOGO")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            catalogo.forEach { m ->
                val on = elegido == m.nombre && nombrePropio.isBlank()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (on) CodeaColores.Lavanda else CodeaColores.CampoSuave)
                        .border(2.dp, if (on) CodeaColores.BordeLavanda else Color.Transparent, RoundedCornerShape(22.dp))
                        .clickable { onElegir(if (on) null else m.nombre) }
                        .padding(horizontal = 16.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(m.nombre, fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CodeaColores.Ink)
                        Text(m.registro, fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.GrisMedio)
                    }
                    CheckCirculo(on)
                }
            }
        }

        SeccionLabel("O CREA UNO PROPIO")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CampoTexto(nombrePropio, { onNombrePropioChange(it); onElegir(null) }, "Nombre del momento")
            CampoTexto(registroPropio, onRegistroPropioChange, "Qué se registra (ej. Foto + nota)")
        }

        Button(
            onClick = onAgregar,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CodeaColores.VioletaMarca),
            contentPadding = PaddingValues(vertical = 18.dp)
        ) {
            Text(textoBoton, fontFamily = Poppins, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
    }
}

@Composable
private fun CheckCirculo(on: Boolean) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .then(
                if (on) Modifier.background(CodeaColores.VioletaMarca)
                else Modifier.border(2.dp, CodeaColores.BordeFuerte, CircleShape)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (on) Text("✓", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
    }
}

@Composable
private fun CampoTexto(
    value: String, onChange: (String) -> Unit, placeholder: String,
) {
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
        textStyle = LocalTextStyle.current.copy(fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
        singleLine = true
    )
}
