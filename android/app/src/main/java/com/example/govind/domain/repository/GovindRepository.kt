package com.example.govind.domain.repository

import com.example.govind.data.model.Category
import com.example.govind.data.model.Product
import com.example.govind.data.model.Cart
import kotlinx.coroutines.flow.Flow

interface GovindRepository {
    fun getFreshBoardProducts(): Flow<List<Product>>
    fun getCategories(): Flow<List<Category>>
    fun getFeaturedProducts(): Flow<List<Product>>
    fun getProductsByCategory(categoryId: String): Flow<List<Product>>
    fun getProductDetails(productId: String): Flow<Product?>
    
    // Cart operations
    fun getCart(): Flow<Cart?>
    suspend fun addToCart(product: Product, quantity: Int)
    suspend fun removeFromCart(cartItemId: String)
    suspend fun updateCartItemQuantity(cartItemId: String, quantity: Int)
    
    // Checkout operations
    fun getAddresses(): Flow<List<com.example.govind.data.model.Address>>
    suspend fun placeOrder(addressId: String, paymentMethod: String): Result<com.example.govind.data.model.Order>
    
    // Order history
    fun getOrders(): Flow<List<com.example.govind.data.model.Order>>
    
    // Auth operations
    suspend fun signUp(email: String, password: String): Result<Unit>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun logout()
    fun isUserLoggedIn(): Boolean
    fun getUserId(): String?
    fun isLoggedIn(): Boolean
}
