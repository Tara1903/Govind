package com.example.govind.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth")
    
    // Fresh
    object Home : Screen("home")
    object Search : Screen("search?query={query}") { fun createRoute(query: String) = "search?query=$query" }
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
    object AddAddress : Screen("add_address")
    object Orders : Screen("orders")
    object OrderDetails : Screen("order_details/{orderId}") {
        fun createRoute(orderId: String) = "order_details/$orderId"
    }
    object Profile : Screen("profile")
    object Favorites : Screen("favorites")
    object SavedAddresses : Screen("saved_addresses")
    object Support : Screen("support")
    object Legal : Screen("legal?tab={tab}") {
        fun createRoute(tab: String = "privacy") = "legal?tab=$tab"
    }

    // Delivery Partner
    object DeliveryPartner : Screen("delivery_partner")
}
