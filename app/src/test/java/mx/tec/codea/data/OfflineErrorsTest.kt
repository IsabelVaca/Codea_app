package mx.tec.codea.data

import mx.tec.codea.data.offline.OfflineErrors
import mx.tec.codea.domain.ErrorKind
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class OfflineErrorsTest {

    private fun http(code: Int) =
        HttpException(Response.error<Any>(code, "{}".toResponseBody("application/json".toMediaType())))

    @Test
    fun `los fallos de red son temporales`() {
        for (error in listOf(IOException(), ConnectException(), UnknownHostException(), SocketTimeoutException())) {
            assertEquals(error::class.simpleName, ErrorKind.TEMPORARY, OfflineErrors.kindOf(error))
        }
    }

    @Test
    fun `el servidor caido, la sesion que no termina de cargar y el exceso de peticiones se reintentan`() {
        for (code in listOf(500, 502, 503, 504, 401, 408, 429)) {
            assertEquals("HTTP $code", ErrorKind.TEMPORARY, OfflineErrors.kindOf(http(code)))
        }
    }

    @Test
    fun `lo que el servidor rechaza con razon no se reintenta`() {
        for (code in listOf(400, 403, 404, 409, 422)) {
            assertEquals("HTTP $code", ErrorKind.PERMANENT, OfflineErrors.kindOf(http(code)))
        }
    }

    @Test
    fun `un error de programacion no se reintenta`() {
        assertEquals(ErrorKind.PERMANENT, OfflineErrors.kindOf(IllegalStateException("bug")))
    }
}
