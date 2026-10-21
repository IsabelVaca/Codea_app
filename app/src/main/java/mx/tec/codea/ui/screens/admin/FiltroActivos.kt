package mx.tec.codea.ui.screens.admin

import mx.tec.codea.ui.components.CodeaColores
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.tec.codea.ui.theme.Poppins

// el filtro de las listas del admin donde se puede dar de baja (docentes, infantes, padres):
// solo los activos (lo normal) o todos, con los dados de baja incluidos.
@Composable
fun FiltroActivos(
    soloActivos: Boolean,
    activos: Int,
    total: Int,
    onCambio: (soloActivos: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OpcionFiltro(texto = "Activos · $activos", elegida = soloActivos) { onCambio(true) }
        OpcionFiltro(texto = "Todos · $total", elegida = !soloActivos) { onCambio(false) }
    }
}

@Composable
private fun OpcionFiltro(texto: String, elegida: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = elegida,
        onClick = onClick,
        label = { Text(texto, fontFamily = Poppins, fontWeight = FontWeight.Bold) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CodeaColores.Lila,
            selectedLabelColor = CodeaColores.Morado,
        ),
    )
}
