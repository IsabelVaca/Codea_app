package mx.tec.codea.ui.screens.report.students

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import mx.tec.codea.ui.theme.Amber
import mx.tec.codea.ui.theme.Coral
import mx.tec.codea.ui.theme.Teal
import mx.tec.codea.ui.theme.Violet

class SelectStudentsViewModel : ViewModel() {

    private val initialStudents = listOf(
        Student(
            id = "1",
            name = "Mateo Torres",
            initials = "MT",
            note = "Raspón en rodilla",
            avatarColor = Amber,
            isSelected = true
        ),
        Student(
            id = "2",
            name = "Sofía Márquez",
            initials = "SM",
            note = "Sin lesión, solo susto",
            avatarColor = Coral,
            isSelected = true
        ),
        Student(
            id = "3",
            name = "Lucía Ramírez",
            initials = "LR",
            note = null,
            avatarColor = Violet,
            isSelected = false
        ),
        Student(
            id = "4",
            name = "Renata Núñez",
            initials = "RN",
            note = null,
            avatarColor = Amber,
            isSelected = false
        ),
        Student(
            id = "5",
            name = "Santiago López",
            initials = "SL",
            note = null,
            avatarColor = Teal,
            isSelected = false
        )
    )

    private val _uiState = MutableStateFlow(SelectStudentsUiState(students = initialStudents))
    val uiState: StateFlow<SelectStudentsUiState> = _uiState.asStateFlow()

    private var lastClickTimestamp: Long = 0L

    /**
     * Debouncing para evitar clics dobles rápidos.
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
     * Sincroniza el tipo de reporte seleccionado desde la pantalla anterior.
     */
    fun setReportTypeLabel(label: String) {
        _uiState.update { it.copy(reportTypeLabel = label.uppercase()) }
    }

    /**
     * Alterna la selección del alumno al hacer clic en cualquier parte de su tarjeta.
     */
    fun toggleStudentSelection(studentId: String) {
        _uiState.update { state ->
            val updatedList = state.students.map { student ->
                if (student.id == studentId) {
                    student.copy(isSelected = !student.isSelected)
                } else {
                    student
                }
            }
            state.copy(students = updatedList)
        }
    }

    /**
     * Guarda el reporte final y notifica el éxito a la vista.
     */
    fun onSaveReportClicked(onSuccess: () -> Unit) {
        if (!canProcessClick()) return
        if (_uiState.value.isSaveEnabled) {
            _uiState.update { it.copy(isSubmitted = true) }
            onSuccess()
        }
    }

    /**
     * Reinicia la selección de alumnos al estado inicial.
     */
    fun resetState() {
        _uiState.value = SelectStudentsUiState(students = initialStudents)
    }
}
