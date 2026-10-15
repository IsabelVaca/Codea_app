package mx.tec.codea.data.api

object ApiConfig {
    // 10.0.2.2 es tu Mac vista desde el emulador. En un celular real, usa la IP de tu Mac.
    const val BASE_URL = "http://10.0.2.2:8000/api/v1/"

    // correo con el que se hace pasar la app en desarrollo (Bearer dev:<correo>).
    // cuando haya login con Firebase, aquí va el token real.
    @Volatile
    var devEmail: String? = null

    // las pantallas de admin solo piden datos si el usuario es admin (los demás roles recibirían 403).
    @Volatile
    var isAdmin: Boolean = false

    @Volatile
    var isTeacher: Boolean = false

    @Volatile
    var isParent: Boolean = false
}