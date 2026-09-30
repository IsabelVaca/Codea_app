package mx.tec.codea.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Poppins
import mx.tec.codea.domain.DocenteError

data class OpcionChip(val nombre: String, val activo: Boolean)

@Composable
fun AltaAsistenteScreen(
    nombre: String, onNombreChange: (String) -> Unit,
    nombreError: DocenteError?,
    telefono: String, onTelefonoChange: (String) -> Unit,
    telefonoError: DocenteError?,
    correo: String, onCorreoChange: (String) -> Unit,
    correoError: DocenteError?,
    salas: List<OpcionChip>, onElegirSala: (String) -> Unit,
    turno: String, onElegirTurno: (String) -> Unit,
    canGuardar: Boolean,
    onGuardar: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textoGuardar = if (nombre.isNotBlank()) "Dar de alta a ${nombre.trim().split(" ")[0]}" else "Dar de alta a la asistente"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Flecha para regresar y título
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFF4F1FB))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color(0xFF2A2140))
            }
            Text(
                text = "Dar de alta asistente",
                fontFamily = Poppins,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2A2140),
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFFFEDE7))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Nueva asistente", fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9A4A32))
            Text(
                "Da de alta su acceso a la app: sala y turno.",
                fontFamily = Poppins, fontSize = 13.sp, lineHeight = 19.sp, color = Color(0xFF9A6A5C)
            )
        }

        SeccionLabel("DATOS PERSONALES")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CampoTexto(nombre, onNombreChange, "Nombre completo", error = nombreError)
            CampoTexto(telefono, onTelefonoChange, "Teléfono de contacto", KeyboardType.Phone, error = telefonoError)
            CampoTexto(correo, onCorreoChange, "Correo electrónico", KeyboardType.Email, error = correoError)
        }

        SeccionLabel("SALA ASIGNADA")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            salas.forEach { o ->
                ChipBoton(o.nombre, o.activo, Color(0xFFFF7A5A)) { onElegirSala(o.nombre) }
            }
        }

        SeccionLabel("TURNO")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Matutino", "Vespertino").forEach { t ->
                ChipBoton(t, turno == t, Color(0xFFFF7A5A)) { onElegirTurno(t) }
            }
        }

        Button(
            onClick = onGuardar,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            enabled = canGuardar,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A5A)),
            contentPadding = PaddingValues(vertical = 17.dp)
        ) {
            Text(textoGuardar, fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }

    }
}

@Composable
private fun ChipBoton(nombre: String, activo: Boolean, colorActivo: Color, onClick: () -> Unit) {
    val bg = if (activo) colorActivo else Color(0xFFF4F1FB)
    val fg = if (activo) Color.White else Color(0xFF6B6183)
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
        shape = RoundedCornerShape(999.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 11.dp)
    ) {
        Text(nombre, fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CampoTexto(
    value: String, onChange: (String) -> Unit, placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: DocenteError? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text(placeholder, fontFamily = Poppins) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = error != null,
        supportingText = {
            error?.let { Text(it.message()) }
        },
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

@Preview(showBackground = true)
@Composable
private fun AltaAsistenteScreenPreview() {
    CodeaTheme {
        AltaAsistenteScreen(
            nombre = "", onNombreChange = {},
            telefono = "", onTelefonoChange = {},
            correo = "", onCorreoChange = {},
            salas = listOf(
                OpcionChip("Maternal", activo = true),
                OpcionChip("Preescolar 1", activo = false),
                OpcionChip("Preescolar 2", activo = false),
            ),
            onElegirSala = {},
            turno = "Matutino",
            onElegirTurno = {},
            onGuardar = {},
            onBack = {},
            nombreError = null,
            telefonoError = null,
            correoError = null,
            canGuardar = true
        )
    }
}

private fun DocenteError.message(): String = when (this) {
    DocenteError.NombreVacio -> "Escribe el nombre completo"
    DocenteError.TelefonoInvalido -> "El teléfono debe tener al menos 10 dígitos"
    DocenteError.CorreoInvalido -> "Escribe un correo válido"
}
