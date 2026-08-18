package com.tosin.musicplayer.ui.visualizer

import com.tosin.musicplayer.ui.visualizer.styles.RadialVisualizationStyle

/**
 * Registry of all available visualization styles.
 *
 * To add a new style:
 * 1. Create a class implementing [VisualizationStyle].
 * 2. Add an enum entry here with its factory lambda.
 *
 * The UI style picker and ViewModel both reference this enum,
 * so a single addition here makes the new style available everywhere.
 */
enum class VisualizerType(
    val displayName: String,
    val factory: () -> VisualizationStyle
) {
    RADIAL("Radial Burst", ::RadialVisualizationStyle);

    // Future styles:
    // WAVEFORM("Waveform", ::WaveformVisualizationStyle),
    // CIRCULAR_BARS("Circular Bars", ::CircularBarsVisualizationStyle),
    // PARTICLE_FIELD("Particle Field", ::ParticleFieldVisualizationStyle)

    companion object {
        val DEFAULT = RADIAL
    }
}
