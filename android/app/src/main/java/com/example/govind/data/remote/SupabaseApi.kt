package com.example.govind.data.remote

import com.example.govind.data.model.Category
import com.example.govind.data.model.Product
import retrofit2.http.GET
import retrofit2.http.Query

interface SupabaseApi {
    
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
}
