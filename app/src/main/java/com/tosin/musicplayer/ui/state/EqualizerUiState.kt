package com.tosin.musicplayer.ui.state

data class EqBand(
    val id: Int,
    val frequency: Int,
    val level: Int, // in mB (millibels)
    val minLevel: Int,
    val maxLevel: Int
)

data class EqualizerPresetUi(
    val id: String,
    val name: String,
    val description: String
)

data class EqualizerUiState(
    val enabled: Boolean = false,
    val isAvailable: Boolean = true,
    val bands: List<EqBand> = emptyList(),
    val presets: List<EqualizerPresetUi> = emptyList(),
    val selectedPresetId: String = "flat",
    val selectedPresetName: String = "Flat",
    val selectedPresetDescription: String = "Balanced sound with no coloration.",
    val bassBoost: Int = 0, // 0 - 1000
    val virtualizer: Int = 0, // 0 - 1000
    val loudness: Int = 0 // 0 - 2000 (mB)
)
