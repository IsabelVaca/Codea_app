package mx.tec.codea.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import mx.tec.codea.data.ChatRepository
import mx.tec.codea.data.ChildRepository
import mx.tec.codea.data.RoutineRepository
import mx.tec.codea.data.TeacherRepository
import mx.tec.codea.domain.AttendanceStatus
import mx.tec.codea.domain.ChatMessage
import mx.tec.codea.domain.CheckInRecord
import mx.tec.codea.domain.Child
import mx.tec.codea.domain.Report
import mx.tec.codea.domain.ReportValidator
import mx.tec.codea.domain.Routine
import mx.tec.codea.domain.RoutineRules
import mx.tec.codea.domain.Subprocess
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// the shared view model: the state that more than one screen needs.
// it is created once in CodeaApp, so every tab reads the same object.
// it does not know about screens or the nav controller: it only keeps state
// and has functions (events) that change it.
class CodeaViewModel : ViewModel() {

    private val teacherRepository = TeacherRepository()
    private val routineRepository = RoutineRepository()
    private val childRepository = ChildRepository()
    private val chatRepository = ChatRepository()

    // state that does not change: we read it once.
    val teacher = teacherRepository.current()

    // state that changes. "private set" means only this class can write it:
    // the screens read it and call the functions below to change it.

    // null until the teacher checks in. menú, checador and mi día all read it.
    var checkIn by mutableStateOf<CheckInRecord?>(null)
        private set

    var routine by mutableStateOf(routineRepository.getToday())
        private set

    // the moment the teacher picked in "mi día". it starts on the one in progress.
    var selectedSubprocessNumber by mutableIntStateOf(firstSelection(routine))
        private set

    var reports by mutableStateOf<List<Report>>(emptyList())
        private set

    var children by mutableStateOf(childRepository.getAll())
        private set

    // the chat that "mi sala" opened last, so the "chats" tab shows it.
    var openChatChildId by mutableStateOf<String?>(null)
        private set

    val hasCheckedIn: Boolean
        get() = checkIn != null

    fun subprocessByNumber(number: Int): Subprocess? =
        routine.subprocesses.firstOrNull { it.number == number }

    fun reportCountOf(subprocessNumber: Int): Int =
        reports.count { it.subprocessNumber == subprocessNumber }

    fun childById(id: String?): Child? = children.firstOrNull { it.id == id }

    fun messagesFor(childId: String): List<ChatMessage> = chatRepository.messagesFor(childId)

    // --- events that come from the ui ---

    fun registerCheckIn(record: CheckInRecord) {
        checkIn = record
    }

    fun selectSubprocess(number: Int) {
        selectedSubprocessNumber = number
    }

    fun confirmSubprocess(number: Int) {
        // the domain decides what "confirm" means. the view model only asks and saves.
        routine = RoutineRules.confirm(routine, number, now())
        // we follow the day: the next moment becomes the selected one.
        selectedSubprocessNumber = routine.activeSubprocess?.number ?: number
    }

    fun addReport(report: Report) {
        if (!ReportValidator.isValid(report.description, report.childIds)) return
        // a new list, not reports.add(...): compose only sees a new object.
        reports = reports + report
    }

    fun setAttendance(childId: String, status: AttendanceStatus) {
        children = children.map { if (it.id == childId) it.copy(attendance = status) else it }
    }

    fun openChat(childId: String) {
        openChatChildId = childId
    }

    // the prototype has no real login, so "cerrar sesión" starts the day again.
    fun resetDay() {
        checkIn = null
        routine = routineRepository.getToday()
        selectedSubprocessNumber = firstSelection(routine)
        reports = emptyList()
        children = childRepository.getAll()
        openChatChildId = null
    }

    private fun firstSelection(routine: Routine): Int =
        routine.activeSubprocess?.number ?: routine.subprocesses.first().number

    private fun now(): String = SimpleDateFormat("H:mm", Locale.getDefault()).format(Date())
}
