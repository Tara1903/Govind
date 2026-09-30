package com.example.govind.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val id: String,
    @SerialName("profile_id") val profileId: String,
    val name: String,
    val phone: String,
    val house: String,
    val street: String,
    val area: String,
    val landmark: String? = null,
    val city: String,
    val pincode: String,
    @SerialName("delivery_instructions") val deliveryInstructions: String? = null,
    @SerialName("is_default") val isDefault: Boolean = false
)

@Serializable
data class ProductImages(
    @SerialName("image_url") val imageUrl: String
)

@Serializable
data class OrderItemProduct(
    val id: String,
    val name: String,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("product_images") val productImages: List<ProductImages> = emptyList()
)

@Serializable
data class OrderItem(
    val id: String = "",
    @SerialName("order_id") val orderId: String = "",
    @SerialName("product_id") val productId: String,
    val quantity: Int,
    val price: Double,
    @SerialName("product_name") val productName: String? = null,
    val unit: String? = null,
    @SerialName("base_price") val basePrice: Double? = null,
    @SerialName("bulk_discount") val bulkDiscount: Double? = null,
    @SerialName("promotion_discount") val promotionDiscount: Double? = null,
    @SerialName("coupon_discount") val couponDiscount: Double? = null,
    @SerialName("effective_unit_price") val effectiveUnitPrice: Double? = null,
    @SerialName("line_total") val lineTotal: Double? = null,
    val discount: Double? = null,
    @SerialName("experience_type") val experienceType: String = "FRESH",
    val products: OrderItemProduct? = null
)

@Serializable
data class OrderStatusHistory(
    val id: String,
    @SerialName("order_id") val orderId: String,
    val status: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class Order(
    val id: String,
    @SerialName("customer_id") val customerId: String,
    val subtotal: Double,
    val discount: Double = 0.0,
    @SerialName("delivery_charge") val deliveryCharge: Double = 0.0,
    val total: Double,
    @SerialName("savings") val savings: Double = 0.0,
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("payment_status") val paymentStatus: String,
    @SerialName("order_status") val orderStatus: String,
    @SerialName("experience_type") val experienceType: String? = null,
    val checkoutUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("order_items") val orderItems: List<OrderItem> = emptyList(),
    @SerialName("order_status_history") val orderStatusHistory: List<OrderStatusHistory> = emptyList(),
    val addresses: Address? = null,
    @SerialName("address_snapshot") val addressSnapshot: kotlinx.serialization.json.JsonObject? = null,
    @SerialName("delivery_partner_id") val deliveryPartnerId: String? = null,
    @SerialName("dest_latitude") val destLatitude: Double? = null,
    @SerialName("dest_longitude") val destLongitude: Double? = null
)

@Serializable
data class RazorpayOrderResponse(
    val id: String,
    val amount: Int,
    val currency: String
)
