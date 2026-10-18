package mx.tec.codea.domain

// lo que ve el papá en "Resumen de la semana" (RF-12), ya en palabras.
enum class EstadoDia { ASISTIO, FALTO, SIN_REGISTRO }

data class DiaSemana(val letra: String, val estado: EstadoDia)

data class ResumenSemana(
    // "12 al 16 de octubre".
    val rango: String,
    val diasAsistidos: Int,
    val diasAusente: Int,
    val animo: String,
    val comidas: String,
    val siesta: String,
    val cambios: Int,
    val logros: List<String>,
    val notasFamilia: Int,
    val dias: List<DiaSemana>,
)

// las palabras del resumen. vive en el dominio (sin Android) para probarlo con JUnit.
object ResumenSemanaTextos {

    private val animos = linkedMapOf(
        "HAPPY" to "Feliz", "CALM" to "Tranquilo", "TIRED" to "Cansado", "SAD" to "Triste", "UPSET" to "Molesto",
    )
    private val comidas = linkedMapOf(
        "ALL" to "Todo", "MOST" to "Casi todo", "HALF" to "La mitad", "LITTLE" to "Poco", "NONE" to "Nada",
    )

    fun animo(conteo: Map<String, Int>): String = describir(conteo, animos)

    fun comidas(conteo: Map<String, Int>): String = describir(conteo, comidas)

    // "Feliz ×2 · Tranquilo ×1": lo más frecuente primero; si empatan, en el orden de la lista.
    // "—" si no se registró nada. lo que la API mande y no conozcamos se ignora.
    private fun describir(conteo: Map<String, Int>, etiquetas: Map<String, String>): String {
        val orden = etiquetas.keys.toList()
        val partes = conteo.entries
            .filter { it.value > 0 && it.key in etiquetas }
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { orden.indexOf(it.key) })
            .map { if (it.value == 1) etiquetas.getValue(it.key) else "${etiquetas.getValue(it.key)} ×${it.value}" }
        return if (partes.isEmpty()) "—" else partes.joinToString(" · ")
    }

    // 75 -> "75 min", null -> "—".
    fun siesta(promedioMinutos: Int?): String = promedioMinutos?.let { "$it min" } ?: "—"

    fun estado(api: String): EstadoDia = when (api) {
        "ATTENDED" -> EstadoDia.ASISTIO
        "ABSENT" -> EstadoDia.FALTO
        else -> EstadoDia.SIN_REGISTRO
    }

    val letrasDeLaSemana = listOf("L", "M", "M", "J", "V")
}
