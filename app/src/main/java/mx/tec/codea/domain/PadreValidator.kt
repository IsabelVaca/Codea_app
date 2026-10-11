package mx.tec.codea.domain

sealed interface PadreError {
    data object NombreVacio : PadreError
    data object TelefonoInvalido : PadreError
    data object CorreoInvalido : PadreError
}

object PadreValidator {
    fun validateNombre(nombre: String): PadreError? =
        if (nombre.isBlank()) PadreError.NombreVacio else null

    fun validateTelefono(telefono: String): PadreError? =
        if (telefono.trim().length < 10) PadreError.TelefonoInvalido else null

    fun validateCorreo(correo: String): PadreError? =
        if (!correo.contains("@") || !correo.contains(".")) PadreError.CorreoInvalido else null

    fun isValid(nombre: String, telefono: String, correo: String): Boolean =
        validateNombre(nombre) == null && validateTelefono(telefono) == null && validateCorreo(correo) == null
}
