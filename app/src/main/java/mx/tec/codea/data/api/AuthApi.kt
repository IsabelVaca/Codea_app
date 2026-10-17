package mx.tec.codea.data.api

import retrofit2.http.Body
import retrofit2.http.POST

// los endpoints de entrada. van con un cliente HTTP aparte, sin el interceptor del token ni el autenticador:
// pedir tokens no puede depender de tener ya un token.
interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body body: LoginBody): TokenDto

    @POST("auth/activate")
    suspend fun activate(@Body body: ActivateBody): TokenDto

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshBody): TokenDto

    @POST("auth/logout")
    suspend fun logout(@Body body: RefreshBody)
}
