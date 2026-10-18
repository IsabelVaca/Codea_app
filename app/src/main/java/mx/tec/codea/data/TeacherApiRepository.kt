package mx.tec.codea.data

import android.graphics.Bitmap
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import mx.tec.codea.data.api.ApiClient
import mx.tec.codea.data.api.AttendanceBody
import mx.tec.codea.data.api.CheckInBody
import mx.tec.codea.data.api.CheckInDto
import mx.tec.codea.data.api.MeDto
import mx.tec.codea.data.api.NotifyBody
import mx.tec.codea.data.api.ReportBody
import mx.tec.codea.data.api.RoomChildDto
import mx.tec.codea.data.api.RoutineDto
import mx.tec.codea.domain.AttendanceStatus
import mx.tec.codea.domain.CheckInRecord
import mx.tec.codea.domain.Child
import mx.tec.codea.domain.Report
import mx.tec.codea.domain.Routine
import mx.tec.codea.domain.Subprocess
import mx.tec.codea.domain.SubprocessStatus

// todo lo que la docente pide o manda a la API.
class TeacherApiRepository {

    private val api get() = ApiClient.api

    suspend fun me(): MeDto = api.me()

    // ----- checador -----

    // null = todavía no checa hoy.
    suspend fun checkInToday(): CheckInRecord? {
        val el: JsonElement = api.checkInToday()
        if (el is JsonNull) return null
        return ApiClient.json.decodeFromJsonElement(CheckInDto.serializer(), el).toRecord()
    }

    // la salida del día: la API pide haber registrado la entrada y estar dentro del radio.
    suspend fun checkOut(latitude: Double, longitude: Double): CheckInRecord =
        api.checkOut(CheckInBody(latitude, longitude)).toRecord()

    suspend fun checkIn(latitude: Double, longitude: Double, lateReason: String): CheckInRecord =
        api.checkIn(
            CheckInBody(latitude, longitude, lateReason.trim().ifBlank { null })
        ).toRecord()

    suspend fun notifyCoordination(reason: String) {
        api.notifyCoordination(NotifyBody(reason = reason))
    }

    // ----- mi sala -----

    suspend fun roomChildren(classroomId: Int): List<RoomChildDto> = api.roomChildren(classroomId)

    suspend fun setAttendance(childId: String, status: AttendanceStatus) {
        api.setAttendance(childId.toInt(), AttendanceBody(status.name))
    }

    // ----- rutina -----

    suspend fun routine(classroomId: Int): RoutineDto = api.routine(classroomId)

    suspend fun confirm(momentId: Int): RoutineDto = api.confirmMoment(momentId)

    // si hay foto, primero se sube a /media y el reporte lleva su media_id.
    suspend fun createReport(momentId: Int, report: Report, photo: Bitmap? = null) {
        val mediaId = photo?.let { MediaUploader.uploadPhoto(it) }
        api.createReport(
            ReportBody(
                routineMomentId = momentId,
                type = report.type.name,
                method = report.method.name,
                description = report.description,
                childIds = report.childIds.map { it.toInt() },
                mediaId = mediaId,
            )
        )
    }
}

private fun CheckInDto.toRecord() = CheckInRecord(
    arrivalMinutes = minutesOfDay(checkedInAt) ?: 0,
    placeName = placeName,
    minutesLate = minutesLate,
    lateReason = lateReason ?: "",
    departureMinutes = minutesOfDay(checkedOutAt),
)

// de lo que manda la API a lo que entiende la app.
fun RoomChildDto.toChild() = Child(
    id = id.toString(),
    name = fullName,
    attendance = if (attendance == "ABSENT") AttendanceStatus.ABSENT else AttendanceStatus.PRESENT,
    hasUnreadChat = hasUnreadChat,
    hasNoteToday = hasNoteToday,
)

fun RoutineDto.toRoutine() = Routine(
    name = name ?: "Rutina del día",
    subprocesses = moments.map {
        Subprocess(
            number = it.number,
            name = it.name,
            status = when (it.status) {
                "DONE" -> SubprocessStatus.DONE
                "ACTIVE" -> SubprocessStatus.ACTIVE
                else -> SubprocessStatus.PENDING
            },
            time = clockOf(it.confirmedAt) ?: clockOf(it.startedAt),
            description = it.description,
            category = it.category,
        )
    },
)
