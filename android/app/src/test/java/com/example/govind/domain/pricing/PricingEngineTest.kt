package com.example.govind.domain.pricing

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.Test

class PricingEngineTest {

    @Test
    fun calculateProductPrice_noTiers_standardPrice() {
        val result = PricingEngine.calculateProductPrice(
            basePrice = 50.0,
            quantity = 2,
            wholesalePricing = null,
            experience = "FRESH"
        )

        assertEquals(50.0, result.baseUnitPrice)
        assertEquals(50.0, result.effectiveUnitPrice)
        assertEquals(100.0, result.subtotal)
        assertEquals(0.0, result.totalDiscount)
        assertNull(result.appliedTier)
    }

    @Test
    fun calculateProductPrice_withPercentageTier_appliesDiscount() {
        val tiers = listOf(
            BulkTier(min_qty = 5, type = "percentage", value = 10.0, status = "active"),
            BulkTier(min_qty = 10, type = "percentage", value = 20.0, status = "active")
        )
        val wholesale = WholesalePricing(wholesale_eligible = true, tiers = tiers)

        // Quantity 7 activates 10% tier
        val result = PricingEngine.calculateProductPrice(
            basePrice = 100.0,
            quantity = 7,
            wholesalePricing = wholesale,
            experience = "FRESH"
        )

        assertEquals(100.0, result.baseUnitPrice)
        assertEquals(90.0, result.effectiveUnitPrice)
        assertEquals(10.0, result.discountAmountPerUnit)
        assertEquals(70.0, result.totalDiscount)
        assertEquals(630.0, result.subtotal)
        assertNotNull(result.appliedTier)
        assertEquals(5, result.appliedTier?.min_qty)
        assertEquals(10, result.nextTier?.min_qty)
        assertEquals(3, result.remainingQuantity) // 10 - 7
    }

    @Test
    fun calculateProductPrice_withFixedTier_appliesFixedPrice() {
        val tiers = listOf(
            BulkTier(min_qty = 10, type = "fixed", value = 35.0, status = "active")
        )
        val wholesale = WholesalePricing(wholesale_eligible = true, tiers = tiers)

        val result = PricingEngine.calculateProductPrice(
            basePrice = 45.0,
            quantity = 12,
            wholesalePricing = wholesale,
            experience = "WHOLESALE"
        )

        assertEquals(35.0, result.effectiveUnitPrice)
        assertEquals(10.0, result.discountAmountPerUnit)
        assertEquals(120.0, result.totalDiscount)
        assertEquals(420.0, result.subtotal)
    }

    @Test
    fun calculateProductPrice_kitchenExperience_doesNotApplyWholesaleTier() {
        val tiers = listOf(
            BulkTier(min_qty = 5, type = "percentage", value = 50.0, status = "active")
        )
        val wholesale = WholesalePricing(wholesale_eligible = true, tiers = tiers)

        // Kitchen cooked food does not give wholesale bulk discounts
        val result = PricingEngine.calculateProductPrice(
            basePrice = 200.0,
            quantity = 10,
            wholesalePricing = wholesale,
            experience = "KITCHEN"
        )

        assertEquals(200.0, result.effectiveUnitPrice)
        assertEquals(2000.0, result.subtotal)
        assertEquals(0.0, result.totalDiscount)
        assertNull(result.appliedTier)
    }
}
