package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimaryDark,
    onPrimary = Navy900,
    primaryContainer = Navy700,
    onPrimaryContainer = BluePrimaryDark,
    secondary = CyanAccentDark,
    onSecondary = Color(0xFF00363F),
    secondaryContainer = Color(0xFF004E5A),
    onSecondaryContainer = Color(0xFFB5E8F7),
    background = SlateSurfaceDark,
    onBackground = Color(0xFFE2E6F2),
    surface = CardSurfaceDark,
    onSurface = Color(0xFFE2E6F2),
    surfaceVariant = Color(0xFF222D47),
    onSurfaceVariant = Color(0xFFC3C7D7),
    error = CrimsonDangerDark,
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = CyanAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFBFE9F2),
    onSecondaryContainer = Color(0xFF001F25),
    background = SlateSurfaceLight,
    onBackground = Color(0xFF191C20),
    surface = Color.White,
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFE0E3ED),
    onSurfaceVariant = Color(0xFF444750),
    error = CrimsonDanger,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our specialized enterprise colors for consistent MDM feel
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
