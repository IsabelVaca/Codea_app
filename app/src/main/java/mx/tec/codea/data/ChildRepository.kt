package mx.tec.codea.data

import mx.tec.codea.domain.AttendanceStatus
import mx.tec.codea.domain.Child

// the children of the room, the same five cards of the prototype.
class ChildRepository {

    fun getAll(): List<Child> = listOf(
        Child("sofia-marquez", "Sofía Márquez", AttendanceStatus.PRESENT, hasUnreadChat = true),
        Child("mateo-torres", "Mateo Torres", AttendanceStatus.PRESENT, hasUnreadChat = true),
        Child("lucia-ramirez", "Lucía Ramírez", AttendanceStatus.PRESENT, hasUnreadChat = false),
        Child("diego-alvarez", "Diego Álvarez", AttendanceStatus.ABSENT, hasUnreadChat = true),
        Child("renata-nunez", "Renata Núñez", AttendanceStatus.PRESENT, hasUnreadChat = false),
    )
}
