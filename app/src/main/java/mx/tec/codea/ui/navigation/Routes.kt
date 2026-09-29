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
