package mx.tec.codea.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeparturePassRulesTest {

    private fun moment(number: Int, status: SubprocessStatus) = Subprocess(number, "Momento $number", status)

    private fun entry(id: String, departed: Boolean = false) = DepartureEntry(
        childId = id,
        name = "Niño $id",
        choices = listOf(PickupChoice(PickupKind.GUARDIAN, "g$id", "Mamá $id", "Mamá")),
        time = if (departed) "15:40" else null,
        pickedUpBy = if (departed) "Mamá $id" else null,
    )

    @Test
    fun routineIsFinished_onlyWhenEveryMomentIsDone() {
        val done = Routine("Día", listOf(moment(1, SubprocessStatus.DONE), moment(2, SubprocessStatus.DONE)))
        val active = Routine("Día", listOf(moment(1, SubprocessStatus.DONE), moment(2, SubprocessStatus.ACTIVE)))
        val pending = Routine("Día", listOf(moment(1, SubprocessStatus.DONE), moment(2, SubprocessStatus.PENDING)))
        assertTrue(DepartureRules.routineFinished(done))
        assertFalse(DepartureRules.routineFinished(active))
        assertFalse(DepartureRules.routineFinished(pending))
    }

    @Test
    fun aRoutineWithoutMoments_isNeverFinished() {
        assertFalse(DepartureRules.routineFinished(Routine("Día", emptyList())))
    }

    @Test
    fun anEntryIsDeparted_onlyWhenSomeoneWasChosen() {
        assertFalse(entry("1").departed)
        assertTrue(entry("1", departed = true).departed)
    }

    @Test
    fun pending_countsTheChildrenThatHaveNotLeft() {
        val entries = listOf(entry("1", departed = true), entry("2"), entry("3"))
        assertEquals(2, DepartureRules.pending(entries))
    }

    @Test
    fun finishDay_needsEveryPresentChildOut() {
        assertFalse(DepartureRules.canFinishDay(listOf(entry("1", departed = true), entry("2")), loaded = true))
        assertTrue(DepartureRules.canFinishDay(listOf(entry("1", departed = true), entry("2", departed = true)), loaded = true))
    }

    @Test
    fun finishDay_isNotOfferedWhileTheListIsStillLoadingOrFailed() {
        assertFalse(DepartureRules.canFinishDay(emptyList(), loaded = false))
    }

    @Test
    fun finishDay_whenNobodyCameToday_isAllowed() {
        assertTrue(DepartureRules.canFinishDay(emptyList(), loaded = true))
    }
}
