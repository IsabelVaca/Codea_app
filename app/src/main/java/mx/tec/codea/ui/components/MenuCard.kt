package mx.tec.codea.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.domain.MenuDelDia
import mx.tec.codea.ui.theme.Poppins

// lo que se come ese día: una línea por comida, con su nombre. la usan Hoy de la familia y Mi día de la asistente.
// con el estilo del admin: tarjeta blanca, el nombre de cada comida en morado y el título en negritas.
@Composable
fun MenuCard(menu: MenuDelDia, modifier: Modifier = Modifier, titulo: String = "MENÚ DEL DÍA") {
    val forma = RoundedCornerShape(22.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, forma, ambientColor = Color(0x1A2A2140), spotColor = Color(0x1A2A2140))
            .clip(forma)
            .background(CodeaColores.Tarjeta)
            .border(1.dp, CodeaColores.Borde, forma)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            // los que la llaman pasan el título en mayúsculas ("MENÚ DEL DÍA"): aquí va como título normal.
            text = titulo.lowercase().replaceFirstChar { it.uppercase() },
            fontFamily = Poppins, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CodeaColores.Ink,
        )
        menu.comidas.forEach { (nombre, texto) ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(nombre, fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = CodeaColores.Morado)
                Text(texto, fontFamily = Poppins, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium, color = CodeaColores.Ink)
            }
        }
    }
}
