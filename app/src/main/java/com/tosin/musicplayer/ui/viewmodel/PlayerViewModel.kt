package com.tosin.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.data.repository.MusicRepository
import com.tosin.musicplayer.player.PlayerController
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val repository: MusicRepository,
    private val playerController: PlayerController
) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())

    // Combine everything into one UI state
    val uiState: StateFlow<`PlayerUiState.kt`> = combine(
        _songs,
        playerController.currentSong,
        playerController.isPlaying,
        playerController.progress
    ) { songs, currentSong, isPlaying, progress ->

        `PlayerUiState.kt`(
            songs = songs,
            currentSong = currentSong,
            isPlaying = isPlaying,
            progress = progress,
            isLoading = songs.isEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = `PlayerUiState.kt`()
    )

    init {
        loadSongs()
    }

    private fun loadSongs() {
        viewModelScope.launch {
            repository.getSongs().collect { songs ->
                _songs.value = songs
            }
        }
    }

    // 🎵 USER ACTIONS

    fun onSongClick(songs: List<Song>, index: Int) {
        playerController.setPlaylist(songs, index)
        playerController.play()
    }

    fun play() = playerController.play()

    fun pause() = playerController.pause()

    fun next() = playerController.next()

    fun previous() = playerController.previous()

    fun seekTo(position: Long) = playerController.seekTo(position)

    override fun onCleared() {
        super.onCleared()
        playerController.release()
    }
}