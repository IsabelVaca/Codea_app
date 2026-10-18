package mx.tec.codea.ui.screens.roleselection

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.ui.navigation.Role
import mx.tec.codea.ui.theme.CodeaTheme

// solo se muestra a quien tiene más de un perfil (por ejemplo, docente y madre de familia). el servidor dice
// cuáles son (GET /me → roles); aquí solo se elige con cuál entrar. sin ninguno, la cuenta no sirve y solo
// se puede cerrar sesión.
@Composable
fun RoleSelectionScreen(
    roles: List<Role>,
    onRoleSelected: (Role) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.app_logo),
            contentDescription = "Logo Codea",
            modifier = Modifier
                .padding(bottom = 20.dp)
                .size(160.dp),
        )

        Text(
            text = stringResource(
                if (roles.isEmpty()) R.string.role_selection_no_roles else R.string.role_selection_title,
            ),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 28.dp),
        )

        roles.forEachIndexed { index, role ->
            RoleButton(
                text = stringResource(
                    when (role) {
                        Role.TEACHER -> R.string.role_teacher
                        Role.ADMIN -> R.string.role_admin
                        Role.PARENT -> R.string.role_parent
                    },
                ),
                onClick = { onRoleSelected(role) },
                modifier = Modifier.padding(top = if (index == 0) 0.dp else 12.dp),
            )
        }

        TextButton(onClick = onLogout, modifier = Modifier.padding(top = 20.dp)) {
            Text(stringResource(R.string.account_logout))
        }
    }
}

@Composable
private fun RoleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shadowElevation = 3.dp,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(
                horizontal = 20.dp,
                vertical = 22.dp,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RoleSelectionScreenPreview() {
    CodeaTheme {
        RoleSelectionScreen(roles = Role.entries, onRoleSelected = {}, onLogout = {})
    }
}
