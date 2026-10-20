package mx.tec.codea.domain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// lo que se come en el centro un día (HN3). cada comida es opcional, pero el menú lleva al menos una.
data class MenuDelDia(
    val desayuno: String = "",
    val comida: String = "",
    val colacion: String = "",
) {
    val vacio: Boolean
        get() = desayuno.isBlank() && comida.isBlank() && colacion.isBlank()

    // las comidas que traen texto, en orden, con su nombre: así las dibujan las pantallas.
    val comidas: List<Pair<String, String>>
        get() = listOf("Desayuno" to desayuno, "Comida" to comida, "Colación" to colacion)
            .filter { it.second.isNotBlank() }
}

// las cuentas de fechas del menú. las fechas viajan como "aaaa-mm-dd".
// no se usa java.time porque minSdk es 24.
object MenuRules {

    // el largo máximo de cada comida, el mismo que acepta la API.
    const val MAX_CARACTERES = 500

    fun puedeGuardar(menu: MenuDelDia): Boolean =
        !menu.vacio && listOf(menu.desayuno, menu.comida, menu.colacion).all { it.length <= MAX_CARACTERES }

    // el lunes de la semana de esa fecha (si ya es lunes, la misma).
    fun lunesDe(fecha: String): String {
        val dia = calendario(fecha)
        val atras = (dia.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
        dia.add(Calendar.DAY_OF_MONTH, -atras)
        return iso(dia)
    }

    fun sumarDias(fecha: String, dias: Int): String =
        calendario(fecha).also { it.add(Calendar.DAY_OF_MONTH, dias) }.let(::iso)

    // de lunes a viernes de la semana que empieza en ese lunes.
    fun diasHabiles(lunes: String): List<String> = (0..4).map { sumarDias(lunes, it) }

    private fun calendario(fecha: String): Calendar {
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        return Calendar.getInstance().apply { time = formato.parse(fecha) ?: error("Fecha inválida: $fecha") }
    }

    private fun iso(dia: Calendar): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(dia.time)
}
