package mx.tec.codea.ui.state

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.TeacherApiRepository
import mx.tec.codea.data.api.ApiConfig
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.data.timeToMinutes
import mx.tec.codea.data.toChild
import mx.tec.codea.data.toRoutine
import mx.tec.codea.domain.AttendanceStatus
import mx.tec.codea.domain.ChatMessage
import mx.tec.codea.domain.CheckInRecord
import mx.tec.codea.domain.Child
import mx.tec.codea.domain.Report
import mx.tec.codea.domain.ReportValidator
import mx.tec.codea.domain.Routine
import mx.tec.codea.domain.Subprocess
import mx.tec.codea.domain.Teacher

// the shared view model of the teacher: the state that more than one screen needs.
// it is created once in CodeaApp, so every tab reads the same object.
// now the data comes from the API: this class asks for it (cargar) and sends the changes.
class CodeaViewModel : ViewModel() {

    private val repository = TeacherApiRepository()

    // ids que la API necesita y la app no guarda en sus clases.
    private var classroomId: Int? = null
    private var momentIds: Map<Int, Int> = emptyMap()          // número del momento -> id
    private var conversationIds: Map<String, Int> = emptyMap() // id del niño -> id de la conversación
    private var selectActiveAfterLoad = true

    // el lugar del checador: el centro, con sus coordenadas.
    var centerLatitude: Double? = null
        private set
    var centerLongitude: Double? = null
        private set

    var teacher by mutableStateOf(Teacher("", "", "", 8 * 60, 16 * 60))
        private set

    // null until the teacher checks in. menú, checador and mi día all read it.
    var checkIn by mutableStateOf<CheckInRecord?>(null)
        private set

    var routine by mutableStateOf(Routine("Rutina del día", emptyList()))
        private set

    // el contador de reportes de cada momento (número -> cuántos).
    private var reportCounts by mutableStateOf<Map<Int, Int>>(emptyMap())

    // the moment the teacher picked in "mi día". it starts on the one in progress.
    var selectedSubprocessNumber by mutableIntStateOf(0)
        private set

    var children by mutableStateOf<List<Child>>(emptyList())
        private set

    // the chat that "mi sala" opened last, so the "chats" tab shows it.
    var openChatChildId by mutableStateOf<String?>(null)
        private set

    private var chatMessages by mutableStateOf<List<ChatMessage>>(emptyList())

    val hasCheckedIn: Boolean
        get() = checkIn != null

    init {
        // cuando algo cambia en el servidor (o cambia el rol), se vuelve a pedir todo.
        viewModelScope.launch { DataEvents.changed.collect { cargar() } }
        cargar()
    }

    // ---------------- cargar ----------------

    fun cargar() {
        if (!ApiConfig.isTeacher) return
        viewModelScope.launch {
            try {
                val me = repository.me()
                val t = me.teacher ?: return@launch
                classroomId = t.classroom?.id
                centerLatitude = t.center.latitude
                centerLongitude = t.center.longitude
                teacher = Teacher(
                    fullName = me.fullName,
                    greetingName = "miss ${me.name}",
                    roomName = t.classroom?.name ?: "Sin sala",
                    shiftStartMinutes = timeToMinutes(t.shift?.startTime) ?: (8 * 60),
                    shiftEndMinutes = timeToMinutes(t.shift?.endTime) ?: (16 * 60),
                )
                checkIn = repository.checkInToday()
                val room = classroomId
                if (room != null) {
                    cargarRutina(room)
                    children = repository.roomChildren(room).also { lista ->
                        conversationIds = lista.mapNotNull { c -> c.conversationId?.let { c.id.toString() to it } }.toMap()
                    }.map { it.toChild() }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice("No se pudo cargar tu día: ${e.toUserMessage()}")
            }
        }
    }

    private suspend fun cargarRutina(room: Int) {
        val dto = repository.routine(room)
        momentIds = dto.moments.associate { it.number to it.id }
        reportCounts = dto.moments.associate { it.number to it.reportCount }
        routine = dto.toRoutine()
        // al abrir la app, o después de confirmar un momento, se sigue el día: el activo queda seleccionado.
        val esValido = routine.subprocesses.any { it.number == selectedSubprocessNumber }
        if (selectActiveAfterLoad || !esValido) {
            selectedSubprocessNumber = routine.activeSubprocess?.number
                ?: routine.subprocesses.firstOrNull()?.number
                ?: 0
            selectActiveAfterLoad = false
        }
    }

    // ---------------- lectura ----------------

    fun subprocessByNumber(number: Int): Subprocess? =
        routine.subprocesses.firstOrNull { it.number == number }

    fun reportCountOf(subprocessNumber: Int): Int = reportCounts[subprocessNumber] ?: 0

    fun childById(id: String?): Child? = children.firstOrNull { it.id == id }

    // solo se guarda la conversación abierta.
    fun messagesFor(childId: String): List<ChatMessage> =
        if (childId == openChatChildId) chatMessages else emptyList()

    // ---------------- eventos de la pantalla ----------------

    // el checador (CheckerViewModel) ya registró la entrada en la API: aquí solo se guarda.
    fun registerCheckIn(record: CheckInRecord) {
        checkIn = record
        // con la entrada arranca el día de la sala: se vuelve a pedir la rutina.
        DataEvents.notifyChanged()
    }

    fun selectSubprocess(number: Int) {
        selectedSubprocessNumber = number
    }

    fun confirmSubprocess(number: Int) {
        val momentId = momentIds[number] ?: return
        selectActiveAfterLoad = true
        launchAction { repository.confirm(momentId) }
    }

    fun addReport(report: Report, photo: Bitmap? = null, onSuccess: () -> Unit = {}) {
        if (!ReportValidator.isValid(report.description, report.childIds)) return
        val momentId = momentIds[report.subprocessNumber] ?: return
        launchAction(onSuccess) { repository.createReport(momentId, report, photo) }
    }

    // se pinta de inmediato; si la API dice que no, se avisa y se vuelve a pedir la lista.
    fun setAttendance(childId: String, status: AttendanceStatus) {
        children = children.map { if (it.id == childId) it.copy(attendance = status) else it }
        viewModelScope.launch {
            try {
                repository.setAttendance(childId, status)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice(e.toUserMessage())
                cargar()
            }
        }
    }

    fun openChat(childId: String) {
        openChatChildId = childId
        chatMessages = emptyList()
        val conversation = conversationIds[childId] ?: return
        viewModelScope.launch {
            try {
                chatMessages = repository.messages(conversation)
                repository.markRead(conversation)
                children = children.map { if (it.id == childId) it.copy(hasUnreadChat = false) else it }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice("No se pudo abrir el chat: ${e.toUserMessage()}")
            }
        }
    }

    fun sendMessage(text: String) {
        val childId = openChatChildId ?: return
        val conversation = conversationIds[childId]
        if (conversation == null) {
            DataEvents.notice("Este niño todavía no tiene chat")
            return
        }
        if (text.isBlank()) return
        viewModelScope.launch {
            try {
                repository.sendMessage(conversation, text)
                chatMessages = repository.messages(conversation)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice("No se pudo enviar: ${e.toUserMessage()}")
            }
        }
    }

    // todavía no hay login real: "cerrar sesión" solo cierra el chat y vuelve a pedir el día.
    fun resetDay() {
        openChatChildId = null
        chatMessages = emptyList()
        selectActiveAfterLoad = true
        cargar()
    }
}
