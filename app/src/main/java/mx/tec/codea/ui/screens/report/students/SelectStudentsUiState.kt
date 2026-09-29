package mx.tec.codea.ui.screens.report.students

/**
 * Estado UI para la pantalla de selección de alumnos (¿A quién le pasó?).
 */
data class SelectStudentsUiState(
    val reportTypeLabel: String = "INCIDENCIA",
    val students: List<Student> = emptyList(),
    val isSubmitted: Boolean = false
) {
    val selectedStudents: List<Student>
        get() = students.filter { it.isSelected }

    val selectedCount: Int
        get() = selectedStudents.size

    val isSaveEnabled: Boolean
        get() = selectedCount > 0
}
