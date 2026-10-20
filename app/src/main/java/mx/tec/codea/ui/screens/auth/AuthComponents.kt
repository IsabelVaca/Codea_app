package mx.tec.codea.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.domain.AuthError
import mx.tec.codea.domain.AuthValidator
import mx.tec.codea.ui.state.AuthFormState

// el marco de las pantallas de entrada: logo, título y el formulario, con scroll para pantallas chicas.
@Composable
fun AuthFrame(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            // sin fondo propio se veía el de la ventana (claro) con texto del tema oscuro.
            .background(MaterialTheme.colorScheme.background)
            // el login está fuera del Scaffold de la app, así que pone sus propios márgenes: barras del sistema y
            // teclado. con el teclado abierto el área se achica, todo sube y el campo que se escribe queda a la vista.
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.app_logo),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier
                .padding(bottom = 16.dp)
                .size(120.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )
        content()
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    enabled: Boolean = true,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction,
            capitalization = capitalization,
            autoCorrectEnabled = false,
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth(),
    )
}

// el ojito para ver lo que se escribe. el estado de "visible" se pierde al girar la pantalla a propósito:
// una contraseña no debería quedar a la vista por accidente.
@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
    enabled: Boolean = true,
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = stringResource(
                        if (visible) R.string.auth_password_hide else R.string.auth_password_show,
                    ),
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = !loading,
        shape = RoundedCornerShape(22.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
fun AuthTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(text = text, textAlign = TextAlign.Center)
    }
}

// el mensaje de error del formulario: el de las reglas locales o el que mandó la API.
@Composable
fun AuthErrorText(form: AuthFormState, modifier: Modifier = Modifier) {
    val text = form.localError?.let { authErrorText(it) } ?: form.remoteError
    if (text != null) FormErrorText(text, modifier)
}

@Composable
fun FormErrorText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
    )
}

@Composable
fun authErrorText(error: AuthError): String = when (error) {
    AuthError.EmailRequired -> stringResource(R.string.auth_error_email_required)
    AuthError.PasswordRequired -> stringResource(R.string.auth_error_password_required)
    AuthError.CodeRequired -> stringResource(R.string.auth_error_code_required)
    AuthError.PasswordTooShort -> stringResource(R.string.auth_error_password_too_short, AuthValidator.PASSWORD_MIN)
    AuthError.PasswordTooLong -> stringResource(R.string.auth_error_password_too_long, AuthValidator.PASSWORD_MAX)
    AuthError.PasswordIsEmail -> stringResource(R.string.auth_error_password_is_email)
    AuthError.PasswordsDontMatch -> stringResource(R.string.auth_error_passwords_dont_match)
    AuthError.CurrentPasswordRequired -> stringResource(R.string.auth_error_current_required)
}

// el campo del código guarda solo letras y números ("YZ5EPEKV") y lo dibuja con el guion ("YZ5E-PEKV").
// así el guion aparece solo al escribir, y pegar el código con o sin guion da lo mismo.
object CodeDashTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val shown = if (raw.length <= 4) raw else raw.take(4) + "-" + raw.drop(4)
        val mapping = object : OffsetMapping {
            // después de la cuarta letra hay un carácter más (el guion).
            override fun originalToTransformed(offset: Int): Int = if (offset <= 4 || raw.length <= 4) offset else offset + 1
            override fun transformedToOriginal(offset: Int): Int = if (offset <= 4) offset else (offset - 1).coerceAtMost(raw.length)
        }
        return TransformedText(AnnotatedString(shown), mapping)
    }
}
