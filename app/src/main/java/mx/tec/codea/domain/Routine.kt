package mx.tec.codea.domain

// the three states a moment of the routine can have during the day.
enum class SubprocessStatus { DONE, ACTIVE, PENDING }

// one moment of the daily routine (for example "comida" or "siesta").
// "time" means "confirmed at" when it is done, and "started at" when it is active.
data class Subprocess(
    val number: Int,
    val name: String,
    val status: SubprocessStatus,
    val time: String? = null,
    val description: String? = null,
)

// the whole routine the teacher follows today.
data class Routine(
    val name: String,
    val subprocesses: List<Subprocess>,
) {
    // the moment that is running now. it can be null when the day is over.
    val activeSubprocess: Subprocess?
        get() = subprocesses.firstOrNull { it.status == SubprocessStatus.ACTIVE }

    // we count the done moments plus the active one, like the "4 de 9" of the prototype.
    val currentStep: Int
        get() = subprocesses.count { it.status != SubprocessStatus.PENDING }
}
