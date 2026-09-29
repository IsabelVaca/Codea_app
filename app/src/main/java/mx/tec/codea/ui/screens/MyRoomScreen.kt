package mx.tec.codea.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import androidx.compose.ui.res.stringResource
import mx.tec.codea.data.ChildRepository
import mx.tec.codea.domain.AttendanceStatus
import mx.tec.codea.domain.Child
import mx.tec.codea.ui.components.ScreenHeader
import mx.tec.codea.ui.theme.CodeaTheme

// the "mi sala" screen: the children of the room, their attendance and a chat button.
// it copies screen 1 of "chatear con los papás" in the prototype.
// the attendance lives in the shared view model, so the report flow sees it too.
@Composable
fun MyRoomScreen(
    children: List<Child>,
    onAttendanceChange: (childId: String, status: AttendanceStatus) -> Unit,
    onChatClick: (childId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // derived values: we count them every time, so they are never out of date.
    val presentCount = children.count { it.attendance == AttendanceStatus.PRESENT }
    val unreadCount = children.count { it.hasUnreadChat }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenHeader(
                overline = stringResource(R.string.my_room_overline),
                title = stringResource(R.string.my_room_title),
            )
        }

        item {
            RoomSummary(
                presentCount = presentCount,
                unreadCount = unreadCount,
            )
        }

        items(
            items = children,
            key = { it.id },
        ) { child ->
            ChildCard(
                child = child,
                onAttendanceSelected = { newStatus -> onAttendanceChange(child.id, newStatus) },
                onChatClick = { onChatClick(child.id) },
            )
        }
    }
}

@Composable
private fun RoomSummary(
    presentCount: Int,
    unreadCount: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        SummaryCard(
            value = presentCount,
            label = stringResource(R.string.my_room_present_count),
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            valueColor = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f),
        )

        SummaryCard(
            value = unreadCount,
            label = stringResource(R.string.my_room_unread_count),
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
            valueColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SummaryCard(
    value: Int,
    label: String,
    containerColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(containerColor, RoundedCornerShape(22.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ChildCard(
    child: Child,
    onAttendanceSelected: (AttendanceStatus) -> Unit,
    onChatClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Text(
                    text = child.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    AttendanceButton(
                        text = stringResource(R.string.my_room_present),
                        selected = child.attendance == AttendanceStatus.PRESENT,
                        selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                        unselectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        selectedContentColor = MaterialTheme.colorScheme.onTertiary,
                        unselectedContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        onClick = {
                            onAttendanceSelected(AttendanceStatus.PRESENT)
                        },
                        modifier = Modifier.weight(1f),
                    )

                    AttendanceButton(
                        text = stringResource(R.string.my_room_absent),
                        selected = child.attendance == AttendanceStatus.ABSENT,
                        selectedContainerColor = MaterialTheme.colorScheme.error,
                        unselectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedContentColor = MaterialTheme.colorScheme.onError,
                        unselectedContentColor = MaterialTheme.colorScheme.onErrorContainer,
                        onClick = {
                            onAttendanceSelected(AttendanceStatus.ABSENT)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            ChatButton(
                hasUnreadMessages = child.hasUnreadChat,
                onClick = onChatClick,
            )
        }
    }
}

@Composable
private fun AttendanceButton(
    text: String,
    selected: Boolean,
    selectedContainerColor: Color,
    unselectedContainerColor: Color,
    selectedContentColor: Color,
    unselectedContentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) selectedContainerColor else unselectedContainerColor,
        contentColor = if (selected) selectedContentColor else unselectedContentColor,
        border = if (selected) {
            null
        } else {
            BorderStroke(
                width = 1.dp,
                color = unselectedContentColor.copy(alpha = 0.25f),
            )
        },
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun ChatButton(
    hasUnreadMessages: Boolean,
    onClick: () -> Unit,
) {
    Box {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.my_room_chat),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }

        if (hasUnreadMessages) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(50),
                    ),
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun MyRoomScreenPreview() {
    CodeaTheme {
        MyRoomScreen(
            children = ChildRepository().getAll(),
            onAttendanceChange = { _, _ -> },
            onChatClick = {},
        )
    }
}
