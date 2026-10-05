package com.example.govind.ui.features.starpay

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

object StarPayTheme {
    val Surface0 = Color(0xFF020617) // Deep slate near-black
    val Surface1 = Color(0xFF0D1125) // Card surface
    val Surface2 = Color(0xFF161C34) // Elevated card
    val BorderSubtle = Color.White.copy(alpha = 0.08f)
    val BorderLight = Color.White.copy(alpha = 0.16f)

    val BrandViolet = Color(0xFF8B5CF6)
    val BrandVioletLight = Color(0xFFA78BFA)
    val BrandCyan = Color(0xFF06B6D4)
    val BrandGradient = Brush.linearGradient(listOf(BrandViolet, BrandCyan))

    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF475569)

    val Emerald = Color(0xFF34D399) // Success
    val Amber = Color(0xFFFBBF24)   // Warning / Under review
    val Red = Color(0xFFF87171)     // Danger / Expired

    val MonoFont = FontFamily.Monospace
}
