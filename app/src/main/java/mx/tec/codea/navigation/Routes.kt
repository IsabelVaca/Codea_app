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

// the admin tabs. each one is its own top level destination (see
// TopLevelDestination), not a graph, because for now they are a single screen.
@Serializable
data object AdminCentroRoute

@Serializable
data object AdminDocentesRoute

@Serializable
data object AdminInfantesRoute
