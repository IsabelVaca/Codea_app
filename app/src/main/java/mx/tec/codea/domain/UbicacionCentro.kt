package mx.tec.codea.domain

// dónde está la estancia y qué tan cerca debe estar la docente para registrar su entrada.
data class UbicacionCentro(
    val latitud: Double?,
    val longitud: Double?,
    val radioMetros: Int,
) {
    val definida: Boolean
        get() = latitud != null && longitud != null

    companion object {
        // los mismos límites que valida la API (RADIUS_INVALID).
        const val RADIO_MIN_M = 10
        const val RADIO_MAX_M = 1000

        // el texto del campo de radio, ya como número, o null si no se puede guardar.
        fun radioDesdeTexto(texto: String): Int? =
            texto.trim().toIntOrNull()?.takeIf { it in RADIO_MIN_M..RADIO_MAX_M }
    }
}
