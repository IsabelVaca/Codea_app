package mx.tec.codea.data

import kotlinx.serialization.builtins.ListSerializer
import mx.tec.codea.data.api.ApiClient
import mx.tec.codea.data.offline.cachedResponse
import mx.tec.codea.data.offline.fetchAndCache
import mx.tec.codea.data.api.ClassroomCapacityBody
import mx.tec.codea.data.api.ClassroomCreateBody
import mx.tec.codea.data.api.ClassroomDto
import mx.tec.codea.data.api.StaffBody
import mx.tec.codea.domain.Sala

const val SIN_ASIGNAR = "Sin asignar"

class SalaApiRepository {

    private val api get() = ApiClient.api

    // de la API; la respuesta se guarda para mostrarla al instante la próxima vez (ver ApiCache).
    suspend fun getAll(): List<Sala> = mapear(fetchAndCache(CACHE, SERIALIZER) { api.classrooms() })

    suspend fun cached(): List<Sala>? = cachedResponse(CACHE, SERIALIZER)?.let { mapear(it) }

    private fun mapear(lista: List<ClassroomDto>): List<Sala> {
        AdminDirectory.rememberClassrooms(lista)
        return lista.map { it.toSala() }
    }

    private companion object {
        const val CACHE = "classrooms"
        val SERIALIZER = ListSerializer(ClassroomDto.serializer())
    }

    suspend fun crear(nombre: String, cupoMaximo: Int, asistente: String?) {
        val teacherId = if (asistente.isNullOrBlank() || asistente == SIN_ASIGNAR) null
        else AdminDirectory.teacherId(asistente)
        api.createClassroom(
            ClassroomCreateBody(name = nombre.trim(), capacity = cupoMaximo, teacherId = teacherId)
        )
    }

    suspend fun editarCupo(salaId: String, cupoMaximo: Int) {
        api.updateClassroom(salaId.toInt(), ClassroomCapacityBody(capacity = cupoMaximo))
    }

    // deja a esa asistente como la principal (y única) de la sala.
    suspend fun asignarAsistente(salaId: String, asistente: String) {
        val teacherId = if (asistente == SIN_ASIGNAR) null else AdminDirectory.teacherId(asistente)
        api.setStaff(
            salaId.toInt(),
            StaffBody(teacherIds = listOfNotNull(teacherId), primaryTeacherId = teacherId)
        )
    }

    suspend fun eliminar(salaId: String) {
        api.deleteClassroom(salaId.toInt())
    }
}

// pasa de lo que manda la API a lo que ya entiende la app.
private fun ClassroomDto.toSala() = Sala(
    id = id.toString(),
    nombre = name,
    cupoMaximo = capacity,
    asistente = primaryTeacher?.fullName ?: SIN_ASIGNAR,
)
