package com.tosin.musicplayer.ui.state


import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.ui.viewmodel.RepeatMode

data class PlayerUiState(
    val songs: List<Song> = emptyList(),
    val queue: List<Song> = emptyList(),
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val progress: Long = 0L,
    val duration: Long = 0L,
    val isLoading: Boolean = true,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val lyricsVisible: Boolean = false
)