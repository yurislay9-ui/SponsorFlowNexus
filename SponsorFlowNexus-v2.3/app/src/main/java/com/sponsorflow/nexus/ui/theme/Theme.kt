/*
 * SponsorFlow Nexus v2.4 - Compose Theme
 * Material 3 Design System
 */
package com.sponsorflow.nexus.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Colores del Brand - SponsorFlow Nexus
private val NexusPrimary = Color(0xFF6366F1) // Indigo
private val NexusSecondary = Color(0xFF8B5CF6) // Violet
private val NexusTertiary = Color(0xFF06B6D4) // Cyan
val NexusSuccess = Color(0xFF10B981) // Emerald
val NexusWarning = Color(0xFFF59E0B) // Amber
private val NexusError = Color(0xFFEF4444) // Red

// Dark Theme
private val DarkColorScheme = darkColorScheme(
    primary = NexusPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4338CA),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = NexusSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF6D28D9),
    onSecondaryContainer = Color(0xFFEDE9FE),
    tertiary = NexusTertiary,
    onTertiary = Color.White,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF1F5F9),
    error = NexusError,
    onError = Color.White
)

// Light Theme
private val LightColorScheme = lightColorScheme(
    primary = NexusPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF312E81),
    secondary = NexusSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF4C1D95),
    tertiary = NexusTertiary,
    onTertiary = Color.White,
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1E293B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1E293B),
    error = NexusError,
    onError = Color.White
)

@Composable
fun NexusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NexusTypography,
        content = content
    )
}