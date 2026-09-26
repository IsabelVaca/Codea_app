package mx.tec.codea.ui.screens.myday

// the three states a subprocess of the routine can have during the day.
enum class SubprocessStatus { DONE, ACTIVE, PENDING }

// one step of the daily routine (for example "comida" or "siesta").
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
)

// fixed data copied from the prototype, so we can build the screen before
// we have a real backend. when the data comes from the server, delete this object.
// subprocesses 1, 2, 3, 7, 8 and 9 do not appear in the prototype, so their names are our own.
object MyDaySampleData {
    val routine = Routine(
        name = "Día completo · Preescolar",
        subprocesses = listOf(
            Subprocess(1, "Recepción y bienvenida", SubprocessStatus.DONE, time = "8:10"),
            Subprocess(2, "Desayuno", SubprocessStatus.DONE, time = "9:05"),
            Subprocess(3, "Trazos y motricidad", SubprocessStatus.DONE, time = "10:05"),
            Subprocess(
                number = 4,
                name = "Patio y juego libre",
                status = SubprocessStatus.ACTIVE,
                time = "11:20",
                description = "14 niños en el patio chico.",
            ),
            Subprocess(5, "Comida", SubprocessStatus.PENDING),
            Subprocess(6, "Siesta", SubprocessStatus.PENDING),
            Subprocess(7, "Higiene y cambio", SubprocessStatus.PENDING),
            Subprocess(8, "Actividad de la tarde", SubprocessStatus.PENDING),
            Subprocess(9, "Entrega a familias", SubprocessStatus.PENDING),
        ),
    )
}
