package mx.tec.codea.ui.state

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import mx.tec.codea.domain.Report
import mx.tec.codea.domain.ReportMethod
import mx.tec.codea.domain.ReportType
import mx.tec.codea.domain.ReportValidator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// everything the teacher has written in the report form, in one object.
// with one object the screen never sees half an update.
data class ReportFormUiState(
    val method: ReportMethod = ReportMethod.WRITTEN,
    val photo: Bitmap? = null,
    val photoTime: String = "",
    val type: ReportType = ReportType.INCIDENT,
    val description: String = "",
    val selectedChildIds: Set<String> = emptySet(),
) {
    // derived state: we calculate it, we do not save it.
    val descriptionLength: Int = description.trim().length

    val canContinue: Boolean = ReportValidator.isDescriptionValid(description)

    val canSave: Boolean = ReportValidator.isValid(description, selectedChildIds)
}

// the form of the report flow (method -> report -> children).
// it lives only while the flow is open: its owner is the first screen of the flow,
// so when the teacher saves or goes back to "mi día", the form is cleared by itself.
class ReportFormViewModel : ViewModel() {

    var uiState by mutableStateOf(ReportFormUiState())
        private set

    fun onMethodChosen(method: ReportMethod) {
        uiState = uiState.copy(method = method)
    }

    fun onPhotoTaken(photo: Bitmap) {
        uiState = uiState.copy(method = ReportMethod.PHOTO, photo = photo, photoTime = now())
    }

    fun onTypeChange(type: ReportType) {
        uiState = uiState.copy(type = type)
    }

    fun onDescriptionChange(text: String) {
        uiState = uiState.copy(description = text)
    }

    // the dictation adds its text after what was already written.
    fun onDictationHeard(text: String) {
        if (text.isBlank()) return
        val existing = uiState.description.trim()
        val combined = if (existing.isEmpty()) text else "$existing $text"
        uiState = uiState.copy(description = combined)
    }

    fun onChildToggle(childId: String) {
        val ids = uiState.selectedChildIds
        uiState = uiState.copy(
            selectedChildIds = if (childId in ids) ids - childId else ids + childId,
        )
    }

    // turns the form into the report the shared view model saves.
    fun toReport(subprocessNumber: Int): Report = Report(
        subprocessNumber = subprocessNumber,
        type = uiState.type,
        method = uiState.method,
        description = uiState.description.trim(),
        childIds = uiState.selectedChildIds,
        hasPhoto = uiState.photo != null,
        time = now(),
    )

    private fun now(): String = SimpleDateFormat("H:mm", Locale.getDefault()).format(Date())
}
