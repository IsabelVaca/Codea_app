package mx.tec.codea.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.tec.codea.R
import mx.tec.codea.domain.ChatMessage
import mx.tec.codea.domain.Conversation
import mx.tec.codea.domain.MessageStatus
import mx.tec.codea.ui.components.BackHeader
import mx.tec.codea.ui.theme.CodeaTheme

// the chat tab. the teacher sees an inbox (one row per child) and opens a conversation;
// the parent goes straight to the conversation when there is only one child.
@Composable
fun ChatsScreen(
    conversations: List<Conversation>,
    loaded: Boolean,
    openConversation: Conversation?,
    messages: List<ChatMessage>,
    isParent: Boolean,
    onOpen: (Int) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSend: (String) -> Unit = {},
    onRetry: (String) -> Unit = {},
) {
    if (openConversation == null) {
        InboxScreen(
            conversations = conversations,
            loaded = loaded,
            isParent = isParent,
            onOpen = onOpen,
            modifier = modifier,
        )
        return
    }

    ConversationScreen(
        conversation = openConversation,
        messages = messages,
        isParent = isParent,
        onBackClick = onBackClick,
        onSend = onSend,
        onRetry = onRetry,
        modifier = modifier,
    )
}

@Composable
private fun InboxScreen(
    conversations: List<Conversation>,
    loaded: Boolean,
    isParent: Boolean,
    onOpen: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(
                if (isParent) R.string.chats_inbox_title_parent else R.string.chats_inbox_title,
            ),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        if (conversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(
                        if (loaded) R.string.chats_inbox_empty else R.string.chats_inbox_loading,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(conversations, key = { it.id }) { conversation ->
                ConversationRow(conversation, onClick = { onOpen(conversation.id) })
            }
        }
    }
}

@Composable
private fun ConversationRow(
    conversation: Conversation,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = conversation.childName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = conversation.lastText ?: stringResource(R.string.chats_no_messages_short),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            conversation.lastTime?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (conversation.unreadCount > 0) {
                Text(
                    text = conversation.unreadCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun ConversationScreen(
    conversation: Conversation,
    messages: List<ChatMessage>,
    isParent: Boolean,
    onBackClick: () -> Unit,
    onSend: (String) -> Unit,
    onRetry: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    // when a message arrives or is sent, the list goes to the bottom.
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 16.dp)
            .imePadding(),
    ) {
        BackHeader(
            overline = stringResource(
                if (isParent) R.string.chats_parent_of else R.string.chats_family_of,
                conversation.childName.substringBefore(" "),
            ),
            title = stringResource(if (isParent) R.string.chats_parent_title else R.string.chats_title),
            onBackClick = onBackClick,
            modifier = Modifier.padding(bottom = 12.dp),
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
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(messages, key = { it.clientId ?: it.id }) { message ->
                    MessageBubble(message, onRetry = onRetry)
                }
            }
        }

        MessageComposer(conversationId = conversation.id, onSend = onSend)
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    onRetry: (String) -> Unit,
) {
    val containerColor =
        if (message.isMine) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        }

    val contentColor =
        if (message.isMine) {
            MaterialTheme.colorScheme.onTertiary
        } else {
            MaterialTheme.colorScheme.onSurface
        }

    val shape =
        if (message.isMine) {
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
            if (message.isMine) {
                Arrangement.End
            } else {
                Arrangement.Start
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .background(containerColor, shape)
                .clickable(enabled = message.status == MessageStatus.FAILED && message.clientId != null) {
                    message.clientId?.let(onRetry)
                }
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
                text = when {
                    message.status == MessageStatus.SENDING -> stringResource(R.string.chats_sending)
                    message.status == MessageStatus.FAILED -> stringResource(R.string.chats_failed_retry)
                    message.isRead && message.isMine -> stringResource(R.string.chats_message_read, message.time)
                    else -> message.time
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
    conversationId: Int,
    onSend: (String) -> Unit,
) {
    var messageText by rememberSaveable(conversationId) {
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
                if (messageText.isNotBlank()) {
                    onSend(messageText)
                    messageText = ""
                }
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
    val conversation = Conversation(1, "Sofía Márquez", 1, "¿Puede recogerla su abuela hoy?", "11:20")
    CodeaTheme {
        ChatsScreen(
            conversations = listOf(conversation),
            loaded = true,
            openConversation = conversation,
            messages = listOf(
                ChatMessage(1, null, "Buenos días miss, ¿Sofía desayunó bien?", "9:02", isMine = false),
                ChatMessage(2, null, "Desayunó todo y se quedó contenta.", "9:14", isMine = true, isRead = true),
                ChatMessage(0, "x", "Gracias miss", "", isMine = false, status = MessageStatus.SENDING),
            ),
            isParent = false,
            onOpen = {},
            onBackClick = {},
        )
    }
}
