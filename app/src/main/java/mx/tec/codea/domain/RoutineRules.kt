package mx.tec.codea.domain

// the rules of the routine. they are pure kotlin, so they do not know
// about screens or buttons: they only receive a routine and return a new one.
object RoutineRules {

    // a moment that has not started can not have reports yet:
    // nothing happened there, so there is nothing to tell the families.
    fun canReport(subprocess: Subprocess): Boolean =
        subprocess.status != SubprocessStatus.PENDING

    // only the moment in progress can be confirmed.
    fun canConfirm(subprocess: Subprocess): Boolean =
        subprocess.status == SubprocessStatus.ACTIVE

    // closes the active moment at "time" and starts the next pending one.
    // we build a new routine instead of changing the old one, so compose sees the change.
    fun confirm(routine: Routine, number: Int, time: String): Routine {
        val current = routine.subprocesses.firstOrNull { it.number == number }
        if (current == null || !canConfirm(current)) return routine

        val next = routine.subprocesses.firstOrNull {
            it.number > number && it.status == SubprocessStatus.PENDING
        }
        val updated = routine.subprocesses.map { subprocess ->
            when (subprocess.number) {
                number -> subprocess.copy(status = SubprocessStatus.DONE, time = time)
                next?.number -> subprocess.copy(status = SubprocessStatus.ACTIVE, time = time)
                else -> subprocess
            }
        }
        return routine.copy(subprocesses = updated)
    }
}
