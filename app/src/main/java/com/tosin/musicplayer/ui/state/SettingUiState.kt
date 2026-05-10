package com.tosin.musicplayer.ui.state

data class SettingsUiState(
    val isDarkMode: Boolean = true,
    val showNotifications: Boolean = true,
    val useDynamicColor: Boolean = true,
    val lastScanDate: String = "Never",
    val gaplessPlayback: Boolean = true,
    val crossfadeEnabled: Boolean = false,
    val crossfadeDuration: Int = 3, // seconds
    val playbackSpeed: Float = 1.0f,
    val sleepTimerMinutes: Int = 0,
    val autoResumeEnabled: Boolean = true,
    val accentColorIndex: Int = 0
)