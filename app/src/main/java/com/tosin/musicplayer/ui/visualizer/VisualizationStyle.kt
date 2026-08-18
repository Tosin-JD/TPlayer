package com.tosin.musicplayer.ui.visualizer

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Contract for pluggable visualization styles.
 * Each style receives processed FFT magnitudes and renders onto a Canvas DrawScope.
 *
 * To add a new style:
 * 1. Create a class implementing this interface.
 * 2. Register it in [VisualizerType].
 */
interface VisualizationStyle {

    /** Human-readable name shown in the style picker UI. */
    val displayName: String

    /**
     * Renders one frame of the visualization.
     *
     * @param magnitudes Normalized FFT magnitudes (0f..1f), typically 48 bars.
     * @param isPlaying  Whether audio is currently playing.
     * @param primaryColor Theme primary color for style-consistent rendering.
     * @param accentColor  Theme accent/secondary color.
     * @param timeMs  Monotonic time in milliseconds for animation phasing.
     */
    fun DrawScope.render(
        magnitudes: FloatArray,
        isPlaying: Boolean,
        primaryColor: Color,
        accentColor: Color,
        timeMs: Long
    )
}
