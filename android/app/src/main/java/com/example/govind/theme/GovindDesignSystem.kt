package com.example.govind.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════
// GOVIND Custom Color Palette — Stitch Source of Truth
//
// These extend MaterialTheme.colorScheme with brand-specific
// semantic tokens used across the GOVIND app.
// ═══════════════════════════════════════════════════════════

@Immutable
data class GovindColors(
    // ── Brand ────────────────────────────────────────────
    val govindGreen: Color,
    val freshGreen: Color,
    val govindOrange: Color,
    val warmWhite: Color,
    val pureWhite: Color,

    // ── Text ─────────────────────────────────────────────
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textOnDark: Color,
    val textOnOrange: Color,

    // ── Borders ──────────────────────────────────────────
    val border: Color,
    val borderFocused: Color,

    // ── Surfaces ─────────────────────────────────────────
    val surfacePressed: Color,
    val surfaceHover: Color,
    val softGreen: Color,
    val softFresh: Color,
    val softOrange: Color,
    val softCream: Color,

    // ── Skeleton / Loading ───────────────────────────────
    val skeleton: Color,
    val skeletonHighlight: Color,

    // ── Feedback ─────────────────────────────────────────
    val error: Color,
    val errorBg: Color,
    val success: Color,
    val successBg: Color,

    // ── Wholesale-specific ───────────────────────────────
    val wholesaleNavy: Color,
    val wholesaleAmber: Color,
    val wholesaleTint: Color,

    // ── Kitchen-specific ─────────────────────────────────
    val kitchenAccent: Color,
    val kitchenTint: Color,

    // ── Inverse (floating dark surfaces) ─────────────────
    val inverseSurface: Color,
    val inverseOnSurface: Color,
    val inversePrimary: Color
) {
    val primary: Color get() = govindGreen
    val onPrimary: Color get() = textOnDark
    val primaryContainer: Color get() = govindGreen
    val primaryFixed: Color get() = govindGreen
    val onPrimaryFixed: Color get() = textOnDark
    val brandPrimary: Color get() = govindGreen

    val secondary: Color get() = freshGreen
    val onSecondary: Color get() = pureWhite
    val secondaryContainer: Color get() = softGreen
    val onSecondaryContainer: Color get() = textPrimary

    val tertiary: Color get() = govindOrange
    val onTertiary: Color get() = pureWhite
    val tertiaryContainer: Color get() = softOrange
    val tertiaryFixed: Color get() = govindOrange
    val onTertiaryFixed: Color get() = pureWhite

    val surface: Color get() = pureWhite
    val onSurface: Color get() = textPrimary
    val onSurfaceVariant: Color get() = textSecondary
    val surfaceContainerLowest: Color get() = pureWhite
    val surfaceContainerLow: Color get() = warmWhite
    val surfaceContainer: Color get() = pureWhite
    val surfaceContainerHigh: Color get() = warmWhite
    val surfaceContainerHighest: Color get() = Color(0xFFF1EFE7)
    val surfaceVariant: Color get() = Color(0xFFF1EFE7)
}

val LocalGovindColors = staticCompositionLocalOf {
    GovindColors(
        govindGreen = Color.Unspecified,
        freshGreen = Color.Unspecified,
        govindOrange = Color.Unspecified,
        warmWhite = Color.Unspecified,
        pureWhite = Color.Unspecified,
        textPrimary = Color.Unspecified,
        textSecondary = Color.Unspecified,
        textMuted = Color.Unspecified,
        textOnDark = Color.Unspecified,
        textOnOrange = Color.Unspecified,
        border = Color.Unspecified,
        borderFocused = Color.Unspecified,
        surfacePressed = Color.Unspecified,
        surfaceHover = Color.Unspecified,
        softGreen = Color.Unspecified,
        softFresh = Color.Unspecified,
        softOrange = Color.Unspecified,
        softCream = Color.Unspecified,
        skeleton = Color.Unspecified,
        skeletonHighlight = Color.Unspecified,
        error = Color.Unspecified,
        errorBg = Color.Unspecified,
        success = Color.Unspecified,
        successBg = Color.Unspecified,
        wholesaleNavy = Color.Unspecified,
        wholesaleAmber = Color.Unspecified,
        wholesaleTint = Color.Unspecified,
        kitchenAccent = Color.Unspecified,
        kitchenTint = Color.Unspecified,
        inverseSurface = Color.Unspecified,
        inverseOnSurface = Color.Unspecified,
        inversePrimary = Color.Unspecified
    )
}

val defaultGovindColors = GovindColors(
    // ── Brand (Stitch-exact) ─────────────────────────────
    govindGreen = GovindPrimaryContainer,            // #064520
    freshGreen = GovindSecondary,                    // #246C18
    govindOrange = Color(0xFFF5450D),                // Govind Orange
    warmWhite = Color(0xFFFEFCF5),                   // Warm canvas
    pureWhite = GovindSurfaceContainerLowest,        // #FFFFFF

    // ── Text (Stitch on-surface) ─────────────────────────
    textPrimary = GovindOnSurface,                   // #181D19
    textSecondary = GovindOnSurfaceVariant,          // #414941
    textMuted = GovindOutline,                       // #717970
    textOnDark = GovindInverseOnSurface,             // #EDF2EB
    textOnOrange = Color(0xFFFFFFFF),

    // ── Borders (Stitch outline) ─────────────────────────
    border = GovindOutlineVariant,                   // #C0C9BE
    borderFocused = GovindPrimaryContainer,          // #064520

    // ── Surfaces ─────────────────────────────────────────
    surfacePressed = GovindSurfaceContainerLow,      // #F0F5EE
    surfaceHover = GovindSurfaceContainer,           // #EBEFE8
    softGreen = GovindSecondaryContainer,            // #A7F690 (30% alpha used in UI)
    softFresh = GovindPrimaryFixed,                  // #B4F1BD
    softOrange = GovindTertiaryFixed,                // #FFDBD1
    softCream = Color(0xFFFFF8F0),

    // ── Skeleton ─────────────────────────────────────────
    skeleton = GovindSurfaceContainerHigh,           // #E5E9E3
    skeletonHighlight = GovindSurfaceContainerLow,   // #F0F5EE

    // ── Feedback ─────────────────────────────────────────
    error = GovindError,                             // #BA1A1A
    errorBg = GovindErrorContainer,                  // #FFDAD6
    success = GovindSecondary,                       // #246C18
    successBg = GovindSecondaryContainer,            // #A7F690

    // ── Wholesale ────────────────────────────────────────
    wholesaleNavy = Color(0xFF1A3038),
    wholesaleAmber = Color(0xFFC47D00),
    wholesaleTint = Color(0xFFF0F7EE),

    // ── Kitchen ──────────────────────────────────────────
    kitchenAccent = GovindOnTertiaryContainer,       // #FF815F
    kitchenTint = GovindTertiaryFixed,               // #FFDBD1

    // ── Inverse ──────────────────────────────────────────
    inverseSurface = GovindInverseSurface,           // #2D322D
    inverseOnSurface = GovindInverseOnSurface,       // #EDF2EB
    inversePrimary = GovindInversePrimary            // #98D5A2
)

object GovindTheme {
    val colors: GovindColors
        @Composable
        @ReadOnlyComposable
        get() = LocalGovindColors.current

    val typography: androidx.compose.material3.Typography
        @Composable
        @ReadOnlyComposable
        get() = androidx.compose.material3.MaterialTheme.typography

    val priceDisplay: androidx.compose.ui.text.TextStyle
        get() = PriceDisplay

    val priceStrikethrough: androidx.compose.ui.text.TextStyle
        get() = PriceStrikethrough
}
