package mx.tec.codea.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.tec.codea.domain.Animo
import mx.tec.codea.domain.Comida
import mx.tec.codea.domain.ResumenDelDiaReglas
import mx.tec.codea.ui.components.BackHeader
import mx.tec.codea.ui.components.InfoBox
import mx.tec.codea.ui.state.ResumenDelDiaViewModel

// lo que la docente captura de cada niño y que el papá ve en Hoy: ánimo, comida, pañales, siesta y una nota.
// también muestra la nota que la familia le dejó a la asistente.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResumenDelDiaScreen(
    viewModel: ResumenDelDiaViewModel,
    onBackClick: () -> Unit,
    onGuardado: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BackHeader(
            overline = "Mi sala",
            title = if (viewModel.nombre.isBlank()) "Resumen del día" else "Resumen de ${viewModel.nombre}",
            onBackClick = onBackClick,
        )

        viewModel.notasFamilia.forEach { nota ->
            InfoBox(
                title = "La familia te dejó una nota",
                body = nota,
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
                titleColor = MaterialTheme.colorScheme.onSurface,
            )
        }

        Seccion("Ánimo")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Animo.entries.forEach { animo ->
                FilterChip(
                    selected = viewModel.animo == animo,
                    onClick = { viewModel.onAnimo(animo) },
                    label = { Text(animo.etiqueta) },
                )
            }
        }

        Seccion("Comida")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Comida.entries.forEach { comida ->
                FilterChip(
                    selected = viewModel.comida == comida,
                    onClick = { viewModel.onComida(comida) },
                    label = { Text(comida.etiqueta) },
                )
            }
        }

        Seccion("Cambios de pañal")
        Contador(
            texto = viewModel.panales.toString(),
            onMenos = viewModel::menosPanales,
            onMas = viewModel::masPanales,
            menosActivo = viewModel.panales > 0,
            masActivo = viewModel.panales < ResumenDelDiaReglas.PANALES_MAXIMO,
        )

        Seccion("Siesta")
        Contador(
            texto = ResumenDelDiaReglas.textoSiesta(viewModel.siesta),
            onMenos = viewModel::menosSiesta,
            onMas = viewModel::masSiesta,
            menosActivo = viewModel.siesta > 0,
            masActivo = viewModel.siesta < ResumenDelDiaReglas.SIESTA_MAXIMA,
        )

        Seccion("Nota para la familia")
        OutlinedTextField(
            value = viewModel.nota,
            onValueChange = viewModel::onNota,
            placeholder = { Text("Cómo estuvo su día (opcional)") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
        )

        Button(
            onClick = { viewModel.guardar(onGuardado) },
            enabled = !viewModel.guardando && !viewModel.cargando,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        ) {
            Text(
                text = if (viewModel.guardando) "Guardando…" else "Guardar resumen",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun Seccion(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun Contador(texto: String, onMenos: () -> Unit, onMas: () -> Unit, menosActivo: Boolean, masActivo: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        OutlinedButton(onClick = onMenos, enabled = menosActivo) { Text("−") }
        Text(
            text = texto,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        OutlinedButton(onClick = onMas, enabled = masActivo) { Text("+") }
    }
}
