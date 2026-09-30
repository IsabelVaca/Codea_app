package mx.tec.codea.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mx.tec.codea.ui.components.CodeaBottomBar
import mx.tec.codea.ui.screens.roleselection.RoleSelectionScreen
import mx.tec.codea.ui.state.CodeaViewModel

// the root of the whole interface. it joins three parts:
// the scaffold (the frame), the bottom bar (the tabs) and the nav host (the screens).
@Composable
fun CodeaApp(modifier: Modifier = Modifier) {
    // there is no login yet, so the user picks a role when the app opens.
    var selectedRole by rememberSaveable { mutableStateOf<Role?>(null) }

    // CodeaApp is called right from setContent, so the owner of this view model is
    // the activity. there is only one for the whole app: every tab sees the same object.
    // if we called viewModel() inside each destination, each one would get its own copy.
    val viewModel: CodeaViewModel = viewModel()

    val role = selectedRole
    if (role == null) {
        RoleSelectionScreen(
            onRoleSelected = { selectedRole = it },
            modifier = modifier,
        )
        return
    }

    // a different role receives a completely fresh navigation controller
    // instead of inheriting another role's back stack.
    key(role) {
        RoleApp(role = role, viewModel = viewModel, modifier = modifier)
    }
}

@Composable
private fun RoleApp(
    role: Role,
    viewModel: CodeaViewModel,
    modifier: Modifier = Modifier,
) {
    // "remember" keeps the same nav controller alive when the ui draws again.
    val navController = rememberNavController()

    // each role only sees its own tabs.
    val visibleDestinations = remember(role) {
        TopLevelDestination.entries.filter { role in it.roles }
    }

    val startDestination: Any = remember(role) {
        when (role) {
            Role.TEACHER -> MyDayGraph
            Role.ADMIN -> AdminCentroGraph
            Role.PARENT -> ParentTodayRoute
        }
    }

    // this value changes every time the user goes to another screen,
    // and compose draws the bottom bar again with the new selected tab.
    val backStackEntry by navController.currentBackStackEntryAsState()

    // we look for the tab whose route matches the screen that is open now.
    val currentDestination = backStackEntry?.destination.toTopLevelDestination()

    val snackbarHostState = remember { SnackbarHostState() }

    // the scaffold gives us fixed places for common parts, like the bottom bar.
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            CodeaBottomBar(
                destinations = visibleDestinations,
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        // innerPadding is the space the bars use. we pass it to the screens,
        // so the content is not hidden behind the bottom bar or the status bar.
        CodeaNavHost(
            navController = navController,
            viewModel = viewModel,
            startDestination = startDestination,
            snackbarHostState = snackbarHostState,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        )
    }
}
