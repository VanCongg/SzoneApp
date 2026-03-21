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
                onLogout = {
                    // Xóa data người dùng và quay lại login
                    navController.navigate(NavScreen.AuthNavScreen) {
                        popUpTo(NavScreen.MainNavScreen) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
