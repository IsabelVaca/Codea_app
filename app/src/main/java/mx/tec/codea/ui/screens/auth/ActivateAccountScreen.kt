package mx.tec.codea.ui.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.domain.AuthValidator
import mx.tec.codea.ui.state.AuthFormState
import mx.tec.codea.ui.theme.CodeaTheme

// primer acceso: correo, el código que dio la dirección y la contraseña nueva. al terminar bien, la sesión
// queda abierta y CodeaApp cambia de pantalla.
@Composable
fun ActivateAccountScreen(
    form: AuthFormState,
    onActivate: (email: String, code: String, password: String, confirmation: String) -> Unit,
    onGoToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    // con un enlace de invitación el correo y el código llegan puestos y solo falta la contraseña.
    initialEmail: String = "",
    initialCode: String = "",
) {
    var email by rememberSaveable { mutableStateOf(initialEmail) }
    var code by rememberSaveable { mutableStateOf(initialCode) }
    // si llega otro enlace con la pantalla abierta, se reemplaza lo escrito.
    LaunchedEffect(initialEmail, initialCode) {
        if (initialEmail.isNotBlank()) email = initialEmail
        if (initialCode.isNotBlank()) code = initialCode
    }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }

    AuthFrame(
        title = stringResource(R.string.auth_activate_title),
        subtitle = stringResource(R.string.auth_activate_subtitle),
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
        AuthTextField(
            value = code,
            onValueChange = { code = it.uppercase() },
            label = stringResource(R.string.auth_code),
            enabled = !form.loading,
        )
        Spacer(Modifier.height(12.dp))
        PasswordField(
            value = password,
            onValueChange = { password = it },
            label = stringResource(R.string.auth_password_new),
            enabled = !form.loading,
        )
        Spacer(Modifier.height(12.dp))
        PasswordField(
            value = confirmation,
            onValueChange = { confirmation = it },
            label = stringResource(R.string.auth_password_confirm),
            imeAction = ImeAction.Done,
            enabled = !form.loading,
        )
        Text(
            text = stringResource(R.string.auth_password_rule, AuthValidator.PASSWORD_MIN),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )
        AuthErrorText(form)
        Spacer(Modifier.height(20.dp))
        AuthPrimaryButton(
            text = stringResource(R.string.auth_activate_button),
            onClick = { onActivate(email, code, password, confirmation) },
            loading = form.loading,
        )
        AuthTextButton(
            text = stringResource(R.string.auth_back_to_login),
            onClick = onGoToLogin,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ActivateAccountScreenPreview() {
    CodeaTheme {
        ActivateAccountScreen(form = AuthFormState(), onActivate = { _, _, _, _ -> }, onGoToLogin = {})
    }
}
