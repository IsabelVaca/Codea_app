package mx.tec.codea.data

import mx.tec.codea.domain.CheckInAttempt

// there is no real clock or gps yet, so the prototype lets us pick the situation
// we want to show. each one is a screen of "registrar entrada" in the html prototype.
enum class CheckInScenario { ON_TIME, LATE, OUT_OF_RANGE }

// where the check-in data comes from. the coordinates are our own (a spot in monterrey),
// because the prototype has no real place. when the clock and the gps are real,
// only this file changes.
class CheckInRepository {

    private val schoolLatitude = 25.651600
    private val schoolLongitude = -100.289600

    // the same base for the three cases. each case only changes what is different.
    private val base = CheckInAttempt(
        date = "Miércoles 19 de agosto",
        arrivalMinutes = 7 * 60 + 52,
        shiftStartMinutes = 8 * 60,
        placeName = "La Oruga",
        distanceMeters = 12,
        allowedRadiusMeters = 60,
        schoolLatitude = schoolLatitude,
        schoolLongitude = schoolLongitude,
        // about 12 m north of the school.
        teacherLatitude = 25.651708,
        teacherLongitude = schoolLongitude,
    )

    fun attemptFor(scenario: CheckInScenario): CheckInAttempt = when (scenario) {
        // screen 2: inside the school, 8 minutes early.
        CheckInScenario.ON_TIME -> base
        // e2: inside the school, 19 minutes late.
        CheckInScenario.LATE -> base.copy(
            arrivalMinutes = 8 * 60 + 19,
            distanceMeters = 8,
            teacherLatitude = 25.651672,
        )
        // e1: 1.4 km away, still on the way.
        CheckInScenario.OUT_OF_RANGE -> base.copy(
            arrivalMinutes = 7 * 60 + 48,
            distanceMeters = 1400,
            teacherLatitude = 25.664200,
        )
    }
}
