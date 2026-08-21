package com.tosin.musicplayer.ui.theme.engine

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Granular design tokens that govern rendering, geometry, lighting, and typography.
 * Marked as @Immutable for optimal Compose recomposition skipping.
 */
@Immutable
data class ThemeParameters(
    // ── Color System ──
    val primaryColor: Color,
    val secondaryColor: Color,
    val tertiaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val surfaceVariantColor: Color,
    val onPrimaryColor: Color,
    val onSurfaceColor: Color,
    val onBackgroundColor: Color,

    // ── Geometry & Spatial Geometry ──
    val cornerRadiusScale: Float = 1.0f,      // Multiplier from 0.0f (Sharp) to 2.0f (Hyper-curved)
    val borderStrokeWidth: Dp = 1.dp,         // Stroke thickness (0.dp to 4.dp)
    val borderColor: Color = Color.Transparent,

    // ── Optical & Material Effects ──
    val surfaceBlur: Dp = 0.dp,               // Glassmorphism blur radius (0.dp to 24.dp)
    val surfaceAlpha: Float = 1.0f,           // Transparency for glassmorphism (0.2f to 1.0f)
    val shadowElevation: Dp = 4.dp,           // Depth/Elevation for physical/neumorphic shadows
    val shadowColor: Color = Color.Black.copy(alpha = 0.25f),
    val highlightColor: Color = Color.White.copy(alpha = 0.6f), // Light-source highlight for Neumorphism/Clay

    // ── Specialty Shader Tokens ──
    val glowRadius: Dp = 0.dp,                // Neon glow aura for Cyberpunk (0.dp to 20.dp)
    val glowColor: Color = Color.Transparent,

    // ── Typography Tokens ──
    val fontScale: Float = 1.0f,              // Dynamic font scaling (0.8f to 1.4f)
    val useSystemFont: Boolean = false,       // Toggle custom font family vs system default
    val isDark: Boolean = true
) {
    companion object {
        /**
         * Material Expressive default token factory
         */
        fun materialExpressiveDefault(isDark: Boolean = true): ThemeParameters {
            return if (isDark) {
                ThemeParameters(
                    primaryColor = Color(0xFFD0BCFF),
                    secondaryColor = Color(0xFFCCC2DC),
                    tertiaryColor = Color(0xFFEFB8C8),
                    backgroundColor = Color(0xFF141218),
                    surfaceColor = Color(0xFF1D1B20),
                    surfaceVariantColor = Color(0xFF49454F),
                    onPrimaryColor = Color(0xFF381E72),
                    onSurfaceColor = Color(0xFFE6E0E9),
                    onBackgroundColor = Color(0xFFE6E0E9),
                    cornerRadiusScale = 1.0f,
                    borderStrokeWidth = 0.dp,
                    borderColor = Color.Transparent,
                    surfaceBlur = 0.dp,
                    surfaceAlpha = 1.0f,
                    shadowElevation = 3.dp,
                    shadowColor = Color.Black.copy(alpha = 0.3f),
                    glowRadius = 0.dp,
                    glowColor = Color.Transparent,
                    fontScale = 1.0f,
                    useSystemFont = true,
                    isDark = true
                )
            } else {
                ThemeParameters(
                    primaryColor = Color(0xFF6750A4),
                    secondaryColor = Color(0xFF625B71),
                    tertiaryColor = Color(0xFF7D5260),
                    backgroundColor = Color(0xFFFEF7FF),
                    surfaceColor = Color(0xFFF7F2FA),
                    surfaceVariantColor = Color(0xFFE7E0EC),
                    onPrimaryColor = Color.White,
                    onSurfaceColor = Color(0xFF1D1B20),
                    onBackgroundColor = Color(0xFF1D1B20),
                    cornerRadiusScale = 1.0f,
                    borderStrokeWidth = 0.dp,
                    borderColor = Color.Transparent,
                    surfaceBlur = 0.dp,
                    surfaceAlpha = 1.0f,
                    shadowElevation = 2.dp,
                    shadowColor = Color.Black.copy(alpha = 0.12f),
                    glowRadius = 0.dp,
                    glowColor = Color.Transparent,
                    fontScale = 1.0f,
                    useSystemFont = true,
                    isDark = false
                )
            }
        }

        /**
         * Cyberpunk Neon default token factory
         */
        fun cyberpunkDefault(isDark: Boolean = true): ThemeParameters {
            val neonPink = Color(0xFFFF007F)
            val neonCyan = Color(0xFF00F0FF)
            val neonYellow = Color(0xFFFFE600)
            return ThemeParameters(
                primaryColor = neonPink,
                secondaryColor = neonCyan,
                tertiaryColor = neonYellow,
                backgroundColor = Color(0xFF08080E),
                surfaceColor = Color(0xFF12111A),
                surfaceVariantColor = Color(0xFF1C1A29),
                onPrimaryColor = Color.Black,
                onSurfaceColor = Color(0xFFF0F0FF),
                onBackgroundColor = Color(0xFFF0F0FF),
                cornerRadiusScale = 0.4f, // Sharp industrial chamfered geometry
                borderStrokeWidth = 1.5.dp,
                borderColor = neonCyan.copy(alpha = 0.7f),
                surfaceBlur = 8.dp,       // Glass HUD aesthetic
                surfaceAlpha = 0.85f,
                shadowElevation = 0.dp,
                glowRadius = 10.dp,       // Radiant neon aura
                glowColor = neonPink.copy(alpha = 0.6f),
                fontScale = 1.0f,
                useSystemFont = false,    // Monospace / Tech aesthetic
                isDark = true
            )
        }

        /**
         * Neumorphism default token factory
         */
        fun neumorphismDefault(isDark: Boolean = true): ThemeParameters {
            return if (isDark) {
                val base = Color(0xFF24272C)
                ThemeParameters(
                    primaryColor = Color(0xFF6C8CFF),
                    secondaryColor = Color(0xFF4EE3B7),
                    tertiaryColor = Color(0xFFFF7597),
                    backgroundColor = base,
                    surfaceColor = base,
                    surfaceVariantColor = Color(0xFF2C3036),
                    onPrimaryColor = Color.White,
                    onSurfaceColor = Color(0xFFE2E4E9),
                    onBackgroundColor = Color(0xFFE2E4E9),
                    cornerRadiusScale = 1.3f,
                    borderStrokeWidth = 0.dp,
                    borderColor = Color.Transparent,
                    surfaceBlur = 0.dp,
                    surfaceAlpha = 1.0f,
                    shadowElevation = 6.dp,
                    shadowColor = Color(0xFF181A1E).copy(alpha = 0.9f),
                    highlightColor = Color(0xFF32363D).copy(alpha = 0.8f),
                    glowRadius = 0.dp,
                    glowColor = Color.Transparent,
                    fontScale = 1.0f,
                    useSystemFont = true,
                    isDark = true
                )
            } else {
                val base = Color(0xFFE8ECF2)
                ThemeParameters(
                    primaryColor = Color(0xFF3D66FF),
                    secondaryColor = Color(0xFF00BFA5),
                    tertiaryColor = Color(0xFFFF4081),
                    backgroundColor = base,
                    surfaceColor = base,
                    surfaceVariantColor = Color(0xFFDCE2EB),
                    onPrimaryColor = Color.White,
                    onSurfaceColor = Color(0xFF2D3139),
                    onBackgroundColor = Color(0xFF2D3139),
                    cornerRadiusScale = 1.3f,
                    borderStrokeWidth = 0.dp,
                    borderColor = Color.Transparent,
                    surfaceBlur = 0.dp,
                    surfaceAlpha = 1.0f,
                    shadowElevation = 6.dp,
                    shadowColor = Color(0xFFA6B4C9).copy(alpha = 0.7f),
                    highlightColor = Color.White.copy(alpha = 0.9f),
                    glowRadius = 0.dp,
                    glowColor = Color.Transparent,
                    fontScale = 1.0f,
                    useSystemFont = true,
                    isDark = false
                )
            }
        }

        /**
         * Claymorphism default token factory
         */
        fun claymorphismDefault(isDark: Boolean = true): ThemeParameters {
            return if (isDark) {
                ThemeParameters(
                    primaryColor = Color(0xFFFF7AC6),
                    secondaryColor = Color(0xFF78E5EB),
                    tertiaryColor = Color(0xFFFFD166),
                    backgroundColor = Color(0xFF1F1D2B),
                    surfaceColor = Color(0xFF2A2739),
                    surfaceVariantColor = Color(0xFF353147),
                    onPrimaryColor = Color(0xFF1F1D2B),
                    onSurfaceColor = Color(0xFFF4F5F8),
                    onBackgroundColor = Color(0xFFF4F5F8),
                    cornerRadiusScale = 1.6f, // Pillowy hyper-rounded shapes
                    borderStrokeWidth = 1.dp,
                    borderColor = Color.White.copy(alpha = 0.15f),
                    surfaceBlur = 0.dp,
                    surfaceAlpha = 1.0f,
                    shadowElevation = 8.dp,
                    shadowColor = Color(0xFF100E17).copy(alpha = 0.8f),
                    highlightColor = Color.White.copy(alpha = 0.25f),
                    glowRadius = 0.dp,
                    glowColor = Color.Transparent,
                    fontScale = 1.0f,
                    useSystemFont = true,
                    isDark = true
                )
            } else {
                ThemeParameters(
                    primaryColor = Color(0xFFFF5286),
                    secondaryColor = Color(0xFF48CAE4),
                    tertiaryColor = Color(0xFFFFB703),
                    backgroundColor = Color(0xFFF0F4FD),
                    surfaceColor = Color(0xFFFFFFFF),
                    surfaceVariantColor = Color(0xFFE4EDFA),
                    onPrimaryColor = Color.White,
                    onSurfaceColor = Color(0xFF1E2433),
                    onBackgroundColor = Color(0xFF1E2433),
                    cornerRadiusScale = 1.6f,
                    borderStrokeWidth = 1.5.dp,
                    borderColor = Color.White.copy(alpha = 0.8f),
                    surfaceBlur = 0.dp,
                    surfaceAlpha = 1.0f,
                    shadowElevation = 10.dp,
                    shadowColor = Color(0xFFB5C8E8).copy(alpha = 0.6f),
                    highlightColor = Color.White,
                    glowRadius = 0.dp,
                    glowColor = Color.Transparent,
                    fontScale = 1.0f,
                    useSystemFont = true,
                    isDark = false
                )
            }
        }

        /**
         * Brutalism / raw web default token factory
         */
        fun brutalismDefault(isDark: Boolean = true): ThemeParameters {
            return if (isDark) {
                ThemeParameters(
                    primaryColor = Color(0xFFFF3333),
                    secondaryColor = Color(0xFF3366FF),
                    tertiaryColor = Color(0xFFFFD600),
                    backgroundColor = Color(0xFF1A1A1A),
                    surfaceColor = Color(0xFF2A2A2A),
                    surfaceVariantColor = Color(0xFF333333),
                    onPrimaryColor = Color.White,
                    onSurfaceColor = Color.White,
                    onBackgroundColor = Color.White,
                    cornerRadiusScale = 0.0f,
                    borderStrokeWidth = 3.dp,
                    borderColor = Color.White,
                    surfaceBlur = 0.dp,
                    surfaceAlpha = 1.0f,
                    shadowElevation = 6.dp,
                    shadowColor = Color.Black,
                    highlightColor = Color.Transparent,
                    glowRadius = 0.dp,
                    glowColor = Color.Transparent,
                    fontScale = 1.1f,
                    useSystemFont = false,
                    isDark = true
                )
            } else {
                ThemeParameters(
                    primaryColor = Color(0xFFFF0000),
                    secondaryColor = Color(0xFF0000FF),
                    tertiaryColor = Color(0xFFFFD600),
                    backgroundColor = Color(0xFFF5F0E8),
                    surfaceColor = Color.White,
                    surfaceVariantColor = Color(0xFFE8E0D4),
                    onPrimaryColor = Color.White,
                    onSurfaceColor = Color.Black,
                    onBackgroundColor = Color.Black,
                    cornerRadiusScale = 0.0f,
                    borderStrokeWidth = 3.dp,
                    borderColor = Color.Black,
                    surfaceBlur = 0.dp,
                    surfaceAlpha = 1.0f,
                    shadowElevation = 6.dp,
                    shadowColor = Color.Black,
                    highlightColor = Color.Transparent,
                    glowRadius = 0.dp,
                    glowColor = Color.Transparent,
                    fontScale = 1.1f,
                    useSystemFont = false,
                    isDark = false
                )
            }
        }

        /**
         * Retro Monochrome / Neo-Brutalist default token factory
         */
        fun retroMonoDefault(isDark: Boolean = true): ThemeParameters {
            return if (isDark) {
                ThemeParameters(
                    primaryColor = Color(0xFF00FF66), // Retro Terminal Green
                    secondaryColor = Color(0xFFFFCC00),
                    tertiaryColor = Color(0xFFFFFFFF),
                    backgroundColor = Color(0xFF000000),
                    surfaceColor = Color(0xFF111111),
                    surfaceVariantColor = Color(0xFF222222),
                    onPrimaryColor = Color.Black,
                    onSurfaceColor = Color(0xFF00FF66),
                    onBackgroundColor = Color(0xFF00FF66),
                    cornerRadiusScale = 0.0f, // Sharp boxy edges
                    borderStrokeWidth = 2.5.dp,
                    borderColor = Color(0xFF00FF66),
                    surfaceBlur = 0.dp,
                    surfaceAlpha = 1.0f,
                    shadowElevation = 5.dp, // Hard offset shadow
                    shadowColor = Color(0xFF00FF66).copy(alpha = 0.4f),
                    glowRadius = 0.dp,
                    glowColor = Color.Transparent,
                    fontScale = 1.0f,
                    useSystemFont = false, // Monospace compulsory
                    isDark = true
                )
            } else {
                ThemeParameters(
                    primaryColor = Color(0xFF000000),
                    secondaryColor = Color(0xFFFF3366),
                    tertiaryColor = Color(0xFF0066FF),
                    backgroundColor = Color(0xFFF4F0EA), // Newsprint cream
                    surfaceColor = Color(0xFFFFFFFF),
                    surfaceVariantColor = Color(0xFFE2DDD5),
                    onPrimaryColor = Color.White,
                    onSurfaceColor = Color.Black,
                    onBackgroundColor = Color.Black,
                    cornerRadiusScale = 0.0f,
                    borderStrokeWidth = 3.dp,
                    borderColor = Color.Black,
                    surfaceBlur = 0.dp,
                    surfaceAlpha = 1.0f,
                    shadowElevation = 6.dp, // Chunky brutalist black shadow
                    shadowColor = Color.Black,
                    glowRadius = 0.dp,
                    glowColor = Color.Transparent,
                    fontScale = 1.0f,
                    useSystemFont = false,
                    isDark = false
                )
            }
        }
    }
}

/**
 * Root theme state encapsulating the active style and customized parameters for every preset.
 */
data class ThemeState(
    val activeStyle: ThemeStyle = ThemeStyle.MATERIAL_EXPRESSIVE,
    val styleConfigs: Map<ThemeStyle, ThemeParameters> = ThemeStyle.entries.associateWith { style ->
        when (style) {
            ThemeStyle.MATERIAL_EXPRESSIVE -> ThemeParameters.materialExpressiveDefault()
            ThemeStyle.CYBERPUNK -> ThemeParameters.cyberpunkDefault()
            ThemeStyle.NEUMORPHISM -> ThemeParameters.neumorphismDefault()
            ThemeStyle.CLAYMORPHISM -> ThemeParameters.claymorphismDefault()
            ThemeStyle.RETRO_MONO -> ThemeParameters.retroMonoDefault()
            ThemeStyle.BRUTALISM -> ThemeParameters.brutalismDefault()
        }
    }
) {
    val currentParams: ThemeParameters
        get() = styleConfigs[activeStyle] ?: ThemeParameters.materialExpressiveDefault()
}

/**
 * CompositionLocal tokens for injection into the Compose hierarchy.
 */
val LocalAppThemeParameters = staticCompositionLocalOf {
    ThemeParameters.materialExpressiveDefault()
}

val LocalThemeStyle = staticCompositionLocalOf {
    ThemeStyle.MATERIAL_EXPRESSIVE
}

/**
 * Unified accessor for both custom tokens and standard Material 3 tokens.
 */
object AppTheme {
    val parameters: ThemeParameters
        @Composable
        @ReadOnlyComposable
        get() = LocalAppThemeParameters.current

    val style: ThemeStyle
        @Composable
        @ReadOnlyComposable
        get() = LocalThemeStyle.current

    val colorScheme: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme
}
