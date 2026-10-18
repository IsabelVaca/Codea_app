package mx.tec.codea.ui.state

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import mx.tec.codea.data.CheckInRepository
import mx.tec.codea.data.CheckInScenario
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.DeviceLocation
import mx.tec.codea.data.GeoPoint
import mx.tec.codea.data.TeacherApiRepository
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.domain.CheckInAttempt
import mx.tec.codea.domain.CheckInRecord
import mx.tec.codea.domain.CheckInRules
import mx.tec.codea.domain.CheckInStatus
import mx.tec.codea.domain.GeoMath
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// lo que el checador necesita saber del centro y del turno. sale de GET /me.
data class CheckerConfig(
    val centerLatitude: Double,
    val centerLongitude: Double,
    val radiusMeters: Int,
    val shiftStartMinutes: Int,
    val placeName: String,
)

// en qué va la búsqueda de la ubicación del teléfono (solo importa en modo real).
enum class LocationState { READY, LOCATING, NEEDS_PERMISSION, UNAVAILABLE }

// what the check-in screen shows right now.
data class CheckerUiState(
    val scenario: CheckInScenario,
    val attempt: CheckInAttempt,
    val locationState: LocationState = LocationState.READY,
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
class CheckerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CheckInRepository()
    private val api = TeacherApiRepository()
    private val location = DeviceLocation(application)

    private var config: CheckerConfig? = null

    // se arranca en modo real: la posición y la hora salen del teléfono. los casos simulados
    // (los chips "A tiempo / Tarde / Lejos") solo existen en las compilaciones de prueba.
    var uiState by mutableStateOf(
        CheckerUiState(
            scenario = CheckInScenario.REAL,
            attempt = repository.attemptFor(CheckInScenario.REAL),
            locationState = LocationState.LOCATING,
        ),
    )
        private set

    fun hasLocationPermission(): Boolean = location.hasPermission()

    // la pantalla avisa cuando ya conoce el centro. solo la primera vez busca la ubicación.
    fun start(newConfig: CheckerConfig) {
        if (config == newConfig) return
        config = newConfig
        if (uiState.scenario == CheckInScenario.REAL) refreshLocation()
    }

    fun onScenarioChange(scenario: CheckInScenario) {
        if (scenario == CheckInScenario.REAL) {
            uiState = uiState.copy(scenario = scenario, locationState = LocationState.LOCATING)
            refreshLocation()
        } else {
            uiState = stateFor(scenario)
        }
    }

    // "volver a intentar": en modo real vuelve a pedir la ubicación y la hora de ahora.
    fun onRetry() {
        if (uiState.scenario == CheckInScenario.REAL) refreshLocation() else onScenarioChange(CheckInScenario.ON_TIME)
    }

    // la pantalla llama esto cuando el usuario contestó el cuadro de permisos.
    fun onPermissionResult() = refreshLocation()

    private fun refreshLocation() {
        val config = config ?: return
        if (!location.hasPermission()) {
            uiState = uiState.copy(locationState = LocationState.NEEDS_PERMISSION)
            return
        }
        uiState = uiState.copy(locationState = LocationState.LOCATING)
        viewModelScope.launch {
            val point = location.current()
            // el usuario pudo cambiar a un caso simulado mientras se buscaba: ya no importa la respuesta.
            if (uiState.scenario != CheckInScenario.REAL) return@launch
            uiState = if (point == null) {
                uiState.copy(locationState = LocationState.UNAVAILABLE)
            } else {
                uiState.copy(attempt = realAttempt(config, point), locationState = LocationState.READY)
            }
        }
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

    // registra la entrada en la API con la posición del teléfono; la hora la pone el servidor.
    // en los casos simulados (solo pruebas) la posición se arma a partir del centro.
    // el registro que regresa la API es el que se guarda; si el servidor dice "no", sale el error.
    fun registrar(onDone: (CheckInRecord) -> Unit) {
        val config = config ?: return
        if (uiState.locationState != LocationState.READY) return
        if (!CheckInRules.canRegister(uiState.attempt)) return
        val attempt = uiState.attempt
        val (latitude, longitude) = if (uiState.scenario == CheckInScenario.REAL) {
            attempt.teacherLatitude to attempt.teacherLongitude
        } else {
            (config.centerLatitude + 0.0001) to config.centerLongitude
        }
        val reason = uiState.lateReason
        viewModelScope.launch {
            try {
                onDone(api.checkIn(latitude, longitude, reason))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice(e.toUserMessage())
            }
        }
    }

    private fun stateFor(scenario: CheckInScenario) =
        CheckerUiState(scenario = scenario, attempt = repository.attemptFor(scenario))

    // la hora y la fecha son las del teléfono; la distancia, la de su posición al centro.
    private fun realAttempt(config: CheckerConfig, point: GeoPoint): CheckInAttempt {
        val now = Calendar.getInstance()
        return CheckInAttempt(
            date = dateText(now.time),
            arrivalMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE),
            shiftStartMinutes = config.shiftStartMinutes,
            placeName = config.placeName,
            distanceMeters = GeoMath.distanceMeters(
                point.latitude, point.longitude, config.centerLatitude, config.centerLongitude,
            ),
            allowedRadiusMeters = config.radiusMeters,
            schoolLatitude = config.centerLatitude,
            schoolLongitude = config.centerLongitude,
            teacherLatitude = point.latitude,
            teacherLongitude = point.longitude,
        )
    }

    // "Domingo 18 de octubre".
    private fun dateText(date: Date): String =
        SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "MX")).format(date)
            .replaceFirstChar { it.uppercase() }
}
