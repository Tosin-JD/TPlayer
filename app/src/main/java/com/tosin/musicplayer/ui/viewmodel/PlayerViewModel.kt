package com.tosin.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.data.repository.MusicRepository
import com.tosin.musicplayer.player.PlayerController
import com.tosin.musicplayer.ui.state.HomeUiState
import com.tosin.musicplayer.ui.state.LibraryGroup
import com.tosin.musicplayer.ui.state.LibraryTab
import com.tosin.musicplayer.ui.state.PlayerUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val repository: MusicRepository,
    private val playerController: PlayerController
) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _hasAudioPermission = MutableStateFlow(true)
    private val _selectedLibraryTab = MutableStateFlow(LibraryTab.All)
    private var songsJob: Job? = null

    val uiState: StateFlow<PlayerUiState> = combine(
        _songs,
        _isLoading,
        playerController.currentSong,
        playerController.isPlaying,
        playerController.progress
    ) { songs, isLoading, currentSong, isPlaying, progress ->
        PlayerUiState(
            songs = songs,
            currentSong = currentSong,
            isPlaying = isPlaying,
            progress = progress,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerUiState()
    )

    val homeUiState: StateFlow<HomeUiState> = combine(
        _songs,
        _isLoading,
        _hasAudioPermission,
        _selectedLibraryTab
    ) { songs, isLoading, hasAudioPermission, selectedTab ->
        HomeUiState(
            isLoading = isLoading,
            hasAudioPermission = hasAudioPermission,
            selectedTab = selectedTab,
            songs = songs,
            libraryGroups = buildLibraryGroups(songs, selectedTab)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun onAudioPermissionResult(isGranted: Boolean) {
        _hasAudioPermission.value = isGranted
        if (isGranted) {
            loadSongs()
        } else {
            songsJob?.cancel()
            _songs.value = emptyList()
            _isLoading.value = false
        }
    }

    fun selectLibraryTab(tab: LibraryTab) {
        _selectedLibraryTab.value = tab
    }

    private fun loadSongs() {
        songsJob?.cancel()
        songsJob = viewModelScope.launch {
            _isLoading.value = true
            repository.getSongs()
                .catch {
                    _songs.value = emptyList()
                    _isLoading.value = false
                }
                .collect { songs ->
                    _songs.value = songs
                    _isLoading.value = false
                }
        }
    }

    private fun buildLibraryGroups(
        songs: List<Song>,
        selectedTab: LibraryTab
    ): List<LibraryGroup> {
        val groupedSongs = when (selectedTab) {
            LibraryTab.All -> return emptyList()
            LibraryTab.Album -> songs.groupBy { it.album.ifBlank { "Unknown album" } }
            LibraryTab.Genre -> songs.groupBy { it.genre?.ifBlank { "Unknown genre" } ?: "Unknown genre" }
            LibraryTab.Folder -> songs.groupBy { it.folder?.ifBlank { "Unknown folder" } ?: "Unknown folder" }
            LibraryTab.Artist -> songs.groupBy { it.artist.ifBlank { "Unknown artist" } }
        }

        return groupedSongs
            .map { (title, groupedItems) ->
                LibraryGroup(
                    id = "${selectedTab.name}-$title",
                    title = title,
                    subtitle = groupSubtitle(selectedTab, groupedItems),
                    songCount = groupedItems.size,
                    artwork = groupedItems.firstOrNull { it.albumArt != null }?.albumArt
                )
            }
            .sortedBy { it.title.lowercase() }
    }

    private fun groupSubtitle(tab: LibraryTab, songs: List<Song>): String {
        return when (tab) {
            LibraryTab.All -> songsLabel(songs.size)
            LibraryTab.Album -> {
                val artists = songs.map { it.artist }.distinct().filter { it.isNotBlank() }
                artists.take(2).joinToString(" • ").ifBlank { songsLabel(songs.size) }
            }
            LibraryTab.Artist -> {
                val albums = songs.map { it.album }.distinct().count()
                "$albums ${if (albums == 1) "album" else "albums"}"
            }
            LibraryTab.Genre,
            LibraryTab.Folder -> songsLabel(songs.size)
        }
    }

    private fun songsLabel(count: Int): String {
        return "$count ${if (count == 1) "song" else "songs"}"
    }

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
