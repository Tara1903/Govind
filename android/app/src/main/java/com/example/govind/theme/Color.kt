package com.example.govind.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════
// GOVIND Design System Colors — Stitch Source of Truth
// ═══════════════════════════════════════════════════════════

// ── Primary / Brand ──────────────────────────────────────────
val GovindPrimary = Color(0xFF002D11)
val GovindPrimaryContainer = Color(0xFF064520)
val GovindOnPrimary = Color(0xFFFFFFFF)
val GovindOnPrimaryContainer = Color(0xFF78B383)
val GovindPrimaryFixed = Color(0xFFB4F1BD)
val GovindPrimaryFixedDim = Color(0xFF98D5A2)

// ── Secondary / Fresh Green ──────────────────────────────────
val GovindSecondary = Color(0xFF246C18)
val GovindSecondaryContainer = Color(0xFFA7F690)
val GovindOnSecondary = Color(0xFFFFFFFF)
val GovindOnSecondaryContainer = Color(0xFF2B731E)

// ── Tertiary / Kitchen Orange-Red ────────────────────────────
val GovindTertiary = Color(0xFF4D0E00)
val GovindTertiaryContainer = Color(0xFF741900)
val GovindOnTertiaryContainer = Color(0xFFFF815F)
val GovindTertiaryFixed = Color(0xFFFFDBD1)
val GovindTertiaryFixedDim = Color(0xFFFFB5A1)

// ── Surfaces ─────────────────────────────────────────────────
val GovindSurface = Color(0xFFF6FBF4)
val GovindSurfaceDim = Color(0xFFD7DBD5)
val GovindSurfaceContainerLowest = Color(0xFFFFFFFF)
val GovindSurfaceContainerLow = Color(0xFFF0F5EE)
val GovindSurfaceContainer = Color(0xFFEBEFE8)
val GovindSurfaceContainerHigh = Color(0xFFE5E9E3)
val GovindSurfaceContainerHighest = Color(0xFFDFE4DD)
val GovindSurfaceVariant = Color(0xFFDFE4DD)
val GovindOnSurface = Color(0xFF181D19)
val GovindOnSurfaceVariant = Color(0xFF414941)
val GovindBackground = Color(0xFFF6FBF4)

// ── Outline / Border ─────────────────────────────────────────
val GovindOutline = Color(0xFF717970)
val GovindOutlineVariant = Color(0xFFC0C9BE)

// ── Error ────────────────────────────────────────────────────
val GovindError = Color(0xFFBA1A1A)
val GovindErrorContainer = Color(0xFFFFDAD6)
val GovindOnError = Color(0xFFFFFFFF)
val GovindOnErrorContainer = Color(0xFF93000A)

// ── Inverse (Dark floating surfaces) ─────────────────────────
val GovindInverseSurface = Color(0xFF2D322D)
val GovindInverseOnSurface = Color(0xFFEDF2EB)
val GovindInversePrimary = Color(0xFF98D5A2)

// ── Surface Tint ─────────────────────────────────────────────
val GovindSurfaceTint = Color(0xFF316A40)

// ═══════════════════════════════════════════════════════════
// Legacy aliases (backward compat — prefer tokens above)
// ═══════════════════════════════════════════════════════════
val PrimaryDarkGreen = GovindPrimaryContainer
val FreshGreen = Color(0xFF38802A)
val Orange = Color(0xFFF5450D)
val WarmWhite = Color(0xFFFEFCF5)
val PureWhite = GovindSurfaceContainerLowest

val SoftGreenTint = Color(0xFFE8F3EB)
val FreshGreenTint = GovindPrimaryFixed
val SoftOrangeTint = GovindTertiaryFixed
val NeutralBorder = GovindOutlineVariant
val PrimaryText = GovindOnSurface
val SecondaryText = GovindOnSurfaceVariant
val MutedText = GovindOutline
val DisabledSurface = GovindSurfaceContainerHigh
val DisabledText = Color(0xFF9AA39E)
