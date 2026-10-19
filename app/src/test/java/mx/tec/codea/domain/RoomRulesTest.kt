package mx.tec.codea.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomRulesTest {

    private fun child(
        id: String,
        name: String,
        attendance: AttendanceStatus = AttendanceStatus.PRESENT,
        unread: Boolean = false,
        notes: List<FamilyNote> = emptyList(),
        summary: Boolean = false,
    ) = Child(id, name, attendance, hasUnreadChat = unread, notes = notes, hasSummaryToday = summary)

    private fun note(id: Int, text: String = "Aviso $id") = FamilyNote(id, text, "7:40")

    @Test
    fun withoutRollCall_nobodyIsPresent() {
        val room = listOf(child("1", "Ana", AttendanceStatus.ABSENT), child("2", "Beto", AttendanceStatus.ABSENT))
        assertEquals(0 to 2, RoomRules.presentOutOf(room))
        assertTrue(RoomRules.present(room).isEmpty())
    }

    @Test
    fun presentFirst_thoseWithSomethingPending_thenByName() {
        val room = listOf(
            child("1", "Ana"),
            child("2", "Beto", unread = true),
            child("3", "Carla", notes = listOf(note(7))),
            child("4", "Diego", attendance = AttendanceStatus.ABSENT, unread = true),
        )
        // Diego está ausente: no entra en la lista principal aunque tenga un chat sin leer.
        assertEquals(listOf("Beto", "Carla", "Ana"), RoomRules.presentSorted(room, seen = emptySet()).map { it.name })
    }

    @Test
    fun aSeenNote_nolongerCountsAsPending() {
        val carla = child("3", "Carla", notes = listOf(note(7)))
        assertTrue(RoomRules.needsAttention(carla, seen = emptySet()))
        assertFalse(RoomRules.needsAttention(carla, seen = setOf(7)))
        assertEquals(emptyList<FamilyNote>(), RoomRules.unseenNotes(carla, seen = setOf(7)))
    }

    @Test
    fun notices_includeAbsentChildren_newestFirst() {
        val room = listOf(
            child("1", "Ana", notes = listOf(note(1))),
            // la mamá avisa que llega tarde: el niño aún figura ausente, pero el aviso se tiene que leer.
            child("2", "Beto", attendance = AttendanceStatus.ABSENT, notes = listOf(note(2), note(3))),
        )
        assertEquals(listOf(3, 2, 1), RoomRules.notices(room, seen = emptySet(), onlyUnseen = true).map { it.note.id })
        assertEquals(listOf(3, 1), RoomRules.notices(room, seen = setOf(2), onlyUnseen = true).map { it.note.id })
        assertEquals(listOf(3, 2, 1), RoomRules.notices(room, seen = setOf(2), onlyUnseen = false).map { it.note.id })
    }

    @Test
    fun filters_countAndListOnlyPresentChildren() {
        val room = listOf(
            child("1", "Ana", notes = listOf(note(1)), summary = true),
            child("2", "Beto", unread = true),
            child("3", "Carla", summary = true),
            child("4", "Diego", attendance = AttendanceStatus.ABSENT, notes = listOf(note(2))),
        )
        val seen = emptySet<Int>()
        assertEquals(3, RoomRules.count(room, RoomFilter.ALL, seen))
        assertEquals(1, RoomRules.count(room, RoomFilter.WITH_NOTE, seen)) // el aviso de Diego no cuenta: está ausente
        assertEquals(1, RoomRules.count(room, RoomFilter.WITH_CHAT, seen))
        assertEquals(1, RoomRules.count(room, RoomFilter.NO_SUMMARY, seen))
        assertEquals(listOf("Beto"), RoomRules.filtered(room, RoomFilter.NO_SUMMARY, seen).map { it.name })
    }

    @Test
    fun noteFilter_dropsTheChildWhenItsNoteIsSeen() {
        val room = listOf(child("1", "Ana", notes = listOf(note(1))))
        assertEquals(1, RoomRules.count(room, RoomFilter.WITH_NOTE, seen = emptySet()))
        assertEquals(0, RoomRules.count(room, RoomFilter.WITH_NOTE, seen = setOf(1)))
    }

    @Test
    fun summaryQueue_isThePresentChildrenWithoutSummary_byName() {
        val room = listOf(
            child("1", "Carla"),
            child("2", "Ana", summary = true),
            child("3", "Beto"),
            child("4", "Diego", attendance = AttendanceStatus.ABSENT),
        )
        assertEquals(listOf("Beto", "Carla"), RoomRules.pendingSummaries(room).map { it.name })
        assertEquals(emptyList<Child>(), RoomRules.pendingSummaries(room.map { it.copy(hasSummaryToday = true) }))
    }
}
