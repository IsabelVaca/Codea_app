package mx.tec.codea.domain

sealed interface InfanteError {
    data object NombreVacio : InfanteError
    data object EdadInvalida : InfanteError
    data object TutorVacio : InfanteError
    data object TelefonoInvalido : InfanteError
}

object InfanteValidator {
    fun validateNombre(nombre: String): InfanteError? =
        if (nombre.isBlank()) InfanteError.NombreVacio else null

    fun validateEdad(edad: String): InfanteError? {
        val valor = edad.toIntOrNull()
        return if (valor == null || valor !in 0..12) InfanteError.EdadInvalida else null
    }

    fun validateTutor(tutor: String): InfanteError? =
        if (tutor.isBlank()) InfanteError.TutorVacio else null

    fun validateTelefono(telefono: String): InfanteError? =
        if (telefono.trim().length < 10) InfanteError.TelefonoInvalido else null

    fun isValid(nombre: String, edad: String, tutor: String, telefono: String): Boolean =
        validateNombre(nombre) == null &&
            validateEdad(edad) == null &&
            validateTutor(tutor) == null &&
            validateTelefono(telefono) == null
}
