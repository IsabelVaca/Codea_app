package mx.tec.codea.ui.screens.admin

import mx.tec.codea.ui.components.CodeaColores
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.domain.RutinaItem
import mx.tec.codea.ui.theme.Poppins

@Composable
fun RutinasAdminScreen(
    rutinas: List<RutinaItem>,
    onAbrirRutina: (String) -> Unit,
    onNuevaRutina: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            rutinas.forEach { r ->
                RutinaTarjeta(r) { onAbrirRutina(r.id) }
            }
        }

        Spacer(Modifier.height(14.dp))

        Button(
            onClick = onNuevaRutina,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CodeaColores.VioletaMarca),
            contentPadding = PaddingValues(vertical = 18.dp)
        ) {
            Text("+ Nueva rutina", fontFamily = Poppins, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
    }
}

@Composable
private fun RutinaTarjeta(r: RutinaItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(CodeaColores.Lavanda)
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(r.nombre, fontFamily = Poppins, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = CodeaColores.Ink)
        Text("${r.cantidadMomentos} momentos", fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.GrisMedio)
    }
}
