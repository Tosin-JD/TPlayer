package com.tosin.musicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tosin.musicplayer.data.models.Playlist
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.data.repository.MusicRepository
import com.tosin.musicplayer.data.repository.PlaylistRepository
import com.tosin.musicplayer.data.repository.PreferencesRepository
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
import com.tosin.musicplayer.data.repository.StatsRepository
import com.tosin.musicplayer.data.models.SongStats

class PlayerViewModel(
    private val repository: MusicRepository,
    private val playerController: PlayerController,
    private val statsRepository: StatsRepository,
    private val playlistRepository: PlaylistRepository,
    private val preferencesRepository: PreferencesRepository
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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _tabOrder = MutableStateFlow(listOf(
        LibraryTab.All,
        LibraryTab.Album,
        LibraryTab.Artist,
        LibraryTab.Genre,
        LibraryTab.Folder
    ))

    private val _mostPlayed = MutableStateFlow<List<SongStats>>(emptyList())
    val mostPlayed = _mostPlayed.asStateFlow()

    val playlists: StateFlow<List<Playlist>> = playlistRepository.playlists

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
        @Suppress("UNCHECKED_CAST")
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
            lyricsVisible = lyricsVisible,
            playbackSpeed = playerController.playbackSpeed.value,
            abRepeatA = playerController.abRepeatA.value,
            abRepeatB = playerController.abRepeatB.value,
            sleepTimerRemaining = playerController.sleepTimerRemaining.value,
            searchQuery = _searchQuery.value
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
        _selectedLibraryTab,
        _tabOrder
    ) { songs, isLoading, hasAudioPermission, selectedTab, tabOrder ->
        HomeUiState(
            isLoading = isLoading,
            hasAudioPermission = hasAudioPermission,
            selectedTab = selectedTab,
            songs = songs,
            libraryGroups = buildLibraryGroups(songs, selectedTab),
            tabOrder = tabOrder
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun loadStats(startTime: Long = 0L) {
        viewModelScope.launch {
            _mostPlayed.value = statsRepository.getMostPlayed(_songs.value, startTime)
        }
    }

    fun reorderTabs(fromIndex: Int, toIndex: Int) {
        val currentOrder = _tabOrder.value.toMutableList()
        val item = currentOrder.removeAt(fromIndex)
        currentOrder.add(toIndex, item)
        _tabOrder.value = currentOrder
    }

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
                    // Restore queue state
                    restoreQueueState(songs)
                }
        }
    }

    private suspend fun restoreQueueState(songs: List<Song>) {
        val queueState = preferencesRepository.loadQueueState() ?: return
        if (queueState.songIds.isEmpty()) return
        val songMap = songs.associateBy { it.id }
        val queueSongs = queueState.songIds.mapNotNull { songMap[it] }
        if (queueSongs.isNotEmpty()) {
            playerController.setPlaylist(queueSongs, queueState.currentIndex.coerceIn(0, queueSongs.size - 1))
        }
    }

    fun saveQueueState() {
        viewModelScope.launch {
            val queue = uiState.value.queue
            if (queue.isNotEmpty()) {
                preferencesRepository.saveQueueState(
                    songIds = queue.map { it.id },
                    currentIndex = playerController.currentIndex.value,
                    positionMs = playerController.getCurrentPosition()
                )
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

    // --- Search & Filter ---
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchSongs(query: String): List<Song> {
        if (query.isBlank()) return _songs.value
        val q = query.lowercase()
        return _songs.value.filter {
            it.title.lowercase().contains(q) ||
            it.artist.lowercase().contains(q) ||
            it.album.lowercase().contains(q) ||
            (it.genre?.lowercase()?.contains(q) == true) ||
            (it.folder?.lowercase()?.contains(q) == true)
        }
    }

    // --- Playback Speed ---
    fun setPlaybackSpeed(speed: Float) {
        playerController.setPlaybackSpeed(speed)
    }

    // --- A-B Repeat ---
    fun setABRepeatA() = playerController.setABRepeatA()
    fun setABRepeatB() = playerController.setABRepeatB()
    fun clearABRepeat() = playerController.clearABRepeat()

    // --- Sleep Timer ---
    fun setSleepTimer(minutes: Int) {
        if (minutes <= 0) {
            playerController.cancelSleepTimer()
        } else {
            playerController.setSleepTimer(minutes * 60L * 1000L)
        }
    }

    fun cancelSleepTimer() = playerController.cancelSleepTimer()

    // --- Fast Forward / Rewind ---
    fun fastForward(stepMs: Long = 10_000) = playerController.fastForward(stepMs)
    fun rewind(stepMs: Long = 10_000) = playerController.rewind(stepMs)

    // --- Playlists ---
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            playlistRepository.createPlaylist(name)
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlistId)
        }
    }

    fun renamePlaylist(playlistId: String, newName: String) {
        viewModelScope.launch {
            playlistRepository.renamePlaylist(playlistId, newName)
        }
    }

    fun addSongToPlaylist(playlistId: String, songId: Long) {
        viewModelScope.launch {
            playlistRepository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: Long) {
        viewModelScope.launch {
            playlistRepository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun getSongsForPlaylist(playlist: Playlist): List<Song> {
        val songMap = _songs.value.associateBy { it.id }
        return playlist.songIds.mapNotNull { songMap[it] }
    }

    fun playPlaylist(playlist: Playlist, startIndex: Int = 0) {
        val songs = getSongsForPlaylist(playlist)
        if (songs.isNotEmpty()) {
            playerController.setPlaylist(songs, startIndex)
            playerController.play()
        }
    }

    override fun onCleared() {
        super.onCleared()
        saveQueueState()
        playerController.release()
    }
}

enum class RepeatMode {
    OFF,
    REPEAT_ALL,
    REPEAT_ONE,
    PLAY_ONE_ONCE,
    PLAY_ALL_ONCE
}
