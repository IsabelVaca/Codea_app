package mx.tec.codea.domain

// the rules of a valid report. the ui asks here before it lets the teacher continue.
object ReportValidator {
    const val DESCRIPTION_MIN = 10

    // a photo alone does not tell the family anything, so we always need some text.
    fun isDescriptionValid(description: String): Boolean =
        description.trim().length >= DESCRIPTION_MIN

    // a child who did not come today can not be part of something that happened here.
    fun canBeInReport(child: Child): Boolean = child.attendance == AttendanceStatus.PRESENT

    // without a child we do not know which family to tell.
    fun hasChildren(childIds: Set<String>): Boolean = childIds.isNotEmpty()

    fun isValid(description: String, childIds: Set<String>): Boolean =
        isDescriptionValid(description) && hasChildren(childIds)
}
