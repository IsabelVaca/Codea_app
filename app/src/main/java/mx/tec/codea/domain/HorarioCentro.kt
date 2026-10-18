package mx.tec.codea.domain

// el horario de todo el centro (hora local del centro). los minutos cuentan desde la medianoche.
// diasDeTrabajo: 1 = lunes … 7 = domingo, como en la API.
data class HorarioCentro(
    val entradaMinutos: Int,
    val salidaMinutos: Int,
    val cierreMinutos: Int,
    val diasDeTrabajo: Set<Int>,
)

// las mismas reglas que valida la API (SCHEDULE_INVALID y WORKDAYS_INVALID).
object HorarioRules {

    val DIAS = listOf(1 to "L", 2 to "M", 3 to "X", 4 to "J", 5 to "V", 6 to "S", 7 to "D")

    // el texto del primer error, o null si el horario se puede guardar.
    fun error(horario: HorarioCentro): String? = when {
        horario.entradaMinutos >= horario.salidaMinutos -> "La entrada debe ser antes de la salida"
        horario.cierreMinutos < horario.salidaMinutos -> "El cierre automático no puede ser antes de la salida"
        horario.diasDeTrabajo.isEmpty() -> "Elige al menos un día de trabajo"
        else -> null
    }

    fun esDiaDeTrabajo(dias: Set<Int>, diaIso: Int): Boolean = diaIso in dias

    // Calendar.DAY_OF_WEEK (domingo = 1 … sábado = 7) a día ISO (lunes = 1 … domingo = 7).
    fun diaIso(calendarDayOfWeek: Int): Int = (calendarDayOfWeek + 5) % 7 + 1
}
