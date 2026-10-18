package mx.tec.codea.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// la API manda fechas ISO ("2026-10-14T14:05:00.123+00:00"). aquí se pasan a la hora del teléfono.
// no se usa java.time porque minSdk es 24.
fun parseIso(iso: String?): Date? {
    if (iso.isNullOrBlank()) return null
    return try {
        val limpio = iso.replace(Regex("\\.\\d+"), "").let {
            if (it.endsWith("Z")) it.dropLast(1) + "+00:00" else it
        }
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).parse(limpio)
    } catch (e: Exception) {
        null
    }
}

// "8:05"
fun clockOf(iso: String?): String? =
    parseIso(iso)?.let { SimpleDateFormat("H:mm", Locale.getDefault()).format(it) }

// minutos después de medianoche: 8:05 -> 485
fun minutesOfDay(iso: String?): Int? {
    val fecha = parseIso(iso) ?: return null
    val c = Calendar.getInstance().apply { time = fecha }
    return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
}

// "08:00:00" -> 480
fun timeToMinutes(hora: String?): Int? {
    val partes = hora?.split(":") ?: return null
    val h = partes.getOrNull(0)?.toIntOrNull() ?: return null
    val m = partes.getOrNull(1)?.toIntOrNull() ?: 0
    return h * 60 + m
}

// "2026-10-18": el día de hoy según el teléfono, como lo pide la API.
fun hoyIso(): String {
    val c = Calendar.getInstance()
    return String.format(Locale.US, "%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
}

// el día de hoy según el teléfono, para las validaciones que dependen de la fecha.
fun hoyLocal(): mx.tec.codea.domain.Fecha {
    val c = Calendar.getInstance()
    return mx.tec.codea.domain.Fecha(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
}

// "23 oct": para mostrar cuándo vence algo.
fun shortDateOf(iso: String?): String? =
    parseIso(iso)?.let { SimpleDateFormat("d MMM", Locale("es", "MX")).format(it) }
