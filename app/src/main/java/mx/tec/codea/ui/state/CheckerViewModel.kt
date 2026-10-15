package mx.tec.codea.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import mx.tec.codea.data.CheckInRepository
import mx.tec.codea.data.CheckInScenario
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.TeacherApiRepository
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.domain.CheckInAttempt
import mx.tec.codea.domain.CheckInRecord
import mx.tec.codea.domain.CheckInRules
import mx.tec.codea.domain.CheckInStatus

// what the check-in screen shows right now.
data class CheckerUiState(
    val scenario: CheckInScenario,
    val attempt: CheckInAttempt,
    val isWritingLateReason: Boolean = false,
    val lateReason: String = "",
    val coordinationNotified: Boolean = false,
) {
    // derived state: the domain decides, the screen only paints it.
    val status: CheckInStatus = CheckInRules.statusOf(attempt)
    val minutesLate: Int = CheckInRules.minutesLate(attempt)
    val minutesEarly: Int = CheckInRules.minutesEarly(attempt)
}

// the state of the check-in screen only. the saved check-in goes to the
// shared CodeaViewModel, because menú and mi día also need it.
class CheckerViewModel : ViewModel() {

    private val repository = CheckInRepository()
    private val api = TeacherApiRepository()

    var uiState by mutableStateOf(stateFor(CheckInScenario.ON_TIME))
        private set

    fun onScenarioChange(scenario: CheckInScenario) {
        uiState = stateFor(scenario)
    }

    // in the prototype "volver a intentar" means "now you are at the school".
    fun onRetry() {
        onScenarioChange(CheckInScenario.ON_TIME)
    }

    // avisa a coordinación; solo se marca como avisado si la API lo guardó.
    fun onNotifyCoordination() {
        val reason = if (uiState.status == CheckInStatus.OUT_OF_RANGE) "OUT_OF_RANGE" else "LATE"
        viewModelScope.launch {
            try {
                api.notifyCoordination(reason)
                uiState = uiState.copy(coordinationNotified = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice(e.toUserMessage())
            }
        }
    }

    fun onWriteLateReasonClick() {
        uiState = uiState.copy(isWritingLateReason = true)
    }

    fun onLateReasonChange(text: String) {
        uiState = uiState.copy(lateReason = text)
    }

    // registra la entrada en la API. todavía no hay gps: la posición se arma a partir de las
    // coordenadas del centro (unos 11 m al norte), y la hora la pone el servidor.
    // el registro que regresa la API es el que se guarda; si el servidor dice "no", sale el error.
    fun registrar(centerLatitude: Double?, centerLongitude: Double?, onDone: (CheckInRecord) -> Unit) {
        if (!CheckInRules.canRegister(uiState.attempt)) return
        if (centerLatitude == null || centerLongitude == null) {
            DataEvents.notice("El centro todavía no tiene ubicación registrada")
            return
        }
        val reason = uiState.lateReason
        viewModelScope.launch {
            try {
                val record = api.checkIn(centerLatitude + 0.0001, centerLongitude, reason)
                onDone(record)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice(e.toUserMessage())
            }
        }
    }

    private fun stateFor(scenario: CheckInScenario) =
        CheckerUiState(scenario = scenario, attempt = repository.attemptFor(scenario))
}
