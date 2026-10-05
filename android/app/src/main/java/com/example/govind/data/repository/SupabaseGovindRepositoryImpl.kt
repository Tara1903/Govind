package com.example.govind.data.repository

import com.example.govind.data.local.CartDao
import com.example.govind.data.local.CartEntity
import com.example.govind.data.local.SessionManager
import com.example.govind.data.model.*
import com.example.govind.data.remote.SupabaseApi
import com.example.govind.data.remote.PaymentGatewayApi
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
    private val paymentGatewayApi: com.example.govind.data.remote.PaymentGatewayApi,
    private val api: SupabaseApi,
    private val cartDao: CartDao,
    private val sessionManager: SessionManager,
    private val realtimeManager: com.example.govind.data.remote.SupabaseRealtimeManager
) : GovindRepository {
    
    // Auth
    override suspend fun verifyOtp(email: String, token: String): Result<Unit> {
        return try {
            val response = api.verifyOtp(com.example.govind.data.model.VerifyOtpRequest(type = "email", email = email, token = token))
            sessionManager.accessToken = response.accessToken
            sessionManager.refreshToken = response.refreshToken
            sessionManager.userId = response.user.id
            sessionManager.userEmail = response.user.email
            sessionManager.isGuest = false
            try {
                val profiles = api.getProfile("eq." + response.user.id)
                val profile = profiles.firstOrNull()
                if (profile != null) {
                    sessionManager.userRole = profile.role
                }
            } catch (ignored: Exception) {}
            try {
                syncCartOnLogin()
            } catch (e: Exception) {
                android.util.Log.e("GovindCartSync", "Error syncing cart on login: ${e.message}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun sendOtp(email: String): Result<Unit> {
        return try {
            api.sendOtp(com.example.govind.data.model.SendOtpRequest(email = email, createUser = true))
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
    override fun getUserRole(): String? = sessionManager.userRole

    override suspend fun getUserProfile(): Result<com.example.govind.data.model.Profile?> {
        return try {
            val userId = sessionManager.userId ?: throw Exception("User not logged in")
            val profiles = api.getProfile("eq.$userId")
            val profile = profiles.firstOrNull()
            if (profile != null) {
                sessionManager.userRole = profile.role
            }
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProfileById(userId: String): Result<com.example.govind.data.model.Profile?> {
        return try {
            val profiles = api.getProfile("eq.$userId")
            Result.success(profiles.firstOrNull())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserPhone(phone: String): Result<Unit> {
        return try {
            val userId = sessionManager.userId ?: throw Exception("User not logged in")
            val updates = kotlinx.serialization.json.buildJsonObject {
                put("phone", kotlinx.serialization.json.JsonPrimitive(phone))
            }
            api.updateProfile("eq.$userId", updates)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Products
    override fun getFreshBoardProducts(): Flow<List<Product>> = flow {
        emit(api.getFreshBoardProducts())
    }

    override fun getCategories(): Flow<List<Category>> = flow { emit(api.getCategories()) }
    override fun getFeaturedProducts(): Flow<List<Product>> = flow { emit(api.getFeaturedProducts()) }
    override fun getPacks(): Flow<List<Product>> = flow { emit(api.getProductsByType("eq.PACK")) }
    override fun getCombos(): Flow<List<Product>> = flow { emit(api.getProductsByType("eq.COMBO")) }
    override fun getProductsByCategory(categoryId: String): Flow<List<Product>> = flow { emit(api.getProductsByCategory("eq.$categoryId")) }
    override fun getProductDetails(productId: String): Flow<Product?> = flow { emit(api.getProductDetails("eq.$productId").firstOrNull()) }
    override fun searchProducts(query: String): Flow<List<Product>> = flow { emit(api.searchProducts("ilike.%$query%")) }

    // Cart
    override fun getGlobalCart(): Flow<Cart?> = cartDao.getCartItems().map { entities ->
        Cart(
            id = "global_cart",
            profileId = sessionManager.userId ?: "guest",
            items = entities.map { it.toCartItem() }
        )
    }

    override suspend fun addToCart(product: Product, quantity: Int, experienceType: String) {
        val existingItem = cartDao.getCartItemForExperience(product.id, experienceType)
        if (existingItem != null) {
            cartDao.updateQuantity(product.id, experienceType, existingItem.quantity + quantity)
        } else {
            val entity = CartEntity(
                id = product.id,
                productId = product.id,
                name = product.name,
                slug = product.slug,
                price = product.price,
                sellingPrice = product.sellingPrice,
                description = product.description,
                imageUrl = product.imageUrl,
                categoryId = product.categoryId,
                unit = product.unit,
                quantity = quantity,
                experienceType = experienceType,
                bundleItemsJson = product.bundleItems?.toString(),
                productType = product.productType
            )
            cartDao.insertItem(entity)
        }
    }

    override suspend fun removeFromCart(cartItemId: String, experienceType: String) {
        cartDao.deleteItem(cartItemId, experienceType)
        val uid = sessionManager.userId
        if (!uid.isNullOrBlank() && sessionManager.isLoggedIn) {
            syncItemToCloud(cartItemId, experienceType, 0)
        }
    }

    override suspend fun updateCartItemQuantity(cartItemId: String, experienceType: String, quantity: Int) {
        if (quantity <= 0) {
            cartDao.deleteItem(cartItemId, experienceType)
        } else {
            cartDao.updateQuantity(cartItemId, experienceType, quantity)
        }
        val uid = sessionManager.userId
        if (!uid.isNullOrBlank() && sessionManager.isLoggedIn) {
            syncItemToCloud(cartItemId, experienceType, quantity)
        }
    }

    // Delivery Settings
    override fun getDeliverySettings(): Flow<com.example.govind.data.model.DeliverySettings?> = flow {
        try {
            val settings = api.getDeliverySettings()
            emit(settings.firstOrNull())
        } catch (e: Exception) {
            emit(null)
        }
    }

    // Addresses
    override fun getAddresses(): Flow<List<com.example.govind.data.model.Address>> = flow {
        val uid = sessionManager.userId
        if (uid != null) {
            try {
                emit(api.getAddresses("eq.$uid"))
            } catch (e: Exception) {
                emit(emptyList())
            }

            realtimeManager.subscribeTable("addresses").collect {
                try {
                    emit(api.getAddresses("eq.$uid"))
                } catch (_: Exception) {}
            }
        } else {
            emit(emptyList())
        }
    }

    override suspend fun addAddress(name: String, phone: String, house: String, street: String, area: String, city: String, pincode: String): Result<Unit> {
        return try {
            val uid = sessionManager.userId ?: throw Exception("Not logged in")
            val addressJson = kotlinx.serialization.json.buildJsonObject {
                put("profile_id", kotlinx.serialization.json.JsonPrimitive(uid))
                put("name", kotlinx.serialization.json.JsonPrimitive(name))
                put("phone", kotlinx.serialization.json.JsonPrimitive(phone))
                put("house", kotlinx.serialization.json.JsonPrimitive(house))
                put("street", kotlinx.serialization.json.JsonPrimitive(street))
                put("area", kotlinx.serialization.json.JsonPrimitive(area))
                put("city", kotlinx.serialization.json.JsonPrimitive(city))
                put("pincode", kotlinx.serialization.json.JsonPrimitive(pincode))
            }
            api.addAddress(addressJson)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAddress(
        id: String, name: String, phone: String, house: String,
        street: String, area: String, landmark: String?, city: String,
        pincode: String, isDefault: Boolean
    ): Result<Unit> {
        return try {
            val json = kotlinx.serialization.json.buildJsonObject {
                put("name", kotlinx.serialization.json.JsonPrimitive(name))
                put("phone", kotlinx.serialization.json.JsonPrimitive(phone))
                put("house", kotlinx.serialization.json.JsonPrimitive(house))
                put("street", kotlinx.serialization.json.JsonPrimitive(street))
                put("area", kotlinx.serialization.json.JsonPrimitive(area))
                landmark?.let { put("landmark", kotlinx.serialization.json.JsonPrimitive(it)) }
                put("city", kotlinx.serialization.json.JsonPrimitive(city))
                put("pincode", kotlinx.serialization.json.JsonPrimitive(pincode))
                put("is_default", kotlinx.serialization.json.JsonPrimitive(isDefault))
            }
            api.updateAddress("eq.$id", json)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAddress(id: String): Result<Unit> {
        return try {
            api.deleteAddress("eq.$id")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setDefaultAddress(id: String): Result<Unit> {
        return try {
            val uid = sessionManager.userId ?: throw Exception("Not logged in")
            val falseObj = kotlinx.serialization.json.buildJsonObject {
                put("is_default", kotlinx.serialization.json.JsonPrimitive(false))
            }
            val userAddresses = api.getAddresses("eq.$uid")
            for (addr in userAddresses) {
                if (addr.isDefault && addr.id != id) {
                    api.updateAddress("eq.${addr.id}", falseObj)
                }
            }
            val trueObj = kotlinx.serialization.json.buildJsonObject {
                put("is_default", kotlinx.serialization.json.JsonPrimitive(true))
            }
            api.updateAddress("eq.$id", trueObj)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Order
    override suspend fun placeGlobalOrder(addressId: String, paymentMethod: String): Result<com.example.govind.data.model.Order> {
        val uid = sessionManager.userId
        if (uid.isNullOrBlank() || !sessionManager.isLoggedIn) {
            return Result.failure(Exception("Authentication required. Please sign in to place an order."))
        }

        val cartEntities = cartDao.getCartItems().first()
        val cartItems = cartEntities.map { it.toCartItem() }
        if (cartItems.isEmpty()) return Result.failure(Exception("Your basket is empty. Please add items before checkout."))

        return try {
            // 1. Revalidate products against live Supabase data (prices, active status, stock)
            val productIds = cartItems.map { it.product.id }.distinct()
            val idFilter = "in.(" + productIds.joinToString(",") + ")"
            val liveProducts = api.getProductsByIds(idFilter)
            val liveProductMap = liveProducts.associateBy { it.id }

            for (item in cartItems) {
                val liveProduct = liveProductMap[item.product.id]
                    ?: return Result.failure(Exception("Product '${item.product.name}' is no longer available."))
                if (!liveProduct.active) {
                    return Result.failure(Exception("Product '${liveProduct.name}' is currently unavailable."))
                }
                if (liveProduct.stockQuantity < item.quantity) {
                    return Result.failure(Exception("Insufficient stock for '${liveProduct.name}'. Available: ${liveProduct.stockQuantity}, in cart: ${item.quantity}."))
                }
            }

            // 2. Fetch selected address for snapshotting
            val addresses = api.getAddresses("eq.$uid")
            val selectedAddress = addresses.firstOrNull { it.id == addressId }
                ?: return Result.failure(Exception("Selected delivery address not found. Please choose or add a valid address."))

            val addressSnapshot = kotlinx.serialization.json.buildJsonObject {
                put("id", kotlinx.serialization.json.JsonPrimitive(selectedAddress.id))
                put("name", kotlinx.serialization.json.JsonPrimitive(selectedAddress.name))
                put("phone", kotlinx.serialization.json.JsonPrimitive(selectedAddress.phone))
                put("house", kotlinx.serialization.json.JsonPrimitive(selectedAddress.house))
                put("street", kotlinx.serialization.json.JsonPrimitive(selectedAddress.street))
                put("area", kotlinx.serialization.json.JsonPrimitive(selectedAddress.area))
                selectedAddress.landmark?.let { put("landmark", kotlinx.serialization.json.JsonPrimitive(it)) }
                put("city", kotlinx.serialization.json.JsonPrimitive(selectedAddress.city))
                put("pincode", kotlinx.serialization.json.JsonPrimitive(selectedAddress.pincode))
                selectedAddress.deliveryInstructions?.let { put("delivery_instructions", kotlinx.serialization.json.JsonPrimitive(it)) }
            }

            // 3. Recompute authoritative pricing with live product prices and bulk tiers
            val pricingResults = cartItems.map { item ->
                val liveProduct = liveProductMap[item.product.id] ?: item.product
                val result = com.example.govind.domain.pricing.PricingEngine.calculateProductPrice(
                    basePrice = liveProduct.sellingPrice,
                    quantity = item.quantity,
                    wholesalePricing = liveProduct.getWholesalePricing(),
                    experience = item.experienceType
                )
                Triple(item, liveProduct, result)
            }

            val subtotal = pricingResults.sumOf { it.third.subtotal }
            val totalSavings = pricingResults.sumOf { (item, liveProduct, pricing) ->
                val itemBasePrice = liveProduct.price ?: liveProduct.sellingPrice
                val totalBasePrice = itemBasePrice * item.quantity
                (totalBasePrice - pricing.subtotal).coerceAtLeast(0.0)
            }

            // Canonical delivery fee from delivery_settings: free if subtotal >= 500, else 40
            val deliveryCharge = if (subtotal >= 500.0) 0.0 else 40.0
            val total = subtotal + deliveryCharge

            // Experience type
            val distinctExperiences = cartItems.map { it.experienceType.uppercase() }.distinct()
            val orderExperienceType = if (distinctExperiences.size == 1) distinctExperiences.first() else "MIXED"

            // 4. Build items JSON payload matching RPC contract
            val itemsJson = kotlinx.serialization.json.buildJsonArray {
                pricingResults.forEach { (item, liveProduct, pricing) ->
                    val baseUnitPrice = liveProduct.sellingPrice
                    val discountPerUnit = (baseUnitPrice - pricing.effectiveUnitPrice).coerceAtLeast(0.0)
                    val totalDiscountForLine = discountPerUnit * item.quantity

                    add(kotlinx.serialization.json.buildJsonObject {
                        put("product_id", kotlinx.serialization.json.JsonPrimitive(liveProduct.id))
                        put("product_name", kotlinx.serialization.json.JsonPrimitive(liveProduct.name))
                        put("unit", kotlinx.serialization.json.JsonPrimitive(liveProduct.unit))
                        put("price", kotlinx.serialization.json.JsonPrimitive(pricing.effectiveUnitPrice))
                        put("quantity", kotlinx.serialization.json.JsonPrimitive(item.quantity))
                        put("experience_type", kotlinx.serialization.json.JsonPrimitive(item.experienceType.uppercase()))
                        put("discount", kotlinx.serialization.json.JsonPrimitive(totalDiscountForLine))
                        put("base_price", kotlinx.serialization.json.JsonPrimitive(baseUnitPrice))
                        put("bulk_discount", kotlinx.serialization.json.JsonPrimitive(pricing.totalDiscount))
                        put("effective_unit_price", kotlinx.serialization.json.JsonPrimitive(pricing.effectiveUnitPrice))
                        put("line_total", kotlinx.serialization.json.JsonPrimitive(pricing.subtotal))
                    })
                }
            }

            val request = kotlinx.serialization.json.buildJsonObject {
                put("p_customer_id", kotlinx.serialization.json.JsonPrimitive(uid))
                put("p_subtotal", kotlinx.serialization.json.JsonPrimitive(subtotal))
                put("p_discount", kotlinx.serialization.json.JsonPrimitive(totalSavings))
                put("p_coupon_id", kotlinx.serialization.json.JsonPrimitive(null as String?))
                put("p_tax", kotlinx.serialization.json.JsonPrimitive(0.0))
                put("p_delivery_charge", kotlinx.serialization.json.JsonPrimitive(deliveryCharge))
                put("p_total", kotlinx.serialization.json.JsonPrimitive(total))
                val dbPaymentMethod = if (paymentMethod.contains("STARPAY", ignoreCase = true) || paymentMethod.contains("ONLINE", ignoreCase = true)) "ONLINE" else "COD"
                put("p_payment_method", kotlinx.serialization.json.JsonPrimitive(dbPaymentMethod))
                put("p_items", itemsJson)
                put("p_savings", kotlinx.serialization.json.JsonPrimitive(totalSavings))
                put("p_address_snapshot", addressSnapshot)
                put("p_experience_type", kotlinx.serialization.json.JsonPrimitive(orderExperienceType))
                put("p_address_id", kotlinx.serialization.json.JsonPrimitive(addressId))
            }

            val rawResponse = api.createOrderRpc(request)
            val orderId = rawResponse.replace("\"", "").trim()

            // 5. Clear Room cart on successful order placement
            cartDao.clearCart()

            // 6. Return populated Order object
            val placedOrderItems = pricingResults.map { (item, liveProduct, pricing) ->
                com.example.govind.data.model.OrderItem(
                    productId = liveProduct.id,
                    quantity = item.quantity,
                    price = pricing.effectiveUnitPrice,
                    productName = liveProduct.name,
                    unit = liveProduct.unit,
                    basePrice = liveProduct.sellingPrice,
                    bulkDiscount = pricing.totalDiscount,
                    effectiveUnitPrice = pricing.effectiveUnitPrice,
                    lineTotal = pricing.subtotal,
                    discount = (liveProduct.sellingPrice - pricing.effectiveUnitPrice) * item.quantity,
                    experienceType = item.experienceType.uppercase()
                )
            }

            Result.success(com.example.govind.data.model.Order(
                id = orderId,
                customerId = uid,
                subtotal = subtotal,
                discount = totalSavings,
                deliveryCharge = deliveryCharge,
                total = total,
                savings = totalSavings,
                paymentMethod = paymentMethod,
                paymentStatus = if (paymentMethod.contains("STARPAY", ignoreCase = true) || paymentMethod.contains("ONLINE", ignoreCase = true)) "AWAITING_PAYMENT" else "PENDING",
                orderStatus = "PLACED",
                experienceType = orderExperienceType,
                addresses = selectedAddress,
                addressSnapshot = addressSnapshot,
                orderItems = placedOrderItems
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    
    override fun getOrders(): Flow<List<Order>> = flow {
        val uid = sessionManager.userId
        if (uid != null) {
            // Initial fetch
            try {
                emit(api.getOrders("eq.$uid"))
            } catch (e: Exception) {
                emit(emptyList())
            }

            // Realtime WebSocket subscription (no polling)
            realtimeManager.subscribeTable("orders").collect {
                try {
                    emit(api.getOrders("eq.$uid"))
                } catch (_: Exception) {}
            }
        } else {
            emit(emptyList())
        }
    }

    override fun getOrderById(orderId: String): Flow<Result<Order>> = flow {
        try {
            val orders = api.getOrderById("eq.$orderId")
            if (orders.isNotEmpty()) {
                emit(Result.success(orders.first()))
            } else {
                emit(Result.failure(Exception("Order not found")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }

        realtimeManager.subscribeTable("orders").collect { event ->
            try {
                val recordId = event.record?.get("id")?.toString()?.replace("\"", "")
                if (recordId == null || recordId.equals(orderId, ignoreCase = true)) {
                    val orders = api.getOrderById("eq.$orderId")
                    if (orders.isNotEmpty()) {
                        emit(Result.success(orders.first()))
                    }
                }
            } catch (_: Exception) {}
        }
    }

    override fun getKitchenMenuItems(): Flow<List<Product>> = flow {
        try { emit(api.getKitchenMenuItems()) } catch (e: Exception) { emit(emptyList()) }
    }

    override fun getWholesaleItems(): Flow<List<Product>> = flow {
        try { emit(api.getWholesaleItems()) } catch (e: Exception) { emit(emptyList()) }
    }

    override suspend fun getDeliveryPartnerOrders(): Result<List<com.example.govind.data.model.Order>> {
        val uid = sessionManager.userId ?: return Result.failure(Exception("Not logged in"))
        return try {
            val orders = api.getDeliveryPartnerOrders("eq.$uid")
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> {
        return try {
            val request = kotlinx.serialization.json.buildJsonObject {
                put("order_status", kotlinx.serialization.json.JsonPrimitive(status))
            }
            api.updateOrderStatus("eq.$orderId", request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markOrderPaid(orderId: String, upiRef: String?): Result<Unit> {
        return try {
            val request = kotlinx.serialization.json.buildJsonObject {
                put("order_status", kotlinx.serialization.json.JsonPrimitive("CONFIRMED"))
                put("payment_status", kotlinx.serialization.json.JsonPrimitive("PAID"))
            }
            api.updateOrderStatus("eq.$orderId", request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDeliveryLocation(orderId: String, lat: Double, lng: Double, accuracy: Float, speed: Float, heading: Float): Result<Unit> {
        return try {
            val request = kotlinx.serialization.json.buildJsonObject {
                put("p_order_id", kotlinx.serialization.json.JsonPrimitive(orderId))
                put("p_lat", kotlinx.serialization.json.JsonPrimitive(lat))
                put("p_lng", kotlinx.serialization.json.JsonPrimitive(lng))
                put("p_accuracy", kotlinx.serialization.json.JsonPrimitive(accuracy.toDouble()))
                put("p_speed", kotlinx.serialization.json.JsonPrimitive(speed.toDouble()))
                put("p_heading", kotlinx.serialization.json.JsonPrimitive(heading.toDouble()))
            }
            api.updateDeliveryLocation(request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Cloud Cart Sync ───────────────────────────────────────────────────────
    private suspend fun syncItemToCloud(productId: String, experienceType: String, quantity: Int) {
        try {
            val uid = sessionManager.userId ?: return
            val carts = api.getCarts("eq.$uid")
            val cart = if (carts.isNotEmpty()) carts.first() else {
                val newCart = api.createCart(kotlinx.serialization.json.buildJsonObject {
                    put("profile_id", kotlinx.serialization.json.JsonPrimitive(uid))
                })
                newCart.firstOrNull() ?: return
            }

            if (quantity <= 0) {
                api.deleteCartItem("eq.${cart.id}", "eq.$productId", "eq.$experienceType")
            } else {
                val itemObj = kotlinx.serialization.json.buildJsonObject {
                    put("cart_id", kotlinx.serialization.json.JsonPrimitive(cart.id))
                    put("product_id", kotlinx.serialization.json.JsonPrimitive(productId))
                    put("quantity", kotlinx.serialization.json.JsonPrimitive(quantity))
                    put("experience_type", kotlinx.serialization.json.JsonPrimitive(experienceType.uppercase()))
                }
                api.upsertCartItem(prefer = "resolution=merge-duplicates", item = itemObj)
            }
        } catch (e: Exception) {
            android.util.Log.e("GovindCartSync", "Error syncing item to cloud: ${e.message}")
        }
    }

    override suspend fun syncCartOnLogin() {
        val uid = sessionManager.userId ?: return
        val localEntities = cartDao.getCartItems().first()
        val carts = api.getCarts("eq.$uid")
        val cloudCart = if (carts.isNotEmpty()) carts.first() else {
            val newCart = api.createCart(kotlinx.serialization.json.buildJsonObject {
                put("profile_id", kotlinx.serialization.json.JsonPrimitive(uid))
            })
            newCart.firstOrNull()
        } ?: return

        val cloudItems = cloudCart.cartItems

        // Merge: match by (productId, experienceType)
        for (localItem in localEntities) {
            val matchingCloud = cloudItems.firstOrNull { 
                it.productId.equals(localItem.productId, ignoreCase = true) && 
                it.experienceType.equals(localItem.experienceType, ignoreCase = true) 
            }
            if (matchingCloud != null) {
                val combinedQty = localItem.quantity + matchingCloud.quantity
                cartDao.updateQuantity(localItem.productId, localItem.experienceType, combinedQty)
                val itemObj = kotlinx.serialization.json.buildJsonObject {
                    put("cart_id", kotlinx.serialization.json.JsonPrimitive(cloudCart.id))
                    put("product_id", kotlinx.serialization.json.JsonPrimitive(localItem.productId))
                    put("quantity", kotlinx.serialization.json.JsonPrimitive(combinedQty))
                    put("experience_type", kotlinx.serialization.json.JsonPrimitive(localItem.experienceType.uppercase()))
                }
                api.upsertCartItem(item = itemObj)
            } else {
                val itemObj = kotlinx.serialization.json.buildJsonObject {
                    put("cart_id", kotlinx.serialization.json.JsonPrimitive(cloudCart.id))
                    put("product_id", kotlinx.serialization.json.JsonPrimitive(localItem.productId))
                    put("quantity", kotlinx.serialization.json.JsonPrimitive(localItem.quantity))
                    put("experience_type", kotlinx.serialization.json.JsonPrimitive(localItem.experienceType.uppercase()))
                }
                api.upsertCartItem(item = itemObj)
            }
        }

        // For cloud items not in local:
        for (cloudItem in cloudItems) {
            val inLocal = localEntities.any { 
                it.productId.equals(cloudItem.productId, ignoreCase = true) && 
                it.experienceType.equals(cloudItem.experienceType, ignoreCase = true) 
            }
            if (!inLocal) {
                try {
                    val pList = api.getProductDetails("eq.${cloudItem.productId}")
                    val p = pList.firstOrNull()
                    if (p != null) {
                        val entity = CartEntity(
                            id = p.id,
                            productId = p.id,
                            name = p.name,
                            slug = p.slug,
                            price = p.price ?: p.sellingPrice,
                            sellingPrice = p.sellingPrice,
                            description = p.description,
                            imageUrl = p.directImageUrl ?: p.imageUrl,
                            categoryId = p.categoryId,
                            unit = p.unit,
                            quantity = cloudItem.quantity,
                            experienceType = cloudItem.experienceType.uppercase(),
                            bundleItemsJson = p.bundleItems?.toString(),
                            productType = p.productType
                        )
                        cartDao.insertItem(entity)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    // ── Favorites CRUD ────────────────────────────────────────────────────────
    override fun getFavorites(): Flow<List<Product>> = flow {
        val uid = sessionManager.userId
        if (uid != null) {
            try {
                val res = api.getFavorites("eq.$uid")
                emit(res.mapNotNull { it.products })
            } catch (e: Exception) {
                emit(emptyList())
            }

            realtimeManager.subscribeTable("favorites").collect {
                try {
                    val res = api.getFavorites("eq.$uid")
                    emit(res.mapNotNull { it.products })
                } catch (_: Exception) {}
            }
        } else {
            emit(emptyList())
        }
    }

    override suspend fun toggleFavorite(productId: String): Result<Boolean> {
        val uid = sessionManager.userId
        if (uid.isNullOrBlank() || !sessionManager.isLoggedIn) {
            return Result.failure(Exception("Please sign in to save items to your favorites."))
        }

        return try {
            val existing = api.getFavorites("eq.$uid")
            val isFav = existing.any { it.productId == productId }
            if (isFav) {
                api.removeFavorite("eq.$uid", "eq.$productId")
                Result.success(false)
            } else {
                val obj = kotlinx.serialization.json.buildJsonObject {
                    put("profile_id", kotlinx.serialization.json.JsonPrimitive(uid))
                    put("product_id", kotlinx.serialization.json.JsonPrimitive(productId))
                }
                api.addFavorite(obj)
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isFavorite(productId: String): Flow<Boolean> = flow {
        val uid = sessionManager.userId
        if (uid != null) {
            try {
                val existing = api.getFavorites("eq.$uid")
                emit(existing.any { it.productId == productId })
            } catch (_: Exception) {
                emit(false)
            }

            realtimeManager.subscribeTable("favorites").collect {
                try {
                    val existing = api.getFavorites("eq.$uid")
                    emit(existing.any { it.productId == productId })
                } catch (_: Exception) {}
            }
        } else {
            emit(false)
        }
    }
}








































