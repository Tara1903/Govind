package com.example.govind.di

import com.example.govind.BuildConfig
import com.example.govind.data.remote.SupabaseApi
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(sessionManager: com.example.govind.data.local.SessionManager, json: Json): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        val authInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
            
            val token = sessionManager.accessToken
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            } else {
                requestBuilder.addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
            }
                
            chain.proceed(requestBuilder.build())
        }

        val authenticator = okhttp3.Authenticator { _, response ->
            if (response.priorResponse != null) return@Authenticator null
            val currentRefreshToken = sessionManager.refreshToken ?: return@Authenticator null
            try {
                val refreshClient = OkHttpClient()
                val body = okhttp3.RequestBody.create(
                    "application/json".toMediaType(),
                    "{\"refresh_token\":\"$currentRefreshToken\"}"
                )
                val refreshReq = okhttp3.Request.Builder()
                    .url("${BuildConfig.SUPABASE_URL}/auth/v1/token?grant_type=refresh_token")
                    .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()
                val refreshResp = refreshClient.newCall(refreshReq).execute()
                if (refreshResp.isSuccessful) {
                    val respBody = refreshResp.body?.string() ?: return@Authenticator null
                    val jsonElem = json.parseToJsonElement(respBody)
                    val obj = if (jsonElem is kotlinx.serialization.json.JsonObject) jsonElem else null
                    val newAccessToken = obj?.get("access_token")?.let {
                        if (it is kotlinx.serialization.json.JsonPrimitive) it.content else null
                    }
                    val newRefreshToken = obj?.get("refresh_token")?.let {
                        if (it is kotlinx.serialization.json.JsonPrimitive) it.content else null
                    }
                    if (!newAccessToken.isNullOrEmpty()) {
                        sessionManager.accessToken = newAccessToken
                        if (!newRefreshToken.isNullOrEmpty()) {
                            sessionManager.refreshToken = newRefreshToken
                        }
                        return@Authenticator response.request.newBuilder()
                            .header("Authorization", "Bearer $newAccessToken")
                            .build()
                    }
                } else {
                    sessionManager.clearSession()
                }
            } catch (e: Exception) {
                // Ignore and return null
            }
            null
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .authenticator(authenticator)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(if (BuildConfig.SUPABASE_URL.endsWith("/")) BuildConfig.SUPABASE_URL else "${BuildConfig.SUPABASE_URL}/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideSupabaseApi(retrofit: Retrofit): SupabaseApi {
        return retrofit.create(SupabaseApi::class.java)
    }
    @Provides
    @Singleton
    fun providePaymentGatewayApi(json: Json): com.example.govind.data.remote.PaymentGatewayApi {
        val contentType = "application/json".toMediaType()
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3001/") // Assuming Payment Gateway runs on port 3001
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(com.example.govind.data.remote.PaymentGatewayApi::class.java)
    }
}




