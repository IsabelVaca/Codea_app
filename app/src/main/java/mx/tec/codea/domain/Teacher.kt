package mx.tec.codea.domain

// the teacher who uses the app. today it is always the same person,
// later it will come from the login.
data class Teacher(
    val fullName: String,
    // the short way the app greets her, like "miss Karla".
    val greetingName: String,
    val roomName: String,
    // we save the hours as "minutes after midnight" (8:00 is 480).
    // plain numbers are easy to compare and they do not need android.
    val shiftStartMinutes: Int,
    val shiftEndMinutes: Int,
) {
    // "Karla Robles" -> "KR". the first letter of the first two words.
    val initials: String
        get() = fullName.split(" ").take(2).joinToString("") { it.take(1) }.uppercase()
}
