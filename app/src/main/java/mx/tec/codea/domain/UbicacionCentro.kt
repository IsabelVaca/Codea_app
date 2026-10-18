package mx.tec.codea.domain

// dónde está la estancia y qué tan cerca debe estar la docente para registrar su entrada.
data class UbicacionCentro(
    val latitud: Double?,
    val longitud: Double?,
    val radioMetros: Int,
) {
    val definida: Boolean
        get() = latitud != null && longitud != null
}
