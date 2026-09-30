package com.example.govind.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════════════
// Shape Scale — Stitch Source of Truth
// ═══════════════════════════════════════════════════════════
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // Badges, small chips
    small = RoundedCornerShape(8.dp),        // Buttons, input fields
    medium = RoundedCornerShape(12.dp),      // Cards, containers
    large = RoundedCornerShape(16.dp),       // Large cards, hero sections
    extraLarge = RoundedCornerShape(28.dp)   // Bottom sheets, dialogs
)

// Full-round shape for pills, switcher, circle
val PillShape = RoundedCornerShape(9999.dp)
