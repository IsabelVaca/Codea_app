package mx.tec.codea.data

import mx.tec.codea.data.api.RoomChildDto
import mx.tec.codea.data.api.RoomNoteDto
import mx.tec.codea.domain.AttendanceStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomChildMappingTest {

    private fun dto(attendance: String, notes: List<RoomNoteDto> = emptyList(), summary: Boolean = false) = RoomChildDto(
        id = 1,
        name = "Sofía",
        fullName = "Sofía Márquez",
        attendance = attendance,
        notes = notes,
        hasSummaryToday = summary,
    )

    @Test
    fun onlyPresent_countsAsPresent() {
        assertEquals(AttendanceStatus.PRESENT, dto("PRESENT").toChild().attendance)
        assertEquals(AttendanceStatus.ABSENT, dto("ABSENT").toChild().attendance)
        // un valor que no conocemos nunca cuenta como presente: sin pase de lista, falta.
        assertEquals(AttendanceStatus.ABSENT, dto("NO_RECORD").toChild().attendance)
    }

    @Test
    fun notesAndSummary_arrive() {
        val child = dto(
            "PRESENT",
            notes = listOf(RoomNoteDto(5, "Hoy no durmió bien.", "2026-10-19T13:40:00+00:00")),
            summary = true,
        ).toChild()
        assertTrue(child.hasNoteToday)
        assertEquals(listOf(5), child.notes.map { it.id })
        assertEquals("Hoy no durmió bien.", child.notes.single().text)
        assertTrue(child.hasSummaryToday)
        assertFalse(dto("PRESENT").toChild().hasNoteToday)
    }
}
