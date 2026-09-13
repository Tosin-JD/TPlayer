package com.tosin.musicplayer.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.ui.viewmodel.RepeatMode
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import com.tosin.musicplayer.widget.WidgetArtworkHelper
import com.tosin.musicplayer.widget.WidgetConstants
import com.tosin.musicplayer.widget.WidgetState
import com.tosin.musicplayer.widget.WidgetStateRepository
import com.tosin.musicplayer.widget.WidgetUpdateDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    suspend fun awaitConnection() {
        if (_isConnected.value && mediaController != null) return
        _isConnected.first { it }
    }

    /**
     * Returns true if the underlying MediaController is actively playing
     * or has media loaded (i.e., the PlaybackService is active).
     * This reads directly from the controller, bypassing StateFlow propagation delays.
     */
    fun isServiceActive(): Boolean {
        val controller = mediaController ?: return false
        return controller.isPlaying ||
               (controller.playbackState != Player.STATE_IDLE && controller.mediaItemCount > 0)
    }

    private var playlist: List<Song> = emptyList()
        set(value) {
            field = value
            _queue.value = value
        }

    private var originalPlaylist: List<Song> = emptyList()

    private var excludedFolders: Set<String> = emptySet()

    private var currentRepeatMode = RepeatMode.PLAY_ALL_ONCE
    private var isShuffleEnabled = false
    
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

    private var pendingPlay = false

    init {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            scope.launch(Dispatchers.Main.immediate) {
                setupController()
            }
        }, MoreExecutors.directExecutor())
    }

    fun setCrossfadeEnabled(enabled: Boolean) {
        val controller = mediaController ?: return
        val args = Bundle().apply { putBoolean(PlaybackService.KEY_CROSSFADE_ENABLED, enabled) }
        controller.sendCustomCommand(
            SessionCommand(PlaybackService.ACTION_SET_CROSSFADE_ENABLED, Bundle.EMPTY),
            args
        )
    }

    fun setCrossfadeDuration(seconds: Int) {
        val controller = mediaController ?: return
        val args = Bundle().apply { putInt(PlaybackService.KEY_CROSSFADE_DURATION_SECONDS, seconds) }
        controller.sendCustomCommand(
            SessionCommand(PlaybackService.ACTION_SET_CROSSFADE_DURATION, Bundle.EMPTY),
            args
        )
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
        
        val isServiceActive = controller.isPlaying || (controller.playbackState != Player.STATE_IDLE && controller.mediaItemCount > 0)
        if (isServiceActive) {
            // Adopt active service repeat mode
            currentRepeatMode = when (controller.repeatMode) {
                Player.REPEAT_MODE_ALL -> RepeatMode.REPEAT_ALL
                Player.REPEAT_MODE_ONE -> RepeatMode.REPEAT_ONE
                else -> currentRepeatMode
            }
        } else {
            setRepeatMode(currentRepeatMode)
        }

        if (playlist.isNotEmpty() && !isServiceActive) {
            val mediaItems = playlist.map { it.toMediaItem() }
            val startIndex = _currentIndex.value.coerceIn(0, playlist.size - 1)
            val currentMediaIds = (0 until controller.mediaItemCount).map { controller.getMediaItemAt(it).mediaId }
            val newMediaIds = playlist.map { it.id.toString() }
            val queueChanged = currentMediaIds != newMediaIds || controller.currentMediaItemIndex !in playlist.indices

            if (queueChanged) {
                controller.setMediaItems(mediaItems, startIndex, _progress.value.coerceAtLeast(0L))
                controller.prepare()
            }

            if (pendingPlay) {
                controller.play()
            }
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
                syncWidgetState()
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
                syncWidgetState()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _progress.value = controller.currentPosition
                }
                // Mark connection as ready now that the session has delivered
                // a real playback state (not the default STATE_IDLE).
                if (!_isConnected.value) {
                    _isConnected.value = true
                }
                syncWidgetState()
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

        syncWidgetState()

        // Never mark connected here — the controller may still be in its
        // initial STATE_IDLE / mediaItemCount=0 state even when the service
        // is actively playing.  Wait for onPlaybackStateChanged to deliver
        // the real session state (line 276), or fall back to the safety
        // timeout below, so restoreQueueState() always sees accurate
        // isPlaying / playbackState / mediaItemCount values.
        //
        // Safety timeout: if no callback arrives within 1 s, connect anyway to
        // avoid blocking the UI forever (e.g. service just stopped).
        scope.launch {
            delay(1_000)
            if (!_isConnected.value) {
                _isConnected.value = true
            }
        }
    }

    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        setPlaylist(songs, startIndex, 0L)
    }

    fun setPlaylist(songs: List<Song>, startIndex: Int = 0, startPositionMs: Long = 0L) {
        playlist = songs
        originalPlaylist = songs
        
        val controller = mediaController ?: return
        val isServiceActive = controller.isPlaying || (controller.playbackState != Player.STATE_IDLE && controller.mediaItemCount > 0)

        val mediaItems = songs.map { it.toMediaItem() }
        val currentMediaIds = (0 until controller.mediaItemCount).map { controller.getMediaItemAt(it).mediaId }
        val newMediaIds = songs.map { it.id.toString() }
        val queueChanged = currentMediaIds != newMediaIds || controller.currentMediaItemIndex !in songs.indices

        if (songs.isNotEmpty() && startIndex in songs.indices) {
            _currentSong.value = songs[startIndex]
            _currentIndex.value = startIndex
            _progress.value = startPositionMs
        }

        if (queueChanged) {
            controller.setMediaItems(mediaItems, startIndex, startPositionMs.coerceAtLeast(0L))
            controller.prepare()
        } else {
            controller.seekTo(startIndex, startPositionMs.coerceAtLeast(0L))
        }

        if (pendingPlay) {
            controller.play()
        }

        if (autoResumeEnabled && startPositionMs <= 0L && !isServiceActive) {
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
        val updated = playlist.toMutableList().apply { addAll(songs) }
        playlist = updated
        originalPlaylist = originalPlaylist.toMutableList().apply { addAll(songs) }
        controller.addMediaItems(controller.mediaItemCount, songs.map { it.toMediaItem() })
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
        originalPlaylist = originalPlaylist.toMutableList().apply {
            addAll(insertIndex.coerceAtMost(size), songs)
        }
        controller.addMediaItems(insertIndex.coerceAtMost(controller.mediaItemCount), songs.map { it.toMediaItem() })
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
        originalPlaylist = originalPlaylist.toMutableList().apply {
            val item = removeAt(fromIndex)
            add(toIndex, item)
        }
        controller.moveMediaItem(fromIndex, toIndex)
    }

    fun play() {
        pendingPlay = true
        val controller = mediaController
        if (controller != null) {
            if (playlist.isNotEmpty() && controller.mediaItemCount == 0) {
                val startIndex = _currentIndex.value.coerceIn(0, playlist.size - 1)
                controller.setMediaItems(playlist.map { it.toMediaItem() }, startIndex, _progress.value.coerceAtLeast(0L))
                controller.prepare()
            } else if (playlist.isNotEmpty() && controller.playbackState == Player.STATE_ENDED) {
                val startIndex = _currentIndex.value.coerceIn(0, playlist.size - 1)
                controller.seekTo(startIndex, 0L)
                controller.prepare()
            } else if (playlist.isNotEmpty() && controller.playbackState == Player.STATE_IDLE) {
                controller.prepare()
            }
            controller.play()
        }
        syncWidgetState()
    }

    fun pause() {
        pendingPlay = false
        mediaController?.pause()
        syncWidgetState()
    }

    fun stop() {
        pendingPlay = false
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
        syncWidgetState()
    }

    fun setExcludedFolders(folders: Set<String>) {
        excludedFolders = folders.map { it.trim().trimEnd('/') }.toSet()

        val controller = mediaController ?: return
        val isServiceActive = controller.isPlaying || (controller.playbackState != Player.STATE_IDLE && controller.mediaItemCount > 0)

        // If the service is actively playing and we don't have a local playlist
        // (e.g. after a cold start), we can't safely filter or stop — the service
        // owns its own queue. Just store the excluded folders; they'll be applied
        // on the next song transition.
        if (isServiceActive && playlist.isEmpty()) return

        val currentSongId = _currentSong.value?.id
        val filteredPlaylist = playlist.filterNot { it.isExcluded(excludedFolders) }
        playlist = filteredPlaylist

        // Stop playback ONLY if we have a local playlist AND the active song is in an excluded folder
        if (playlist.isNotEmpty() && currentSongId != null && filteredPlaylist.none { it.id == currentSongId }) {
            stop()
            return
        }

        // Do NOT re-set media items on the controller if service is active
        if (isServiceActive) return

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
        isShuffleEnabled = enabled
        if (!enabled) {
            restoreOriginalPlaylistOrder()
        }
        syncWidgetState()
    }

    /**
     * Reshuffles the current playlist so the currently playing song is first,
     * followed by all remaining songs in random order.
     */
    fun reshuffleCurrentPlaylist() {
        val controller = mediaController ?: return
        if (playlist.isEmpty()) return

        val currentIndex = controller.currentMediaItemIndex
        if (currentIndex !in playlist.indices) return

        val currentSong = playlist[currentIndex]

        val remaining = playlist.toMutableList().apply { removeAt(currentIndex) }
        remaining.shuffle()

        val shuffled = mutableListOf(currentSong).apply { addAll(remaining) }

        if (currentIndex != 0) {
            controller.moveMediaItem(currentIndex, 0)
        }
        if (controller.mediaItemCount > 1) {
            controller.removeMediaItems(1, controller.mediaItemCount)
        }
        if (remaining.isNotEmpty()) {
            controller.addMediaItems(1, remaining.map { it.toMediaItem() })
        }

        playlist = shuffled
    }

    private fun restoreOriginalPlaylistOrder() {
        val controller = mediaController ?: return
        if (originalPlaylist.isEmpty() || playlist.isEmpty()) return
        if (playlist == originalPlaylist) return

        val currentSongId = _currentSong.value?.id
        val currentIndex = controller.currentMediaItemIndex.takeIf { it in playlist.indices }
            ?: playlist.indexOfFirst { it.id == currentSongId }

        if (currentIndex > 0) {
            controller.moveMediaItem(currentIndex, 0)
        }

        val desiredOrder = originalPlaylist
        for (targetIndex in desiredOrder.indices) {
            val desiredSongId = desiredOrder[targetIndex].id
            val actualIndex = playlist.indexOfFirst { it.id == desiredSongId }
            if (actualIndex != -1 && actualIndex != targetIndex) {
                controller.moveMediaItem(actualIndex, targetIndex)
                val movedSong = playlist[actualIndex]
                val mutable = playlist.toMutableList()
                mutable.removeAt(actualIndex)
                mutable.add(targetIndex, movedSong)
                playlist = mutable
            }
        }

        playlist = desiredOrder
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
        syncWidgetState()
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
                val controller = mediaController
                if (controller != null) {
                    _progress.value = controller.currentPosition.coerceAtLeast(0)
                } else {
                    _progress.value = 0L
                }

                delay(500L)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
    }

    suspend fun restoreSavedQueue(startPlaying: Boolean = false): Boolean {
        val settings = preferencesRepository.loadSettings()
        val rememberLastPlay = (settings["rememberLastPlay"] as? Boolean) ?: true
        if (!rememberLastPlay) return false

        awaitConnection()
        val controller = mediaController
        val isServiceActive = controller != null && (controller.isPlaying || (controller.playbackState != Player.STATE_IDLE && controller.mediaItemCount > 0))
        if (isServiceActive) return true

        val queueState = preferencesRepository.loadQueueState() ?: return false
        if (queueState.songIds.isEmpty()) return false

        var songs = preferencesRepository.loadSongCache()
        if (songs.isEmpty()) {
            songs = com.tosin.musicplayer.data.local.MusicLoader(context.contentResolver).loadSongs()
        }
        val songMap = songs.associateBy { it.id }
        val queueSongs = queueState.songIds.mapNotNull { songMap[it] }
        if (queueSongs.isNotEmpty()) {
            val startIndex = queueState.currentIndex.coerceIn(0, queueSongs.size - 1)
            setPlaylist(queueSongs, startIndex, queueState.positionMs)
            if (startPlaying || queueState.wasPlaying) {
                ignoreNextPlayForStats()
                play()
            }
            return true
        }
        return false
    }

    private fun syncWidgetState() {
        val song = _currentSong.value
        val isPlayingState = _isPlaying.value
        val progressVal = _progress.value
        val isShuffle = isShuffleEnabled
        val repeatModeName = currentRepeatMode.name
        val isQueueEmpty = song == null && playlist.isEmpty()

        scope.launch(Dispatchers.IO) {
            val repo = WidgetStateRepository(context)
            val state = WidgetState(
                title = song?.title.orEmpty(),
                artist = song?.artist.orEmpty(),
                album = song?.album.orEmpty(),
                isPlaying = isPlayingState,
                isShuffleEnabled = isShuffle,
                repeatMode = repeatModeName,
                progressMs = progressVal,
                durationMs = song?.duration ?: 0L,
                albumArtUri = song?.albumArt,
                isEmptyQueue = isQueueEmpty,
                isOffline = false,
                lastUpdatedMs = System.currentTimeMillis()
            )
            repo.saveState(state)

            val artworkBytes = WidgetArtworkHelper.extractArtworkBytes(
                context = context,
                albumArtUriStr = song?.albumArt,
                songUriStr = song?.uri
            )
            if (artworkBytes != null && artworkBytes.isNotEmpty()) {
                repo.saveArtwork(artworkBytes)
            } else {
                repo.clearArtwork()
            }

            WidgetUpdateDispatcher.updateAll(context)
        }
    }

    private fun Song.isExcluded(excluded: Set<String>): Boolean {
        val folderPathMatch = folderPath?.trim()?.trimEnd('/')?.let { it in excluded } ?: false
        val folderNameMatch = folder?.trim()?.let { it in excluded } ?: false
        return folderPathMatch || folderNameMatch
    }
}

fun Song.toMediaItem(): MediaItem {
    val artworkUri = albumArtOrDefault()
    val parsedUri = when {
        uri.startsWith("content://") || uri.startsWith("file://") || uri.startsWith("http://") || uri.startsWith("https://") -> Uri.parse(uri)
        uri.startsWith("/") -> Uri.fromFile(java.io.File(uri))
        else -> Uri.parse(uri)
    }
    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(parsedUri)
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
