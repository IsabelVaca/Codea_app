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
import mx.tec.codea.domain.SalaError

data class DocenteOpcion(val nombre: String, val activo: Boolean)

@Composable
fun CrearSalaScreen(
    nombre: String,
    onNombreChange: (String) -> Unit,
    nombreError: SalaError?,
    cupo: String,
    onCupoChange: (String) -> Unit,
    cupoError: SalaError?,
    asistentes: List<DocenteOpcion>,
    onElegirAsistente: (String) -> Unit,
    canGuardar: Boolean,
    onGuardar: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textoGuardar = if (nombre.isNotBlank()) "Crear sala \"$nombre\"" else "Crear sala"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Flecha para regresar a "Centro" y título
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
                text = "Crear sala",
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
                .background(Color(0xFFE8FBF6))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Nueva sala",
                fontFamily = Poppins,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F7A6C)
            )
            Text(
                text = "Define su nombre, cupo y quién la atiende.",
                fontFamily = Poppins,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = Color(0xFF3E7A70)
            )
        }

        // Datos de la sala
        Text(
            text = "DATOS DE LA SALA",
            fontFamily = Poppins,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = Color(0xFFA79BBF)
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = nombre,
                onValueChange = onNombreChange,
                placeholder = { Text("Nombre de la sala", fontFamily = Poppins) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                isError = nombreError != null,
                supportingText = {
                    nombreError?.let { Text(it.message()) }
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
            OutlinedTextField(
                value = cupo,
                onValueChange = onCupoChange,
                placeholder = { Text("Cupo máximo (ej. 16)", fontFamily = Poppins) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = cupoError != null,
                supportingText = {
                    cupoError?.let { Text(it.message()) }
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

        // Asistente asignada
        Text(
            text = "ASISTENTE ASIGNADA",
            fontFamily = Poppins,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = Color(0xFFA79BBF)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            asistentes.forEach { o ->
                val bg = if (o.activo) Color(0xFF12A594) else Color(0xFFF4F1FB)
                val fg = if (o.activo) Color(0xFFFFFDFA) else Color(0xFF6B6183)
                Button(
                    onClick = { onElegirAsistente(o.nombre) },
                    colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
                    shape = RoundedCornerShape(999.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 11.dp)
                ) {
                    Text(o.nombre, fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Guardar
        Button(
            onClick = onGuardar,
            enabled = canGuardar,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF12A594)),
            contentPadding = PaddingValues(vertical = 17.dp)
        ) {
            Text(textoGuardar, fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
        Text(
            text = "La sala queda disponible para inscribir infantes y asignar personal.",
            fontFamily = Poppins,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = Color(0xFF8C7FA8),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CrearSalaScreenPreview() {
    CodeaTheme {
        CrearSalaScreen(
            nombre = "",
            onNombreChange = {},
            nombreError = null,
            cupo = "",
            onCupoChange = {},
            asistentes = listOf(
                DocenteOpcion("Paola Sánchez", activo = true),
                DocenteOpcion("Fernando Ríos", activo = false),
                DocenteOpcion("Gabriela Ortiz", activo = false),
            ),
            onElegirAsistente = {},
            onGuardar = {},
            onBack = {},
            canGuardar = true,
            cupoError = null
        )
    }
}

private fun SalaError.message(): String = when (this) {
    SalaError.NombreVacio -> "Escribe un nombre para la sala"
    SalaError.CupoInvalido -> "El cupo debe ser un número mayor a 0"
}