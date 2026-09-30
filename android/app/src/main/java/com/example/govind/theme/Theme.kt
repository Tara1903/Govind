package com.example.govind.theme

import androidx.compose.runtime.CompositionLocalProvider
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ═══════════════════════════════════════════════════════════
// Color Schemes — Stitch Source of Truth
// ═══════════════════════════════════════════════════════════

private val GovindLightColorScheme = lightColorScheme(
    primary = GovindPrimaryContainer,        // #064520 — brand primary
    onPrimary = GovindOnPrimary,             // white
    primaryContainer = GovindPrimaryContainer,
    onPrimaryContainer = GovindOnPrimaryContainer,
    secondary = GovindSecondary,             // #246C18 — fresh green
    onSecondary = GovindOnSecondary,
    secondaryContainer = GovindSecondaryContainer,
    onSecondaryContainer = GovindOnSecondaryContainer,
    tertiary = GovindTertiary,
    onTertiary = Color.White,
    tertiaryContainer = GovindTertiaryContainer,
    onTertiaryContainer = GovindOnTertiaryContainer,
    background = GovindBackground,           // #F6FBF4
    onBackground = GovindOnSurface,          // #181D19
    surface = GovindSurface,                 // #F6FBF4
    onSurface = GovindOnSurface,
    surfaceVariant = GovindSurfaceVariant,   // #DFE4DD
    onSurfaceVariant = GovindOnSurfaceVariant,
    surfaceTint = GovindSurfaceTint,
    inverseSurface = GovindInverseSurface,
    inverseOnSurface = GovindInverseOnSurface,
    inversePrimary = GovindInversePrimary,
    outline = GovindOutline,
    outlineVariant = GovindOutlineVariant,
    error = GovindError,
    onError = GovindOnError,
    errorContainer = GovindErrorContainer,
    onErrorContainer = GovindOnErrorContainer,
    surfaceContainerLowest = GovindSurfaceContainerLowest,
    surfaceContainerLow = GovindSurfaceContainerLow,
    surfaceContainer = GovindSurfaceContainer,
    surfaceContainerHigh = GovindSurfaceContainerHigh,
    surfaceContainerHighest = GovindSurfaceContainerHighest,
    surfaceDim = GovindSurfaceDim,
    surfaceBright = GovindSurface
)

private val GovindDarkColorScheme = darkColorScheme(
    primary = GovindInversePrimary,
    onPrimary = GovindPrimary,
    primaryContainer = GovindPrimaryContainer,
    onPrimaryContainer = GovindOnPrimaryContainer,
    secondary = GovindSecondaryContainer,
    onSecondary = Color(0xFF012200),
    background = Color(0xFF111511),
    onBackground = GovindInverseOnSurface,
    surface = Color(0xFF111511),
    onSurface = GovindInverseOnSurface,
    surfaceVariant = Color(0xFF2D322D),
    onSurfaceVariant = GovindOutlineVariant,
    inverseSurface = GovindSurfaceContainerHighest,
    inverseOnSurface = GovindInverseSurface,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = GovindErrorContainer,
    outline = Color(0xFF8A938A),
    outlineVariant = GovindOnSurfaceVariant
)

@Composable
fun GovindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> GovindDarkColorScheme
        else -> GovindLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalGovindColors provides defaultGovindColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = GovindTypography,
            shapes = Shapes,
            content = content
        )
    }
}
