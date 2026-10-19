package mx.tec.codea.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChildChoiceTest {

    private val mateo = ChildOption(1, "Mateo", "Mateo Iglesias")
    private val lucia = ChildOption(2, "Lucía", "Lucía Iglesias")

    @Test
    fun pick_keepsTheCurrentChildWhenTheListReloads() {
        assertEquals(lucia, ChildChoice.pick(listOf(mateo, lucia), current = 2))
    }

    @Test
    fun pick_fallsBackToTheFirstWhenThereIsNoSelectionOrItDisappeared() {
        assertEquals(mateo, ChildChoice.pick(listOf(mateo, lucia), current = null))
        assertEquals(mateo, ChildChoice.pick(listOf(mateo, lucia), current = 99))
    }

    @Test
    fun pick_isNullWithoutChildren() {
        assertNull(ChildChoice.pick(emptyList(), current = 1))
    }

    @Test
    fun needsSelector_onlyWithMoreThanOneChild() {
        assertFalse(ChildChoice.needsSelector(emptyList()))
        assertFalse(ChildChoice.needsSelector(listOf(mateo)))
        assertTrue(ChildChoice.needsSelector(listOf(mateo, lucia)))
    }
}
