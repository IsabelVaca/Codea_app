package mx.tec.codea.domain

object SalaValidator {
    fun validateNombre(nombre: String): SalaError? =
        if (nombre.isBlank()) SalaError.NombreVacio else null

    fun validateCupo(cupo: String): SalaError? =
        if (cupo.toIntOrNull() == null || cupo.toInt() <= 0) SalaError.CupoInvalido else null

    fun isValid(nombre: String, cupo: String): Boolean =
        validateNombre(nombre) == null && validateCupo(cupo) == null
}