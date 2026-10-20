package mx.tec.codea.domain

enum class AttendanceStatus { PRESENT, ABSENT }

// un aviso que el papá mandó desde casa para hoy. "time" ya viene como "7:40" para dibujarlo.
data class FamilyNote(
    val id: Int,
    val text: String,
    val time: String,
)

// a child of the room. "mi sala", "mi día" and the report flow use the same list,
// so a child marked absent in "mi sala" also shows as absent in the report.
// sin pase de lista el niño cuenta como ausente: la docente lo marca presente en "Mi día".
data class Child(
    val id: String,
    val name: String,
    val attendance: AttendanceStatus,
    val hasUnreadChat: Boolean,
    // los avisos que los papás le dejaron a la asistente hoy, del más viejo al más nuevo.
    val notes: List<FamilyNote> = emptyList(),
    // true cuando la docente ya guardó el resumen del día de este niño.
    val hasSummaryToday: Boolean = false,
    // la hora a la que se le pasó lista como presente, como "8:05"; null si está ausente o la marca es vieja.
    val arrivalTime: String? = null,
) {
    val hasNoteToday: Boolean
        get() = notes.isNotEmpty()

    val firstName: String
        get() = name.substringBefore(" ")

    // "Sofía Márquez" -> "SM".
    val initials: String
        get() = name.split(" ").take(2).joinToString("") { it.take(1) }.uppercase()
}
