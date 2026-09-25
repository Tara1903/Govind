package com.example.govind.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.govind.ui.features.cart.CartScreen
import com.example.govind.ui.features.checkout.CheckoutScreen
import com.example.govind.ui.features.home.HomeScreen
import com.example.govind.ui.features.onboarding.OnboardingScreen
import com.example.govind.ui.features.orders.OrdersScreen
import com.example.govind.ui.features.product.ProductDetailsScreen
import com.example.govind.ui.features.profile.ProfileScreen
import com.example.govind.ui.features.splash.SplashScreen

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem(Screen.Home.route, "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Search : BottomNavItem(Screen.Search.route, "Search", Icons.Filled.Search, Icons.Outlined.Search)
    object Orders : BottomNavItem(Screen.Orders.route, "Orders", Icons.Filled.List, Icons.Outlined.List)
    object Profile : BottomNavItem(Screen.Profile.route, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarDestinations = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Orders,
        BottomNavItem.Profile
    )

    val showBottomBar = bottomBarDestinations.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    bottomBarDestinations.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinish = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                    onNavigateToProduct = { productId -> navController.navigate(Screen.ProductDetails.createRoute(productId)) },
                    onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                )
            }

            composable(Screen.Search.route) {
                com.example.govind.ui.features.search.SearchScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToProduct = { productId -> navController.navigate(Screen.ProductDetails.createRoute(productId)) }
                )
            }

            composable(Screen.Orders.route) {
                OrdersScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToOrders = { navController.navigate(Screen.Orders.route) }
                )
            }

            composable(Screen.ProductDetails.route) {
                ProductDetailsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Cart.route) {
                CartScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCheckout = { navController.navigate(Screen.Checkout.route) }
                )
            }

            composable(Screen.Checkout.route) {
                CheckoutScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                )
            }
        }
    }
}
