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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.domain.PadreError
import mx.tec.codea.ui.components.PersonasMultiDropdown
import mx.tec.codea.ui.theme.Poppins
import mx.tec.codea.ui.components.BackHeader

@Composable
fun InscribirPadreScreen(
    nombre: String, onNombreChange: (String) -> Unit,
    nombreError: PadreError?,
    apellido: String = "", onApellidoChange: (String) -> Unit = {},
    telefono: String, onTelefonoChange: (String) -> Unit,
    telefonoError: PadreError?,
    correo: String, onCorreoChange: (String) -> Unit,
    correoError: PadreError?,
    parentesco: String?, onElegirParentesco: (String) -> Unit,
    hijosDisponibles: List<Pair<String, String>>,
    hijosSeleccionados: List<String>,
    onToggleHijo: (String) -> Unit,
    canGuardar: Boolean,
    onGuardar: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textoGuardar = if (nombre.isNotBlank()) "Inscribir a ${nombre.trim().split(" ")[0]}" else "Inscribir al padre o tutor"
    val morado = Color(0xFF6C4BF6)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BackHeader(
            title = "Inscribir a un padre",
            onBackClick = onBack,
        )

        // Encabezado
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFEFE9FF))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Nuevo padre o tutor", fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4A3A80))
            Text(
                "Registra sus datos y vincúlalo con sus hijos. Al guardar, recibe su acceso a la app.",
                fontFamily = Poppins, fontSize = 13.sp, lineHeight = 19.sp, color = Color(0xFF5B4B8A)
            )
        }

        SeccionLabel("DATOS PERSONALES")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CampoTexto(nombre, onNombreChange, "Nombre(s)", error = nombreError)
            CampoTexto(apellido, onApellidoChange, "Apellido(s)")
            CampoTexto(telefono, onTelefonoChange, "Teléfono de contacto", KeyboardType.Phone, error = telefonoError)
            CampoTexto(correo, onCorreoChange, "Correo electrónico", KeyboardType.Email, error = correoError)
        }

        SeccionLabel("PARENTESCO")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Mamá", "Papá", "Tutor", "Tutora").forEach { p ->
                ChipBoton(p, parentesco == p, morado) { onElegirParentesco(p) }
            }
        }

        SeccionLabel("HIJOS VINCULADOS")
        PersonasMultiDropdown(
            opciones = hijosDisponibles,
            seleccionados = hijosSeleccionados,
            onToggle = onToggleHijo,
            placeholder = "Elige uno o más hijos"
        )

        Button(
            onClick = onGuardar,
            enabled = canGuardar,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = morado),
            contentPadding = PaddingValues(vertical = 17.dp)
        ) {
            Text(textoGuardar, fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
        Text(
            "Recibe una invitación por SMS para entrar a la app.",
            fontFamily = Poppins, fontSize = 12.sp, lineHeight = 18.sp,
            color = Color(0xFF8C7FA8), textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CampoTexto(
    value: String, onChange: (String) -> Unit, placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: PadreError? = null
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

private fun PadreError.message(): String = when (this) {
    PadreError.NombreVacio -> "Escribe el nombre del padre o tutor"
    PadreError.TelefonoInvalido -> "El teléfono debe tener al menos 10 dígitos"
    PadreError.CorreoInvalido -> "Escribe un correo electrónico válido"
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
