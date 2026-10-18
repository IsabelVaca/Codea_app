package mx.tec.codea.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---------- /me ----------

@Serializable
data class ShiftTimesDto(
    val id: Int,
    val name: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
)

@Serializable
data class CenterDto(
    val id: Int,
    val name: String,
    val timezone: String = "",
    // el horario del centro; la API manda las horas como "08:00:00" y los días de 1 (lunes) a 7 (domingo).
    @SerialName("entry_time") val entryTime: String = "08:00:00",
    @SerialName("exit_time") val exitTime: String = "16:00:00",
    @SerialName("auto_close_time") val autoCloseTime: String = "21:00:00",
    @SerialName("work_days") val workDays: List<Int> = listOf(1, 2, 3, 4, 5),
)

// el admin cambia el horario completo; lo que queda en null no se manda.
@Serializable
data class CenterScheduleBody(
    @SerialName("entry_time") val entryTime: String,
    @SerialName("exit_time") val exitTime: String,
    @SerialName("auto_close_time") val autoCloseTime: String,
    @SerialName("work_days") val workDays: List<Int>,
)

@Serializable
data class MeTeacherDto(
    val classroom: RefDto? = null,
    @SerialName("is_primary") val isPrimary: Boolean = false,
    val shift: ShiftTimesDto? = null,
    val center: CenterDto,
)

@Serializable
data class MeChildDto(
    val id: Int,
    val name: String,
    val surname: String = "",
    @SerialName("full_name") val fullName: String,
    val classroom: RefDto? = null,
    val relationship: String? = null,
)

@Serializable
data class MeParentDto(
    val children: List<MeChildDto> = emptyList(),
)

@Serializable
data class MeDto(
    val id: String,
    val name: String,
    val surname: String = "",
    @SerialName("full_name") val fullName: String,
    val email: String? = null,
    // "ADMIN", "TEACHER" y/o "PARENT": con ellos la app decide qué tabs mostrar.
    val roles: List<String> = emptyList(),
    val teacher: MeTeacherDto? = null,
    val parent: MeParentDto? = null,
)

// ---------- mi sala ----------

@Serializable
data class RoomChildDto(
    val id: Int,
    val name: String,
    val surname: String = "",
    @SerialName("full_name") val fullName: String,
    val attendance: String,
    @SerialName("conversation_id") val conversationId: Int? = null,
    @SerialName("has_unread_chat") val hasUnreadChat: Boolean = false,
    @SerialName("unread_count") val unreadCount: Int = 0,
    @SerialName("has_note_today") val hasNoteToday: Boolean = false,
)

@Serializable
data class AttendanceBody(
    val status: String,
    // el día de la marca ("aaaa-mm-dd"); sin él la API usa hoy. Importa cuando la marca llega después.
    val date: String? = null,
)

// ---------- rutina ----------

@Serializable
data class MomentDto(
    val id: Int,
    val number: Int,
    val name: String,
    val category: String? = null,
    val status: String,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("confirmed_at") val confirmedAt: String? = null,
    val description: String? = null,
    @SerialName("report_count") val reportCount: Int = 0,
)

@Serializable
data class RoutineDto(
    @SerialName("classroom_id") val classroomId: Int,
    val name: String? = null,
    val moments: List<MomentDto> = emptyList(),
)

@Serializable
data class ReportBody(
    @SerialName("routine_moment_id") val routineMomentId: Int,
    val type: String,
    val method: String,
    val description: String,
    @SerialName("child_ids") val childIds: List<Int>,
    @SerialName("media_id") val mediaId: Int? = null,
)

// ---------- checador ----------

// la entrada no manda ubicación: solo el motivo del retardo, si lo hay.
@Serializable
data class CheckInBody(
    @SerialName("late_reason") val lateReason: String? = null,
)

@Serializable
data class CheckInDto(
    val id: Int,
    @SerialName("checked_in_at") val checkedInAt: String,
    val status: String,
    @SerialName("minutes_late") val minutesLate: Int = 0,
    @SerialName("late_reason") val lateReason: String? = null,
    @SerialName("place_name") val placeName: String = "",
    @SerialName("checked_out_at") val checkedOutAt: String? = null,
    @SerialName("auto_closed") val autoClosed: Boolean = false,
)

// ---------- chat ----------

@Serializable
data class MessageDto(
    val id: Int,
    @SerialName("conversation_id") val conversationId: Int,
    @SerialName("author_name") val authorName: String = "",
    val text: String,
    @SerialName("client_id") val clientId: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("is_mine") val isMine: Boolean = false,
    @SerialName("is_read") val isRead: Boolean = false,
)

@Serializable
data class ConversationChildDto(
    val id: Int,
    @SerialName("full_name") val fullName: String,
)

@Serializable
data class ConversationDto(
    val id: Int,
    val child: ConversationChildDto,
    @SerialName("unread_count") val unreadCount: Int = 0,
    @SerialName("last_message") val lastMessage: MessageDto? = null,
)

@Serializable
data class MessagePage(
    val items: List<MessageDto> = emptyList(),
    @SerialName("next_cursor") val nextCursor: String? = null,
)

@Serializable
data class MessageBody(
    val text: String,
    @SerialName("client_id") val clientId: String? = null,
)
