package com.tosin.musicplayer.widget

data class WidgetState(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val isPlaying: Boolean = false,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: String = "PLAY_ALL_ONCE",
    val progressMs: Long = 0L,
    val durationMs: Long = 0L,
    val albumArtUri: String? = null,
    val isEmptyQueue: Boolean = true,
    val isOffline: Boolean = false,
    val lastUpdatedMs: Long = System.currentTimeMillis()
)
