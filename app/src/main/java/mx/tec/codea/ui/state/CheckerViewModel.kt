package mx.tec.codea.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import mx.tec.codea.data.CheckInRepository
import mx.tec.codea.data.CheckInScenario
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

    var uiState by mutableStateOf(stateFor(CheckInScenario.ON_TIME))
        private set

    fun onScenarioChange(scenario: CheckInScenario) {
        uiState = stateFor(scenario)
    }

    // in the prototype "volver a intentar" means "now you are at the school".
    fun onRetry() {
        onScenarioChange(CheckInScenario.ON_TIME)
    }

    fun onNotifyCoordination() {
        uiState = uiState.copy(coordinationNotified = true)
    }

    fun onWriteLateReasonClick() {
        uiState = uiState.copy(isWritingLateReason = true)
    }

    fun onLateReasonChange(text: String) {
        uiState = uiState.copy(lateReason = text)
    }

    // null when the rules do not let the teacher check in.
    fun buildRecord(): CheckInRecord? = CheckInRules.register(uiState.attempt, uiState.lateReason)

    private fun stateFor(scenario: CheckInScenario) =
        CheckerUiState(scenario = scenario, attempt = repository.attemptFor(scenario))
}
