package mx.tec.codea.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import mx.tec.codea.R

// a "top level destination" is one of the tabs in the bottom bar.
// we keep all the tab information in one enum, so the bottom bar and the
// navigation graph read from the same place. if you add a tab, you only add it here.
enum class TopLevelDestination(
    // the route that opens when the user taps this tab. it can be one screen
    // or a graph, when the tab has more than one screen inside.
    val route: Any,
    // the icon we draw inside the tab.
    val icon: ImageVector,
    // we save the text id (not the text itself), so android can translate it.
    @StringRes val labelRes: Int,
    // a locked tab is visible but the user cannot open it yet.
    // all tabs are open now, but we keep this for the next sections.
    val isLocked: Boolean,
    // which roles see this tab. CodeaApp filters TopLevelDestination.entries
    // with this before handing the list to the bottom bar, the same way the
    // "avisos" app hides its publish button when the session's role does not
    // allow it: the role decides what exists, the tab does not ask.
    val roles: Set<Role>,
) {
    // the order here is the order of the tabs, the same as in the prototype.
    MY_DAY(
        route = MyDayGraph,
        icon = Icons.Filled.DateRange,
        labelRes = R.string.nav_my_day,
        isLocked = false,
        roles = setOf(Role.TEACHER),
    ),
    MY_ROOM(
        route = MyRoomRoute,
        icon = Icons.Filled.Face,
        labelRes = R.string.nav_my_room,
        isLocked = false,
        roles = setOf(Role.TEACHER),
    ),

    // the basic icon set has no chat bubble, so we use the envelope for now.
    CHATS(
        route = ChatsRoute,
        icon = Icons.Filled.Email,
        labelRes = R.string.nav_chats,
        isLocked = false,
        roles = setOf(Role.TEACHER),
    ),
    MENU(
        route = MenuGraph,
        icon = Icons.Filled.Menu,
        labelRes = R.string.nav_menu,
        isLocked = false,
        roles = setOf(Role.TEACHER),
    ),

    // the admin tabs. each is a single screen for now, so its route is not a graph.
    CENTRO(
        route = AdminCentroRoute,
        icon = Icons.Filled.Home,
        labelRes = R.string.nav_admin_centro,
        isLocked = false,
        roles = setOf(Role.ADMIN),
    ),
    DOCENTES(
        route = AdminDocentesRoute,
        icon = Icons.Filled.School,
        labelRes = R.string.nav_admin_docentes,
        isLocked = false,
        roles = setOf(Role.ADMIN),
    ),
    INFANTES(
        route = AdminInfantesRoute,
        icon = Icons.Filled.Groups,
        labelRes = R.string.nav_admin_infantes,
        isLocked = false,
        roles = setOf(Role.ADMIN),
    ),
}

// finds the tab that a screen belongs to, or null if it is not inside any tab.
// "hierarchy" also checks parent graphs, useful if a tab has inner screens later.
fun NavDestination?.toTopLevelDestination(): TopLevelDestination? =
    TopLevelDestination.entries.firstOrNull { destination ->
        this?.hierarchy?.any { it.hasRoute(destination.route::class) } == true
    }
