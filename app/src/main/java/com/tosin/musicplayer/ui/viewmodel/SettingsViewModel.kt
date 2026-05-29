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

class SettingsViewModel(
    private val musicRepository: MusicRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.settingsFlow.collect { saved ->
                _uiState.update { state ->
                    state.copy(
                        isDarkMode = saved["isDarkMode"] as? Boolean ?: true,
                        showNotifications = saved["showNotifications"] as? Boolean ?: true,
                        useDynamicColor = saved["useDynamicColor"] as? Boolean ?: true,
                        lastScanDate = saved["lastScanDate"] as? String ?: "Never",
                        gaplessPlayback = saved["gaplessPlayback"] as? Boolean ?: true,
                        crossfadeEnabled = saved["crossfadeEnabled"] as? Boolean ?: false,
                        crossfadeDuration = (saved["crossfadeDuration"] as? Number)?.toInt() ?: 3,
                        playbackSpeed = (saved["playbackSpeed"] as? Number)?.toFloat() ?: 1.0f,
                        sleepTimerMinutes = (saved["sleepTimerMinutes"] as? Number)?.toInt() ?: 0,
                        autoResumeEnabled = saved["autoResumeEnabled"] as? Boolean ?: true,
                        accentColorIndex = (saved["accentColorIndex"] as? Number)?.toInt() ?: 0
                    )
                }
            }
        }
    }

    private fun updateSetting(key: String, value: Any) {
        viewModelScope.launch {
            val current = preferencesRepository.settingsFlow.value.toMutableMap()
            current[key] = value
            preferencesRepository.saveSettings(current)
        }
    }

    fun toggleDarkMode(enabled: Boolean) = updateSetting("isDarkMode", enabled)
    fun toggleDynamicColor(enabled: Boolean) = updateSetting("useDynamicColor", enabled)
    fun toggleGaplessPlayback(enabled: Boolean) = updateSetting("gaplessPlayback", enabled)
    fun toggleCrossfade(enabled: Boolean) = updateSetting("crossfadeEnabled", enabled)
    fun setCrossfadeDuration(seconds: Int) = updateSetting("crossfadeDuration", seconds)
    fun setPlaybackSpeed(speed: Float) = updateSetting("playbackSpeed", speed)
    fun setSleepTimerMinutes(minutes: Int) = updateSetting("sleepTimerMinutes", minutes)
    fun toggleAutoResume(enabled: Boolean) = updateSetting("autoResumeEnabled", enabled)
    fun setAccentColor(index: Int) = updateSetting("accentColorIndex", index)
    fun toggleNotifications(enabled: Boolean) = updateSetting("showNotifications", enabled)

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