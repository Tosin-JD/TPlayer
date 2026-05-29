package com.tosin.musicplayer.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
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

class PlayerController(
    private val context: Context,
    private val statsRepository: StatsRepository
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

    private var currentRepeatMode = RepeatMode.PLAY_ALL_ONCE

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
        controller.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startProgressUpdate()
                } else {
                    stopProgressUpdate()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
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
                // Clear A-B repeat on track change
                clearABRepeat()

                song?.let {
                    scope.launch {
                        statsRepository.recordPlay(it.id, it.duration)
                    }
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
        })
    }

    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        playlist = songs
        val controller = mediaController ?: return
        val mediaItems = songs.map { it.toMediaItem() }
        controller.setMediaItems(mediaItems)
        controller.prepare()
        controller.seekTo(startIndex, 0)
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
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
        mediaController?.shuffleModeEnabled = enabled
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
            RepeatMode.OFF -> {
                controller.repeatMode = Player.REPEAT_MODE_OFF
            }
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
        stopProgressUpdate()
        sleepTimerJob?.cancel()
        abRepeatJob?.cancel()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
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
}

fun Song.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(Uri.parse(uri))
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(albumArt?.let { Uri.parse(it) })
                .build()
        )
        .build()
}
