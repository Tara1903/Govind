package com.example.govind.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = FreshGreen,
    onPrimary = PureWhite,
    primaryContainer = PrimaryDarkGreen,
    onPrimaryContainer = PureWhite,
    secondary = Orange,
    onSecondary = PureWhite,
    background = Color(0xFF121212),
    onBackground = PureWhite,
    surface = Color(0xFF1E1E1E),
    onSurface = PureWhite,
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFFB3B3B3)
)

private val LightColorScheme = lightColorScheme(
    primary = FreshGreen,
    onPrimary = PureWhite,
    primaryContainer = LightGreenBackground,
    onPrimaryContainer = PrimaryDarkGreen,
    secondary = Orange,
    onSecondary = PureWhite,
    background = WarmWhite,
    onBackground = TextPrimary,
    surface = PureWhite,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = TextSecondary,
    outline = DividerColor
)

@Composable
fun GovindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors to strictly enforce brand colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Edge-to-edge transparency is handled by enableEdgeToEdge in MainActivity
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
