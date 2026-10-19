package mx.tec.codea.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.domain.AttendanceStatus
import mx.tec.codea.domain.Child
import mx.tec.codea.domain.FamilyNote
import mx.tec.codea.ui.theme.CodeaTheme

// un aviso que un papá mandó desde casa, con su texto completo para leerlo sin abrir otra pantalla.
// "Responder" abre el chat del niño; "Ya lo vi" lo quita de lo pendiente (solo en este teléfono).
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = if (seen) {
            MaterialTheme.colorScheme.surfaceContainerLow
        } else {
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
        },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                InitialsAvatar(initials = child.initials, color = avatarColor, size = 40.dp)
                Text(
                    text = if (note.time.isBlank()) {
                        child.name
                    } else {
                        stringResource(R.string.notice_from, child.name, note.time)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Text(
                text = note.text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (!seen) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onReply,
                        modifier = Modifier.heightIn(min = 48.dp),
                        shape = RoundedCornerShape(14.dp),
                        // sobre el fondo ámbar el lila por omisión casi no se lee: se pinta con el color del texto.
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)),
                    ) {
                        Text(stringResource(R.string.notice_reply), fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onSeen,
                        modifier = Modifier.heightIn(min = 48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                        ),
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                        Text(stringResource(R.string.notice_seen), fontWeight = FontWeight.Bold)
                    }
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
