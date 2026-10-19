package mx.tec.codea.data

import mx.tec.codea.data.api.ApiClient
import mx.tec.codea.data.api.ClassroomDto
import mx.tec.codea.data.api.TeacherDto

// la app habla con nombres (sala "Maternal", asistente "Paola Sánchez") y la API con ids.
// aquí se guardan las últimas listas que llegaron para traducir de nombre a id.
// si el nombre no está, se vuelve a pedir la lista una vez.
object AdminDirectory {

    private var classrooms: List<ClassroomDto> = emptyList()
    private var teachers: List<TeacherDto> = emptyList()

    fun rememberClassrooms(list: List<ClassroomDto>) {
        classrooms = list
    }

    fun rememberTeachers(list: List<TeacherDto>) {
        teachers = list
    }

    // null = sin sala (nombre vacío). si el nombre no existe, lanza error.
    suspend fun classroomId(name: String): Int? {
        if (name.isBlank()) return null
        classrooms.firstOrNull { it.name == name }?.let { return it.id }
        classrooms = ApiClient.api.classrooms()
        return classrooms.firstOrNull { it.name == name }?.id
            ?: throw IllegalStateException("No se encontró la sala \"$name\"")
    }

    // la asistente se identifica por su nombre completo.
    suspend fun teacherId(fullName: String): String? {
        if (fullName.isBlank()) return null
        teachers.firstOrNull { it.fullName == fullName }?.let { return it.id }
        teachers = ApiClient.api.teachers()
        return teachers.firstOrNull { it.fullName == fullName }?.id
            ?: throw IllegalStateException("No se encontró a \"$fullName\"")
    }
}
