package mx.tec.codea.data.api

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object ApiClient {

    // si la API agrega campos nuevos, la app no se rompe.
    val json = Json { ignoreUnknownKeys = true }

    // agrega el token a cada petición.
    private val auth = Interceptor { chain ->
        val email = ApiConfig.devEmail
        val request = if (email != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer dev:$email")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private val http = OkHttpClient.Builder()
        .addInterceptor(auth)
        // en Logcat, filtra por "OkHttp" para ver cada petición y respuesta.
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()

    val api: CodeaApi = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(http)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(CodeaApi::class.java)
}