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
import mx.tec.codea.domain.CheckInAttempt
import mx.tec.codea.domain.CheckInRecord
import mx.tec.codea.domain.CheckInRules
import mx.tec.codea.domain.CheckInStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// lo que el checador necesita saber del turno y del centro. sale de GET /me.
data class CheckerConfig(
    val shiftStartMinutes: Int,
    val placeName: String,
)

// what the check-in screen shows right now.
data class CheckerUiState(
    val attempt: CheckInAttempt,
    val isWritingLateReason: Boolean = false,
    val lateReason: String = "",
) {
    // derived state: the domain decides, the screen only paints it.
    val status: CheckInStatus = CheckInRules.statusOf(attempt)
    val minutesLate: Int = CheckInRules.minutesLate(attempt)
    val minutesEarly: Int = CheckInRules.minutesEarly(attempt)
}

// the state of the check-in screen only. the saved check-in goes to the
// shared CodeaViewModel, because menú and mi día also need it.
// el checador no usa ubicación: la hora y la fecha son las del teléfono (el servidor pone la suya al guardar).
class CheckerViewModel : ViewModel() {

    private val api = TeacherApiRepository()

    private var config = CheckerConfig(shiftStartMinutes = 8 * 60, placeName = "La Oruga")

    var uiState by mutableStateOf(CheckerUiState(attempt = attemptNow(config)))
        private set

    // la pantalla avisa cuando ya conoce el turno y el centro.
    fun start(newConfig: CheckerConfig) {
        config = newConfig
        refreshTime()
    }

    // vuelve a leer la hora del teléfono: la pantalla lo pide cada cierto tiempo para que el reloj no se quede atrás.
    fun refreshTime() {
        uiState = uiState.copy(attempt = attemptNow(config))
    }

    fun onWriteLateReasonClick() {
        uiState = uiState.copy(isWritingLateReason = true)
    }

    fun onLateReasonChange(text: String) {
        uiState = uiState.copy(lateReason = text)
    }

    // registra la entrada en la API; la hora la pone el servidor.
    // el registro que regresa la API es el que se guarda; si el servidor dice "no", sale el error.
    fun registrar(onDone: (CheckInRecord) -> Unit) {
        val reason = uiState.lateReason
        viewModelScope.launch {
            try {
                onDone(api.checkIn(reason))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice(e.toUserMessage())
            }
        }
    }

    // registra la salida; la API la rechaza si no hay entrada hoy.
    fun registrarSalida(onDone: (CheckInRecord) -> Unit) {
        viewModelScope.launch {
            try {
                onDone(api.checkOut())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice(e.toUserMessage())
            }
        }
    }

    // la hora y la fecha son las del teléfono.
    private fun attemptNow(config: CheckerConfig): CheckInAttempt {
        val now = Calendar.getInstance()
        return CheckInAttempt(
            date = dateText(now.time),
            arrivalMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE),
            shiftStartMinutes = config.shiftStartMinutes,
            placeName = config.placeName,
        )
    }

    // "Domingo 18 de octubre".
    private fun dateText(date: Date): String =
        SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "MX")).format(date)
            .replaceFirstChar { it.uppercase() }
}
