package mx.tec.codea.domain

enum class AttendanceStatus { PRESENT, ABSENT }

// a child of the room. "mi sala" and the report flow use the same list,
// so a child marked absent in "mi sala" also shows as absent in the report.
data class Child(
    val id: String,
    val name: String,
    val attendance: AttendanceStatus,
    val hasUnreadChat: Boolean,
    // el papá le dejó una nota a la asistente hoy: se marca en Mi sala.
    val hasNoteToday: Boolean = false,
) {
    val firstName: String
        get() = name.substringBefore(" ")

    // "Sofía Márquez" -> "SM".
    val initials: String
        get() = name.split(" ").take(2).joinToString("") { it.take(1) }.uppercase()
}
