package mx.tec.codea.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChildHealthRulesTest {

    private val saved = ChildHealth(allergies = "Nueces", intolerances = "Lactosa")

    @Test
    fun anEmptyHealth_isEmpty() {
        assertTrue(ChildHealth().isEmpty)
        assertTrue(ChildHealth(allergies = "   ").isEmpty)
        assertFalse(saved.isEmpty)
    }

    @Test
    fun clean_trimsEveryField() {
        val clean = ChildHealthRules.clean(ChildHealth(allergies = "  Nueces ", otherNotes = "\nMiedo a la oscuridad "))
        assertEquals("Nueces", clean.allergies)
        assertEquals("Miedo a la oscuridad", clean.otherNotes)
    }

    @Test
    fun save_isOffWhileNothingChanged() {
        assertFalse(ChildHealthRules.canSave(saved, saved))
        // un espacio de más no es un cambio.
        assertFalse(ChildHealthRules.canSave(saved, saved.copy(allergies = "Nueces ")))
    }

    @Test
    fun save_turnsOnWhenSomethingChanges() {
        assertTrue(ChildHealthRules.canSave(saved, saved.copy(medicalConditions = "Asma leve")))
        // borrar un dato también es un cambio.
        assertTrue(ChildHealthRules.canSave(saved, saved.copy(allergies = "")))
    }

    @Test
    fun save_isOffWhenAFieldDoesNotFitTheApi() {
        val tooLong = "a".repeat(ChildHealthRules.MAX_LENGTH + 1)
        assertTrue(ChildHealthRules.isTooLong(tooLong))
        assertFalse(ChildHealthRules.canSave(saved, saved.copy(otherNotes = tooLong)))
        // justo en el límite sí cabe.
        assertTrue(ChildHealthRules.canSave(saved, saved.copy(otherNotes = "a".repeat(ChildHealthRules.MAX_LENGTH))))
    }
}
