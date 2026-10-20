package mx.tec.codea.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import mx.tec.codea.ui.theme.Poppins
import mx.tec.codea.ui.components.ScreenHeader

// Ajustes: el menú de todo lo que el administrador puede configurar. cada fila abre su pantalla.
// para agregar una opción nueva, súmala aquí y su ruta en AjustesAdminGraph.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    totalRutinas: Int,
    onRutinasClick: () -> Unit,
    onHorarioClick: () -> Unit,
    onMenuClick: () -> Unit,
    onPersonasAutorizadasClick: () -> Unit,
    onCuentaClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = { ScreenHeader(
                title = "Ajustes",
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp),
            ) },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // lo que se configura de la estancia, en una sola tarjeta.
            AjustesGrupo {
                AjusteFila(
                    icono = Icons.AutoMirrored.Outlined.ListAlt,
                    titulo = "Rutinas",
                    detalle = if (totalRutinas == 1) "1 plantilla" else "$totalRutinas plantillas",
                    onClick = onRutinasClick,
                )
                AjusteSeparador()
                AjusteFila(
                    icono = Icons.Outlined.Schedule,
                    titulo = "Horario",
                    detalle = "Entrada, salida y días de trabajo",
                    onClick = onHorarioClick,
                )
                AjusteSeparador()
                AjusteFila(
                    icono = Icons.Outlined.Restaurant,
                    titulo = "Menú del día",
                    detalle = "Desayuno, comida y colación",
                    onClick = onMenuClick,
                )
                AjusteSeparador()
                AjusteFila(
                    icono = Icons.Outlined.HowToReg,
                    titulo = "Personas autorizadas",
                    detalle = "Quién recoge a cada niño",
                    onClick = onPersonasAutorizadasClick,
                )
            }
            // la sesión va aparte, abajo.
            AjustesGrupo {
                AjusteFila(
                    icono = Icons.Outlined.AccountCircle,
                    titulo = "Mi cuenta",
                    detalle = "Perfil, contraseña y cerrar sesión",
                    onClick = onCuentaClick,
                )
            }
        }
    }
}

// una tarjeta blanca que agrupa varias filas, como la configuración del teléfono.
@Composable
private fun AjustesGrupo(contenido: @Composable ColumnScope.() -> Unit) {
    val forma = RoundedCornerShape(22.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, forma, ambientColor = Color.Black.copy(alpha = 0.10f), spotColor = Color.Black.copy(alpha = 0.10f))
            .clip(forma)
            .background(Color.White)
            .border(1.dp, Color(0xFFE6E0F2), forma),
        content = contenido,
    )
}

@Composable
private fun AjusteSeparador() {
    HorizontalDivider(modifier = Modifier.padding(start = 70.dp), color = Color(0xFFF1EEF8))
}

@Composable
private fun AjusteFila(icono: ImageVector, titulo: String, detalle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEDE9FE)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icono, contentDescription = null, tint = Color(0xFF6D28D9), modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(titulo, fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140))
            Text(detalle, fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF8A80A5), maxLines = 1)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFFB9B0D0))
    }
}
