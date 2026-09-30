package mx.tec.codea.domain

// what the phone knows when the teacher opens the check-in:
// the hour, and where she is compared with the school.
data class CheckInAttempt(
    val date: String,
    val arrivalMinutes: Int,
    val shiftStartMinutes: Int,
    val placeName: String,
    val distanceMeters: Int,
    val allowedRadiusMeters: Int,
)

// the three answers the check-in can give.
enum class CheckInStatus { ON_TIME, LATE, OUT_OF_RANGE }

// the check-in that was saved. the rest of the app only needs this.
data class CheckInRecord(
    val arrivalMinutes: Int,
    val placeName: String,
    val minutesLate: Int,
    val lateReason: String,
) {
    val isLate: Boolean
        get() = minutesLate > 0
}
