package mx.tec.codea.data

import mx.tec.codea.domain.Teacher

// where the teacher comes from. today it is fixed data from the prototype;
// when there is a login, only this file changes.
class TeacherRepository {

    fun current(): Teacher = Teacher(
        fullName = "Karla Robles",
        greetingName = "miss Karla",
        roomName = "Preescolar 2",
        shiftStartMinutes = 8 * 60,
        shiftEndMinutes = 16 * 60,
    )
}
