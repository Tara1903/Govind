package com.example.govind.data.remote

import com.example.govind.data.model.Category
import com.example.govind.data.model.Product
import retrofit2.http.GET
import retrofit2.http.Query

interface SupabaseApi {

    @GET("rest/v1/products")
    suspend fun getFreshBoardProducts(
        @Query("select") select: String = "*,product_images(image_url)",
        @Query("active") active: String = "eq.true",
        @Query("on_fresh_board") onFreshBoard: String = "eq.true"
    ): List<Product>
    
    @GET("rest/v1/categories")
    suspend fun getCategories(
        @Query("select") select: String = "*",
        @Query("active") active: String = "eq.true",
        @Query("order") order: String = "display_order.asc"
    ): List<Category>

    @GET("rest/v1/products")
    suspend fun searchProducts(
        @Query("name") nameIlike: String,
        @Query("active") active: String = "eq.true"
    ): List<Product>

    @GET("rest/v1/products")
    suspend fun getProductsByType(
        @Query("product_type") type: String,
        @Query("active") active: String = "eq.true"
    ): List<Product>
    
    @GET("rest/v1/products")
    suspend fun getFeaturedProducts(
        @Query("select") select: String = "*,product_images(image_url)",
        @Query("active") active: String = "eq.true",
        @Query("featured") featured: String = "eq.true"
    ): List<Product>

    @GET("rest/v1/products")
    suspend fun getProductsByCategory(
        @Query("category_id") categoryId: String,
        @Query("select") select: String = "*,product_images(image_url)",
        @Query("active") active: String = "eq.true"
    ): List<Product>

    @GET("rest/v1/products")
    suspend fun getProductDetails(
        @Query("id") id: String,
        @Query("select") select: String = "*,product_images(image_url)"
    ): List<Product>

    @GET("rest/v1/products")
    suspend fun getProductsByIds(
        @Query("id") idFilter: String,
        @Query("select") select: String = "*,product_images(image_url)"
    ): List<Product>
    

    @GET("rest/v1/products")
    suspend fun getKitchenMenuItems(
        @Query("select") select: String = "*,product_images(image_url)",
        @Query("active") active: String = "eq.true",
        @Query("experience_type") experienceType: String = "eq.KITCHEN",
        @Query("order") order: String = "name.asc"
    ): List<Product>

    @GET("rest/v1/products")
    suspend fun getWholesaleItems(
        @Query("select") select: String = "*,product_images(image_url)",
        @Query("active") active: String = "eq.true",
        @Query("bulk_available") bulkAvailable: String = "eq.true",
        @Query("order") order: String = "name.asc"
    ): List<Product>
    // Auth
    @retrofit2.http.POST("auth/v1/otp")
    suspend fun sendOtp(@retrofit2.http.Body request: com.example.govind.data.model.SendOtpRequest)

    @retrofit2.http.POST("auth/v1/verify")
    suspend fun verifyOtp(@retrofit2.http.Body request: com.example.govind.data.model.VerifyOtpRequest): com.example.govind.data.model.AuthResponse
    
    // Profiles
    @GET("rest/v1/profiles")
    suspend fun getProfile(
        @Query("id") id: String,
        @Query("select") select: String = "*"
    ): List<com.example.govind.data.model.Profile>
    
    @retrofit2.http.POST("rest/v1/profiles")
    suspend fun createProfile(@retrofit2.http.Body profile: com.example.govind.data.model.Profile)
    
    @retrofit2.http.PATCH("rest/v1/profiles")
    suspend fun updateProfile(
        @retrofit2.http.Query("id") id: String,
        @retrofit2.http.Body updates: kotlinx.serialization.json.JsonObject
    )
    
    // Addresses
    @GET("rest/v1/addresses")
    suspend fun getAddresses(
        @Query("profile_id") profileId: String,
        @Query("select") select: String = "*"
    ): List<com.example.govind.data.model.Address>

    @retrofit2.http.POST("rest/v1/addresses")
    suspend fun addAddress(@retrofit2.http.Body address: kotlinx.serialization.json.JsonObject)

    // Delivery Settings
    @GET("rest/v1/delivery_settings")
    suspend fun getDeliverySettings(
        @Query("select") select: String = "*",
        @Query("active") active: String = "eq.true"
    ): List<com.example.govind.data.model.DeliverySettings>
    
    // Orders
    @GET("rest/v1/orders")
    suspend fun getOrders(
        @Query("customer_id") customerId: String,
        @Query("select") select: String = "*,order_items(*,products(*,product_images(image_url))),order_status_history(*),addresses(*)",
        @Query("order") order: String = "created_at.desc"
    ): List<com.example.govind.data.model.Order>

    @GET("rest/v1/orders")
    suspend fun getOrderById(
        @Query("id") idQuery: String,
        @Query("select") select: String = "*,order_items(*,products(*,product_images(image_url))),order_status_history(*),addresses(*)"
    ): List<com.example.govind.data.model.Order>

    @retrofit2.http.POST("rest/v1/rpc/create_order_and_decrement_stock")
    suspend fun createOrderRpc(
        @retrofit2.http.Body request: kotlinx.serialization.json.JsonObject
    ): String

    @GET("rest/v1/delivery_locations")
    suspend fun getDeliveryLocation(
        @Query("order_id") orderId: String,
        @Query("select") select: String = "*"
    ): List<com.example.govind.data.model.DeliveryLocation>

    // Edge Functions (Razorpay checkout)
    @retrofit2.http.POST("functions/v1/create-razorpay-order")
    suspend fun createRazorpayOrder(
        @retrofit2.http.Body request: Map<String, Double>
    ): com.example.govind.data.model.RazorpayOrderResponse

    @GET("rest/v1/orders")
    suspend fun getDeliveryPartnerOrders(
        @Query("delivery_partner_id") deliveryPartnerId: String,
        @Query("order_status") orderStatus: String = "in.(OUT_FOR_DELIVERY,READY_FOR_DELIVERY)",
        @Query("select") select: String = "*,order_items(*,products(*,product_images(image_url))),order_status_history(*),addresses(*)"
    ): List<com.example.govind.data.model.Order>

    @retrofit2.http.PATCH("rest/v1/orders")
    suspend fun updateOrderStatus(
        @Query("id") idQuery: String,
        @retrofit2.http.Body request: kotlinx.serialization.json.JsonObject
    )

    @retrofit2.http.POST("rest/v1/rpc/update_delivery_location")
    suspend fun updateDeliveryLocation(
        @retrofit2.http.Body request: kotlinx.serialization.json.JsonObject
    )

    // Address Management CRUD
    @retrofit2.http.PATCH("rest/v1/addresses")
    suspend fun updateAddress(
        @Query("id") idQuery: String,
        @retrofit2.http.Body updates: kotlinx.serialization.json.JsonObject
    )

    @retrofit2.http.DELETE("rest/v1/addresses")
    suspend fun deleteAddress(
        @Query("id") idQuery: String
    )

    // Favorites CRUD
    @GET("rest/v1/favorites")
    suspend fun getFavorites(
        @Query("profile_id") profileIdQuery: String,
        @Query("select") select: String = "*,products(*,product_images(image_url))"
    ): List<com.example.govind.data.model.FavoriteResponse>

    @retrofit2.http.POST("rest/v1/favorites")
    suspend fun addFavorite(
        @retrofit2.http.Body favorite: kotlinx.serialization.json.JsonObject
    )

    @retrofit2.http.DELETE("rest/v1/favorites")
    suspend fun removeFavorite(
        @Query("profile_id") profileIdQuery: String,
        @Query("product_id") productIdQuery: String
    )

    // Cloud Cart Sync
    @GET("rest/v1/carts")
    suspend fun getCarts(
        @Query("profile_id") profileIdQuery: String,
        @Query("select") select: String = "*,cart_items(*)"
    ): List<com.example.govind.data.model.CloudCart>

    @retrofit2.http.POST("rest/v1/carts")
    suspend fun createCart(
        @retrofit2.http.Body cart: kotlinx.serialization.json.JsonObject
    ): List<com.example.govind.data.model.CloudCart>

    @retrofit2.http.POST("rest/v1/cart_items")
    suspend fun upsertCartItem(
        @retrofit2.http.Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @retrofit2.http.Body item: kotlinx.serialization.json.JsonObject
    )

    @retrofit2.http.DELETE("rest/v1/cart_items")
    suspend fun deleteCartItem(
        @Query("cart_id") cartIdQuery: String,
        @Query("product_id") productIdQuery: String,
        @Query("experience_type") expQuery: String
    )

    @retrofit2.http.DELETE("rest/v1/cart_items")
    suspend fun clearCloudCart(
        @Query("cart_id") cartIdQuery: String
    )
}








