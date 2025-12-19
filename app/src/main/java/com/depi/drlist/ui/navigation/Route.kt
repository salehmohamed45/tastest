package com.depi.drlist.ui.navigation

sealed class Route(val route: String) {
    object Login : Route("login")
    object Home : Route("home")
    object Search : Route("search")
    object Cart : Route("cart")
    object Profile : Route("profile")
    object Checkout : Route("checkout")
}
