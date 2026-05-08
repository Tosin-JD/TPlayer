package com.tosin.musicplayer.ui.state


data class SettingsUiState(
    val isDarkMode: Boolean = true,
    val showNotifications: Boolean = true,
    val useDynamicColor: Boolean = true,
    val lastScanDate: String = "Never"
)