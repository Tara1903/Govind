package com.example.govind.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth")
    
    // Fresh
    object Home : Screen("home")
    object Search : Screen("search")
    object RateList : Screen("ratelist")
    object ProductDetails : Screen("product/{productId}") {
        fun createRoute(productId: String) = "product/$productId"
    }
    
    // Kitchen
    object KitchenHome : Screen("kitchen_home")
    object KitchenMenu : Screen("kitchen_menu")
    
    // Wholesale
    object WholesaleHome : Screen("wholesale_home")
    object WholesaleCatalog : Screen("wholesale_catalog")
    
    // Shared
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
    object Orders : Screen("orders")
    object Profile : Screen("profile")
}
