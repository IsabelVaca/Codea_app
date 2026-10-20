package mx.tec.codea.ui.screens.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.domain.AuthError
import mx.tec.codea.domain.AuthValidator
import mx.tec.codea.ui.components.BackHeader
import mx.tec.codea.ui.screens.auth.FormErrorText
import mx.tec.codea.ui.screens.auth.PasswordField
import mx.tec.codea.ui.screens.auth.authErrorText
import mx.tec.codea.ui.theme.CodeaTheme

// "Mi cuenta": quién soy, cambiar la contraseña y cerrar sesión. la pantalla solo dibuja: el estado y las
// llamadas a la API viven en AccountViewModel.
@Composable
fun AccountScreen(
    name: String,
    email: String,
    children: List<String>,
    loadError: String?,
    changing: Boolean,
    localError: AuthError?,
    remoteError: String?,
    onChangePassword: (current: String, new: String, confirmation: String) -> Unit,
    onClearPasswordErrors: () -> Unit,
    onLogoutConfirm: () -> Unit,
    onBackClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    passwordChanged: Boolean = false,
    onPasswordChangedShown: () -> Unit = {},
) {
    var showChangeDialog by rememberSaveable { mutableStateOf(false) }
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (onBackClick != null) {
            BackHeader(
                title = stringResource(R.string.account_title),
                onBackClick = onBackClick,
            )
        } else {
            Text(
                text = stringResource(R.string.account_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(name.ifBlank { "…" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (email.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.account_email) + ": " + email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (children.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.account_children) + ": " + children.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
        if (loadError != null) FormErrorText(loadError)

        OutlinedButton(
            onClick = { showChangeDialog = true },
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                stringResource(R.string.account_change_password),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }
        Button(
            onClick = { showLogoutDialog = true },
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                stringResource(R.string.account_logout),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
    }

    if (showChangeDialog) {
        ChangePasswordDialog(
            changing = changing,
            localError = localError,
            remoteError = remoteError,
            onConfirm = onChangePassword,
            onDismiss = {
                showChangeDialog = false
                onClearPasswordErrors()
            },
        )
    }
    // la contraseña se cambió: se cierra el formulario (el ViewModel avisa con passwordChanged).
    if (passwordChanged && showChangeDialog) {
        showChangeDialog = false
        onPasswordChangedShown()
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.menu_logout_dialog_title)) },
            text = { Text(stringResource(R.string.account_logout_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutConfirm()
                    },
                ) { Text(stringResource(R.string.account_logout)) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.account_cancel))
                }
            },
        )
    }
}

// las contraseñas no se guardan con rememberSaveable: no deben quedar en el estado guardado de la actividad.
@Composable
private fun ChangePasswordDialog(
    changing: Boolean,
    localError: AuthError?,
    remoteError: String?,
    onConfirm: (current: String, new: String, confirmation: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var current by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!changing) onDismiss() },
        title = { Text(stringResource(R.string.account_change_password)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    stringResource(R.string.account_change_password_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                PasswordField(current, { current = it }, stringResource(R.string.account_password_current), enabled = !changing)
                PasswordField(new, { new = it }, stringResource(R.string.auth_password_new), enabled = !changing)
                PasswordField(
                    confirmation,
                    { confirmation = it },
                    stringResource(R.string.auth_password_confirm),
                    imeAction = ImeAction.Done,
                    enabled = !changing,
                )
                Text(
                    stringResource(R.string.auth_password_rule, AuthValidator.PASSWORD_MIN),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val error = localError?.let { authErrorText(it) } ?: remoteError
                if (error != null) FormErrorText(error)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(current, new, confirmation) }, enabled = !changing) {
                Text(stringResource(R.string.account_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !changing) { Text(stringResource(R.string.account_cancel)) }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun AccountScreenPreview() {
    CodeaTheme {
        AccountScreen(
            name = "Laura Iglesias",
            email = "laura.iglesias@oruga.test",
            children = listOf("Mateo Iglesias"),
            loadError = null,
            changing = false,
            localError = null,
            remoteError = null,
            onChangePassword = { _, _, _ -> },
            onClearPasswordErrors = {},
            onLogoutConfirm = {},
            onBackClick = {},
        )
    }
}
