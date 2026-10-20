package mx.tec.codea.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mx.tec.codea.CodeaApplication
import mx.tec.codea.data.DataEvents
import mx.tec.codea.data.SessionRepository
import mx.tec.codea.data.SessionState
import mx.tec.codea.data.api.ApiConfig
import mx.tec.codea.domain.InviteLink
import mx.tec.codea.domain.Session
import mx.tec.codea.ui.components.CodeaBottomBar
import mx.tec.codea.ui.screens.auth.ActivateAccountScreen
import mx.tec.codea.ui.screens.auth.LoginScreen
import mx.tec.codea.ui.screens.auth.SplashScreen
import mx.tec.codea.ui.screens.roleselection.RoleSelectionScreen
import mx.tec.codea.ui.state.CodeaViewModel
import mx.tec.codea.ui.state.SessionViewModel

// the root of the whole interface. what it draws depends on the session (the login is not a route:
// closing the session just changes the state, so there is nothing to navigate):
//   Loading   -> the splash, while the saved session is read from the phone
//   Anonymous -> login / activate account
//   Active    -> the app of the role (the scaffold, the bottom bar and the nav host)
@Composable
fun CodeaApp(
    modifier: Modifier = Modifier,
    // el enlace de invitación con el que se abrió la app: precarga "Activar mi cuenta". null si no se abrió con uno.
    inviteLink: InviteLink? = null,
    onInviteUsed: () -> Unit = {},
) {
    val container = (LocalContext.current.applicationContext as CodeaApplication).container
    val sessionViewModel: SessionViewModel = viewModel(factory = SessionViewModel.Factory(container.sessionRepository))
    val state by sessionViewModel.state.collectAsState()
    val form by sessionViewModel.form.collectAsState()

    // lo que la persona anterior tenía en pantalla no debe verlo la siguiente: al cerrar sesión se borra todo.
    val sessionScopes: SessionScopesViewModel = viewModel()
    LaunchedEffect(state) {
        if (state is SessionState.Anonymous) sessionScopes.clearAll()
    }

    when (val current = state) {
        SessionState.Loading -> SplashScreen(modifier)

        SessionState.Anonymous -> {
            var activating by rememberSaveable { mutableStateOf(false) }
            // con un enlace de invitación se salta el login y se llega a "Activar mi cuenta" con todo puesto.
            // el enlace se gasta en cuanto se lee: lo que trae se guarda aquí, para que sobreviva a girar la pantalla
            // y para que, ya con la sesión abierta, no quede un enlace pendiente.
            var prefillEmail by rememberSaveable { mutableStateOf("") }
            var prefillCode by rememberSaveable { mutableStateOf("") }
            LaunchedEffect(inviteLink) {
                if (inviteLink != null) {
                    prefillEmail = inviteLink.email
                    prefillCode = inviteLink.code
                    sessionViewModel.clearError()
                    activating = true
                    onInviteUsed()
                }
            }
            if (activating) {
                ActivateAccountScreen(
                    form = form,
                    initialEmail = prefillEmail,
                    initialCode = prefillCode,
                    onActivate = sessionViewModel::activate,
                    onGoToLogin = {
                        sessionViewModel.clearError()
                        activating = false
                    },
                    modifier = modifier,
                )
            } else {
                LoginScreen(
                    form = form,
                    onLogin = sessionViewModel::login,
                    onGoToActivate = {
                        sessionViewModel.clearError()
                        activating = true
                    },
                    modifier = modifier,
                )
            }
        }

        is SessionState.Active -> {
            // con la sesión abierta un enlace de invitación no sirve: se descarta y se avisa.
            LaunchedEffect(inviteLink) {
                if (inviteLink != null) {
                    DataEvents.notice("Ya tienes una sesión abierta. Cierra sesión para activar otra cuenta.")
                    onInviteUsed()
                }
            }
            // cada sesión tiene su propio almacén de ViewModels: sobrevive a girar la pantalla y se borra al salir.
            val owner = sessionScopes.ownerFor(current.session.userId)
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                SignedIn(
                    session = current.session,
                    sessions = container.sessionRepository,
                    onLogout = sessionViewModel::logout,
                    modifier = modifier,
                )
            }
        }
    }
}

// guarda un almacén de ViewModels por sesión. vive en la actividad (sobrevive a girar la pantalla) pero cada
// almacén se borra al cerrar sesión, así el estado de una persona nunca llega a la siguiente.
class SessionScopesViewModel : ViewModel() {
    private val stores = mutableMapOf<String, ViewModelStore>()

    fun ownerFor(userId: String): ViewModelStoreOwner {
        val store = stores.getOrPut(userId) { ViewModelStore() }
        return object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = store
        }
    }

    fun clearAll() {
        stores.values.forEach { it.clear() }
        stores.clear()
    }

    override fun onCleared() = clearAll()
}

// la app de quien ya entró. el rol sale de la sesión: con uno solo se entra directo, y con varios se elige.
@Composable
private fun SignedIn(
    session: Session,
    sessions: SessionRepository,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val roles = Role.entries.filter { it.name in session.roles }
    var chosen by rememberSaveable { mutableStateOf<String?>(null) }
    val role = when {
        roles.size == 1 -> roles.first()
        else -> roles.firstOrNull { it.name == chosen }
    }

    // los ViewModels piden datos solo si el rol lo permite; se apagan al salir.
    ApiConfig.userKey = session.userId
    ApiConfig.isAdmin = role == Role.ADMIN
    ApiConfig.isTeacher = role == Role.TEACHER
    ApiConfig.isParent = role == Role.PARENT
    DisposableEffect(Unit) {
        onDispose {
            ApiConfig.userKey = null
            ApiConfig.isAdmin = false
            ApiConfig.isTeacher = false
            ApiConfig.isParent = false
        }
    }
    // al elegir rol o entrar con otra persona, las listas se vuelven a pedir.
    LaunchedEffect(role, session.userId) { DataEvents.notifyChanged() }

    if (role == null) {
        RoleSelectionScreen(
            roles = roles,
            onRoleSelected = { chosen = it.name },
            onLogout = onLogout,
            modifier = modifier,
        )
        return
    }

    // CodeaViewModel is the shared view model of the teacher. it lives in the store of this session,
    // so every tab sees the same object and a new session starts with a new one.
    val viewModel: CodeaViewModel = viewModel()

    // a different role receives a completely fresh navigation controller
    // instead of inheriting another role's back stack.
    key(role) {
        RoleApp(role = role, viewModel = viewModel, sessions = sessions, onLogout = onLogout, modifier = modifier)
    }
}

@Composable
private fun RoleApp(
    role: Role,
    viewModel: CodeaViewModel,
    sessions: SessionRepository,
    onLogout: () -> Unit,
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
            Role.ADMIN -> AdminAvisosGraph
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
            sessions = sessions,
            onLogout = onLogout,
            // imePadding al final: con el teclado abierto todas las pantallas suben y el campo que se escribe no queda
            // tapado. los insets de las barras ya se consumieron arriba, así que no se cuentan dos veces.
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding(),
        )
    }
}
