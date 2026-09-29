package mx.tec.codea

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import mx.tec.codea.ui.screens.roleselection.RoleSelectionScreen

// the root of the whole interface. it joins three parts:
// the scaffold (the frame), the bottom bar (the tabs) and the nav host (the screens).
@Composable
fun CodeaApp(
    modifier: Modifier = Modifier,
) {
    // Temporary replacement for a real login/session.
    var selectedRole by rememberSaveable {
        mutableStateOf<Role?>(null)
    }

    val role = selectedRole

    if (role == null) {
        RoleSelectionScreen(
            onRoleSelected = { selectedRole = it },
            modifier = modifier,
        )
        return
    }

    // We have not created the parent's navigation destinations yet.
    // This branch disappears in the next step when those tabs are added.
    if (role == Role.PARENT) {
        ParentSetupPlaceholder(modifier)
        return
    }

    // A different role receives a completely fresh navigation controller
    // instead of inheriting another role's back stack.
    key(role) {
        RoleApp(
            role = role,
            modifier = modifier,
        )
    }
}

@Composable
private fun RoleApp(
    role: Role,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    val visibleDestinations = remember(role) {
        TopLevelDestination.entries.filter { role in it.roles }
    }

    val startDestination = remember(role) {
        when (role) {
            Role.TEACHER -> MyDayGraph
            Role.ADMIN -> AdminCentroGraph

            // RoleApp is not called for PARENT until its routes exist.
            Role.PARENT -> error("Parent navigation is not configured yet.")
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination =
        backStackEntry?.destination.toTopLevelDestination()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            CodeaBottomBar(
                destinations = visibleDestinations,
                currentDestination = currentDestination,
                onDestinationClick = { destination ->
                    if (destination == currentDestination) {
                        navController.reselectTopLevel(destination)
                    } else {
                        navController.navigateToTopLevel(destination)
                    }
                },
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
    ) { innerPadding ->
        CodeaNavHost(
            navController = navController,
            startDestination = startDestination,
            snackbarHostState = snackbarHostState,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun ParentSetupPlaceholder(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.parent_setup_pending),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}