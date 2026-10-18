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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import mx.tec.codea.CodeaApplication
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.SessionState
import mx.tec.codea.data.api.ApiClient
import mx.tec.codea.data.api.AttendanceBody
import mx.tec.codea.data.api.RoomChildDto
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

    fun init(context: Context) {
        val db = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "codea.db").build()
        val dao = db.dao()
        attendance = OfflineAttendance(RoomPendingStore(dao), RetrofitAttendanceRemote(), OfflineErrors::kindOf)
        cache = RoomCache(dao)
        scheduler = SyncScheduler(context.applicationContext)
    }
}

private class RoomPendingStore(private val dao: OfflineDao) : PendingStore {
    override suspend fun add(childId: Int, status: String, date: String) {
        dao.insertPending(PendingAttendanceEntity(childId = childId, status = status, date = date, createdAt = System.currentTimeMillis()))
    }

    override suspend fun all(): List<PendingAttendance> =
        dao.pending().map { PendingAttendance(it.id, it.childId, it.status, it.date) }

    override suspend fun remove(ids: List<Long>) = dao.deletePending(ids)

    override suspend fun removeFor(childId: Int, date: String) = dao.deletePendingFor(childId, date)

    override fun count(): Flow<Int> = dao.pendingCount()
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
                )
            },
        )
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
