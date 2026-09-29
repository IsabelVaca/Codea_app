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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.domain.InfanteError
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Poppins

data class SalaOpcion(val nombre: String, val activo: Boolean)

@Composable
fun InscribirInfanteScreen(
    nombre: String, onNombreChange: (String) -> Unit,
    nombreError: InfanteError?,
    edad: String, onEdadChange: (String) -> Unit,
    edadError: InfanteError?,
    alergias: String, onAlergiasChange: (String) -> Unit,
    tutor: String, onTutorChange: (String) -> Unit,
    tutorError: InfanteError?,
    tituloTutor: String, onElegirTituloTutor: (String) -> Unit,
    telefono: String, onTelefonoChange: (String) -> Unit,
    telefonoError: InfanteError?,
    salas: List<SalaOpcion>,
    onElegirSala: (String) -> Unit,
    canGuardar: Boolean,
    onGuardar: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    val textoGuardar = if (nombre.isNotBlank()) "Inscribir a ${nombre.trim().split(" ")[0]}" else "Inscribir al niño"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Flecha para regresar a "Infantes"
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
                text = "Inscribir a un infante",
                fontFamily = Poppins,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2A2140),
                modifier = Modifier.padding(start = 4.dp)
            )
        }


        // Encabezado
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFEFE9FF))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Text("Nueva inscripción", fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4A3A80))
            Text(
                "Completa los datos del niño y su familia. Al guardar, queda inscrito y aparece en su sala.",
                fontFamily = Poppins, fontSize = 13.sp, lineHeight = 19.sp, color = Color(0xFF5B4B8A)
            )
        }

        SeccionLabel("DATOS DEL NIÑO")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CampoTexto(nombre, onNombreChange, "Nombre completo", error = nombreError)
            CampoTexto(edad, onEdadChange, "Edad en años (ej. 3)", KeyboardType.Number, error = edadError)
            CampoTexto(alergias, onAlergiasChange, "Alergias o notas médicas (opcional)")
        }

        SeccionLabel("TUTOR RESPONSABLE")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CampoTexto(tutor, onTutorChange, "Nombre del tutor o tutora", error = tutorError)
            CampoTexto(telefono, onTelefonoChange, "Teléfono de contacto", KeyboardType.Phone, error = telefonoError)
        }

        SeccionLabel("PARENTESCO")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Mamá", "Papá", "Tutor legal").forEach { t ->
                val bg = if (tituloTutor == t) Color(0xFF6C4BF6) else Color(0xFFF4F1FB)
                val fg = if (tituloTutor == t) Color.White else Color(0xFF6B6183)
                Button(
                    onClick = { onElegirTituloTutor(t) },
                    colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
                    shape = RoundedCornerShape(999.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 11.dp)
                ) {
                    Text(t, fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        SeccionLabel("SALA ASIGNADA")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            salas.forEach { o ->
                val bg = if (o.activo) Color(0xFF6C4BF6) else Color(0xFFF4F1FB)
                val fg = if (o.activo) Color.White else Color(0xFF6B6183)
                Button(
                    onClick = { onElegirSala(o.nombre) },
                    colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
                    shape = RoundedCornerShape(999.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 11.dp)
                ) {
                    Text(o.nombre, fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Button(
            onClick = onGuardar,
            enabled = canGuardar,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C4BF6)),
            contentPadding = PaddingValues(vertical = 17.dp)
        ) {
            Text(textoGuardar, fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
        Text(
            "Se crea su expediente y el acceso de su familia a la app.",
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
    error: InfanteError? = null
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

private fun InfanteError.message(): String = when (this) {
    InfanteError.NombreVacio -> "Escribe el nombre completo del niño"
    InfanteError.EdadInvalida -> "La edad debe ser un número entre 0 y 12"
    InfanteError.TutorVacio -> "Escribe el nombre del tutor"
    InfanteError.TelefonoInvalido -> "El teléfono debe tener al menos 10 dígitos"
}

@Preview(showBackground = true)
@Composable
private fun InscribirInfanteScreenPreview() {
    CodeaTheme {
        InscribirInfanteScreen(
            nombre = "", onNombreChange = {},
            nombreError = null,
            edad = "", onEdadChange = {},
            edadError = null,
            alergias = "", onAlergiasChange = {},
            tutor = "", onTutorChange = {},
            tutorError = null,
            tituloTutor = "Mamá", onElegirTituloTutor = {},
            telefono = "", onTelefonoChange = {},
            telefonoError = null,
            salas = listOf(
                SalaOpcion("Maternal", activo = true),
                SalaOpcion("Preescolar 1", activo = false),
                SalaOpcion("Preescolar 2", activo = false),
            ),
            onElegirSala = {},
            canGuardar = true,
            onGuardar = {},
            onBack = {},
        )
    }
}
