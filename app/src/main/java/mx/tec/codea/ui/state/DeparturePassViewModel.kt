package mx.tec.codea.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.TeacherApiRepository
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.domain.CheckInRecord
import mx.tec.codea.domain.DepartureEntry
import mx.tec.codea.domain.DepartureRules
import mx.tec.codea.domain.PickupChoice

// el pase de salida de la docente: al terminar la rutina registra la hora de salida de cada niño y con quién se fue.
// cuando ya salieron todos, "Finalizar día" registra la salida de la docente y termina su turno.
// necesita conexión: la hora y las personas autorizadas las pone el servidor.
class DeparturePassViewModel : ViewModel() {

    private val repository = TeacherApiRepository()
    private var classroomId: Int? = null

    var loading by mutableStateOf(true)
        private set
    var failed by mutableStateOf(false)
        private set
    var entries by mutableStateOf<List<DepartureEntry>>(emptyList())
        private set

    // el niño al que se le está guardando la salida (para no tocar dos veces).
    var busyChildId by mutableStateOf<String?>(null)
        private set
    var finishing by mutableStateOf(false)
        private set

    val pending: Int
        get() = DepartureRules.pending(entries)

    // true cuando la lista ya llegó de la API: con lo guardado en pantalla todavía no se puede finalizar el día.
    private var actualizada by mutableStateOf(false)

    val canFinishDay: Boolean
        get() = DepartureRules.canFinishDay(entries, loaded = actualizada && !loading && !failed)

    fun load(room: Int) {
        classroomId = room
        refresh(showLoading = entries.isEmpty())
    }

    fun retry() = refresh(showLoading = true)

    private fun refresh(showLoading: Boolean) {
        val room = classroomId ?: return
        if (showLoading) loading = true
        failed = false
        viewModelScope.launch {
            // lo guardado de hoy (ApiCache) sale al instante; la lista de la API lo reemplaza al llegar.
            if (entries.isEmpty()) runCatching { repository.departuresGuardadas(room) }.getOrNull()?.let {
                entries = it
                loading = false
            }
            try {
                entries = repository.departures(room)
                actualizada = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                failed = true
                DataEvents.notice("No se pudo cargar el pase de salida: ${e.toUserMessage()}")
            } finally {
                loading = false
            }
        }
    }

    // guarda la salida de un niño con la persona elegida. la hora la pone el servidor; después se vuelve a pedir la lista.
    fun register(entry: DepartureEntry, choice: PickupChoice) = runOnChild(entry) {
        repository.registerDeparture(entry.childId, choice)
    }

    // por si se eligió a la persona equivocada: quita la salida para registrarla otra vez.
    fun undo(entry: DepartureEntry) = runOnChild(entry) {
        repository.undoDeparture(entry.childId)
    }

    private fun runOnChild(entry: DepartureEntry, block: suspend () -> Unit) {
        if (busyChildId != null) return
        busyChildId = entry.childId
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice(e.toUserMessage())
            } finally {
                busyChildId = null
            }
            // pase lo que pase se vuelve a pedir la lista: si dos docentes tocaron al mismo niño, queda la verdad del servidor.
            refresh(showLoading = false)
        }
    }

    // termina el día: registra la salida de la docente. solo si ya salieron todos los niños que vinieron.
    fun finishDay(onDone: (CheckInRecord) -> Unit) {
        if (!canFinishDay || finishing) return
        finishing = true
        viewModelScope.launch {
            try {
                onDone(repository.checkOut())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice(e.toUserMessage())
            } finally {
                finishing = false
            }
        }
    }
}
