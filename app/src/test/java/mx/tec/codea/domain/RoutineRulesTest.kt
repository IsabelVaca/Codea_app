package mx.tec.codea.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineRulesTest {

    // el día de ejemplo del prototipo: tres momentos hechos, el cuarto en curso y cinco pendientes.
    private val routine = Routine(
        name = "Día completo · Preescolar",
        subprocesses = listOf(
            Subprocess(1, "Recepción y bienvenida", SubprocessStatus.DONE, time = "8:10"),
            Subprocess(2, "Desayuno", SubprocessStatus.DONE, time = "9:05"),
            Subprocess(3, "Trazos y motricidad", SubprocessStatus.DONE, time = "10:05"),
            Subprocess(
                number = 4,
                name = "Patio y juego libre",
                status = SubprocessStatus.ACTIVE,
                time = "11:20",
                description = "14 niños en el patio chico.",
            ),
            Subprocess(5, "Comida", SubprocessStatus.PENDING),
            Subprocess(6, "Siesta", SubprocessStatus.PENDING),
            Subprocess(7, "Higiene y cambio", SubprocessStatus.PENDING),
            Subprocess(8, "Actividad de la tarde", SubprocessStatus.PENDING),
            Subprocess(9, "Entrega a familias", SubprocessStatus.PENDING),
        ),
    )

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
