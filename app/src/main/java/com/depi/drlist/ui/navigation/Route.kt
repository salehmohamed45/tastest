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

    // Customer order screens
    object OrderHistory : Route("order_history")
    object OrderTracking : Route("order_tracking/{orderId}") {
        fun createRoute(orderId: String) = "order_tracking/$orderId"
    }

    // Admin screens
    object AdminDashboard : Route("admin_dashboard")
    object AdminOrders : Route("admin_orders")
    object AdminProducts : Route("admin_products")   // ✅ NEW
    object AddProduct : Route("add_product")

    object AdminOrderDetail : Route("admin_order_detail/{orderId}") {
        fun createRoute(orderId: String) = "admin_order_detail/$orderId"
    }

    object CustomerDetails : Route("customer_details/{userId}") {
        fun createRoute(userId: String) = "customer_details/$userId"
    }
}
