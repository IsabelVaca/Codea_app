package mx.tec.codea.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import mx.tec.codea.data.api.AuthApi
import mx.tec.codea.data.api.AuthInterceptor
import mx.tec.codea.data.api.TokenAuthenticator
import mx.tec.codea.data.local.SessionStore
import mx.tec.codea.data.local.TokenCipher
import mx.tec.codea.domain.Session
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

// el cifrador real usa el Keystore de Android, que no existe en la JVM: aquí se usa uno falso.
private class FakeTokenCipher : TokenCipher {
    override fun encrypt(plain: String) = "cifrado:$plain"
    override fun decrypt(encoded: String) = encoded.removePrefix("cifrado:").takeIf { encoded.startsWith("cifrado:") }
}

// prueba el interceptor y el autenticador de verdad (OkHttp + el SessionRepository real) contra un servidor falso:
// no se prueba una copia de la lógica, se prueba la lógica.
class TokenAuthenticatorTest {

    @get:Rule
    val folder = TemporaryFolder()

    private lateinit var server: MockWebServer
    private lateinit var sessions: SessionRepository
    private lateinit var client: OkHttpClient

    private val refreshOk = """{"access_token":"access-2","refresh_token":"refresh-2","expires_in":300,"refresh_expires_in":604800}"""

    @Before
    fun preparar() {
        server = MockWebServer().apply { start() }
        val authApi = Retrofit.Builder()
            .baseUrl(server.url("/api/v1/"))
            .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AuthApi::class.java)
        val store = SessionStore(
            PreferenceDataStoreFactory.create { File(folder.root, "session.preferences_pb") },
            FakeTokenCipher(),
        )
        sessions = SessionRepository(store, authApi)
        runBlocking {
            store.write(Session("access-1", "refresh-1", "user-1", "Ana Pérez", setOf("TEACHER")))
            sessions.load()
        }
        client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessions))
            .authenticator(TokenAuthenticator(sessions))
            .build()
    }

    @After
    fun cerrar() {
        server.close()
    }

    private fun get(path: String = "/api/v1/ping") =
        client.newCall(Request.Builder().url(server.url(path)).build()).execute()

    private fun json(code: Int, body: String) = MockResponse.Builder().code(code).body(body).build()

    private fun RecordedRequest.bearer() = headers["Authorization"]

    @Test
    fun cada_peticion_lleva_el_access_token() {
        server.enqueue(json(200, "{}"))
        assertEquals(200, get().code)
        assertEquals("Bearer access-1", server.takeRequest().bearer())
    }

    @Test
    fun con_el_token_vencido_renueva_una_vez_y_repite_con_el_nuevo() {
        server.enqueue(json(401, """{"code":"TOKEN_EXPIRED"}"""))
        server.enqueue(json(200, refreshOk))
        server.enqueue(json(200, "{}"))

        assertEquals(200, get().code)

        val primera = server.takeRequest()
        val renovar = server.takeRequest()
        val repetida = server.takeRequest()
        assertEquals("Bearer access-1", primera.bearer())
        assertEquals("/api/v1/auth/refresh", renovar.url.encodedPath)
        assertTrue(renovar.body!!.utf8().contains("refresh-1"))
        assertEquals("Bearer access-2", repetida.bearer())
        // la sesión guardada ya tiene los tokens nuevos
        assertEquals("access-2", sessions.current()?.accessToken)
        assertEquals("refresh-2", sessions.current()?.refreshToken)
        // y conserva quién es
        assertEquals("Ana Pérez", sessions.current()?.name)
    }

    @Test
    fun si_el_refresh_token_ya_no_sirve_la_sesion_termina() {
        server.enqueue(json(401, """{"code":"TOKEN_EXPIRED"}"""))
        server.enqueue(json(401, """{"code":"REFRESH_INVALID"}"""))

        assertEquals(401, get().code)
        assertEquals(SessionState.Anonymous, sessions.state.value)
    }

    @Test
    fun si_el_servidor_falla_al_renovar_la_sesion_se_conserva() {
        server.enqueue(json(401, """{"code":"TOKEN_EXPIRED"}"""))
        server.enqueue(json(503, "{}"))

        assertEquals(401, get().code)
        assertEquals("access-1", sessions.current()?.accessToken)
    }

    @Test
    fun si_el_token_nuevo_tambien_falla_no_hay_ciclo() {
        server.enqueue(json(401, """{"code":"TOKEN_EXPIRED"}"""))
        server.enqueue(json(200, refreshOk))
        server.enqueue(json(401, """{"code":"TOKEN_INVALID"}"""))

        assertEquals(401, get().code)
        // una petición, un refresh, un reintento: tres en total y ni una más
        assertEquals(3, server.requestCount)
    }

    @Test
    fun las_rutas_de_auth_no_llevan_token_ni_disparan_renovacion() {
        server.enqueue(json(401, """{"code":"INVALID_CREDENTIALS"}"""))

        assertEquals(401, get("/api/v1/auth/login").code)
        assertEquals(null, server.takeRequest().bearer())
        assertEquals(1, server.requestCount)
        assertEquals("access-1", sessions.current()?.accessToken)
    }

    @Test
    fun varias_peticiones_con_el_token_vencido_hacen_un_solo_refresh() {
        val renovaciones = AtomicInteger(0)
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when {
                request.url.encodedPath.endsWith("/auth/refresh") -> {
                    renovaciones.incrementAndGet()
                    Thread.sleep(150) // da tiempo a que las otras peticiones lleguen al autenticador
                    json(200, refreshOk)
                }
                request.bearer() == "Bearer access-2" -> json(200, "{}")
                else -> json(401, """{"code":"TOKEN_EXPIRED"}""")
            }
        }

        val codigos = java.util.Collections.synchronizedList(mutableListOf<Int>())
        val listos = CountDownLatch(4)
        repeat(4) {
            thread {
                codigos += get().code
                listos.countDown()
            }
        }
        listos.await()

        assertEquals(1, renovaciones.get())
        assertEquals(listOf(200, 200, 200, 200), codigos.toList())
    }

    @Test
    fun si_la_api_dice_que_la_persona_esta_de_baja_se_cierra_la_sesion() {
        server.enqueue(json(403, """{"code":"USER_INACTIVE"}"""))

        assertEquals(403, get().code)
        assertEquals(SessionState.Anonymous, sessions.state.value)
    }
}
