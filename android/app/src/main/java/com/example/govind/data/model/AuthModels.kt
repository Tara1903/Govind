package com.example.govind.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    val user: User
)

@Serializable
data class User(
    val id: String,
    val email: String
)

@Serializable
data class Profile(
    val id: String,
    val name: String = "",
    @SerialName("full_name") val fullName: String? = null,
    val phone: String? = null,
    val role: String = "customer",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class SendOtpRequest(
    val email: String,
    @SerialName("create_user") val createUser: Boolean = true
)

@Serializable
data class VerifyOtpRequest(
    val type: String,
    val email: String,
    val token: String
)
