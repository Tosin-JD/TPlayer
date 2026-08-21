package com.tosin.musicplayer.ui.theme.engine

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
fun UniversalAppTheme(
    state: ThemeState,
    content: @Composable () -> Unit
) {
    val currentParams = state.currentParams
    val style = state.activeStyle

    // ── Smooth animated transitions for colors ──
    val animatedPrimary by animateColorAsState(currentParams.primaryColor, tween(300), label = "primary")
    val animatedSecondary by animateColorAsState(currentParams.secondaryColor, tween(300), label = "secondary")
    val animatedBackground by animateColorAsState(currentParams.backgroundColor, tween(300), label = "bg")
    val animatedSurface by animateColorAsState(currentParams.surfaceColor, tween(300), label = "surface")
    val animatedSurfaceVariant by animateColorAsState(currentParams.surfaceVariantColor, tween(300), label = "surfaceVariant")

    // ── Material 3 ColorScheme Bridge ──
    // Derive secondary/primary containers from theme tokens
    // Use higher alpha so FABs, chips, and icon containers are opaque and icons are visible
    val secondaryContainerColor = animatedSecondary.copy(alpha = 0.30f)
    val onSecondaryContainerColor = animatedSecondary
    val primaryContainerColor = animatedPrimary.copy(alpha = 0.30f)
    val onPrimaryContainerColor = animatedPrimary
    val surfaceContainerLowest = animatedBackground
    val surfaceContainerLow = animatedSurface  // Fully opaque

    val m3ColorScheme: ColorScheme = if (currentParams.isDark) {
        darkColorScheme(
            primary = animatedPrimary,
            onPrimary = currentParams.onPrimaryColor,
            primaryContainer = primaryContainerColor,
            onPrimaryContainer = onPrimaryContainerColor,
            secondary = animatedSecondary,
            onSecondary = currentParams.onPrimaryColor,
            secondaryContainer = secondaryContainerColor,
            onSecondaryContainer = onSecondaryContainerColor,
            tertiary = currentParams.tertiaryColor,
            background = animatedBackground,
            onBackground = currentParams.onBackgroundColor,
            surface = animatedSurface,
            onSurface = currentParams.onSurfaceColor,
            surfaceVariant = animatedSurfaceVariant,
            onSurfaceVariant = currentParams.onSurfaceColor.copy(alpha = 0.85f),
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = animatedSurfaceVariant,
            surfaceContainerHigh = animatedSurfaceVariant,
            surfaceContainerHighest = animatedSurfaceVariant,
            outline = if (currentParams.borderStrokeWidth > 0.dp) currentParams.borderColor else currentParams.onSurfaceColor.copy(alpha = 0.2f)
        )
    } else {
        lightColorScheme(
            primary = animatedPrimary,
            onPrimary = currentParams.onPrimaryColor,
            primaryContainer = primaryContainerColor,
            onPrimaryContainer = onPrimaryContainerColor,
            secondary = animatedSecondary,
            onSecondary = currentParams.onPrimaryColor,
            secondaryContainer = secondaryContainerColor,
            onSecondaryContainer = onSecondaryContainerColor,
            tertiary = currentParams.tertiaryColor,
            background = animatedBackground,
            onBackground = currentParams.onBackgroundColor,
            surface = animatedSurface,
            onSurface = currentParams.onSurfaceColor,
            surfaceVariant = animatedSurfaceVariant,
            onSurfaceVariant = currentParams.onSurfaceColor.copy(alpha = 0.85f),
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = animatedSurfaceVariant,
            surfaceContainerHigh = animatedSurfaceVariant,
            surfaceContainerHighest = animatedSurfaceVariant,
            outline = if (currentParams.borderStrokeWidth > 0.dp) currentParams.borderColor else currentParams.onSurfaceColor.copy(alpha = 0.2f)
        )
    }

    // ── Dynamic Shape Scaling ──
    val scale = currentParams.cornerRadiusScale
    val dynamicShapes = Shapes(
        extraSmall = RoundedCornerShape((2f * scale).dp),
        small = RoundedCornerShape((2f * scale).dp),
        medium = RoundedCornerShape((3f * scale).dp),
        large = RoundedCornerShape((4f * scale).dp),
        extraLarge = RoundedCornerShape((4f * scale).dp)
    )

    // ── Dynamic Typography Bridge ──
    val activeFontFamily = when {
        currentParams.useSystemFont -> FontFamily.Default
        style == ThemeStyle.RETRO_MONO || style == ThemeStyle.CYBERPUNK -> FontFamily.Monospace
        else -> FontFamily.SansSerif
    }

    val dynamicTypography = buildScaledTypography(activeFontFamily, currentParams.fontScale)

    // ── Provide tokens to entire composition subtree ──
    CompositionLocalProvider(
        LocalAppThemeParameters provides currentParams,
        LocalThemeStyle provides style
    ) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            shapes = dynamicShapes,
            typography = dynamicTypography,
            content = content
        )
    }
}

/**
 * Builds scaled typography respecting user fontScale override and active FontFamily.
 */
private fun buildScaledTypography(fontFamily: FontFamily, scale: Float): Typography {
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = fontFamily, fontSize = base.displayLarge.fontSize * scale),
        displayMedium = base.displayMedium.copy(fontFamily = fontFamily, fontSize = base.displayMedium.fontSize * scale),
        displaySmall = base.displaySmall.copy(fontFamily = fontFamily, fontSize = base.displaySmall.fontSize * scale),
        headlineLarge = base.headlineLarge.copy(fontFamily = fontFamily, fontSize = base.headlineLarge.fontSize * scale),
        headlineMedium = base.headlineMedium.copy(fontFamily = fontFamily, fontSize = base.headlineMedium.fontSize * scale),
        headlineSmall = base.headlineSmall.copy(fontFamily = fontFamily, fontSize = base.headlineSmall.fontSize * scale),
        titleLarge = base.titleLarge.copy(fontFamily = fontFamily, fontSize = base.titleLarge.fontSize * scale),
        titleMedium = base.titleMedium.copy(fontFamily = fontFamily, fontSize = base.titleMedium.fontSize * scale),
        titleSmall = base.titleSmall.copy(fontFamily = fontFamily, fontSize = base.titleSmall.fontSize * scale),
        bodyLarge = base.bodyLarge.copy(fontFamily = fontFamily, fontSize = base.bodyLarge.fontSize * scale),
        bodyMedium = base.bodyMedium.copy(fontFamily = fontFamily, fontSize = base.bodyMedium.fontSize * scale),
        bodySmall = base.bodySmall.copy(fontFamily = fontFamily, fontSize = base.bodySmall.fontSize * scale),
        labelLarge = base.labelLarge.copy(fontFamily = fontFamily, fontSize = base.labelLarge.fontSize * scale),
        labelMedium = base.labelMedium.copy(fontFamily = fontFamily, fontSize = base.labelMedium.fontSize * scale),
        labelSmall = base.labelSmall.copy(fontFamily = fontFamily, fontSize = base.labelSmall.fontSize * scale)
    )
}
