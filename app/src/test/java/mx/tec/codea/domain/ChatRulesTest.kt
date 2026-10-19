package mx.tec.codea.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatRulesTest {

    private fun sent(id: Int, clientId: String? = null, mine: Boolean = false) =
        ChatMessage(id, clientId, "m$id", "9:00", isMine = mine)

    private fun pending(clientId: String, status: MessageStatus = MessageStatus.SENDING) =
        ChatMessage(0, clientId, "p", "", isMine = true, status = status)

    @Test
    fun merge_dropsDuplicatedIds() {
        val result = ChatRules.merge(listOf(sent(1), sent(2)), listOf(sent(2), sent(3)))
        assertEquals(listOf(1, 2, 3), result.map { it.id })
    }

    @Test
    fun merge_serverMessageReplacesPendingWithSameClientId() {
        val result = ChatRules.merge(listOf(sent(1), pending("a")), listOf(sent(2, clientId = "a", mine = true)))
        assertEquals(listOf(1, 2), result.map { it.id })
        assertTrue(result.all { it.status == MessageStatus.SENT })
    }

    @Test
    fun merge_keepsPendingAndFailedAtTheEnd() {
        val result = ChatRules.merge(
            listOf(sent(1), pending("a"), pending("b", MessageStatus.FAILED)),
            listOf(sent(2)),
        )
        assertEquals(listOf(1, 2, 0, 0), result.map { it.id })
        assertEquals(MessageStatus.FAILED, result.last().status)
    }

    @Test
    fun lastConfirmedId_ignoresPending() {
        assertEquals(5, ChatRules.lastConfirmedId(listOf(sent(3), sent(5), pending("a"))))
        assertNull(ChatRules.lastConfirmedId(listOf(pending("a"))))
    }

    @Test
    fun hasIncoming_onlyForMessagesFromTheOtherPerson() {
        assertTrue(ChatRules.hasIncoming(listOf(sent(1, mine = true), sent(2))))
        assertFalse(ChatRules.hasIncoming(listOf(sent(1, mine = true))))
    }

    @Test
    fun canSend_rejectsBlankText() {
        assertFalse(ChatRules.canSend("   "))
        assertTrue(ChatRules.canSend(" hola "))
    }

    @Test
    fun guardiansLabel_namesTheMotherAndTheFather() {
        val label = ChatRules.guardiansLabel(
            listOf(ChatGuardian("Laura Iglesias", "Mamá"), ChatGuardian("Jorge Flores", "Papá")),
        )
        assertEquals("Mamá: Laura Iglesias · Papá: Jorge Flores", label)
    }

    @Test
    fun guardiansLabel_withoutRelationship_isJustTheName() {
        assertEquals("Rosa Pérez", ChatRules.guardiansLabel(listOf(ChatGuardian("Rosa Pérez", null))))
    }

    @Test
    fun guardiansLabel_withNobody_isNull() {
        assertNull(ChatRules.guardiansLabel(emptyList()))
        assertNull(ChatRules.guardiansLabel(listOf(ChatGuardian("  ", "Mamá"))))
    }

    @Test
    fun canSend_aPhotoWithoutTextIsEnough() {
        assertFalse(ChatRules.canSend("  "))
        assertTrue(ChatRules.canSend("  ", hasPhoto = true))
        assertTrue(ChatRules.canSend("hola"))
    }

    @Test
    fun preview_showsFotoWhenThereIsOnlyAPhoto() {
        assertEquals("hola", ChatRules.preview("hola", hasPhoto = true))
        assertEquals("Foto", ChatRules.preview("", hasPhoto = true))
        assertNull(ChatRules.preview("", hasPhoto = false))
    }
}
