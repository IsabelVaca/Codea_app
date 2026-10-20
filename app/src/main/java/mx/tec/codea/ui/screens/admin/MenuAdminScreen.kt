package mx.tec.codea.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import java.text.SimpleDateFormat
import java.util.Locale
import mx.tec.codea.domain.MenuDelDia
import mx.tec.codea.domain.MenuRules
import mx.tec.codea.ui.components.BackHeader
import mx.tec.codea.ui.theme.Poppins

// Ajustes → Menú del día: una semana (lunes a viernes) a la vez. cada día muestra su desayuno, comida y colación,
// y se edita en un cuadro. las familias lo ven en Hoy y la asistente en Mi día.
@Composable
fun MenuAdminScreen(
    lunes: String,
    dias: List<String>,
    menus: Map<String, MenuDelDia>,
    cargando: Boolean,
    onSemanaAnterior: () -> Unit,
    onSemanaSiguiente: () -> Unit,
    onEstaSemana: () -> Unit,
    onGuardar: (fecha: String, menu: MenuDelDia) -> Unit,
    onQuitar: (fecha: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // el día que se está editando; null = ningún cuadro abierto.
    var editando by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            BackHeader(
                title = "Menú del día",
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onSemanaAnterior) { Text("‹ Anterior", color = Violeta, fontWeight = FontWeight.Bold) }
                Text(
                    semanaTexto(lunes),
                    modifier = Modifier.weight(1f),
                    fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                TextButton(onClick = onSemanaSiguiente) { Text("Siguiente ›", color = Violeta, fontWeight = FontWeight.Bold) }
            }
            TextButton(onClick = onEstaSemana, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Ir a esta semana", color = Violeta, fontSize = 12.sp)
            }
            if (cargando && menus.isEmpty()) {
                Text("Cargando…", fontFamily = Poppins, fontSize = 13.sp, color = Gris)
            }
            dias.forEach { fecha ->
                DiaMenuCard(
                    fecha = fecha,
                    menu = menus[fecha],
                    onEditar = { editando = fecha },
                )
            }
        }
    }

    editando?.let { fecha ->
        EditarMenuDialog(
            fecha = fecha,
            actual = menus[fecha],
            onGuardar = { menu ->
                onGuardar(fecha, menu)
                editando = null
            },
            onQuitar = {
                onQuitar(fecha)
                editando = null
            },
            onCancelar = { editando = null },
        )
    }
}

private val Ink = Color(0xFF2A2140)
private val Violeta = Color(0xFF5B3FE0)
private val Gris = Color(0xFF6B6485)
private val Campo = Color(0xFFEFE9FF)

@Composable
private fun DiaMenuCard(fecha: String, menu: MenuDelDia?, onEditar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Campo)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                etiquetaDia(fecha),
                modifier = Modifier.weight(1f),
                fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Ink,
            )
            TextButton(onClick = onEditar) {
                Text(if (menu == null) "Agregar" else "Editar", color = Violeta, fontWeight = FontWeight.Bold)
            }
        }
        if (menu == null) {
            Text("Sin menú", fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Gris)
        } else {
            menu.comidas.forEach { (nombre, texto) ->
                Text(
                    "$nombre: $texto",
                    fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, lineHeight = 19.sp, color = Ink,
                )
            }
        }
    }
}

@Composable
private fun EditarMenuDialog(
    fecha: String,
    actual: MenuDelDia?,
    onGuardar: (MenuDelDia) -> Unit,
    onQuitar: () -> Unit,
    onCancelar: () -> Unit,
) {
    var menu by remember { mutableStateOf(actual ?: MenuDelDia()) }
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(etiquetaDia(fecha)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CampoComida("Desayuno", menu.desayuno) { menu = menu.copy(desayuno = it) }
                CampoComida("Comida", menu.comida) { menu = menu.copy(comida = it) }
                CampoComida("Colación", menu.colacion) { menu = menu.copy(colacion = it) }
                if (menu.vacio) {
                    Text("Escribe al menos una comida.", fontSize = 12.sp, color = Gris)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onGuardar(menu) }, enabled = MenuRules.puedeGuardar(menu)) { Text("Guardar") }
        },
        dismissButton = {
            Row {
                if (actual != null) TextButton(onClick = onQuitar) { Text("Quitar menú") }
                TextButton(onClick = onCancelar) { Text("Cancelar") }
            }
        },
    )
}

@Composable
private fun CampoComida(etiqueta: String, valor: String, onCambio: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = { if (it.length <= MenuRules.MAX_CARACTERES) onCambio(it) },
        label = { Text(etiqueta) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
    )
}

// "lunes 12 de octubre"
private fun etiquetaDia(iso: String): String {
    val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso) ?: return iso
    return SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "MX")).format(fecha).replaceFirstChar { it.uppercase() }
}

// "Semana del 12 al 16 de octubre"
private fun semanaTexto(lunes: String): String {
    val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val desde = formato.parse(lunes) ?: return lunes
    val hasta = formato.parse(MenuRules.sumarDias(lunes, 4)) ?: return lunes
    val dia = SimpleDateFormat("d", Locale.US)
    val mes = SimpleDateFormat("MMMM", Locale("es", "MX"))
    val mesDesde = mes.format(desde)
    val mesHasta = mes.format(hasta)
    return if (mesDesde == mesHasta) "Semana del ${dia.format(desde)} al ${dia.format(hasta)} de $mesHasta"
    else "Semana del ${dia.format(desde)} de $mesDesde al ${dia.format(hasta)} de $mesHasta"
}
