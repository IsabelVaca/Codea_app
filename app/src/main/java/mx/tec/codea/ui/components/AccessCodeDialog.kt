package mx.tec.codea.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
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
import mx.tec.codea.ui.theme.CodeaTheme

// el código de acceso de alguien recién dado de alta. la API lo da una sola vez (en la base solo queda su hash),
// así que este diálogo es la única oportunidad de copiarlo o compartirlo.
@Composable
fun AccessCodeDialog(notice: AccessCodeNotice, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val shareText = stringResource(R.string.access_code_share_text, notice.code)
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
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.access_code_close)) }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(send, null))
                },
            ) { Text(stringResource(R.string.access_code_share)) }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun AccessCodeDialogPreview() {
    CodeaTheme { AccessCodeDialog(AccessCodeNotice("Paola Sánchez", "K7QX-M4PZ", "24 oct"), onDismiss = {}) }
}
