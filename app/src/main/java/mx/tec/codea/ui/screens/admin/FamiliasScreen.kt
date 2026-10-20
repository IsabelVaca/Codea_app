package mx.tec.codea.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.domain.Infante
import mx.tec.codea.domain.Padre
import mx.tec.codea.ui.theme.Poppins
import mx.tec.codea.ui.components.ScreenHeader

// las dos mitades de la pantalla: los niños y las personas que los recogen.
private enum class SeccionFamilias(val etiqueta: String) {
    INFANTES("Infantes"),
    PADRES("Padres y tutores"),
}

// "Familias" junta lo que antes eran dos pestañas (Infantes y Padres). arriba va el selector;
// abajo, la lista elegida con su botón de inscribir.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamiliasScreen(
    infantes: List<Infante>,
    padres: List<Padre>,
    onInfanteClick: (Infante) -> Unit,
    onInscribirInfante: () -> Unit,
    onPadreClick: (Padre) -> Unit,
    onInscribirPadre: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // al volver de un detalle se queda en la lista donde estaba.
    var seleccion by rememberSaveable { mutableIntStateOf(SeccionFamilias.INFANTES.ordinal) }
    val seccion = SeccionFamilias.entries[seleccion]

    Scaffold(
        topBar = { ScreenHeader(
                title = "Familias",
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp),
            ) },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // InfanteCard trae 15 dp de margen lateral propio: el resto del contenido usa los mismos 15 dp
            // para que todo quede alineado.
            val lateral = Modifier.padding(horizontal = 15.dp)

            SingleChoiceSegmentedButtonRow(modifier = lateral.fillMaxWidth()) {
                SeccionFamilias.entries.forEachIndexed { i, s ->
                    SegmentedButton(
                        selected = seccion == s,
                        onClick = { seleccion = i },
                        shape = SegmentedButtonDefaults.itemShape(index = i, count = SeccionFamilias.entries.size),
                    ) { Text(s.etiqueta, fontFamily = Poppins, fontWeight = FontWeight.Bold) }
                }
            }

            when (seccion) {
                SeccionFamilias.INFANTES -> {
                    Box(modifier = lateral) { SeccionLabel("INFANTES INSCRITOS") }
                    Column {
                        infantes.forEach { infante ->
                            InfanteCard(infante = infante, onClick = { onInfanteClick(infante) })
                        }
                    }
                    InscribirInfanteButton(onClick = onInscribirInfante, modifier = lateral)
                }

                SeccionFamilias.PADRES -> {
                    Box(modifier = lateral) { SeccionLabel("PADRES Y TUTORES") }
                    Column(modifier = lateral, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        padres.forEachIndexed { i, p ->
                            val (bg, fg) = avatarColores[i % avatarColores.size]
                            PadreFila(p, bg, fg) { onPadreClick(p) }
                        }
                    }
                    BotonInscribirPadre(onClick = onInscribirPadre, modifier = lateral)
                }
            }
        }
    }
}

@Composable
private fun BotonInscribirPadre(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(2.dp, Color(0xFFEDE9FE)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFEDE9FE),
            contentColor = Color(0xFF6D28D9),
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = "+ Inscribir a un padre o tutor",
            fontSize = 15.sp,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
        )
    }
}
