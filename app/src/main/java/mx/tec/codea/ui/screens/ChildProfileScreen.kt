package mx.tec.codea.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.domain.AttendanceStatus
import mx.tec.codea.domain.Child
import mx.tec.codea.domain.ChildProfile
import mx.tec.codea.domain.ChildProfileRules
import mx.tec.codea.domain.Contact
import mx.tec.codea.domain.FamilyNote
import mx.tec.codea.domain.RoomRules
import mx.tec.codea.ui.components.BackHeader
import mx.tec.codea.ui.components.InitialsAvatar
import mx.tec.codea.ui.components.NoticeCard
import mx.tec.codea.ui.components.avatarColorAt
import mx.tec.codea.ui.theme.CodeaTheme

// la ficha de un niño: todo lo suyo en un solo lugar y las acciones que la docente necesita.
// los datos que vienen de Mi sala (asistencia, avisos, resumen) se pintan siempre; los que se piden al abrir
// (alergias, edad, contactos) cargan aparte y, si fallan, solo esa parte avisa y deja reintentar.
@Composable
fun ChildProfileScreen(
    child: Child,
    seenNoteIds: Set<Int>,
    roomName: String,
    avatarIndex: Int,
    profile: ChildProfile?,
    profileLoading: Boolean,
    profileFailed: Boolean,
    onBackClick: () -> Unit,
    onChatClick: () -> Unit,
    onSummaryClick: () -> Unit,
    onNoteSeen: (noteId: Int) -> Unit,
    onAttendanceFix: (AttendanceStatus) -> Unit,
    onRetryProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var fixing by rememberSaveable { mutableStateOf(false) }
    val isPresent = child.attendance == AttendanceStatus.PRESENT

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BackHeader(
            title = child.name,
            onBackClick = onBackClick,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            InitialsAvatar(initials = child.initials, color = avatarColorAt(avatarIndex), size = 56.dp)
            val line = listOfNotNull(profile?.ageText, roomName.ifBlank { null }).joinToString(" · ")
            if (line.isNotBlank()) {
                Text(
                    text = line,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (!isPresent) {
            Callout(
                title = stringResource(R.string.profile_absent_title),
                body = stringResource(R.string.profile_absent_body),
                container = MaterialTheme.colorScheme.surfaceVariant,
                content = MaterialTheme.colorScheme.onSurfaceVariant,
                withWarningIcon = false,
            )
        }

        // las alergias van arriba y bien marcadas: es lo que la docente no puede pasar por alto.
        profile?.allergies?.let { allergies ->
            Callout(
                title = stringResource(R.string.profile_allergies_title),
                body = allergies,
                container = MaterialTheme.colorScheme.errorContainer,
                content = MaterialTheme.colorScheme.onErrorContainer,
                withWarningIcon = true,
            )
        }

        // lo demás que la familia escribió en "Mi hijo": condiciones, intolerancias, medicamentos y otras indicaciones.
        profile?.health?.let { health ->
            listOf(
                R.string.profile_conditions_title to health.medicalConditions,
                R.string.profile_intolerances_title to health.intolerances,
                R.string.profile_medications_title to health.medications,
                R.string.profile_other_notes_title to health.otherNotes,
            ).filter { (_, text) -> text.isNotBlank() }.forEach { (title, text) ->
                Callout(
                    title = stringResource(title),
                    body = text,
                    container = MaterialTheme.colorScheme.primaryContainer,
                    content = MaterialTheme.colorScheme.onPrimaryContainer,
                    withWarningIcon = false,
                )
            }
        }

        // los avisos de hoy de este niño, completos. los vistos se quedan, ya sin botones.
        child.notes.sortedByDescending { it.id }.forEach { note ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionLabel(stringResource(R.string.profile_notes_title))
                NoticeCard(
                    child = child,
                    note = note,
                    avatarColor = avatarColorAt(avatarIndex),
                    seen = note.id in seenNoteIds,
                    onReply = onChatClick,
                    onSeen = { onNoteSeen(note.id) },
                )
            }
        }

        Button(
            onClick = onChatClick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            shape = RoundedCornerShape(18.dp),
        ) {
            Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.padding(end = 10.dp))
            Text(
                text = stringResource(R.string.profile_write_family),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        SectionLabel(stringResource(R.string.profile_today_title))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column {
                TodayRow(
                    label = stringResource(R.string.profile_summary_label),
                    value = if (child.hasSummaryToday) {
                        stringResource(R.string.profile_summary_done)
                    } else {
                        stringResource(R.string.profile_summary_pending)
                    },
                    actionText = if (child.hasSummaryToday) {
                        stringResource(R.string.profile_summary_edit)
                    } else {
                        stringResource(R.string.profile_summary_capture)
                    },
                    actionEnabled = isPresent,
                    onActionClick = onSummaryClick,
                )
                if (!isPresent) {
                    Text(
                        text = stringResource(R.string.profile_summary_blocked),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                TodayRow(
                    label = stringResource(R.string.profile_attendance_label),
                    value = if (isPresent) {
                        stringResource(R.string.profile_attendance_present)
                    } else {
                        stringResource(R.string.profile_attendance_absent)
                    },
                    actionText = stringResource(R.string.profile_attendance_fix),
                    actionEnabled = true,
                    onActionClick = { fixing = true },
                )
            }
        }

        ContactsSection(
            profile = profile,
            loading = profileLoading,
            failed = profileFailed,
            onRetry = onRetryProfile,
        )
    }

    if (fixing) {
        FixAttendanceDialog(
            childName = child.firstName,
            current = child.attendance,
            onDismiss = { fixing = false },
            onSave = { chosen ->
                fixing = false
                if (chosen != child.attendance) onAttendanceFix(chosen)
            },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun Callout(
    title: String,
    body: String,
    container: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color,
    withWarningIcon: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(container, RoundedCornerShape(18.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (withWarningIcon) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = content, modifier = Modifier.size(24.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = content,
            )
            Text(text = body, style = MaterialTheme.typography.bodyLarge, color = content)
        }
    }
}

@Composable
private fun TodayRow(
    label: String,
    value: String,
    actionText: String,
    actionEnabled: Boolean,
    onActionClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        TextButton(onClick = onActionClick, enabled = actionEnabled, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(actionText, fontWeight = FontWeight.Bold)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

// quién lo recoge y a quién llamar. son datos de contacto de la familia: solo se piden al abrir la ficha.
@Composable
private fun ContactsSection(
    profile: ChildProfile?,
    loading: Boolean,
    failed: Boolean,
    onRetry: () -> Unit,
) {
    when {
        loading -> Text(
            text = stringResource(R.string.profile_loading),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        failed || profile == null -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.profile_failed),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = onRetry, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.profile_retry), fontWeight = FontWeight.Bold)
            }
        }

        else -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (profile.guardians.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SectionLabel(stringResource(R.string.profile_family_title))
                    profile.guardians.forEach { ContactRow(it) }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SectionLabel(stringResource(R.string.profile_pickups_title))
                if (profile.pickups.isEmpty()) {
                    Text(
                        text = stringResource(R.string.profile_pickups_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                profile.pickups.forEach { ContactRow(it) }
            }
        }
    }
}

@Composable
private fun ContactRow(contact: Contact) {
    val context = LocalContext.current
    val dialable = ChildProfileRules.dialable(contact.phone)
    val noDialer = stringResource(R.string.profile_no_dialer)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = listOfNotNull(contact.relationship, contact.phone?.ifBlank { null }).joinToString(" · ")
                    .ifBlank { stringResource(R.string.profile_no_phone) },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (dialable != null) {
            OutlinedButton(
                onClick = { dial(context, dialable, noDialer) },
                modifier = Modifier.heightIn(min = 48.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(
                    Icons.Filled.Call,
                    contentDescription = stringResource(R.string.profile_call_to, contact.name),
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .size(18.dp),
                )
                Text(stringResource(R.string.profile_call), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// abre el marcador del teléfono con el número ya escrito; la docente decide si llama. no hace falta ningún permiso.
private fun dial(context: Context, phone: String, noDialerMessage: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, noDialerMessage, Toast.LENGTH_SHORT).show()
    }
}

// corregir una marca de asistencia. es un diálogo y no un botón siempre visible: hacen falta dos toques a propósito.
@Composable
private fun FixAttendanceDialog(
    childName: String,
    current: AttendanceStatus,
    onDismiss: () -> Unit,
    onSave: (AttendanceStatus) -> Unit,
) {
    var chosen by rememberSaveable { mutableStateOf(current) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.fix_title, childName), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.fix_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AttendanceOption(
                    text = stringResource(R.string.profile_attendance_present),
                    selected = chosen == AttendanceStatus.PRESENT,
                    onClick = { chosen = AttendanceStatus.PRESENT },
                )
                AttendanceOption(
                    text = stringResource(R.string.profile_attendance_absent),
                    selected = chosen == AttendanceStatus.ABSENT,
                    onClick = { chosen = AttendanceStatus.ABSENT },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(chosen) }, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.fix_save), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    )
}

@Composable
private fun AttendanceOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun ChildProfileScreenPreview() {
    CodeaTheme {
        ChildProfileScreen(
            child = Child(
                "1", "Sofía Márquez", AttendanceStatus.PRESENT, hasUnreadChat = false,
                notes = listOf(FamilyNote(1, "Hoy casi no desayunó y tiene un poco de tos.", "7:40")),
            ),
            seenNoteIds = emptySet(),
            roomName = "Maternal",
            avatarIndex = 0,
            profile = ChildProfile(
                allergies = "Nuez y lácteos",
                ageText = "2 años 4 meses",
                guardians = listOf(Contact("Laura Márquez", "Mamá", "81 1234 5678")),
                pickups = listOf(Contact("Rosa Pérez", "Abuela", null)),
            ),
            profileLoading = false,
            profileFailed = false,
            onBackClick = {},
            onChatClick = {},
            onSummaryClick = {},
            onNoteSeen = {},
            onAttendanceFix = {},
            onRetryProfile = {},
        )
    }
}
