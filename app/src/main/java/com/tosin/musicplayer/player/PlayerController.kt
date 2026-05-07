package com.tosin.musicplayer.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.tosin.musicplayer.data.models.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerController(private val context: Context) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val mediaController: MediaController?
        get() = if (controllerFuture?.isDone == true) controllerFuture?.get() else null

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0L)
    val progress = _progress.asStateFlow()

    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    
    private var playlist: List<Song> = emptyList()

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
                val index = controller.currentMediaItemIndex
                if (index >= 0 && index < playlist.size) {
                    _currentSong.value = playlist[index]
                } else {
                    mediaItem?.let {
                        _currentSong.value = Song(
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
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _progress.value = controller.currentPosition
                }
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

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    fun next() {
        mediaController?.seekToNext()
    }

    fun previous() {
        mediaController?.seekToPrevious()
    }

    fun release() {
        stopProgressUpdate()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                _progress.value = mediaController?.currentPosition ?: 0L
                delay(1000)
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
                .setArtworkUri(albumArt?.let { Uri.parse(it) })
                .build()
        )
        .build()
}
