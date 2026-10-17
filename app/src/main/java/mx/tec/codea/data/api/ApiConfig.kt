package mx.tec.codea.data.api

import mx.tec.codea.BuildConfig

object ApiConfig {
    // la dirección sale del build: ver `API_BASE_URL` en app/build.gradle.kts.
    // emulador: http://10.0.2.2:8000/api/v1/ (10.0.2.2 es tu Mac vista desde el emulador).
    // celular físico por USB: http://localhost:8000/api/v1/ y
    //   adb reverse tcp:8000 tcp:8000 && adb reverse tcp:9000 tcp:9000
    const val BASE_URL = BuildConfig.API_BASE_URL

    // el id de quien tiene la sesión abierta. sirve para guardar datos locales por persona (avisos leídos).
    @Volatile
    var userKey: String? = null

    // los ViewModels solo piden datos si el rol de la sesión lo permite (los demás roles recibirían 403).
    // CodeaApp los fija con el rol activo y los apaga al cerrar sesión.
    @Volatile
    var isAdmin: Boolean = false

    @Volatile
    var isTeacher: Boolean = false

    @Volatile
    var isParent: Boolean = false
}
