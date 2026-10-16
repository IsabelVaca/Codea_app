package mx.tec.codea.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.domain.MomentoRutina
import mx.tec.codea.ui.theme.Poppins
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun RutinaDetalleScreen(
    nombreRutina: String,
    momentos: List<MomentoRutina>,
    onReordenar: (from: Int, to: Int) -> Unit,
    onAgregarMomento: () -> Unit,
    onGuardar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(listState) { from, to ->
        onReordenar(from.index - 1, to.index - 1) // -1 por el encabezado
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFEFE9FF))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(nombreRutina, fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4A3A80))
                Text(
                    "Arrastra de las rayitas para dejar los momentos en el orden deseado.",
                    fontFamily = Poppins, fontSize = 13.sp, lineHeight = 19.sp, color = Color(0xFF5B4B8A)
                )
            }
        }

        itemsIndexed(momentos, key = { _, m -> m.id }) { i, m ->
            ReorderableItem(reorderState, key = m.id) { arrastrando ->
                FilaMomentoArrastrable(i, m, arrastrando)
            }
        }

        item(key = "acciones") {
            Column(Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(2.dp, Color(0xFFD8D0E8), RoundedCornerShape(20.dp))
                        .clickable(onClick = onAgregarMomento)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+ Agregar un momento", fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B6183))
                }
                BotonPrincipal("Guardar rutina", Color(0xFF12A594), onGuardar)
            }
        }
    }
}
