package com.tosin.musicplayer.ui.state

data class FolderEntry(
    val path: String,
    val label: String
)

data class SettingsUiState(
    val isDarkMode: Boolean = true,
    val showNotifications: Boolean = true,
    val useDynamicColor: Boolean = true,
    val themePreset: String = "Amoled",
    val lastScanDate: String = "Never",
    val rememberLastPlay: Boolean = true,
    val storageScopeAll: String = "Both",
    val storageScopeAlbum: String = "Both",
    val storageScopeArtist: String = "Both",
    val storageScopeGenre: String = "Both",
    val storageScopeFolder: String = "Both",
    val gaplessPlayback: Boolean = true,
    val crossfadeEnabled: Boolean = false,
    val crossfadeDuration: Int = 3, // seconds
    val playbackSpeed: Float = 1.0f,
    val sleepTimerMinutes: Int = 0,
    val autoResumeEnabled: Boolean = true,
    val pauseOnZeroVolume: Boolean = true,
    val accentColorIndex: Int = 0,
    val isScanning: Boolean = false,
    val tabOrder: List<String> = listOf("All", "Album", "Artist", "Genre", "Folder"),
    val visibleTabs: List<String> = listOf("All", "Album", "Artist", "Genre", "Folder"),
    val availableFolders: List<FolderEntry> = emptyList(),
    val excludedFolders: List<String> = emptyList()
)
