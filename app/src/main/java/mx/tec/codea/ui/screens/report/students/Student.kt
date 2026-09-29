package mx.tec.codea.ui.screens.report.students

import androidx.compose.ui.graphics.Color

/**
 * Modelo de datos para representar a un alumno.
 */
data class Student(
    val id: String,
    val name: String,
    val initials: String,
    val note: String? = null,
    val avatarColor: Color,
    val isSelected: Boolean = false
)
