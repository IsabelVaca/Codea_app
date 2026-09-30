package mx.tec.codea.ui.navigation

// the role of the person using the app right now. each tab in the bottom bar
// says which roles can see it (TopLevelDestination.roles), and CodeaApp
// filters the tab list with this value before drawing the bar.
// there is no login yet, so for now CodeaApp receives a fixed role instead
// of reading it from a session.
enum class Role {
    TEACHER,
    ADMIN,
    PARENT,
}
