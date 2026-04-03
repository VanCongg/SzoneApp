package com.app.szone.presentation.navigation


import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.app.szone.presentation.screen.auth.SetupAuthNavGraph
import com.app.szone.presentation.screen.home.SetupMainNavGraph
import com.app.szone.presentation.screen.welcome.WelcomeScreen

@ExperimentalAnimationApi
@Composable
fun SetupNavGraph(
    navController: NavHostController,
    startDestination: NavScreen,
) {
    val onLogout: () -> Unit = {
        navController.navigate(NavScreen.AuthNavScreen) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() },
    ) {
        composable<NavScreen.WelcomeNavScreen> {
            WelcomeScreen(
                onContinueClick = {
                    navController.navigate(NavScreen.MainNavScreen)
                }
            )
        }
        composable<NavScreen.AuthNavScreen> {
            SetupAuthNavGraph(
                rootNavController = navController
            )
        }
        composable<NavScreen.MainNavScreen>() {
            SetupMainNavGraph(
                onLogout = onLogout,
                startDestination = NavScreen.MainNavScreen
            )
        }

        composable<NavScreen.WarehouseScannerNavScreen> {
            SetupMainNavGraph(
                onLogout = onLogout,
                startDestination = NavScreen.WarehouseScannerNavScreen
            )
        }
    }
}
