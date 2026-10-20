package mx.tec.codea.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.data.todayLabel
import mx.tec.codea.domain.AttendanceStatus
import mx.tec.codea.domain.Child
import mx.tec.codea.domain.FamilyNote
import mx.tec.codea.domain.RoomFilter
import mx.tec.codea.domain.RoomNotice
import mx.tec.codea.domain.RoomRules
import mx.tec.codea.ui.theme.CodeaTheme

// "Mi sala": la sala de un vistazo, de lo más urgente a lo menos.
// 1) arriba: la sala, la fecha y cuántos niños hay hoy.
// 2) "De casa hoy": los avisos de los papás juntos en un solo bloque, para leerlos y contestarlos ahí mismo.
// 3) "Los niños": una barra de filtros y una ficha por niño en cuadrícula. tocar la ficha abre su ficha completa.
//    las fichas que esperan algo (un aviso por ver, un chat sin leer) llevan borde ámbar y salen primero.
// 4) "Ausentes hoy": plegado al final. el pase de lista se hace en "Mi día"; aquí no se marca asistencia.
// abajo, siempre a la vista, la barra de "Capturar resúmenes": es lo que más se hace al final del día.
// solo hay dos colores con significado: el violeta es lo que se toca y el ámbar es lo que espera a la docente.
// es una pantalla "tonta": recibe datos y avisa de los toques, quien la usa decide qué hacer.
private val ScreenPadding = 20.dp
private val GridGap = 12.dp
private val AvatarSize = 44.dp

// en pantallas anchas (tabletas) el contenido se queda en una columna centrada, para leerlo sin recorrer la vista de lado a lado.
private val MaxContentWidth = 640.dp

// ancho mínimo de una ficha: de ahí sale cuántas columnas caben (siempre dos o más).
private val MinTileWidth = 150.dp
// la altura de una ficha con el máximo de estados (aviso y resumen): así todas las de una fila miden lo mismo.
private val MinTileHeight = 190.dp
private val CaptureBarSpace = 112.dp
private val CardShape = RoundedCornerShape(16.dp)
private val ControlShape = RoundedCornerShape(12.dp)

// el gris de los textos secundarios: la tinta con algo de transparencia, así se lee igual en claro y en oscuro.
private val ColorScheme.muted: Color
    get() = onSurface.copy(alpha = 0.68f)

// lo que espera a la docente (un aviso por ver, un chat sin leer, algo sin enviar): ámbar suave.
private fun ColorScheme.attention(alpha: Float): Color = secondary.copy(alpha = alpha)

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
    val pendingSummaries = RoomRules.pendingSummaries(children).size
    val showCaptureBar = presentCount > 0 && pendingSummaries > 0

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        // los márgenes van como relleno de la cuadrícula, no como su ancho: así se desliza desde cualquier punto de la pantalla.
        val sideInset = ((maxWidth - MaxContentWidth) / 2).coerceAtLeast(0.dp) + ScreenPadding
        val contentWidth = minOf(maxWidth, MaxContentWidth) - ScreenPadding * 2
        val columns = ((contentWidth + GridGap) / (MinTileWidth + GridGap)).toInt().coerceAtLeast(2)

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = sideInset,
                top = 24.dp,
                end = sideInset,
                bottom = if (showCaptureBar) CaptureBarSpace else 32.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(GridGap),
            verticalArrangement = Arrangement.spacedBy(GridGap),
        ) {
            fullSpan("header") {
                RoomHeader(roomName = roomName, presentCount = presentCount, total = total)
            }

            if (pendingChanges > 0) {
                fullSpan("pending") { PendingChangesNote(count = pendingChanges) }
            }

            // con la falta por omisión, una sala sin pase de lista se vería vacía: se dice qué hacer.
            if (total > 0 && presentCount == 0) {
                fullSpan("nobody") { NobodyPresentCard(onRollCallClick = onRollCallClick) }
            }

            if (notices.isNotEmpty()) {
                fullSpan("notices-title") {
                    SectionHeading(text = stringResource(R.string.my_room_notices_title), modifier = Modifier.padding(top = 20.dp))
                }
                fullSpan("notices") {
                    NoticeGroup(
                        notices = notices.take(RoomRules.NOTICES_ON_HOME),
                        totalCount = notices.size,
                        onReply = onChatClick,
                        onSeen = onNoteSeen,
                        onSeeAll = onSeeAllNotices,
                    )
                }
            }

            if (children.isEmpty()) {
                fullSpan("empty") {
                    Text(
                        text = stringResource(R.string.my_room_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.muted,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            } else {
                if (presentCount > 0) {
                    fullSpan("children-title") {
                        SectionHeading(text = stringResource(R.string.my_room_children_title), modifier = Modifier.padding(top = 20.dp))
                    }
                    fullSpan("filters") {
                        RoomFilterBar(
                            selected = filter,
                            countOf = { RoomRules.count(children, it, seenNoteIds) },
                            onSelect = { filter = it },
                        )
                    }
                    if (shownChildren.isEmpty()) {
                        fullSpan("filter-empty") {
                            Text(
                                text = stringResource(R.string.my_room_filter_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.muted,
                                modifier = Modifier.padding(vertical = 12.dp),
                            )
                        }
                    }
                    items(shownChildren, key = { "child-${it.id}" }) { child ->
                        ChildTile(
                            child = child,
                            seenNoteIds = seenNoteIds,
                            onClick = { onChildClick(child.id) },
                            onChatClick = { onChatClick(child.id) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }

                if (absentChildren.isNotEmpty()) {
                    fullSpan("absent-title") {
                        AbsentHeader(
                            count = absentChildren.size,
                            open = absentOpen,
                            onClick = { absentOpen = !absentOpen },
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    if (absentOpen) {
                        fullSpan("absent-hint") {
                            Text(
                                text = stringResource(R.string.my_room_absent_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.muted,
                            )
                        }
                        items(absentChildren, key = { "absent-${it.id}" }) { child ->
                            ChildTile(
                                child = child,
                                seenNoteIds = seenNoteIds,
                                onClick = { onChildClick(child.id) },
                                onChatClick = { onChatClick(child.id) },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
            }
        }

        // resumen guiado: un niño tras otro, sin volver a la lista. solo si hay a quién capturar.
        if (showCaptureBar) {
            CaptureBar(
                count = pendingSummaries,
                onClick = onCaptureSummaries,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

// un elemento que ocupa todo el ancho de la cuadrícula.
private fun LazyGridScope.fullSpan(key: String, content: @Composable () -> Unit) {
    item(key = key, span = { GridItemSpan(maxLineSpan) }) { content() }
}

// la sala y la fecha a la izquierda; a la derecha, cuántos niños hay hoy.
@Composable
private fun RoomHeader(roomName: String, presentCount: Int, total: Int, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = if (roomName.isBlank()) {
                    stringResource(R.string.my_room_title_generic)
                } else {
                    stringResource(R.string.my_room_title, roomName)
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = todayLabel(),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.muted,
            )
        }
        if (total > 0) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.my_room_present_count, presentCount, total),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                )
                Text(
                    text = pluralStringResource(R.plurals.my_room_present_label, total),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.muted,
                )
            }
        }
    }
}

@Composable
private fun SectionHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
            .fillMaxWidth()
            .semantics { heading() },
    )
}

@Composable
private fun PendingChangesNote(count: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = ControlShape,
        color = MaterialTheme.colorScheme.attention(0.22f),
    ) {
        Text(
            text = pluralStringResource(R.plurals.my_room_pending_changes, count, count),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun NobodyPresentCard(onRollCallClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        color = colors.primaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.my_room_nobody_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onPrimaryContainer,
            )
            Text(
                text = stringResource(R.string.my_room_nobody_body),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onPrimaryContainer,
            )
            Spacer(Modifier.size(4.dp))
            Button(
                onClick = onRollCallClick,
                modifier = Modifier.heightIn(min = 48.dp),
                shape = ControlShape,
            ) {
                Text(stringResource(R.string.my_room_nobody_action), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// los avisos de casa juntos en un solo bloque, uno debajo de otro y separados por una línea.
// si hay más de los que caben aquí, al pie del bloque está "Ver todos".
@Composable
private fun NoticeGroup(
    notices: List<RoomNotice>,
    totalCount: Int,
    onReply: (childId: String) -> Unit,
    onSeen: (noteId: Int) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = CardShape,
        color = colors.attention(0.16f),
    ) {
        Column {
            notices.forEachIndexed { index, notice ->
                key(notice.note.id) {
                    if (index > 0) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = colors.onSurface.copy(alpha = 0.12f))
                    }
                    NoticeEntry(
                        child = notice.child,
                        note = notice.note,
                        onReply = { onReply(notice.child.id) },
                        onSeen = { onSeen(notice.note.id) },
                    )
                }
            }
            if (totalCount > notices.size) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = colors.onSurface.copy(alpha = 0.12f))
                TextButton(
                    onClick = onSeeAll,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                ) {
                    Text(stringResource(R.string.my_room_notices_see_all, totalCount), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// un aviso que un papá mandó desde casa: quién, a qué hora y el texto completo, para leerlo sin abrir otra pantalla.
// "Responder" abre el chat del niño; "Ya lo vi" lo quita de lo pendiente (solo en este teléfono).
@Composable
private fun NoticeEntry(
    child: Child,
    note: FamilyNote,
    onReply: () -> Unit,
    onSeen: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(start = 16.dp, top = 14.dp, end = 8.dp, bottom = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = child.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (note.time.isNotBlank()) {
                Text(
                    text = note.time,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.muted,
                )
            }
        }
        Text(
            text = note.text,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, end = 8.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onSeen, modifier = Modifier.heightIn(min = 48.dp), shape = ControlShape) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text(stringResource(R.string.notice_seen), fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onReply, modifier = Modifier.heightIn(min = 48.dp), shape = ControlShape) {
                Text(stringResource(R.string.notice_reply), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// los cuatro filtros en una sola barra: el número arriba y el nombre abajo. el elegido va elevado y con borde, no solo de otro color.
@Composable
private fun RoomFilterBar(selected: RoomFilter, countOf: (RoomFilter) -> Int, onSelect: (RoomFilter) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(colors.surfaceContainer, ControlShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        RoomFilter.entries.forEach { option ->
            val isSelected = option == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .heightIn(min = 60.dp)
                    .background(
                        color = if (isSelected) colors.surfaceContainerLowest else Color.Transparent,
                        shape = RoundedCornerShape(9.dp),
                    )
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(option) })
                    .then(if (isSelected) Modifier.border(1.5.dp, colors.primary, RoundedCornerShape(9.dp)) else Modifier)
                    .padding(horizontal = 2.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = countOf(option).toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) colors.primary else colors.onSurface,
                )
                Text(
                    text = filterLabel(option),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = if (isSelected) colors.onSurface else colors.muted,
                )
            }
        }
    }
}

@Composable
private fun filterLabel(filter: RoomFilter): String = when (filter) {
    RoomFilter.ALL -> stringResource(R.string.my_room_filter_all)
    RoomFilter.WITH_NOTE -> stringResource(R.string.my_room_filter_note)
    RoomFilter.WITH_CHAT -> stringResource(R.string.my_room_filter_chat)
    RoomFilter.NO_SUMMARY -> stringResource(R.string.my_room_filter_summary)
}

@Composable
private fun AbsentHeader(count: Int, open: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val muted = MaterialTheme.colorScheme.muted
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(onClick = onClick, role = Role.Button),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.my_room_absent_section, count),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = muted,
        )
        Icon(
            imageVector = if (open) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = muted,
        )
    }
}

// la ficha de un niño: arriba el avatar y el atajo al chat, luego el nombre y, abajo, lo que tiene pendiente.
// tocarla abre la ficha completa. la que espera algo lleva borde ámbar; la del niño ausente se ve apagada.
@Composable
private fun ChildTile(
    child: Child,
    seenNoteIds: Set<Int>,
    onClick: () -> Unit,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val isAbsent = child.attendance == AttendanceStatus.ABSENT
    val hasUnseenNote = RoomRules.unseenNotes(child, seenNoteIds).isNotEmpty()
    val needsAttention = hasUnseenNote || child.hasUnreadChat
    val openLabel = stringResource(R.string.my_room_open_child, child.name)
    val lastNames = child.name.substringAfter(" ", "")

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        color = if (isAbsent) Color.Transparent else colors.surfaceContainerLowest,
        border = if (needsAttention) {
            BorderStroke(1.5.dp, colors.secondary)
        } else {
            BorderStroke(1.dp, colors.outlineVariant)
        },
    ) {
        Column(
            modifier = Modifier
                .heightIn(min = MinTileHeight)
                .clickable(onClickLabel = openLabel, role = Role.Button, onClick = onClick)
                .padding(start = 14.dp, top = 12.dp, end = 6.dp, bottom = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ChildAvatar(initials = child.initials, absent = isAbsent)
                ChatButton(childName = child.name, hasUnread = child.hasUnreadChat, onClick = onChatClick)
            }

            // el primer nombre grande y, debajo, los apellidos; así se distinguen de un vistazo.
            Column(
                modifier = Modifier.padding(top = 8.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = child.firstName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isAbsent) colors.muted else colors.onSurface,
                )
                if (lastNames.isNotBlank()) {
                    Text(
                        text = lastNames,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = colors.muted,
                    )
                }
            }

            // el estado va con icono y texto, no solo con color. lo que pide atención va en negritas y oscuro; lo demás, tenue.
            Column(
                modifier = Modifier.padding(top = 10.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (isAbsent) {
                    StatusItem(text = stringResource(R.string.my_room_tag_absent), icon = null, color = colors.muted)
                }
                if (hasUnseenNote) {
                    StatusItem(
                        text = stringResource(R.string.my_room_tag_note),
                        icon = Icons.Filled.MailOutline,
                        color = colors.onSurface,
                        emphasized = true,
                    )
                } else if (child.hasNoteToday) {
                    StatusItem(
                        text = stringResource(R.string.my_room_tag_note_seen),
                        icon = Icons.Filled.Check,
                        color = colors.muted,
                        iconColor = colors.primary,
                    )
                }
                if (!isAbsent) {
                    if (child.hasSummaryToday) {
                        StatusItem(
                            text = stringResource(R.string.my_room_tag_summary_done),
                            icon = Icons.Filled.Check,
                            color = colors.muted,
                            iconColor = colors.primary,
                        )
                    } else {
                        StatusItem(
                            text = stringResource(R.string.my_room_tag_summary_pending),
                            icon = Icons.Filled.Edit,
                            color = colors.muted,
                        )
                    }
                }
            }
        }
    }
}

// las iniciales en un círculo de un solo tono para todos; el ausente va en gris.
@Composable
private fun ChildAvatar(initials: String, absent: Boolean) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(AvatarSize)
            .background(if (absent) colors.outlineVariant else colors.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (absent) colors.muted else colors.onPrimaryContainer,
        )
    }
}

@Composable
private fun StatusItem(
    text: String,
    icon: ImageVector?,
    color: Color,
    modifier: Modifier = Modifier,
    iconColor: Color = color,
    emphasized: Boolean = false,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Normal,
            color = color,
        )
    }
}

// el atajo al chat de la familia. sin mensajes por leer es un icono sencillo; con mensajes, va relleno de ámbar.
@Composable
private fun ChatButton(childName: String, hasUnread: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val description = if (hasUnread) {
        stringResource(R.string.my_room_chat_unread, childName)
    } else {
        stringResource(R.string.my_room_chat_with, childName)
    }
    // el área táctil es de 48 dp; el círculo que se ve es de 40 dp.
    IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(if (hasUnread) colors.attention(0.32f) else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (hasUnread) Icons.AutoMirrored.Filled.Chat else Icons.AutoMirrored.Outlined.Chat,
                contentDescription = description,
                tint = if (hasUnread) colors.onSurface else colors.muted,
            )
        }
    }
}

// la barra de abajo: no se mueve con la lista, para capturar resúmenes desde cualquier punto de la pantalla.
@Composable
private fun CaptureBar(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier.background(colors.background)) {
        HorizontalDivider(color = colors.outlineVariant)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenPadding, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Button(
                onClick = onClick,
                modifier = Modifier
                    .widthIn(max = MaxContentWidth - ScreenPadding * 2)
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = ControlShape,
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.my_room_capture_summaries, count),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun MyRoomPreviewContent() {
    CodeaTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
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
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun MyRoomScreenPreview() {
    MyRoomPreviewContent()
}

@Preview(showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MyRoomScreenDarkPreview() {
    MyRoomPreviewContent()
}
