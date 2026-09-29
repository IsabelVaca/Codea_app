

package mx.tec.codea.navigation

import kotlinx.serialization.Serializable

// a route is the "address" of a screen inside the app.
// we use @Serializable objects instead of plain strings, so the compiler
// can check our routes for us and we avoid typos like "my_dya".
// a "graph" route is a group of screens that live inside the same tab.

// the "mi día" tab has two screens, so we group them in a graph.
// it is also the first tab the user sees when the app opens.
@Serializable
data object MyDayGraph

// the routine of today.
@Serializable
data object MyDayRoute

// the message we show when the teacher has not checked in yet.
@Serializable
data object DayNotStartedRoute

// the list of children in the room of the teacher.
@Serializable
data object MyRoomRoute

// the conversations with the families.
@Serializable
data object ChatsRoute

// the "menú" tab also has two screens: the menu and the check-in.
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
