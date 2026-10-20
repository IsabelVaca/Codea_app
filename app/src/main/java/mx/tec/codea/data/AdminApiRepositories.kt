package mx.tec.codea.data

import mx.tec.codea.data.api.AnnouncementBody
import mx.tec.codea.data.api.ApiClient
import mx.tec.codea.data.api.ChildClassroomBody
import mx.tec.codea.data.api.ChildCreateBody
import mx.tec.codea.data.api.CenterDto
import mx.tec.codea.data.api.CenterScheduleBody
import mx.tec.codea.data.api.ChildDto
import mx.tec.codea.data.api.ParentCreateBody
import mx.tec.codea.data.api.PickupBody
import mx.tec.codea.data.api.ParentDto
import mx.tec.codea.data.api.TeacherClassroomBody
import mx.tec.codea.data.api.TeacherCreateBody
import mx.tec.codea.data.api.TeacherDto
import mx.tec.codea.domain.Aviso
import mx.tec.codea.domain.Docente
import mx.tec.codea.domain.HorarioCentro
import mx.tec.codea.domain.formatTimeOfDay
import mx.tec.codea.domain.Infante
import mx.tec.codea.domain.Padre
import mx.tec.codea.domain.PersonaAutorizada
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

// ---------------- docentes ----------------

class DocenteApiRepository {

    private val api get() = ApiClient.api

    suspend fun getAll(): List<Docente> {
        val lista = api.teachers()
        AdminDirectory.rememberTeachers(lista)
        return lista.map { it.toDocente() }.sortedBy { it.nombre }
    }

    suspend fun alta(nombre: String, apellido: String, telefono: String, correo: String, sala: String) {
        val creada = api.createTeacher(
            TeacherCreateBody(
                name = nombre.trim(),
                surname = apellido.trim(),
                phone = telefono.trim(),
                email = correo.trim(),
                classroomId = AdminDirectory.classroomId(sala),
            )
        )
        // la API da el código de acceso una sola vez: el admin lo comparte para que la docente active su cuenta.
        creada.accessCode?.let {
            DataEvents.accessCode(
                AccessCodeNotice(creada.fullName, it.code, shortDateOf(it.expiresAt).orEmpty(), creada.phone, creada.email, it.link),
            )
        }
    }

    // sala vacía = sin sala.
    suspend fun cambiarSala(docenteId: String, sala: String) {
        api.updateTeacher(docenteId, TeacherClassroomBody(classroomId = AdminDirectory.classroomId(sala)))
    }

    suspend fun setBaja(docenteId: String, baja: Boolean) {
        if (baja) api.deactivateTeacher(docenteId) else api.reactivateTeacher(docenteId)
    }
}

private fun TeacherDto.toDocente() = Docente(
    id = id,
    nombre = fullName,
    iniciales = inicialesDe(fullName),
    sala = classroom?.name ?: "",
    horaChecada = horaDe(checkedInAt),
    baja = !active,
)

//  infantes

class InfanteApiRepository {

    private val api get() = ApiClient.api

    suspend fun getAll(): List<Infante> =
        api.children().map { it.toInfante() }.sortedBy { it.nombre }

    suspend fun inscribir(nombre: String, apellido: String, nacimiento: String, sala: String, alergias: String) {
        api.createChild(
            ChildCreateBody(
                name = nombre.trim(),
                surname = apellido.trim(),
                birthDate = nacimiento,
                allergies = alergias.trim().ifBlank { null },
                classroomId = AdminDirectory.classroomId(sala),
            )
        )
    }

    // sala vacía = sacar de la sala.
    suspend fun cambiarSala(infanteId: String, sala: String) {
        api.updateChild(infanteId.toInt(), ChildClassroomBody(classroomId = AdminDirectory.classroomId(sala)))
    }

    suspend fun setBaja(infanteId: String, baja: Boolean) {
        if (baja) api.deactivateChild(infanteId.toInt()) else api.reactivateChild(infanteId.toInt())
    }
}

private fun ChildDto.toInfante() = Infante(
    id = id.toString(),
    nombre = fullName,
    iniciales = inicialesDe(fullName),
    edad = age ?: 0,
    sala = classroom?.name ?: "",
    baja = !active,
)

// las personas que pueden recoger a un niño: las registra el admin, la docente las elige en el pase de salida.
class PersonaAutorizadaApiRepository {

    private val api get() = ApiClient.api

    suspend fun deNino(infanteId: String): List<PersonaAutorizada> =
        api.pickups(infanteId.toInt()).map {
            PersonaAutorizada(id = it.id.toString(), nombre = it.fullName, parentesco = it.relationship.orEmpty(), telefono = it.phone.orEmpty())
        }

    suspend fun agregar(infanteId: String, nombre: String, parentesco: String, telefono: String) {
        api.addPickup(
            infanteId.toInt(),
            PickupBody(
                fullName = nombre.trim(),
                relationship = parentesco.trim().ifBlank { null },
                phone = telefono.trim().ifBlank { null },
            ),
        )
    }

    suspend fun quitar(infanteId: String, personaId: String) {
        api.removePickup(infanteId.toInt(), personaId.toInt())
    }
}

//  padres

class PadreApiRepository {

    private val api get() = ApiClient.api

    suspend fun getAll(): List<Padre> =
        api.parents().map { it.toPadre() }.sortedBy { it.nombre }

    suspend fun inscribir(nombre: String, apellido: String, telefono: String, correo: String, parentesco: String, hijoIds: List<String>) {
        val creado = api.createParent(
            ParentCreateBody(
                name = nombre.trim(),
                surname = apellido.trim(),
                phone = telefono.trim(),
                email = correo.trim(),
                relationship = parentescoToApi(parentesco),
                childIds = hijoIds.map { it.toInt() },
            )
        )
        creado.accessCode?.let {
            DataEvents.accessCode(
                AccessCodeNotice(creado.fullName, it.code, shortDateOf(it.expiresAt).orEmpty(), creado.phone, creado.email, it.link),
            )
        }
    }

    suspend fun setAcceso(padreId: String, conAcceso: Boolean) {
        if (conAcceso) api.reactivateParent(padreId) else api.deactivateParent(padreId)
    }

    // la API genera un código nuevo; se muestra para que el admin lo comparta.
    suspend fun reenviarInvitacion(padreId: String) {
        val padre = api.resendInvitation(padreId)
        padre.accessCode?.let {
            DataEvents.accessCode(
                AccessCodeNotice(padre.fullName, it.code, shortDateOf(it.expiresAt).orEmpty(), padre.phone, padre.email, it.link),
            )
        }
    }
}

private fun ParentDto.toPadre() = Padre(
    id = id,
    nombre = fullName,
    iniciales = inicialesDe(fullName),
    parentesco = parentescoFromApi(relationship),
    hijoIds = childIds.map { it.toString() },
    telefono = phone ?: "",
    correo = email ?: "",
    conAcceso = hasAccess,
)

// horario del centro

class CentroApiRepository {

    suspend fun horario(): HorarioCentro = ApiClient.api.center().toHorario()

    suspend fun guardarHorario(horario: HorarioCentro): HorarioCentro =
        ApiClient.api.updateCenterSchedule(
            CenterScheduleBody(
                entryTime = formatTimeOfDay(horario.entradaMinutos),
                exitTime = formatTimeOfDay(horario.salidaMinutos),
                autoCloseTime = formatTimeOfDay(horario.cierreMinutos),
                workDays = horario.diasDeTrabajo.sorted(),
            ),
        ).toHorario()
}

fun CenterDto.toHorario() = HorarioCentro(
    entradaMinutos = timeToMinutes(entryTime) ?: (8 * 60),
    salidaMinutos = timeToMinutes(exitTime) ?: (16 * 60),
    cierreMinutos = timeToMinutes(autoCloseTime) ?: (21 * 60),
    diasDeTrabajo = workDays.toSet(),
)

// avisos

class AvisoApiRepository {

    // salaId = null: aviso para todo el centro.
    // jpeg = la imagen ya comprimida (ImageCompressor); el aviso lleva texto, imagen o ambos.
    suspend fun publicar(texto: String, salaId: String? = null, jpeg: ByteArray? = null) {
        val mediaId = jpeg?.let { MediaUploader.upload(it, "image/jpeg", "PHOTO") }
        ApiClient.api.publishAnnouncement(
            AnnouncementBody(text = texto.trim(), mediaId = mediaId, classroomId = salaId?.toInt())
        )
    }

    // los avisos que le tocan a quien pregunta, el más reciente primero:
    // el admin ve todos; la asistente y los papás, los del centro entero y los de su sala.
    suspend fun getAll(): List<Aviso> = ApiClient.api.announcements().map {
        Aviso(
            id = it.id.toString(),
            destino = it.classroomName ?: it.classroomId?.let { "Sala eliminada" },
            fecha = formatearFecha(it.createdAt),
            texto = it.text,
            imagenUrl = it.mediaUrl,
        )
    }
}

// "2026-10-16T22:30:12.123Z" (UTC) -> "16 oct · 16:30" en la hora del teléfono.
// no se usa java.time porque la app soporta Android 7 (API 24).
internal fun formatearFecha(iso: String): String = try {
    val utc = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
    val fecha = utc.parse(iso.take(19))!!
    SimpleDateFormat("d MMM · HH:mm", Locale("es", "MX")).format(fecha)
} catch (e: Exception) {
    iso.take(10)
}

// ayudas

private fun inicialesDe(nombre: String): String =
    nombre.trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

private fun parentescoToApi(texto: String): String = when (texto) {
    "Mamá" -> "MOTHER"
    "Papá" -> "FATHER"
    "Tutor", "Tutora" -> "LEGAL_GUARDIAN"
    else -> "OTHER"
}

private fun parentescoFromApi(valor: String?): String = when (valor) {
    "MOTHER" -> "Mamá"
    "FATHER" -> "Papá"
    "LEGAL_GUARDIAN" -> "Tutor"
    else -> "Otro"
}

// "2026-10-14T14:05:00.123+00:00" -> "8:05" en la hora del teléfono.
// null cuando la docente no ha checado hoy (o la hora no se puede leer).
private fun horaDe(iso: String?): String? {
    if (iso.isNullOrBlank()) return null
    return try {
        val limpio = iso.replace(Regex("\\.\\d+"), "").let {
            if (it.endsWith("Z")) it.dropLast(1) + "+00:00" else it
        }
        val fecha = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).parse(limpio)
        if (fecha == null) null else SimpleDateFormat("H:mm", Locale.getDefault()).format(fecha)
    } catch (e: Exception) {
        try {
            iso.substring(11, 16)
        } catch (e2: Exception) {
            null
        }
    }
}
