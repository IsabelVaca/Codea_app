package mx.tec.codea.domain

import java.util.Locale

// turns "minutes after midnight" into a clock text: 472 -> "07:52".
// we use Locale.ROOT, so the numbers look the same on every phone.
fun formatTimeOfDay(minutes: Int): String =
    String.format(Locale.ROOT, "%02d:%02d", minutes / 60, minutes % 60)

// un lapso en palabras cortas: 45 -> "45 min", 60 -> "1 h", 1006 -> "16 h 46 min". Para que no se lea "1006 minutos".
fun formatDuration(minutes: Int): String {
    val m = minutes.coerceAtLeast(0)
    val horas = m / 60
    val resto = m % 60
    return when {
        horas == 0 -> "$resto min"
        resto == 0 -> "$horas h"
        else -> "$horas h $resto min"
    }
}
