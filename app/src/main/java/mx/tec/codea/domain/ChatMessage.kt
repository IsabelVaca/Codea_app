package mx.tec.codea.domain

enum class MessageStatus { SENT, SENDING, FAILED }

// one message of the chat between a teacher and a family.
// id = 0 and a clientId means the server has not confirmed it yet.
data class ChatMessage(
    val id: Int,
    val clientId: String?,
    val text: String,
    val time: String,
    val isMine: Boolean,
    val isRead: Boolean = false,
    val status: MessageStatus = MessageStatus.SENT,
)

// one row of the inbox: the chat about one child.
data class Conversation(
    val id: Int,
    val childName: String,
    val unreadCount: Int,
    val lastText: String?,
    val lastTime: String?,
)
