package com.example.govind.data.remote

import com.example.govind.data.model.Category
import com.example.govind.data.model.Product
import retrofit2.http.GET
import retrofit2.http.Query

interface SupabaseApi {

    @GET("rest/v1/products")
    suspend fun getFreshBoardProducts(
        @Query("select") select: String = "*",
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
    suspend fun getFeaturedProducts(
        @Query("select") select: String = "*",
        @Query("active") active: String = "eq.true",
        @Query("featured") featured: String = "eq.true"
    ): List<Product>

    @GET("rest/v1/products")
    suspend fun getProductsByCategory(
        @Query("category_id") categoryId: String,
        @Query("select") select: String = "*",
        @Query("active") active: String = "eq.true"
    ): List<Product>

    @GET("rest/v1/products")
    suspend fun getProductDetails(
        @Query("id") id: String,
        @Query("select") select: String = "*"
    ): List<Product>
    
    // Auth
    @retrofit2.http.POST("auth/v1/signup")
    suspend fun signUp(@retrofit2.http.Body request: com.example.govind.data.model.AuthRequest): com.example.govind.data.model.AuthResponse

    @retrofit2.http.POST("auth/v1/token?grant_type=password")
    suspend fun login(@retrofit2.http.Body request: com.example.govind.data.model.AuthRequest): com.example.govind.data.model.AuthResponse
    
    // Profiles
    @GET("rest/v1/profiles")
    suspend fun getProfile(
        @Query("id") id: String,
        @Query("select") select: String = "*"
    ): List<com.example.govind.data.model.Profile>
    
    @retrofit2.http.POST("rest/v1/profiles")
    suspend fun createProfile(@retrofit2.http.Body profile: com.example.govind.data.model.Profile)
    
    // Addresses
    @GET("rest/v1/addresses")
    suspend fun getAddresses(
        @Query("profile_id") profileId: String,
        @Query("select") select: String = "*"
    ): List<com.example.govind.data.model.Address>
    
    // Orders
    @GET("rest/v1/orders")
    suspend fun getOrders(
        @Query("customer_id") customerId: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): List<com.example.govind.data.model.Order>

    @retrofit2.http.POST("rest/v1/rpc/create_order_and_decrement_stock")
    suspend fun createOrderRpc(
        @retrofit2.http.Body request: kotlinx.serialization.json.JsonObject
    ): String

    // Edge Functions (Razorpay checkout)
    @retrofit2.http.POST("functions/v1/create-razorpay-order")
    suspend fun createRazorpayOrder(
        @retrofit2.http.Body request: Map<String, Double>
    ): com.example.govind.data.model.RazorpayOrderResponse
}
