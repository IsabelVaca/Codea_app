package mx.tec.codea.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.R
import mx.tec.codea.data.AccessCodeNotice
import mx.tec.codea.domain.Invite
import mx.tec.codea.ui.theme.CodeaTheme

// la invitación de alguien recién dado de alta. la API da el código una sola vez (en la base solo queda su hash),
// así que este diálogo es la única oportunidad de mandarlo. el botón principal abre WhatsApp con el mensaje listo
// (enlace que abre la app y, por si no abre, el correo y el código escritos); "Compartir" usa cualquier otra app.
@Composable
fun AccessCodeDialog(notice: AccessCodeNotice, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val message = if (notice.link != null) {
        stringResource(
            R.string.invite_message_with_link,
            notice.personName, notice.link, notice.email.orEmpty(), notice.code, notice.expiresAt,
        )
    } else {
        stringResource(
            R.string.invite_message_no_link,
            notice.personName, notice.email.orEmpty(), notice.code, notice.expiresAt,
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.access_code_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.access_code_body, notice.personName, notice.expiresAt),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = notice.code,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                )
                Button(
                    onClick = { sendByWhatsApp(context, notice.phone, message) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(stringResource(R.string.access_code_whatsapp), fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.access_code_close)) }
        },
        dismissButton = {
            TextButton(onClick = { share(context, message) }) { Text(stringResource(R.string.access_code_share)) }
        },
    )
}

// abre el chat de WhatsApp con la persona y el mensaje escrito. sin teléfono válido, o sin WhatsApp, cae al selector de compartir.
private fun sendByWhatsApp(context: Context, phone: String?, message: String) {
    val url = Invite.whatsAppUrl(phone, message)
    if (url == null) {
        share(context, message)
        return
    }
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        share(context, message)
    }
}

private fun share(context: Context, message: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(send, null))
}

@Preview(showBackground = true)
@Composable
private fun AccessCodeDialogPreview() {
    CodeaTheme {
        AccessCodeDialog(
            AccessCodeNotice("Paola Sánchez", "K7QX-M4PZ", "24 oct", "81 1234 5678", "paola@correo.test", null),
            onDismiss = {},
        )
    }
}
