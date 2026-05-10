package com.tosin.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tosin.musicplayer.player.EqualizerManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EqualizerViewModel : ViewModel() {
    val uiState = EqualizerManager.uiState

    fun setEnabled(enabled: Boolean) {
        EqualizerManager.setEnabled(enabled)
    }

    fun setBandLevel(bandId: Int, level: Int) {
        EqualizerManager.setBandLevel(bandId, level)
    }

    fun setPreset(presetIndex: Int) {
        EqualizerManager.setPreset(presetIndex)
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
