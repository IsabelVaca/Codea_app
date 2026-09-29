package mx.tec.codea.domain

// the rules of the check-in, copied from the "reglas del branching" of the prototype.
object CheckInRules {

    // outside the school the teacher can not check in. late, she can, but it shows.
    fun statusOf(attempt: CheckInAttempt): CheckInStatus = when {
        attempt.distanceMeters > attempt.allowedRadiusMeters -> CheckInStatus.OUT_OF_RANGE
        minutesLate(attempt) > 0 -> CheckInStatus.LATE
        else -> CheckInStatus.ON_TIME
    }

    fun minutesLate(attempt: CheckInAttempt): Int =
        (attempt.arrivalMinutes - attempt.shiftStartMinutes).coerceAtLeast(0)

    fun minutesEarly(attempt: CheckInAttempt): Int =
        (attempt.shiftStartMinutes - attempt.arrivalMinutes).coerceAtLeast(0)

    fun canRegister(attempt: CheckInAttempt): Boolean =
        statusOf(attempt) != CheckInStatus.OUT_OF_RANGE

    // returns null when the rules say "no": there is nothing to save.
    fun register(attempt: CheckInAttempt, lateReason: String): CheckInRecord? {
        if (!canRegister(attempt)) return null
        return CheckInRecord(
            arrivalMinutes = attempt.arrivalMinutes,
            placeName = attempt.placeName,
            minutesLate = minutesLate(attempt),
            lateReason = lateReason.trim(),
        )
    }
}
