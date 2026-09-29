package mx.tec.codea.domain

import mx.tec.codea.data.RoutineRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineRulesTest {

    private val routine = RoutineRepository().getToday()

    @Test
    fun confirm_closesActiveAndStartsNext() {
        val updated = RoutineRules.confirm(routine, number = 4, time = "11:45")

        assertEquals(SubprocessStatus.DONE, updated.subprocesses[3].status)
        assertEquals("11:45", updated.subprocesses[3].time)
        assertEquals(5, updated.activeSubprocess?.number)
        assertEquals(5, updated.currentStep)
    }

    @Test
    fun confirm_ignoresMomentsThatAreNotActive() {
        assertEquals(routine, RoutineRules.confirm(routine, number = 2, time = "12:00"))
        assertEquals(routine, RoutineRules.confirm(routine, number = 6, time = "12:00"))
    }

    @Test
    fun confirm_lastMomentEndsTheDay() {
        var day = routine
        for (number in 4..9) day = RoutineRules.confirm(day, number, "16:00")

        assertNull(day.activeSubprocess)
        assertEquals(9, day.currentStep)
    }

    @Test
    fun canReport_onlyWhenTheMomentStarted() {
        assertTrue(RoutineRules.canReport(routine.subprocesses[0]))
        assertTrue(RoutineRules.canReport(routine.subprocesses[3]))
        assertFalse(RoutineRules.canReport(routine.subprocesses[4]))
    }
}
