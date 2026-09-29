package mx.tec.codea.domain

// one message of the chat between the teacher and a family.
data class ChatMessage(
    val text: String,
    val time: String,
    val isFromTeacher: Boolean,
    val isRead: Boolean = false,
)
