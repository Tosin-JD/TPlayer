package com.tosin.musicplayer.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class AppThemePreset(val label: String) {
    AMOLED("Amoled"),
    SEPIA("Sepia"),
    HIGH_CONTRAST("High Contrast"),
    GRAYSCALE("Grayscale"),
    MONOCHROME("Monochrome"),
    SOLARIZED("Solarized"),
    NORD("Nord"),
    DRACULA("Dracula"),
    RETRO("Retro"),
    NEON("Neon"),
    PASTEL("Pastel"),
    CYBERPUNK("Cyberpunk"),
    OCEAN("Ocean"),
    FOREST("Forest"),
    SUNSET("Sunset"),
    MINIMALIST("Minimalist"),
    VIBRANT("Vibrant"),
    EARTHY("Earthy"),
    LAVENDER("Lavender"),
    MIDNIGHT_BLUE("Midnight Blue"),
    WARM_SEPIA("Warm Sepia");

    companion object {
        fun fromStored(value: String): AppThemePreset {
            return entries.firstOrNull { it.label.equals(value, ignoreCase = true) } ?: AMOLED
        }
    }
}

fun AppThemePreset.colorScheme(darkTheme: Boolean): ColorScheme {
    return when (this) {
        AppThemePreset.AMOLED -> buildScheme(
            darkTheme = darkTheme,
            primary = Color(0xFFB39DFF),
            secondary = Color(0xFF00E5FF),
            tertiary = Color(0xFFFF80AB),
            background = Color.Black,
            surface = Color(0xFF000000),
            surfaceVariant = Color(0xFF141414),
            outline = Color(0xFF707070),
            surfaceLowest = Color.Black,
            surfaceLow = Color(0xFF050505),
            surfaceHigh = Color(0xFF111111),
            surfaceHighest = Color(0xFF1B1B1B),
            inverseSurface = Color(0xFFF5F5F5),
            inverseOnSurface = Color(0xFF121212),
            inversePrimary = Color(0xFF6750A4)
        )

        AppThemePreset.SEPIA -> buildScheme(
            darkTheme = darkTheme,
            primary = Color(0xFFB87333),
            secondary = Color(0xFF8D6E63),
            tertiary = Color(0xFFA1887F),
            background = Color(0xFFF6E7D7),
            surface = Color(0xFFF9F0E6),
            surfaceVariant = Color(0xFFE8D4BE),
            outline = Color(0xFF8D6E63),
            surfaceLowest = Color(0xFFFFFCF8),
            surfaceLow = Color(0xFFF8EBDD),
            surfaceHigh = Color(0xFFEEDBC6),
            surfaceHighest = Color(0xFFE0C7A8),
            inverseSurface = Color(0xFF3E2723),
            inverseOnSurface = Color(0xFFFFF7F0),
            inversePrimary = Color(0xFFFFD9B3)
        )

        AppThemePreset.HIGH_CONTRAST -> buildScheme(
            darkTheme = darkTheme,
            primary = Color.White,
            secondary = Color(0xFFFFD400),
            tertiary = Color(0xFF00E5FF),
            background = Color.Black,
            surface = Color.Black,
            surfaceVariant = Color(0xFF222222),
            outline = Color.White,
            surfaceLowest = Color.Black,
            surfaceLow = Color(0xFF090909),
            surfaceHigh = Color(0xFF161616),
            surfaceHighest = Color(0xFF2A2A2A),
            inverseSurface = Color.White,
            inverseOnSurface = Color.Black,
            inversePrimary = Color(0xFF000000)
        )

        AppThemePreset.GRAYSCALE -> buildScheme(
            darkTheme = darkTheme,
            primary = Color(0xFFE0E0E0),
            secondary = Color(0xFFBDBDBD),
            tertiary = Color(0xFF9E9E9E),
            background = Color(0xFFF5F5F5),
            surface = Color(0xFFFAFAFA),
            surfaceVariant = Color(0xFFE0E0E0),
            outline = Color(0xFF757575),
            surfaceLowest = Color.White,
            surfaceLow = Color(0xFFF0F0F0),
            surfaceHigh = Color(0xFFE1E1E1),
            surfaceHighest = Color(0xFFD6D6D6),
            inverseSurface = Color(0xFF212121),
            inverseOnSurface = Color.White,
            inversePrimary = Color(0xFFBDBDBD)
        )

        AppThemePreset.MONOCHROME -> buildScheme(
            darkTheme = darkTheme,
            primary = Color(0xFF111111),
            secondary = Color(0xFF424242),
            tertiary = Color(0xFF757575),
            background = Color(0xFFF8F8F8),
            surface = Color.White,
            surfaceVariant = Color(0xFFE0E0E0),
            outline = Color(0xFF424242),
            surfaceLowest = Color.White,
            surfaceLow = Color(0xFFF3F3F3),
            surfaceHigh = Color(0xFFE8E8E8),
            surfaceHighest = Color(0xFFDCDCDC),
            inverseSurface = Color(0xFF111111),
            inverseOnSurface = Color.White,
            inversePrimary = Color(0xFF757575)
        )

        AppThemePreset.SOLARIZED -> buildScheme(
            darkTheme = darkTheme,
            primary = Color(0xFF268BD2),
            secondary = Color(0xFF2AA198),
            tertiary = Color(0xFFB58900),
            background = Color(0xFFFDF6E3),
            surface = Color(0xFFFCF3D5),
            surfaceVariant = Color(0xFFEEE8D5),
            outline = Color(0xFF93A1A1),
            surfaceLowest = Color(0xFFFFFCF2),
            surfaceLow = Color(0xFFF7EED0),
            surfaceHigh = Color(0xFFEEE0B5),
            surfaceHighest = Color(0xFFE4D29B),
            inverseSurface = Color(0xFF073642),
            inverseOnSurface = Color(0xFFFDF6E3),
            inversePrimary = Color(0xFF93A1A1)
        )

        AppThemePreset.NORD -> buildScheme(
            darkTheme = darkTheme,
            primary = Color(0xFF88C0D0),
            secondary = Color(0xFF81A1C1),
            tertiary = Color(0xFFA3BE8C),
            background = Color(0xFFE5EEF5),
            surface = Color(0xFFF2F6FA),
            surfaceVariant = Color(0xFFD8E1EA),
            outline = Color(0xFF6B7C93),
            surfaceLowest = Color(0xFFFFFFFF),
            surfaceLow = Color(0xFFEAF0F5),
            surfaceHigh = Color(0xFFDCE6EF),
            surfaceHighest = Color(0xFFCAD8E4),
            inverseSurface = Color(0xFF2E3440),
            inverseOnSurface = Color(0xFFF8FBFF),
            inversePrimary = Color(0xFFB48EAD)
        )

        AppThemePreset.DRACULA -> buildScheme(
            darkTheme = darkTheme,
            primary = Color(0xFFBD93F9),
            secondary = Color(0xFF8BE9FD),
            tertiary = Color(0xFFFF79C6),
            background = Color(0xFFF2ECFF),
            surface = Color(0xFFF8F2FF),
            surfaceVariant = Color(0xFFE7D7FF),
            outline = Color(0xFF7E6A99),
            surfaceLowest = Color.White,
            surfaceLow = Color(0xFFF0E7FF),
            surfaceHigh = Color(0xFFE3D3F8),
            surfaceHighest = Color(0xFFD6C0F0),
            inverseSurface = Color(0xFF282A36),
            inverseOnSurface = Color(0xFFF8F8F2),
            inversePrimary = Color(0xFFBD93F9)
        )

        AppThemePreset.RETRO -> buildScheme(
            darkTheme = darkTheme,
            primary = Color(0xFFFF7043),
            secondary = Color(0xFF26A69A),
            tertiary = Color(0xFFFFCA28),
            background = Color(0xFFFFF7E8),
            surface = Color(0xFFFFF3D9),
            surfaceVariant = Color(0xFFF5D9B2),
            outline = Color(0xFF9C6B3F),
            surfaceLowest = Color(0xFFFFFBF2),
            surfaceLow = Color(0xFFFFECCD),
            surfaceHigh = Color(0xFFF6D7A4),
            surfaceHighest = Color(0xFFEBC37B),
            inverseSurface = Color(0xFF3E2723),
            inverseOnSurface = Color(0xFFFFF8E1),
            inversePrimary = Color(0xFFFFAB91)
        )

        AppThemePreset.NEON -> buildScheme(
            darkTheme = darkTheme,
            primary = Color(0xFF39FF14),
            secondary = Color(0xFF00E5FF),
            tertiary = Color(0xFFFF1744),
            background = Color(0xFFF4FFF1),
            surface = Color(0xFFF9FFF7),
            surfaceVariant = Color(0xFFD8FBD1),
            outline = Color(0xFF55A14A),
            surfaceLowest = Color.White,
            surfaceLow = Color(0xFFF0FFE9),
            surfaceHigh = Color(0xFFE0FFD6),
            surfaceHighest = Color(0xFFCFFBC3),
            inverseSurface = Color(0xFF101010),
            inverseOnSurface = Color(0xFFE8FFE5),
            inversePrimary = Color(0xFF39FF14)
        )

        AppThemePreset.PASTEL -> buildScheme(
            darkTheme = darkTheme,
            primary = Color(0xFFF48FB1),
            secondary = Color(0xFF81D4FA),
            tertiary = Color(0xFFA5D6A7),
            background = Color(0xFFFFF8FB),
            surface = Color(0xFFFFFAFC),
            surfaceVariant = Color(0xFFF6E7EF),
            outline = Color(0xFFB48BA1),
            surfaceLowest = Color.White,
            surfaceLow = Color(0xFFFDF0F6),
            surfaceHigh = Color(0xFFF6E0EB),
            surfaceHighest = Color(0xFFEDCEDC),
            inverseSurface = Color(0xFF402B3A),
            inverseOnSurface = Color(0xFFFFF6FA),
            inversePrimary = Color(0xFFF8BBD0)
        )

        AppThemePreset.CYBERPUNK -> buildScheme(
            darkTheme = darkTheme,
            primary = Color(0xFFFF2DAA),
            secondary = Color(0xFF00F5FF),
            tertiary = Color(0xFFFFD400),
            background = Color(0xFFFFF0F8),
            surface = Color(0xFFFFF6FB),
            surfaceVariant = Color(0xFFF9D8E9),
            outline = Color(0xFFAD5D8B),
            surfaceLowest = Color.White,
            surfaceLow = Color(0xFFFFE8F4),
            surfaceHigh = Color(0xFFF6D2E5),
            surfaceHighest = Color(0xFFE8B9D1),
            inverseSurface = Color(0xFF120012),
            inverseOnSurface = Color(0xFFFFEFFF),
            inversePrimary = Color(0xFFFF2DAA)
        )

        AppThemePreset.OCEAN -> buildScheme(
            darkTheme = darkTheme,
            primary = Color(0xFF0288D1),
            secondary = Color(0xFF26C6DA),
            tertiary = Color(0xFF4DD0E1),
            background = Color(0xFFF1FBFF),
            surface = Color(0xFFF7FDFF),
            surfaceVariant = Color(0xFFD8EEF7),
            outline = Color(0xFF5B8FA6),
            surfaceLowest = Color.White,
            surfaceLow = Color(0xFFEAF8FD),
            surfaceHigh = Color(0xFFD8EFF8),
            surfaceHighest = Color(0xFFC4E6F2),
            inverseSurface = Color(0xFF002B3A),
            inverseOnSurface = Color(0xFFE8F7FF),
            inversePrimary = Color(0xFF81D4FA)
        )

        AppThemePreset.FOREST -> buildScheme(
            darkTheme = darkTheme,
            primary = Color(0xFF2E7D32),
            secondary = Color(0xFF66BB6A),
            tertiary = Color(0xFF81C784),
            background = Color(0xFFF4FBF2),
            surface = Color(0xFFF9FDF8),
            surfaceVariant = Color(0xFFDCEAD8),
            outline = Color(0xFF5E7F5C),
            surfaceLowest = Color.White,
            surfaceLow = Color(0xFFE9F6E6),
            surfaceHigh = Color(0xFFD7E9D2),
            surfaceHighest = Color(0xFFC3DABD),
            inverseSurface = Color(0xFF102A10),
            inverseOnSurface = Color(0xFFE8F5E9),
            inversePrimary = Color(0xFFA5D6A7)
        )

        AppThemePreset.SUNSET -> buildScheme(
            darkTheme = darkTheme,
            primary = Color(0xFFFF7043),
            secondary = Color(0xFFFFB74D),
            tertiary = Color(0xFFFF8A65),
            background = Color(0xFFFFF4EE),
            surface = Color(0xFFFFF9F6),
            surfaceVariant = Color(0xFFF3DBD0),
            outline = Color(0xFFAF7C6A),
            surfaceLowest = Color.White,
            surfaceLow = Color(0xFFFFEADF),
            surfaceHigh = Color(0xFFF7D8CB),
            surfaceHighest = Color(0xFFEEC4B2),
            inverseSurface = Color(0xFF3B1F19),
            inverseOnSurface = Color(0xFFFFF3EA),
            inversePrimary = Color(0xFFFFAB91)
        )

        AppThemePreset.MINIMALIST -> buildScheme(
            darkTheme = darkTheme,
            primary = Color(0xFF3A3A3A),
            secondary = Color(0xFF6F6F6F),
            tertiary = Color(0xFF9E9E9E),
            background = Color(0xFFF9F9F9),
            surface = Color.White,
            surfaceVariant = Color(0xFFE8E8E8),
            outline = Color(0xFF8E8E8E),
            surfaceLowest = Color.White,
            surfaceLow = Color(0xFFF5F5F5),
            surfaceHigh = Color(0xFFEEEEEE),
            surfaceHighest = Color(0xFFE2E2E2),
            inverseSurface = Color(0xFF121212),
            inverseOnSurface = Color.White,
            inversePrimary = Color(0xFFBDBDBD)
        )

        AppThemePreset.VIBRANT -> buildScheme(
            darkTheme = darkTheme,
            primary = Color(0xFF7C4DFF),
            secondary = Color(0xFF00C853),
            tertiary = Color(0xFFFF6D00),
            background = Color(0xFFFFFBF5),
            surface = Color(0xFFFFFCF8),
            surfaceVariant = Color(0xFFF0E1D4),
            outline = Color(0xFF9D7B63),
            surfaceLowest = Color.White,
            surfaceLow = Color(0xFFFFF1E4),
            surfaceHigh = Color(0xFFF7DDC1),
            surfaceHighest = Color(0xFFEEC89C),
            inverseSurface = Color(0xFF241E2F),
            inverseOnSurface = Color(0xFFFFF9F2),
            inversePrimary = Color(0xFFB39DFF)
        )

        AppThemePreset.EARTHY -> buildScheme(
            darkTheme = darkTheme,
            primary = Color(0xFF8D6E63),
            secondary = Color(0xFF689F38),
            tertiary = Color(0xFFA1887F),
            background = Color(0xFFF6F1E8),
            surface = Color(0xFFFDF9F3),
            surfaceVariant = Color(0xFFE3D6C8),
            outline = Color(0xFF8A6E5A),
            surfaceLowest = Color.White,
            surfaceLow = Color(0xFFF1E7DA),
            surfaceHigh = Color(0xFFE5D3C1),
            surfaceHighest = Color(0xFFD8C2AA),
            inverseSurface = Color(0xFF33251D),
            inverseOnSurface = Color(0xFFFFF8EF),
            inversePrimary = Color(0xFFC8B39A)
        )

        AppThemePreset.LAVENDER -> buildScheme(
            darkTheme = darkTheme,
            primary = Color(0xFF9C89FF),
            secondary = Color(0xFFD7B9FF),
            tertiary = Color(0xFFB39DDB),
            background = Color(0xFFF8F4FF),
            surface = Color(0xFFFDFBFF),
            surfaceVariant = Color(0xFFEBDDFF),
            outline = Color(0xFF8B76B0),
            surfaceLowest = Color.White,
            surfaceLow = Color(0xFFF2ECFF),
            surfaceHigh = Color(0xFFE6DBFF),
            surfaceHighest = Color(0xFFD9CAFF),
            inverseSurface = Color(0xFF221B34),
            inverseOnSurface = Color(0xFFF8F4FF),
            inversePrimary = Color(0xFFC3B1FF)
        )

        AppThemePreset.MIDNIGHT_BLUE -> buildScheme(
            darkTheme = darkTheme,
            primary = Color(0xFF5E8BFF),
            secondary = Color(0xFF3DD5F3),
            tertiary = Color(0xFF7C9CFF),
            background = Color(0xFFF3F7FF),
            surface = Color(0xFFF9FBFF),
            surfaceVariant = Color(0xFFD9E5FF),
            outline = Color(0xFF6681B5),
            surfaceLowest = Color.White,
            surfaceLow = Color(0xFFE8F0FF),
            surfaceHigh = Color(0xFFD8E4FF),
            surfaceHighest = Color(0xFFC6D6FF),
            inverseSurface = Color(0xFF0E1A33),
            inverseOnSurface = Color(0xFFEAF1FF),
            inversePrimary = Color(0xFFB3C8FF)
        )

        AppThemePreset.WARM_SEPIA -> buildScheme(
            darkTheme = darkTheme,
            primary = Color(0xFFAA6C39),
            secondary = Color(0xFFCF9A63),
            tertiary = Color(0xFFB98C5A),
            background = Color(0xFFFFF4E8),
            surface = Color(0xFFFFF9F3),
            surfaceVariant = Color(0xFFEAD7C2),
            outline = Color(0xFF9A7B63),
            surfaceLowest = Color.White,
            surfaceLow = Color(0xFFFFF0E0),
            surfaceHigh = Color(0xFFEED9C0),
            surfaceHighest = Color(0xFFDCC29F),
            inverseSurface = Color(0xFF392317),
            inverseOnSurface = Color(0xFFFFF6EC),
            inversePrimary = Color(0xFFE7C08F)
        )
    }
}

private fun buildScheme(
    darkTheme: Boolean,
    primary: Color,
    secondary: Color,
    tertiary: Color,
    background: Color,
    surface: Color,
    surfaceVariant: Color,
    outline: Color,
    surfaceLowest: Color,
    surfaceLow: Color,
    surfaceHigh: Color,
    surfaceHighest: Color,
    inverseSurface: Color,
    inverseOnSurface: Color,
    inversePrimary: Color
): ColorScheme {
    return if (darkTheme) {
        darkColorScheme(
            primary = primary,
            secondary = secondary,
            tertiary = tertiary,
            background = Color(0xFF101010),
            surface = Color(0xFF121212),
            surfaceVariant = surfaceVariant,
            surfaceContainerLowest = surfaceLowest,
            surfaceContainerLow = surfaceLow,
            surfaceContainer = surfaceHigh,
            surfaceContainerHigh = surfaceHighest,
            surfaceContainerHighest = surfaceHighest,
            outline = outline,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            inversePrimary = inversePrimary
        )
    } else {
        lightColorScheme(
            primary = primary,
            secondary = secondary,
            tertiary = tertiary,
            background = background,
            surface = surface,
            surfaceVariant = surfaceVariant,
            surfaceContainerLowest = surfaceLowest,
            surfaceContainerLow = surfaceLow,
            surfaceContainer = surfaceHigh,
            surfaceContainerHigh = surfaceHighest,
            surfaceContainerHighest = surfaceHighest,
            outline = outline,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            inversePrimary = inversePrimary
        )
    }
}
