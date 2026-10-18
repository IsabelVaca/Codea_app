package mx.tec.codea.data

import mx.tec.codea.data.api.ApiClient
import mx.tec.codea.data.api.ChangePasswordBody
import mx.tec.codea.data.api.MeDto

// lo que la pantalla "Mi cuenta" pide a la API: el perfil y el cambio de contraseña.
class AccountApiRepository(private val sessions: SessionRepository) {

    private val api get() = ApiClient.api

    suspend fun me(): MeDto = api.me()

    // la API cierra todas las sesiones al cambiar la contraseña y devuelve tokens nuevos para esta: se guardan
    // para que quien la cambió siga dentro.
    suspend fun changePassword(current: String, new: String) {
        sessions.save(api.changePassword(ChangePasswordBody(current, new)))
    }
}
