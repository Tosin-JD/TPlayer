package com.tosin.musicplayer.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.SystemClock
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.ui.viewmodel.RepeatMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

import com.tosin.musicplayer.data.repository.StatsRepository
import com.tosin.musicplayer.data.repository.PreferencesRepository

class PlayerController(
    private val context: Context,
    private val statsRepository: StatsRepository,
    private val preferencesRepository: PreferencesRepository
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val mediaController: MediaController?
        get() = if (controllerFuture?.isDone == true) controllerFuture?.get() else null

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0L)
    val progress = _progress.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue = _queue.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed = _playbackSpeed.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()

    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private var playlist: List<Song> = emptyList()
        set(value) {
            field = value
            _queue.value = value
        }

    private var excludedFolders: Set<String> = emptySet()

    private var currentRepeatMode = RepeatMode.PLAY_ALL_ONCE
    
    private var pauseOnZeroVolumeEnabled = true
        private var wasPlayingBeforeZeroVolume = false

    private var playStartedAtMs: Long? = null
    private var accumulatedListenMs = 0L
    private var suppressStatsForRestore = false

    private companion object {
        const val MIN_LISTEN_MS = 1_000L
    }

    // Auto-resume (per-track position)
    private var autoResumeEnabled = true
    private var lastPersistedSongId: Long? = null

    // A-B Repeat
    private val _abRepeatA = MutableStateFlow<Long?>(null)
    val abRepeatA = _abRepeatA.asStateFlow()
    private val _abRepeatB = MutableStateFlow<Long?>(null)
    val abRepeatB = _abRepeatB.asStateFlow()
    private var abRepeatJob: Job? = null

    // Sleep timer
    private val _sleepTimerRemaining = MutableStateFlow<Long?>(null)
    val sleepTimerRemaining = _sleepTimerRemaining.asStateFlow()

    init {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            setupController()
        }, MoreExecutors.directExecutor())
    }

    private fun setupController() {
        val controller = mediaController ?: return
        
        _isPlaying.value = controller.isPlaying
        if (controller.isPlaying) {
            startProgressUpdate()
        }
        
        val index = controller.currentMediaItemIndex
        _currentIndex.value = index
        val item = controller.currentMediaItem
        if (item != null) {
            val song = if (index >= 0 && index < playlist.size) {
                playlist[index]
            } else {
                Song(
                    id = item.mediaId.toLongOrNull() ?: 0L,
                    title = item.mediaMetadata.title?.toString() ?: "",
                    artist = item.mediaMetadata.artist?.toString() ?: "",
                    album = item.mediaMetadata.albumTitle?.toString() ?: "Unknown album",
                    genre = null,
                    folder = null,
                    uri = item.localConfiguration?.uri?.toString() ?: "",
                    albumArt = item.mediaMetadata.artworkUri?.toString(),
                    duration = controller.duration.coerceAtLeast(0)
                )
            }
            _currentSong.value = song
        }
        
        _progress.value = controller.currentPosition.coerceAtLeast(0)
        _playbackSpeed.value = controller.playbackParameters.speed

        if (playlist.isNotEmpty() && controller.mediaItemCount == 0) {
            val startIndex = _currentIndex.value.coerceIn(0, playlist.size - 1)
            controller.setMediaItems(playlist.map { it.toMediaItem() })
            controller.prepare()
            controller.seekTo(startIndex, _progress.value)
        }
        
        controller.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startProgressUpdate()
                    startListenIfNeeded()
                } else {
                    stopProgressUpdate()
                    persistPosition(_currentSong.value, mediaController?.currentPosition ?: 0L)
                    accumulateListenSegment()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // Close out the outgoing song's listened-time before switching.
                recordCurrentListen()

                // Persist the outgoing song's position before switching to the new one.
                persistPosition(_currentSong.value, _progress.value)

                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO && currentRepeatMode == RepeatMode.PLAY_ONE_ONCE) {
                    controller.pause()
                    controller.seekToPrevious()
                }

                val index = controller.currentMediaItemIndex
                _currentIndex.value = index
                val song = if (index >= 0 && index < playlist.size) {
                    playlist[index]
                } else {
                    mediaItem?.let {
                        Song(
                            id = it.mediaId.toLongOrNull() ?: 0L,
                            title = it.mediaMetadata.title?.toString() ?: "",
                            artist = it.mediaMetadata.artist?.toString() ?: "",
                            album = it.mediaMetadata.albumTitle?.toString() ?: "Unknown album",
                            genre = null,
                            folder = null,
                            uri = it.localConfiguration?.uri?.toString() ?: "",
                            albumArt = it.mediaMetadata.artworkUri?.toString(),
                            duration = controller.duration
                        )
                    }
                }

                _currentSong.value = song
                lastPersistedSongId = null
                // Clear A-B repeat on track change
                clearABRepeat()

                // Keep counting if playback continues seamlessly into the next song.
                if (controller.isPlaying) {
                    startListenIfNeeded()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _progress.value = controller.currentPosition
                }
            }

            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                _playbackSpeed.value = playbackParameters.speed
            }

            override fun onDeviceVolumeChanged(volume: Int, muted: Boolean) {
                if (pauseOnZeroVolumeEnabled) {
                    if (volume == 0 || muted) {
                        if (controller.isPlaying) {
                            wasPlayingBeforeZeroVolume = true
                            pause()
                        }
                    } else if (volume > 0 && !muted) {
                        if (wasPlayingBeforeZeroVolume) {
                            play()
                            wasPlayingBeforeZeroVolume = false
                        }
                    }
                }
            }
        })
    }

    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        setPlaylist(songs, startIndex, 0L)
    }

    fun setPlaylist(songs: List<Song>, startIndex: Int = 0, startPositionMs: Long = 0L) {
        playlist = songs
        if (songs.isNotEmpty() && startIndex in songs.indices) {
            _currentSong.value = songs[startIndex]
            _currentIndex.value = startIndex
            _progress.value = startPositionMs
        }
        val controller = mediaController ?: return
        val mediaItems = songs.map { it.toMediaItem() }
        controller.setMediaItems(mediaItems)
        controller.prepare()
        controller.seekTo(startIndex, startPositionMs.coerceAtLeast(0L))

        if (autoResumeEnabled && startPositionMs <= 0L) {
            val startSongId = songs.getOrNull(startIndex)?.id
            if (startSongId != null) {
                scope.launch {
                    val saved = preferencesRepository.getResumePosition(startSongId)
                    if (saved > 0L) {
                        controller.seekTo(startIndex, saved)
                        _progress.value = saved
                    }
                }
            }
        }
    }

    fun addSongsToQueue(songs: List<Song>) {
        if (songs.isEmpty()) return
        val controller = mediaController ?: return
        val currentIndex = controller.currentMediaItemIndex.takeIf { it >= 0 } ?: playlist.lastIndex.coerceAtLeast(0)
        val updated = playlist.toMutableList().apply { addAll(songs) }
        playlist = updated
        controller.setMediaItems(updated.map { it.toMediaItem() }, currentIndex.coerceAtMost(updated.lastIndex), controller.currentPosition)
        controller.prepare()
    }

    fun playNextSongs(songs: List<Song>) {
        if (songs.isEmpty()) return
        val controller = mediaController ?: return
        val currentIndex = controller.currentMediaItemIndex.takeIf { it >= 0 } ?: -1
        val insertIndex = if (currentIndex >= 0) currentIndex + 1 else 0
        val updated = playlist.toMutableList().apply {
            addAll(insertIndex.coerceAtMost(size), songs)
        }
        playlist = updated
        val seekIndex = currentIndex.coerceAtLeast(0).coerceAtMost(updated.lastIndex)
        controller.setMediaItems(updated.map { it.toMediaItem() }, seekIndex, controller.currentPosition)
        controller.prepare()
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        if (fromIndex !in playlist.indices || toIndex !in playlist.indices) return
        val controller = mediaController ?: return
        val reordered = playlist.toMutableList().apply {
            val item = removeAt(fromIndex)
            add(toIndex, item)
        }
        playlist = reordered
        val currentId = _currentSong.value?.id
        val currentIndex = reordered.indexOfFirst { it.id == currentId }.takeIf { it >= 0 } ?: controller.currentMediaItemIndex.coerceAtLeast(0)
        controller.setMediaItems(reordered.map { it.toMediaItem() }, currentIndex.coerceAtMost(reordered.lastIndex), controller.currentPosition)
        controller.prepare()
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun stop() {
        recordCurrentListen()
        persistPosition(_currentSong.value, _progress.value)
        lastPersistedSongId = null
        mediaController?.run {
            stop()
        }
        _isPlaying.value = false
        _currentSong.value = null
        _currentIndex.value = 0
        _progress.value = 0L
        sleepTimerJob?.cancel()
        _sleepTimerRemaining.value = null
        clearABRepeat()
    }

    fun setExcludedFolders(folders: Set<String>) {
        excludedFolders = folders.map { it.trim().trimEnd('/') }.toSet()
        val currentSongId = _currentSong.value?.id
        val filteredPlaylist = playlist.filterNot { it.isExcluded(excludedFolders) }
        playlist = filteredPlaylist

        if (currentSongId != null && filteredPlaylist.none { it.id == currentSongId }) {
            stop()
            return
        }

        val controller = mediaController ?: return
        if (filteredPlaylist.isNotEmpty()) {
            val currentIndex = filteredPlaylist.indexOfFirst { it.id == currentSongId }.takeIf { it >= 0 }
                ?: _currentIndex.value.coerceIn(0, filteredPlaylist.lastIndex)
            controller.setMediaItems(filteredPlaylist.map { it.toMediaItem() }, currentIndex, controller.currentPosition)
        }
    }

    fun updateSongMetadata(
        songId: Long,
        title: String? = null,
        artist: String? = null,
        album: String? = null,
        genre: String? = null,
        lyrics: String? = null
    ) {
        val updated = playlist.map { song ->
            if (song.id == songId) {
                song.copy(
                    title = title?.takeIf { it.isNotBlank() } ?: song.title,
                    artist = artist?.takeIf { it.isNotBlank() } ?: song.artist,
                    album = album?.takeIf { it.isNotBlank() } ?: song.album,
                    genre = genre?.takeIf { it.isNotBlank() } ?: song.genre,
                    lyrics = lyrics ?: song.lyrics
                )
            } else {
                song
            }
        }
        playlist = updated
        if (_currentSong.value?.id == songId) {
            _currentSong.value = updated.firstOrNull { it.id == songId }
        }
    }

    fun seekToMediaItem(index: Int) {
        mediaController?.seekTo(index, 0)
        mediaController?.play()
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    fun next() {
        mediaController?.seekToNext()
    }

    fun previous() {
        mediaController?.seekToPrevious()
    }

    fun setShuffleEnabled(enabled: Boolean) {
        if (enabled && playlist.size > 1) {
            val controller = mediaController ?: return
            val currentId = _currentSong.value?.id
            val currentPos = controller.currentPosition
            val shuffled = playlist.toMutableList()
            // Remove current song, shuffle the rest, put current song at index 0
            val currentSongObj = shuffled.firstOrNull { it.id == currentId }
            if (currentSongObj != null) {
                shuffled.remove(currentSongObj)
                shuffled.shuffle()
                shuffled.add(0, currentSongObj)
            } else {
                shuffled.shuffle()
            }
            playlist = shuffled
            val newIndex = if (currentSongObj != null) 0 else 0
            controller.setMediaItems(shuffled.map { it.toMediaItem() }, newIndex, currentPos)
            controller.prepare()
        }
        // Don't use Media3's built-in shuffle — we manage it ourselves
    }

    fun setRepeatMode(mode: RepeatMode) {
        currentRepeatMode = mode
        val controller = mediaController ?: return
        when (mode) {
            RepeatMode.PLAY_ALL_ONCE -> {
                controller.repeatMode = Player.REPEAT_MODE_OFF
            }
            RepeatMode.PLAY_ONE_ONCE -> {
                controller.repeatMode = Player.REPEAT_MODE_OFF
            }
            RepeatMode.REPEAT_ALL -> {
                controller.repeatMode = Player.REPEAT_MODE_ALL
            }
            RepeatMode.REPEAT_ONE -> {
                controller.repeatMode = Player.REPEAT_MODE_ONE
            }
        }
    }

    fun setPauseOnZeroVolumeEnabled(enabled: Boolean) {
        pauseOnZeroVolumeEnabled = enabled
        if (!enabled) {
            wasPlayingBeforeZeroVolume = false
        }
    }

    fun setAutoResumeEnabled(enabled: Boolean) {
        autoResumeEnabled = enabled
    }

    private fun persistPosition(song: Song?, positionMs: Long) {
        if (!autoResumeEnabled) return
        val songId = song?.id ?: return
        if (lastPersistedSongId == songId) return
        if (positionMs <= 0L) return
        lastPersistedSongId = songId
        scope.launch {
            preferencesRepository.saveResumePosition(songId, positionMs)
        }
    }

    // --- Playback Speed ---
    fun setPlaybackSpeed(speed: Float) {
        val controller = mediaController ?: return
        controller.playbackParameters = PlaybackParameters(speed)
        _playbackSpeed.value = speed
    }

    // --- A-B Repeat ---
    fun setABRepeatA() {
        _abRepeatA.value = mediaController?.currentPosition
        _abRepeatB.value = null
        abRepeatJob?.cancel()
    }

    fun setABRepeatB() {
        val a = _abRepeatA.value ?: return
        val b = mediaController?.currentPosition ?: return
        if (b <= a) return
        _abRepeatB.value = b
        startABRepeatLoop(a, b)
    }

    fun clearABRepeat() {
        _abRepeatA.value = null
        _abRepeatB.value = null
        abRepeatJob?.cancel()
    }

    private fun startABRepeatLoop(a: Long, b: Long) {
        abRepeatJob?.cancel()
        abRepeatJob = scope.launch {
            while (isActive) {
                val pos = mediaController?.currentPosition ?: break
                if (pos >= b) {
                    mediaController?.seekTo(a)
                }
                delay(100)
            }
        }
    }

    // --- Sleep Timer ---
    fun setSleepTimer(durationMs: Long) {
        sleepTimerJob?.cancel()
        _sleepTimerRemaining.value = durationMs
        sleepTimerJob = scope.launch {
            var remaining = durationMs
            while (remaining > 0 && isActive) {
                delay(1000)
                remaining -= 1000
                _sleepTimerRemaining.value = remaining
            }
            if (remaining <= 0) {
                pause()
                _sleepTimerRemaining.value = null
            }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimerRemaining.value = null
    }

    // --- Fast forward / Rewind ---
    fun fastForward(stepMs: Long = 10_000) {
        val controller = mediaController ?: return
        val newPos = (controller.currentPosition + stepMs).coerceAtMost(controller.duration)
        controller.seekTo(newPos)
    }

    fun rewind(stepMs: Long = 10_000) {
        val controller = mediaController ?: return
        val newPos = (controller.currentPosition - stepMs).coerceAtLeast(0)
        controller.seekTo(newPos)
    }

    fun getCurrentPosition(): Long {
        return mediaController?.currentPosition ?: 0L
    }

    fun release() {
        recordCurrentListen()
        persistPosition(_currentSong.value, mediaController?.currentPosition ?: 0L)
        stopProgressUpdate()
        sleepTimerJob?.cancel()
        abRepeatJob?.cancel()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }

    fun ignoreNextPlayForStats() {
        suppressStatsForRestore = true
    }

    private fun accumulateListenSegment() {
        val now = SystemClock.elapsedRealtime()
        val segment = playStartedAtMs?.let { (now - it).coerceAtLeast(0L) } ?: 0L
        playStartedAtMs = null
        accumulatedListenMs += segment
    }

    private fun recordCurrentListen() {
        val song = _currentSong.value ?: return
        val now = SystemClock.elapsedRealtime()
        val segment = playStartedAtMs?.let { (now - it).coerceAtLeast(0L) } ?: 0L
        playStartedAtMs = null
        val total = accumulatedListenMs + segment
        accumulatedListenMs = 0L
        if (total >= MIN_LISTEN_MS) {
            scope.launch {
                statsRepository.recordPlay(song.id, total)
            }
        }
    }

    private fun startListenIfNeeded() {
        if (suppressStatsForRestore) {
            suppressStatsForRestore = false
            playStartedAtMs = null
            return
        }
        if (playStartedAtMs == null) {
            playStartedAtMs = SystemClock.elapsedRealtime()
        }
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                _progress.value = mediaController?.currentPosition ?: 0L
                delay(500) // More responsive progress updates
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
    }

    private fun Song.isExcluded(excluded: Set<String>): Boolean {
        val folderPathMatch = folderPath?.trim()?.trimEnd('/')?.let { it in excluded } ?: false
        val folderNameMatch = folder?.trim()?.let { it in excluded } ?: false
        return folderPathMatch || folderNameMatch
    }
}

fun Song.toMediaItem(): MediaItem {
    val artworkUri = albumArtOrDefault()
    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(Uri.parse(uri))
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(artworkUri)
                .build()
        )
        .build()
}

private fun Song.albumArtOrDefault(): Uri {
    return Uri.parse(
        albumArt?.takeIf { it.isNotBlank() }
            ?: "android.resource://com.tosin.musicplayer/drawable/album_art"
    )
}
