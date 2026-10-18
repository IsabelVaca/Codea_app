

package mx.tec.codea.ui.navigation

import kotlinx.serialization.Serializable

// a route is the "address" of a screen inside the app.
// we use @Serializable objects instead of plain strings, so the compiler
// can check our routes for us and we avoid typos like "my_dya".
// a "graph" route is a group of screens that live inside the same tab.
// routes only carry ids (a number, a name), never whole objects:
// the screen reads the fresh data from the view model with that id.

// the "mi día" tab has more than one screen, so we group them in a graph.
// it is also the first tab the user sees when the app opens.
@Serializable
data object MyDayGraph

// the routine of today. before the check-in it shows "aún el día no comienza".
@Serializable
data object MyDayRoute

// la rutina de hoy, de solo lectura. se abre una sola vez, justo después de registrar la entrada.
@Serializable
data object RutinaDeHoyRoute

// the three steps of the report. each one knows which moment of the day it is about.
@Serializable
data class SelectReportMethodRoute(val subprocessNumber: Int)

@Serializable
data class ReportRoute(val subprocessNumber: Int)

@Serializable
data class SelectStudentsRoute(val subprocessNumber: Int)

// the list of children in the room of the teacher.
@Serializable
data object MyRoomRoute

// the conversations with the families.
@Serializable
data object ChatsRoute

// the "menú" tab also has more than one screen: the menu, the check-in and its result.
@Serializable
data object MenuGraph

// the teacher profile, the check-in and other options.
// when openChecker is true, the menu taps the check-in card by itself,
// so the teacher sees where the check-in lives before it opens.
@Serializable
data class MenuRoute(val openChecker: Boolean = false)

// the check-in screen, where the teacher registers the start of the shift.
@Serializable
data object CheckerRoute

// the "entrada registrada" feedback after the check-in.
@Serializable
data object CheckInDoneRoute

// an option of the menu that is not built yet. we send the enum name as text.
@Serializable
data class MenuOptionRoute(val optionName: String)

// la lista de avisos de la fundación que ve la asistente (opción "Avisos" del menú).
@Serializable
data object AvisosFundacionRoute

// the "centro" tab has two screens, so it is a graph like MyDayGraph: the
// bottom bar keeps "centro" selected while "crear sala" is open on top of it.
@Serializable
data object AdminCentroGraph

@Serializable
data object AdminCentroRoute

@Serializable
data object AdminCrearSalaRoute

// detalle de una sala: viaja el id, no el objeto.
@Serializable
data class AdminSalaDetalleRoute(val salaId: String)

// "docentes" is still a single screen.
@Serializable
data object AdminDocentesRoute


@Serializable
data object AdminDocentesGraph

@Serializable
data object AdminAltaAsistenteRoute

// detalle de un docente: viaja el id, no el objeto.
@Serializable
data class AdminDocenteDetalleRoute(val docenteId: String)
// "infantes" is a graph too, for the same reason as "centro": "inscribir
// infante" opens on top of it and the bottom bar stays on "infantes".
@Serializable
data object AdminInfantesGraph

@Serializable
data object AdminInfantesRoute

@Serializable
data object AdminInscribirInfanteRoute

// detalle de un infante: mismo patrón que sala y docente.
@Serializable
data class AdminInfanteDetalleRoute(val infanteId: String)

// the payment log of one child, opened from its detail.
@Serializable
data class AdminPagosInfanteRoute(val infanteId: String)

// parent tabs. for now each tab has a single screen.
// if one later gains inner screens, it can be converted into a graph
// without changing the role-based bottom navigation idea.
@Serializable
data object ParentTodayRoute

@Serializable
data object ParentCalendarRoute

@Serializable
data object ParentPhotosRoute

@Serializable
data object ParentDocumentsRoute


@Serializable
data object PadresAdminGraph

@Serializable
data object PadresAdminRoute

@Serializable
data class PadresAdminDetalleRoute(val padreId: String)

@Serializable
data object PadresAdminInscribirRoute

// "rutinas" is a graph too: the list, "nueva rutina" and the detail of one
// routine all keep the bottom bar on the "rutinas" tab.
@Serializable
data object RutinasAdminGraph

@Serializable
data object RutinasAdminRoute

// "nueva rutina" tiene tres pasos pero un solo formulario, como el reporte:
// el dueño del form es la primera pantalla del flujo.
@Serializable
data object RutinasAdminNuevaRoute

@Serializable
data object RutinasAdminCatalogoRoute

@Serializable
data object RutinasAdminOrdenRoute

// detalle de una rutina: viaja el id, no el objeto.
@Serializable
data class RutinasAdminDetalleRoute(val rutinaId: String)

// "agregar momento" abre sobre el detalle de la rutina que lo recibe.
@Serializable
data class RutinasAdminAgregarMomentoRoute(val rutinaId: String)
// "Mi cuenta": perfil, cambiar contraseña y cerrar sesión. la abren el admin (Centro), el padre (Hoy)
// y la docente (Menú → Mi perfil).
@Serializable
data object AccountRoute
