package mx.tec.codea.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---------- hoy ----------

@Serializable
data class DayChildDto(
    val id: Int,
    val name: String,
    val surname: String = "",
    @SerialName("full_name") val fullName: String,
    val classroom: RefDto? = null,
    @SerialName("center_name") val centerName: String = "",
)

@Serializable
data class DailyEntryDto(
    val mood: String? = null,
    val meals: String? = null,
    @SerialName("diaper_changes") val diaperChanges: Int? = null,
    @SerialName("nap_minutes") val napMinutes: Int? = null,
    @SerialName("general_note") val generalNote: String? = null,
)

@Serializable
data class TimelineEventDto(
    val id: String,
    val type: String,
    val title: String,
    val time: String,
    val description: String = "",
)

@Serializable
data class DayDto(
    val child: DayChildDto,
    val date: String,
    val summary: DailyEntryDto? = null,
    val timeline: List<TimelineEventDto> = emptyList(),
)

@Serializable
data class NoteBody(
    val text: String,
)

// ---------- calendario ----------

@Serializable
data class CalendarDayDto(
    val day: Int,
    val date: String,
    val status: String,
)

@Serializable
data class CalendarSummaryDto(
    @SerialName("full_days") val fullDays: Int = 0,
    @SerialName("with_note") val withNote: Int = 0,
    val absences: Int = 0,
)

@Serializable
data class CalendarDto(
    val month: String,
    val days: List<CalendarDayDto> = emptyList(),
    val summary: CalendarSummaryDto = CalendarSummaryDto(),
)

// ---------- resumen semanal ----------

@Serializable
data class WeekDayDto(
    val date: String,
    val status: String,
)

@Serializable
data class WeeklySummaryDto(
    @SerialName("week_start") val weekStart: String,
    @SerialName("week_end") val weekEnd: String,
    @SerialName("days_attended") val daysAttended: Int = 0,
    @SerialName("days_absent") val daysAbsent: Int = 0,
    val moods: Map<String, Int> = emptyMap(),
    val meals: Map<String, Int> = emptyMap(),
    @SerialName("nap_minutes_average") val napMinutesAverage: Int? = null,
    @SerialName("diaper_changes_total") val diaperChangesTotal: Int = 0,
    val achievements: List<String> = emptyList(),
    @SerialName("family_notes") val familyNotes: Int = 0,
    val days: List<WeekDayDto> = emptyList(),
)

// ---------- documentos ----------

@Serializable
data class DocumentDto(
    val id: Int,
    val title: String,
    @SerialName("due_date") val dueDate: String? = null,
    val status: String,
    @SerialName("uploaded_at") val uploadedAt: String? = null,
    val url: String? = null,
)

@Serializable
data class PickupDto(
    val id: Int,
    @SerialName("full_name") val fullName: String,
    val relationship: String? = null,
    val phone: String? = null,
)

@Serializable
data class PickupBody(
    @SerialName("full_name") val fullName: String,
)

@Serializable
data class DocumentUploadBody(
    @SerialName("media_id") val mediaId: Int,
)

// ---------- fotos ----------

@Serializable
data class FeedItemDto(
    val id: Int,
    val url: String,
    val category: String,
    @SerialName("taken_at") val takenAt: String,
    @SerialName("teacher_name") val teacherName: String = "",
    val note: String? = null,
)

@Serializable
data class FeedPage(
    val items: List<FeedItemDto> = emptyList(),
    @SerialName("next_cursor") val nextCursor: String? = null,
)

@Serializable
data class DeletionBody(
    val reason: String? = null,
)

// ---------- subir archivos ----------

@Serializable
data class UploadBody(
    @SerialName("content_type") val contentType: String,
    @SerialName("size_bytes") val sizeBytes: Int,
    val kind: String,
)

@Serializable
data class UploadDto(
    @SerialName("media_id") val mediaId: Int,
    val url: String,
    val fields: Map<String, String> = emptyMap(),
)
