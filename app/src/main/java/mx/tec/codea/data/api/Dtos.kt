package mx.tec.codea.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---------- lo que responde la API ----------

@Serializable
data class StaffDto(
    val id: String,
    val name: String,
    val surname: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("is_primary") val isPrimary: Boolean = false,
)

@Serializable
data class ClassroomDto(
    val id: Int,
    val name: String,
    @SerialName("school_grade") val schoolGrade: String? = null,
    val capacity: Int,
    val enrolled: Int,
    val available: Int,
    @SerialName("primary_teacher") val primaryTeacher: StaffDto? = null,
)

@Serializable
data class RefDto(
    val id: Int,
    val name: String,
)

@Serializable
data class ShiftDto(
    val id: Int,
    val name: String,
)

@Serializable
data class TeacherDto(
    val id: String,
    val name: String,
    val surname: String = "",
    @SerialName("full_name") val fullName: String,
    val phone: String? = null,
    val email: String? = null,
    val classroom: RefDto? = null,
    @SerialName("is_primary") val isPrimary: Boolean = false,
    val shift: ShiftDto? = null,
    @SerialName("checked_in_at") val checkedInAt: String? = null,
    val active: Boolean = true,
    @SerialName("has_account") val hasAccount: Boolean = false,
    @SerialName("children_count") val childrenCount: Int = 0,
)

@Serializable
data class ChildDto(
    val id: Int,
    val name: String,
    val surname: String = "",
    @SerialName("full_name") val fullName: String,
    val age: Int? = null,
    val allergies: String? = null,
    val classroom: RefDto? = null,
    val active: Boolean = true,
)

@Serializable
data class ParentDto(
    val id: String,
    val name: String,
    val surname: String = "",
    @SerialName("full_name") val fullName: String,
    val phone: String? = null,
    val email: String? = null,
    val relationship: String? = null,
    @SerialName("child_ids") val childIds: List<Int> = emptyList(),
    @SerialName("has_access") val hasAccess: Boolean = true,
    @SerialName("has_account") val hasAccount: Boolean = false,
)

// ---------- lo que manda la app ----------
// los campos con valor por defecto NO se mandan si no cambian (la API los toma como "no tocar").
// los campos sin valor por defecto sí se mandan, incluso en null (por ejemplo "sacar de la sala").

@Serializable
data class ClassroomCreateBody(
    val name: String,
    val capacity: Int,
    @SerialName("teacher_id") val teacherId: String? = null,
)

@Serializable
data class ClassroomCapacityBody(
    val capacity: Int,
)

@Serializable
data class StaffBody(
    @SerialName("teacher_ids") val teacherIds: List<String>,
    @SerialName("primary_teacher_id") val primaryTeacherId: String? = null,
)

@Serializable
data class TeacherCreateBody(
    val name: String,
    val surname: String,
    val phone: String,
    val email: String,
    @SerialName("classroom_id") val classroomId: Int? = null,
    @SerialName("shift_id") val shiftId: Int? = null,
)

@Serializable
data class TeacherClassroomBody(
    @SerialName("classroom_id") val classroomId: Int?,
)

@Serializable
data class ChildCreateBody(
    val name: String,
    val surname: String,
    @SerialName("birth_date") val birthDate: String,
    val allergies: String? = null,
    @SerialName("classroom_id") val classroomId: Int? = null,
)

@Serializable
data class ChildClassroomBody(
    @SerialName("classroom_id") val classroomId: Int?,
)

@Serializable
data class ParentCreateBody(
    val name: String,
    val surname: String,
    val phone: String,
    val email: String,
    val relationship: String,
    @SerialName("child_ids") val childIds: List<Int>,
)

@Serializable
data class AnnouncementBody(
    val text: String,
    // sin sala (null) el aviso es para todo el centro.
    @SerialName("classroom_id") val classroomId: Int? = null,
)

@Serializable
data class AnnouncementDto(
    val id: Int,
    val text: String,
    @SerialName("classroom_id") val classroomId: Int? = null,
    @SerialName("classroom_name") val classroomName: String? = null,
    @SerialName("created_at") val createdAt: String,
)


// ----- plantillas de rutina -----
@Serializable
data class TemplateMomentDto(
    val number: Int,
    val name: String,
    val category: String,
)

@Serializable
data class TemplateDto(
    val id: Int,
    val name: String,
    @SerialName("is_default") val isDefault: Boolean = false,
    val moments: List<TemplateMomentDto> = emptyList(),
    // las salas que la tienen asignada (las que no tienen una propia usan la predeterminada).
    val classrooms: List<RefDto> = emptyList(),
)

@Serializable
data class TemplateMomentBody(
    val name: String,
    val category: String,
)

// la plantilla se manda entera, con sus momentos en orden.
@Serializable
data class TemplateBody(
    val name: String,
    val moments: List<TemplateMomentBody>,
    @SerialName("is_default") val isDefault: Boolean,
)

// templateId = null deja a la sala con la plantilla predeterminada.
@Serializable
data class TemplateAssignBody(
    @SerialName("template_id") val templateId: Int?,
)
