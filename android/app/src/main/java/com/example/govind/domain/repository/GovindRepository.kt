package com.example.govind.domain.repository

import com.example.govind.data.model.Category
import com.example.govind.data.model.Product
import com.example.govind.data.model.Cart
import kotlinx.coroutines.flow.Flow

interface GovindRepository {
    fun getFreshBoardProducts(): Flow<List<Product>>
    fun getCategories(): Flow<List<Category>>
    fun getFeaturedProducts(): Flow<List<Product>>
    fun getPacks(): Flow<List<Product>>
    fun getCombos(): Flow<List<Product>>
    fun getProductsByCategory(categoryId: String): Flow<List<Product>>
    fun getKitchenMenuItems(): Flow<List<Product>>
    fun getWholesaleItems(): Flow<List<Product>>
    fun getProductDetails(productId: String): Flow<Product?>
    fun searchProducts(query: String): Flow<List<Product>>
    
    // Cart operations
    fun getGlobalCart(): Flow<Cart?>
    suspend fun addToCart(product: Product, quantity: Int, experienceType: String)
    suspend fun removeFromCart(cartItemId: String, experienceType: String)
    suspend fun updateCartItemQuantity(cartItemId: String, experienceType: String, quantity: Int)
    
    // Checkout operations
    fun getDeliverySettings(): Flow<com.example.govind.data.model.DeliverySettings?>
    fun getAddresses(): Flow<List<com.example.govind.data.model.Address>>
    suspend fun addAddress(name: String, phone: String, house: String, street: String, area: String, city: String, pincode: String): Result<Unit>
    suspend fun placeGlobalOrder(addressId: String, paymentMethod: String): Result<com.example.govind.data.model.Order>
    
    // Order history
    fun getOrders(): Flow<List<com.example.govind.data.model.Order>>
    fun getOrderById(orderId: String): Flow<Result<com.example.govind.data.model.Order>>
    
    // Auth operations
    suspend fun verifyOtp(email: String, token: String): Result<Unit>
    suspend fun sendOtp(email: String): Result<Unit>
    suspend fun logout()
    fun isUserLoggedIn(): Boolean
    suspend fun getUserProfile(): Result<com.example.govind.data.model.Profile?>
    suspend fun getProfileById(userId: String): Result<com.example.govind.data.model.Profile?>
    fun getUserRole(): String?
    suspend fun updateUserPhone(phone: String): Result<Unit>
    fun getUserId(): String?
    fun isLoggedIn(): Boolean
    suspend fun getDeliveryPartnerOrders(): Result<List<com.example.govind.data.model.Order>>
    suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit>
    suspend fun markOrderPaid(orderId: String, upiRef: String?): Result<Unit>
    suspend fun updateDeliveryLocation(orderId: String, lat: Double, lng: Double, accuracy: Float, speed: Float, heading: Float): Result<Unit>
    
    // Cloud Cart Sync
    suspend fun syncCartOnLogin()

    // Favorites
    fun getFavorites(): Flow<List<Product>>
    suspend fun toggleFavorite(productId: String): Result<Boolean>
    fun isFavorite(productId: String): Flow<Boolean>

    // Address Management
    suspend fun updateAddress(id: String, name: String, phone: String, house: String, street: String, area: String, landmark: String?, city: String, pincode: String, isDefault: Boolean): Result<Unit>
    suspend fun deleteAddress(id: String): Result<Unit>
    suspend fun setDefaultAddress(id: String): Result<Unit>
}










