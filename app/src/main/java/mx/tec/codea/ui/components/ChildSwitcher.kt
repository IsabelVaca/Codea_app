package mx.tec.codea.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.domain.ChildOption
import mx.tec.codea.ui.theme.CodeaTheme

// la tira para cambiar de hijo, arriba de las pestañas del padre. solo se dibuja con más de un hijo.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildSwitcher(
    children: List<ChildOption>,
    selectedId: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        children.forEach { child ->
            FilterChip(
                selected = child.id == selectedId,
                onClick = { onSelect(child.id) },
                label = { Text(child.firstName) },
                modifier = Modifier.semantics { contentDescription = "Ver a ${child.fullName}" },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChildSwitcherPreview() {
    CodeaTheme {
        ChildSwitcher(
            children = listOf(ChildOption(1, "Mateo", "Mateo Iglesias"), ChildOption(2, "Lucía", "Lucía Iglesias")),
            selectedId = 2,
            onSelect = {},
        )
    }
}
