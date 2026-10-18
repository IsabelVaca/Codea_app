package mx.tec.codea.domain

// la sesión de quien entró: sus dos tokens y lo mínimo para dibujar la app.
// los roles van como texto ("ADMIN", "TEACHER", "PARENT") porque domain no conoce la interfaz.
// los tokens se guardan cifrados en el teléfono (ver data/local/SessionStore).
data class Session(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val name: String,
    val roles: Set<String>,
)
