package mx.tec.codea.domain

// el resumen que la docente captura de cada niño y que el papá ve en Hoy. Los códigos son los de la API.
enum class Animo(val api: String, val etiqueta: String) {
    FELIZ("HAPPY", "Feliz"),
    TRANQUILO("CALM", "Tranquilo"),
    CANSADO("TIRED", "Cansado"),
    TRISTE("SAD", "Triste"),
    MOLESTO("UPSET", "Molesto");

    companion object {
        // un código que no conocemos (la API pudo crecer) se ignora en lugar de romper la pantalla.
        fun deApi(codigo: String?): Animo? = entries.firstOrNull { it.api == codigo }
    }
}

enum class Comida(val api: String, val etiqueta: String) {
    TODO("ALL", "Todo"),
    CASI_TODO("MOST", "Casi todo"),
    MITAD("HALF", "La mitad"),
    POCO("LITTLE", "Poco"),
    NADA("NONE", "Nada");

    companion object {
        fun deApi(codigo: String?): Comida? = entries.firstOrNull { it.api == codigo }
    }
}

object ResumenDelDiaReglas {
    const val PANALES_MAXIMO = 20
    const val SIESTA_PASO = 15
    const val SIESTA_MAXIMA = 360

    fun panales(valor: Int): Int = valor.coerceIn(0, PANALES_MAXIMO)

    // la siesta va de 15 en 15 minutos, de 0 a 6 horas.
    fun siesta(minutos: Int): Int = minutos.coerceIn(0, SIESTA_MAXIMA)

    // 0 -> "Sin siesta", 45 -> "45 min", 90 -> "1 h 30 min", 120 -> "2 h".
    fun textoSiesta(minutos: Int): String = when {
        minutos <= 0 -> "Sin siesta"
        minutos < 60 -> "$minutos min"
        minutos % 60 == 0 -> "${minutos / 60} h"
        else -> "${minutos / 60} h ${minutos % 60} min"
    }
}
