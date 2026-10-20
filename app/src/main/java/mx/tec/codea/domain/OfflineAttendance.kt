package mx.tec.codea.domain

import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.cancellation.CancellationException

// una marca de asistencia que se hizo sin conexión y todavía no llega al servidor.
// createdAt es el instante (en milisegundos) en que se hizo la marca: sirve de hora de entrada cuando llega tarde.
data class PendingAttendance(val id: Long, val childId: Int, val status: String, val date: String, val createdAt: Long = 0)

// dónde espera la cola. en la app es Room; en las pruebas, una lista en memoria.
interface PendingStore {
    suspend fun add(childId: Int, status: String, date: String)
    suspend fun all(): List<PendingAttendance>
    suspend fun remove(ids: List<Long>)
    suspend fun removeFor(childId: Int, date: String)
    fun count(): Flow<Int>
}

// la puerta al servidor. en la app es Retrofit; en las pruebas, un doble.
interface AttendanceRemote {
    // markedAt: cuándo se hizo la marca, si fue sin conexión; null para que el servidor use su propia hora.
    suspend fun send(childId: Int, status: String, date: String, markedAt: Long? = null)
}

// qué tipo de error fue: uno temporal (sin red, servidor caído) se reintenta; uno permanente (404, 422) no tiene remedio.
enum class ErrorKind { TEMPORARY, PERMANENT }

enum class MarkResult { SENT, QUEUED }

// cuántas marcas se enviaron, cuántas se descartaron por no tener remedio y cuántas siguen esperando.
data class FlushResult(val sent: Int, val dropped: Int, val remaining: Int)

// la asistencia que sobrevive a no tener internet: se intenta mandar; si no hay conexión, se guarda en la cola
// y se manda sola al volver la red. Kotlin puro para poder probarlo con JUnit.
class OfflineAttendance(
    private val store: PendingStore,
    private val remote: AttendanceRemote,
    private val classify: (Throwable) -> ErrorKind,
) {
    fun pendingCount(): Flow<Int> = store.count()

    // intenta mandar la marca. Si el error es temporal, la guarda para después; si no, lo vuelve a lanzar
    // (por ejemplo, un niño que no es de tu sala): guardarlo solo repetiría el error.
    suspend fun mark(childId: Int, status: String, date: String): MarkResult {
        try {
            remote.send(childId, status, date)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (classify(e) == ErrorKind.TEMPORARY) {
                store.add(childId, status, date)
                return MarkResult.QUEUED
            }
            throw e
        }
        // lo que llegó ahora es lo más nuevo: una marca vieja en la cola no debe pisarla después.
        store.removeFor(childId, date)
        return MarkResult.SENT
    }

    // lo que está en la cola para ese día, por niño: la última marca de cada uno. sirve para pintar la pantalla.
    suspend fun pendingMarks(date: String): Map<Int, String> =
        store.all().filter { it.date == date }.sortedBy { it.id }.associate { it.childId to it.status }

    // manda lo que esté esperando. Se queda con la última marca de cada niño y día (las anteriores ya no importan).
    // si falla por algo temporal, se detiene: lo más probable es que no haya red, y no tiene caso insistir con las demás.
    suspend fun flush(): FlushResult {
        val groups = store.all().groupBy { it.childId to it.date }.values.sortedBy { group -> group.maxOf { it.id } }
        var sent = 0
        var dropped = 0
        for ((index, group) in groups.withIndex()) {
            val latest = group.maxBy { it.id }
            try {
                remote.send(latest.childId, latest.status, latest.date, latest.createdAt.takeIf { it > 0 })
                store.remove(group.map { it.id })
                sent++
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (classify(e) == ErrorKind.PERMANENT) {
                    store.remove(group.map { it.id })
                    dropped++
                } else {
                    return FlushResult(sent, dropped, remaining = groups.size - index)
                }
            }
        }
        return FlushResult(sent, dropped, remaining = 0)
    }
}
