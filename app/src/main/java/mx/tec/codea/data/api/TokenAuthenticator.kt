package mx.tec.codea.data.api

import kotlinx.coroutines.runBlocking
import mx.tec.codea.data.RefreshResult
import mx.tec.codea.data.SessionRepository
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

// OkHttp lo llama cuando la API responde 401. renueva el access token con el refresh token y repite la petición
// una sola vez. las tres guardas evitan ciclos:
//  1) la petición no traía token (no hay nada que renovar),
//  2) la petición era de /auth/ (renovar no se renueva a sí mismo),
//  3) ya era un reintento (si el token nuevo también falla, se deja pasar el 401).
class TokenAuthenticator(private val sessions: SessionRepository) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val sent = response.request.header("Authorization")?.removePrefix("Bearer ") ?: return null
        if (response.request.url.encodedPath.contains("/auth/")) return null
        if (response.priorResponse != null) return null

        // `refresh` serializa las renovaciones y no repite una que otra petición ya hizo.
        val result = runBlocking { sessions.refresh(staleAccessToken = sent) }
        if (result != RefreshResult.Renewed) return null

        val fresh = sessions.current()?.accessToken ?: return null
        return response.request.newBuilder().header("Authorization", "Bearer $fresh").build()
    }
}
