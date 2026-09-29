package mx.tec.codea.ui.screens.roleselection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.navigation.Role
import mx.tec.codea.ui.theme.CodeaTheme

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (Role) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.role_selection_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 28.dp),
        )

        RoleButton(
            text = stringResource(R.string.role_teacher),
            onClick = { onRoleSelected(Role.TEACHER) },
        )

        RoleButton(
            text = stringResource(R.string.role_admin),
            onClick = { onRoleSelected(Role.ADMIN) },
            modifier = Modifier.padding(top = 12.dp),
        )

        RoleButton(
            text = stringResource(R.string.role_parent),
            onClick = { onRoleSelected(Role.PARENT) },
            modifier = Modifier.padding(top = 12.dp),
        )
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
        RoleSelectionScreen(onRoleSelected = {})
    }
}
