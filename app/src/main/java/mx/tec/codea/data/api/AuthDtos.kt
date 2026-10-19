package mx.tec.codea.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---------- lo que manda la app ----------

@Serializable
data class LoginBody(val email: String, val password: String)

@Serializable
data class ActivateBody(val email: String, val code: String, val password: String)

@Serializable
data class RefreshBody(@SerialName("refresh_token") val refreshToken: String)

@Serializable
data class ChangePasswordBody(
    @SerialName("current_password") val currentPassword: String,
    @SerialName("new_password") val newPassword: String,
)

// ---------- lo que responde la API ----------

// login, activar, refrescar y cambiar contraseña regresan esto. `me` viene en todos menos en refrescar.
@Serializable
data class TokenDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Int = 0,
    @SerialName("refresh_expires_in") val refreshExpiresIn: Int = 0,
    val me: MeDto? = null,
)

// el código que el administrador comparte para que alguien active su cuenta. la API lo muestra una sola vez.
@Serializable
data class AccessCodeDto(
    val code: String,
    val purpose: String,
    @SerialName("expires_at") val expiresAt: String,
    // el enlace que abre la app con el correo y el código ya puestos; null si el servidor no tiene URL pública.
    val link: String? = null,
)
