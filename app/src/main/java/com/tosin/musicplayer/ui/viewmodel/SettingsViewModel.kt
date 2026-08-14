package com.tosin.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tosin.musicplayer.data.local.ScanProgress
import com.tosin.musicplayer.data.repository.MusicRepository
import com.tosin.musicplayer.data.repository.PreferencesRepository
import com.tosin.musicplayer.ui.state.FolderEntry
import com.tosin.musicplayer.ui.state.SettingsUiState
import com.tosin.musicplayer.ui.state.StorageScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    private val _events = MutableSharedFlow<SettingsEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    init {
        loadSavedSettings()
        loadAvailableFolders()
    }

    // ── Appearance ──
    fun toggleDarkMode(enabled: Boolean) {
        updateSettings { it.copy(isDarkMode = enabled) }
    }

    fun toggleDynamicColor(enabled: Boolean) {
        updateSettings { it.copy(useDynamicColor = enabled) }
    }

    fun setThemePreset(themePreset: String) {
        updateSettings { it.copy(themePreset = themePreset) }
    }

    fun toggleRememberLastPlay(enabled: Boolean) {
        updateSettings { it.copy(rememberLastPlay = enabled) }
    }

    fun setStorageScopeForTab(tab: String, scope: StorageScope) {
        updateSettings { state ->
            when (tab) {
                "All" -> state.copy(storageScopeAll = scope.name)
                "Album" -> state.copy(storageScopeAlbum = scope.name)
                "Artist" -> state.copy(storageScopeArtist = scope.name)
                "Genre" -> state.copy(storageScopeGenre = scope.name)
                "Folder" -> state.copy(storageScopeFolder = scope.name)
                else -> state
            }
        }
    }

    fun getStorageScopeForTab(tab: String): StorageScope {
        val raw = when (tab) {
            "All" -> uiState.value.storageScopeAll
            "Album" -> uiState.value.storageScopeAlbum
            "Artist" -> uiState.value.storageScopeArtist
            "Genre" -> uiState.value.storageScopeGenre
            "Folder" -> uiState.value.storageScopeFolder
            else -> uiState.value.storageScopeAll
        }
        return StorageScope.entries.firstOrNull { it.name == raw } ?: StorageScope.Both
    }

    fun setAccentColor(index: Int) {
        updateSettings { it.copy(accentColorIndex = index) }
    }

    fun reorderTab(fromIndex: Int, toIndex: Int) {
        updateSettings { state ->
            val tabs = state.tabOrder.toMutableList()
            if (fromIndex in tabs.indices && toIndex in tabs.indices) {
                val item = tabs.removeAt(fromIndex)
                tabs.add(toIndex, item)
            }
            state.copy(tabOrder = tabs)
        }
    }

    fun toggleTabVisibility(tab: String) {
        updateSettings { state ->
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
    fun toggleNotifications(enabled: Boolean) {
        updateSettings { it.copy(showNotifications = enabled) }
    }

    fun scanForChanges() {
        startScan(isFullScan = false)
    }

    fun fullScan() {
        startScan(isFullScan = true)
    }

    private fun startScan(isFullScan: Boolean) {
        if (_uiState.value.isScanning) return
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isScanning = true,
                        scanProgress = 0,
                        scanTotal = 0,
                        scanFolderCount = 0,
                        scanCurrentFolder = null,
                        scanLabel = if (isFullScan) "Full scan" else "Scan library"
                    )
                }

                val scanner: suspend (ScanProgress) -> Unit = { progress ->
                    _uiState.update {
                        it.copy(
                            scanProgress = progress.scannedItems,
                            scanTotal = progress.totalItems,
                            scanFolderCount = progress.scannedFolders,
                            scanCurrentFolder = progress.currentFolder
                        )
                    }
                }

                if (isFullScan) {
                    musicRepository.fullScan(scanner)
                } else {
                    musicRepository.scanForChanges(scanner)
                }

                loadAvailableFolders()
                updateSettings { current ->
                    current.copy(lastScanDate = formatNow())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _events.emit(
                    SettingsEvent.ScanFinished(
                        isFullScan = isFullScan
                    )
                )
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanCurrentFolder = null
                    )
                }
            }
        }
    }

    // ── Playback ──
    fun toggleGaplessPlayback(enabled: Boolean) {
        updateSettings { it.copy(gaplessPlayback = enabled) }
    }

    fun toggleCrossfade(enabled: Boolean) {
        updateSettings { it.copy(crossfadeEnabled = enabled) }
    }

    fun setCrossfadeDuration(seconds: Int) {
        updateSettings { it.copy(crossfadeDuration = seconds) }
    }

    fun setPlaybackSpeed(speed: Float) {
        updateSettings { it.copy(playbackSpeed = speed) }
    }

    fun setSleepTimerMinutes(minutes: Int) {
        updateSettings { it.copy(sleepTimerMinutes = minutes) }
    }

    fun toggleAutoResume(enabled: Boolean) {
        updateSettings { it.copy(autoResumeEnabled = enabled) }
    }

    fun togglePauseOnZeroVolume(enabled: Boolean) {
        updateSettings { it.copy(pauseOnZeroVolume = enabled) }
    }

    fun toggleExcludedFolder(folderPath: String) {
        updateSettings { state ->
            val excluded = state.excludedFolders.toMutableList()
            if (folderPath in excluded) {
                excluded.remove(folderPath)
            } else {
                excluded.add(folderPath)
            }
            state.copy(excludedFolders = excluded.distinct())
        }
    }

    // ── Reset Helpers ──
    fun resetGeneralSettings() {
        _uiState.update {
            it.copy(
                showNotifications = true,
                lastScanDate = "Never",
                rememberLastPlay = true,
                storageScopeAll = "Both",
                storageScopeAlbum = "Both",
                storageScopeArtist = "Both",
                storageScopeGenre = "Both",
                storageScopeFolder = "Both",
                excludedFolders = emptyList()
            )
        }
        viewModelScope.launch {
            val current = preferencesRepository.loadSettings().toMutableMap()
            current.remove("showNotifications")
            current.remove("lastScanDate")
            current.remove("rememberLastPlay")
            current.remove("storageScopeAll")
            current.remove("storageScopeAlbum")
            current.remove("storageScopeArtist")
            current.remove("storageScopeGenre")
            current.remove("storageScopeFolder")
            current.remove("excludedFolders")
            preferencesRepository.saveSettings(current)
        }
    }

    fun resetAppearanceSettings() {
        _uiState.update {
            it.copy(
                isDarkMode = true,
                useDynamicColor = true,
                themePreset = "Amoled",
                rememberLastPlay = true,
                storageScopeAll = "Both",
                storageScopeAlbum = "Both",
                storageScopeArtist = "Both",
                storageScopeGenre = "Both",
                storageScopeFolder = "Both",
                accentColorIndex = 0,
                tabOrder = listOf("All", "Album", "Artist", "Genre", "Folder"),
                visibleTabs = listOf("All", "Album", "Artist", "Genre", "Folder")
            )
        }
        viewModelScope.launch {
            val current = preferencesRepository.loadSettings().toMutableMap()
            current.remove("isDarkMode")
            current.remove("useDynamicColor")
            current.remove("themePreset")
            current.remove("rememberLastPlay")
            current.remove("storageScopeAll")
            current.remove("storageScopeAlbum")
            current.remove("storageScopeArtist")
            current.remove("storageScopeGenre")
            current.remove("storageScopeFolder")
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
                autoResumeEnabled = true,
                pauseOnZeroVolume = true
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
            current.remove("pauseOnZeroVolume")
            preferencesRepository.saveSettings(current)
        }
    }

    fun refreshAvailableFolders() {
        loadAvailableFolders()
    }

    fun resetAllSettings() {
        _uiState.value = SettingsUiState()
        viewModelScope.launch {
            preferencesRepository.saveSettings(emptyMap())
        }
    }

    private fun loadSavedSettings() {
        viewModelScope.launch {
            val saved = preferencesRepository.loadSettings()
            _uiState.update { current ->
                current.copy(
                    isDarkMode = saved.boolean("isDarkMode", current.isDarkMode),
                    showNotifications = saved.boolean("showNotifications", current.showNotifications),
                    useDynamicColor = saved.boolean("useDynamicColor", current.useDynamicColor),
                    themePreset = saved.string("themePreset", current.themePreset),
                    lastScanDate = saved.string("lastScanDate", current.lastScanDate),
                    rememberLastPlay = saved.boolean("rememberLastPlay", current.rememberLastPlay),
                    storageScopeAll = saved.string("storageScopeAll", current.storageScopeAll),
                    storageScopeAlbum = saved.string("storageScopeAlbum", current.storageScopeAlbum),
                    storageScopeArtist = saved.string("storageScopeArtist", current.storageScopeArtist),
                    storageScopeGenre = saved.string("storageScopeGenre", current.storageScopeGenre),
                    storageScopeFolder = saved.string("storageScopeFolder", current.storageScopeFolder),
                    gaplessPlayback = saved.boolean("gaplessPlayback", current.gaplessPlayback),
                    crossfadeEnabled = saved.boolean("crossfadeEnabled", current.crossfadeEnabled),
                    crossfadeDuration = saved.int("crossfadeDuration", current.crossfadeDuration),
                    playbackSpeed = saved.float("playbackSpeed", current.playbackSpeed),
                    sleepTimerMinutes = saved.int("sleepTimerMinutes", current.sleepTimerMinutes),
                    autoResumeEnabled = saved.boolean("autoResumeEnabled", current.autoResumeEnabled),
                    pauseOnZeroVolume = saved.boolean("pauseOnZeroVolume", current.pauseOnZeroVolume),
                    accentColorIndex = saved.int("accentColorIndex", current.accentColorIndex),
                    tabOrder = saved.stringList("tabOrder", current.tabOrder),
                    visibleTabs = saved.stringList("visibleTabs", current.visibleTabs),
                    excludedFolders = saved.stringList("excludedFolders", current.excludedFolders)
                )
            }
        }
    }

    private fun loadAvailableFolders() {
        viewModelScope.launch {
            val folders = musicRepository.loadAllSongs()
                .mapNotNull { song -> song.folderPath?.takeIf { it.isNotBlank() } }
                .distinct()
                .sortedBy { it.lowercase() }
                .map { path ->
                    FolderEntry(
                        path = path,
                        label = File(path).name.ifBlank { path }
                    )
                }
            _uiState.update { it.copy(availableFolders = folders) }
        }
    }

    private fun updateSettings(transform: (SettingsUiState) -> SettingsUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            persistSettings(updated)
            updated
        }
    }

    private fun persistSettings(state: SettingsUiState) {
        viewModelScope.launch {
            preferencesRepository.saveSettings(state.toMap())
        }
    }

    private fun Map<String, Any>.boolean(key: String, default: Boolean): Boolean =
        (this[key] as? Boolean) ?: default

    private fun Map<String, Any>.int(key: String, default: Int): Int =
        when (val value = this[key]) {
            is Int -> value
            is Long -> value.toInt()
            is Double -> value.toInt()
            is Float -> value.toInt()
            else -> default
        }

    private fun Map<String, Any>.float(key: String, default: Float): Float =
        when (val value = this[key]) {
            is Float -> value
            is Double -> value.toFloat()
            is Int -> value.toFloat()
            is Long -> value.toFloat()
            else -> default
        }

    private fun Map<String, Any>.string(key: String, default: String): String =
        (this[key] as? String) ?: default

    private fun Map<String, Any>.stringList(key: String, default: List<String>): List<String> =
        (this[key] as? List<*>)?.mapNotNull { it as? String } ?: default

    private fun SettingsUiState.toMap(): Map<String, Any> = mapOf(
        "isDarkMode" to isDarkMode,
        "showNotifications" to showNotifications,
        "useDynamicColor" to useDynamicColor,
        "themePreset" to themePreset,
        "lastScanDate" to lastScanDate,
        "rememberLastPlay" to rememberLastPlay,
        "storageScopeAll" to storageScopeAll,
        "storageScopeAlbum" to storageScopeAlbum,
        "storageScopeArtist" to storageScopeArtist,
        "storageScopeGenre" to storageScopeGenre,
        "storageScopeFolder" to storageScopeFolder,
        "gaplessPlayback" to gaplessPlayback,
        "crossfadeEnabled" to crossfadeEnabled,
        "crossfadeDuration" to crossfadeDuration,
        "playbackSpeed" to playbackSpeed,
        "sleepTimerMinutes" to sleepTimerMinutes,
        "autoResumeEnabled" to autoResumeEnabled,
        "pauseOnZeroVolume" to pauseOnZeroVolume,
        "accentColorIndex" to accentColorIndex,
        "tabOrder" to tabOrder,
        "visibleTabs" to visibleTabs,
        "excludedFolders" to excludedFolders
    )

    private fun formatNow(): String {
        val formatter = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
        return formatter.format(Date())
    }
}

sealed class SettingsEvent {
    data class ScanFinished(val isFullScan: Boolean) : SettingsEvent()
}
