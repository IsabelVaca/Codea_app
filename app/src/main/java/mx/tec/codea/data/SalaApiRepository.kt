package mx.tec.codea.data

import mx.tec.codea.data.api.ApiClient
import mx.tec.codea.data.api.ClassroomCapacityBody
import mx.tec.codea.data.api.ClassroomCreateBody
import mx.tec.codea.data.api.ClassroomDto
import mx.tec.codea.data.api.StaffBody
import mx.tec.codea.domain.Sala

const val SIN_ASIGNAR = "Sin asignar"

class SalaApiRepository {

    private val api get() = ApiClient.api

    suspend fun getAll(): List<Sala> {
        val lista = api.classrooms()
        AdminDirectory.rememberClassrooms(lista)
        return lista.map { it.toSala() }
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
