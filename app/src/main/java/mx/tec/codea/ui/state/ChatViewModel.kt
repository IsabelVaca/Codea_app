package mx.tec.codea.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mx.tec.codea.data.ChatApiRepository
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.api.ApiConfig
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.domain.ChatMessage
import mx.tec.codea.domain.ChatRules
import mx.tec.codea.domain.Conversation
import mx.tec.codea.domain.MessageStatus
import java.util.UUID

// el chat de la docente (bandeja y conversación) y el del papá. la pantalla llama a refresh()
// cada pocos segundos mientras está visible; no hay WebSocket todavía.
class ChatViewModel : ViewModel() {

    private val repository = ChatApiRepository()

    var conversations by mutableStateOf<List<Conversation>>(emptyList())
        private set

    // la conversación abierta; null = se ve la bandeja.
    var openId by mutableStateOf<Int?>(null)
        private set

    var messages by mutableStateOf<List<ChatMessage>>(emptyList())
        private set

    // si ya llegó la primera respuesta de la bandeja (para no mostrar "sin chats" antes de tiempo).
    var loaded by mutableStateOf(false)
        private set

    val openConversation: Conversation?
        get() = conversations.firstOrNull { it.id == openId }

    // el papá con un solo hijo no tiene bandeja: entra directo a la conversación.
    val skipsInbox: Boolean
        get() = ApiConfig.isParent && conversations.size == 1

    private var openJob: Job? = null

    // fotos que ya se subieron pero cuyo mensaje no llegó: un reintento no las sube otra vez.
    private val uploadedMedia = mutableMapOf<String, Int>()

    init {
        // otro usuario entra o algo cambió en el servidor: se vuelve a pedir la bandeja.
        viewModelScope.launch { DataEvents.changed.collect { loadConversations() } }
    }

    // llamar cada pocos segundos con la pantalla visible.
    suspend fun refresh() {
        loadConversations()
        val id = openId ?: return
        pullNewer(id)
    }

    private suspend fun loadConversations() {
        if (!ApiConfig.isTeacher && !ApiConfig.isParent) {
            conversations = emptyList()
            return
        }
        try {
            conversations = repository.conversations()
            loaded = true
            if (openId == null && skipsInbox) open(conversations.first().id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // un fallo de red durante la consulta periódica no debe llenar la pantalla de avisos.
            if (!loaded) DataEvents.notice("No se pudieron cargar los chats: ${e.toUserMessage()}")
        }
    }

    fun open(conversationId: Int) {
        if (openId == conversationId) return
        openId = conversationId
        messages = emptyList()
        openJob?.cancel()
        openJob = viewModelScope.launch {
            try {
                messages = repository.messages(conversationId)
                markRead(conversationId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice("No se pudo abrir el chat: ${e.toUserMessage()}")
            }
        }
    }

    fun close() {
        openJob?.cancel()
        openId = null
        messages = emptyList()
    }

    private suspend fun pullNewer(conversationId: Int) {
        try {
            val last = ChatRules.lastConfirmedId(messages)
            val fresh = if (last == null) repository.messages(conversationId)
            else repository.newerThan(conversationId, last)
            if (conversationId != openId) return
            if (fresh.isNotEmpty()) {
                messages = ChatRules.merge(messages, fresh)
                if (ChatRules.hasIncoming(fresh)) markRead(conversationId)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // sin red: se vuelve a intentar en la siguiente vuelta.
        }
    }

    private suspend fun markRead(conversationId: Int) {
        try {
            repository.markRead(conversationId)
            conversations = conversations.map {
                if (it.id == conversationId) it.copy(unreadCount = 0) else it
            }
            // Mi sala quita el punto de "mensaje nuevo".
            DataEvents.notifyChanged()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // no es grave: se marca de nuevo en la siguiente vuelta.
        }
    }

    fun send(text: String) {
        if (!ChatRules.canSend(text)) return
        val id = openId ?: return
        val clientId = UUID.randomUUID().toString()
        messages = messages + ChatMessage(
            id = 0,
            clientId = clientId,
            text = text.trim(),
            time = "",
            isMine = true,
            status = MessageStatus.SENDING,
        )
        deliver(id, text.trim(), clientId)
    }

    // la foto ya viene comprimida (ImageCompressor); el texto es opcional.
    fun sendPhoto(jpeg: ByteArray, caption: String) {
        val id = openId ?: return
        val clientId = UUID.randomUUID().toString()
        messages = messages + ChatMessage(
            id = 0,
            clientId = clientId,
            text = caption.trim(),
            time = "",
            isMine = true,
            status = MessageStatus.SENDING,
            localImage = jpeg,
        )
        deliver(id, caption.trim(), clientId, jpeg)
    }

    // el mismo clientId: si el mensaje sí había llegado, la API no lo duplica.
    fun retry(clientId: String) {
        val id = openId ?: return
        val pending = messages.firstOrNull { it.clientId == clientId && it.status == MessageStatus.FAILED }
            ?: return
        replace(clientId) { it.copy(status = MessageStatus.SENDING) }
        deliver(id, pending.text, clientId, pending.localImage)
    }

    private fun deliver(conversationId: Int, text: String, clientId: String, jpeg: ByteArray? = null) {
        viewModelScope.launch {
            try {
                val sent = if (jpeg == null) {
                    repository.send(conversationId, text, clientId)
                } else {
                    val mediaId = uploadedMedia.getOrPut(clientId) { repository.uploadPhoto(jpeg) }
                    repository.sendPhoto(conversationId, mediaId, text, clientId).also { uploadedMedia.remove(clientId) }
                }
                if (conversationId == openId) messages = ChatRules.merge(messages, listOf(sent))
                conversations = conversations.map {
                    if (it.id == conversationId) {
                        it.copy(lastText = ChatRules.preview(sent.text, sent.hasImage), lastTime = sent.time)
                    } else {
                        it
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                replace(clientId) { it.copy(status = MessageStatus.FAILED) }
                DataEvents.notice("No se pudo enviar: ${e.toUserMessage()}")
            }
        }
    }

    private fun replace(clientId: String, change: (ChatMessage) -> ChatMessage) {
        messages = messages.map { if (it.clientId == clientId && it.status != MessageStatus.SENT) change(it) else it }
    }
}
