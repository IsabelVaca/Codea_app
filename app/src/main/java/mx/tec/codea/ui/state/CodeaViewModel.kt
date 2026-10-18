package mx.tec.codea.ui.state

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.TeacherApiRepository
import mx.tec.codea.data.api.ApiConfig
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.data.hoyIso
import mx.tec.codea.data.offline.CachedRoom
import mx.tec.codea.data.offline.Offline
import mx.tec.codea.data.offline.OfflineErrors
import mx.tec.codea.data.timeToMinutes
import mx.tec.codea.data.toChild
import mx.tec.codea.data.toRoutine
import mx.tec.codea.domain.AttendanceStatus
import mx.tec.codea.domain.CheckInRecord
import mx.tec.codea.domain.Child
import mx.tec.codea.domain.ErrorKind
import mx.tec.codea.domain.MarkResult
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

    // el nombre del centro, para el checador. con estado, para que se vea cuando llegue aunque
    // el checador se abra antes de que termine de cargar.
    var centerName by mutableStateOf("")
        private set

    // los días en que se trabaja (1 = lunes … 7 = domingo); el checador avisa si hoy no es uno de ellos.
    var centerWorkDays by mutableStateOf(setOf(1, 2, 3, 4, 5))
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

    // cuántas marcas de asistencia esperan conexión para llegar al servidor.
    var pendingChanges by mutableIntStateOf(0)
        private set

    val hasCheckedIn: Boolean
        get() = checkIn != null

    init {
        // cuando algo cambia en el servidor (o cambia el rol), se vuelve a pedir todo.
        viewModelScope.launch { DataEvents.changed.collect { cargar() } }
        cargar()
        // lo que quedó sin enviar de otra vez: se cuenta y se vuelve a intentar al abrir la app.
        viewModelScope.launch { Offline.attendance.pendingCount().collect { pendingChanges = it } }
        viewModelScope.launch { if (Offline.attendance.pendingCount().first() > 0) Offline.scheduler.schedule() }
    }

    // ---------------- cargar ----------------

    fun cargar() {
        if (!ApiConfig.isTeacher) return
        viewModelScope.launch {
            try {
                val me = repository.me()
                val t = me.teacher ?: return@launch
                classroomId = t.classroom?.id
                centerName = t.center.name
                centerWorkDays = t.center.workDays.toSet()
                teacher = Teacher(
                    fullName = me.fullName,
                    greetingName = "miss ${me.name}",
                    roomName = t.classroom?.name ?: "Sin sala",
                    // la entrada y la salida son las del centro, iguales para todas (las configura el admin).
                    shiftStartMinutes = timeToMinutes(t.center.entryTime) ?: (8 * 60),
                    shiftEndMinutes = timeToMinutes(t.center.exitTime) ?: (16 * 60),
                )
                checkIn = repository.checkInToday()
                val room = classroomId
                if (room != null) {
                    cargarRutina(room)
                    val lista = repository.roomChildren(room)
                    conversationIds = lista.mapNotNull { c -> c.conversationId?.let { c.id.toString() to it } }.toMap()
                    children = conPendientes(lista.map { it.toChild() })
                    // lo último que se vio queda guardado para poder abrir Mi sala sin conexión.
                    runCatching {
                        Offline.cache.save(
                            CachedRoom(
                                classroomId = room,
                                roomName = teacher.roomName,
                                teacherFullName = teacher.fullName,
                                teacherGreeting = teacher.greetingName,
                                shiftStartMinutes = teacher.shiftStartMinutes,
                                shiftEndMinutes = teacher.shiftEndMinutes,
                                children = lista,
                            ),
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // sin conexión (o con el servidor caído) se muestra lo último que se guardó, si hay algo.
                if (OfflineErrors.kindOf(e) == ErrorKind.TEMPORARY && cargarDesdeCache()) {
                    DataEvents.notice("Sin conexión: se muestra lo último que se guardó en el teléfono")
                } else {
                    DataEvents.notice("No se pudo cargar tu día: ${e.toUserMessage()}")
                }
            }
        }
    }

    // Mi sala con lo último que se vio antes de perder la conexión. regresa false si todavía no había nada guardado.
    private suspend fun cargarDesdeCache(): Boolean {
        val cached = runCatching { Offline.cache.load() }.getOrNull() ?: return false
        classroomId = cached.classroomId
        teacher = Teacher(
            fullName = cached.teacherFullName,
            greetingName = cached.teacherGreeting,
            roomName = cached.roomName,
            shiftStartMinutes = cached.shiftStartMinutes,
            shiftEndMinutes = cached.shiftEndMinutes,
        )
        conversationIds = cached.children.mapNotNull { c -> c.conversationId?.let { c.id.toString() to it } }.toMap()
        children = conPendientes(cached.children.map { it.toChild() })
        return true
    }

    // las marcas que esperan conexión valen más que lo que dice la lista (que todavía no las tiene).
    private suspend fun conPendientes(lista: List<Child>): List<Child> {
        val marcas = Offline.attendance.pendingMarks(hoyIso())
        if (marcas.isEmpty()) return lista
        return lista.map { child ->
            val estado = marcas[child.id.toIntOrNull()] ?: return@map child
            child.copy(attendance = if (estado == "ABSENT") AttendanceStatus.ABSENT else AttendanceStatus.PRESENT)
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

    // la conversación de un niño de la sala (la maneja ChatViewModel); null = todavía no tiene.
    fun conversationOf(childId: String): Int? = conversationIds[childId]

    // ---------------- eventos de la pantalla ----------------

    // el checador (CheckerViewModel) ya registró la entrada en la API: aquí solo se guarda.
    fun registerCheckIn(record: CheckInRecord) {
        checkIn = record
        // con la entrada arranca el día de la sala: se vuelve a pedir la rutina.
        DataEvents.notifyChanged()
    }

    // la salida ya quedó en la API: solo se actualiza el registro de hoy.
    fun registerCheckOut(record: CheckInRecord) {
        checkIn = record
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

    // se pinta de inmediato. sin conexión la marca espera en la cola y se manda sola al volver la red;
    // si la API dice que no (por ejemplo, el niño no es de tu sala), se avisa y se vuelve a pedir la lista.
    fun setAttendance(childId: String, status: AttendanceStatus) {
        children = children.map { if (it.id == childId) it.copy(attendance = status) else it }
        viewModelScope.launch {
            try {
                val resultado = Offline.attendance.mark(childId.toInt(), status.name, hoyIso())
                if (resultado == MarkResult.QUEUED) {
                    Offline.scheduler.schedule()
                    DataEvents.notice("Sin conexión: la marca se enviará sola cuando vuelva la red")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice(e.toUserMessage())
                cargar()
            }
        }
    }
}
