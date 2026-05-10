package com.tosin.musicplayer.ui.state

data class EqBand(
    val id: Int,
    val frequency: Int,
    val level: Int, // in mB (millibels)
    val minLevel: Int,
    val maxLevel: Int
)

data class EqualizerUiState(
    val enabled: Boolean = false,
    val bands: List<EqBand> = emptyList(),
    val presets: List<String> = emptyList(),
    val selectedPreset: Int = -1,
    val bassBoost: Int = 0, // 0 - 1000
    val virtualizer: Int = 0, // 0 - 1000
    val loudness: Int = 0 // 0 - 2000 (mB)
)
