package com.tosin.musicplayer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext

val AccentColors = listOf(
    Color(0xFF6750A4),
    Color(0xFFE91E63),
    Color(0xFFF44336),
    Color(0xFFFF9800),
    Color(0xFF4CAF50),
    Color(0xFF2196F3),
    Color(0xFF00BCD4),
    Color(0xFF9C27B0),
    Color(0xFF795548),
    Color(0xFF607D8B)
)

fun accentColorAt(index: Int): Color {
    if (index <= 0) return AccentColors.first()
    return AccentColors[index.coerceIn(AccentColors.indices)]
}

@Composable
fun TPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    themePreset: AppThemePreset = AppThemePreset.AMOLED,
    accentColorIndex: Int = 0,
    content: @Composable () -> Unit
) {
    val baseColorScheme = themePreset.colorScheme(darkTheme)
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        val dynamicScheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        baseColorScheme.copy(
            primary = dynamicScheme.primary,
            onPrimary = dynamicScheme.onPrimary,
            primaryContainer = dynamicScheme.primaryContainer,
            onPrimaryContainer = dynamicScheme.onPrimaryContainer,
            secondary = dynamicScheme.secondary,
            onSecondary = dynamicScheme.onSecondary,
            secondaryContainer = dynamicScheme.secondaryContainer,
            onSecondaryContainer = dynamicScheme.onSecondaryContainer,
            tertiary = dynamicScheme.tertiary,
            onTertiary = dynamicScheme.onTertiary,
            tertiaryContainer = dynamicScheme.tertiaryContainer,
            onTertiaryContainer = dynamicScheme.onTertiaryContainer,
            inversePrimary = dynamicScheme.inversePrimary
        )
    } else {
        baseColorScheme
    }

    val finalColorScheme = if (accentColorIndex > 0) {
        val accent = accentColorAt(accentColorIndex)
        val onAccent = if (accent.luminance() > 0.5f) Color.Black else Color.White
        colorScheme.copy(
            primary = accent,
            onPrimary = onAccent,
            primaryContainer = accent,
            onPrimaryContainer = onAccent
        )
    } else {
        colorScheme
    }

    MaterialTheme(
        colorScheme = finalColorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
