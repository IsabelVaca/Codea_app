package mx.tec.codea.data.offline

import android.content.Context
import androidx.room.Room
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import mx.tec.codea.CodeaApplication
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.SessionRepository
import mx.tec.codea.data.SessionState
import mx.tec.codea.data.api.ApiClient
import mx.tec.codea.data.api.AttendanceBody
import mx.tec.codea.data.api.RoomChildDto
import mx.tec.codea.data.api.RoomNoteDto
import mx.tec.codea.domain.AttendanceRemote
import mx.tec.codea.domain.OfflineAttendance
import mx.tec.codea.domain.PendingAttendance
import mx.tec.codea.domain.PendingStore
import java.util.concurrent.TimeUnit

// la asistente puede seguir marcando asistencia sin internet: las marcas esperan en la base local (Room)
// y WorkManager las manda solas cuando vuelve la red. También se guarda lo último que se vio de Mi sala
// para poder abrirla sin conexión. Se crea una vez, en AppContainer, y los ViewModels lo piden aquí.
object Offline {

    lateinit var attendance: OfflineAttendance
        private set
    lateinit var cache: RoomCache
        private set
    lateinit var scheduler: SyncScheduler
        private set

    fun init(context: Context, sessions: SessionRepository) {
        val db = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "codea.db")
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .build()
        val dao = db.dao()
        attendance = OfflineAttendance(RoomPendingStore(dao, sessions), RetrofitAttendanceRemote(), OfflineErrors::kindOf)
        cache = RoomCache(dao)
        scheduler = SyncScheduler(context.applicationContext)
    }
}

// la cola de quien tiene la sesión abierta: cada persona ve y manda solo lo suyo.
private class RoomPendingStore(private val dao: OfflineDao, private val sessions: SessionRepository) : PendingStore {

    private fun userId(): String? = sessions.current()?.userId

    override suspend fun add(childId: Int, status: String, date: String) {
        // sin sesión no hay a nombre de quién guardarla.
        val owner = userId() ?: throw IllegalStateException("No hay una sesión abierta")
        dao.insertPending(
            PendingAttendanceEntity(
                childId = childId, status = status, date = date, createdAt = System.currentTimeMillis(), userId = owner,
            ),
        )
    }

    override suspend fun all(): List<PendingAttendance> {
        val owner = userId() ?: return emptyList()
        return dao.pending(owner).map { PendingAttendance(it.id, it.childId, it.status, it.date) }
    }

    override suspend fun remove(ids: List<Long>) = dao.deletePending(ids)

    override suspend fun removeFor(childId: Int, date: String) {
        val owner = userId() ?: return
        dao.deletePendingFor(childId, date, owner)
    }

    // se vuelve a calcular cuando cambia la sesión: al entrar otra persona, el contador es el suyo.
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun count(): Flow<Int> = sessions.state.flatMapLatest { state ->
        if (state is SessionState.Active) dao.pendingCount(state.session.userId) else flowOf(0)
    }
}

private class RetrofitAttendanceRemote : AttendanceRemote {
    override suspend fun send(childId: Int, status: String, date: String) {
        ApiClient.api.setAttendance(childId, AttendanceBody(status = status, date = date))
    }
}

// lo último que se vio de Mi sala.
class CachedRoom(
    val classroomId: Int,
    val roomName: String,
    val teacherFullName: String,
    val teacherGreeting: String,
    val shiftStartMinutes: Int,
    val shiftEndMinutes: Int,
    val children: List<RoomChildDto>,
)

class RoomCache(private val dao: OfflineDao) {

    // la caché tiene nombres de niños: se borra cuando se cierra la sesión. La cola de marcas no (solo lleva ids y estados).
    suspend fun clear() {
        dao.clearChildren()
        dao.clearRoom()
    }

    suspend fun save(room: CachedRoom) {
        dao.replaceRoom(
            CachedRoomEntity(
                classroomId = room.classroomId,
                roomName = room.roomName,
                teacherFullName = room.teacherFullName,
                teacherGreeting = room.teacherGreeting,
                shiftStartMinutes = room.shiftStartMinutes,
                shiftEndMinutes = room.shiftEndMinutes,
                savedAt = System.currentTimeMillis(),
            ),
            room.children.map {
                CachedChildEntity(
                    id = it.id,
                    classroomId = room.classroomId,
                    fullName = it.fullName,
                    attendance = it.attendance,
                    conversationId = it.conversationId,
                    hasUnreadChat = it.hasUnreadChat,
                    hasNoteToday = it.hasNoteToday,
                    notesJson = ApiClient.json.encodeToString(notesSerializer, it.notes),
                    hasSummaryToday = it.hasSummaryToday,
                )
            },
        )
    }

    suspend fun load(): CachedRoom? {
        val room = dao.room() ?: return null
        return CachedRoom(
            classroomId = room.classroomId,
            roomName = room.roomName,
            teacherFullName = room.teacherFullName,
            teacherGreeting = room.teacherGreeting,
            shiftStartMinutes = room.shiftStartMinutes,
            shiftEndMinutes = room.shiftEndMinutes,
            children = dao.children(room.classroomId).map {
                RoomChildDto(
                    id = it.id,
                    name = it.fullName.substringBefore(" "),
                    fullName = it.fullName,
                    attendance = it.attendance,
                    conversationId = it.conversationId,
                    hasUnreadChat = it.hasUnreadChat,
                    hasNoteToday = it.hasNoteToday,
                    // si el JSON guardado no se puede leer, se pierde solo el texto de los avisos, no la lista.
                    notes = runCatching { ApiClient.json.decodeFromString(notesSerializer, it.notesJson) }.getOrDefault(emptyList()),
                    hasSummaryToday = it.hasSummaryToday,
                )
            },
        )
    }

    private companion object {
        val notesSerializer = ListSerializer(RoomNoteDto.serializer())
    }
}

// pide a WorkManager que mande la cola en cuanto haya red; si falla, reintenta con espera creciente.
class SyncScheduler(private val context: Context) {

    fun schedule() {
        val request = OneTimeWorkRequestBuilder<AttendanceSyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()
        // si ya hay uno esperando, no se duplica.
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }

    private companion object {
        const val WORK_NAME = "sync-attendance"
    }
}

class AttendanceSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // al arrancar en segundo plano la sesión todavía puede estar leyéndose del disco: se espera.
        val container = (applicationContext as CodeaApplication).container
        val state = container.sessionRepository.state.first { it !is SessionState.Loading }
        // sin sesión no se manda nada, pero tampoco se pierde: la cola espera a que alguien entre.
        if (state !is SessionState.Active) return Result.success()

        val result = Offline.attendance.flush()
        if (result.sent > 0) DataEvents.notifyChanged()
        return if (result.remaining > 0) Result.retry() else Result.success()
    }
}
