package com.example.govind.domain.pricing

import kotlinx.serialization.Serializable
import kotlin.math.round

@Serializable
data class BulkTier(
    val min_qty: Int,
    val type: String, // "percentage" or "fixed"
    val value: Double,
    val status: String // "active" or "inactive"
)

@Serializable
data class WholesalePricing(
    val wholesale_eligible: Boolean = false,
    val tiers: List<BulkTier> = emptyList()
)

data class PricingResult(
    val baseUnitPrice: Double,
    val effectiveUnitPrice: Double,
    val appliedTier: BulkTier?,
    val discountType: String,
    val discountValue: Double,
    val discountAmountPerUnit: Double,
    val totalDiscount: Double,
    val subtotal: Double,
    val nextTier: BulkTier?,
    val remainingQuantity: Int?
)

object PricingEngine {
    private fun round2(v: Double): Double = round(v * 100.0) / 100.0

    fun calculateProductPrice(
        basePrice: Double,
        quantity: Int,
        wholesalePricing: WholesalePricing?,
        experience: String
    ): PricingResult {
        var effectiveUnitPrice = basePrice
        var appliedTier: BulkTier? = null
        var discountType = "none"
        var discountValue = 0.0
        var discountAmountPerUnit = 0.0

        var activeTiers: List<BulkTier> = emptyList()

        if (wholesalePricing != null && wholesalePricing.wholesale_eligible && experience != "KITCHEN") {
            activeTiers = wholesalePricing.tiers
                .filter { it.status == "active" }
                .sortedBy { it.min_qty }

            for (tier in activeTiers) {
                if (quantity >= tier.min_qty) {
                    appliedTier = tier
                }
            }

            if (appliedTier != null) {
                discountType = appliedTier.type
                discountValue = appliedTier.value
                if (appliedTier.type == "percentage") {
                    discountAmountPerUnit = round2(basePrice * (appliedTier.value / 100.0))
                    effectiveUnitPrice = round2(basePrice - discountAmountPerUnit)
                } else if (appliedTier.type == "fixed") {
                    effectiveUnitPrice = round2(appliedTier.value)
                    discountAmountPerUnit = round2(basePrice - effectiveUnitPrice)
                }
            }
        }

        var nextTier: BulkTier? = null
        var remainingQuantity: Int? = null

        if (activeTiers.isNotEmpty()) {
            for (tier in activeTiers) {
                if (tier.min_qty > quantity) {
                    nextTier = tier
                    remainingQuantity = tier.min_qty - quantity
                    break
                }
            }
        }

        val subtotal = round2(effectiveUnitPrice * quantity)
        val totalDiscount = round2(discountAmountPerUnit * quantity)

        return PricingResult(
            baseUnitPrice = basePrice,
            effectiveUnitPrice = effectiveUnitPrice,
            appliedTier = appliedTier,
            discountType = discountType,
            discountValue = discountValue,
            discountAmountPerUnit = discountAmountPerUnit,
            totalDiscount = totalDiscount,
            subtotal = subtotal,
            nextTier = nextTier,
            remainingQuantity = remainingQuantity
        )
    }
}
