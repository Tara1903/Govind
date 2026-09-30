package com.example.govind.theme

import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════════════
// Spacing & Dimension Tokens — Stitch Source of Truth
// ═══════════════════════════════════════════════════════════

object Dimens {
    // ── Stitch Spacing Scale ─────────────────────────────
    val SpaceXs = 4.dp       // space-xs
    val SpaceSm = 8.dp       // space-sm
    val SpaceMd = 12.dp      // space-md
    val SpaceLg = 16.dp      // space-lg
    val SpaceXl = 24.dp      // space-xl

    // ── Layout Margins ───────────────────────────────────
    val Margin = 16.dp       // Screen horizontal margin (mobile)
    val MarginDesktop = 32.dp

    // ── Grid Gutter ──────────────────────────────────────
    val Gutter = 12.dp       // Grid gutter (mobile)
    val GutterDesktop = 24.dp

    // ── Legacy aliases ───────────────────────────────────
    val PaddingMicro = SpaceXs
    val PaddingSmall = SpaceSm
    val PaddingMedium = SpaceLg
    val PaddingLarge = SpaceXl
    val PaddingExtraLarge = 32.dp

    // ── Icon Sizes ───────────────────────────────────────
    val IconSizeXs = 14.dp
    val IconSizeSmall = 16.dp
    val IconSizeMedium = 20.dp
    val IconSizeLarge = 24.dp
    val IconSizeXl = 32.dp

    // ── Corner Radius ────────────────────────────────────
    val CornerRadiusSmall = 8.dp
    val CornerRadiusMedium = 12.dp
    val CornerRadiusLarge = 16.dp
    val CornerRadiusExtraLarge = 28.dp
    val CornerRadiusFull = 9999.dp

    val RadiusCard = CornerRadiusLarge
    val RadiusLg = CornerRadiusSmall
    val RadiusXl = CornerRadiusMedium
    val RadiusFull = CornerRadiusFull

    // ── Component Sizes ──────────────────────────────────
    val ProductCardWidth = 160.dp
    val ProductCardImageHeight = 112.dp // h-28 = 7rem = 112dp
    val CategoryCircleSize = 52.dp     // w-13 h-13
    val CategoryImageSize = 40.dp      // w-10 h-10
    val TopBarLogoHeight = 32.dp       // h-8
    val CartBadgeSize = 16.dp          // w-4 h-4
    val ProfileAvatarSize = 32.dp      // w-8 h-8
    val ExperiencePillMinHeight = 44.dp
    val BottomNavHeight = 56.dp
    val FloatingDockHeight = 56.dp
    val AddButtonMinHeight = 32.dp
    val SearchBarHeight = 48.dp

    // ── Touch targets ────────────────────────────────────
    val MinTouchTarget = 44.dp
}
