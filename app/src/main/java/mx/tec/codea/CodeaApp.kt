package mx.tec.codea

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mx.tec.codea.navigation.CodeaNavHost
import mx.tec.codea.navigation.TopLevelDestination
import mx.tec.codea.navigation.toTopLevelDestination
import mx.tec.codea.ui.components.CodeaBottomBar

// the root of the whole interface. it joins three parts:
// the scaffold (the frame), the bottom bar (the tabs) and the nav host (the screens).
@Composable
fun CodeaApp(modifier: Modifier = Modifier) {
    // "remember" keeps the same nav controller alive when the ui draws again.
    val navController = rememberNavController()

    // this value changes every time the user goes to another screen,
    // and compose draws the bottom bar again with the new selected tab.
    val backStackEntry by navController.currentBackStackEntryAsState()

    // we look for the tab whose route matches the screen that is open now.
    val currentDestination = backStackEntry?.destination.toTopLevelDestination()

    // the scaffold gives us fixed places for common parts, like the bottom bar.
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            CodeaBottomBar(
                currentDestination = currentDestination,
                onDestinationClick = { destination ->
                    navController.navigateToTopLevel(destination)
                },
            )
        },
    ) { innerPadding ->
        // innerPadding is the space the bars use. we pass it to the screens,
        // so the content is not hidden behind the bottom bar or the status bar.
        CodeaNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// this is the recommended way to change tabs. it is an extension function:
// it adds a new ability to NavHostController without changing its class.
private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        // we remove the screens above the start screen, so the back button
        // does not go through every tab the user visited.
        // saveState remembers the state of the tab we are leaving.
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        // if the user taps the same tab again, we do not open a second copy.
        launchSingleTop = true
        // when the user comes back to a tab, we give back its saved state.
        restoreState = true
    }
}
