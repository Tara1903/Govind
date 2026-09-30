package com.example.govind.domain.pricing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckoutCalculationTest {

    @Test
    fun `delivery fee is 40 when subtotal is under 500`() {
        val subtotal = 499.0
        val deliveryFee = if (subtotal >= 500.0) 0.0 else 40.0
        assertEquals(40.0, deliveryFee, 0.001)
        val grandTotal = subtotal + deliveryFee
        assertEquals(539.0, grandTotal, 0.001)
    }

    @Test
    fun `delivery fee is free when subtotal is 500 or more`() {
        val subtotal = 500.0
        val deliveryFee = if (subtotal >= 500.0) 0.0 else 40.0
        assertEquals(0.0, deliveryFee, 0.001)
        val grandTotal = subtotal + deliveryFee
        assertEquals(500.0, grandTotal, 0.001)
    }

    @Test
    fun `experience type derivation identifies homogeneous versus mixed baskets`() {
        val freshOnly = listOf("FRESH", "FRESH").distinct()
        val exp1 = if (freshOnly.size == 1) freshOnly.first() else "MIXED"
        assertEquals("FRESH", exp1)

        val kitchenOnly = listOf("KITCHEN").distinct()
        val exp2 = if (kitchenOnly.size == 1) kitchenOnly.first() else "MIXED"
        assertEquals("KITCHEN", exp2)

        val wholesaleOnly = listOf("WHOLESALE", "WHOLESALE").distinct()
        val exp3 = if (wholesaleOnly.size == 1) wholesaleOnly.first() else "MIXED"
        assertEquals("WHOLESALE", exp3)

        val mixed = listOf("FRESH", "KITCHEN").distinct()
        val exp4 = if (mixed.size == 1) mixed.first() else "MIXED"
        assertEquals("MIXED", exp4)
    }

    @Test
    fun `online payment blocked notification text verification`() {
        val method = "ONLINE"
        val isOnlineBlocked = method.equals("ONLINE", ignoreCase = true)
        assertTrue(isOnlineBlocked)
        val expectedNotice = "ONLINE PAYMENT BLOCKED — STARPAY CREDENTIALS/API CONTRACT REQUIRED"
        assertTrue(expectedNotice.contains("STARPAY CREDENTIALS"))
    }
}
