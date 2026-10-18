package mx.tec.codea.data

import mx.tec.codea.data.api.AnnouncementBody
import mx.tec.codea.data.api.ApiClient
import mx.tec.codea.data.api.ChildClassroomBody
import mx.tec.codea.data.api.ChildCreateBody
import mx.tec.codea.data.api.ChildDto
import mx.tec.codea.data.api.ParentCreateBody
import mx.tec.codea.data.api.ParentDto
import mx.tec.codea.data.api.TeacherClassroomBody
import mx.tec.codea.data.api.TeacherCreateBody
import mx.tec.codea.data.api.TeacherDto
import mx.tec.codea.domain.Aviso
import mx.tec.codea.domain.Docente
import mx.tec.codea.domain.Infante
import mx.tec.codea.domain.Padre
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

    suspend fun alta(nombre: String, apellido: String, telefono: String, correo: String, sala: String, turno: String) {
        val creada = api.createTeacher(
            TeacherCreateBody(
                name = nombre.trim(),
                surname = apellido.trim(),
                phone = telefono.trim(),
                email = correo.trim(),
                classroomId = AdminDirectory.classroomId(sala),
                shiftId = AdminDirectory.shiftId(turno),
            )
        )
        // la API da el código de acceso una sola vez: el admin lo comparte para que la docente active su cuenta.
        creada.accessCode?.let { DataEvents.accessCode(AccessCodeNotice(creada.fullName, it.code, shortDateOf(it.expiresAt).orEmpty())) }
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

    suspend fun inscribir(nombre: String, apellido: String, edad: Int, sala: String, alergias: String) {
        api.createChild(
            ChildCreateBody(
                name = nombre.trim(),
                surname = apellido.trim(),
                birthDate = nacimientoParaEdad(edad),
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
        creado.accessCode?.let { DataEvents.accessCode(AccessCodeNotice(creado.fullName, it.code, shortDateOf(it.expiresAt).orEmpty())) }
    }

    suspend fun setAcceso(padreId: String, conAcceso: Boolean) {
        if (conAcceso) api.reactivateParent(padreId) else api.deactivateParent(padreId)
    }

    // la API genera un código nuevo; se muestra para que el admin lo comparta.
    suspend fun reenviarInvitacion(padreId: String) {
        val padre = api.resendInvitation(padreId)
        padre.accessCode?.let { DataEvents.accessCode(AccessCodeNotice(padre.fullName, it.code, shortDateOf(it.expiresAt).orEmpty())) }
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

// avisos

class AvisoApiRepository {

    // salaId = null: aviso para todo el centro.
    suspend fun publicar(texto: String, salaId: String? = null) {
        ApiClient.api.publishAnnouncement(
            AnnouncementBody(text = texto.trim(), classroomId = salaId?.toInt())
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
        )
    }
}

// "2026-10-16T22:30:12.123Z" (UTC) -> "16 oct · 16:30" en la hora del teléfono.
// no se usa java.time porque la app soporta Android 7 (API 24).
private fun formatearFecha(iso: String): String = try {
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

// el formulario pide la edad y la API la fecha de nacimiento: se calcula una fecha que dé esa edad.
// se resta un día de más para que la diferencia de zona horaria con el servidor no cambie la edad.
private fun nacimientoParaEdad(edad: Int): String {
    val c = Calendar.getInstance()
    c.add(Calendar.YEAR, -edad)
    c.add(Calendar.DAY_OF_YEAR, -1)
    return String.format(
        Locale.US, "%04d-%02d-%02d",
        c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)
    )
}

// "2026-10-14T14:05:00.123+00:00" -> "8:05" en la hora del teléfono. sin checada: "--:--".
private fun horaDe(iso: String?): String {
    if (iso.isNullOrBlank()) return "--:--"
    return try {
        val limpio = iso.replace(Regex("\\.\\d+"), "").let {
            if (it.endsWith("Z")) it.dropLast(1) + "+00:00" else it
        }
        val fecha = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).parse(limpio)
        if (fecha == null) "--:--" else SimpleDateFormat("H:mm", Locale.getDefault()).format(fecha)
    } catch (e: Exception) {
        try {
            iso.substring(11, 16)
        } catch (e2: Exception) {
            "--:--"
        }
    }
}
