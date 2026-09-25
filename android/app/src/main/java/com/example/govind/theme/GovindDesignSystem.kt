package com.example.govind.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class GovindColors(
    val govindGreen: Color,
    val freshGreen: Color,
    val govindOrange: Color,
    val warmWhite: Color,
    val pureWhite: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textOnDark: Color,
    val textOnOrange: Color,
    val border: Color,
    val borderFocused: Color,
    val surfacePressed: Color,
    val surfaceHover: Color,
    val softGreen: Color,
    val softFresh: Color,
    val softOrange: Color,
    val softCream: Color,
    val skeleton: Color,
    val skeletonHighlight: Color,
    val error: Color,
    val errorBg: Color,
    val success: Color,
    val successBg: Color
)

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
        successBg = Color.Unspecified
    )
}

val defaultGovindColors = GovindColors(
    govindGreen = Color(0xFF064520),
    freshGreen = Color(0xFF38802A),
    govindOrange = Color(0xFFF5450D),
    warmWhite = Color(0xFFFEFCF5),
    pureWhite = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF163126),
    textSecondary = Color(0xFF617068),
    textMuted = Color(0xFF8A958F),
    textOnDark = Color(0xFFFEFCF5),
    textOnOrange = Color(0xFFFFFFFF),
    border = Color(0xFFDDE5DF),
    borderFocused = Color(0xFF064520),
    surfacePressed = Color(0xFFEFF6F0),
    surfaceHover = Color(0xFFF5F9F6),
    softGreen = Color(0xFFEFF6F0),
    softFresh = Color(0xFFF0F7EE),
    softOrange = Color(0xFFFFF0EB),
    softCream = Color(0xFFFFF8F0),
    skeleton = Color(0xFFEEF1EF),
    skeletonHighlight = Color(0xFFF8FAF8),
    error = Color(0xFFDC2626),
    errorBg = Color(0xFFFEF2F2),
    success = Color(0xFF16A34A),
    successBg = Color(0xFFF0FDF4)
)

object GovindTheme {
    val colors: GovindColors
        @Composable
        @ReadOnlyComposable
        get() = LocalGovindColors.current
}
