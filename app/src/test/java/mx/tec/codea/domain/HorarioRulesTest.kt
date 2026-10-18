package mx.tec.codea.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class HorarioRulesTest {

    // 8:00 a 16:00, cierre a las 21:00, lunes a viernes.
    private val base = HorarioCentro(8 * 60, 16 * 60, 21 * 60, setOf(1, 2, 3, 4, 5))

    @Test
    fun `el horario por omision se puede guardar`() {
        assertNull(HorarioRules.error(base))
    }

    @Test
    fun `la entrada debe ser antes de la salida`() {
        assertNotNull(HorarioRules.error(base.copy(entradaMinutos = 16 * 60)))
        assertNotNull(HorarioRules.error(base.copy(entradaMinutos = 17 * 60)))
    }

    @Test
    fun `el cierre automatico no puede ser antes de la salida`() {
        assertNotNull(HorarioRules.error(base.copy(cierreMinutos = 15 * 60)))
        // igual a la salida si se puede, como en la API.
        assertNull(HorarioRules.error(base.copy(cierreMinutos = 16 * 60)))
    }

    @Test
    fun `hace falta al menos un dia de trabajo`() {
        assertNotNull(HorarioRules.error(base.copy(diasDeTrabajo = emptySet())))
    }

    @Test
    fun `un dia de trabajo se reconoce por su numero`() {
        assertTrue(HorarioRules.esDiaDeTrabajo(base.diasDeTrabajo, 3))
        assertFalse(HorarioRules.esDiaDeTrabajo(base.diasDeTrabajo, 7))
    }

    @Test
    fun `los dias del calendario de Android se pasan a dias ISO`() {
        assertEquals(1, HorarioRules.diaIso(Calendar.MONDAY))
        assertEquals(3, HorarioRules.diaIso(Calendar.WEDNESDAY))
        assertEquals(6, HorarioRules.diaIso(Calendar.SATURDAY))
        assertEquals(7, HorarioRules.diaIso(Calendar.SUNDAY))
    }
}
