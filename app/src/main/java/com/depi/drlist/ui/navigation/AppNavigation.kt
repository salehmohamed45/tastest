package com.depi.drlist.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.depi.drlist.ui.screens.cart.CartScreen
import com.depi.drlist.ui.screens.checkout.CheckoutScreen
import com.depi.drlist.ui.screens.home.HomeScreen
import com.depi.drlist.ui.screens.login.LoginScreen
import com.depi.drlist.ui.screens.login.LoginViewModel
import com.depi.drlist.ui.screens.profile.ProfileScreen
import com.depi.drlist.ui.screens.reset.PasswordResetScreen
import com.depi.drlist.ui.screens.reset.PasswordResetViewModel

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

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel()
    val currentUser by loginViewModel.currentUser.collectAsState()

    val startDestination = if (currentUser != null) Route.Home.route else Route.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
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
            val passwordResetViewModel: PasswordResetViewModel = viewModel()
            PasswordResetScreen(
                viewModel = passwordResetViewModel,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        composable(Route.Home.route) {
            MainScreen(
                navController = navController,
                onSignOut = {
                    loginViewModel.signOut()
                    navController.navigate(Route.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

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
    }
}

@Composable
fun MainScreen(
    navController: androidx.navigation.NavHostController,
    onSignOut: () -> Unit
) {
    val mainNavController = rememberNavController()
    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Cart,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
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
    ) { innerPadding ->
        NavHost(
            navController = mainNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onCartClick = {
                        mainNavController.navigate(BottomNavItem.Cart.route)
                    },
                    onProductClick = { product ->
                        // Could navigate to product detail screen if needed
                    }
                )
            }

            composable(BottomNavItem.Search.route) {
                SearchScreen(
                    onBackClick = {
                        mainNavController.navigate(BottomNavItem.Home.route) {
                            popUpTo(BottomNavItem.Home.route) { inclusive = true }
                        }
                    },
                    onProductClick = { product ->
                        // Could navigate to product detail screen if needed
                    },
                    onAddToCartClick = { product ->
                        // Add to cart handled in SearchViewModel
                    }
                )
            }

            composable(BottomNavItem.Cart.route) {
                CartScreen(
                    onBackClick = {
                        mainNavController.navigate(BottomNavItem.Home.route) {
                            popUpTo(BottomNavItem.Home.route) { inclusive = true }
                        }
                    },
                    onCheckoutClick = {
                        navController.navigate(Route.Checkout.route)
                    },
                    onBrowseProductsClick = {
                        mainNavController.navigate(BottomNavItem.Home.route) {
                            popUpTo(BottomNavItem.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onBackClick = {
                        mainNavController.navigate(BottomNavItem.Home.route) {
                            popUpTo(BottomNavItem.Home.route) { inclusive = true }
                        }
                    },
                    onSignOutClick = onSignOut
                )
            }
        }
    }
}
