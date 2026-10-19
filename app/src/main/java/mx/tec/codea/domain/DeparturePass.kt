package mx.tec.codea.domain

// de dónde sale quien puede recoger al niño: una persona autorizada (la registró el admin) o uno de sus tutores.
enum class PickupKind { AUTHORIZED, GUARDIAN }

data class PickupChoice(
    val kind: PickupKind,
    // el id que pide la API: el número de la persona autorizada o el uuid del tutor.
    val id: String,
    val name: String,
    // "Mamá", "Tía"…; null si no se sabe.
    val relationship: String?,
)

// un niño en el pase de salida: ya salió (con quién y a qué hora) o todavía falta.
data class DepartureEntry(
    val childId: String,
    val name: String,
    val choices: List<PickupChoice>,
    // "15:42"; null mientras no salga.
    val time: String? = null,
    val pickedUpBy: String? = null,
) {
    val departed: Boolean
        get() = pickedUpBy != null
}

object DepartureRules {

    // el pase de salida se abre cuando ya no queda ningún momento de la rutina por hacer.
    fun routineFinished(routine: Routine): Boolean =
        routine.subprocesses.isNotEmpty() && routine.subprocesses.all { it.status == SubprocessStatus.DONE }

    fun pending(entries: List<DepartureEntry>): Int = entries.count { !it.departed }

    // "Finalizar día" solo aparece cuando ya salieron todos los que vinieron. sin niños presentes no hay a quién esperar.
    fun canFinishDay(entries: List<DepartureEntry>, loaded: Boolean): Boolean = loaded && pending(entries) == 0
}
