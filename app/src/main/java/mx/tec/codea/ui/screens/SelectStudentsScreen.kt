package mx.tec.codea.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.data.ChildRepository
import mx.tec.codea.domain.AttendanceStatus
import mx.tec.codea.domain.Child
import mx.tec.codea.domain.ReportValidator
import mx.tec.codea.ui.components.BackHeader
import mx.tec.codea.ui.components.InfoBox
import mx.tec.codea.ui.components.InitialsAvatar
import mx.tec.codea.ui.components.avatarColorAt
import mx.tec.codea.ui.state.ReportFormUiState
import mx.tec.codea.ui.theme.CodeaTheme

// step 3 of the report: which children does it talk about? (screen 6 and e2 of the prototype).
@Composable
fun SelectStudentsScreen(
    uiState: ReportFormUiState,
    children: List<Child>,
    onChildToggle: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedChildren = children.filter { it.id in uiState.selectedChildIds }
    val selectedCount = selectedChildren.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackHeader(
            overline = stringResource(R.string.students_overline, stringResource(uiState.type.labelRes())),
            title = stringResource(R.string.students_title),
            onBackClick = onBackClick,
        )

        if (selectedCount == 0) {
            InfoBox(
                title = stringResource(R.string.students_none_title),
                body = stringResource(R.string.students_none_body),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                titleColor = MaterialTheme.colorScheme.onErrorContainer,
            )
        } else {
            InfoBox(
                title = pluralStringResource(R.plurals.students_selected, selectedCount, selectedCount),
                body = stringResource(R.string.students_families_notified),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        children.forEachIndexed { index, child ->
            StudentCard(
                child = child,
                avatarColor = avatarColorAt(index),
                isSelected = child.id in uiState.selectedChildIds,
                isEnabled = ReportValidator.canBeInReport(child),
                onClick = { onChildToggle(child.id) },
            )
        }

        InfoBox(
            title = stringResource(R.string.students_who_sees),
            body = whoSeesText(selectedChildren.map { it.firstName }),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleColor = MaterialTheme.colorScheme.onSurface,
        )

        Button(
            onClick = onSaveClick,
            enabled = uiState.canSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = pluralStringResource(R.plurals.students_save, selectedCount, selectedCount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        if (!uiState.canSave) {
            Text(
                text = stringResource(R.string.students_save_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// "familias de mateo y sofía · coordinación de la estancia. el grupo no lo ve."
@Composable
private fun whoSeesText(firstNames: List<String>): String {
    if (firstNames.isEmpty()) return stringResource(R.string.students_who_sees_none)
    val names = if (firstNames.size == 1) {
        firstNames.first()
    } else {
        stringResource(R.string.students_names_and, firstNames.dropLast(1).joinToString(", "), firstNames.last())
    }
    return pluralStringResource(R.plurals.students_who_sees_families, firstNames.size, names)
}

// one child. the whole card is the checkbox. absent children are grey and can not be picked.
@Composable
private fun StudentCard(
    child: Child,
    avatarColor: Color,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isEnabled) 1f else 0.5f),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            InitialsAvatar(initials = child.initials, color = avatarColor)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = child.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(
                        if (child.attendance == AttendanceStatus.PRESENT) R.string.my_room_present else R.string.my_room_absent,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            CheckSquare(isChecked = isSelected)
        }
    }
}

// a square that is filled with a check when the child is picked.
@Composable
private fun CheckSquare(isChecked: Boolean) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .size(28.dp)
            .then(
                if (isChecked) {
                    Modifier.background(MaterialTheme.colorScheme.primary, shape)
                } else {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.outline, shape)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isChecked) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun SelectStudentsScreenPreview() {
    CodeaTheme {
        SelectStudentsScreen(
            uiState = ReportFormUiState(
                description = "Se tropezaron jugando a las carreras.",
                selectedChildIds = setOf("mateo-torres", "sofia-marquez"),
            ),
            children = ChildRepository().getAll(),
            onChildToggle = {},
            onSaveClick = {},
            onBackClick = {},
        )
    }
}
