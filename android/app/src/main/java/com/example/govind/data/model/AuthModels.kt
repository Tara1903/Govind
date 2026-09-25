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
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
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
    val name: String,
    val phone: String?,
    @SerialName("created_at") val createdAt: String? = null
)
