package com.tosin.musicplayer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun TPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    themePreset: AppThemePreset = AppThemePreset.AMOLED,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
