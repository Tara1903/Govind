package com.example.govind.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.govind.theme.GovindTheme
import com.example.govind.ui.features.cart.CartScreen
import com.example.govind.ui.features.cart.CartViewModel
import com.example.govind.ui.features.checkout.AddAddressScreen
import com.example.govind.ui.features.checkout.CheckoutScreen
import com.example.govind.ui.features.address.SavedAddressesScreen
import com.example.govind.ui.features.favorites.FavoritesScreen
import com.example.govind.ui.features.home.HomeScreen
import com.example.govind.ui.features.kitchen.KitchenHomeScreen
import com.example.govind.ui.features.kitchen.KitchenMenuScreen
import com.example.govind.ui.features.onboarding.OnboardingScreen
import com.example.govind.ui.features.orders.OrdersScreen
import com.example.govind.ui.features.product.ProductDetailsScreen
import com.example.govind.ui.features.profile.ProfileScreen
import com.example.govind.ui.features.splash.SplashScreen
import com.example.govind.ui.features.support.LegalScreen
import com.example.govind.ui.features.support.SupportScreen
import com.example.govind.ui.features.wholesale.WholesaleCatalogScreen
import com.example.govind.ui.features.wholesale.WholesaleHomeScreen
import com.example.govind.ui.shared.GovindCartDock

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem(Screen.Home.route, "Home", Icons.Filled.Storefront, Icons.Outlined.Storefront)
    object Explore : BottomNavItem(Screen.Search.route, "Explore", Icons.Filled.GridView, Icons.Outlined.GridView)
    object Orders : BottomNavItem(Screen.Orders.route, "Orders", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong)
    object Cart : BottomNavItem(Screen.Cart.route, "Cart", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart)
    object Profile : BottomNavItem(Screen.Profile.route, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun MainAppScreen(
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentExperience by AppState.currentExperience.collectAsState()
    val cartUiState by cartViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        cartViewModel.loadCart()
    }

    LaunchedEffect(currentExperience) {
        val destination = when (currentExperience) {
            GovindExperience.FRESH -> Screen.Home.route
            GovindExperience.KITCHEN -> Screen.KitchenHome.route
            GovindExperience.WHOLESALE -> Screen.WholesaleHome.route
        }
        if (currentDestination?.route != destination &&
            currentDestination?.route != Screen.Splash.route &&
            currentDestination?.route != Screen.Auth.route &&
            currentDestination?.route != Screen.Onboarding.route
        ) {
            navController.navigate(destination) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    // Stitch 5-tab unified bottom navigation
    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Explore,
        BottomNavItem.Orders,
        BottomNavItem.Cart,
        BottomNavItem.Profile
    )

    // Screens where bottom bar should show
    val isHomeRoute = currentDestination?.route == Screen.Home.route ||
            currentDestination?.route == Screen.KitchenHome.route ||
            currentDestination?.route == Screen.KitchenMenu.route ||
            currentDestination?.route == Screen.WholesaleHome.route ||
            currentDestination?.route == Screen.WholesaleCatalog.route

    val showBottomBar = isHomeRoute ||
            currentDestination?.route == Screen.Search.route ||
            currentDestination?.route == Screen.Orders.route ||
            currentDestination?.route == Screen.Profile.route

    val totalCartItems = cartUiState.cart?.items?.sumOf { it.quantity } ?: 0
    val showCartDock = isHomeRoute && totalCartItems > 0

    Scaffold(
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Floating Cart Dock right above bottom navigation per Stitch
                if (showCartDock) {
                    GovindCartDock(
                        itemCount = totalCartItems,
                        totalPrice = cartUiState.totalAmount,
                        savingsText = if (cartUiState.totalSavings > 0) {
                            "Unified Cart • Save ₹${cartUiState.totalSavings.toInt()}"
                        } else {
                            "Fresh + Kitchen + Wholesale"
                        },
                        onViewCartClick = { navController.navigate(Screen.Cart.route) }
                    )
                }

                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tonalElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        bottomNavItems.forEach { item ->
                            val isSelected = when (item) {
                                BottomNavItem.Home -> isHomeRoute
                                else -> currentDestination?.hierarchy?.any { it.route == item.route } == true
                            }

                            NavigationBarItem(
                                icon = {
                                    Box {
                                        Icon(
                                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.title,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        if (item == BottomNavItem.Cart && totalCartItems > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .align(Alignment.TopEnd)
                                                    .offset(x = 8.dp, y = (-6).dp)
                                                    .clip(CircleShape)
                                                    .background(GovindTheme.colors.secondary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = totalCartItems.toString(),
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                selected = isSelected,
                                onClick = {
                                    val targetRoute = when (item) {
                                        BottomNavItem.Home -> when (currentExperience) {
                                            GovindExperience.FRESH -> Screen.Home.route
                                            GovindExperience.KITCHEN -> Screen.KitchenHome.route
                                            GovindExperience.WHOLESALE -> Screen.WholesaleHome.route
                                        }
                                        else -> item.route
                                    }
                                    navController.navigate(targetRoute) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primaryContainer,
                                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
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
                        val destination = when (currentExperience) {
                            GovindExperience.FRESH -> Screen.Home.route
                            GovindExperience.KITCHEN -> Screen.KitchenHome.route
                            GovindExperience.WHOLESALE -> Screen.WholesaleHome.route
                        }
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToDeliveryPartner = {
                        navController.navigate(Screen.DeliveryPartner.route) {
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
                val destination = when (currentExperience) {
                    GovindExperience.FRESH -> Screen.Home.route
                    GovindExperience.KITCHEN -> Screen.KitchenHome.route
                    GovindExperience.WHOLESALE -> Screen.WholesaleHome.route
                }
                com.example.govind.ui.features.auth.AuthScreen(
                    onContinueAsGuest = {
                        navController.navigate(destination) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onAuthSuccess = { userRole ->
                        if (userRole == "delivery") {
                            navController.navigate(Screen.DeliveryPartner.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        } else if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else {
                            navController.navigate(destination) {
                                popUpTo(Screen.Auth.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                    onNavigateToProduct = { productId -> navController.navigate(Screen.ProductDetails.createRoute(productId)) },
                    onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onNavigateToRateList = { navController.navigate(Screen.RateList.route) }
                )
            }

            composable(Screen.KitchenHome.route) {
                KitchenHomeScreen(
                    onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                    onNavigateToMenu = { navController.navigate(Screen.KitchenMenu.route) }
                )
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

            composable(Screen.RateList.route) {
                com.example.govind.ui.features.ratelist.RateListScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Orders.route) {
                val destination = when (currentExperience) {
                    GovindExperience.FRESH -> Screen.Home.route
                    GovindExperience.KITCHEN -> Screen.KitchenHome.route
                    GovindExperience.WHOLESALE -> Screen.WholesaleHome.route
                }
                com.example.govind.ui.features.orders.OrdersScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToOrderDetails = { orderId -> navController.navigate(Screen.OrderDetails.createRoute(orderId)) },
                    onNavigateToAuth = { navController.navigate(Screen.Auth.route) },
                    onNavigateToHome = { navController.navigate(destination) }
                )
            }

            composable(
                route = Screen.OrderDetails.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
                com.example.govind.ui.features.orders.OrderDetailsScreen(
                    orderId = orderId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToOrders = { navController.navigate(Screen.Orders.route) },
                    onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                    onNavigateToSavedAddresses = { navController.navigate(Screen.SavedAddresses.route) },
                    onNavigateToAddAddress = { navController.navigate(Screen.AddAddress.route) },
                    onNavigateToSupport = { navController.navigate(Screen.Support.route) },
                    onNavigateToLegal = { tab -> navController.navigate(Screen.Legal.createRoute(tab)) },
                    onNavigateToDeliveryPartner = { navController.navigate(Screen.DeliveryPartner.route) },
                    onNavigateToAuth = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToProduct = { productId -> navController.navigate(Screen.ProductDetails.createRoute(productId)) },
                    onNavigateToAuth = { navController.navigate(Screen.Auth.route) }
                )
            }

            composable(Screen.SavedAddresses.route) {
                SavedAddressesScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddAddress = { navController.navigate(Screen.AddAddress.route) }
                )
            }

            composable(Screen.Support.route) {
                SupportScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Legal.route,
                arguments = listOf(navArgument("tab") {
                    type = NavType.StringType
                    defaultValue = "privacy"
                })
            ) { backStackEntry ->
                val tab = backStackEntry.arguments?.getString("tab") ?: "privacy"
                LegalScreen(
                    initialTab = tab,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.DeliveryPartner.route) {
                com.example.govind.ui.features.delivery.DeliveryPartnerScreen(
                    onLogoutSuccess = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.AddAddress.route) {
                AddAddressScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.ProductDetails.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                ProductDetailsScreen(
                    productId = productId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAuth = { navController.navigate(Screen.Auth.route) }
                )
            }

            composable(Screen.Cart.route) {
                CartScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCheckout = { navController.navigate(Screen.Checkout.route) }
                )
            }

            composable(Screen.Checkout.route) {
                val destination = when (currentExperience) {
                    GovindExperience.FRESH -> Screen.Home.route
                    GovindExperience.KITCHEN -> Screen.KitchenHome.route
                    GovindExperience.WHOLESALE -> Screen.WholesaleHome.route
                }
                CheckoutScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddAddress = { navController.navigate(Screen.AddAddress.route) },
                    onNavigateToAuth = { navController.navigate(Screen.Auth.route) },
                    onNavigateToOrderDetails = { orderId -> navController.navigate(Screen.OrderDetails.createRoute(orderId)) },
                    onNavigateToHome = {
                        navController.navigate(destination) {
                            popUpTo(destination) { inclusive = false }
                        }
                    }
                )
            }
        }
    }
}
