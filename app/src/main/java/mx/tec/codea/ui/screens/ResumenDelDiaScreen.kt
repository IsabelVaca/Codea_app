package mx.tec.codea.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import mx.tec.codea.R
import mx.tec.codea.domain.Animo
import mx.tec.codea.domain.Comida
import mx.tec.codea.domain.ResumenDelDiaReglas
import mx.tec.codea.ui.components.BackHeader
import mx.tec.codea.ui.components.CodeaColores
import mx.tec.codea.ui.components.InfoBox
import mx.tec.codea.ui.components.Pastilla
import mx.tec.codea.ui.components.TarjetaBlanca
import mx.tec.codea.ui.state.ResumenDelDiaViewModel
import mx.tec.codea.ui.theme.Poppins

// lo que la docente captura de cada niño y que el papá ve en Hoy: ánimo, comida, pañales, siesta y una nota.
// también muestra la nota que la familia le dejó a la asistente.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResumenDelDiaScreen(
    viewModel: ResumenDelDiaViewModel,
    onBackClick: () -> Unit,
    onGuardado: () -> Unit,
    modifier: Modifier = Modifier,
    // resumen guiado: en qué niño de la cola va ("2 de 5"); null cuando se abrió suelto desde la ficha.
    guidedPosition: Int? = null,
    guidedTotal: Int = 0,
    isLastGuided: Boolean = true,
    onSkip: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BackHeader(
            title = if (viewModel.nombre.isBlank()) "Resumen del día" else "Resumen de ${viewModel.nombre}",
            onBackClick = onBackClick,
        )
        // en el recorrido guiado una pastilla dice en cuál va ("Resumen 2 de 5"); fuera de él no sale.
        guidedPosition?.let { Pastilla(stringResource(R.string.summary_guided_overline, it, guidedTotal)) }

        viewModel.notasFamilia.forEach { nota ->
            InfoBox(
                title = "La familia te dejó una nota",
                body = nota,
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
                titleColor = CodeaColores.Ink,
            )
        }

        // cada dato en su tarjeta blanca, con la etiqueta adentro como en los formularios del papá.
        TarjetaBlanca(espacio = 10.dp) {
            Seccion("Ánimo")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Animo.entries.forEach { animo ->
                    Opcion(animo.etiqueta, viewModel.animo == animo) { viewModel.onAnimo(animo) }
                }
            }
        }

        TarjetaBlanca(espacio = 10.dp) {
            Seccion("Comida")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Comida.entries.forEach { comida ->
                    Opcion(comida.etiqueta, viewModel.comida == comida) { viewModel.onComida(comida) }
                }
            }
        }

        TarjetaBlanca(espacio = 10.dp) {
            Seccion("Cambios de pañal")
            Contador(
                texto = viewModel.panales.toString(),
                onMenos = viewModel::menosPanales,
                onMas = viewModel::masPanales,
                menosActivo = viewModel.panales > 0,
                masActivo = viewModel.panales < ResumenDelDiaReglas.PANALES_MAXIMO,
            )
        }

        TarjetaBlanca(espacio = 10.dp) {
            Seccion("Siesta")
            Contador(
                texto = ResumenDelDiaReglas.textoSiesta(viewModel.siesta),
                onMenos = viewModel::menosSiesta,
                onMas = viewModel::masSiesta,
                menosActivo = viewModel.siesta > 0,
                masActivo = viewModel.siesta < ResumenDelDiaReglas.SIESTA_MAXIMA,
            )
        }

        TarjetaBlanca(espacio = 10.dp) {
            Seccion("Nota para la familia")
            OutlinedTextField(
                value = viewModel.nota,
                onValueChange = viewModel::onNota,
                placeholder = { Text("Cómo estuvo su día (opcional)", fontFamily = Poppins, color = CodeaColores.Gris) },
                minLines = 3,
                textStyle = TextStyle(fontFamily = Poppins, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = CodeaColores.Ink),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CodeaColores.Campo,
                    unfocusedContainerColor = CodeaColores.Campo,
                    focusedBorderColor = CodeaColores.Morado,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = CodeaColores.Morado,
                ),
            )
        }

        Button(
            onClick = { viewModel.guardar(onGuardado) },
            enabled = !viewModel.guardando && !viewModel.cargando,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = BotonVioleta(),
            elevation = ButtonDefaults.buttonElevation(0.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        ) {
            Text(
                text = when {
                    viewModel.guardando -> "Guardando…"
                    guidedPosition == null -> "Guardar resumen"
                    isLastGuided -> stringResource(R.string.summary_guided_save_last)
                    else -> stringResource(R.string.summary_guided_save)
                },
                fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.Bold,
            )
        }

        // sin guardar este niño, se pasa al siguiente de la cola.
        if (guidedPosition != null) {
            Button(
                onClick = onSkip,
                enabled = !viewModel.guardando,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CodeaColores.Lila, contentColor = CodeaColores.Morado),
                elevation = ButtonDefaults.buttonElevation(0.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
            ) {
                Text(
                    text = stringResource(if (isLastGuided) R.string.summary_guided_finish else R.string.summary_guided_skip),
                    fontFamily = Poppins, fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun Seccion(texto: String) {
    Text(
        texto.uppercase(),
        fontFamily = Poppins, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.1.sp, color = CodeaColores.Gris,
    )
}

// una opción de ánimo o comida: lila con borde morado si está elegida, gris claro si no.
@Composable
private fun Opcion(texto: String, elegida: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (elegida) CodeaColores.Lila else CodeaColores.Campo,
        border = if (elegida) BorderStroke(2.dp, CodeaColores.Morado) else null,
    ) {
        Text(
            texto,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            fontFamily = Poppins, fontSize = 13.sp,
            fontWeight = if (elegida) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (elegida) CodeaColores.Morado else CodeaColores.GrisTexto,
        )
    }
}

@Composable
private fun Contador(texto: String, onMenos: () -> Unit, onMas: () -> Unit, menosActivo: Boolean, masActivo: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        BotonContador("−", menosActivo, onMenos)
        Text(texto, fontFamily = Poppins, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = CodeaColores.Ink)
        BotonContador("+", masActivo, onMas)
    }
}

@Composable
private fun BotonContador(texto: String, activo: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = activo,
        modifier = Modifier.size(48.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CodeaColores.Lila,
            contentColor = CodeaColores.Morado,
            disabledContainerColor = CodeaColores.Campo,
            disabledContentColor = CodeaColores.Gris,
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Text(texto, fontFamily = Poppins, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}
