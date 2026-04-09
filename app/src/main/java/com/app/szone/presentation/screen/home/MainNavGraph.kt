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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.remember
import androidx.navigation.toRoute
import com.app.szone.presentation.navigation.NavScreen
import com.app.szone.presentation.screen.profile.ProfileScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SetupMainNavGraph(
    onLogout: () -> Unit = {},
    startDestination: NavScreen = NavScreen.MainNavScreen
) {
    val bottomNavController = rememberNavController()
    Scaffold(
        contentWindowInsets = WindowInsets(0),
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() },
        ) {
            composable<NavScreen.MainNavScreen> { backStackEntry ->
                // ✅ Get shared ViewModel from backstack
                val parentBackStackEntry = remember(backStackEntry) {
                    bottomNavController.getBackStackEntry(NavScreen.MainNavScreen)
                }
                val orderViewModel: com.app.szone.presentation.viewmodel.OrderViewModel = koinViewModel(
                    viewModelStoreOwner = parentBackStackEntry
                )

                ShipperHomeScreen(
                    navController = bottomNavController,
                    orderViewModel = orderViewModel
                )
            }
            composable<NavScreen.OrderDetailNavScreen> { backStackEntry ->
                // Use the same shared OrderViewModel as Home/Scanner.
                val parentBackStackEntry = remember(backStackEntry) {
                    bottomNavController.getBackStackEntry(NavScreen.MainNavScreen)
                }
                val orderViewModel: com.app.szone.presentation.viewmodel.OrderViewModel = koinViewModel(
                    viewModelStoreOwner = parentBackStackEntry
                )

                val args = backStackEntry.toRoute<NavScreen.OrderDetailNavScreen>()
                OrderDetailScreen(
                    orderId = args.orderId,
                    viewModel = orderViewModel,
                    onBackClick = { bottomNavController.popBackStack() }
                )
            }

            composable<NavScreen.WarehouseScannerNavScreen> {
                WarehouseScannerScreen(navController = bottomNavController)
            }

            composable<NavScreen.ShipperScannerNavScreen> { backStackEntry ->
                // ✅ Get same shared ViewModel as Home
                val parentBackStackEntry = remember(backStackEntry) {
                    bottomNavController.getBackStackEntry(NavScreen.MainNavScreen)
                }
                val orderViewModel: com.app.szone.presentation.viewmodel.OrderViewModel = koinViewModel(
                    viewModelStoreOwner = parentBackStackEntry
                )

                ShipperScannerScreen(
                    navController = bottomNavController,
                    orderViewModel = orderViewModel
                )
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
