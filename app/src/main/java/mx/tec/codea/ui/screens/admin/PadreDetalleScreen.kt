package mx.tec.codea.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.domain.Padre
import mx.tec.codea.ui.theme.AvatarBg
import mx.tec.codea.ui.theme.AvatarFg
import mx.tec.codea.ui.theme.Poppins
import mx.tec.codea.ui.components.BackHeader

@Composable
fun PadreDetalleScreen(
    padre: Padre,
    hijosNombres: List<String>,
    onToggleAcceso: () -> Unit,
    onReenviarInvitacion: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDFA))
            .padding(horizontal = 22.dp)
    ) {
        BackHeader(
            overline = "Familias",
            title = padre.nombre,
            onBackClick = onBack,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            // Encabezado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFFEFE9FF))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(AvatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(padre.iniciales, fontFamily = Poppins, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = AvatarFg)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(padre.nombre, fontFamily = Poppins, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140))
                    Text("${padre.parentesco} de ${hijosNombres.joinToString(", ")}", fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5B4B8A))
                }
                Text(
                    text = if (padre.conAcceso) "Con acceso" else "Sin acceso",
                    fontFamily = Poppins, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                    color = if (padre.conAcceso) Color(0xFF0F7A6C) else Color(0xFFC4432A),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (padre.conAcceso) Color(0xFFE8FBF6) else Color(0xFFFFEDE7))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            SeccionLabel("INFORMACIÓN")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoTarjeta("TELÉFONO", padre.telefono)
                InfoTarjeta("CORREO", padre.correo)
                InfoTarjeta("HIJOS INSCRITOS", hijosNombres.joinToString(", "))
            }

            SeccionLabel("ACCIONES")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (padre.conAcceso) {
                    BotonAccionPadre("Suspender su acceso a la app", Color(0xFFFFEDE7), Color(0xFFC4432A), onToggleAcceso)
                } else {
                    BotonAccionPadre("Reactivar su acceso a la app", Color(0xFFE8FBF6), Color(0xFF0F7A6C), onToggleAcceso)
                }
                BotonAccionPadre("Reenviar invitación", Color(0xFFF4F1FB), Color(0xFF4A4066), onReenviarInvitacion)
            }
        }
    }
}

@Composable
private fun InfoTarjeta(etiqueta: String, valor: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(22.dp), ambientColor = Color(0x0F2A2140), spotColor = Color(0x0F2A2140))
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(etiqueta, fontFamily = Poppins, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.1.sp, color = Color(0xFFA79BBF))
        Text(valor, fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2A2140))
    }
}

@Composable
private fun BotonAccionPadre(texto: String, bg: Color, fg: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
        contentPadding = PaddingValues(vertical = 15.dp),
        elevation = null
    ) {
        Text(texto, fontFamily = Poppins, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
