package mx.tec.codea.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonaAutorizadaRulesTest {

    @Test
    fun onlyTheNameIsRequired() {
        assertTrue(PersonaAutorizadaRules.puedeAgregar("Andrea Torres"))
        assertFalse(PersonaAutorizadaRules.puedeAgregar(""))
        assertFalse(PersonaAutorizadaRules.puedeAgregar("   "))
    }

    @Test
    fun theListIsSortedByNameIgnoringCase() {
        val personas = listOf(
            PersonaAutorizada("1", "luis Torres"),
            PersonaAutorizada("2", "Andrea Torres"),
            PersonaAutorizada("3", "Beatriz Pérez"),
        )
        assertEquals(listOf("Andrea Torres", "Beatriz Pérez", "luis Torres"), PersonaAutorizadaRules.ordenadas(personas).map { it.nombre })
    }
}
