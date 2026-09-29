package mx.tec.codea.domain

// the three ways to write a new report.
enum class ReportMethod { WRITTEN, DICTATION, PHOTO }

// what kind of thing happened. the spanish name lives in the ui (strings.xml),
// so this file does not care about the language of the app.
enum class ReportType { INCIDENT, ACHIEVEMENT, HEALTH, BEHAVIOR }

// a saved report. it points to the moment and the children by their id,
// never by the whole object, so it always reads fresh data.
data class Report(
    val subprocessNumber: Int,
    val type: ReportType,
    val method: ReportMethod,
    val description: String,
    val childIds: Set<String>,
    val hasPhoto: Boolean,
    val time: String,
)
