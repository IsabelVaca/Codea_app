package mx.tec.codea.ui.screens.admin

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.domain.MomentoRutina
import mx.tec.codea.ui.theme.Poppins
import sh.calvin.reorderable.ReorderableCollectionItemScope

@Composable
fun ReorderableCollectionItemScope.FilaMomentoArrastrable(i: Int, m: MomentoRutina, arrastrando: Boolean) {
    val elev by animateDpAsState(if (arrastrando) 16.dp else 6.dp, label = "elev")
    val forma = RoundedCornerShape(22.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elev, forma, ambientColor = Color(0x1F2A2140), spotColor = Color(0x1F2A2140))
            .clip(forma)
            .background(Color.White)
            .border(1.dp, Color(0xFFE6E0F2), forma)
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Column(
            modifier = Modifier
                .size(26.dp, 34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (arrastrando) Color(0xFFEFE9FF) else Color.Transparent)
                .draggableHandle(),
            verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            repeat(3) { Box(Modifier.size(13.dp, 2.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFC4BAD8))) }
        }
        Box(Modifier.size(24.dp).clip(CircleShape).background(Color(0xFFEFE9FF)), contentAlignment = Alignment.Center) {
            Text("${i + 1}", fontFamily = Poppins, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF5B3FE0))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(m.nombre, fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(m.registro, fontFamily = Poppins, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFA79BBF), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun BotonPrincipal(texto: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(vertical = 18.dp)
    ) {
        Text(texto, fontFamily = Poppins, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
    }
}

@Composable
fun BotonPunteado(texto: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(2.dp, Color(0xFFD8D0E8), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(texto, fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B6183))
    }
}
