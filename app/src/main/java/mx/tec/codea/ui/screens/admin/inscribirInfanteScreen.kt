package mx.tec.codea.ui.screens.admin

import mx.tec.codea.ui.components.CodeaColores
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
import mx.tec.codea.ui.components.BackHeader

data class SalaOpcion(val nombre: String, val activo: Boolean)

@Composable
fun InscribirInfanteScreen(
    nombre: String, onNombreChange: (String) -> Unit,
    nombreError: InfanteError?,
    apellido: String = "", onApellidoChange: (String) -> Unit = {},
    nacimiento: String, onNacimientoChange: (String) -> Unit,
    nacimientoError: InfanteError?,
    alergias: String, onAlergiasChange: (String) -> Unit,
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
        BackHeader(
            title = "Inscribir a un infante",
            onBackClick = onBack,
        )


        // Encabezado
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CodeaColores.Lavanda)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Text("Nueva inscripción", fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CodeaColores.InkMorado)
            Text(
                "Completa los datos del niño. Al guardar, queda inscrito y aparece en su sala.",
                fontFamily = Poppins, fontSize = 13.sp, lineHeight = 19.sp, color = CodeaColores.MoradoGris
            )
        }

        SeccionLabel("DATOS DEL NIÑO")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CampoTexto(nombre, onNombreChange, "Nombre(s)", error = nombreError)
            CampoTexto(apellido, onApellidoChange, "Apellido(s) (opcional)")
            CampoTexto(nacimiento, onNacimientoChange, "Fecha de nacimiento (dd/mm/aaaa)", KeyboardType.Text, error = nacimientoError)
            CampoTexto(alergias, onAlergiasChange, "Alergias o notas médicas (opcional)")
        }

        SeccionLabel("SALA ASIGNADA")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            salas.forEach { o ->
                val bg = if (o.activo) CodeaColores.VioletaMarca else CodeaColores.Campo
                val fg = if (o.activo) Color.White else CodeaColores.GrisOscuro
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
            colors = ButtonDefaults.buttonColors(containerColor = CodeaColores.VioletaMarca),
            contentPadding = PaddingValues(vertical = 17.dp)
        ) {
            Text(textoGuardar, fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
        Text(
            "Se crea su expediente y queda listo para vincular a su padre o tutor.",
            fontFamily = Poppins, fontSize = 12.sp, lineHeight = 18.sp,
            color = CodeaColores.GrisMedio, textAlign = TextAlign.Center,
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

private fun InfanteError.message(): String = when (this) {
    InfanteError.NombreVacio -> "Escribe el nombre del niño"
    InfanteError.NacimientoInvalido -> "Escribe una fecha real como 14/03/2022 (de 0 a 12 años)"
}

@Preview(showBackground = true)
@Composable
private fun InscribirInfanteScreenPreview() {
    CodeaTheme {
        InscribirInfanteScreen(
            nombre = "", onNombreChange = {},
            nombreError = null,
            nacimiento = "", onNacimientoChange = {},
            nacimientoError = null,
            alergias = "", onAlergiasChange = {},
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
