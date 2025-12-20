package com.depi.drlist.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.depi.drlist.ui.screens.admin.dashboard.AdminDashboardScreen
import com.depi.drlist.ui.screens.admin.orders.AdminOrderDetailScreen
import com.depi.drlist.ui.screens.admin.orders.AdminOrdersScreen
import com.depi.drlist.ui.screens.admin.products.AddProductScreen
import com.depi.drlist.ui.screens.admin.products.AdminProductsScreen
import com.depi.drlist.ui.screens.admin.customers.CustomerDetailsScreen
import com.depi.drlist.ui.screens.cart.CartScreen
import com.depi.drlist.ui.screens.checkout.CheckoutScreen
import com.depi.drlist.ui.screens.detail.ProductDetailScreen
import com.depi.drlist.ui.screens.home.HomeScreen
import com.depi.drlist.ui.screens.login.AuthCheckState
import com.depi.drlist.ui.screens.login.LoginScreen
import com.depi.drlist.ui.screens.login.LoginViewModel
import com.depi.drlist.ui.screens.orders.OrderHistoryScreen
import com.depi.drlist.ui.screens.orders.OrderTrackingScreen
import com.depi.drlist.ui.screens.profile.ProfileScreen
import com.depi.drlist.ui.screens.reset.PasswordResetScreen
import com.depi.drlist.ui.screens.reset.PasswordResetViewModel
import com.depi.drlist.ui.screens.search.SearchScreen

// -------------------- Bottom Nav Items --------------------
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(Route.Home.route, "Home", Icons.Default.Home)
    object Search : BottomNavItem(Route.Search.route, "Search", Icons.Default.Search)
    object Cart : BottomNavItem(Route.Cart.route, "Cart", Icons.Default.ShoppingCart)
    object Profile : BottomNavItem(Route.Profile.route, "Profile", Icons.Default.Person)
}

// -------------------- ROOT NAV --------------------
@Composable
fun AppNavigation(themeViewModel: com.depi.drlist.ui.theme.ThemeViewModel) {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel()
    val currentUser by loginViewModel.currentUser.collectAsState()
    val authCheckState by loginViewModel.authCheckState.collectAsState()

    // ✅ Wait for auth check to complete
    when (authCheckState) {
        AuthCheckState.Checking -> {
            // Show minimal loading screen
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        AuthCheckState.Completed -> {
            // ✅ Auth check done, show appropriate screen
            val startDestination = if (currentUser != null) Route.Home.route else Route.Login.route

            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {

                // -------- Login --------
                composable(Route.Login.route) {
                    LoginScreen(
                        viewModel = loginViewModel,
                        onLoginSuccess = {
                            navController.navigate(Route.Home.route) {
                                popUpTo(Route.Login.route) { inclusive = true }
                            }
                        },
                        onNavigateToPasswordReset = {
                            navController.navigate(Route.PasswordReset.route)
                        }
                    )
                }

                composable(Route.PasswordReset.route) {
                    val vm: PasswordResetViewModel = viewModel()
                    PasswordResetScreen(
                        viewModel = vm,
                        onNavigateBack = { navController.navigateUp() }
                    )
                }

                // -------- Main (Bottom Nav) --------
                composable(Route.Home.route) {
                    MainScreen(
                        navController = navController,
                        currentUser = currentUser,
                        themeViewModel = themeViewModel,
                        onSignOut = {
                            loginViewModel.signOut()
                            navController.navigate(Route.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                // -------- Product Detail --------
                composable(
                    route = Route.ProductDetail.route,
                    arguments = listOf(navArgument("productId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val productId =
                        backStackEntry.arguments?.getString("productId") ?: return@composable

                    ProductDetailScreen(
                        productId = productId,
                        onBackClick = { navController.navigateUp() },
                        onCartClick = {
                            navController.navigate(Route.Home.route) {
                                popUpTo(Route.Home.route) { inclusive = false }
                            }
                        }
                    )
                }

                // -------- Checkout --------
                composable(Route.Checkout.route) {
                    CheckoutScreen(
                        onBackClick = { navController.navigateUp() },
                        onOrderPlaced = {
                            navController.navigate(Route.Home.route) {
                                popUpTo(Route.Home.route) { inclusive = true }
                            }
                        }
                    )
                }

                // -------- Orders --------
                composable(Route.OrderHistory.route) {
                    OrderHistoryScreen(
                        onOrderClick = { orderId ->
                            navController.navigate(Route.OrderTracking.createRoute(orderId))
                        },
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable(
                    route = Route.OrderTracking.route,
                    arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val orderId =
                        backStackEntry.arguments?.getString("orderId") ?: return@composable

                    OrderTrackingScreen(
                        orderId = orderId,
                        onBackClick = { navController.navigateUp() }
                    )
                }

                // -------- Admin --------
                composable(Route.AdminDashboard.route) {
                    AdminDashboardScreen(
                        onNavigateToOrders = {
                            navController.navigate(Route.AdminOrders.route)
                        },
                        onNavigateToProducts = {
                            navController.navigate(Route.AdminProducts.route)
                        },
                        onNavigateToAddProduct = {
                            navController.navigate(Route.AddProduct.route)
                        },
                        onNavigateToOrderDetail = { orderId ->
                            navController.navigate(Route.AdminOrderDetail.createRoute(orderId))
                        },
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable(Route.AdminOrders.route) {
                    AdminOrdersScreen(
                        onOrderClick = { orderId ->
                            navController.navigate(Route.AdminOrderDetail.createRoute(orderId))
                        },
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable(
                    route = Route.AdminOrderDetail.route,
                    arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val orderId =
                        backStackEntry.arguments?.getString("orderId") ?: return@composable

                    AdminOrderDetailScreen(
                        orderId = orderId,
                        onNavigateToCustomer = { userId ->
                            navController.navigate(Route.CustomerDetails.createRoute(userId))
                        },
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable(
                    route = Route.CustomerDetails.route,
                    arguments = listOf(navArgument("userId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val userId =
                        backStackEntry.arguments?.getString("userId") ?: return@composable

                    CustomerDetailsScreen(
                        userId = userId,
                        onOrderClick = { orderId ->
                            navController.navigate(Route.AdminOrderDetail.createRoute(orderId))
                        },
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable(Route.AdminProducts.route) {
                    AdminProductsScreen(
                        onBackClick = { navController.navigateUp() }
                    )
                }

                composable(Route.AddProduct.route) {
                    AddProductScreen(
                        onProductAdded = { navController.navigateUp() },
                        onBackClick = { navController.navigateUp() }
                    )
                }
            }
        }
    }
}

// -------------------- MAIN SCREEN (BOTTOM NAV) --------------------
@Composable
fun MainScreen(
    navController: NavHostController,
    currentUser: com.depi.drlist.data.model.User?,
    themeViewModel: com.depi.drlist.ui.theme.ThemeViewModel,
    onSignOut: () -> Unit
) {
    val mainNavController = rememberNavController()
    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Cart,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy
                            ?.any { it.route == item.route } == true,
                        onClick = {
                            mainNavController.navigate(item.route) {
                                popUpTo(mainNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = mainNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(padding)
        ) {

            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onCartClick = {
                        mainNavController.navigate(BottomNavItem.Cart.route)
                    },
                    onProductClick = { product ->
                        navController.navigate(
                            Route.ProductDetail.createRoute(product.id)
                        )
                    },
                    onAdminDashboardClick = {
                        navController.navigate(Route.AdminDashboard.route)
                    },
                    isAdmin = currentUser?.role == "admin"
                )
            }

            composable(BottomNavItem.Search.route) {
                SearchScreen(
                    onBackClick = {
                        mainNavController.navigate(BottomNavItem.Home.route)
                    },
                    onProductClick = { product ->
                        navController.navigate(
                            Route.ProductDetail.createRoute(product.id)
                        )
                    },
                    onAddToCartClick = {}
                )
            }

            composable(BottomNavItem.Cart.route) {
                CartScreen(
                    onBackClick = {
                        mainNavController.navigate(BottomNavItem.Home.route)
                    },
                    onCheckoutClick = {
                        navController.navigate(Route.Checkout.route)
                    },
                    onBrowseProductsClick = {
                        mainNavController.navigate(BottomNavItem.Home.route)
                    }
                )
            }

            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onBackClick = {
                        mainNavController.navigate(BottomNavItem.Home.route)
                    },
                    onSignOutClick = onSignOut,
                    onViewOrderHistory = {
                        navController.navigate(Route.OrderHistory.route)
                    },
                    themeViewModel = themeViewModel
                )
            }
        }
    }
}