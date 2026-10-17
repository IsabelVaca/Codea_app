package mx.tec.codea.ui.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.ui.state.AuthFormState
import mx.tec.codea.ui.theme.CodeaTheme

// iniciar sesión con correo y contraseña. la contraseña NO se guarda con rememberSaveable: no debe quedar
// en el estado guardado de la actividad.
@Composable
fun LoginScreen(
    form: AuthFormState,
    onLogin: (email: String, password: String) -> Unit,
    onGoToActivate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AuthFrame(
        title = stringResource(R.string.auth_login_title),
        subtitle = stringResource(R.string.auth_login_subtitle),
        modifier = modifier,
    ) {
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = stringResource(R.string.auth_email),
            keyboardType = KeyboardType.Email,
            enabled = !form.loading,
        )
        Spacer(Modifier.height(12.dp))
        PasswordField(
            value = password,
            onValueChange = { password = it },
            label = stringResource(R.string.auth_password),
            imeAction = ImeAction.Done,
            enabled = !form.loading,
        )
        AuthErrorText(form)
        Spacer(Modifier.height(20.dp))
        AuthPrimaryButton(
            text = stringResource(R.string.auth_login_button),
            onClick = { onLogin(email, password) },
            loading = form.loading,
        )
        AuthTextButton(
            text = stringResource(R.string.auth_login_to_activate),
            onClick = onGoToActivate,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = stringResource(R.string.auth_login_forgot),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    CodeaTheme { LoginScreen(form = AuthFormState(), onLogin = { _, _ -> }, onGoToActivate = {}) }
}
