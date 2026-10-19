package mx.tec.codea.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.domain.AttendanceStatus
import mx.tec.codea.domain.Child
import mx.tec.codea.ui.components.BackHeader
import mx.tec.codea.ui.components.InitialsAvatar
import mx.tec.codea.ui.components.avatarColorAt
import mx.tec.codea.ui.theme.CodeaTheme

// el pase de lista: el primer paso del día, en "Mi día". toda la sala en una lista; se toca a cada niño que llegó.
// el que no se marca cuenta como falta (la API no tiene un tercer estado "sin pasar lista").
// cada toque se guarda al instante (y espera la conexión si no la hay), así que "Listo" solo cierra la pantalla.
@Composable
fun RollCallScreen(
    children: List<Child>,
    onPresentChange: (childId: String, present: Boolean) -> Unit,
    onMarkAllPresent: () -> Unit,
    onDoneClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // en orden alfabético y sin reordenarse al tocar: la lista no debe moverse bajo el dedo.
    val sorted = children.sortedBy { it.name }
    val presentCount = sorted.count { it.attendance == AttendanceStatus.PRESENT }

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                BackHeader(
                    overline = stringResource(R.string.nav_my_day),
                    title = stringResource(R.string.roll_call_title),
                    onBackClick = onBackClick,
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.roll_call_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = pluralStringResource(R.plurals.roll_call_card_progress, sorted.size, presentCount, sorted.size),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (presentCount < sorted.size) {
                            OutlinedButton(
                                onClick = onMarkAllPresent,
                                modifier = Modifier.heightIn(min = 48.dp),
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Text(stringResource(R.string.roll_call_mark_all), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (sorted.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.roll_call_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            }

            items(sorted, key = { it.id }) { child ->
                RollCallRow(
                    child = child,
                    avatarColor = avatarColorAt(children.indexOfFirst { it.id == child.id }.coerceAtLeast(0)),
                    onPresentChange = { onPresentChange(child.id, it) },
                )
            }
        }

        Button(
            onClick = onDoneClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 12.dp)
                .heightIn(min = 56.dp),
            shape = RoundedCornerShape(18.dp),
        ) {
            Text(
                text = stringResource(R.string.roll_call_done),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// el renglón entero es el botón: se puede tocar en cualquier parte, no solo en el círculo.
@Composable
private fun RollCallRow(
    child: Child,
    avatarColor: Color,
    onPresentChange: (Boolean) -> Unit,
) {
    val isPresent = child.attendance == AttendanceStatus.PRESENT
    val description = stringResource(
        if (isPresent) R.string.roll_call_present_description else R.string.roll_call_absent_description,
        child.name,
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isPresent) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, if (isPresent) Color.Transparent else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = isPresent, role = Role.Checkbox, onValueChange = onPresentChange)
            .semantics { contentDescription = description },
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 72.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InitialsAvatar(initials = child.initials, color = avatarColor, size = 48.dp)
            Text(
                text = child.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isPresent) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = if (isPresent) MaterialTheme.colorScheme.tertiary else Color.Transparent,
                            shape = CircleShape,
                        )
                        .border(
                            width = 2.dp,
                            color = if (isPresent) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isPresent) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiary)
                    }
                }
                Text(
                    text = stringResource(if (isPresent) R.string.my_room_present else R.string.my_room_absent),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isPresent) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun RollCallScreenPreview() {
    CodeaTheme {
        RollCallScreen(
            children = listOf(
                Child("1", "Ana López", AttendanceStatus.PRESENT, hasUnreadChat = false),
                Child("2", "Diego Álvarez", AttendanceStatus.ABSENT, hasUnreadChat = false),
                Child("3", "Sofía Márquez", AttendanceStatus.PRESENT, hasUnreadChat = false),
            ),
            onPresentChange = { _, _ -> },
            onMarkAllPresent = {},
            onDoneClick = {},
            onBackClick = {},
        )
    }
}
