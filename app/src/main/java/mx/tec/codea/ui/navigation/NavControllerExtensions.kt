package mx.tec.codea.ui.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder

// these are extension functions: they add new abilities to NavHostController
// without changing its class. we keep every "how do we move" rule in this file.

// this is the recommended way to change tabs.
fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        leaveCurrentTab(startDestinationId = graph.findStartDestination().id)
        // when the user comes back to a tab, we give back its saved state.
        restoreState = true
    }
}

// runs when the user taps the tab that is already selected.
fun NavHostController.reselectTopLevel(destination: TopLevelDestination) {
    when (destination) {
        // a second tap closes the inner screen of the tab and shows its first screen,
        // like "reporte" -> "mi día" or "checador" -> "menú".
        TopLevelDestination.MY_DAY -> popBackStack<MyDayRoute>(inclusive = false)
        TopLevelDestination.MENU -> popBackStack<MenuRoute>(inclusive = false)
        // the other tabs have only one screen, so there is nothing to do.
        TopLevelDestination.MY_ROOM, TopLevelDestination.CHATS -> Unit
    }
}

// goes to the "menú" tab and asks it to tap the check-in card by itself.
// we do not restore the old state of the tab here, because we want a fresh
// menu that receives openChecker = true.
fun NavHostController.navigateToCheckerThroughMenu() {
    navigate(MenuRoute(openChecker = true)) {
        leaveCurrentTab(startDestinationId = graph.findStartDestination().id)
    }
}

// after "registrar mi entrada" we show the result. we remove the check-in screen
// from the stack first, so the back button can not register the same entry twice.
fun NavHostController.navigateToCheckInDone() {
    navigate(CheckInDoneRoute) {
        popUpTo<MenuRoute>()
    }
}

// after the check-in, we close the result screen (so the "menú" tab opens
// on the menu next time) and jump to "mi día", which now shows the routine.
fun NavHostController.navigateToMyDayAfterCheckIn() {
    popBackStack<MenuRoute>(inclusive = false)
    navigateToTopLevel(TopLevelDestination.MY_DAY)
}

// after "cerrar sesión" the day starts again, so we go to "mi día",
// which now shows "aún el día no comienza".
fun NavHostController.navigateToMyDayAfterLogout() {
    navigateToTopLevel(TopLevelDestination.MY_DAY)
}

// the rules we use every time we jump from one tab to another.
private fun NavOptionsBuilder.leaveCurrentTab(startDestinationId: Int) {
    // we remove the screens above the start screen, so the back button
    // does not go through every tab the user visited.
    // saveState remembers the state of the tab we are leaving.
    popUpTo(startDestinationId) {
        saveState = true
    }
    // if the user taps the same tab again, we do not open a second copy.
    launchSingleTop = true
}
