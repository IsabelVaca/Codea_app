package mx.tec.codea.domain

sealed interface InfanteError {
    data object NombreVacio : InfanteError
    data object NacimientoInvalido : InfanteError
}

// un día del calendario, sin hora ni zona. se pasa "hoy" desde fuera para poder probarlo sin el reloj.
data class Fecha(val anio: Int, val mes: Int, val dia: Int)

object InfanteValidator {
    // la guardería recibe niños de 0 a 12 años (la API usa la misma regla).
    private const val EDAD_MAXIMA = 12

    fun validateNombre(nombre: String): InfanteError? =
        if (nombre.isBlank()) InfanteError.NombreVacio else null

    // "14/03/2022" (también "14-3-2022") -> "2022-03-14", la forma que pide la API.
    // null si no es una fecha que exista, si es de mañana en adelante o si el niño tendría más de 12 años.
    fun parseNacimiento(texto: String, hoy: Fecha): String? {
        val partes = texto.trim().split('/', '-')
        if (partes.size != 3) return null
        val dia = partes[0].trim().toIntOrNull() ?: return null
        val mes = partes[1].trim().toIntOrNull() ?: return null
        val anioTexto = partes[2].trim()
        // el año va completo: con "22" no se sabe si es 2022 o 1922.
        if (anioTexto.length != 4) return null
        val anio = anioTexto.toIntOrNull() ?: return null
        if (mes !in 1..12 || dia !in 1..diasDelMes(anio, mes)) return null
        val nacimiento = Fecha(anio, mes, dia)
        if (esPosterior(nacimiento, hoy)) return null
        if (edadEn(nacimiento, hoy) > EDAD_MAXIMA) return null
        return String.format(java.util.Locale.US, "%04d-%02d-%02d", anio, mes, dia)
    }

    fun validateNacimiento(texto: String, hoy: Fecha): InfanteError? =
        if (parseNacimiento(texto, hoy) == null) InfanteError.NacimientoInvalido else null

    fun isValid(nombre: String, nacimiento: String, hoy: Fecha): Boolean =
        validateNombre(nombre) == null && validateNacimiento(nacimiento, hoy) == null

    private fun diasDelMes(anio: Int, mes: Int): Int = when (mes) {
        2 -> if (esBisiesto(anio)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }

    private fun esBisiesto(anio: Int) = (anio % 4 == 0 && anio % 100 != 0) || anio % 400 == 0

    private fun esPosterior(a: Fecha, b: Fecha): Boolean =
        a.anio > b.anio || (a.anio == b.anio && (a.mes > b.mes || (a.mes == b.mes && a.dia > b.dia)))

    // años cumplidos a la fecha de `hoy`.
    private fun edadEn(nacimiento: Fecha, hoy: Fecha): Int {
        val anios = hoy.anio - nacimiento.anio
        val yaCumplio = hoy.mes > nacimiento.mes || (hoy.mes == nacimiento.mes && hoy.dia >= nacimiento.dia)
        return if (yaCumplio) anios else anios - 1
    }
}
