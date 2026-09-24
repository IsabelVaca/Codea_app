package mx.tec.codea.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import mx.tec.codea.ui.screens.myroom.MyRoomScreen

// the nav host is like a map of the app: it connects each route to its screen.
// the nav controller moves the user from one screen to another.
@Composable
fun CodeaNavHost(
    navController: NavHostController,
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
            composable<MyDayRoute> {
                MyDayScreen(
                    onSwitchClick = { navController.navigate(DayNotStartedRoute) },
                )
            }
            composable<DayNotStartedRoute> {
                DayNotStartedScreen(
                    onCheckerClick = { navController.navigateToCheckerThroughMenu() },
                    // "mi día" is right below this screen, so going back is enough.
                    onSwitchClick = { navController.popBackStack() },
                )
            }
        }

        composable<MyRoomRoute> {
            MyRoomScreen()
        }

        composable<ChatsRoute> {
            ChatsScreen()
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
