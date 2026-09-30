package mx.tec.codea.ui.screens.myroom

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
}

data class Child(
    val id: String,
    val name: String,
    val initialAttendance: AttendanceStatus,
    val hasUnreadChat: Boolean,
)

object MyRoomSampleData {
    // The prototype starts with 4 children marked present.
    // The five children below are the visible sample cards from the prototype.
    const val initialPresentCount = 4

    val children = listOf(
        Child(
            id = "sofia-marquez",
            name = "Sofía Márquez",
            initialAttendance = AttendanceStatus.PRESENT,
            hasUnreadChat = false,
        ),
        Child(
            id = "mateo-torres",
            name = "Mateo Torres",
            initialAttendance = AttendanceStatus.PRESENT,
            hasUnreadChat = false,
        ),
        Child(
            id = "lucia-ramirez",
            name = "Lucía Ramírez",
            initialAttendance = AttendanceStatus.PRESENT,
            hasUnreadChat = false,
        ),
        Child(
            id = "diego-alvarez",
            name = "Diego Álvarez",
            initialAttendance = AttendanceStatus.ABSENT,
            hasUnreadChat = false,
        ),
        Child(
            id = "renata-nunez",
            name = "Renata Núñez",
            initialAttendance = AttendanceStatus.PRESENT,
            hasUnreadChat = false,
        ),
    )
}
