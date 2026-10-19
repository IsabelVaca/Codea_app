package mx.tec.codea.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.tec.codea.R
import mx.tec.codea.data.todayLabel
import mx.tec.codea.domain.AttendanceStatus
import mx.tec.codea.domain.Child
import mx.tec.codea.domain.FamilyNote
import mx.tec.codea.domain.RoomFilter
import mx.tec.codea.domain.RoomRules
import mx.tec.codea.ui.components.InitialsAvatar
import mx.tec.codea.ui.components.NoticeCard
import mx.tec.codea.ui.components.ScreenHeader
import mx.tec.codea.ui.components.avatarColorAt
import mx.tec.codea.ui.theme.CodeaTheme

// "Mi sala": la sala de un vistazo, en una sola columna.
// 1) "De casa hoy": los avisos que mandaron los papás, para leerlos aquí mismo.
// 2) "Los niños": quién está, y qué tiene pendiente cada uno. tocar un renglón abre su ficha.
// 3) "Ausentes hoy": plegado al final. el pase de lista se hace en "Mi día"; aquí no se marca asistencia.
// es una pantalla "tonta": recibe datos y avisa de los toques, quien la usa decide qué hacer.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MyRoomScreen(
    children: List<Child>,
    seenNoteIds: Set<Int>,
    onChildClick: (childId: String) -> Unit,
    onChatClick: (childId: String) -> Unit,
    onNoteSeen: (noteId: Int) -> Unit,
    onSeeAllNotices: () -> Unit,
    onRollCallClick: () -> Unit,
    onCaptureSummaries: () -> Unit,
    modifier: Modifier = Modifier,
    // el nombre real de la sala de la docente; vacío mientras carga.
    roomName: String = "",
    // marcas de asistencia que esperan conexión para llegar al servidor.
    pendingChanges: Int = 0,
) {
    var filter by rememberSaveable { mutableStateOf(RoomFilter.ALL) }
    var absentOpen by rememberSaveable { mutableStateOf(false) }

    // se calculan en cada pintada, así nunca quedan desactualizados.
    val (presentCount, total) = RoomRules.presentOutOf(children)
    val notices = RoomRules.notices(children, seenNoteIds, onlyUnseen = true)
    val shownChildren = RoomRules.filtered(children, filter, seenNoteIds)
    val absentChildren = RoomRules.absent(children)
    // el color del avatar sale de la posición en la lista completa: no cambia cuando la lista se reordena.
    fun colorOf(child: Child) = avatarColorAt(children.indexOfFirst { it.id == child.id }.coerceAtLeast(0))

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                ScreenHeader(
                    overline = stringResource(R.string.my_room_overline),
                    title = if (roomName.isBlank()) {
                        stringResource(R.string.my_room_title_generic)
                    } else {
                        stringResource(R.string.my_room_title, roomName)
                    },
                )
                if (total > 0) {
                    Text(
                        text = pluralStringResource(R.plurals.my_room_today_line, total, todayLabel(), presentCount, total),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (pendingChanges > 0) {
            item {
                Text(
                    text = pluralStringResource(R.plurals.my_room_pending_changes, pendingChanges, pendingChanges),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }
        }

        // con la falta por omisión, una sala sin pase de lista se vería vacía: se dice qué hacer.
        if (total > 0 && presentCount == 0) {
            item {
                NobodyPresentCard(onRollCallClick = onRollCallClick)
            }
        }

        if (notices.isNotEmpty()) {
            item {
                SectionTitle(
                    text = stringResource(R.string.my_room_notices_title),
                    actionText = if (notices.size > RoomRules.NOTICES_ON_HOME) {
                        stringResource(R.string.my_room_notices_see_all, notices.size)
                    } else {
                        null
                    },
                    onActionClick = onSeeAllNotices,
                )
            }
            items(notices.take(RoomRules.NOTICES_ON_HOME), key = { "notice-${it.note.id}" }) { notice ->
                NoticeCard(
                    child = notice.child,
                    note = notice.note,
                    avatarColor = colorOf(notice.child),
                    seen = false,
                    onReply = { onChatClick(notice.child.id) },
                    onSeen = { onNoteSeen(notice.note.id) },
                    modifier = Modifier.animateItem(),
                )
            }
        }

        if (children.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.my_room_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            }
        } else {
            if (presentCount > 0) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(RoomFilter.entries, key = { it.name }) { option ->
                            FilterChip(
                                selected = filter == option,
                                onClick = { filter = option },
                                label = { Text(filterLabel(option, RoomRules.count(children, option, seenNoteIds))) },
                            )
                        }
                    }
                }

                // resumen guiado: un niño tras otro, sin volver a la lista. solo si hay a quién capturar.
                val pendingSummaries = RoomRules.pendingSummaries(children).size
                if (pendingSummaries > 0) {
                    item {
                        FilledTonalButton(
                            onClick = onCaptureSummaries,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.padding(end = 8.dp).size(18.dp))
                            Text(
                                text = stringResource(R.string.my_room_capture_summaries, pendingSummaries),
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                item {
                    SectionTitle(text = stringResource(R.string.my_room_children_title))
                }

                if (shownChildren.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.my_room_filter_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp),
                        )
                    }
                }
                items(shownChildren, key = { "child-${it.id}" }) { child ->
                    ChildRow(
                        child = child,
                        seenNoteIds = seenNoteIds,
                        avatarColor = colorOf(child),
                        onClick = { onChildClick(child.id) },
                        onChatClick = { onChatClick(child.id) },
                        modifier = Modifier.animateItem(),
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }

            if (absentChildren.isNotEmpty()) {
                item {
                    AbsentHeader(
                        count = absentChildren.size,
                        open = absentOpen,
                        onClick = { absentOpen = !absentOpen },
                    )
                }
                if (absentOpen) {
                    item {
                        Text(
                            text = stringResource(R.string.my_room_absent_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    items(absentChildren, key = { "absent-${it.id}" }) { child ->
                        ChildRow(
                            child = child,
                            seenNoteIds = seenNoteIds,
                            avatarColor = colorOf(child),
                            onClick = { onChildClick(child.id) },
                            onChatClick = { onChatClick(child.id) },
                            modifier = Modifier.animateItem(),
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun filterLabel(filter: RoomFilter, count: Int): String = when (filter) {
    RoomFilter.ALL -> stringResource(R.string.my_room_filter_all, count)
    RoomFilter.WITH_NOTE -> stringResource(R.string.my_room_filter_note, count)
    RoomFilter.WITH_CHAT -> stringResource(R.string.my_room_filter_chat, count)
    RoomFilter.NO_SUMMARY -> stringResource(R.string.my_room_filter_summary, count)
}

@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (actionText != null) {
            TextButton(onClick = onActionClick, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(actionText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun NobodyPresentCard(onRollCallClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.my_room_nobody_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = stringResource(R.string.my_room_nobody_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Button(
            onClick = onRollCallClick,
            modifier = Modifier.heightIn(min = 48.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(stringResource(R.string.my_room_nobody_action), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AbsentHeader(count: Int, open: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick, role = Role.Button),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.my_room_absent_section, count),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Icon(
            imageVector = if (open) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// un renglón de la lista: avatar, nombre y, debajo, lo que tiene pendiente. a la derecha, el atajo al chat.
// tocar el renglón abre la ficha del niño. un niño ausente se ve atenuado.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChildRow(
    child: Child,
    seenNoteIds: Set<Int>,
    avatarColor: Color,
    onClick: () -> Unit,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isAbsent = child.attendance == AttendanceStatus.ABSENT
    val hasUnseenNote = RoomRules.unseenNotes(child, seenNoteIds).isNotEmpty()
    val openLabel = stringResource(R.string.my_room_open_child, child.name)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clickable(onClickLabel = openLabel, role = Role.Button, onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InitialsAvatar(
            initials = child.initials,
            color = if (isAbsent) MaterialTheme.colorScheme.outline else avatarColor,
            size = 48.dp,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = child.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isAbsent) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (isAbsent) {
                    Tag(text = stringResource(R.string.my_room_tag_absent), icon = null, tone = TagTone.QUIET)
                }
                if (hasUnseenNote) {
                    Tag(text = stringResource(R.string.my_room_tag_note), icon = Icons.Filled.MailOutline, tone = TagTone.ATTENTION)
                } else if (child.hasNoteToday) {
                    Tag(text = stringResource(R.string.my_room_tag_note_seen), icon = Icons.Filled.Check, tone = TagTone.QUIET)
                }
                if (!isAbsent) {
                    if (child.hasSummaryToday) {
                        Tag(text = stringResource(R.string.my_room_tag_summary_done), icon = Icons.Filled.Check, tone = TagTone.DONE)
                    } else {
                        Tag(text = stringResource(R.string.my_room_tag_summary_pending), icon = Icons.Filled.Edit, tone = TagTone.QUIET)
                    }
                }
            }
        }

        ChatShortcut(
            childName = child.name,
            hasUnread = child.hasUnreadChat,
            onClick = onChatClick,
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
        )
    }
}

@Composable
private fun ChatShortcut(childName: String, hasUnread: Boolean, onClick: () -> Unit) {
    val description = if (hasUnread) {
        stringResource(R.string.my_room_chat_unread, childName)
    } else {
        stringResource(R.string.my_room_chat_with, childName)
    }
    Box {
        IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.Filled.Chat,
                contentDescription = description,
                tint = if (hasUnread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (hasUnread) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .size(12.dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape),
            )
        }
    }
}

private enum class TagTone { ATTENTION, DONE, QUIET }

// una etiqueta chica: el estado va con icono y texto, no solo con color (también se entiende en modo oscuro).
@Composable
private fun Tag(text: String, icon: ImageVector?, tone: TagTone) {
    val colors = MaterialTheme.colorScheme
    val (container, content) = when (tone) {
        TagTone.ATTENTION -> colors.secondary.copy(alpha = 0.24f) to colors.onSurface
        TagTone.DONE -> colors.tertiaryContainer to colors.onTertiaryContainer
        TagTone.QUIET -> colors.surfaceVariant to colors.onSurfaceVariant
    }
    Row(
        modifier = Modifier
            .background(container, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = content, modifier = Modifier.size(14.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = content,
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun MyRoomScreenPreview() {
    CodeaTheme {
        MyRoomScreen(
            children = listOf(
                Child(
                    "1", "Sofía Márquez", AttendanceStatus.PRESENT, hasUnreadChat = true,
                    notes = listOf(FamilyNote(1, "Hoy casi no desayunó y tiene un poco de tos.", "7:40")),
                ),
                Child("2", "Mateo Ruiz", AttendanceStatus.PRESENT, hasUnreadChat = false, hasSummaryToday = true),
                Child("3", "Ana López", AttendanceStatus.PRESENT, hasUnreadChat = false),
                Child("4", "Diego Álvarez", AttendanceStatus.ABSENT, hasUnreadChat = false),
            ),
            seenNoteIds = emptySet(),
            onChildClick = {},
            onChatClick = {},
            onNoteSeen = {},
            onSeeAllNotices = {},
            onRollCallClick = {},
            onCaptureSummaries = {},
            roomName = "Maternal",
        )
    }
}
