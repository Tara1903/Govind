package com.example.govind.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
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
import com.example.govind.ui.features.kitchen.KitchenHomeScreen
import com.example.govind.ui.features.kitchen.KitchenMenuScreen
import com.example.govind.ui.features.wholesale.WholesaleHomeScreen
import com.example.govind.ui.features.wholesale.WholesaleCatalogScreen

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    // Fresh
    object Home : BottomNavItem(Screen.Home.route, "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Search : BottomNavItem(Screen.Search.route, "Shop", Icons.Filled.Search, Icons.Outlined.Search)
    
    // Kitchen
    object KitchenHome : BottomNavItem(Screen.KitchenHome.route, "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object KitchenMenu : BottomNavItem(Screen.KitchenMenu.route, "Menu", Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List)
    
    // Wholesale
    object WholesaleHome : BottomNavItem(Screen.WholesaleHome.route, "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object WholesaleCatalog : BottomNavItem(Screen.WholesaleCatalog.route, "Catalog", Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List)
    
    // Shared 
    object Cart : BottomNavItem(Screen.Cart.route, "Cart", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart)
    object Orders : BottomNavItem(Screen.Orders.route, "Orders", Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List)
    object Profile : BottomNavItem(Screen.Profile.route, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentExperience by AppState.currentExperience.collectAsState()

    val bottomBarDestinations = when (currentExperience) {
        GovindExperience.FRESH -> listOf(
            BottomNavItem.Home,
            BottomNavItem.Search,
            BottomNavItem.Orders,
            BottomNavItem.Cart,
            BottomNavItem.Profile
        )
        GovindExperience.KITCHEN -> listOf(
            BottomNavItem.KitchenHome,
            BottomNavItem.KitchenMenu,
            BottomNavItem.Orders,
            BottomNavItem.Cart,
            BottomNavItem.Profile
        )
        GovindExperience.WHOLESALE -> listOf(
            BottomNavItem.WholesaleHome,
            BottomNavItem.WholesaleCatalog,
            BottomNavItem.Orders,
            BottomNavItem.Cart,
            BottomNavItem.Profile
        )
    }

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
                    },
                    onNavigateToAuth = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinish = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Auth.route) {
                com.example.govind.ui.features.auth.AuthScreen(
                    onAuthSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
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
            
            composable(Screen.KitchenHome.route) {
                KitchenHomeScreen()
            }
            
            composable(Screen.KitchenMenu.route) {
                KitchenMenuScreen()
            }
            
            composable(Screen.WholesaleHome.route) {
                WholesaleHomeScreen()
            }
            
            composable(Screen.WholesaleCatalog.route) {
                WholesaleCatalogScreen()
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
