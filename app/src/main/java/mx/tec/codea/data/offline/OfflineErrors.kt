package mx.tec.codea.data.offline

import mx.tec.codea.domain.ErrorKind
import retrofit2.HttpException
import java.io.IOException

// decide si un error de red vale la pena reintentarlo.
object OfflineErrors {

    fun kindOf(error: Throwable): ErrorKind = when (error) {
        // el servidor contestó: 5xx, "demasiadas peticiones" y "tiempo agotado" se arreglan solos; 401 también,
        // porque puede ser que la sesión todavía no termine de cargar al arrancar en segundo plano.
        // cualquier otro 4xx (404 no es tu sala, 422 dato inválido) siempre va a repetir lo mismo.
        is HttpException -> if (error.code() in 500..599 || error.code() in TEMPORARY_CODES) ErrorKind.TEMPORARY else ErrorKind.PERMANENT
        // sin conexión, DNS, tiempo agotado, servidor apagado: todos son IOException.
        is IOException -> ErrorKind.TEMPORARY
        else -> ErrorKind.PERMANENT
    }

    private val TEMPORARY_CODES = setOf(401, 408, 429)
}
