package com.example.govind.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val slug: String,
    val image: String? = null,
    val description: String? = null,
    @SerialName("display_order") val displayOrder: Int = 0,
    val active: Boolean = true
)
