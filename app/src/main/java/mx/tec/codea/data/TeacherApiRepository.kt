package mx.tec.codea.data

import android.graphics.Bitmap
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import mx.tec.codea.data.api.ChildDetailDto
import mx.tec.codea.data.api.DepartureSheetDto
import mx.tec.codea.data.offline.Offline
import mx.tec.codea.data.offline.cachedResponse
import mx.tec.codea.data.offline.fetchAndCache
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

    // las lecturas se guardan en la caché (ApiCache) y cada una tiene su "…Guardado()" con lo último que se vio.
    // las de "hoy" llevan la fecha en la llave: lo de ayer nunca se muestra como si fuera de hoy.
    suspend fun me(): MeDto = fetchAndCache("me", MeDto.serializer()) { api.me() }
    suspend fun meGuardado(): MeDto? = cachedResponse("me", MeDto.serializer())

    // ----- checador -----

    // null = todavía no checa hoy.
    suspend fun checkInToday(): CheckInRecord? {
        val hoy = fetchAndCache("checkin:${hoyIso()}", CheckInHoy.serializer()) {
            val el: JsonElement = api.checkInToday()
            CheckInHoy(if (el is JsonNull) null else ApiClient.json.decodeFromJsonElement(CheckInDto.serializer(), el))
        }
        return hoy.registro?.toRecord()
    }

    // lo guardado de la checada de hoy: null = no hay nada guardado; registro = null quiere decir que todavía no checa.
    suspend fun checkInGuardado(): CheckInGuardado? =
        cachedResponse("checkin:${hoyIso()}", CheckInHoy.serializer())?.let { CheckInGuardado(it.registro?.toRecord()) }

    // la salida del día: la API pide haber registrado la entrada.
    suspend fun checkOut(): CheckInRecord = api.checkOut().toRecord()

    suspend fun checkIn(lateReason: String): CheckInRecord =
        api.checkIn(CheckInBody(lateReason.trim().ifBlank { null })).toRecord()

    // ----- mi sala -----

    suspend fun roomChildren(classroomId: Int): List<RoomChildDto> = api.roomChildren(classroomId)

    // el resumen de hoy del niño (lo que ya se capturó) y las notas de la familia.
    suspend fun day(childId: Int): DayDto = api.childDay(childId, null)

    // la ficha: alergias y edad del niño, sus tutores con teléfono y las personas autorizadas a recogerlo.
    suspend fun childProfile(childId: Int): ChildProfile = ficha(
        fetchAndCache("child:$childId", ChildDetailDto.serializer()) { api.childDetail(childId) },
        fetchAndCache("pickups:$childId", PICKUPS) { api.pickups(childId) },
    )

    // la ficha con lo último que se guardó, si están las dos partes.
    suspend fun childProfileGuardado(childId: Int): ChildProfile? {
        val detail = cachedResponse("child:$childId", ChildDetailDto.serializer()) ?: return null
        val pickups = cachedResponse("pickups:$childId", PICKUPS) ?: return null
        return ficha(detail, pickups)
    }

    private fun ficha(detail: ChildDetailDto, pickups: List<PickupDto>): ChildProfile {
        val fecha = detail.birthDate?.let { runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it) }.getOrNull() }
        return ChildProfile(
            allergies = detail.allergies?.trim()?.ifBlank { null },
            birthDateText = fecha?.let { SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "MX")).format(it) },
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
        fetchAndCache("departures:$classroomId:${hoyIso()}", DepartureSheetDto.serializer()) { api.departures(classroomId) }
            .children.map { it.toEntry() }

    suspend fun departuresGuardadas(classroomId: Int): List<DepartureEntry>? =
        cachedResponse("departures:$classroomId:${hoyIso()}", DepartureSheetDto.serializer())?.children?.map { it.toEntry() }

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

    suspend fun routine(classroomId: Int): RoutineDto =
        fetchAndCache(llaveRutina(classroomId), RoutineDto.serializer()) { api.routine(classroomId) }

    suspend fun routineGuardada(classroomId: Int): RoutineDto? = cachedResponse(llaveRutina(classroomId), RoutineDto.serializer())

    // la rutina que regresa la API al confirmar también se guarda: así la caché no se queda con el momento anterior.
    suspend fun confirm(momentId: Int): RoutineDto {
        val dto = api.confirmMoment(momentId)
        runCatching { Offline.apiCache?.write(llaveRutina(dto.classroomId), dto, RoutineDto.serializer()) }
        return dto
    }

    private fun llaveRutina(classroomId: Int) = "routine:$classroomId:${hoyIso()}"

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
    arrivalTime = clockOf(arrivedAt),
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

// la checada de hoy como se guarda en la caché: registro = null quiere decir que todavía no checa.
@Serializable
data class CheckInHoy(val registro: CheckInDto? = null)

private val PICKUPS = ListSerializer(PickupDto.serializer())

data class CheckInGuardado(val registro: CheckInRecord?)
