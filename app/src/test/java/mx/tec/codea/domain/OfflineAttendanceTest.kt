package mx.tec.codea.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

// la cola en memoria, con la misma regla que la de Room.
private class FakeStore : PendingStore {
    private var nextId = 1L
    val rows = mutableListOf<PendingAttendance>()
    private val size = MutableStateFlow(0)

    override suspend fun add(childId: Int, status: String, date: String) {
        rows += PendingAttendance(nextId++, childId, status, date)
        size.value = rows.size
    }

    override suspend fun all(): List<PendingAttendance> = rows.toList()

    override suspend fun remove(ids: List<Long>) {
        rows.removeAll { it.id in ids }
        size.value = rows.size
    }

    override suspend fun removeFor(childId: Int, date: String) {
        rows.removeAll { it.childId == childId && it.date == date }
        size.value = rows.size
    }

    override fun count(): Flow<Int> = size
}

// un servidor que se puede "apagar" y que apunta lo que recibe.
private class FakeRemote : AttendanceRemote {
    var failure: Exception? = null
    val received = mutableListOf<Triple<Int, String, String>>()

    override suspend fun send(childId: Int, status: String, date: String) {
        failure?.let { throw it }
        received += Triple(childId, status, date)
    }
}

class OfflineAttendanceTest {

    private val store = FakeStore()
    private val remote = FakeRemote()
    private val offline = OfflineAttendance(store, remote) { e ->
        if (e is IOException) ErrorKind.TEMPORARY else ErrorKind.PERMANENT
    }
    private val today = "2026-10-18"

    @Test
    fun `con conexion la marca se manda y no queda nada en la cola`() = runBlocking {
        assertEquals(MarkResult.SENT, offline.mark(5, "ABSENT", today))
        assertEquals(listOf(Triple(5, "ABSENT", today)), remote.received)
        assertTrue(store.rows.isEmpty())
    }

    @Test
    fun `sin conexion la marca espera en la cola`() = runBlocking {
        remote.failure = IOException("sin red")
        assertEquals(MarkResult.QUEUED, offline.mark(5, "ABSENT", today))
        assertEquals(1, offline.pendingCount().first())
        assertTrue(remote.received.isEmpty())
    }

    @Test
    fun `un error permanente se avisa y no se guarda`() = runBlocking {
        remote.failure = IllegalStateException("404: ese niño no es de tu sala")
        try {
            offline.mark(5, "ABSENT", today)
            fail("debía lanzar el error")
        } catch (e: IllegalStateException) {
            assertTrue(store.rows.isEmpty())
        }
    }

    @Test
    fun `al volver la conexion flush manda lo pendiente y vacia la cola`() = runBlocking {
        remote.failure = IOException()
        offline.mark(5, "ABSENT", today)
        offline.mark(6, "PRESENT", today)
        remote.failure = null

        assertEquals(FlushResult(sent = 2, dropped = 0, remaining = 0), offline.flush())
        assertEquals(listOf(5, 6), remote.received.map { it.first })
        assertTrue(store.rows.isEmpty())
    }

    @Test
    fun `de varias marcas del mismo niño y dia solo se manda la ultima`() = runBlocking {
        remote.failure = IOException()
        offline.mark(5, "ABSENT", today)
        offline.mark(5, "PRESENT", today)
        offline.mark(5, "ABSENT", today)
        remote.failure = null

        assertEquals(FlushResult(sent = 1, dropped = 0, remaining = 0), offline.flush())
        assertEquals(listOf(Triple(5, "ABSENT", today)), remote.received)
        assertTrue(store.rows.isEmpty())
    }

    @Test
    fun `si la red sigue caida flush se detiene y no pierde nada`() = runBlocking {
        remote.failure = IOException()
        offline.mark(5, "ABSENT", today)
        offline.mark(6, "ABSENT", today)

        assertEquals(FlushResult(sent = 0, dropped = 0, remaining = 2), offline.flush())
        assertEquals(2, store.rows.size)
    }

    @Test
    fun `una marca que ya no tiene remedio se descarta y las demas siguen`() = runBlocking {
        remote.failure = IOException()
        offline.mark(5, "ABSENT", today)
        offline.mark(6, "ABSENT", today)
        remote.failure = null
        // el niño 5 ya no es de la sala: el servidor lo rechaza para siempre.
        val rechazaAlCinco = object : AttendanceRemote {
            override suspend fun send(childId: Int, status: String, date: String) {
                if (childId == 5) throw IllegalStateException("404")
                remote.send(childId, status, date)
            }
        }
        val resultado = OfflineAttendance(store, rechazaAlCinco) { ErrorKind.PERMANENT }.flush()

        assertEquals(FlushResult(sent = 1, dropped = 1, remaining = 0), resultado)
        assertEquals(listOf(6), remote.received.map { it.first })
        assertTrue(store.rows.isEmpty())
    }

    @Test
    fun `una marca hecha con conexion no la pisa una vieja de la cola`() = runBlocking {
        remote.failure = IOException()
        offline.mark(5, "ABSENT", today) // sin red: queda en la cola
        remote.failure = null
        offline.mark(5, "PRESENT", today) // ya hay red: llega directo y es lo más nuevo

        assertTrue("la marca vieja debía borrarse de la cola", store.rows.isEmpty())
        assertEquals(FlushResult(sent = 0, dropped = 0, remaining = 0), offline.flush())
        assertEquals(listOf(Triple(5, "PRESENT", today)), remote.received)
    }

    @Test
    fun `pendingMarks da la ultima marca de cada niño solo del dia pedido`() = runBlocking {
        remote.failure = IOException()
        offline.mark(5, "ABSENT", today)
        offline.mark(5, "PRESENT", today)
        offline.mark(6, "ABSENT", "2026-10-17")

        assertEquals(mapOf(5 to "PRESENT"), offline.pendingMarks(today))
    }
}
