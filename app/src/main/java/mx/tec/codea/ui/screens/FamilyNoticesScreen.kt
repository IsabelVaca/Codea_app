package mx.tec.codea.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.domain.AttendanceStatus
import mx.tec.codea.domain.Child
import mx.tec.codea.domain.FamilyNote
import mx.tec.codea.domain.RoomRules
import mx.tec.codea.ui.components.BackHeader
import mx.tec.codea.ui.components.NoticeCard
import mx.tec.codea.ui.components.avatarColorAt
import mx.tec.codea.ui.theme.CodeaTheme

// "Avisos de casa": todas las notas que dejaron los papás hoy. arriba las que faltan por ver, abajo las que ya se vieron.
// es la misma tarjeta de Mi sala, para cuando hay más de tres avisos.
@Composable
fun FamilyNoticesScreen(
    children: List<Child>,
    seenNoteIds: Set<Int>,
    onBackClick: () -> Unit,
    onReply: (childId: String) -> Unit,
    onNoteSeen: (noteId: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val unseen = RoomRules.notices(children, seenNoteIds, onlyUnseen = true)
    val seen = RoomRules.notices(children, seenNoteIds, onlyUnseen = false).filter { it.note.id in seenNoteIds }
    fun colorOf(child: Child) = avatarColorAt(children.indexOfFirst { it.id == child.id }.coerceAtLeast(0))

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            BackHeader(
                title = stringResource(R.string.notices_title),
                onBackClick = onBackClick,
            )
        }

        if (unseen.isEmpty() && seen.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.notices_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            }
        }

        if (unseen.isNotEmpty()) {
            item { GroupTitle(stringResource(R.string.notices_unseen, unseen.size)) }
            items(unseen, key = { "unseen-${it.note.id}" }) { notice ->
                NoticeCard(
                    child = notice.child,
                    note = notice.note,
                    avatarColor = colorOf(notice.child),
                    seen = false,
                    onReply = { onReply(notice.child.id) },
                    onSeen = { onNoteSeen(notice.note.id) },
                    modifier = Modifier.animateItem(),
                )
            }
        }

        if (seen.isNotEmpty()) {
            item { GroupTitle(stringResource(R.string.notices_seen, seen.size)) }
            items(seen, key = { "seen-${it.note.id}" }) { notice ->
                NoticeCard(
                    child = notice.child,
                    note = notice.note,
                    avatarColor = colorOf(notice.child),
                    seen = true,
                    onReply = { onReply(notice.child.id) },
                    onSeen = {},
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun GroupTitle(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun FamilyNoticesScreenPreview() {
    CodeaTheme {
        FamilyNoticesScreen(
            children = listOf(
                Child("1", "Sofía Márquez", AttendanceStatus.PRESENT, hasUnreadChat = false, notes = listOf(FamilyNote(1, "Tiene tos leve.", "7:40"))),
                Child("2", "Mateo Ruiz", AttendanceStatus.ABSENT, hasUnreadChat = false, notes = listOf(FamilyNote(2, "Llega a las 10, va al doctor.", "7:55"))),
            ),
            seenNoteIds = setOf(2),
            onBackClick = {},
            onReply = {},
            onNoteSeen = {},
        )
    }
}
