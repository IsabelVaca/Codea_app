package mx.tec.codea.data.api

object ApiConfig {
    // 10.0.2.2 es tu Mac vista desde el emulador de Android: no hace falta nada más.
    // (Celular físico por USB: usa "http://localhost:8000/api/v1/" y corre
    //  adb reverse tcp:8000 tcp:8000 && adb reverse tcp:9000 tcp:9000)
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