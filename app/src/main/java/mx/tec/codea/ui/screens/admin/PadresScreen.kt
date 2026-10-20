package mx.tec.codea.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.domain.Padre
import mx.tec.codea.ui.theme.Poppins

// la tarjeta de un padre o tutor, con el mismo diseño que InfanteCard: iniciales, nombre, parentesco e hijos,
// y a la derecha si ya tiene acceso a la app.
@Composable
internal fun PadreFila(p: Padre, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 15.dp)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color.Black.copy(alpha = 0.70f),
                spotColor = Color.Black.copy(alpha = 0.30f)
            )
            .clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .padding(18.dp)
                    .size(50.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEDE9FE)),
                contentAlignment = Alignment.Center
            ) {
                Text(p.iniciales, color = Color(0xFF6D28D9), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    p.nombre,
                    modifier = Modifier.padding(top = 10.dp),
                    color = Color(red = 1, green = 1, blue = 1),
                    fontSize = 17.sp, fontFamily = Poppins, fontWeight = FontWeight.ExtraBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                val hijosTexto = if (p.hijoIds.size == 1) "1 hijo" else "${p.hijoIds.size} hijos"
                Text(
                    "${p.parentesco} · $hijosTexto",
                    modifier = Modifier.padding(top = 3.dp, bottom = 7.dp),
                    color = Color(red = 138, green = 128, blue = 165),
                    fontSize = 13.sp, lineHeight = 13.sp, fontFamily = Poppins, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            // con acceso, en morado como la sala del infante; sin acceso, en gris como «Sin checar».
            Surface(
                modifier = Modifier.padding(end = 9.dp),
                color = if (p.conAcceso) Color(0xFFEDE9FE) else Color(0xFFF4F1FB),
                shape = RoundedCornerShape(percent = 50)
            ) {
                Text(
                    text = if (p.conAcceso) "Con acceso" else "Sin acceso",
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                    color = if (p.conAcceso) Color(0xFF6D28D9) else Color(0xFFA79BBF),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
