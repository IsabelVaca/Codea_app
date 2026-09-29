package mx.tec.codea.ui.screens.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
// import androidx.compose.foundation.layout.size
// import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.ui.components.ScreenHeader
import mx.tec.codea.ui.screens.myroom.Child
import mx.tec.codea.ui.screens.myroom.MyRoomSampleData
import mx.tec.codea.ui.theme.CodeaTheme

@Composable
fun ChatsScreen(
    selectedChild: Child? = null,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (selectedChild == null) {
        EmptyChatsScreen(modifier = modifier)
        return
    }

    ConversationScreen(
        child = selectedChild,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@Composable
private fun EmptyChatsScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.chats_empty_instruction),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ConversationScreen(
    child: Child,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val messages = ChatSampleData.messagesFor(child.id)
    val firstName = child.name.substringBefore(" ")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 16.dp)
            .imePadding(),
    ) {
        ChatHeader(
            childFirstName = firstName,
            onBackClick = onBackClick,
        )

        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.chats_no_messages),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                itemsIndexed(messages) { _, message ->
                    MessageBubble(message)
                }
            }
        }

        MessageComposer(childId = child.id)
    }
}

@Composable
private fun ChatHeader(
    childFirstName: String,
    onBackClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FilledTonalIconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.chats_back),
            )
        }

        ScreenHeader(
            overline = stringResource(
                R.string.chats_family_of,
                childFirstName,
            ),
            title = stringResource(R.string.chats_title),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
) {
    val containerColor =
        if (message.isFromTeacher) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        }

    val contentColor =
        if (message.isFromTeacher) {
            MaterialTheme.colorScheme.onTertiary
        } else {
            MaterialTheme.colorScheme.onSurface
        }

    val shape =
        if (message.isFromTeacher) {
            RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 6.dp,
            )
        } else {
            RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = 6.dp,
                bottomEnd = 20.dp,
            )
        }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            if (message.isFromTeacher) {
                Arrangement.End
            } else {
                Arrangement.Start
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .background(containerColor, shape)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = contentColor,
            )

            Text(
                text = if (message.isRead) {
                    stringResource(
                        R.string.chats_message_read,
                        message.time,
                    )
                } else {
                    message.time
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun MessageComposer(
    childId: String,
) {
    var messageText by rememberSaveable(childId) {
        mutableStateOf("")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(
            onClick = {
                // File selection will be added in a later delivery.
            },
            modifier = Modifier.background(
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                shape = RoundedCornerShape(14.dp),
            ),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.chats_attach),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }

        OutlinedTextField(
            value = messageText,
            onValueChange = { messageText = it },
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(stringResource(R.string.chats_message_hint))
            },
            shape = RoundedCornerShape(18.dp),
            maxLines = 3,
        )

        IconButton(
            onClick = {
                // Sending will be implemented when the chat has real behavior.
            },
            modifier = Modifier.background(
                color = MaterialTheme.colorScheme.tertiary,
                shape = RoundedCornerShape(14.dp),
            ),
        ) {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = stringResource(R.string.chats_send),
                tint = MaterialTheme.colorScheme.onTertiary,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ChatsScreenPreview() {
    CodeaTheme {
        ChatsScreen(
            selectedChild = MyRoomSampleData.children.first(),
        )
    }
}
