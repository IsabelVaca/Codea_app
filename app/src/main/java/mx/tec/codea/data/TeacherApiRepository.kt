package mx.tec.codea.data

import android.graphics.Bitmap
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import mx.tec.codea.data.api.ApiClient
import mx.tec.codea.data.api.AttendanceBody
import mx.tec.codea.data.api.CheckInBody
import mx.tec.codea.data.api.CheckInDto
import mx.tec.codea.data.api.DailyEntryBody
import mx.tec.codea.data.api.DayDto
import mx.tec.codea.data.api.DepartureBody
import mx.tec.codea.data.api.DepartureChildDto
import mx.tec.codea.data.api.PickupChoiceDto
import mx.tec.codea.data.api.GuardianDto
import mx.tec.codea.data.api.PickupDto
import mx.tec.codea.data.api.MeDto
import mx.tec.codea.data.api.ReportBody
import mx.tec.codea.data.api.RoomChildDto
import mx.tec.codea.data.api.RoutineDto
import mx.tec.codea.domain.AttendanceStatus
import mx.tec.codea.domain.CheckInRecord
import mx.tec.codea.domain.Child
import mx.tec.codea.domain.ChildHealth
import mx.tec.codea.domain.ChildProfile
import mx.tec.codea.domain.ChildProfileRules
import mx.tec.codea.domain.Contact
import mx.tec.codea.domain.DepartureEntry
import mx.tec.codea.domain.PickupChoice
import mx.tec.codea.domain.PickupKind
import mx.tec.codea.domain.FamilyNote
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

    // la salida del día: la API pide haber registrado la entrada.
    suspend fun checkOut(): CheckInRecord = api.checkOut().toRecord()

    suspend fun checkIn(lateReason: String): CheckInRecord =
        api.checkIn(CheckInBody(lateReason.trim().ifBlank { null })).toRecord()

    // ----- mi sala -----

    suspend fun roomChildren(classroomId: Int): List<RoomChildDto> = api.roomChildren(classroomId)

    // el resumen de hoy del niño (lo que ya se capturó) y las notas de la familia.
    suspend fun day(childId: Int): DayDto = api.childDay(childId, null)

    // la ficha: alergias y edad del niño, sus tutores con teléfono y las personas autorizadas a recogerlo.
    suspend fun childProfile(childId: Int): ChildProfile {
        val detail = api.childDetail(childId)
        val pickups = api.pickups(childId)
        return ChildProfile(
            allergies = detail.allergies?.trim()?.ifBlank { null },
            ageText = ChildProfileRules.ageText(detail.birthDate, hoyLocal()),
            guardians = detail.guardians.sortedByDescending { it.isPrimary }.map { it.toContact() },
            pickups = pickups.map { it.toContact() },
            health = ChildHealth(
                allergies = detail.allergies.orEmpty(),
                medicalConditions = detail.medicalConditions.orEmpty(),
                intolerances = detail.intolerances.orEmpty(),
                medications = detail.medications.orEmpty(),
                otherNotes = detail.otherNotes.orEmpty(),
            ),
        )
    }

    // ----- pase de salida -----

    // los niños que vinieron hoy, con quién puede recoger a cada uno y la salida de los que ya se fueron.
    suspend fun departures(classroomId: Int): List<DepartureEntry> =
        api.departures(classroomId).children.map { it.toEntry() }

    suspend fun registerDeparture(childId: String, choice: PickupChoice) {
        val body = when (choice.kind) {
            PickupKind.AUTHORIZED -> DepartureBody(pickupId = choice.id.toInt())
            PickupKind.GUARDIAN -> DepartureBody(guardianId = choice.id)
        }
        api.registerDeparture(childId.toInt(), body)
    }

    suspend fun undoDeparture(childId: String) {
        api.undoDeparture(childId.toInt())
    }

    suspend fun saveDailyEntry(childId: Int, body: DailyEntryBody) {
        api.saveDailyEntry(childId, body)
    }

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
    autoClosed = autoClosed,
)

// de lo que manda la API a lo que entiende la app.
fun RoomChildDto.toChild() = Child(
    id = id.toString(),
    name = fullName,
    // sin pase de lista el niño falta: solo "PRESENT" cuenta como presente.
    attendance = if (attendance == "PRESENT") AttendanceStatus.PRESENT else AttendanceStatus.ABSENT,
    hasUnreadChat = hasUnreadChat,
    notes = notes.map { FamilyNote(id = it.id, text = it.text, time = clockOf(it.createdAt) ?: "") },
    hasSummaryToday = hasSummaryToday,
)

private fun DepartureChildDto.toEntry() = DepartureEntry(
    childId = childId.toString(),
    name = fullName,
    choices = choices.map { it.toChoice() },
    time = departure?.let { clockOf(it.departedAt) },
    pickedUpBy = departure?.pickedUpBy,
)

private fun PickupChoiceDto.toChoice() = PickupChoice(
    kind = if (kind == "GUARDIAN") PickupKind.GUARDIAN else PickupKind.AUTHORIZED,
    id = id,
    name = fullName,
    relationship = parentesco(relationship),
)

internal fun GuardianDto.toContact() = Contact(name = fullName, relationship = parentesco(relationship), phone = phone)

private fun PickupDto.toContact() = Contact(name = fullName, relationship = relationship?.let(::parentesco), phone = phone)

// el parentesco llega como código de la API ("MOTHER") o como texto libre de una persona autorizada ("Abuela").
internal fun parentesco(valor: String?): String? = when (valor) {
    "MOTHER" -> "Mamá"
    "FATHER" -> "Papá"
    "LEGAL_GUARDIAN" -> "Tutor"
    "OTHER" -> null
    else -> valor?.ifBlank { null }
}

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
