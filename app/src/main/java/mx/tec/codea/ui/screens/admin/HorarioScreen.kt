package mx.tec.codea.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.domain.HorarioCentro
import mx.tec.codea.domain.HorarioRules
import mx.tec.codea.domain.formatTimeOfDay
import mx.tec.codea.ui.components.BackHeader

// Ajustes → Horario: el horario de todo el centro. la hora de entrada decide quién llega tarde, la de salida
// es la esperada, y a la hora de cierre el servidor cierra solas las entradas que siguen abiertas.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorarioScreen(
    horario: HorarioCentro?,
    error: String?,
    canGuardar: Boolean,
    onEntrada: (Int) -> Unit,
    onSalida: (Int) -> Unit,
    onCierre: (Int) -> Unit,
    onToggleDia: (Int) -> Unit,
    onGuardar: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            BackHeader(
                overline = "Ajustes",
                title = "Horario",
                onBackClick = onBack,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
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
            if (horario == null) {
                Text("Cargando…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                return@Column
            }
            Tarjeta {
                Text("Horas del día", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140))
                FilaHora("Entrada", "A esta hora empieza el día; después se marca retardo", horario.entradaMinutos, onEntrada)
                FilaHora("Salida", "La hora en que termina el día", horario.salidaMinutos, onSalida)
                FilaHora(
                    "Cierre automático",
                    "A esta hora se cierran solas las entradas que sigan abiertas",
                    horario.cierreMinutos,
                    onCierre,
                )
            }

            Tarjeta {
                Text("Días de trabajo", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140))
                Text(
                    "Los otros días no se puede registrar entrada.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6B6485),
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    HorarioRules.DIAS.forEach { (dia, letra) ->
                        FilterChip(
                            selected = dia in horario.diasDeTrabajo,
                            onClick = { onToggleDia(dia) },
                            label = { Text(letra, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Button(onClick = onGuardar, enabled = canGuardar, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar horario")
            }
        }
    }
}

// una fila con el nombre, su explicación y la hora; al tocarla se abre el selector.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilaHora(titulo: String, detalle: String, minutos: Int, onCambio: (Int) -> Unit) {
    var eligiendo by remember { mutableStateOf(false) }
    if (eligiendo) {
        val estado = rememberTimePickerState(initialHour = minutos / 60, initialMinute = minutos % 60, is24Hour = true)
        AlertDialog(
            onDismissRequest = { eligiendo = false },
            title = { Text(titulo) },
            text = { TimePicker(state = estado) },
            confirmButton = {
                TextButton(onClick = {
                    onCambio(estado.hour * 60 + estado.minute)
                    eligiendo = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { eligiendo = false }) { Text("Cancelar") } },
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { eligiendo = true }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140))
            Text(detalle, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B6485))
        }
        Text(
            formatTimeOfDay(minutos),
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF5B3FE0),
        )
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
