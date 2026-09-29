package mx.tec.codea.domain

import java.util.Locale

// turns "minutes after midnight" into a clock text: 472 -> "07:52".
// we use Locale.ROOT, so the numbers look the same on every phone.
fun formatTimeOfDay(minutes: Int): String =
    String.format(Locale.ROOT, "%02d:%02d", minutes / 60, minutes % 60)
