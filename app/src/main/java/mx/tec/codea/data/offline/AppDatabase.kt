package mx.tec.codea.data.offline

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    // quién la hizo: si otra docente entra en el mismo teléfono, la cola de la primera no se manda con la sesión de la segunda.
    // las filas de la versión 1 (sin dueño) quedan con "" y nadie las manda.
    @ColumnInfo(defaultValue = "")
    val userId: String = "",
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
    // los avisos de casa de hoy, como JSON (lista de RoomNoteDto); "[]" si no hay.
    // sin defaultValue en la anotación: la migración 2 -> 3 pone el DEFAULT y Room no compara los defaults que la entidad no declara.
    val notesJson: String = "[]",
    val hasSummaryToday: Boolean = false,
    // la hora de entrada del niño (ISO), tal como la mandó la API; null si está ausente.
    val arrivedAt: String? = null,
)

@Dao
interface OfflineDao {

    @Insert
    suspend fun insertPending(entity: PendingAttendanceEntity)

    @Query("SELECT * FROM pending_attendance WHERE userId = :userId ORDER BY id")
    suspend fun pending(userId: String): List<PendingAttendanceEntity>

    @Query("DELETE FROM pending_attendance WHERE id IN (:ids)")
    suspend fun deletePending(ids: List<Long>)

    @Query("DELETE FROM pending_attendance WHERE childId = :childId AND date = :date AND userId = :userId")
    suspend fun deletePendingFor(childId: Int, date: String, userId: String)

    @Query("SELECT COUNT(*) FROM pending_attendance WHERE userId = :userId")
    fun pendingCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: CachedRoomEntity)

    @Query("SELECT * FROM cached_room WHERE id = 1")
    suspend fun room(): CachedRoomEntity?

    @Query("DELETE FROM cached_child")
    suspend fun clearChildren()

    @Query("DELETE FROM cached_room")
    suspend fun clearRoom()

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
    version = 4,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): OfflineDao

    companion object {
        // versión 2: cada marca pendiente guarda quién la hizo.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE pending_attendance ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
            }
        }

        // versión 3: la lista guardada de Mi sala también lleva el texto de los avisos y si el resumen del día ya está.
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cached_child ADD COLUMN notesJson TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE cached_child ADD COLUMN hasSummaryToday INTEGER NOT NULL DEFAULT 0")
            }
        }

        // versión 4: la lista guardada de Mi sala también lleva la hora de entrada de cada niño.
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cached_child ADD COLUMN arrivedAt TEXT")
            }
        }
    }
}
