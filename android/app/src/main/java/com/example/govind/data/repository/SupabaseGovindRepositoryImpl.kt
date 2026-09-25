package com.example.govind.data.repository

import com.example.govind.data.local.CartDao
import com.example.govind.data.local.CartEntity
import com.example.govind.data.local.SessionManager
import com.example.govind.data.model.*
import com.example.govind.data.remote.SupabaseApi
import com.example.govind.domain.repository.GovindRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseGovindRepositoryImpl @Inject constructor(
    private val api: SupabaseApi,
    private val cartDao: CartDao,
    private val sessionManager: SessionManager
) : GovindRepository {
    
    // Auth
    override suspend fun signUp(email: String, password: String): Result<Unit> {
        return try {
            val response = api.signUp(AuthRequest(email, password))
            sessionManager.accessToken = response.accessToken
            sessionManager.refreshToken = response.refreshToken
            sessionManager.userId = response.user.id
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = api.login(AuthRequest(email, password))
            sessionManager.accessToken = response.accessToken
            sessionManager.refreshToken = response.refreshToken
            sessionManager.userId = response.user.id
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun logout() {
        sessionManager.clearSession()
        cartDao.clearCart()
    }
    
    override fun isUserLoggedIn(): Boolean = sessionManager.isLoggedIn
    override fun getUserId(): String? = sessionManager.userId
    override fun isLoggedIn(): Boolean = sessionManager.isLoggedIn

    // Products
    override fun getFreshBoardProducts(): Flow<List<Product>> = flow {
        emit(api.getFreshBoardProducts())
    }

    override fun getCategories(): Flow<List<Category>> = flow { emit(api.getCategories()) }
    override fun getFeaturedProducts(): Flow<List<Product>> = flow { emit(api.getFeaturedProducts()) }
    override fun getProductsByCategory(categoryId: String): Flow<List<Product>> = flow { emit(api.getProductsByCategory("eq.$categoryId")) }
    override fun getProductDetails(productId: String): Flow<Product?> = flow { emit(api.getProductDetails("eq.$productId").firstOrNull()) }

    // Cart
    override fun getCart(): Flow<Cart?> = cartDao.getCartItems().map { entities ->
        Cart(
            id = "local_cart",
            profileId = sessionManager.userId ?: "guest",
            items = entities.map { it.toCartItem() }
        )
    }

    override suspend fun addToCart(product: Product, quantity: Int) {
        val existingItem = cartDao.getCartItem(product.id)
        if (existingItem != null) {
            cartDao.updateQuantity(product.id, existingItem.quantity + quantity)
        } else {
            val entity = CartEntity.fromCartItem(CartItem(
                id = product.id,
                cartId = "local_cart",
                product = product,
                quantity = quantity
            ))
            cartDao.insertItem(entity)
        }
    }

    override suspend fun removeFromCart(cartItemId: String) {
        cartDao.deleteItem(cartItemId)
    }

    override suspend fun updateCartItemQuantity(cartItemId: String, quantity: Int) {
        if (quantity <= 0) {
            cartDao.deleteItem(cartItemId)
        } else {
            cartDao.updateQuantity(cartItemId, quantity)
        }
    }

    // Addresses
    override fun getAddresses(): Flow<List<Address>> = flow {
        val uid = sessionManager.userId
        if (uid != null) {
            try {
                emit(api.getAddresses("eq.$uid"))
            } catch (e: Exception) {
                emit(emptyList())
            }
        } else {
            emit(emptyList())
        }
    }

    // Order
    override suspend fun placeOrder(addressId: String, paymentMethod: String): Result<Order> {
        val cartEntities = cartDao.getCartItems().first()
        val cartItems = cartEntities.map { it.toCartItem() }
        if (cartItems.isEmpty()) return Result.failure(Exception("Cart is empty"))
        
        val subtotal = cartItems.sumOf { it.product.sellingPrice * it.quantity }
        val deliveryCharge = if (subtotal > 200) 0.0 else 30.0
        val total = subtotal + deliveryCharge
        
        val itemsJson = kotlinx.serialization.json.buildJsonArray {
            cartItems.forEach { item ->
                add(kotlinx.serialization.json.buildJsonObject {
                    put("product_id", kotlinx.serialization.json.JsonPrimitive(item.product.id))
                    put("product_name", kotlinx.serialization.json.JsonPrimitive(item.product.name))
                    put("unit", kotlinx.serialization.json.JsonPrimitive(item.product.unit))
                    put("price", kotlinx.serialization.json.JsonPrimitive(item.product.sellingPrice))
                    put("quantity", kotlinx.serialization.json.JsonPrimitive(item.quantity))
                    put("discount", kotlinx.serialization.json.JsonPrimitive(
                        if (item.product.price > item.product.sellingPrice) 
                            item.product.price - item.product.sellingPrice 
                        else 0.0
                    ))
                })
            }
        }
        
        val request = kotlinx.serialization.json.buildJsonObject {
            put("p_customer_id", kotlinx.serialization.json.JsonPrimitive(sessionManager.userId))
            put("p_subtotal", kotlinx.serialization.json.JsonPrimitive(subtotal))
            put("p_discount", kotlinx.serialization.json.JsonPrimitive(0.0))
            put("p_coupon_id", kotlinx.serialization.json.JsonPrimitive(null as String?))
            put("p_delivery_charge", kotlinx.serialization.json.JsonPrimitive(deliveryCharge))
            put("p_total", kotlinx.serialization.json.JsonPrimitive(total))
            put("p_savings", kotlinx.serialization.json.JsonPrimitive(0.0))
            put("p_address_snapshot", kotlinx.serialization.json.buildJsonObject { put("id", kotlinx.serialization.json.JsonPrimitive(addressId)) })
            put("p_payment_method", kotlinx.serialization.json.JsonPrimitive(paymentMethod))
            put("p_items", itemsJson)
        }

        return try {
            val orderId = api.createOrderRpc(request).replace("\"", "")
            
            if (paymentMethod == "ONLINE") {
                val razorpayOrder = api.createRazorpayOrder(mapOf("amount" to total))
                // Note: The backend should ideally link razorpay order with supabase order ID.
                // We're returning the order to the UI.
            }
            cartDao.clearCart()
            Result.success(Order(
                id = orderId,
                customerId = sessionManager.userId ?: "guest",
                subtotal = subtotal,
                total = total,
                paymentMethod = paymentMethod,
                paymentStatus = "PENDING",
                orderStatus = if (paymentMethod == "ONLINE") "INITIATED" else "CONFIRMED"
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getOrders(): Flow<List<Order>> = flow {
        val uid = sessionManager.userId
        if (uid != null) {
            try {
                emit(api.getOrders("eq.$uid"))
            } catch (e: Exception) {
                emit(emptyList())
            }
        } else {
            emit(emptyList())
        }
    }
}
