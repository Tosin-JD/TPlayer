package com.tosin.musicplayer.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.tosin.musicplayer.MainActivity
import com.tosin.musicplayer.R
import com.tosin.musicplayer.data.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private var showNotifications = true

    // Crossfade audio processor state — shared with the audio thread
    private val crossfadeState = CrossfadeState()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var positionPollingJob: Job? = null

    companion object {
        private const val NOTIFICATION_ID = 1001

        // Custom session command actions for real-time crossfade settings updates
        const val ACTION_SET_CROSSFADE_ENABLED = "com.tosin.musicplayer.SET_CROSSFADE_ENABLED"
        const val ACTION_SET_CROSSFADE_DURATION = "com.tosin.musicplayer.SET_CROSSFADE_DURATION"

        // Bundle keys
        const val KEY_CROSSFADE_ENABLED = "crossfade_enabled"
        const val KEY_CROSSFADE_DURATION_SECONDS = "crossfade_duration_seconds"
    }

    override fun onCreate() {
        super.onCreate()
        val savedEqState = runBlocking { PreferencesRepository(this@PlaybackService).loadEqualizerState() }
        val settings = runBlocking { PreferencesRepository(this@PlaybackService).loadSettings() }
        showNotifications = settings["showNotifications"] as? Boolean ?: true

        // Initialize crossfade state from persisted settings
        crossfadeState.enabled = settings["crossfadeEnabled"] as? Boolean ?: false
        val crossfadeDurationSeconds = toIntSafe(settings["crossfadeDuration"], 3)
        crossfadeState.fadeDurationUs = crossfadeDurationSeconds * 1_000_000L

        // Build the primary player with CrossfadeRenderersFactory so the
        // CrossfadeAudioProcessor is in the audio pipeline
        val primaryPlayer = ExoPlayer.Builder(this, CrossfadeRenderersFactory(this, crossfadeState))
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                /* handleAudioFocus= */ true
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        val playbackSpeed = (settings["playbackSpeed"] as? Float)
            ?: (settings["playbackSpeed"] as? Double)?.toFloat()
        if (playbackSpeed != null && playbackSpeed > 0f) {
            primaryPlayer.playbackParameters = PlaybackParameters(playbackSpeed)
        }

        primaryPlayer.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                if (audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                    EqualizerManager.init(audioSessionId, savedEqState)
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    startPositionPolling(primaryPlayer)
                } else {
                    stopPositionPolling()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // Reset audio processor position tracking on track change
                crossfadeState.seekPositionUs = 0L
                crossfadeState.trackDurationUs = C.TIME_UNSET
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    // Update seek position so the audio processor recalculates fade boundaries
                    val posMs = newPosition.positionMs
                    crossfadeState.seekPositionUs = posMs * 1_000L
                }
            }
        })

        // Create pending intent for notification tap -> open app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val sessionActivity = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationProvider = DefaultMediaNotificationProvider.Builder(this).build()
        setMediaNotificationProvider(object : MediaNotification.Provider {
            override fun createNotification(
                mediaSession: MediaSession,
                customActionButtons: ImmutableList<CommandButton>,
                actionFactory: MediaNotification.ActionFactory,
                callback: MediaNotification.Provider.Callback
            ): MediaNotification {
                if (!showNotifications) {
                    return createSilentNotification()
                }
                return notificationProvider.createNotification(
                    mediaSession,
                    customActionButtons,
                    actionFactory,
                    callback
                )
            }

            override fun handleCustomCommand(
                mediaSession: MediaSession,
                customAction: String,
                extras: Bundle
            ): Boolean {
                return notificationProvider.handleCustomCommand(mediaSession, customAction, extras)
            }

            override fun getNotificationChannelInfo(): MediaNotification.Provider.NotificationChannelInfo {
                return notificationProvider.getNotificationChannelInfo()
            }
        })

        mediaSession = MediaSession.Builder(this, primaryPlayer)
            .setSessionActivity(sessionActivity)
            .setCallback(object : MediaSession.Callback {
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    val commands = MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS
                        .buildUpon()
                        .add(SessionCommand(ACTION_SET_CROSSFADE_ENABLED, Bundle.EMPTY))
                        .add(SessionCommand(ACTION_SET_CROSSFADE_DURATION, Bundle.EMPTY))
                        .build()
                    return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                        .setAvailableSessionCommands(commands)
                        .build()
                }

                override fun onCustomCommand(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    customCommand: SessionCommand,
                    args: Bundle
                ): ListenableFuture<SessionResult> {
                    when (customCommand.customAction) {
                        ACTION_SET_CROSSFADE_ENABLED -> {
                            crossfadeState.enabled = args.getBoolean(KEY_CROSSFADE_ENABLED, false)
                            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                        }
                        ACTION_SET_CROSSFADE_DURATION -> {
                            val seconds = args.getInt(KEY_CROSSFADE_DURATION_SECONDS, 3)
                            crossfadeState.fadeDurationUs = seconds * 1_000_000L
                            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                        }
                    }
                    return Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_NOT_SUPPORTED))
                }
            })
            .build()
    }

    /**
     * Periodically updates [CrossfadeState] with current playback position and track
     * duration so the [CrossfadeAudioProcessor] on the audio thread can calculate
     * where it is relative to track boundaries.
     */
    private fun startPositionPolling(player: ExoPlayer) {
        stopPositionPolling()
        positionPollingJob = serviceScope.launch {
            while (isActive) {
                val posMs = player.currentPosition
                val durMs = player.duration
                crossfadeState.seekPositionUs = posMs * 1_000L
                if (durMs > 0 && durMs != C.TIME_UNSET) {
                    crossfadeState.trackDurationUs = durMs * 1_000L
                }
                delay(200L)
            }
        }
    }

    private fun stopPositionPolling() {
        positionPollingJob?.cancel()
        positionPollingJob = null
    }

    private fun createSilentNotification(): MediaNotification {
        val channelId = "tplayer_quiet_notifications"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                channelId,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                setShowBadge(false)
                setSound(null, null)
            }
            manager.createNotificationChannel(channel)
        }
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        val notification = builder
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(getString(R.string.app_name))
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        return MediaNotification(NOTIFICATION_ID, notification)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        EqualizerManager.release()
        stopPositionPolling()
        serviceScope.cancel()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    /**
     * Safely converts a JSON-deserialized number to Int.
     * JSONObject.get() can return Int, Long, or Double depending on the value.
     */
    private fun toIntSafe(value: Any?, default: Int): Int {
        return when (value) {
            is Int -> value
            is Long -> value.toInt()
            is Double -> value.toInt()
            is Float -> value.toInt()
            else -> default
        }
    }
}
