package mx.tec.codea.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.domain.MomentoRutina
import mx.tec.codea.ui.theme.Poppins
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import mx.tec.codea.ui.components.BackHeader

@Composable
fun OrdenRutinaScreen(
    momentos: List<MomentoRutina>,
    onReordenar: (from: Int, to: Int) -> Unit,
    onGuardar: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(listState) { from, to ->
        onReordenar(from.index - 1, to.index - 1) // -1 por el encabezado
    }

    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "header") {
                Column(Modifier.padding(bottom = 6.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    BackHeader(title = "Nueva rutina", onBackClick = onBack)
                    SeccionLabel("PASO 3 DE 3")
                    Text(
                        "Arrastra de las rayitas para dejar los momentos en orden.",
                        fontFamily = Poppins, fontSize = 13.sp, lineHeight = 19.sp, color = Color(0xFF8C7FA8)
                    )
                }
            }
            itemsIndexed(momentos, key = { _, m -> m.id }) { i, m ->
                ReorderableItem(reorderState, key = m.id) { arrastrando ->
                    FilaMomentoArrastrable(i, m, arrastrando)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        BotonPrincipal("Guardar rutina", Color(0xFF12A594), onGuardar)
    }
}
