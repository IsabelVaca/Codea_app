package mx.tec.codea.data.api

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import retrofit2.HttpException
import java.io.IOException

// convierte cualquier error de red o de la API en un texto para el snackbar.
// la API manda "Problem Details": { title, detail, code, status }.
fun Throwable.toUserMessage(): String = when (this) {
    is HttpException -> {
        val cuerpo = try {
            response()?.errorBody()?.string()
        } catch (e: Exception) {
            null
        }
        textoDelProblema(cuerpo) ?: "Error del servidor (${code()})"
    }
    is IOException -> "No se pudo conectar con el servidor"
    else -> message ?: "Ocurrió un error"
}

private fun textoDelProblema(cuerpo: String?): String? {
    if (cuerpo.isNullOrBlank()) return null
    return try {
        val objeto = Json.parseToJsonElement(cuerpo).jsonObject
        (objeto["detail"] as? JsonPrimitive)?.contentOrNull
            ?: (objeto["title"] as? JsonPrimitive)?.contentOrNull
    } catch (e: Exception) {
        null
    }
}
