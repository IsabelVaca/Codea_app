package mx.tec.codea.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mx.tec.codea.ui.components.CodeaBottomBar
import mx.tec.codea.ui.state.CodeaViewModel

// the root of the whole interface. it joins three parts:
// the scaffold (the frame), the bottom bar (the tabs) and the nav host (the screens).
@Composable
fun CodeaApp(modifier: Modifier = Modifier) {
    // "remember" keeps the same nav controller alive when the ui draws again.
    val navController = rememberNavController()

    // CodeaApp is called right from setContent, so the owner of this view model is
    // the activity. there is only one for the whole app: every tab sees the same object.
    // if we called viewModel() inside each destination, each one would get its own copy.
    val viewModel: CodeaViewModel = viewModel()

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
                    // tapping the tab that is already open has its own behavior.
                    if (destination == currentDestination) {
                        navController.reselectTopLevel(destination)
                    } else {
                        navController.navigateToTopLevel(destination)
                    }
                },
            )
        },
    ) { innerPadding ->
        // innerPadding is the space the bars use. we pass it to the screens,
        // so the content is not hidden behind the bottom bar or the status bar.
        CodeaNavHost(
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
