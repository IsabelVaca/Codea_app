package mx.tec.codea.domain

// what the phone knows when the teacher opens the check-in: the date, the hour and when her shift starts.
// el checador no usa ubicación: llegar tarde solo depende de la hora.
data class CheckInAttempt(
    val date: String,
    val arrivalMinutes: Int,
    val shiftStartMinutes: Int,
    val placeName: String,
)

// the two answers the check-in can give.
enum class CheckInStatus { ON_TIME, LATE }

// the check-in that was saved. the rest of the app only needs this.
data class CheckInRecord(
    val arrivalMinutes: Int,
    val placeName: String,
    val minutesLate: Int,
    val lateReason: String,
    // la salida, en minutos desde la medianoche; null mientras no la registre.
    val departureMinutes: Int? = null,
    // true si la salida la puso el cierre automático de las 9 pm y no la docente.
    val autoClosed: Boolean = false,
) {
    val isLate: Boolean
        get() = minutesLate > 0
}
