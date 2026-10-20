package mx.tec.codea.data.offline

import kotlinx.serialization.KSerializer
import mx.tec.codea.data.SessionRepository
import mx.tec.codea.data.api.ApiClient

// la última respuesta de la API de cada lista o pantalla, guardada en Room (tabla cached_response).
// las pantallas del admin y del padre muestran primero lo guardado y luego lo reemplazan con lo que llega de la API,
// así no se quedan vacías ni esperan cada vez que se cambia de pestaña o se abre la app.
// cada usuario tiene lo suyo (la llave lleva su id) y todo se borra al cerrar la sesión (ver AppContainer).
class ApiCache(private val dao: OfflineDao, private val sessions: SessionRepository) {

    private fun key(name: String): String? = sessions.current()?.userId?.let { "$it|$name" }

    // lo guardado, o null si no hay (o si cambió la forma de los datos y ya no se puede leer).
    suspend fun <T> read(name: String, serializer: KSerializer<T>): T? {
        val k = key(name) ?: return null
        val row = dao.response(k) ?: return null
        return runCatching { ApiClient.json.decodeFromString(serializer, row.json) }.getOrNull()
    }

    suspend fun <T> write(name: String, value: T, serializer: KSerializer<T>) {
        val userId = sessions.current()?.userId ?: return
        dao.putResponse(
            CachedResponseEntity(
                key = "$userId|$name",
                userId = userId,
                json = ApiClient.json.encodeToString(serializer, value),
                savedAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun clear() = dao.clearResponses()
}

// pide a la API y guarda la respuesta para la próxima vez. Si la caché no está lista, solo pide a la API.
suspend fun <T> fetchAndCache(name: String, serializer: KSerializer<T>, fetch: suspend () -> T): T {
    val value = fetch()
    runCatching { Offline.apiCache?.write(name, value, serializer) }
    return value
}

// lo último que se guardó de esa respuesta, o null.
suspend fun <T> cachedResponse(name: String, serializer: KSerializer<T>): T? =
    runCatching { Offline.apiCache?.read(name, serializer) }.getOrNull()
