package mx.tec.codea.data

import mx.tec.codea.domain.ChatMessage

// the messages of each family. only sofía has a conversation in the prototype.
class ChatRepository {

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
