package com.tosin.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tosin.musicplayer.data.repository.PreferencesRepository
import com.tosin.musicplayer.player.EqualizerManager
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class EqualizerViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    val uiState = EqualizerManager.uiState

    init {
        viewModelScope.launch {
            EqualizerManager.uiState
                .debounce(150)
                .collect { state ->
                    if (EqualizerManager.isReady) {
                        preferencesRepository.saveEqualizerState(state)
                    }
                }
        }
    }

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
