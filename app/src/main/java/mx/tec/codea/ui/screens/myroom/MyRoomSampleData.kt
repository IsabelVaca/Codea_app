package mx.tec.codea.ui.screens.myroom

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
}

data class Child(
    val id: String,
    val name: String,
    val hasUnreadChat: Boolean,
)

object MyRoomSampleData {
    val children = listOf(
        Child(
            id = "sofia-marquez",
            name = "Sofía Márquez",
            hasUnreadChat = false,
        ),
        Child(
            id = "mateo-torres",
            name = "Mateo Torres",
            hasUnreadChat = false,
        ),
        Child(
            id = "lucia-ramirez",
            name = "Lucía Ramírez",
            hasUnreadChat = false,
        ),
        Child(
            id = "diego-alvarez",
            name = "Diego Álvarez",
            hasUnreadChat = false,
        ),
        Child(
            id = "renata-nunez",
            name = "Renata Núñez",
            hasUnreadChat = false,
        ),
    )
}
