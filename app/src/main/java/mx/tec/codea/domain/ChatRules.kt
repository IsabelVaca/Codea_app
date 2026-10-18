package mx.tec.codea.domain

// pure rules of the chat list, so they can be tested without Android.
object ChatRules {

    // mixes what the server sent with what we already show.
    // - a server message replaces our pending one with the same clientId.
    // - the same id never appears twice.
    // - messages still sending or failed stay at the end until the server confirms them.
    fun merge(current: List<ChatMessage>, incoming: List<ChatMessage>): List<ChatMessage> {
        val confirmed = LinkedHashMap<Int, ChatMessage>()
        current.filter { it.status == MessageStatus.SENT }.forEach { confirmed[it.id] = it }
        incoming.forEach { confirmed[it.id] = it }

        val confirmedClientIds = confirmed.values.mapNotNull { it.clientId }.toSet()
        val pending = current.filter {
            it.status != MessageStatus.SENT && it.clientId !in confirmedClientIds
        }
        return confirmed.values.sortedBy { it.id } + pending
    }

    // the id to ask "what is new after this one" with; null = ask for everything.
    fun lastConfirmedId(messages: List<ChatMessage>): Int? =
        messages.filter { it.status == MessageStatus.SENT }.maxOfOrNull { it.id }

    // a new message from the other person means we have to mark the chat as read again.
    fun hasIncoming(messages: List<ChatMessage>): Boolean = messages.any { !it.isMine }

    fun canSend(text: String): Boolean = text.isNotBlank()
}
