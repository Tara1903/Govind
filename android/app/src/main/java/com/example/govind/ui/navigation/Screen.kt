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
    object Notifications : Screen("notifications")

    // StarPay Gateway
    object StarPay : Screen("starpay?amount={amount}&orderId={orderId}&orderRef={orderRef}&description={description}&customerName={customerName}&customerEmail={customerEmail}&customerPhone={customerPhone}&internalOrderId={internalOrderId}") {
        fun createRoute(
            amount: Double,
            orderId: String = "",
            orderRef: String = "",
            description: String = "",
            customerName: String = "",
            customerEmail: String = "",
            customerPhone: String = "",
            internalOrderId: String = ""
        ): String {
            val encDesc = try { java.net.URLEncoder.encode(description, "UTF-8") } catch (_: Exception) { "" }
            val encName = try { java.net.URLEncoder.encode(customerName, "UTF-8") } catch (_: Exception) { "" }
            val encEmail = try { java.net.URLEncoder.encode(customerEmail, "UTF-8") } catch (_: Exception) { "" }
            return "starpay?amount=$amount&orderId=$orderId&orderRef=$orderRef&description=$encDesc&customerName=$encName&customerEmail=$encEmail&customerPhone=$customerPhone&internalOrderId=$internalOrderId"
        }
    }

    // Delivery Partner
    object DeliveryPartner : Screen("delivery_partner")
}
