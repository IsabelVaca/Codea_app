package mx.tec.codea.data

import mx.tec.codea.data.api.ApiClient
import mx.tec.codea.data.api.ApiConfig
import mx.tec.codea.data.api.ConversationDto
import mx.tec.codea.data.api.MessageBody
import mx.tec.codea.data.api.MessageDto
import mx.tec.codea.domain.ChatMessage
import mx.tec.codea.domain.ChatGuardian
import mx.tec.codea.domain.ChatRules
import mx.tec.codea.domain.Conversation

// el chat entre docentes y familias. lo usan la docente y el papá.
class ChatApiRepository {

    private val api get() = ApiClient.api

    suspend fun conversations(): List<Conversation> = api.conversations().map { it.toConversation() }

    // la API los manda del más nuevo al más viejo; la pantalla los quiere en orden.
    suspend fun messages(conversationId: Int): List<ChatMessage> =
        api.messages(conversationId, 100, null).items.reversed().map { it.toChatMessage() }

    // solo lo que llegó después del último mensaje que ya se tiene.
    suspend fun newerThan(conversationId: Int, lastId: Int): List<ChatMessage> =
        api.messages(conversationId, 100, lastId).items.sortedBy { it.id }.map { it.toChatMessage() }

    // el clientId evita que un reintento duplique el mensaje.
    suspend fun send(conversationId: Int, text: String, clientId: String): ChatMessage =
        api.sendMessage(conversationId, MessageBody(text.trim(), clientId)).toChatMessage()

    // una foto son dos pasos: subirla al almacenamiento y mandar el mensaje con su media_id.
    // los papás no pueden subir PHOTO; su foto va como DOCUMENT (la API acepta imágenes en ambos).
    suspend fun uploadPhoto(jpeg: ByteArray): Int =
        MediaUploader.upload(jpeg, "image/jpeg", if (ApiConfig.isParent) "DOCUMENT" else "PHOTO")

    suspend fun sendPhoto(conversationId: Int, mediaId: Int, caption: String, clientId: String): ChatMessage =
        api.sendMessage(conversationId, MessageBody(caption.trim(), clientId, mediaId)).toChatMessage()

    suspend fun markRead(conversationId: Int) {
        api.markRead(conversationId)
    }
}

private fun MessageDto.toChatMessage() = ChatMessage(
    id = id,
    clientId = clientId,
    text = text,
    time = clockOf(createdAt) ?: "",
    isMine = isMine,
    isRead = isRead,
    imageUrl = mediaUrl,
)

private fun ConversationDto.toConversation() = Conversation(
    id = id,
    childName = child.fullName,
    unreadCount = unreadCount,
    lastText = lastMessage?.let { ChatRules.preview(it.text, it.mediaUrl != null) },
    lastTime = lastMessage?.createdAt?.let { clockOf(it) },
    guardians = child.guardians.map { ChatGuardian(it.fullName, parentesco(it.relationship)) },
)
