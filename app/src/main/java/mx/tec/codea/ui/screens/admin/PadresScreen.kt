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

private val avatarColores = listOf(
    Color(0xFFEFE9FF) to Color(0xFF5B3FE0),
    Color(0xFFFFEDE7) to Color(0xFFC4432A),
    Color(0xFFE8FBF6) to Color(0xFF0F7A6C),
    Color(0xFFFFF4D6) to Color(0xFFB37F09),
    Color(0xFFF4F1FB) to Color(0xFF6B6183)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PadresScreen(
    totalPadres: Int,
    padres: List<Padre>,
    onAbrirPadre: (Int) -> Unit,
    onInscribirPadre: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Padres registrados", fontWeight = FontWeight.Bold) })
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Resumen
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFEFE9FF))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("$totalPadres", fontFamily = Poppins, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF5B3FE0))
                Text("familias con acceso a la app", fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5B4B8A))
            }

            SeccionLabel("PADRES Y TUTORES")

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                padres.forEachIndexed { i, p ->
                    val (bg, fg) = avatarColores[i % avatarColores.size]
                    PadreFila(p, bg, fg) { onAbrirPadre(i) }
                }
            }

            // Inscribir
            Button(
                onClick = onInscribirPadre,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, Color(0xFFEDE9FE)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEDE9FE),
                    contentColor = Color(0xFF6D28D9)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "+ Inscribir a un padre o tutor",
                    fontSize = 15.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PadreFila(p: Padre, avatarBg: Color, avatarFg: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x122A2140), spotColor = Color(0x122A2140))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(avatarBg),
            contentAlignment = Alignment.Center
        ) {
            Text(p.iniciales, fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = avatarFg)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(p.nombre, fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140), maxLines = 1, overflow = TextOverflow.Ellipsis)
            val hijosTexto = if (p.hijoIds.size == 1) "1 hijo vinculado" else "${p.hijoIds.size} hijos vinculados"
            Text("${p.parentesco} · $hijosTexto", fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF8C7FA8), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(
            text = if (p.conAcceso) "Con acceso" else "Sin acceso",
            fontFamily = Poppins, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
            color = if (p.conAcceso) Color(0xFF0F7A6C) else Color(0xFF8C7FA8),
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(if (p.conAcceso) Color(0xFFE8FBF6) else Color(0xFFF4F1FB))
                .padding(horizontal = 11.dp, vertical = 6.dp)
        )
    }
}