package mx.tec.codea.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckInRulesTest {

    // turno de las 8:00; cada prueba cambia solo la hora de llegada.
    private fun attempt(arrivalMinutes: Int) = CheckInAttempt(
        date = "Miércoles 14 de octubre",
        arrivalMinutes = arrivalMinutes,
        shiftStartMinutes = 8 * 60,
        placeName = "La Oruga",
    )

    @Test
    fun onTime_beforeTheShift() {
        val attempt = attempt(7 * 60 + 52)

        assertEquals(CheckInStatus.ON_TIME, CheckInRules.statusOf(attempt))
        assertEquals(8, CheckInRules.minutesEarly(attempt))
        assertEquals(0, CheckInRules.minutesLate(attempt))
    }

    @Test
    fun exactlyAtTheStart_isOnTime() {
        assertEquals(CheckInStatus.ON_TIME, CheckInRules.statusOf(attempt(8 * 60)))
    }

    @Test
    fun late_registersButMarksTheDelay() {
        val attempt = attempt(8 * 60 + 19)
        val record = CheckInRules.register(attempt, lateReason = "  tráfico  ")

        assertEquals(CheckInStatus.LATE, CheckInRules.statusOf(attempt))
        assertEquals(19, record.minutesLate)
        assertTrue(record.isLate)
        assertEquals("tráfico", record.lateReason)
    }

    @Test
    fun onTime_recordIsNotLateAndHasNoExit() {
        val record = CheckInRules.register(attempt(7 * 60 + 52), lateReason = "")

        assertFalse(record.isLate)
        assertEquals(null, record.departureMinutes)
        assertFalse(record.autoClosed)
    }

    @Test
    fun formatTimeOfDay_addsLeadingZeros() {
        assertEquals("07:52", formatTimeOfDay(7 * 60 + 52))
        assertEquals("16:00", formatTimeOfDay(16 * 60))
    }
}
