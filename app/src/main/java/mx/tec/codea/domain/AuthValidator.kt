package mx.tec.codea.domain

// los errores que la app detecta antes de llamar a la API. la API repite las mismas reglas y tiene la última palabra
// (guarderia-api/app/core/rules.py, check_password).
enum class AuthError {
    EmailRequired,
    PasswordRequired,
    CodeRequired,
    PasswordTooShort,
    PasswordTooLong,
    PasswordIsEmail,
    PasswordsDontMatch,
    CurrentPasswordRequired,
}

object AuthValidator {

    // RNF01: "mayores a 8 caracteres" → 9 o más. el tope evita mandar textos enormes que gasten CPU en el servidor.
    const val PASSWORD_MIN = 9
    const val PASSWORD_MAX = 128

    fun login(email: String, password: String): AuthError? = when {
        email.isBlank() -> AuthError.EmailRequired
        password.isEmpty() -> AuthError.PasswordRequired
        else -> null
    }

    fun activate(email: String, code: String, password: String, confirmation: String): AuthError? = when {
        email.isBlank() -> AuthError.EmailRequired
        code.isBlank() -> AuthError.CodeRequired
        else -> newPassword(password, confirmation, email)
    }

    fun changePassword(current: String, new: String, confirmation: String, email: String?): AuthError? = when {
        current.isEmpty() -> AuthError.CurrentPasswordRequired
        else -> newPassword(new, confirmation, email)
    }

    private fun newPassword(password: String, confirmation: String, email: String?): AuthError? = when {
        password.length < PASSWORD_MIN -> AuthError.PasswordTooShort
        password.length > PASSWORD_MAX -> AuthError.PasswordTooLong
        email != null && password.trim().equals(email.trim(), ignoreCase = true) -> AuthError.PasswordIsEmail
        password != confirmation -> AuthError.PasswordsDontMatch
        else -> null
    }
}
