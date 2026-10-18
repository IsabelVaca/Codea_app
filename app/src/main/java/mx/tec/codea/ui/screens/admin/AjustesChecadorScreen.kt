package mx.tec.codea.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.domain.UbicacionCentro

// Ajustes → Checador: la ubicación de la estancia (con el GPS del admin) y el radio en metros.
// las dos cosas cambian dónde todas las docentes pueden registrar su entrada.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesChecadorScreen(
    ubicacion: UbicacionCentro?,
    ubicando: Boolean,
    onUsarMiUbicacion: () -> Unit,
    radioTexto: String,
    onRadioChange: (String) -> Unit,
    radioInvalido: Boolean,
    canGuardarRadio: Boolean,
    onGuardarRadio: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmarUbicacion by remember { mutableStateOf(false) }
    if (confirmarUbicacion) {
        // cambia el lugar donde todas las docentes registran su entrada, así que se pide confirmar.
        AlertDialog(
            onDismissRequest = { confirmarUbicacion = false },
            title = { Text("¿Usar tu ubicación actual?") },
            text = {
                Text(
                    "La estancia quedará donde estás ahora. Las docentes solo podrán registrar su entrada " +
                        "a menos de ${ubicacion?.radioMetros ?: 60} metros de aquí.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmarUbicacion = false
                    onUsarMiUbicacion()
                }) { Text("Sí, usar mi ubicación") }
            },
            dismissButton = { TextButton(onClick = { confirmarUbicacion = false }) { Text("Cancelar") } },
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checador", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Tarjeta {
                Text("Ubicación de la estancia", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140))
                Text(
                    text = when {
                        ubicacion == null -> "Cargando…"
                        ubicacion.definida ->
                            String.format(java.util.Locale.US, "%.5f, %.5f", ubicacion.latitud, ubicacion.longitud)
                        else -> "Sin ubicación: las docentes no pueden registrar su entrada"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6B6485),
                )
                Button(
                    onClick = { confirmarUbicacion = true },
                    enabled = !ubicando && ubicacion != null,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (ubicando) "Buscando tu ubicación…" else "Usar mi ubicación actual") }
            }

            Tarjeta {
                Text("Radio para registrar la entrada", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140))
                Text(
                    "Qué tan cerca de la estancia debe estar la docente. " +
                        "De ${UbicacionCentro.RADIO_MIN_M} a ${UbicacionCentro.RADIO_MAX_M} metros.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6B6485),
                )
                OutlinedTextField(
                    value = radioTexto,
                    onValueChange = onRadioChange,
                    label = { Text("Radio en metros") },
                    suffix = { Text("m") },
                    singleLine = true,
                    isError = radioInvalido,
                    supportingText = if (radioInvalido) {
                        { Text("Escribe un número entre ${UbicacionCentro.RADIO_MIN_M} y ${UbicacionCentro.RADIO_MAX_M}") }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = onGuardarRadio,
                    enabled = canGuardarRadio,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Guardar radio") }
            }
        }
    }
}

@Composable
private fun Tarjeta(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFF4F1FB))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) { content() }
}
