package mx.tec.codea.ui.navigation

import android.widget.Toast
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import mx.tec.codea.ui.state.CheckerViewModel
import mx.tec.codea.ui.state.CodeaViewModel
import mx.tec.codea.ui.state.ReportFormViewModel

// the nav host is like a map of the app: it connects each route to its screen.
// here we also connect each screen to its state: the screens are "dumb",
// so this file reads the view models and passes plain data and lambdas.
@Composable
fun CodeaNavHost(
    navController: NavHostController,
    viewModel: CodeaViewModel,
    modifier: Modifier = Modifier,
) {
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
