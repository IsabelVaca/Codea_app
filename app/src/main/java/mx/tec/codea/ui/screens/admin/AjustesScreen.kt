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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.ui.theme.Poppins

// Ajustes: el menú de todo lo que el administrador puede configurar. cada fila abre su pantalla.
// para agregar una opción nueva, súmala aquí y su ruta en AjustesAdminGraph.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    totalRutinas: Int,
    onRutinasClick: () -> Unit,
    onCuentaClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Ajustes", fontWeight = FontWeight.Bold) }) },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SeccionLabel("LA ESTANCIA")
            AjusteFila(
                titulo = "Rutinas",
                detalle = if (totalRutinas == 1) "1 plantilla · momentos del día y salas" else "$totalRutinas plantillas · momentos del día y salas",
                onClick = onRutinasClick,
            )
            SeccionLabel("MI SESIÓN")
            AjusteFila(
                titulo = "Mi cuenta",
                detalle = "Perfil, cambiar contraseña y cerrar sesión",
                onClick = onCuentaClick,
            )
        }
    }
}

@Composable
private fun AjusteFila(titulo: String, detalle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFEFE9FF))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(titulo, fontFamily = Poppins, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140))
            Text(detalle, fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B6485))
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF5B3FE0))
    }
}
