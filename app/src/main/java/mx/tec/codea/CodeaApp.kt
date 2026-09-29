package mx.tec.codea

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mx.tec.codea.navigation.AdminCentroGraph
import mx.tec.codea.navigation.CodeaNavHost
import mx.tec.codea.navigation.MyDayGraph
import mx.tec.codea.navigation.Role
import mx.tec.codea.navigation.TopLevelDestination
import mx.tec.codea.navigation.navigateToTopLevel
import mx.tec.codea.navigation.reselectTopLevel
import mx.tec.codea.navigation.toTopLevelDestination
import mx.tec.codea.ui.components.CodeaBottomBar

// the root of the whole interface. it joins three parts:
// the scaffold (the frame), the bottom bar (the tabs) and the nav host (the screens).
@Composable
fun CodeaApp(
    modifier: Modifier = Modifier,
    // there is no login yet, so the role comes in as a fixed value instead of
    // from a session. once there is a real session, this becomes something
    // like `sesion.rol` and gets read from it instead of passed in.
    role: Role = Role.ADMIN,
) {
    // "remember" keeps the same nav controller alive when the ui draws again.
    val navController = rememberNavController()

    // only the tabs the current role can see. the bottom bar and the start
    // screen both read from this, so the two never disagree about who can
    // see what — the same way "avisos" checks sesion.puedePublicar once and
    // trusts it everywhere the role matters.
    val visibleDestinations = remember(role) {
        TopLevelDestination.entries.filter { role in it.roles }
    }

    // the first screen to open depends on the role: a teacher starts at "mi
    // día", an admin starts at "centro". there is no MENU-equivalent home for
    // admin yet, so we just take its first visible tab.
    val startDestination = remember(role) {
        if (role == Role.ADMIN) AdminCentroGraph else MyDayGraph
    }

    // this value changes every time the user goes to another screen,
    // and compose draws the bottom bar again with the new selected tab.
    val backStackEntry by navController.currentBackStackEntryAsState()

    // we look for the tab whose route matches the screen that is open now.
    val currentDestination = backStackEntry?.destination.toTopLevelDestination()

    // uno solo para toda la app: vive aquí, fuera del NavHost, así que un
    // mensaje sigue en pantalla aunque justo después se navegue a otra ruta
    // (por ejemplo, "Sala creada" después de volver de "Crear sala").
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
            startDestination = startDestination,
            snackbarHostState = snackbarHostState,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
