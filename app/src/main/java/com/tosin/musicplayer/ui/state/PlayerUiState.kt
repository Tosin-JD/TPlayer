package com.tosin.musicplayer.ui.state


import com.tosin.musicplayer.data.models.Song

data class PlayerUiState(
    val songs: List<Song> = emptyList(),
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val progress: Long = 0L,
    val isLoading: Boolean = true
)