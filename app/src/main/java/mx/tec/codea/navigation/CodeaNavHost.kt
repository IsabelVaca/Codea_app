package mx.tec.codea.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import mx.tec.codea.ui.state.DocentesAdminViewModel
import mx.tec.codea.ui.state.InfantesAdminViewModel
import mx.tec.codea.ui.state.SalasAdminViewModel
import mx.tec.codea.ui.screens.admin.CentroScreen
import mx.tec.codea.ui.screens.admin.CrearSalaScreen
import mx.tec.codea.ui.screens.admin.DocenteDetalleScreen
import mx.tec.codea.ui.screens.admin.DocenteOpcion
import mx.tec.codea.ui.screens.admin.DocenteScreen
import mx.tec.codea.ui.screens.admin.InfanteDetalleScreen
import mx.tec.codea.ui.screens.admin.InfanteScreen
import mx.tec.codea.ui.screens.admin.InscribirInfanteScreen
import mx.tec.codea.ui.screens.admin.SalaAdminDetalleScreen
import mx.tec.codea.ui.screens.admin.SalaOpcion
import mx.tec.codea.ui.screens.chats.ChatsScreen
import mx.tec.codea.ui.screens.checker.CheckerScreen
import mx.tec.codea.ui.screens.menu.MenuScreen
import mx.tec.codea.ui.screens.myday.DayNotStartedScreen
import mx.tec.codea.ui.screens.myday.MyDayScreen
import mx.tec.codea.ui.screens.myroom.Child
import mx.tec.codea.ui.screens.myroom.MyRoomScreen
import mx.tec.codea.ui.screens.admin.OpcionChip
import mx.tec.codea.ui.screens.admin.AltaAsistenteScreen
import mx.tec.codea.ui.state.CentroViewModel
import mx.tec.codea.ui.state.CrearSalaViewModel
import mx.tec.codea.ui.state.AltaAsistenteViewModel
import mx.tec.codea.ui.state.InscribirInfanteViewModel
import mx.tec.codea.ui.screens.myroom.MyRoomSampleData
import mx.tec.codea.ui.screens.report.method.ReportMethod
import mx.tec.codea.ui.screens.report.method.SelectReportMethodScreen
import mx.tec.codea.ui.screens.report.report.ReportScreen
import mx.tec.codea.ui.screens.report.report.ReportViewModel
import mx.tec.codea.ui.screens.report.students.SelectStudentsScreen
import mx.tec.codea.ui.screens.report.students.SelectStudentsViewModel

// the nav host is like a map of the app: it connects each route to its screen.
// the nav controller moves the user from one screen to another.
@Composable
fun CodeaNavHost(
    navController: NavHostController,
    // which tree of screens opens first. CodeaApp passes MyDayGraph for a
    // teacher and AdminCentroRoute for an admin, the same way AvisosNavHost
    // only exists once there is a session: here there is no session yet, so
    // CodeaApp decides the start screen from the fixed role it was given.
    startDestination: Any = MyDayGraph,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    // compartidos por toda la app.
    val docentesViewModel: DocentesAdminViewModel = viewModel()
    val salasViewModel: SalasAdminViewModel = viewModel()
    val infantesViewModel: InfantesAdminViewModel = viewModel()

    // showSnackbar es suspend, necesita un scope.
    val coroutineScope = rememberCoroutineScope()

    var lastOpenedChatChildId by rememberSaveable {
        mutableStateOf<String?>(null)
    }
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
            composable<MyDayRoute> {
                MyDayScreen(
                    onSwitchClick = { navController.navigate(DayNotStartedRoute) },
                    onMakeReportClick = { subprocess ->
                        navController.navigate(SelectReportMethodRoute(subprocessName = subprocess.name))
                    }
                )
            }
            composable<DayNotStartedRoute> {
                DayNotStartedScreen(
                    onCheckerClick = { navController.navigateToCheckerThroughMenu() },
                    // "mi día" is right below this screen, so going back is enough.
                    onSwitchClick = { navController.popBackStack() },
                )
            }
            composable<SelectReportMethodRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<SelectReportMethodRoute>()
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry<MyDayGraph>() }
                val reportViewModel: ReportViewModel = viewModel(parentEntry)
                val context = LocalContext.current

                val takePictureLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.TakePicturePreview()
                ) { bitmap: Bitmap? ->
                    if (bitmap != null) {
                        reportViewModel.setReportMethod(ReportMethod.PHOTO)
                        reportViewModel.setCapturedPhoto(bitmap)
                        navController.navigate(ReportRoute(subprocessName = route.subprocessName))
                    }
                }

                val cameraPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        takePictureLauncher.launch(null)
                    } else {
                        Toast.makeText(context, "Se requiere permiso de cámara para tomar la foto", Toast.LENGTH_SHORT).show()
                    }
                }

                fun launchCameraDirectly() {
                    val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                        takePictureLauncher.launch(null)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }

                SelectReportMethodScreen(
                    subprocessName = route.subprocessName,
                    onMethodSelected = { method ->
                        if (method == ReportMethod.PHOTO) {
                            launchCameraDirectly()
                        } else {
                            reportViewModel.setReportMethod(method)
                            reportViewModel.setCapturedPhoto(null)
                            navController.navigate(ReportRoute(subprocessName = route.subprocessName))
                        }
                    },
                    onBackClicked = {
                        navController.popBackStack()
                    }
                )
            }
            composable<ReportRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<ReportRoute>()
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry<MyDayGraph>() }
                val reportViewModel: ReportViewModel = viewModel(parentEntry)
                val studentsViewModel: SelectStudentsViewModel = viewModel(parentEntry)

                ReportScreen(
                    subprocessName = route.subprocessName,
                    viewModel = reportViewModel,
                    onBackClicked = {
                        navController.popBackStack()
                    },
                    onChangePhotoClicked = {
                        // Cambiar foto manejado internamente
                    },
                    onContinueSuccess = {
                        val reportTypeLabel = reportViewModel.uiState.value.selectedReportType.label
                        studentsViewModel.setReportTypeLabel(reportTypeLabel)
                        navController.navigate(SelectStudentsRoute(reportTypeLabel = reportTypeLabel))
                    }
                )
            }
            composable<SelectStudentsRoute> { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry<MyDayGraph>() }
                val reportViewModel: ReportViewModel = viewModel(parentEntry)
                val studentsViewModel: SelectStudentsViewModel = viewModel(parentEntry)
                val context = LocalContext.current

                SelectStudentsScreen(
                    viewModel = studentsViewModel,
                    onBackClicked = {
                        navController.popBackStack()
                    },
                    onSaveSuccess = {
                        val count = studentsViewModel.uiState.value.selectedCount
                        val text = "Reporte guardado exitosamente para $count ${if (count == 1) "niño" else "niños"}"
                        Toast.makeText(context, text, Toast.LENGTH_LONG).show()

                        reportViewModel.resetState()
                        studentsViewModel.resetState()

                        navController.popBackStack(MyDayRoute, inclusive = false)
                    }
                )
            }
        }

        composable<MyRoomRoute> {
            MyRoomScreen(
                onChatClick = { child ->
                    lastOpenedChatChildId = child.id
                    navController.navigateToTopLevel(TopLevelDestination.CHATS)
                },
            )
        }

        composable<ChatsRoute> {
            val selectedChild = MyRoomSampleData.children.firstOrNull { child ->
                child.id == lastOpenedChatChildId
            }

            ChatsScreen(
                selectedChild = selectedChild,
                onBackClick = {
                    navController.navigateToTopLevel(TopLevelDestination.MY_ROOM)
                },
            )
        }

        navigation<MenuGraph>(startDestination = MenuRoute()) {
            composable<MenuRoute> { backStackEntry ->
                // toRoute reads the values we sent with the route (here, openChecker).
                val route = backStackEntry.toRoute<MenuRoute>()
                MenuScreen(
                    openChecker = route.openChecker,
                    onCheckerClick = { navController.navigate(CheckerRoute) },
                )
            }
            composable<CheckerRoute> {
                CheckerScreen()
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
    }
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
