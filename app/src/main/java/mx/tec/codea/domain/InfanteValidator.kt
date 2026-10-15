package mx.tec.codea.domain

sealed interface InfanteError {
    data object NombreVacio : InfanteError
    data object EdadInvalida : InfanteError
}

object InfanteValidator {
    fun validateNombre(nombre: String): InfanteError? =
        if (nombre.isBlank()) InfanteError.NombreVacio else null

    fun validateEdad(edad: String): InfanteError? {
        val valor = edad.toIntOrNull()
        return if (valor == null || valor !in 0..12) InfanteError.EdadInvalida else null
    }

    fun isValid(nombre: String, edad: String): Boolean =
        validateNombre(nombre) == null && validateEdad(edad) == null
}
