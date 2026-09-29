package mx.tec.codea.domain

import mx.tec.codea.data.CheckInRepository
import mx.tec.codea.data.CheckInScenario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckInRulesTest {

    private val repository = CheckInRepository()

    @Test
    fun onTime_insideTheSchoolAndEarly() {
        val attempt = repository.attemptFor(CheckInScenario.ON_TIME)

        assertEquals(CheckInStatus.ON_TIME, CheckInRules.statusOf(attempt))
        assertEquals(8, CheckInRules.minutesEarly(attempt))
    }

    @Test
    fun late_registersButMarksTheDelay() {
        val attempt = repository.attemptFor(CheckInScenario.LATE)
        val record = CheckInRules.register(attempt, lateReason = "  tráfico  ")

        assertEquals(CheckInStatus.LATE, CheckInRules.statusOf(attempt))
        assertNotNull(record)
        assertEquals(19, record!!.minutesLate)
        assertTrue(record.isLate)
        assertEquals("tráfico", record.lateReason)
    }

    @Test
    fun outOfRange_doesNotRegister() {
        val attempt = repository.attemptFor(CheckInScenario.OUT_OF_RANGE)

        assertEquals(CheckInStatus.OUT_OF_RANGE, CheckInRules.statusOf(attempt))
        assertNull(CheckInRules.register(attempt, lateReason = ""))
    }

    @Test
    fun formatTimeOfDay_addsLeadingZeros() {
        assertEquals("07:52", formatTimeOfDay(7 * 60 + 52))
        assertEquals("16:00", formatTimeOfDay(16 * 60))
    }
}
