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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.R
import mx.tec.codea.domain.AttendanceStatus
import mx.tec.codea.domain.Child
import mx.tec.codea.domain.ReportValidator
import mx.tec.codea.ui.components.BackHeader
import mx.tec.codea.ui.components.CodeaColores
import mx.tec.codea.ui.components.InfoBox
import mx.tec.codea.ui.components.InitialsAvatar
import mx.tec.codea.ui.components.avatarColorAt
import mx.tec.codea.ui.state.ReportFormUiState
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Poppins

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
            title = stringResource(R.string.students_title),
            onBackClick = onBackClick,
        )

        if (selectedCount == 0) {
            InfoBox(
                title = stringResource(R.string.students_none_title),
                body = stringResource(R.string.students_none_body),
                containerColor = CodeaColores.RojoSuave,
                titleColor = CodeaColores.Rojo,
            )
        } else {
            InfoBox(
                title = pluralStringResource(R.plurals.students_selected, selectedCount, selectedCount),
                body = stringResource(R.string.students_families_notified),
                containerColor = CodeaColores.Lila,
                titleColor = CodeaColores.Morado,
            )
        }

        // sin pase de lista todos cuentan como ausentes y ninguno se puede elegir: se dice por qué.
        if (children.isNotEmpty() && children.none { ReportValidator.canBeInReport(it) }) {
            InfoBox(
                title = stringResource(R.string.roll_call_title),
                body = stringResource(R.string.students_nobody_present),
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f),
                titleColor = MaterialTheme.colorScheme.onSurface,
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
            containerColor = CodeaColores.Campo,
            titleColor = CodeaColores.Ink,
        )

        Button(
            onClick = onSaveClick,
            enabled = uiState.canSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = BotonVioleta(),
            elevation = ButtonDefaults.buttonElevation(0.dp),
        ) {
            Text(
                text = pluralStringResource(R.plurals.students_save, selectedCount, selectedCount),
                fontFamily = Poppins, fontSize = 15.sp, fontWeight = FontWeight.Bold,
            )
        }
        if (!uiState.canSave) {
            Text(
                text = stringResource(R.string.students_save_hint),
                fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.Gris,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// "familias de mateo y sofía y coordinación de la estancia. el grupo no lo ve."
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
    // tarjeta blanca como las del admin; la elegida en lila con borde morado.
    Surface(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isEnabled) 1f else 0.5f)
            .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x1A2A2140), spotColor = Color(0x1A2A2140)),
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) CodeaColores.Lila else Color.White,
        border = if (isSelected) BorderStroke(2.dp, CodeaColores.Morado) else null,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            InitialsAvatar(
                initials = child.initials,
                color = avatarColor,
                background = if (isSelected) Color.White else CodeaColores.Lila,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(child.name, fontFamily = Poppins, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                Text(
                    text = stringResource(
                        if (child.attendance == AttendanceStatus.PRESENT) R.string.my_room_present else R.string.my_room_absent,
                    ),
                    fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.GrisTexto,
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
                    Modifier.background(CodeaColores.Morado, shape)
                } else {
                    Modifier.border(2.dp, CodeaColores.Gris, shape)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isChecked) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
