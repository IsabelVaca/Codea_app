package mx.tec.codea.ui.navigation

import android.widget.Toast
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import kotlinx.coroutines.launch
import mx.tec.codea.R
import mx.tec.codea.domain.ReportMethod
import mx.tec.codea.ui.components.rememberDictation
import mx.tec.codea.ui.components.rememberTakePhoto
import mx.tec.codea.ui.screens.ChatsScreen
import mx.tec.codea.ui.screens.CheckInDoneScreen
import mx.tec.codea.ui.screens.CheckerScreen
import mx.tec.codea.ui.screens.ComingSoonScreen
import mx.tec.codea.ui.screens.DayNotStartedScreen
import mx.tec.codea.ui.screens.MenuOption
import mx.tec.codea.ui.screens.MenuScreen
import mx.tec.codea.ui.screens.MyDayScreen
import mx.tec.codea.ui.screens.MyRoomScreen
import mx.tec.codea.ui.screens.ReportScreen
import mx.tec.codea.ui.screens.SelectReportMethodScreen
import mx.tec.codea.ui.screens.SelectStudentsScreen
import mx.tec.codea.ui.screens.admin.AltaAsistenteScreen
import mx.tec.codea.ui.screens.admin.CentroScreen
import mx.tec.codea.ui.screens.admin.CrearSalaScreen
import mx.tec.codea.ui.screens.admin.DocenteDetalleScreen
import mx.tec.codea.ui.screens.admin.DocenteOpcion
import mx.tec.codea.ui.screens.admin.DocenteScreen
import mx.tec.codea.ui.screens.admin.InfanteDetalleScreen
import mx.tec.codea.ui.screens.admin.InfanteScreen
import mx.tec.codea.ui.screens.admin.InscribirInfanteScreen
import mx.tec.codea.ui.screens.admin.OpcionChip
import mx.tec.codea.ui.screens.admin.SalaAdminDetalleScreen
import mx.tec.codea.ui.screens.admin.SalaOpcion
import mx.tec.codea.ui.screens.parent.calendar.ParentCalendarScreen
import mx.tec.codea.ui.screens.parent.documents.ParentDocumentsScreen
import mx.tec.codea.ui.screens.parent.photos.ParentPhotosScreen
import mx.tec.codea.ui.screens.parent.today.ParentTodayScreen
import mx.tec.codea.ui.state.AltaAsistenteViewModel
import mx.tec.codea.ui.state.CentroViewModel
import mx.tec.codea.ui.state.CheckerViewModel
import mx.tec.codea.ui.state.CodeaViewModel
import mx.tec.codea.ui.state.CrearSalaViewModel
import mx.tec.codea.ui.state.DocentesAdminViewModel
import mx.tec.codea.ui.state.InfantesAdminViewModel
import mx.tec.codea.ui.state.InscribirInfanteViewModel
import mx.tec.codea.ui.state.ReportFormViewModel
import mx.tec.codea.ui.state.SalasAdminViewModel

// the nav host is like a map of the app: it connects each route to its screen.
// here we also connect each screen to its state: the screens are "dumb",
// so this file reads the view models and passes plain data and lambdas.
@Composable
fun CodeaNavHost(
    navController: NavHostController,
    viewModel: CodeaViewModel,
    // which tree of screens opens first. CodeaApp decides it from the role:
    // MyDayGraph for a teacher, AdminCentroGraph for an admin, ParentTodayRoute for a parent.
    startDestination: Any,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    // shared by all the admin screens.
    val docentesViewModel: DocentesAdminViewModel = viewModel()
    val salasViewModel: SalasAdminViewModel = viewModel()
    val infantesViewModel: InfantesAdminViewModel = viewModel()

    // showSnackbar is a suspend function, so it needs a scope.
    val coroutineScope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        // by default the nav host changes screens with a fade.
        // we use a slide instead, so the screens move like the tabs in the bar.
        // the "pop" versions run when the user goes back.
        enterTransition = { slideIntoContainer(tabSlideDirection(isGoingBack = false)) },
        exitTransition = { slideOutOfContainer(tabSlideDirection(isGoingBack = false)) },
        popEnterTransition = { slideIntoContainer(tabSlideDirection(isGoingBack = true)) },
        popExitTransition = { slideOutOfContainer(tabSlideDirection(isGoingBack = true)) },
    ) {
        // "navigation" creates a small graph inside the big one: all its screens
        // belong to the same tab, so the bottom bar keeps that tab selected.
        navigation<MyDayGraph>(startDestination = MyDayRoute) {
            // the same route shows "aún el día no comienza" before the check-in and
            // the routine after it. with only one route, the back button, the tab and
            // the report flow always come back to the right screen.
            composable<MyDayRoute> {
                if (viewModel.hasCheckedIn) {
                    MyDayScreen(
                        routine = viewModel.routine,
                        selectedSubprocessNumber = viewModel.selectedSubprocessNumber,
                        reportCountOf = viewModel::reportCountOf,
                        onSubprocessClick = viewModel::selectSubprocess,
                        onMakeReportClick = { number ->
                            navController.navigate(SelectReportMethodRoute(subprocessNumber = number))
                        },
                        onConfirmSubprocessClick = viewModel::confirmSubprocess,
                    )
                } else {
                    DayNotStartedScreen(
                        teacherName = viewModel.teacher.greetingName,
                        onCheckerClick = { navController.navigateToCheckerThroughMenu() },
                    )
                }
            }

            // the report flow has three screens but one form. the owner of the form
            // view model is the first screen of the flow: while the flow is open it stays
            // in the stack, and when the flow closes, the form is cleared by itself.
            composable<SelectReportMethodRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<SelectReportMethodRoute>()
                val subprocess = viewModel.subprocessByNumber(route.subprocessNumber) ?: return@composable
                val form: ReportFormViewModel = viewModel(backStackEntry)
                val openReport = { navController.navigate(ReportRoute(route.subprocessNumber)) }
                val takePhoto = rememberTakePhoto { photo ->
                    form.onPhotoTaken(photo)
                    openReport()
                }

                SelectReportMethodScreen(
                    subprocess = subprocess,
                    onMethodSelected = { method ->
                        // with a photo, the camera opens first and the form comes after.
                        if (method == ReportMethod.PHOTO) {
                            takePhoto()
                        } else {
                            form.onMethodChosen(method)
                            openReport()
                        }
                    },
                    onBackClick = { navController.popBackStack() },
                )
            }
            composable<ReportRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<ReportRoute>()
                val subprocess = viewModel.subprocessByNumber(route.subprocessNumber) ?: return@composable
                val form = reportForm(navController, backStackEntry)
                val takePhoto = rememberTakePhoto(onPhotoTaken = form::onPhotoTaken)
                val dictate = rememberDictation(onTextHeard = form::onDictationHeard)

                ReportScreen(
                    subprocess = subprocess,
                    uiState = form.uiState,
                    onTypeChange = form::onTypeChange,
                    onDescriptionChange = form::onDescriptionChange,
                    onDictateClick = dictate,
                    onTakePhotoClick = takePhoto,
                    onContinueClick = { navController.navigate(SelectStudentsRoute(route.subprocessNumber)) },
                    onBackClick = { navController.popBackStack() },
                )
            }
            composable<SelectStudentsRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<SelectStudentsRoute>()
                val form = reportForm(navController, backStackEntry)
                val context = LocalContext.current
                val count = form.uiState.selectedChildIds.size
                val savedMessage = pluralStringResource(R.plurals.report_saved, count, count)

                SelectStudentsScreen(
                    uiState = form.uiState,
                    children = viewModel.children,
                    onChildToggle = form::onChildToggle,
                    onSaveClick = {
                        // the shared view model receives the report...
                        viewModel.addReport(form.toReport(route.subprocessNumber))
                        Toast.makeText(context, savedMessage, Toast.LENGTH_LONG).show()
                        // ...and the navigation is decided here, by the ui.
                        navController.popBackStack(MyDayRoute, inclusive = false)
                    },
                    onBackClick = { navController.popBackStack() },
                )
            }
        }

        composable<MyRoomRoute> {
            MyRoomScreen(
                children = viewModel.children,
                onAttendanceChange = viewModel::setAttendance,
                onChatClick = { childId ->
                    viewModel.openChat(childId)
                    navController.navigateToTopLevel(TopLevelDestination.CHATS)
                },
            )
        }

        composable<ChatsRoute> {
            val child = viewModel.childById(viewModel.openChatChildId)
            ChatsScreen(
                selectedChild = child,
                messages = child?.let { viewModel.messagesFor(it.id) }.orEmpty(),
                onBackClick = { navController.navigateToTopLevel(TopLevelDestination.MY_ROOM) },
            )
        }

        navigation<MenuGraph>(startDestination = MenuRoute()) {
            composable<MenuRoute> { backStackEntry ->
                // toRoute reads the values we sent with the route (here, openChecker).
                val route = backStackEntry.toRoute<MenuRoute>()
                MenuScreen(
                    teacher = viewModel.teacher,
                    checkIn = viewModel.checkIn,
                    openChecker = route.openChecker,
                    onCheckerClick = { navController.navigate(CheckerRoute) },
                    onOptionClick = { option -> navController.navigate(MenuOptionRoute(option.name)) },
                    onLogoutConfirm = {
                        viewModel.resetDay()
                        navController.navigateToMyDayAfterLogout()
                    },
                )
            }
            composable<CheckerRoute> {
                // this view model lives inside the destination: its owner is this screen,
                // so the chosen case and the late reason are cleared when the teacher leaves.
                val checker: CheckerViewModel = viewModel()
                CheckerScreen(
                    uiState = checker.uiState,
                    registeredCheckIn = viewModel.checkIn,
                    // the menu is right below the check-in, so going back is enough.
                    onBackClick = { navController.popBackStack() },
                    onScenarioChange = checker::onScenarioChange,
                    onCheckInClick = {
                        // the domain can say "no" (out of range): then nothing happens.
                        val record = checker.buildRecord()
                        if (record != null) {
                            viewModel.registerCheckIn(record)
                            navController.navigateToCheckInDone()
                        }
                    },
                    onRetryClick = checker::onRetry,
                    onNotifyCoordinationClick = checker::onNotifyCoordination,
                    onWriteLateReasonClick = checker::onWriteLateReasonClick,
                    onLateReasonChange = checker::onLateReasonChange,
                )
            }
            composable<CheckInDoneRoute> {
                val record = viewModel.checkIn ?: return@composable
                CheckInDoneScreen(
                    record = record,
                    teacherName = viewModel.teacher.greetingName,
                    onGoToMyDayClick = { navController.navigateToMyDayAfterCheckIn() },
                )
            }
            composable<MenuOptionRoute> { backStackEntry ->
                val option = MenuOption.valueOf(backStackEntry.toRoute<MenuOptionRoute>().optionName)
                ComingSoonScreen(
                    title = stringResource(option.titleRes),
                    onBackClick = { navController.popBackStack() },
                )
            }
        }

        // the "centro" tab. it is a graph, like MyDayGraph: "centro" is the
        // first screen and "crear sala" opens on top of it, both keeping the
        // bottom bar on the "centro" tab.
        navigation<AdminCentroGraph>(startDestination = AdminCentroRoute) {
            composable<AdminCentroRoute> {
                val centroViewModel: CentroViewModel = viewModel()
                val salas by salasViewModel.salas.collectAsState()
                val infantesCentro by infantesViewModel.infantes.collectAsState()
                CentroScreen(
                    salas = salas,
                    infantes = infantesCentro,
                    onSalaClick = { sala ->
                        navController.navigate(AdminSalaDetalleRoute(sala.id))
                    },
                    avisoTexto = centroViewModel.avisoTexto,
                    onAvisoTextoChange = centroViewModel::onAvisoTextoChange,
                    canPublicarAviso = centroViewModel.canPublicar,
                    onPublicarAviso = {
                        centroViewModel.publicarAviso()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Aviso publicado")
                        }
                    },
                    onCrearSala = { navController.navigate(AdminCrearSalaRoute) }
                )
            }

            composable<AdminSalaDetalleRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<AdminSalaDetalleRoute>()
                val salas by salasViewModel.salas.collectAsState()
                val sala = salas.firstOrNull { it.id == route.salaId } ?: return@composable
                val infantes by infantesViewModel.infantes.collectAsState()
                val docentes by docentesViewModel.docentes.collectAsState()
                val ninos = infantes.filter { it.sala == sala.nombre }
                SalaAdminDetalleScreen(
                    sala = sala,
                    ninos = ninos,
                    onQuitarInfante = { infante ->
                        infantesViewModel.cambiarSala(infanteId = infante.id, nuevaSala = "")
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("\"${infante.nombre}\" salió de ${sala.nombre}")
                        }
                    },
                    infantesDisponibles = infantes.filter { it.sala != sala.nombre },
                    onAgregarInfante = { infante ->
                        infantesViewModel.cambiarSala(infanteId = infante.id, nuevaSala = sala.nombre)
                    },
                    asistentes = docentes.map { it.nombre },
                    onAsistenteSeleccionada = { nombre ->
                        salasViewModel.asignarAsistente(salaId = sala.id, asistente = nombre)
                    },
                    onEditarCupo = { nuevoCupo ->
                        salasViewModel.editarCupo(salaId = sala.id, nuevoCupoMaximo = nuevoCupo.toInt())
                    },
                    onEliminar = {
                        salasViewModel.eliminarSala(sala.id)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Sala \"${sala.nombre}\" eliminada")
                        }
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<AdminCrearSalaRoute> {
                val crearSalaViewModel: CrearSalaViewModel = viewModel()
                val uiState = crearSalaViewModel.uiState
                val docentes by docentesViewModel.docentes.collectAsState()
                val asistentes = remember(docentes, uiState.asistenteElegida) {
                    docentes.map { docente ->
                        DocenteOpcion(nombre = docente.nombre, activo = docente.nombre == uiState.asistenteElegida)
                    }
                }
                CrearSalaScreen(
                    nombre = uiState.nombre,
                    onNombreChange = crearSalaViewModel::onNombreChange,
                    nombreError = uiState.nombreError,
                    cupo = uiState.cupo,
                    onCupoChange = crearSalaViewModel::onCupoChange,
                    cupoError = uiState.cupoError,
                    asistentes = asistentes,
                    onElegirAsistente = crearSalaViewModel::onElegirAsistente,
                    canGuardar = uiState.canGuardar,
                    onGuardar = {
                        salasViewModel.crearSala(
                            nombre = uiState.nombre,
                            cupoMaximo = uiState.cupo.toInt(),
                            asistente = uiState.asistenteElegida
                        )
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Sala \"${uiState.nombre}\" creada")
                        }
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() },
                )
            }
        }
        navigation<AdminDocentesGraph>(startDestination= AdminDocentesRoute) {
            composable<AdminDocentesRoute>{
                val docentes by docentesViewModel.docentes.collectAsState()
                val infantes by infantesViewModel.infantes.collectAsState()
                val salas by salasViewModel.salas.collectAsState()
                DocenteScreen(
                    docentes = docentes,
                    infantes = infantes,
                    totalSalas = salas.size,
                    onDocenteClick = { docente ->
                        navController.navigate(AdminDocenteDetalleRoute(docente.id))
                    },
                    onDarDeAltaAsistente = {navController.navigate(AdminAltaAsistenteRoute)}
                )
            }
            composable<AdminDocenteDetalleRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<AdminDocenteDetalleRoute>()
                val docentes by docentesViewModel.docentes.collectAsState()
                val docente = docentes.firstOrNull { it.id == route.docenteId }
                    ?: return@composable
                val salasDisponibles by salasViewModel.salas.collectAsState()
                DocenteDetalleScreen(
                    docente = docente,
                    salas = salasDisponibles.map { it.nombre },
                    onSalaSeleccionada = { nuevaSala ->
                        docentesViewModel.cambiarSala(docenteId = docente.id, nuevaSala = nuevaSala)
                    },
                    onDarDeBaja = { docentesViewModel.darDeBaja(docente.id) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable<AdminAltaAsistenteRoute> {
                val altaAsistenteViewModel: AltaAsistenteViewModel = viewModel()
                val uiState = altaAsistenteViewModel.uiState
                val salasDisponibles by salasViewModel.salas.collectAsState()
                val salas = remember(salasDisponibles, uiState.salaElegida) {
                    salasDisponibles.map { OpcionChip(nombre = it.nombre, activo = it.nombre == uiState.salaElegida) }
                }
                AltaAsistenteScreen(
                    nombre = uiState.nombre, onNombreChange = altaAsistenteViewModel::onNombreChange,
                    nombreError = uiState.nombreError,
                    telefono = uiState.telefono, onTelefonoChange = altaAsistenteViewModel::onTelefonoChange,
                    telefonoError = uiState.telefonoError,
                    correo = uiState.correo, onCorreoChange = altaAsistenteViewModel::onCorreoChange,
                    correoError = uiState.correoError,
                    salas = salas,
                    onElegirSala = altaAsistenteViewModel::onElegirSala,
                    turno = uiState.turno, onElegirTurno = altaAsistenteViewModel::onElegirTurno,
                    canGuardar = uiState.canGuardar,
                    onGuardar = {
                        docentesViewModel.altaDocente(nombre = uiState.nombre, sala = uiState.salaElegida ?: "")
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Asistente \"${uiState.nombre}\" dada de alta")
                        }
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() },
                )
            }
        }



        // the "infantes" tab. same shape as "centro": a graph with the list
        // screen first and "inscribir infante" opening on top of it.
        navigation<AdminInfantesGraph>(startDestination = AdminInfantesRoute) {
            composable<AdminInfantesRoute> {
                val infantes by infantesViewModel.infantes.collectAsState()
                val salas by salasViewModel.salas.collectAsState()
                InfanteScreen(
                    infantes = infantes,
                    totalSalas = salas.size,
                    onInfanteClick = { infante ->
                        navController.navigate(AdminInfanteDetalleRoute(infante.id))
                    },
                    onInscribirInfante = { navController.navigate(AdminInscribirInfanteRoute) },
                )
            }
            composable<AdminInfanteDetalleRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<AdminInfanteDetalleRoute>()
                val infantes by infantesViewModel.infantes.collectAsState()
                val infante = infantes.firstOrNull { it.id == route.infanteId }
                    ?: return@composable
                val salasDisponibles by salasViewModel.salas.collectAsState()
                InfanteDetalleScreen(
                    infante = infante,
                    salas = salasDisponibles.map { it.nombre },
                    onSalaSeleccionada = { nuevaSala ->
                        infantesViewModel.cambiarSala(infanteId = infante.id, nuevaSala = nuevaSala)
                    },
                    onDarBaja = { infantesViewModel.darDeBaja(infante.id) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable<AdminInscribirInfanteRoute> {
                val inscribirInfanteViewModel: InscribirInfanteViewModel = viewModel()
                val uiState = inscribirInfanteViewModel.uiState
                val salasDisponibles by salasViewModel.salas.collectAsState()
                val salas = remember(salasDisponibles, uiState.salaElegida) {
                    salasDisponibles.map { sala ->
                        SalaOpcion(nombre = sala.nombre, activo = sala.nombre == uiState.salaElegida)
                    }
                }
                InscribirInfanteScreen(
                    nombre = uiState.nombre,
                    onNombreChange = inscribirInfanteViewModel::onNombreChange,
                    nombreError = uiState.nombreError,
                    edad = uiState.edad,
                    onEdadChange = inscribirInfanteViewModel::onEdadChange,
                    edadError = uiState.edadError,
                    alergias = uiState.alergias,
                    onAlergiasChange = inscribirInfanteViewModel::onAlergiasChange,
                    tutor = uiState.tutor,
                    onTutorChange = inscribirInfanteViewModel::onTutorChange,
                    tutorError = uiState.tutorError,
                    tituloTutor = uiState.tituloTutor,
                    onElegirTituloTutor = inscribirInfanteViewModel::onTituloTutorChange,
                    telefono = uiState.telefono,
                    onTelefonoChange = inscribirInfanteViewModel::onTelefonoChange,
                    telefonoError = uiState.telefonoError,
                    salas = salas,
                    onElegirSala = inscribirInfanteViewModel::onElegirSala,
                    canGuardar = uiState.canGuardar,
                    onGuardar = {
                        infantesViewModel.inscribirInfante(
                            nombre = uiState.nombre,
                            edad = uiState.edad.toInt(),
                            tutor = uiState.tutor,
                            tituloTutor = uiState.tituloTutor,
                            sala = uiState.salaElegida ?: ""
                        )
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("\"${uiState.nombre}\" inscrito")
                        }
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() },
                )
            }
        }

        // parent tabs. each is only one screen for now.
        composable<ParentTodayRoute> {
            val context = LocalContext.current
            ParentTodayScreen(
                onNavigateToCalendar = {
                    navController.navigateToTopLevel(TopLevelDestination.PARENT_CALENDAR)
                },
                onSendNote = { _ ->
                    Toast.makeText(context, "Nota enviada a la asistente", Toast.LENGTH_SHORT).show()
                },
            )
        }

        composable<ParentCalendarRoute> {
            ParentCalendarScreen(
                onDaySelected = { _ ->
                    navController.navigateToTopLevel(TopLevelDestination.PARENT_TODAY)
                },
            )
        }

        composable<ParentPhotosRoute> {
            ParentPhotosScreen()
        }

        composable<ParentDocumentsRoute> {
            ParentDocumentsScreen()
        }
    }
}

// the second and third screens of the report ask for the form of the first one,
// so the three screens share the same object.
@Composable
private fun reportForm(navController: NavHostController, backStackEntry: NavBackStackEntry): ReportFormViewModel {
    val flowEntry = remember(backStackEntry) { navController.getBackStackEntry<SelectReportMethodRoute>() }
    return viewModel(flowEntry)
}

// decides which way the screens move. if the new tab is to the right of the old one
// (for example "mi día" to "chats"), the screens move to the left, and the other way around.
// this way the movement always matches the position of the tabs in the bottom bar.
// inside the same tab (for example "menú" to "checador"), opening a screen moves
// to the left and going back moves to the right, like turning pages.
private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabSlideDirection(
    isGoingBack: Boolean,
): SlideDirection {
    // "initialState" is the screen we leave, "targetState" is the screen we open.
    val from = initialState.destination.toTopLevelDestination()?.ordinal ?: 0
    val to = targetState.destination.toTopLevelDestination()?.ordinal ?: 0
    return when {
        to > from -> SlideDirection.Left
        to < from -> SlideDirection.Right
        isGoingBack -> SlideDirection.Right
        else -> SlideDirection.Left
    }
}
