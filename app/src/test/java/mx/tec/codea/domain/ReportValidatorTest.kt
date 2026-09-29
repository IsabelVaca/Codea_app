package mx.tec.codea.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportValidatorTest {

    @Test
    fun description_needsTenCharactersWithoutSpaces() {
        assertFalse(ReportValidator.isDescriptionValid("   corto   "))
        assertTrue(ReportValidator.isDescriptionValid("Raspón en la rodilla"))
    }

    @Test
    fun report_needsAtLeastOneChild() {
        assertFalse(ReportValidator.isValid("Raspón en la rodilla", emptySet()))
        assertTrue(ReportValidator.isValid("Raspón en la rodilla", setOf("mateo-torres")))
    }

    @Test
    fun absentChild_cannotBeInReport() {
        val absent = Child("diego-alvarez", "Diego Álvarez", AttendanceStatus.ABSENT, hasUnreadChat = false)
        assertFalse(ReportValidator.canBeInReport(absent))
        assertTrue(ReportValidator.canBeInReport(absent.copy(attendance = AttendanceStatus.PRESENT)))
    }
}
