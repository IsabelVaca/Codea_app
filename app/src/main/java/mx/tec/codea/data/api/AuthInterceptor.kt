package mx.tec.codea.data.api

import kotlinx.coroutines.runBlocking
import mx.tec.codea.data.SessionRepository
import okhttp3.Interceptor
import okhttp3.Response

// pone `Authorization: Bearer <access token>` en cada petición (menos las de /auth/, que no lo necesitan).
// si la API contesta que la persona está de baja, cierra la sesión: ya no tiene acceso.
class AuthInterceptor(private val sessions: SessionRepository) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = sessions.current()?.accessToken
        val request = if (token != null && !original.url.encodedPath.contains("/auth/")) {
            original.newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            original
        }
        val response = chain.proceed(request)
        if (response.code == 403 && response.peekBody(2048).string().contains("USER_INACTIVE")) {
            runBlocking { sessions.clear() }
        }
        return response
    }
}
