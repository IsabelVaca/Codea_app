package mx.tec.codea.domain

sealed interface DocenteError {
    data object NombreVacio : DocenteError
    data object TelefonoInvalido : DocenteError
    data object CorreoInvalido : DocenteError
}

object DocenteValidator {
    fun validateNombre(nombre: String): DocenteError? =
        if (nombre.isBlank()) DocenteError.NombreVacio else null

    fun validateTelefono(telefono: String): DocenteError? =
        if (telefono.trim().length < 10) DocenteError.TelefonoInvalido else null

    fun validateCorreo(correo: String): DocenteError? =
        if (!correo.contains("@") || !correo.contains(".")) DocenteError.CorreoInvalido else null

    fun isValid(nombre: String, telefono: String, correo: String): Boolean =
        validateNombre(nombre) == null && validateTelefono(telefono) == null && validateCorreo(correo) == null
}