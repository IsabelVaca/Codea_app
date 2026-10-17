package mx.tec.codea.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import mx.tec.codea.data.api.AuthApi
import mx.tec.codea.data.local.SessionStore
import mx.tec.codea.data.local.TokenCipher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File

private class XorCipher(private val works: () -> Boolean = { true }) : TokenCipher {
    override fun encrypt(plain: String) = "c:$plain"
    override fun decrypt(encoded: String) = if (works()) encoded.removePrefix("c:") else null
}

class SessionRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    private lateinit var server: MockWebServer
    private var cipherWorks = true

    private fun authApi() = Retrofit.Builder()
        .baseUrl(server.url("/api/v1/"))
        .client(OkHttpClient())
        .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(AuthApi::class.java)

    // DataStore no permite dos instancias sobre el mismo archivo, así que se comparte una. dos repositorios nuevos
    // sobre ella simulan cerrar la app y volver a abrirla: empiezan en Loading y leen lo que quedó guardado.
    private val dataStore by lazy { PreferenceDataStoreFactory.create { File(folder.root, "s.preferences_pb") } }

    private fun repository() = SessionRepository(SessionStore(dataStore, XorCipher { cipherWorks }), authApi())

    private val loginOk = """
        {"access_token":"a1","refresh_token":"r1","expires_in":300,"refresh_expires_in":604800,
         "me":{"id":"u-7","name":"Karla","surname":"Robles","full_name":"Karla Robles","roles":["TEACHER","PARENT"]}}
    """.trimIndent()

    @Before
    fun preparar() {
        server = MockWebServer().apply { start() }
    }

    @After
    fun cerrar() {
        server.close()
    }

    @Test
    fun sin_nada_guardado_no_hay_sesion() = runBlocking {
        val repo = repository()
        assertEquals(SessionState.Loading, repo.state.value)
        repo.load()
        assertEquals(SessionState.Anonymous, repo.state.value)
    }

    @Test
    fun el_login_guarda_la_sesion_y_sobrevive_a_reabrir_la_app() = runBlocking {
        server.enqueue(MockResponse.Builder().code(200).body(loginOk).build())
        val primera = repository()
        primera.load()
        primera.login(" karla@oruga.test ", "una-clave-larga")

        val sesion = primera.current()!!
        assertEquals("a1", sesion.accessToken)
        assertEquals("Karla Robles", sesion.name)
        assertEquals(setOf("TEACHER", "PARENT"), sesion.roles)
        // el correo se manda sin espacios sobrantes
        assertEquals(true, server.takeRequest().body!!.utf8().contains("\"karla@oruga.test\""))

        // "reabrir la app": otro repositorio lee lo que quedó en disco
        val segunda = repository()
        segunda.load()
        assertEquals("r1", segunda.current()?.refreshToken)
        assertEquals("u-7", segunda.current()?.userId)
    }

    @Test
    fun si_el_login_falla_no_queda_sesion_y_el_error_llega_a_quien_llamo() = runBlocking {
        server.enqueue(MockResponse.Builder().code(401).body("""{"code":"INVALID_CREDENTIALS"}""").build())
        val repo = repository()
        repo.load()
        try {
            repo.login("karla@oruga.test", "mala")
            error("debió fallar")
        } catch (e: HttpException) {
            assertEquals(401, e.code())
        }
        assertNull(repo.current())
        assertEquals(SessionState.Anonymous, repo.state.value)
    }

    @Test
    fun cerrar_sesion_borra_lo_local_aunque_el_servidor_no_responda() = runBlocking {
        server.enqueue(MockResponse.Builder().code(200).body(loginOk).build())
        server.enqueue(MockResponse.Builder().code(500).build())
        val repo = repository()
        repo.load()
        repo.login("karla@oruga.test", "una-clave-larga")

        repo.logout()

        assertNull(repo.current())
        // y al reabrir la app tampoco hay sesión
        val reabierta = repository()
        reabierta.load()
        assertEquals(SessionState.Anonymous, reabierta.state.value)
        // el servidor sí recibió el aviso de cierre con el refresh token
        server.takeRequest()
        val cierre = server.takeRequest()
        assertEquals("/api/v1/auth/logout", cierre.url.encodedPath)
        assertEquals(true, cierre.body!!.utf8().contains("r1"))
    }

    @Test
    fun si_los_tokens_no_se_pueden_descifrar_se_trata_como_sin_sesion() = runBlocking {
        server.enqueue(MockResponse.Builder().code(200).body(loginOk).build())
        val repo = repository()
        repo.load()
        repo.login("karla@oruga.test", "una-clave-larga")
        assertNotNull(repo.current())

        // el sistema borró la llave del Keystore: lo guardado ya no se puede leer
        cipherWorks = false
        val reabierta = repository()
        reabierta.load()
        assertEquals(SessionState.Anonymous, reabierta.state.value)
    }

    @Test
    fun al_refrescar_se_conservan_el_nombre_y_los_roles() = runBlocking {
        server.enqueue(MockResponse.Builder().code(200).body(loginOk).build())
        server.enqueue(
            MockResponse.Builder().code(200)
                .body("""{"access_token":"a2","refresh_token":"r2","expires_in":300,"refresh_expires_in":604800}""")
                .build(),
        )
        val repo = repository()
        repo.load()
        repo.login("karla@oruga.test", "una-clave-larga")

        assertEquals(RefreshResult.Renewed, repo.refresh(staleAccessToken = "a1"))

        val sesion = repo.current()!!
        assertEquals("a2", sesion.accessToken)
        assertEquals("Karla Robles", sesion.name)
        assertEquals(setOf("TEACHER", "PARENT"), sesion.roles)
        assertEquals("u-7", sesion.userId)
    }
}
