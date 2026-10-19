package mx.tec.codea.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChildProfileRulesTest {

    private val today = Fecha(2026, 10, 19)

    @Test
    fun age_inYearsAndMonths() {
        assertEquals("2 años 4 meses", ChildProfileRules.ageText("2024-06-10", today))
        assertEquals("1 año 1 mes", ChildProfileRules.ageText("2025-09-01", today))
    }

    @Test
    fun age_dropsTheZeroPart() {
        assertEquals("3 años", ChildProfileRules.ageText("2023-10-19", today))
        assertEquals("8 meses", ChildProfileRules.ageText("2026-02-19", today))
        assertEquals("Menos de 1 mes", ChildProfileRules.ageText("2026-10-05", today))
    }

    @Test
    fun age_theMonthIsNotCompleteUntilItsDay() {
        // nació el 25 de junio: el 19 de octubre todavía no cumple el cuarto mes.
        assertEquals("2 años 3 meses", ChildProfileRules.ageText("2024-06-25", today))
    }

    @Test
    fun age_isNullWithoutAUsableDate() {
        assertNull(ChildProfileRules.ageText(null, today))
        assertNull(ChildProfileRules.ageText("no es fecha", today))
        assertNull(ChildProfileRules.ageText("2027-01-01", today)) // de mañana en adelante
    }

    @Test
    fun dialable_keepsOnlyDigitsAndPlus() {
        assertEquals("8112345678", ChildProfileRules.dialable("81 1234-5678"))
        assertEquals("+528112345678", ChildProfileRules.dialable("+52 (81) 1234 5678"))
    }

    @Test
    fun dialable_isNullWhenThereIsNoNumber() {
        assertNull(ChildProfileRules.dialable(null))
        assertNull(ChildProfileRules.dialable("  "))
        assertNull(ChildProfileRules.dialable("123"))
    }
}
