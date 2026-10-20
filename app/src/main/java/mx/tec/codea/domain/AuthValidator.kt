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
    PasswordWeak,
    PasswordsDontMatch,
    CurrentPasswordRequired,
}

// qué tan segura se ve la contraseña que se está escribiendo, para el medidor de "Activar mi cuenta":
// una de las tres reglas cumplida es Baja, dos es Media, las tres es Alta.
enum class PasswordStrength { WEAK, MEDIUM, STRONG }

object AuthValidator {

    // RNF01: mínimo de caracteres. el tope evita mandar textos enormes que gasten CPU en el servidor.
    // la mayúscula y el número (abajo, PasswordWeak) solo los exige la app: la API no los repite.
    const val PASSWORD_MIN = 6
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
        !hasUppercase(password) || !hasDigit(password) -> AuthError.PasswordWeak
        email != null && password.trim().equals(email.trim(), ignoreCase = true) -> AuthError.PasswordIsEmail
        password != confirmation -> AuthError.PasswordsDontMatch
        else -> null
    }

    private fun hasUppercase(password: String) = password.any { it.isUpperCase() }
    private fun hasDigit(password: String) = password.any { it.isDigit() }

    // para el medidor visual mientras se escribe: cuántas de las tres reglas (tamaño, mayúscula, número)
    // ya se cumplen ahora mismo. no es un error: una contraseña corta también cuenta lo que ya tiene bien.
    fun strength(password: String): PasswordStrength {
        val reglasCumplidas = listOf(password.length >= PASSWORD_MIN, hasUppercase(password), hasDigit(password))
            .count { it }
        return when (reglasCumplidas) {
            3 -> PasswordStrength.STRONG
            2 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }
}
