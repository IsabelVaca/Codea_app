package mx.tec.codea.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.R
import mx.tec.codea.domain.AttendanceStatus
import mx.tec.codea.domain.Child
import mx.tec.codea.domain.FamilyNote
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Poppins

// un aviso que un papá mandó desde casa, con su texto completo para leerlo sin abrir otra pantalla.
// "Responder" abre el chat del niño; "Ya lo vi" lo quita de lo pendiente (solo en este teléfono).
// tarjeta blanca como las del admin; si falta por ver lleva el borde ámbar de lo pendiente (como en Mi sala).
// si el aviso ya se vio, los botones se ocultan y la tarjeta queda tranquila (se usa en "Avisos de casa").
@Composable
fun NoticeCard(
    child: Child,
    note: FamilyNote,
    avatarColor: androidx.compose.ui.graphics.Color,
    seen: Boolean,
    onReply: () -> Unit,
    onSeen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val forma = RoundedCornerShape(22.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, forma, ambientColor = Color(0x1A2A2140), spotColor = Color(0x1A2A2140))
            .clip(forma)
            .background(CodeaColores.Tarjeta)
            .then(
                if (seen) Modifier.border(1.dp, CodeaColores.Borde, forma)
                else Modifier.border(2.dp, MaterialTheme.colorScheme.secondary, forma),
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InitialsAvatar(initials = child.initials, color = avatarColor, size = 40.dp)
            // el nombre y abajo la hora, sin "·".
            Column {
                Text(child.name, fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = CodeaColores.Ink)
                if (note.time.isNotBlank()) {
                    Text(note.time, fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.Gris)
                }
            }
        }

        Text(
            text = note.text,
            fontFamily = Poppins, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium, color = CodeaColores.Ink,
        )

        if (!seen) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onReply,
                    modifier = Modifier.heightIn(min = 44.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CodeaColores.Lila, contentColor = CodeaColores.Morado),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                ) {
                    Text(stringResource(R.string.notice_reply), fontFamily = Poppins, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onSeen,
                    modifier = Modifier.heightIn(min = 44.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CodeaColores.Violeta, contentColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    Text(stringResource(R.string.notice_seen), fontFamily = Poppins, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoticeCardPreview() {
    CodeaTheme {
        NoticeCard(
            child = Child("1", "Sofía Márquez", AttendanceStatus.PRESENT, hasUnreadChat = false),
            note = FamilyNote(1, "Hoy casi no desayunó y tiene un poco de tos. Si le sube, avísenme.", "7:40"),
            avatarColor = avatarColorAt(0),
            seen = false,
            onReply = {},
            onSeen = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
