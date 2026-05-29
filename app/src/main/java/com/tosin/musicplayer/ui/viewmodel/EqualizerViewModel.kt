package com.tosin.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.tosin.musicplayer.player.EqualizerManager

class EqualizerViewModel : ViewModel() {
    val uiState = EqualizerManager.uiState

    fun setEnabled(enabled: Boolean) {
        EqualizerManager.setEnabled(enabled)
    }

    fun setBandLevel(bandId: Int, level: Int) {
        EqualizerManager.setBandLevel(bandId, level)
    }

    fun setPreset(presetId: String) {
        EqualizerManager.setPreset(presetId)
    }

    fun setBassBoost(strength: Int) {
        EqualizerManager.setBassBoost(strength)
    }

    fun setVirtualizer(strength: Int) {
        EqualizerManager.setVirtualizer(strength)
    }

    fun setLoudness(gain: Int) {
        EqualizerManager.setLoudness(gain)
    }
}
