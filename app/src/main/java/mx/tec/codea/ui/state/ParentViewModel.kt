package mx.tec.codea.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.ParentApiRepository
import mx.tec.codea.data.api.ApiConfig
import mx.tec.codea.data.api.CalendarDto
import mx.tec.codea.data.api.DayDto
import mx.tec.codea.data.api.FeedItemDto
import mx.tec.codea.data.api.toUserMessage
import mx.tec.codea.data.clockOf
import mx.tec.codea.data.api.WeeklySummaryDto
import mx.tec.codea.data.api.ChildDetailDto
import mx.tec.codea.data.hoyLocal
import mx.tec.codea.data.parseIso
import mx.tec.codea.data.toContact
import mx.tec.codea.domain.ChildChoice
import mx.tec.codea.domain.ChildHealth
import mx.tec.codea.domain.ChildOption
import mx.tec.codea.domain.ChildProfileRules
import mx.tec.codea.domain.MyChild
import mx.tec.codea.domain.DiaSemana
import mx.tec.codea.domain.ResumenSemana
import mx.tec.codea.domain.ResumenSemanaTextos
import mx.tec.codea.ui.screens.parent.calendar.CalendarDay
import mx.tec.codea.ui.screens.parent.calendar.CalendarMonthData
import mx.tec.codea.ui.screens.parent.calendar.DayAttendanceStatus
import mx.tec.codea.ui.screens.parent.photos.Categoria
import mx.tec.codea.ui.screens.parent.photos.Foto
import mx.tec.codea.ui.screens.parent.photos.Tono
import mx.tec.codea.data.toMenu
import mx.tec.codea.domain.MenuDelDia
import mx.tec.codea.ui.screens.parent.today.DaySummary
import mx.tec.codea.ui.screens.parent.today.TimelineEvent
import mx.tec.codea.ui.screens.parent.today.TimelineEventType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// el estado de las pantallas del padre: hoy, calendario, fotos y "Mi hijo" (sus datos y su salud).
// un padre puede tener varios hijos: `children` es la lista para el selector y todo lo demás es del hijo elegido.
class ParentViewModel : ViewModel() {

    private val repository = ParentApiRepository()
    private val es = Locale("es", "MX")

    private var childId: Int? = null
    private var childFirstName = ""

    // el día que se está viendo en "Hoy". null = hoy.
    private var selectedDate: String? = null

    // el mes que muestra el calendario: año y mes (0-11).
    private val shownYear = Calendar.getInstance().get(Calendar.YEAR)
    private val shownMonth = Calendar.getInstance().get(Calendar.MONTH)

    var childName by mutableStateOf("")
        private set

    // los hijos de la familia y el elegido; el selector solo sale con más de uno.
    var children by mutableStateOf<List<ChildOption>>(emptyList())
        private set

    var selectedChildId by mutableStateOf<Int?>(null)
        private set

    var summary by mutableStateOf(
        DaySummary(
            childFirstName = "", childFullName = "", childInitials = "", childGroup = "",
            dateFormatted = "", generalNote = "", mood = "—", meals = "—",
            diaperOrBathroom = "—", napDuration = "—",
        )
    )
        private set

    var timeline by mutableStateOf<List<TimelineEvent>>(emptyList())
        private set

    // lo que se come ese día (HN3); null si todavía no lo capturan.
    var menu by mutableStateOf<MenuDelDia?>(null)
        private set

    var monthData by mutableStateOf(
        CalendarMonthData(childFirstName = "", monthName = "", days = emptyList(), summaryText = "")
    )
        private set

    // el resumen de la semana de su hijo; null mientras carga.
    var weekly by mutableStateOf<ResumenSemana?>(null)
        private set

    // los datos de su hijo para "Mi hijo"; null mientras carga.
    var myChild by mutableStateOf<MyChild?>(null)
        private set

    // true mientras se guardan los datos de salud.
    var savingHealth by mutableStateOf(false)
        private set

    var photos by mutableStateOf<List<Foto>>(emptyList())
        private set

    val todayDayNumber: Int
        get() = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    init {
        viewModelScope.launch { DataEvents.changed.collect { cargar() } }
        cargar()
    }

    fun cargar() {
        if (!ApiConfig.isParent) return
        viewModelScope.launch {
            try {
                val me = repository.me()
                children = me.parent?.children.orEmpty().map { ChildOption(it.id, it.name, it.fullName) }
                // la recarga que sigue a cada cambio conserva al hijo elegido.
                val hijo = ChildChoice.pick(children, childId) ?: return@launch
                childId = hijo.id
                selectedChildId = hijo.id
                childName = hijo.fullName
                childFirstName = hijo.firstName
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DataEvents.notice("No se pudo cargar tu perfil: ${e.toUserMessage()}")
                return@launch
            }
            // cada parte se pide por separado: si una falla, las demás se muestran.
            parte("el día") { cargarDia() }
            parte("el calendario") { cargarMes() }
            parte("el resumen semanal") { cargarSemana() }
            parte("los datos de tu hijo") { cargarHijo() }
            parte("las fotos") { cargarFotos() }
        }
    }

    // la familia cambia de hijo: lo del anterior se quita para no mostrarlo mientras carga el nuevo.
    fun selectChild(id: Int) {
        val hijo = children.firstOrNull { it.id == id } ?: return
        if (hijo.id == childId) return
        childId = hijo.id
        selectedChildId = hijo.id
        childName = hijo.fullName
        childFirstName = hijo.firstName
        selectedDate = null
        timeline = emptyList()
        menu = null
        weekly = null
        myChild = null
        photos = emptyList()
        cargar()
    }

    private suspend fun parte(nombre: String, bloque: suspend () -> Unit) {
        try {
            bloque()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            DataEvents.notice("No se pudo cargar $nombre: ${e.toUserMessage()}")
        }
    }

    // ---------------- hoy ----------------

    private suspend fun cargarDia() {
        val id = childId ?: return
        val dia = repository.day(id, selectedDate)
        summary = dia.toSummary()
        timeline = dia.timeline.map { it.toEvent() }
        menu = dia.menu?.toMenu()?.takeUnless { it.vacio }
    }

    // el calendario manda aquí el día que tocó la mamá.
    fun selectDay(dayNumber: Int) {
        selectedDate = String.format(Locale.US, "%04d-%02d-%02d", shownYear, shownMonth + 1, dayNumber)
        viewModelScope.launch { parte("el día") { cargarDia() } }
    }

    fun sendNote(text: String, onSuccess: () -> Unit = {}) {
        val id = childId ?: return
        if (text.isBlank()) return
        launchAction(onSuccess) { repository.sendNote(id, text) }
    }

    // ---------------- calendario ----------------

    private suspend fun cargarMes() {
        val id = childId ?: return
        val mes = String.format(Locale.US, "%04d-%02d", shownYear, shownMonth + 1)
        monthData = repository.calendar(id, mes).toMonthData()
    }

    // ---------------- resumen semanal ----------------

    private suspend fun cargarSemana() {
        val id = childId ?: return
        weekly = repository.weekly(id).toResumen()
    }

    // "2026-10-12" y "2026-10-16" -> "12 al 16 de octubre" (si cruza de mes: "28 de septiembre al 2 de octubre").
    private fun rango(inicio: String, fin: String): String {
        val entrada = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val a = entrada.parse(inicio) ?: return "$inicio al $fin"
        val b = entrada.parse(fin) ?: return "$inicio al $fin"
        val mes = SimpleDateFormat("MMMM", es)
        val dia = SimpleDateFormat("d", es)
        return if (mes.format(a) == mes.format(b)) {
            "${dia.format(a)} al ${dia.format(b)} de ${mes.format(b)}"
        } else {
            "${dia.format(a)} de ${mes.format(a)} al ${dia.format(b)} de ${mes.format(b)}"
        }
    }

    private fun WeeklySummaryDto.toResumen() = ResumenSemana(
        rango = rango(weekStart, weekEnd),
        diasAsistidos = daysAttended,
        diasAusente = daysAbsent,
        animo = ResumenSemanaTextos.animo(moods),
        comidas = ResumenSemanaTextos.comidas(meals),
        siesta = ResumenSemanaTextos.siesta(napMinutesAverage),
        cambios = diaperChangesTotal,
        logros = achievements,
        notasFamilia = familyNotes,
        dias = days.mapIndexed { i, d ->
            DiaSemana(ResumenSemanaTextos.letrasDeLaSemana.getOrElse(i) { "" }, ResumenSemanaTextos.estado(d.status))
        },
    )

    // ---------------- mi hijo ----------------

    private suspend fun cargarHijo() {
        val id = childId ?: return
        myChild = repository.child(id).toMyChild()
    }

    // la familia escribe la salud de su hijo. la API regresa el expediente ya guardado y eso es lo que se ve.
    fun saveHealth(health: ChildHealth) {
        val id = childId ?: return
        if (savingHealth) return
        savingHealth = true
        launchAction(onSuccess = { DataEvents.notice("Datos de tu hijo guardados") }) {
            try {
                myChild = repository.saveHealth(id, health).toMyChild()
            } finally {
                savingHealth = false
            }
        }
    }

    private fun ChildDetailDto.toMyChild(): MyChild {
        val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(birthDate.orEmpty())
        return MyChild(
            fullName = fullName,
            birthDateText = fecha?.let { SimpleDateFormat("d 'de' MMMM 'de' yyyy", es).format(it) },
            ageText = ChildProfileRules.ageText(birthDate, hoyLocal()),
            classroomName = classroom?.name,
            guardians = guardians.sortedByDescending { it.isPrimary }.map { it.toContact() },
            health = ChildHealth(
                allergies = allergies.orEmpty(),
                medicalConditions = medicalConditions.orEmpty(),
                intolerances = intolerances.orEmpty(),
                medications = medications.orEmpty(),
                otherNotes = otherNotes.orEmpty(),
            ),
        )
    }

    // ---------------- fotos ----------------

    // al entrar a Fotos o a Hoy se vuelve a pedir lo del día: la maestra pudo subir algo mientras la app estaba abierta.
    fun refrescarFotos() {
        viewModelScope.launch { parte("las fotos") { cargarFotos() } }
    }

    fun refrescarDia() {
        viewModelScope.launch { parte("el día") { cargarDia() } }
    }

    private suspend fun cargarFotos() {
        val id = childId ?: return
        photos = repository.photos(id).map { it.toFoto() }
    }

    // la foto se borra de inmediato, sin aprobación de nadie (también si salen niños de otras familias).
    fun deletePhoto(foto: Foto) {
        launchAction(onSuccess = { DataEvents.notice("Foto borrada") }) {
            repository.deletePhoto(foto.id)
        }
    }

    // ---------------- de la API a las pantallas ----------------

    private fun fechaLarga(iso: String): String {
        val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso) ?: return iso
        return SimpleDateFormat("d 'de' MMMM 'de' yyyy", es).format(fecha)
    }

    private fun DayDto.toSummary(): DaySummary {
        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date())
        val s = summary
        val iniciales = child.fullName.split(" ").filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.first().uppercase() }
        return DaySummary(
            childFirstName = child.name,
            childFullName = child.fullName,
            childInitials = iniciales,
            childGroup = listOfNotNull(child.classroom?.name, child.centerName.ifBlank { null }).joinToString(" · "),
            dateFormatted = if (date == hoy) "Hoy · ${fechaLarga(date)}" else fechaLarga(date),
            generalNote = s?.generalNote?.ifBlank { null } ?: "Todavía no hay notas de este día.",
            mood = when (s?.mood) {
                "HAPPY" -> "Contento"
                "CALM" -> "Tranquilo"
                "TIRED" -> "Cansado"
                "SAD" -> "Triste"
                "UPSET" -> "Molesto"
                else -> "—"
            },
            meals = when (s?.meals) {
                "ALL" -> "Todo el plato"
                "MOST" -> "Casi todo"
                "HALF" -> "La mitad"
                "LITTLE" -> "Poco"
                "NONE" -> "No comió"
                else -> "—"
            },
            diaperOrBathroom = s?.diaperChanges?.let { if (it == 1) "1 cambio" else "$it cambios" } ?: "—",
            napDuration = s?.napMinutes?.let { "$it minutos" } ?: "—",
        )
    }

    private fun mmss(iso: String): String = clockOf(iso)?.let {
        // las pantallas del padre usan "08:05".
        if (it.indexOf(':') == 1) "0$it" else it
    } ?: ""

    private fun mx.tec.codea.data.api.TimelineEventDto.toEvent() = TimelineEvent(
        id = id,
        title = title,
        time = mmss(time),
        description = description,
        type = when (type) {
            "MEAL" -> TimelineEventType.MEAL
            "ACTIVITY" -> TimelineEventType.ACTIVITY
            "NAP" -> TimelineEventType.NAP
            "ARRIVAL" -> TimelineEventType.ARRIVAL
            "DEPARTURE" -> TimelineEventType.DEPARTURE
            else -> TimelineEventType.CHECK_IN
        },
    )

    // "1 día", "3 días".
    private fun dias(n: Int) = if (n == 1) "1 día" else "$n días"

    private fun CalendarDto.toMonthData(): CalendarMonthData {
        val primero = Calendar.getInstance().apply { set(shownYear, shownMonth, 1) }
        // la cuadrícula empieza en lunes: lunes = 0 ... domingo = 6.
        val offset = (primero.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
        val nombreMes = SimpleDateFormat("LLLL yyyy", es).format(primero.time)
            .replaceFirstChar { it.uppercase() }
        val ausencias = summary.absences
        return CalendarMonthData(
            childFirstName = childFirstName,
            monthName = nombreMes,
            days = days.map { CalendarDay(it.day, it.status.toDayStatus()) },
            summaryText = "${dias(summary.fullDays)} ${if (summary.fullDays == 1) "asistido" else "asistidos"} · " +
                (if (ausencias == 1) "1 ausencia" else "$ausencias ausencias") +
                " · ${dias(summary.withNote)} con nota de la maestra. Toca cualquier día para ver su resumen.",
            startOffset = offset,
        )
    }

    private fun String.toDayStatus() = when (this) {
        "FULL_DAY" -> DayAttendanceStatus.FULL_DAY
        "WITH_NOTE" -> DayAttendanceStatus.WITH_NOTE
        "ABSENT" -> DayAttendanceStatus.ABSENT
        else -> DayAttendanceStatus.NO_RECORD
    }

    private fun FeedItemDto.toFoto(): Foto {
        val fecha = parseIso(takenAt)
        val hoy = Calendar.getInstance()
        val ayer = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val dia = Calendar.getInstance().apply { time = fecha ?: java.util.Date() }
        fun mismoDia(a: Calendar, b: Calendar) =
            a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
        val etiquetaDia = when {
            mismoDia(dia, hoy) -> "Hoy"
            mismoDia(dia, ayer) -> "Ayer"
            else -> SimpleDateFormat("EEEE", es).format(dia.time).replaceFirstChar { it.uppercase() }
        }
        val categoria = when (category) {
            "MEALS" -> Categoria.COMIDAS
            "NAP" -> Categoria.SIESTA
            "INCIDENTS" -> Categoria.INCIDENTES
            else -> Categoria.ACTIVIDADES
        }
        val etiqueta = when (categoria) {
            Categoria.COMIDAS -> "Comida"
            Categoria.SIESTA -> "Siesta"
            Categoria.INCIDENTES -> "Incidente"
            else -> "Actividad"
        }
        return Foto(
            id = id,
            dia = etiquetaDia,
            fecha = SimpleDateFormat("EEE d MMM", es).format(dia.time).replace(".", "").lowercase(es),
            etiqueta = etiqueta,
            categoria = categoria,
            tono = Tono.entries[id % Tono.entries.size],
            hora = clockOf(takenAt) ?: "",
            maestra = teacherName,
            nota = note ?: "",
            url = url,
        )
    }
}
