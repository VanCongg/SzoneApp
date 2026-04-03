package com.app.szone.presentation.screen.home

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.app.szone.presentation.navigation.NavScreen
import com.app.szone.presentation.screen.home.common.NavBottomBar
import com.app.szone.presentation.screen.profile.ProfileScreen

@Composable
fun SetupMainNavGraph(
    onLogout: () -> Unit = {},
    startDestination: NavScreen = NavScreen.MainNavScreen
) {
    val bottomNavController = rememberNavController()
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            NavBottomBar(navController = bottomNavController)
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() },
        ) {
            composable<NavScreen.MainNavScreen> {
                ShipperHomeScreen(
                    navController = bottomNavController
                )
            }
            composable<NavScreen.OrderDetailNavScreen> { backStackEntry ->
                val args = backStackEntry.toRoute<NavScreen.OrderDetailNavScreen>()
                OrderDetailScreen(
                    orderId = args.orderId,
                    onBackClick = { bottomNavController.popBackStack() }
                )
            }

            composable<NavScreen.WarehouseScannerNavScreen> {
                WarehouseScannerScreen(navController = bottomNavController)
            }

            composable<NavScreen.ShipperScannerNavScreen> {
                ShipperScannerScreen(navController = bottomNavController)
            }

            composable<NavScreen.ProfileNavScreen> {
                ProfileScreen(
                    navController = bottomNavController,
                    onLogout = onLogout
                )
            }
        }
    }
}
