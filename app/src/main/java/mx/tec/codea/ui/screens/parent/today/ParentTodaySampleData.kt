package mx.tec.codea.ui.screens.parent.today

enum class TimelineEventType {
    CHECK_IN,
    MEAL,
    ACTIVITY,
    NAP,
}

data class TimelineEvent(
    val id: String,
    val title: String,
    val time: String,
    val description: String,
    val type: TimelineEventType = TimelineEventType.CHECK_IN,
)

data class DaySummary(
    val childFirstName: String = "Mateo",
    val childFullName: String = "Mateo Iglesias",
    val childInitials: String = "MI",
    val childGroup: String = "Preescolar 2 · Estancia La Oruga",
    val dateFormatted: String = "Hoy · 19 de agosto de 2026",
    val generalNote: String = "Día tranquilo. Comió todo, durmió su siesta completa y participó en la actividad.",
    val mood: String = "Contento",
    val meals: String = "Todo el plato",
    val diaperOrBathroom: String = "2 cambios",
    val napDuration: String = "50 minutos",
)

object ParentTodaySampleData {
    val daySummary = DaySummary()

    val timelineEvents = listOf(
        TimelineEvent(
            id = "event-1",
            title = "Llegada",
            time = "08:05",
            description = "Entró contento y se fue directo a su mesa.",
            type = TimelineEventType.CHECK_IN,
        ),
        TimelineEvent(
            id = "event-2",
            title = "Desayuno",
            time = "09:05",
            description = "Comió todo: fruta, avena y leche.",
            type = TimelineEventType.MEAL,
        ),
        TimelineEvent(
            id = "event-3",
            title = "Actividad de trazos",
            time = "10:30",
            description = "Trabajó líneas curvas. Se llevó su hoja a casa.",
            type = TimelineEventType.ACTIVITY,
        ),
        TimelineEvent(
            id = "event-4",
            title = "Siesta",
            time = "12:40",
            description = "Durmió 50 minutos.",
            type = TimelineEventType.NAP,
        ),
    )
}
