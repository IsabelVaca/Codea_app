package mx.tec.codea.ui.screens.admin

import mx.tec.codea.ui.components.CodeaColores
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import mx.tec.codea.ui.theme.Poppins
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import mx.tec.codea.domain.Docente
import mx.tec.codea.domain.Infante
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import mx.tec.codea.domain.Sala
import androidx.compose.ui.draw.shadow
import mx.tec.codea.ui.components.ScreenHeader

private enum class SeccionDocentes(val etiqueta: String) {
    DOCENTES("Docentes"),
    SALAS("Salas"),
}

// "Docentes" junta a las docentes y las salas (la ocupación que antes estaba en Centro).
// arriba va el selector; se jala hacia abajo para recargar.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocenteScreen(
    docentes: List<Docente>,
    infantes: List<Infante>,
    salas: List<Sala>,
    onDocenteClick: (Docente) -> Unit = {},
    onDarDeAltaAsistente: () -> Unit = {},
    onSalaClick: (Sala) -> Unit = {},
    onCrearSala: () -> Unit = {},
    cargando: Boolean = false,
    onRecargar: () -> Unit = {},
){
    // al volver de un detalle (o de crear una sala) se queda en la lista donde estaba.
    var seleccion by rememberSaveable { mutableIntStateOf(SeccionDocentes.DOCENTES.ordinal) }
    // por omisión solo las activas; «Todos» muestra también a las dadas de baja.
    var soloActivas by rememberSaveable { mutableStateOf(true) }
    val seccion = SeccionDocentes.entries[seleccion]

    Scaffold(
        topBar = {
            ScreenHeader(
                title = "Docentes",
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp),
            )
        },
        content = { paddingValues ->
            PullToRefreshBox(
                isRefreshing = cargando,
                onRefresh = onRecargar,
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // DocenteCard trae 15 dp de margen lateral propio: lo demás usa los mismos 15 dp.
                val lateral = Modifier.padding(horizontal = 15.dp)

                SingleChoiceSegmentedButtonRow(modifier = lateral.fillMaxWidth()) {
                    SeccionDocentes.entries.forEachIndexed { i, s ->
                        SegmentedButton(
                            selected = seccion == s,
                            onClick = { seleccion = i },
                            shape = SegmentedButtonDefaults.itemShape(index = i, count = SeccionDocentes.entries.size),
                        ) { Text(s.etiqueta, fontFamily = Poppins, fontWeight = FontWeight.Bold) }
                    }
                }

                when (seccion) {
                    SeccionDocentes.DOCENTES -> {
                        // el botón de agregar va arriba de la lista, igual que en Salas.
                        DarDeAltaAsistenteButton(onClick = onDarDeAltaAsistente, modifier = lateral)
                        FiltroActivos(
                            soloActivos = soloActivas,
                            activos = docentes.count { !it.baja },
                            total = docentes.size,
                            onCambio = { soloActivas = it },
                            modifier = lateral,
                        )
                        Column(modifier = Modifier.padding(bottom = 16.dp)) {
                            docentes.filter { !soloActivas || !it.baja }.forEach { docente ->
                                val ninosDelDocente = infantes.count { it.sala == docente.sala }
                                DocenteCard(docente = docente, ninosEnSala = ninosDelDocente, onClick = { onDocenteClick(docente) })
                            }
                        }
                    }
                    SeccionDocentes.SALAS -> SalasSeccion(
                        salas = salas,
                        infantes = infantes,
                        onSalaClick = onSalaClick,
                        onCrearSala = onCrearSala,
                        modifier = lateral.padding(bottom = 16.dp),
                    )
                }
            }
            }
        }
    )
}

@Composable
fun DocenteCard(docente: Docente, ninosEnSala: Int, onClick: () -> Unit = {}) {
    Surface(modifier = Modifier.fillMaxWidth(1f).padding(vertical = 5.dp, horizontal = 15.dp).shadow(
            elevation = 20.dp,
        shape = RoundedCornerShape(18.dp),
        ambientColor = Color.Black.copy(alpha = 0.70f),
        spotColor = Color.Black.copy(alpha = 0.30f)
    ).clickable(onClick = onClick),
        color = CodeaColores.Tarjeta,
        border = BorderStroke(1.dp, CodeaColores.Borde),
        shape = RoundedCornerShape(18.dp))
    {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //siglas del nombre
            Box(
                modifier = Modifier.padding(18.dp)
                    .size(50.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CodeaColores.Lila)
                    ,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = docente.iniciales,
                    color = CodeaColores.Morado,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            //Nombre y datos
            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = docente.nombre,
                    modifier = Modifier.padding(top = 10.dp),
                    color = CodeaColores.Titulo,
                    fontSize = 17.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.ExtraBold
                )
                FlowRow(
                    modifier = Modifier.padding(top = 3.dp, bottom = 7.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    //sala
                    Text(
                        text = (docente.sala.ifBlank { "Sin sala" }) + " • ",
                        modifier = Modifier,
                        color = CodeaColores.GrisTexto,
                        fontSize = 13.sp,
                        lineHeight = 13.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold
                    )
                    //cantidad de niños en la sala
                    Text(
                        text = "$ninosEnSala niños",
                        modifier = Modifier,
                        color = CodeaColores.GrisTexto,
                        fontSize = 13.sp,
                        lineHeight = 13.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold
                    )

                }

            }

            // sin checada todavía (o dada de baja): la pastilla va en gris.
            val checo = docente.horaChecada != null && !docente.baja
            Surface(
                modifier = Modifier.padding(end = 9.dp),
                color = if (checo) CodeaColores.Lila else CodeaColores.Campo,
                shape = RoundedCornerShape(percent = 50)
            ) {
                Text(
                    text = when {
                        docente.baja -> "De baja"
                        else -> docente.horaChecada?.let { "Checó $it" } ?: "Sin checar"
                    },
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                    color = if (checo) CodeaColores.Morado else CodeaColores.Gris,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }

        }

    }
}

@Composable
fun DarDeAltaAsistenteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(2.dp, CodeaColores.Lila),
        colors = ButtonDefaults.buttonColors(
            containerColor = CodeaColores.Lila,
            contentColor = CodeaColores.Morado
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = "+ Dar de alta a una asistente",
            fontSize = 15.sp,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold
        )
    }
}
