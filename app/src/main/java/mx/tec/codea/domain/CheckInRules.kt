package mx.tec.codea.domain

// the rules of the check-in. no hay ubicación: la docente siempre puede registrar su entrada.
// si llega tarde, se registra igual, pero queda marcado.
object CheckInRules {

    fun statusOf(attempt: CheckInAttempt): CheckInStatus =
        if (minutesLate(attempt) > 0) CheckInStatus.LATE else CheckInStatus.ON_TIME

    fun minutesLate(attempt: CheckInAttempt): Int =
        (attempt.arrivalMinutes - attempt.shiftStartMinutes).coerceAtLeast(0)

    fun minutesEarly(attempt: CheckInAttempt): Int =
        (attempt.shiftStartMinutes - attempt.arrivalMinutes).coerceAtLeast(0)

    fun register(attempt: CheckInAttempt, lateReason: String): CheckInRecord =
        CheckInRecord(
            arrivalMinutes = attempt.arrivalMinutes,
            placeName = attempt.placeName,
            minutesLate = minutesLate(attempt),
            lateReason = lateReason.trim(),
        )
}
