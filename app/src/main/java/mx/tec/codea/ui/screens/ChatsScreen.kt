package mx.tec.codea.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import mx.tec.codea.data.ImageCompressor
import mx.tec.codea.ui.components.rememberCapturePhoto
import mx.tec.codea.ui.components.rememberPickPhoto
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import mx.tec.codea.R
import mx.tec.codea.domain.ChatMessage
import mx.tec.codea.domain.ChatRules
import mx.tec.codea.domain.Conversation
import mx.tec.codea.domain.MessageStatus
import mx.tec.codea.ui.components.BackHeader
import mx.tec.codea.ui.components.CodeaColores
import mx.tec.codea.ui.components.InitialsAvatar
import mx.tec.codea.ui.components.Pastilla
import mx.tec.codea.ui.theme.CodeaTheme
import mx.tec.codea.ui.theme.Poppins
import mx.tec.codea.ui.components.ScreenHeader

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
    onSendPhoto: (Int, ByteArray, String) -> Unit = { _, _, _ -> },
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
        onSendPhoto = onSendPhoto,
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
        ScreenHeader(
            title = stringResource(
                if (isParent) R.string.chats_inbox_title_parent else R.string.chats_inbox_title,
            ),
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
                    fontFamily = Poppins, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.Gris,
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
                ConversationRow(conversation, showGuardians = !isParent, onClick = { onOpen(conversation.id) })
            }
        }
    }
}

@Composable
private fun ConversationRow(
    conversation: Conversation,
    showGuardians: Boolean,
    onClick: () -> Unit,
) {
    // tarjeta blanca con las iniciales en lila, como las listas del admin.
    val forma = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .shadow(6.dp, forma, ambientColor = Color(0x1A2A2140), spotColor = Color(0x1A2A2140))
            .clip(forma)
            .background(CodeaColores.Tarjeta)
            .border(1.dp, CodeaColores.Borde, forma)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InitialsAvatar(initials = iniciales(conversation.childName), size = 46.dp)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(conversation.childName, fontFamily = Poppins, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CodeaColores.Titulo)
            // de quién es el chat: la mamá o el papá del niño. el papá ya sabe que es su chat, así que solo lo ve la docente.
            if (showGuardians) {
                ChatRules.guardiansLabel(conversation.guardians)?.let { label ->
                    Text(
                        text = label,
                        fontFamily = Poppins, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CodeaColores.Morado,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = conversation.lastText ?: stringResource(R.string.chats_no_messages_short),
                fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.GrisTexto,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            conversation.lastTime?.let {
                Text(it, fontFamily = Poppins, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.Gris)
            }
            if (conversation.unreadCount > 0) {
                Text(
                    text = conversation.unreadCount.toString(),
                    fontFamily = Poppins, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(CodeaColores.Violeta, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
    }
}

// "Diego Álvarez" -> "DÁ", como en Mi sala.
private fun iniciales(nombre: String): String =
    nombre.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

@Composable
private fun ConversationScreen(
    conversation: Conversation,
    messages: List<ChatMessage>,
    isParent: Boolean,
    onBackClick: () -> Unit,
    onSend: (String) -> Unit,
    onRetry: (String) -> Unit,
    onSendPhoto: (Int, ByteArray, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val photoFailed = stringResource(R.string.chats_photo_failed)
    // the photo chosen but not sent yet; while it is not null the preview covers the chat.
    var previewUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var showAttach by rememberSaveable { mutableStateOf(false) }
    // the message whose photo is open full screen.
    var viewing by remember { mutableStateOf<ChatMessage?>(null) }
    val pickPhoto = rememberPickPhoto { previewUri = it }
    val capturePhoto = rememberCapturePhoto { previewUri = it }

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
        // la docente ve de quién es el chat: el niño de título y su mamá o papá en una pastilla abajo (sin sobretítulo).
        val guardians = if (isParent) null else ChatRules.guardiansLabel(conversation.guardians)
        BackHeader(
            title = if (isParent) stringResource(R.string.chats_parent_title) else conversation.childName,
            onBackClick = onBackClick,
            modifier = Modifier.padding(bottom = if (guardians == null) 12.dp else 8.dp),
        )
        guardians?.let { Pastilla(it, modifier = Modifier.padding(bottom = 12.dp)) }

        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.chats_no_messages),
                    fontFamily = Poppins, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.Gris,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                // un poco de aire para que no se corte la sombra de las burbujas.
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(messages, key = { it.clientId ?: it.id }) { message ->
                    MessageBubble(message, onRetry = onRetry, onImageClick = { viewing = message })
                }
            }
        }

        MessageComposer(
            conversationId = conversation.id,
            onSend = onSend,
            onAttach = { showAttach = true },
        )
    }

    if (showAttach) {
        AttachSheet(
            onDismiss = { showAttach = false },
            onCamera = { showAttach = false; capturePhoto() },
            onGallery = { showAttach = false; pickPhoto() },
        )
    }

    viewing?.let { message -> PhotoViewer(message = message, onClose = { viewing = null }) }

    previewUri?.let { uri ->
        PhotoPreview(
            uri = uri,
            onClose = { previewUri = null },
            onSend = { caption ->
                previewUri = null
                scope.launch {
                    try {
                        onSendPhoto(conversation.id, ImageCompressor.compress(context.contentResolver, uri), caption)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Toast.makeText(context, photoFailed, Toast.LENGTH_SHORT).show()
                    }
                }
            },
        )
    }
}

// the "+" button: the two ways to add a photo.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttachSheet(onDismiss: () -> Unit, onCamera: () -> Unit, onGallery: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 22.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
        ) {
            AttachOption(
                icon = Icons.Filled.PhotoCamera,
                label = stringResource(R.string.chats_attach_camera),
                tint = CodeaColores.Morado,
                onClick = onCamera,
            )
            AttachOption(
                icon = Icons.Filled.PhotoLibrary,
                label = stringResource(R.string.chats_attach_gallery),
                tint = CodeaColores.Morado,
                onClick = onGallery,
            )
        }
    }
}

@Composable
private fun AttachOption(icon: ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(CodeaColores.Lila, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint)
        }
        Text(text = label, fontFamily = Poppins, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CodeaColores.Ink)
    }
}

// the photo full screen, with an optional caption, before it is sent.
@Composable
private fun PhotoPreview(uri: Uri, onClose: () -> Unit, onSend: (String) -> Unit) {
    var caption by rememberSaveable { mutableStateOf("") }
    BackHandler(onBack = onClose)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF15111F))
            .statusBarsPadding()
            .imePadding()
            .padding(bottom = 14.dp),
    ) {
        IconButton(onClick = onClose, modifier = Modifier.padding(8.dp)) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.chats_photo_close),
                tint = Color.White,
            )
        }
        AsyncImage(
            model = uri,
            contentDescription = stringResource(R.string.chats_photo_description),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.chats_photo_caption_hint)) },
                shape = RoundedCornerShape(18.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                    focusedBorderColor = Color.White.copy(alpha = 0.6f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    cursorColor = Color.White,
                ),
            )
            IconButton(
                onClick = { onSend(caption) },
                modifier = Modifier.background(CodeaColores.Violeta, RoundedCornerShape(14.dp)),
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = stringResource(R.string.chats_send),
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    onRetry: (String) -> Unit,
    onImageClick: () -> Unit,
) {
    val containerColor =
        if (message.isMine) {
            CodeaColores.Violeta
        } else {
            CodeaColores.Tarjeta
        }

    val contentColor =
        if (message.isMine) {
            Color.White
        } else {
            CodeaColores.Ink
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

    // the photo follows the corners of the bubble, 4 dp inside it.
    val imageShape =
        if (message.isMine) {
            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
        } else {
            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
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
                // la burbuja blanca lleva la misma sombra suave de las tarjetas para que se despegue del fondo.
                .shadow(if (message.isMine) 0.dp else 4.dp, shape, ambientColor = Color(0x1A2A2140), spotColor = Color(0x1A2A2140))
                .background(containerColor, shape)
                .clickable(enabled = message.status == MessageStatus.FAILED && message.clientId != null) {
                    message.clientId?.let(onRetry)
                }
                .padding(
                    horizontal = if (message.hasImage) 4.dp else 14.dp,
                    vertical = if (message.hasImage) 4.dp else 12.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val statusText = when {
                message.status == MessageStatus.SENDING -> stringResource(R.string.chats_sending)
                message.status == MessageStatus.FAILED -> stringResource(R.string.chats_failed_retry)
                message.isRead && message.isMine -> stringResource(R.string.chats_message_read, message.time)
                else -> message.time
            }

            if (message.hasImage) {
                MessageImage(
                    message = message,
                    // a failed message uses the tap to retry instead.
                    onClick = onImageClick.takeIf { message.status != MessageStatus.FAILED },
                    shape = imageShape,
                    // without a caption the time goes over the photo, like WhatsApp.
                    overlay = if (message.text.isBlank()) statusText else null,
                )
            }

            if (message.text.isNotBlank()) {
                Text(
                    text = message.text,
                    fontFamily = Poppins, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium,
                    color = contentColor,
                    modifier = if (message.hasImage) Modifier.padding(horizontal = 10.dp, vertical = 2.dp) else Modifier,
                )
            }

            if (!message.hasImage || message.text.isNotBlank()) {
                Text(
                    text = statusText,
                    fontFamily = Poppins, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                    color = contentColor.copy(alpha = 0.7f),
                    modifier = if (message.hasImage) {
                        Modifier
                            .align(Alignment.End)
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    } else {
                        Modifier
                    },
                )
            }
        }
    }
}

// the signed URL changes every time, so the cache is keyed by the message to avoid downloading it again.
@Composable
private fun chatImageRequest(message: ChatMessage): ImageRequest =
    ImageRequest.Builder(LocalContext.current)
        .data(message.localImage ?: message.imageUrl)
        .apply {
            if (message.localImage == null && message.id > 0) {
                memoryCacheKey("chat-photo-${message.id}")
                diskCacheKey("chat-photo-${message.id}")
            }
        }
        .build()

// the whole photo, not cropped, over the chat.
@Composable
private fun PhotoViewer(message: ChatMessage, onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF15111F))
            .clickable(onClick = onClose),
    ) {
        AsyncImage(
            model = chatImageRequest(message),
            contentDescription = stringResource(R.string.chats_photo_description),
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
        IconButton(onClick = onClose, modifier = Modifier.statusBarsPadding().padding(8.dp)) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.chats_photo_close),
                tint = Color.White,
            )
        }
    }
}

// the photo inside the bubble. while it is sending we show the file we just compressed.
@Composable
private fun MessageImage(message: ChatMessage, shape: Shape, overlay: String?, onClick: (() -> Unit)?) {
    val request = chatImageRequest(message)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        AsyncImage(
            model = request,
            contentDescription = stringResource(R.string.chats_photo_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (overlay != null) {
            Text(
                text = overlay,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 7.dp, vertical = 1.dp),
            )
        }
    }
}

@Composable
private fun MessageComposer(
    conversationId: Int,
    onSend: (String) -> Unit,
    onAttach: () -> Unit,
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
            onClick = onAttach,
            modifier = Modifier.background(
                color = CodeaColores.Lila,
                shape = RoundedCornerShape(14.dp),
            ),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.chats_attach),
                tint = CodeaColores.Morado,
            )
        }

        OutlinedTextField(
            value = messageText,
            onValueChange = { messageText = it },
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(stringResource(R.string.chats_message_hint), fontFamily = Poppins, color = CodeaColores.Gris)
            },
            textStyle = TextStyle(fontFamily = Poppins, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = CodeaColores.Ink),
            shape = RoundedCornerShape(18.dp),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CodeaColores.Tarjeta,
                unfocusedContainerColor = CodeaColores.Tarjeta,
                focusedBorderColor = CodeaColores.Morado,
                unfocusedBorderColor = CodeaColores.Lila,
                cursorColor = CodeaColores.Morado,
            ),
        )

        IconButton(
            onClick = {
                if (messageText.isNotBlank()) {
                    onSend(messageText)
                    messageText = ""
                }
            },
            modifier = Modifier.background(
                color = CodeaColores.VioletaTexto,
                shape = RoundedCornerShape(14.dp),
            ),
        ) {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = stringResource(R.string.chats_send),
                tint = Color.White,
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
