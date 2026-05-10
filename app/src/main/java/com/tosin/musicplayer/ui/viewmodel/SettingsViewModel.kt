package com.tosin.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.tosin.musicplayer.ui.state.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
    }

    fun toggleDynamicColor(enabled: Boolean) {
        _uiState.update { it.copy(useDynamicColor = enabled) }
    }

    fun toggleGaplessPlayback(enabled: Boolean) {
        _uiState.update { it.copy(gaplessPlayback = enabled) }
    }

    fun toggleCrossfade(enabled: Boolean) {
        _uiState.update { it.copy(crossfadeEnabled = enabled) }
    }

    fun setCrossfadeDuration(seconds: Int) {
        _uiState.update { it.copy(crossfadeDuration = seconds) }
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    fun setSleepTimerMinutes(minutes: Int) {
        _uiState.update { it.copy(sleepTimerMinutes = minutes) }
    }

    fun toggleAutoResume(enabled: Boolean) {
        _uiState.update { it.copy(autoResumeEnabled = enabled) }
    }

    fun setAccentColor(index: Int) {
        _uiState.update { it.copy(accentColorIndex = index) }
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.update { it.copy(showNotifications = enabled) }
    }
}