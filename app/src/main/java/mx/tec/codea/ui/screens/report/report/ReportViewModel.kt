package mx.tec.codea.ui.screens.report.report

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import mx.tec.codea.ui.screens.report.method.ReportMethod
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    private var lastClickTimestamp: Long = 0L

    /**
     * Prevención simple de doble clic (debouncing).
     */
    fun canProcessClick(thresholdMs: Long = 800L): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTimestamp < thresholdMs) {
            return false
        }
        lastClickTimestamp = currentTime
        return true
    }

    /**
     * Establece el método seleccionado (WRITTEN, DICTATION, PHOTO).
     */
    fun setReportMethod(method: ReportMethod) {
        _uiState.update { it.copy(selectedMethod = method) }
    }

    /**
     * Establece la foto capturada y registra la hora actual de la captura.
     */
    fun setCapturedPhoto(bitmap: Bitmap?) {
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        _uiState.update {
            it.copy(
                capturedPhoto = bitmap,
                photoTime = currentTime
            )
        }
    }

    /**
     * Cambia el tipo de reporte seleccionado (Incidencia, Logro, Salud, Conducta).
     */
    fun selectReportType(type: ReportType) {
        _uiState.update { it.copy(selectedReportType = type) }
    }

    /**
     * Actualiza el texto de la descripción.
     */
    fun updateDescriptionText(newText: String) {
        _uiState.update { it.copy(descriptionText = newText) }
    }

    /**
     * Concatena o asigna el texto resultante del dictado por voz.
     */
    fun appendSpeechText(spokenText: String) {
        if (spokenText.isBlank()) return
        _uiState.update { current ->
            val existing = current.descriptionText.trim()
            val combined = if (existing.isEmpty()) {
                spokenText
            } else {
                "$existing $spokenText"
            }
            current.copy(descriptionText = combined)
        }
    }

    /**
     * Acción del botón 'Continuar'. Solo se ejecuta si el botón está desbloqueado.
     */
    fun onContinueClicked(onSuccess: () -> Unit) {
        if (!canProcessClick()) return
        if (_uiState.value.isContinueEnabled) {
            _uiState.update { it.copy(isSubmitted = true) }
            onSuccess()
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Reinicia el estado a los valores por defecto.
     */
    fun resetState() {
        _uiState.value = ReportUiState()
    }
}
