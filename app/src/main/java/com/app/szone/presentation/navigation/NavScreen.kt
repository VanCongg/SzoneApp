package com.app.szone.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class NavScreen {

    // Mâm 1
    @Serializable
    data object WelcomeNavScreen : NavScreen()

    @Serializable
    data object AuthNavScreen : NavScreen()

    @Serializable
    data object MainNavScreen : NavScreen()

    @Serializable
    data class OrderDetailNavScreen(val orderId: String) : NavScreen()

    @Serializable
    data object WarehouseScannerNavScreen : NavScreen()

    @Serializable
    data object ShipperScannerNavScreen : NavScreen()

    @Serializable
    data object LoginNavScreen : NavScreen()

    @Serializable
    data object ProfileNavScreen : NavScreen()
}
