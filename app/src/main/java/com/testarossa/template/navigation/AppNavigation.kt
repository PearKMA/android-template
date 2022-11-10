package com.testarossa.template.navigation

import android.net.Uri
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.testarossa.template.library.compose.utils.defaultScreenEnterTransition
import com.testarossa.template.library.compose.utils.defaultScreenExitTransition
import com.testarossa.template.library.compose.utils.defaultScreenPopEnterTransition
import com.testarossa.template.library.compose.utils.defaultScreenPopExitTransition

internal sealed class NavScreen(val route: String) {
    object Splash : NavScreen("Splash")
    object Intro : NavScreen("Intro")
    object Guide : NavScreen("Guide")
    object Home : NavScreen("Home")
    object Library : NavScreen("Library")
    object Preview : NavScreen("Preview/{urlMedia}") {
        fun createRoute(url: String): String {
            return "Preview/${Uri.encode(url)}"
        }
    }
}


@ExperimentalAnimationApi
@Composable
internal fun AppNavigation(
    navController: NavHostController,
    isFirstUse: Boolean,
    removeFirstUse: () -> Unit,
    exitApp: () -> Unit
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = NavScreen.Splash.route,
        enterTransition = { defaultScreenEnterTransition(initialState, targetState) },
        exitTransition = { defaultScreenExitTransition(initialState, targetState) },
        popEnterTransition = { defaultScreenPopEnterTransition() },
        popExitTransition = { defaultScreenPopExitTransition() }
    ) {
        composable(
            route = NavScreen.Splash.route
        ) {
            if (isFirstUse) {
                navController.navigate(NavScreen.Intro.route) {
                    popUpTo(NavScreen.Splash.route) { inclusive = true }
                    launchSingleTop = true
                }
                removeFirstUse()
            } else {
                navController.navigate(NavScreen.Home.route) {
                    popUpTo(NavScreen.Splash.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        composable(
            route = NavScreen.Preview.route,
            arguments = listOf(
                navArgument("urlMedia") { type = NavType.StringType }
            )
        ) {
//            PreviewMedia {
//                navController.navigateUp()
//            }
        }
    }
}