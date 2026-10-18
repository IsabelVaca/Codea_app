package mx.tec.codea.ui.navigation

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import kotlinx.coroutines.launch
import mx.tec.codea.R
import mx.tec.codea.data.AccessCodeNotice
import mx.tec.codea.data.SessionRepository
import mx.tec.codea.ui.components.AccessCodeDialog
import mx.tec.codea.ui.screens.account.AccountScreen
import mx.tec.codea.data.DataEvents
import mx.tec.codea.domain.ReportMethod
import mx.tec.codea.ui.components.rememberDictation
import mx.tec.codea.ui.components.rememberTakePhoto
import mx.tec.codea.ui.screens.ChatsScreen
import mx.tec.codea.ui.screens.CheckInDoneScreen
import mx.tec.codea.ui.screens.CheckerScreen
import mx.tec.codea.ui.screens.ComingSoonScreen
import mx.tec.codea.ui.screens.DayNotStartedScreen
import mx.tec.codea.ui.screens.MenuOption
import mx.tec.codea.ui.screens.AvisosFundacionScreen
import mx.tec.codea.ui.screens.MenuScreen
import mx.tec.codea.ui.screens.MyDayScreen
import mx.tec.codea.ui.screens.RutinaDeHoyScreen
import mx.tec.codea.ui.screens.MyRoomScreen
import mx.tec.codea.ui.screens.ReportScreen
import mx.tec.codea.ui.screens.SelectReportMethodScreen
import mx.tec.codea.ui.screens.SelectStudentsScreen
import mx.tec.codea.ui.screens.admin.AltaAsistenteScreen
import mx.tec.codea.ui.screens.ResumenDelDiaScreen
import mx.tec.codea.ui.screens.admin.CentroScreen
import mx.tec.codea.ui.screens.admin.SolicitudesBorradoScreen
import mx.tec.codea.ui.screens.admin.CrearSalaScreen
import mx.tec.codea.ui.screens.admin.DocenteDetalleScreen
import mx.tec.codea.ui.screens.admin.DocenteOpcion
import mx.tec.codea.ui.screens.admin.DocenteScreen
import mx.tec.codea.ui.screens.admin.InfanteDetalleScreen
import mx.tec.codea.ui.screens.admin.FamiliasScreen
import mx.tec.codea.ui.screens.admin.AjustesScreen
import mx.tec.codea.ui.screens.admin.InscribirInfanteScreen
import mx.tec.codea.ui.screens.admin.OpcionChip
import mx.tec.codea.ui.screens.admin.InscribirPadreScreen
import mx.tec.codea.ui.screens.admin.PadreDetalleScreen
import mx.tec.codea.ui.screens.admin.AgregarMomentoScreen
import mx.tec.codea.ui.screens.admin.CatalogoMomentosScreen
import mx.tec.codea.ui.screens.admin.NuevaRutinaScreen
import mx.tec.codea.ui.screens.admin.OrdenRutinaScreen
import mx.tec.codea.ui.screens.admin.RutinaDetalleScreen
import mx.tec.codea.ui.screens.admin.RutinasAdminScreen
import mx.tec.codea.ui.screens.admin.SalaAdminDetalleScreen
import mx.tec.codea.ui.screens.admin.SalaOpcion
import mx.tec.codea.ui.screens.parent.calendar.ParentCalendarScreen
import mx.tec.codea.ui.screens.parent.documents.AddAuthorizedDialog
import mx.tec.codea.ui.screens.parent.documents.ParentDocumentsScreen
import mx.tec.codea.ui.screens.parent.photos.ParentPhotosScreen
import mx.tec.codea.ui.screens.parent.today.ParentTodayScreen
import mx.tec.codea.ui.state.AgregarMomentoViewModel
import mx.tec.codea.ui.state.AltaAsistenteViewModel
import mx.tec.codea.ui.state.AvisosViewModel
import mx.tec.codea.ui.state.ChatViewModel
import mx.tec.codea.ui.state.CentroViewModel
import mx.tec.codea.ui.state.SolicitudesBorradoViewModel
import mx.tec.codea.ui.state.CheckerConfig
import mx.tec.codea.ui.state.CheckerViewModel
import mx.tec.codea.ui.state.ResumenDelDiaViewModel
import mx.tec.codea.ui.state.AccountViewModel
import mx.tec.codea.ui.state.CodeaViewModel
import mx.tec.codea.ui.state.CrearSalaViewModel
import mx.tec.codea.ui.state.DocentesAdminViewModel
import mx.tec.codea.ui.state.InfantesAdminViewModel
import mx.tec.codea.ui.state.InscribirInfanteViewModel
import mx.tec.codea.ui.state.InscribirPadreViewModel
import mx.tec.codea.ui.state.NuevaRutinaViewModel
import mx.tec.codea.ui.state.PadresAdminViewModel
import mx.tec.codea.ui.state.ParentViewModel
import mx.tec.codea.ui.state.ReportFormViewModel
import mx.tec.codea.ui.state.RutinasAdminViewModel
import mx.tec.codea.ui.state.SalasAdminViewModel

// the nav host is like a map of the app: it connects each route to its screen.
// here we also connect each screen to its state: the screens are "dumb",
// so this file reads the view models and passes plain data and lambdas.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeaNavHost(
    navController: NavHostController,
    viewModel: CodeaViewModel,
    // which tree of screens opens first. CodeaApp decides it from the role:
    // MyDayGraph for a teacher, AdminCentroGraph for an admin, ParentTodayRoute for a parent.
    startDestination: Any,
    snackbarHostState: SnackbarHostState,
    // la sesión: Mi cuenta cambia la contraseña con ella, y onLogout la cierra (CodeaApp cambia de pantalla solo).
    sessions: SessionRepository,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // shared by all the admin screens.
    val docentesViewModel: DocentesAdminViewModel = viewModel()
    val salasViewModel: SalasAdminViewModel = viewModel()
    val infantesViewModel: InfantesAdminViewModel = viewModel()
    val padresViewModel: PadresAdminViewModel = viewModel()
    val rutinasViewModel: RutinasAdminViewModel = viewModel()
    val parentViewModel: ParentViewModel = viewModel()
    // los avisos que le llegan a la asistente (Menú) y al papá (campana de Hoy).
    val avisosViewModel: AvisosViewModel = viewModel()
    // la bandeja y la conversación de la docente y del papá.
    val chatViewModel: ChatViewModel = viewModel()
    val accountViewModel: AccountViewModel = viewModel(factory = AccountViewModel.Factory(sessions))

    // el código de acceso de alguien recién dado de alta: se muestra una sola vez.
    var codigoPendiente by remember { mutableStateOf<AccessCodeNotice?>(null) }

    // showSnackbar is a suspend function, so it needs a scope.
    val coroutineScope = rememberCoroutineScope()

    // errores y avisos que mandan los ViewModels de admin (por ejemplo, un error de la API).
    LaunchedEffect(Unit) {
        DataEvents.notices.collect { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(Unit) {
        DataEvents.accessCodes.collect { codigoPendiente = it }
    }
    codigoPendiente?.let { AccessCodeDialog(notice = it, onDismiss = { codigoPendiente = null }) }

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
                if (viewModel.hasCheckedIn && viewModel.routine.subprocesses.isNotEmpty()) {
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

            // "rutina de hoy": se abre solo después de registrar la entrada. es de solo lectura
            // (la rutina la asigna coordinación) y su botón lleva a "mi día".
            composable<RutinaDeHoyRoute> {
                RutinaDeHoyScreen(
                    routine = viewModel.routine,
                    onComenzar = { navController.popBackStack(MyDayRoute, inclusive = false) },
                )
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
                        viewModel.addReport(form.toReport(route.subprocessNumber), photo = form.uiState.photo, onSuccess = {
                            Toast.makeText(context, savedMessage, Toast.LENGTH_LONG).show()
                            // ...and the navigation is decided here, by the ui.
                            navController.popBackStack(MyDayRoute, inclusive = false)
                        })
                    },
                    onBackClick = { navController.popBackStack() },
                )
            }
        }

        composable<MyRoomRoute> {
            MyRoomScreen(
                children = viewModel.children,
                roomName = viewModel.teacher.roomName,
                pendingChanges = viewModel.pendingChanges,
                onSummaryClick = { childId -> navController.navigate(ResumenDelDiaRoute(childId)) },
                onAttendanceChange = viewModel::setAttendance,
                onChatClick = { childId ->
                    val conversation = viewModel.conversationOf(childId)
                    if (conversation == null) {
                        DataEvents.notice("Este niño todavía no tiene chat")
                    } else {
                        chatViewModel.open(conversation)
                        navController.navigateToTopLevel(TopLevelDestination.CHATS)
                    }
                },
            )
        }

        composable<ResumenDelDiaRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ResumenDelDiaRoute>()
            val resumenViewModel: ResumenDelDiaViewModel = viewModel()
            LaunchedEffect(route.childId) { route.childId.toIntOrNull()?.let(resumenViewModel::cargar) }
            ResumenDelDiaScreen(
                viewModel = resumenViewModel,
                onBackClick = { navController.popBackStack() },
                onGuardado = {
                    coroutineScope.launch { snackbarHostState.showSnackbar("Resumen guardado") }
                    navController.popBackStack()
                },
            )
        }

        composable<ChatsRoute> {
            ChatTab(
                chatViewModel = chatViewModel,
                isParent = false,
                // desde la conversación se vuelve a la bandeja; desde la bandeja, a Mi sala.
                onBackClick = {
                    if (chatViewModel.openId != null) chatViewModel.close()
                    else navController.navigateToTopLevel(TopLevelDestination.MY_ROOM)
                },
            )
        }

        navigation<MenuGraph>(startDestination = MenuRoute()) {
            composable<MenuRoute> { backStackEntry ->
                // toRoute reads the values we sent with the route (here, openChecker).
                val route = backStackEntry.toRoute<MenuRoute>()
                val avisosPendientes by avisosViewModel.pendientes.collectAsState()
                MenuScreen(
                    teacher = viewModel.teacher,
                    checkIn = viewModel.checkIn,
                    openChecker = route.openChecker,
                    avisosPendientes = avisosPendientes,
                    onCheckerClick = { navController.navigate(CheckerRoute) },
                    onOptionClick = { option ->
                        if (option == MenuOption.NOTICES) {
                            navController.navigate(AvisosFundacionRoute)
                        } else if (option == MenuOption.PROFILE) {
                            navController.navigate(AccountRoute)
                        } else {
                            navController.navigate(MenuOptionRoute(option.name))
                        }
                    },
                    // cerrar sesión de verdad: CodeaApp ve que la sesión terminó y dibuja el inicio de sesión.
                    onLogoutConfirm = onLogout,
                )
            }
            composable<CheckerRoute> {
                // this view model lives inside the destination: its owner is this screen,
                // so the late reason is cleared when the teacher leaves.
                val checker: CheckerViewModel = viewModel()
                // el turno y el nombre del centro llegan de GET /me; si cambian, el checador los vuelve a leer.
                LaunchedEffect(viewModel.centerName, viewModel.teacher.shiftStartMinutes) {
                    checker.start(
                        CheckerConfig(
                            shiftStartMinutes = viewModel.teacher.shiftStartMinutes,
                            placeName = viewModel.centerName.ifBlank { "La Oruga" },
                        ),
                    )
                }
                CheckerScreen(
                    uiState = checker.uiState,
                    registeredCheckIn = viewModel.checkIn,
                    // the menu is right below the check-in, so going back is enough.
                    onBackClick = { navController.popBackStack() },
                    onRefreshTime = checker::refreshTime,
                    onCheckOutClick = { checker.registrarSalida { record -> viewModel.registerCheckOut(record) } },
                    onCheckInClick = {
                        checker.registrar { record ->
                            viewModel.registerCheckIn(record)
                            navController.navigateToCheckInDone()
                        }
                    },
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
            composable<AvisosFundacionRoute> {
                // al entrar, lo que no había visto se marca como leído (el badge del menú baja a 0),
                // pero esta vez se sigue viendo en "Nuevos".
                LaunchedEffect(Unit) { avisosViewModel.alAbrir() }
                val avisos by avisosViewModel.avisos.collectAsState()
                AvisosFundacionScreen(
                    avisos = avisos,
                    onBackClick = { navController.popBackStack() },
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
                val avisos by centroViewModel.avisos.collectAsState()
                val pendientesBorrado by centroViewModel.pendientesBorrado.collectAsState()
                CentroScreen(
                    salas = salas,
                    infantes = infantesCentro,
                    destinoSalaId = centroViewModel.destinoSalaId,
                    onDestinoChange = centroViewModel::onDestinoChange,
                    avisos = avisos,
                    solicitudesBorrado = pendientesBorrado,
                    onSolicitudesBorradoClick = { navController.navigate(AdminSolicitudesBorradoRoute) },
                    onSalaClick = { sala ->
                        navController.navigate(AdminSalaDetalleRoute(sala.id))
                    },
                    avisoTexto = centroViewModel.avisoTexto,
                    onAvisoTextoChange = centroViewModel::onAvisoTextoChange,
                    canPublicarAviso = centroViewModel.canPublicar,
                    onPublicarAviso = {
                        centroViewModel.publicarAviso(onSuccess = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Aviso publicado")
                            }
                        })
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
                        salasViewModel.eliminarSala(sala.id, onSuccess = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Sala \"${sala.nombre}\" eliminada")
                            }
                            navController.popBackStack()
                        })
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<AdminSolicitudesBorradoRoute> {
                val borradoViewModel: SolicitudesBorradoViewModel = viewModel()
                val solicitudes by borradoViewModel.solicitudes.collectAsState()
                val cargandoBorrado by borradoViewModel.cargando.collectAsState()
                SolicitudesBorradoScreen(
                    solicitudes = solicitudes,
                    cargando = cargandoBorrado,
                    onBackClick = { navController.popBackStack() },
                    onAprobar = { solicitud ->
                        borradoViewModel.aprobar(solicitud.id) {
                            coroutineScope.launch { snackbarHostState.showSnackbar("Foto borrada") }
                        }
                    },
                    onRechazar = { solicitud ->
                        borradoViewModel.rechazar(solicitud.id) {
                            coroutineScope.launch { snackbarHostState.showSnackbar("La foto se conserva") }
                        }
                    },
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
                            asistente = uiState.asistenteElegida,
                            onSuccess = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Sala \"${uiState.nombre}\" creada")
                                }
                                navController.popBackStack()
                            }
                        )
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
                    apellido = uiState.apellido, onApellidoChange = altaAsistenteViewModel::onApellidoChange,
                    telefono = uiState.telefono, onTelefonoChange = altaAsistenteViewModel::onTelefonoChange,
                    telefonoError = uiState.telefonoError,
                    correo = uiState.correo, onCorreoChange = altaAsistenteViewModel::onCorreoChange,
                    correoError = uiState.correoError,
                    salas = salas,
                    onElegirSala = altaAsistenteViewModel::onElegirSala,
                    turno = uiState.turno, onElegirTurno = altaAsistenteViewModel::onElegirTurno,
                    canGuardar = uiState.canGuardar,
                    onGuardar = {
                        docentesViewModel.altaDocente(
                            nombre = uiState.nombre,
                            apellido = uiState.apellido,
                            sala = uiState.salaElegida ?: "",
                            telefono = uiState.telefono,
                            correo = uiState.correo,
                            turno = uiState.turno,
                            onSuccess = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Asistente \"${uiState.nombre}\" dada de alta")
                                }
                                navController.popBackStack()
                            }
                        )
                    },
                    onBack = { navController.popBackStack() },
                )
            }
        }



        // la pestaña "familias" junta infantes y padres: la lista tiene un selector y todo lo que abre
        // (detalle e inscribir de cada uno) deja la barra inferior en "Familias".
        navigation<AdminFamiliasGraph>(startDestination = AdminFamiliasRoute) {
            composable<AdminFamiliasRoute> {
                val infantes by infantesViewModel.infantes.collectAsState()
                val padres by padresViewModel.padres.collectAsState()
                val salas by salasViewModel.salas.collectAsState()
                FamiliasScreen(
                    infantes = infantes,
                    padres = padres,
                    totalSalas = salas.size,
                    onInfanteClick = { infante -> navController.navigate(AdminInfanteDetalleRoute(infante.id)) },
                    onInscribirInfante = { navController.navigate(AdminInscribirInfanteRoute) },
                    onPadreClick = { padre -> navController.navigate(PadresAdminDetalleRoute(padre.id)) },
                    onInscribirPadre = { navController.navigate(PadresAdminInscribirRoute) },
                )
            }
            composable<AdminInfanteDetalleRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<AdminInfanteDetalleRoute>()
                val infantes by infantesViewModel.infantes.collectAsState()
                val infante = infantes.firstOrNull { it.id == route.infanteId }
                    ?: return@composable
                val salasDisponibles by salasViewModel.salas.collectAsState()
                val padres by padresViewModel.padres.collectAsState()
                val padresVinculados = padres.filter { infante.id in it.hijoIds }
                InfanteDetalleScreen(
                    infante = infante,
                    salas = salasDisponibles.map { it.nombre },
                    padresVinculados = padresVinculados,
                    onAbrirPadre = { padreId -> navController.navigate(PadresAdminDetalleRoute(padreId)) },
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
                    apellido = uiState.apellido,
                    onApellidoChange = inscribirInfanteViewModel::onApellidoChange,
                    nacimiento = uiState.nacimiento,
                    onNacimientoChange = inscribirInfanteViewModel::onNacimientoChange,
                    nacimientoError = uiState.nacimientoError,
                    alergias = uiState.alergias,
                    onAlergiasChange = inscribirInfanteViewModel::onAlergiasChange,
                    salas = salas,
                    onElegirSala = inscribirInfanteViewModel::onElegirSala,
                    canGuardar = uiState.canGuardar,
                    onGuardar = {
                        infantesViewModel.inscribirInfante(
                            nombre = uiState.nombre,
                            apellido = uiState.apellido,
                            nacimiento = uiState.nacimientoIso ?: return@InscribirInfanteScreen,
                            sala = uiState.salaElegida ?: "",
                            alergias = uiState.alergias,
                            onSuccess = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("\"${uiState.nombre}\" inscrito")
                                }
                                navController.popBackStack()
                            }
                        )
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable<PadresAdminDetalleRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<PadresAdminDetalleRoute>()
                val padres by padresViewModel.padres.collectAsState()
                val padre = padres.firstOrNull { it.id == route.padreId }
                    ?: return@composable
                val infantes by infantesViewModel.infantes.collectAsState()
                val hijosNombres = infantes.filter { it.id in padre.hijoIds }.map { it.nombre }
                PadreDetalleScreen(
                    padre = padre,
                    hijosNombres = hijosNombres,
                    onToggleAcceso = { padresViewModel.toggleAcceso(padre.id) },
                    onReenviarInvitacion = {
                        padresViewModel.reenviarInvitacion(padre.id, onSuccess = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Invitación reenviada a \"${padre.nombre}\"")
                            }
                        })
                    },
                    onAbrirChat = { navController.navigateToTopLevel(TopLevelDestination.CHATS) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable<PadresAdminInscribirRoute> {
                val inscribirPadreViewModel: InscribirPadreViewModel = viewModel()
                val uiState = inscribirPadreViewModel.uiState
                val infantesDisponibles by infantesViewModel.infantes.collectAsState()
                InscribirPadreScreen(
                    nombre = uiState.nombre,
                    onNombreChange = inscribirPadreViewModel::onNombreChange,
                    nombreError = uiState.nombreError,
                    apellido = uiState.apellido,
                    onApellidoChange = inscribirPadreViewModel::onApellidoChange,
                    telefono = uiState.telefono,
                    onTelefonoChange = inscribirPadreViewModel::onTelefonoChange,
                    telefonoError = uiState.telefonoError,
                    correo = uiState.correo,
                    onCorreoChange = inscribirPadreViewModel::onCorreoChange,
                    correoError = uiState.correoError,
                    parentesco = uiState.parentesco,
                    onElegirParentesco = inscribirPadreViewModel::onElegirParentesco,
                    hijosDisponibles = infantesDisponibles.map { it.id to it.nombre },
                    hijosSeleccionados = uiState.hijosSeleccionados,
                    onToggleHijo = inscribirPadreViewModel::onToggleHijo,
                    canGuardar = uiState.canGuardar,
                    onGuardar = {
                        padresViewModel.inscribirPadre(
                            nombre = uiState.nombre,
                            apellido = uiState.apellido,
                            telefono = uiState.telefono,
                            correo = uiState.correo,
                            parentesco = uiState.parentesco ?: "",
                            hijoIds = uiState.hijosSeleccionados,
                            onSuccess = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("\"${uiState.nombre}\" inscrito")
                                }
                                navController.popBackStack()
                            }
                        )
                    },
                    onBack = { navController.popBackStack() },
                )
            }
        }

        // mi cuenta: perfil, cambiar contraseña y cerrar sesión. la abren los tres roles.
        composable<AccountRoute> {
            val context = LocalContext.current
            AccountScreen(
                name = accountViewModel.name,
                email = accountViewModel.email,
                children = accountViewModel.children,
                loadError = accountViewModel.loadError,
                changing = accountViewModel.changing,
                localError = accountViewModel.localError,
                remoteError = accountViewModel.remoteError,
                onChangePassword = { current, new, confirmation ->
                    accountViewModel.changePassword(current, new, confirmation, onSuccess = {
                        Toast.makeText(context, context.getString(R.string.account_password_changed), Toast.LENGTH_SHORT).show()
                    })
                },
                onClearPasswordErrors = accountViewModel::clearPasswordErrors,
                onLogoutConfirm = onLogout,
                onBackClick = { navController.popBackStack() },
                passwordChanged = accountViewModel.passwordChanged,
                onPasswordChangedShown = accountViewModel::passwordChangedShown,
            )
        }

        // parent tabs. each is only one screen. the data comes from ParentViewModel.
        composable<ParentTodayRoute> {
            val context = LocalContext.current
            val pendientes by avisosViewModel.pendientes.collectAsState()
            val notifs by avisosViewModel.notifs.collectAsState()
            ParentTodayScreen(
                summary = parentViewModel.summary,
                timelineEvents = parentViewModel.timeline,
                pendientes = pendientes,
                notifs = notifs,
                onAbrirNotif = avisosViewModel::alAbrir,
                onCuentaClick = { navController.navigate(AccountRoute) },
                onNavigateToCalendar = {
                    navController.navigateToTopLevel(TopLevelDestination.PARENT_CALENDAR)
                },
                onSendNote = { texto ->
                    parentViewModel.sendNote(texto, onSuccess = {
                        Toast.makeText(context, "Nota enviada a la asistente", Toast.LENGTH_SHORT).show()
                    })
                },
            )
        }

        composable<ParentCalendarRoute> {
            ParentCalendarScreen(
                monthData = parentViewModel.monthData,
                resumenSemana = parentViewModel.weekly,
                initialSelectedDay = parentViewModel.todayDayNumber,
                onDaySelected = { day ->
                    parentViewModel.selectDay(day.dayNumber)
                    navController.navigateToTopLevel(TopLevelDestination.PARENT_TODAY)
                },
            )
        }

        composable<ParentPhotosRoute> {
            val context = LocalContext.current
            ParentPhotosScreen(
                nombreNino = parentViewModel.childName.substringBefore(" "),
                fotos = parentViewModel.photos,
                // "descargar" abre la foto en el navegador, que la puede guardar.
                onDescargar = { foto ->
                    foto.url?.let { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
                },
                onPedirBorrar = parentViewModel::requestPhotoDeletion,
            )
        }

        composable<ParentChatRoute> {
            ChatTab(
                chatViewModel = chatViewModel,
                isParent = true,
                // con un solo hijo no hay bandeja: atrás regresa a Hoy.
                onBackClick = {
                    if (chatViewModel.openId != null && !chatViewModel.skipsInbox) chatViewModel.close()
                    else navController.navigateToTopLevel(TopLevelDestination.PARENT_TODAY)
                },
            )
        }

        composable<ParentDocumentsRoute> {
            val context = LocalContext.current
            var documentoElegido by remember { mutableStateOf<String?>(null) }
            var mostrarAgregar by remember { mutableStateOf(false) }
            // el selector de archivos del teléfono: PDF o imagen.
            val selector = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                val documentoId = documentoElegido
                if (uri != null && documentoId != null) {
                    val tipo = context.contentResolver.getType(uri) ?: "application/pdf"
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes != null) parentViewModel.uploadDocument(documentoId, bytes, tipo)
                }
                documentoElegido = null
            }
            ParentDocumentsScreen(
                childName = parentViewModel.childName.substringBefore(" "),
                documents = parentViewModel.documents,
                authorizedPeople = parentViewModel.authorizedPeople,
                onDocumentAction = { documento ->
                    documentoElegido = documento.id
                    selector.launch("*/*")
                },
                onAddAuthorized = { mostrarAgregar = true },
                onRemoveAuthorized = parentViewModel::removeAuthorized,
            )
            if (mostrarAgregar) {
                AddAuthorizedDialog(
                    onConfirm = { nombre ->
                        parentViewModel.addAuthorized(nombre)
                        mostrarAgregar = false
                    },
                    onDismiss = { mostrarAgregar = false },
                )
            }
        }


        // la pestaña "ajustes": el menú de lo que el admin puede configurar. rutinas abre
        // encima y la barra inferior se queda en "Ajustes".
        navigation<AjustesAdminGraph>(startDestination = AjustesAdminRoute) {
            composable<AjustesAdminRoute> {
                // al entrar se piden las rutinas otra vez, para el contador.
                LaunchedEffect(Unit) { rutinasViewModel.cargar() }
                val rutinas by rutinasViewModel.rutinas.collectAsState()
                AjustesScreen(
                    totalRutinas = rutinas.size,
                    onRutinasClick = { navController.navigate(RutinasAdminRoute) },
                    onCuentaClick = { navController.navigate(AccountRoute) },
                )
            }
            composable<RutinasAdminRoute> {
                // al entrar (o volver del detalle) se piden las rutinas otra vez: lo que no se guardó se descarta.
                LaunchedEffect(Unit) { rutinasViewModel.cargar() }
                val rutinas by rutinasViewModel.rutinas.collectAsState()
                // la lista de rutinas ya no es una pestaña: lleva su título y su botón de volver.
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Rutinas", fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                                }
                            },
                        )
                    },
                ) { paddingValues ->
                    RutinasAdminScreen(
                        rutinas = rutinas,
                        onAbrirRutina = { rutinaId -> navController.navigate(RutinasAdminDetalleRoute(rutinaId)) },
                        onNuevaRutina = { navController.navigate(RutinasAdminNuevaRoute) },
                        modifier = Modifier.padding(paddingValues),
                    )
                }
            }
            composable<RutinasAdminDetalleRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<RutinasAdminDetalleRoute>()
                val rutinas by rutinasViewModel.rutinas.collectAsState()
                val rutina = rutinas.firstOrNull { it.id == route.rutinaId }
                    ?: return@composable
                val momentosPorRutina by rutinasViewModel.momentos.collectAsState()
                val salas by salasViewModel.salas.collectAsState()
                RutinaDetalleScreen(
                    nombreRutina = rutina.nombre,
                    momentos = momentosPorRutina[rutina.id].orEmpty(),
                    onReordenar = { from, to -> rutinasViewModel.reordenar(rutina.id, from, to) },
                    onAgregarMomento = { navController.navigate(RutinasAdminAgregarMomentoRoute(rutina.id)) },
                    onGuardar = {
                        rutinasViewModel.guardar(rutina.id, onSuccess = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("\"${rutina.nombre}\" guardada")
                            }
                            navController.popBackStack()
                        })
                    },
                    salas = salas,
                    salasAsignadas = rutina.salasIds.toSet(),
                    esPredeterminada = rutina.esPredeterminada,
                    onAlternarSala = { salaId, asignar -> rutinasViewModel.asignarSala(rutina.id, salaId, asignar) },
                )
            }
            composable<RutinasAdminAgregarMomentoRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<RutinasAdminAgregarMomentoRoute>()
                val agregarMomentoViewModel: AgregarMomentoViewModel = viewModel()
                val uiState = agregarMomentoViewModel.uiState
                val momentosPorRutina by rutinasViewModel.momentos.collectAsState()
                val catalogo = remember(momentosPorRutina[route.rutinaId]) {
                    rutinasViewModel.catalogoDisponible(route.rutinaId)
                }
                AgregarMomentoScreen(
                    catalogo = catalogo,
                    elegido = uiState.elegido,
                    onElegir = agregarMomentoViewModel::onElegir,
                    nombrePropio = uiState.nombrePropio,
                    onNombrePropioChange = agregarMomentoViewModel::onNombrePropioChange,
                    registroPropio = uiState.registroPropio,
                    onRegistroPropioChange = agregarMomentoViewModel::onRegistroPropioChange,
                    onAgregar = {
                        val nombre = uiState.nombrePropio.trim().ifBlank { uiState.elegido.orEmpty() }
                        if (nombre.isNotBlank()) {
                            val registro = uiState.registroPropio.trim().ifBlank {
                                catalogo.firstOrNull { it.nombre == nombre }?.registro.orEmpty()
                            }
                            rutinasViewModel.agregarMomento(route.rutinaId, nombre, registro)
                            navController.popBackStack()
                        }
                    },
                )
            }
            // el asistente de "nueva rutina" tiene tres pasos pero un solo formulario:
            // "nueva rutina" lo crea y lo guarda mientras el flujo sigue abierto.
            composable<RutinasAdminNuevaRoute> { backStackEntry ->
                val form: NuevaRutinaViewModel = viewModel(backStackEntry)
                val uiState = form.uiState
                NuevaRutinaScreen(
                    nombre = uiState.nombre,
                    onNombreChange = form::onNombreChange,
                    etapa = uiState.etapa,
                    onElegirEtapa = form::onElegirEtapa,
                    onElegirMomentos = { navController.navigate(RutinasAdminCatalogoRoute) },
                )
            }
            composable<RutinasAdminCatalogoRoute> { backStackEntry ->
                val form = nuevaRutinaForm(navController, backStackEntry)
                val uiState = form.uiState
                CatalogoMomentosScreen(
                    catalogo = form.catalogoCompleto(),
                    seleccionados = uiState.seleccionados,
                    onAlternar = form::onAlternar,
                    creando = uiState.creando,
                    onAbrirCrear = form::onAbrirCrear,
                    nombrePropio = uiState.nombrePropio,
                    onNombrePropioChange = form::onNombrePropioChange,
                    registroPropio = uiState.registroPropio,
                    onRegistroPropioChange = form::onRegistroPropioChange,
                    onGuardarPropio = form::onGuardarPropio,
                    onContinuar = { navController.navigate(RutinasAdminOrdenRoute) },
                )
            }
            composable<RutinasAdminOrdenRoute> { backStackEntry ->
                val form = nuevaRutinaForm(navController, backStackEntry)
                OrdenRutinaScreen(
                    momentos = form.momentosFinal(),
                    onReordenar = form::reordenarMomentos,
                    onGuardar = {
                        val nombre = form.uiState.nombre.trim().ifBlank { "Nueva rutina" }
                        rutinasViewModel.crearRutina(nombre, form.momentosFinal(), onSuccess = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("\"$nombre\" creada")
                            }
                            navController.popBackStack(RutinasAdminRoute, inclusive = false)
                        })
                    },
                )
            }
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

// los pasos 2 y 3 de "nueva rutina" piden el formulario del paso 1,
// igual que el reporte con los suyos.
@Composable
private fun nuevaRutinaForm(navController: NavHostController, backStackEntry: NavBackStackEntry): NuevaRutinaViewModel {
    val flowEntry = remember(backStackEntry) { navController.getBackStackEntry<RutinasAdminNuevaRoute>() }
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

// the chat tab of the teacher and of the parent. while it is visible it asks the API for news
// every 10 seconds; it stops when the app goes to the background (no WebSocket yet).
@Composable
private fun ChatTab(
    chatViewModel: ChatViewModel,
    isParent: Boolean,
    onBackClick: () -> Unit,
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                chatViewModel.refresh()
                delay(CHAT_POLL_MS)
            }
        }
    }
    ChatsScreen(
        conversations = chatViewModel.conversations,
        loaded = chatViewModel.loaded,
        openConversation = chatViewModel.openConversation,
        messages = chatViewModel.messages,
        isParent = isParent,
        onOpen = chatViewModel::open,
        onBackClick = onBackClick,
        onSend = chatViewModel::send,
        onRetry = chatViewModel::retry,
    )
}

private const val CHAT_POLL_MS = 10_000L
