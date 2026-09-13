package com.tosin.musicplayer.widget

import android.appwidget.AppWidgetManager
import android.os.Bundle

object WidgetSizing {
    enum class SizeMode {
        TINY,
        SMALL,
        MEDIUM,
        LARGE,
        COMPACT,
        FULLSCREEN
    }

    /**
     * Resolves the optimal size mode based on widget options and optional
     * provider class name. Dedicated provider classes (Compact / Fullscreen)
     * are locked to their intended layout regardless of launcher-reported
     * dimensions, which vary between launchers and are often 0 or generic
     * defaults before the user resizes.
     */
    fun resolveSize(options: Bundle?, providerClassName: String? = null): SizeMode {
        // Fast-path: dedicated providers always render their target layout.
        if (providerClassName?.contains("CompactWidgetProvider") == true) {
            return SizeMode.COMPACT
        }
        if (providerClassName?.contains("FullscreenWidgetProvider") == true) {
            return SizeMode.FULLSCREEN
        }

        if (options == null) return SizeMode.SMALL

        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)

        // If launcher returns 0 for dimensions (common before first resize), default to SMALL
        if (minWidth <= 0 && minHeight <= 0) return SizeMode.SMALL

        return when {
            // Fullscreen (4x4 or 5x5 grid)
            minWidth >= 280 && minHeight >= 280 -> SizeMode.FULLSCREEN
            // Tiny (1x1 or small 2x1)
            minWidth < 120 && minHeight < 120 -> SizeMode.TINY
            // Compact bar (any wide but short widget, e.g. 3x1, 4x1, 5x1)
            minHeight <= 100 && minWidth >= 150 -> SizeMode.COMPACT
            // Short height fallback
            minHeight <= 90 -> SizeMode.COMPACT
            // Large (4x3+)
            minWidth >= 300 && minHeight >= 170 -> SizeMode.LARGE
            // Medium (3x2 or 4x2)
            minWidth >= 220 && minHeight >= 110 -> SizeMode.MEDIUM
            // Small (2x2)
            else -> SizeMode.SMALL
        }
    }
}