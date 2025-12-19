package com.depi.drlist.ui.navigation

sealed class Route(val route: String) {
    object Login : Route("login")
    object PasswordReset : Route("password_reset")
    object Home : Route("home")
    object Search : Route("search")
    object Cart : Route("cart")
    object Profile : Route("profile")
    object Checkout : Route("checkout")
    object ProductDetail : Route("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
}
