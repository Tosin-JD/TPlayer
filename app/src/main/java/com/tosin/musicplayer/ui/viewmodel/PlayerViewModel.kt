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
import kotlinx.coroutines.flow.asStateFlow
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

    private val _shuffle = MutableStateFlow(false)
    val shuffleEnabled = _shuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode = _repeatMode.asStateFlow()

    private val _lyricsVisible = MutableStateFlow(false)
    val lyricsVisible = _lyricsVisible.asStateFlow()

    val uiState: StateFlow<PlayerUiState> = combine(
        _songs,
        _isLoading,
        playerController.currentSong,
        playerController.isPlaying,
        playerController.progress,
        _shuffle,
        _repeatMode,
        _lyricsVisible,
        playerController.queue
    ) { args ->
        val songs = args[0] as List<Song>
        val isLoading = args[1] as Boolean
        val currentSong = args[2] as Song?
        val isPlaying = args[3] as Boolean
        val progress = args[4] as Long
        val shuffle = args[5] as Boolean
        val repeatMode = args[6] as RepeatMode
        val lyricsVisible = args[7] as Boolean
        val queue = args[8] as List<Song>
        
        PlayerUiState(
            songs = songs,
            queue = queue,
            currentSong = currentSong,
            isPlaying = isPlaying,
            progress = progress,
            isLoading = isLoading,
            shuffleEnabled = shuffle,
            repeatMode = repeatMode,
            lyricsVisible = lyricsVisible
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

    fun toggleShuffle() {
        _shuffle.value = !_shuffle.value
        playerController.setShuffleEnabled(_shuffle.value)
    }

    fun toggleLyrics() {
        _lyricsVisible.value = !_lyricsVisible.value
    }

    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.REPEAT_ALL
            RepeatMode.REPEAT_ALL -> RepeatMode.REPEAT_ONE
            RepeatMode.REPEAT_ONE -> RepeatMode.PLAY_ONE_ONCE
            RepeatMode.PLAY_ONE_ONCE -> RepeatMode.PLAY_ALL_ONCE
            RepeatMode.PLAY_ALL_ONCE -> RepeatMode.OFF
        }
        playerController.setRepeatMode(_repeatMode.value)
    }

    fun playSongFromQueue(song: Song) {
        val index = uiState.value.queue.indexOfFirst { it.id == song.id }
        if (index != -1) {
            playerController.seekToMediaItem(index)
        }
    }

    fun getSongsForGroup(tab: LibraryTab, title: String): List<Song> {
        val allSongs = _songs.value
        return when (tab) {
            LibraryTab.All -> allSongs
            LibraryTab.Album -> allSongs.filter { it.album == title || (it.album.isBlank() && title == "Unknown album") }
            LibraryTab.Genre -> allSongs.filter { it.genre == title || (it.genre.isNullOrBlank() && title == "Unknown genre") }
            LibraryTab.Folder -> allSongs.filter { it.folder == title || (it.folder.isNullOrBlank() && title == "Unknown folder") }
            LibraryTab.Artist -> allSongs.filter { it.artist == title || (it.artist.isBlank() && title == "Unknown artist") }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerController.release()
    }
}

// Repeat modes requested by UI
enum class RepeatMode {
    OFF,
    REPEAT_ALL,
    REPEAT_ONE,
    PLAY_ONE_ONCE,
    PLAY_ALL_ONCE
}
