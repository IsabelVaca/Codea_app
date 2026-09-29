package mx.tec.codea.data

import mx.tec.codea.domain.Routine
import mx.tec.codea.domain.Subprocess
import mx.tec.codea.domain.SubprocessStatus

// where the routine of today comes from. it is a list in memory, copied from the prototype.
// moments 1, 2, 3, 7, 8 and 9 do not appear in the prototype, so their names are our own.
class RoutineRepository {

    fun getToday(): Routine = Routine(
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
