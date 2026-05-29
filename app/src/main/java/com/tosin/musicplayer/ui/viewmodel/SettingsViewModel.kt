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
 * ViewModel for the Settings screen and all sub-settings screens.
 * Single ViewModel shared across settings navigation to keep state consistent.
 */
class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // ── Appearance ──
    fun toggleDarkMode(enabled: Boolean) = _uiState.update { it.copy(isDarkMode = enabled) }
    fun toggleDynamicColor(enabled: Boolean) = _uiState.update { it.copy(useDynamicColor = enabled) }
    fun setAccentColor(index: Int) = _uiState.update { it.copy(accentColorIndex = index) }

    fun reorderTab(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val tabs = state.tabOrder.toMutableList()
            if (fromIndex in tabs.indices && toIndex in tabs.indices) {
                val item = tabs.removeAt(fromIndex)
                tabs.add(toIndex, item)
            }
            state.copy(tabOrder = tabs)
        }
    }

    fun toggleTabVisibility(tab: String) {
        _uiState.update { state ->
            val visible = state.visibleTabs.toMutableList()
            if (tab in visible) {
                // Don't allow hiding all tabs
                if (visible.size > 1) visible.remove(tab)
            } else {
                visible.add(tab)
            }
            state.copy(visibleTabs = visible)
        }
    }

    // ── General ──
    fun toggleNotifications(enabled: Boolean) = _uiState.update { it.copy(showNotifications = enabled) }

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

    // ── Playback ──
    fun toggleGaplessPlayback(enabled: Boolean) = _uiState.update { it.copy(gaplessPlayback = enabled) }
    fun toggleCrossfade(enabled: Boolean) = _uiState.update { it.copy(crossfadeEnabled = enabled) }
    fun setCrossfadeDuration(seconds: Int) = _uiState.update { it.copy(crossfadeDuration = seconds) }
    fun setPlaybackSpeed(speed: Float) = _uiState.update { it.copy(playbackSpeed = speed) }
    fun setSleepTimerMinutes(minutes: Int) = _uiState.update { it.copy(sleepTimerMinutes = minutes) }
    fun toggleAutoResume(enabled: Boolean) = _uiState.update { it.copy(autoResumeEnabled = enabled) }

    // ── Reset Helpers ──
    fun resetGeneralSettings() {
        _uiState.update { it.copy(showNotifications = true) }
        viewModelScope.launch {
            val current = preferencesRepository.loadSettings().toMutableMap()
            current.remove("showNotifications")
            preferencesRepository.saveSettings(current)
        }
    }

    fun resetAppearanceSettings() {
        _uiState.update {
            it.copy(
                isDarkMode = true,
                useDynamicColor = true,
                accentColorIndex = 0,
                tabOrder = listOf("All", "Album", "Artist", "Genre", "Folder"),
                visibleTabs = listOf("All", "Album", "Artist", "Genre", "Folder")
            )
        }
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
        _uiState.update {
            it.copy(
                gaplessPlayback = true,
                crossfadeEnabled = false,
                crossfadeDuration = 3,
                playbackSpeed = 1.0f,
                sleepTimerMinutes = 0,
                autoResumeEnabled = true
            )
        }
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

    fun resetAllSettings() {
        _uiState.value = SettingsUiState()
        viewModelScope.launch {
            preferencesRepository.saveSettings(emptyMap())
        }
    }
}