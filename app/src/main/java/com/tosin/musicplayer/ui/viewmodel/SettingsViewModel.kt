package com.tosin.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tosin.musicplayer.data.repository.MusicRepository
import com.tosin.musicplayer.data.repository.PreferencesRepository
import com.tosin.musicplayer.ui.state.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings screen. Handles UI state and interacts with repositories.
 */
class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // --------------------------- UI toggle functions ---------------------------
    fun toggleDarkMode(enabled: Boolean) = _uiState.update { it.copy(isDarkMode = enabled) }
    fun toggleDynamicColor(enabled: Boolean) = _uiState.update { it.copy(useDynamicColor = enabled) }
    fun toggleGaplessPlayback(enabled: Boolean) = _uiState.update { it.copy(gaplessPlayback = enabled) }
    fun toggleCrossfade(enabled: Boolean) = _uiState.update { it.copy(crossfadeEnabled = enabled) }
    fun setCrossfadeDuration(seconds: Int) = _uiState.update { it.copy(crossfadeDuration = seconds) }
    fun setPlaybackSpeed(speed: Float) = _uiState.update { it.copy(playbackSpeed = speed) }
    fun setSleepTimerMinutes(minutes: Int) = _uiState.update { it.copy(sleepTimerMinutes = minutes) }
    fun toggleAutoResume(enabled: Boolean) = _uiState.update { it.copy(autoResumeEnabled = enabled) }
    fun setAccentColor(index: Int) = _uiState.update { it.copy(accentColorIndex = index) }
    fun toggleNotifications(enabled: Boolean) = _uiState.update { it.copy(showNotifications = enabled) }

    // --------------------------- Persistence helpers ---------------------------
    fun resetAppearanceSettings() {
        viewModelScope.launch {
            val current = preferencesRepository.loadSettings().toMutableMap()
            current.remove("isDarkMode")
            current.remove("useDynamicColor")
            current.remove("accentColorIndex")
            current.remove("tabOrder")
            current.remove("visibleTabs")
            preferencesRepository.saveSettings(current)
        }
    }

    fun resetPlaybackSettings() {
        viewModelScope.launch {
            val current = preferencesRepository.loadSettings().toMutableMap()
            current.remove("gaplessPlayback")
            current.remove("crossfadeEnabled")
            current.remove("crossfadeDuration")
            current.remove("playbackSpeed")
            current.remove("sleepTimerMinutes")
            current.remove("autoResumeEnabled")
            preferencesRepository.saveSettings(current)
        }
    }

    fun scanForChanges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }
            musicRepository.scanForChanges()
            _uiState.update { it.copy(isScanning = false) }
        }
    }

    fun fullScan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }
            musicRepository.fullScan()
            _uiState.update { it.copy(isScanning = false) }
        }
    }
}