package mx.tec.codea.data.api

import kotlinx.serialization.json.Json
import mx.tec.codea.data.SessionRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object ApiClient {

    // si la API agrega campos nuevos, la app no se rompe.
    val json = Json { ignoreUnknownKeys = true }

    private var sessions: SessionRepository? = null

    // CodeaApplication lo llama una vez, antes de que nadie pida datos.
    fun init(sessions: SessionRepository) {
        this.sessions = sessions
    }

    // en Logcat, filtra por "OkHttp" para ver método, ruta y código de cada petición.
    // el nivel es BASIC a propósito: el cuerpo trae tokens y datos de menores, y el token del encabezado se oculta.
    private fun logging() = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
        redactHeader("Authorization")
    }

    private fun retrofit(client: OkHttpClient) = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    // entrar y renovar van sin token ni autenticador: no pueden depender de tener ya una sesión.
    fun createAuthApi(): AuthApi =
        retrofit(OkHttpClient.Builder().addInterceptor(logging()).build()).create(AuthApi::class.java)

    val api: CodeaApi by lazy {
        val repository = checkNotNull(sessions) { "ApiClient.init() no se llamó: ver CodeaApplication" }
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(repository))
            .authenticator(TokenAuthenticator(repository))
            .addInterceptor(logging())
            .build()
        retrofit(client).create(CodeaApi::class.java)
    }
}
