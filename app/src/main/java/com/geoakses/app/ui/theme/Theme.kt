package com.geoakses.app.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = White,
    primaryContainer = Color(0xFFD9E8EF),
    onPrimaryContainer = BrandBlue,
    secondary = BrandTeal,
    onSecondary = White,
    tertiary = RouteGold,
    onTertiary = Ink,
    background = Cloud,
    onBackground = Ink,
    surface = White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE8EFF1),
    onSurfaceVariant = Muted,
    outline = Line
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF416B7F),
    onPrimary = White,
    primaryContainer = BrandBlue,
    onPrimaryContainer = Color(0xFFD9E8EF),
    secondary = Color(0xFF74C8B7),
    onSecondary = Night,
    tertiary = RouteGold,
    onTertiary = Night,
    background = Night,
    onBackground = Color(0xFFE8F0F2),
    surface = NightSurface,
    onSurface = Color(0xFFE8F0F2),
    surfaceVariant = NightRaised,
    onSurfaceVariant = Color(0xFFB5C4CA),
    outline = Color(0xFF425963)
)

@Composable
fun GeoAksesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
            window.statusBarColor = colorScheme.primary.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
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
