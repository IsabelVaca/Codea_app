package mx.tec.codea.ui.screens.parent.calendar

enum class DayAttendanceStatus {
    FULL_DAY,
    WITH_NOTE,
    ABSENT,
    NO_RECORD,
}

data class CalendarDay(
    val dayNumber: Int,
    val status: DayAttendanceStatus = DayAttendanceStatus.NO_RECORD,
)

data class CalendarMonthData(
    val childFirstName: String = "Mateo",
    val monthName: String = "Agosto 2026",
    val days: List<CalendarDay> = defaultDays,
    val summaryText: String = "12 días asistidos · 1 ausencia · 4 días con nota de la maestra. Toca cualquier día para ver su resumen.",
) {
    companion object {
        val defaultDays = (1..31).map { day ->
            val status = when (day) {
                3, 5, 6, 10, 12, 14, 17 -> DayAttendanceStatus.FULL_DAY
                4, 11, 13, 18 -> DayAttendanceStatus.WITH_NOTE
                7 -> DayAttendanceStatus.ABSENT
                19 -> DayAttendanceStatus.FULL_DAY
                else -> DayAttendanceStatus.NO_RECORD
            }
            CalendarDay(dayNumber = day, status = status)
        }
    }
}

object ParentCalendarSampleData {
    val monthData = CalendarMonthData()
}
