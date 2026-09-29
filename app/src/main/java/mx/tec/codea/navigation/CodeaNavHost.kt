package mx.tec.codea.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import mx.tec.codea.ui.screens.chats.ChatsScreen
import mx.tec.codea.ui.screens.checker.CheckerScreen
import mx.tec.codea.ui.screens.menu.MenuScreen
import mx.tec.codea.ui.screens.myday.DayNotStartedScreen
import mx.tec.codea.ui.screens.myday.MyDayScreen
import mx.tec.codea.ui.screens.myroom.Child
import mx.tec.codea.ui.screens.myroom.MyRoomScreen
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
    modifier: Modifier = Modifier,
) {
    var lastOpenedChatChildId by rememberSaveable {
        mutableStateOf<String?>(null)
    }
    NavHost(
        navController = navController,
        // this is the first tab we show when the app starts.
        startDestination = MyDayGraph,
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
