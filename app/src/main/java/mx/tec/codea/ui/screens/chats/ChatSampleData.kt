package mx.tec.codea.ui.screens.chats

data class ChatMessage(
    val text: String,
    val time: String,
    val isFromTeacher: Boolean,
    val isRead: Boolean = false,
)

object ChatSampleData {
    private val sofiaMessages = listOf(
        ChatMessage(
            text = "Buenos días miss, ¿Sofía desayunó bien? Amaneció un poco desganada.",
            time = "9:02",
            isFromTeacher = false,
        ),
        ChatMessage(
            text = "Buenos días. Desayunó todo y se quedó contenta en la mesa. Le tomé una foto en trazos.",
            time = "9:14",
            isFromTeacher = true,
            isRead = true,
        ),
        ChatMessage(
            text = "Gracias miss. ¿Puede recogerla su abuela hoy a las 3?",
            time = "11:20",
            isFromTeacher = false,
        ),
    )

    fun messagesFor(childId: String): List<ChatMessage> =
        when (childId) {
            "sofia-marquez" -> sofiaMessages
            else -> emptyList()
        }
}
