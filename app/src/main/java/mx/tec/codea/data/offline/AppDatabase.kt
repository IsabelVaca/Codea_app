package mx.tec.codea.data.offline

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

// las marcas de asistencia que esperan conexión.
@Entity(tableName = "pending_attendance")
data class PendingAttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val childId: Int,
    val status: String,
    // "aaaa-mm-dd" del día en que se hizo la marca.
    val date: String,
    val createdAt: Long,
)

// lo último que se vio de Mi sala, para poder abrirla sin conexión. Solo hay una fila (id = 1).
@Entity(tableName = "cached_room")
data class CachedRoomEntity(
    @PrimaryKey val id: Int = 1,
    val classroomId: Int,
    val roomName: String,
    val teacherFullName: String,
    val teacherGreeting: String,
    val shiftStartMinutes: Int,
    val shiftEndMinutes: Int,
    val savedAt: Long,
)

@Entity(tableName = "cached_child")
data class CachedChildEntity(
    @PrimaryKey val id: Int,
    val classroomId: Int,
    val fullName: String,
    val attendance: String,
    val conversationId: Int?,
    val hasUnreadChat: Boolean,
    val hasNoteToday: Boolean,
)

@Dao
interface OfflineDao {

    @Insert
    suspend fun insertPending(entity: PendingAttendanceEntity)

    @Query("SELECT * FROM pending_attendance ORDER BY id")
    suspend fun pending(): List<PendingAttendanceEntity>

    @Query("DELETE FROM pending_attendance WHERE id IN (:ids)")
    suspend fun deletePending(ids: List<Long>)

    @Query("DELETE FROM pending_attendance WHERE childId = :childId AND date = :date")
    suspend fun deletePendingFor(childId: Int, date: String)

    @Query("SELECT COUNT(*) FROM pending_attendance")
    fun pendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: CachedRoomEntity)

    @Query("SELECT * FROM cached_room WHERE id = 1")
    suspend fun room(): CachedRoomEntity?

    @Query("DELETE FROM cached_child")
    suspend fun clearChildren()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChildren(children: List<CachedChildEntity>)

    @Query("SELECT * FROM cached_child WHERE classroomId = :classroomId ORDER BY fullName")
    suspend fun children(classroomId: Int): List<CachedChildEntity>

    // la lista nueva reemplaza a la vieja completa, o no cambia nada.
    @Transaction
    suspend fun replaceRoom(room: CachedRoomEntity, children: List<CachedChildEntity>) {
        clearChildren()
        insertRoom(room)
        insertChildren(children)
    }
}

@Database(
    entities = [PendingAttendanceEntity::class, CachedRoomEntity::class, CachedChildEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): OfflineDao
}
