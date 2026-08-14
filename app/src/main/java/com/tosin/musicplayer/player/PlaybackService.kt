package com.tosin.musicplayer.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.collect.ImmutableList
import com.tosin.musicplayer.MainActivity
import com.tosin.musicplayer.R
import com.tosin.musicplayer.data.repository.PreferencesRepository
import kotlinx.coroutines.runBlocking

@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private val crossfadeState = CrossfadeState()
    private var showNotifications = true

    override fun onCreate() {
        super.onCreate()
        val savedEqState = runBlocking { PreferencesRepository(this@PlaybackService).loadEqualizerState() }
        val settings = runBlocking { PreferencesRepository(this@PlaybackService).loadSettings() }
        crossfadeState.enabled = settings["crossfadeEnabled"] as? Boolean ?: false
        val crossfadeDurationSeconds = (settings["crossfadeDuration"] as? Int) ?: 3
        crossfadeState.fadeDurationUs = crossfadeDurationSeconds * 1_000_000L
        showNotifications = settings["showNotifications"] as? Boolean ?: true

        val player = ExoPlayer.Builder(this, CrossfadeRenderersFactory(this, crossfadeState))
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                /* handleAudioFocus= */ true
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()

        val playbackSpeed = (settings["playbackSpeed"] as? Float)
            ?: (settings["playbackSpeed"] as? Double)?.toFloat()
        if (playbackSpeed != null && playbackSpeed > 0f) {
            player.playbackParameters = PlaybackParameters(playbackSpeed)
        }

        // The audio session id is only assigned once the player's audio track is created,
        // so the Equalizer must attach as soon as the id becomes available.
        player.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                if (audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                    EqualizerManager.init(audioSessionId, savedEqState)
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                crossfadeState.updateFromPlayer(player)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    crossfadeState.updateFromPlayer(player)
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    crossfadeState.updateFromPlayer(player)
                    crossfadeState.trackStartUs += newPosition.positionMs * 1000L
                }
            }
        })

        // Create pending intent for notification tap -> open app
        val sessionActivity = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
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
                extras: android.os.Bundle
            ): Boolean {
                return notificationProvider.handleCustomCommand(mediaSession, customAction, extras)
            }

            override fun getNotificationChannelInfo(): MediaNotification.Provider.NotificationChannelInfo {
                return notificationProvider.getNotificationChannelInfo()
            }
        })

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivity)
            .build()
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

    private companion object {
        const val NOTIFICATION_ID = 1001
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
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private fun CrossfadeState.updateFromPlayer(player: Player) {
        val currentIndex = player.currentMediaItemIndex
        if (currentIndex == C.INDEX_UNSET) return
        val timeline = player.currentTimeline
        if (currentIndex >= timeline.windowCount) return

        var startUs = 0L
        val window = Timeline.Window()
        for (index in 0 until currentIndex) {
            timeline.getWindow(index, window)
            if (window.durationUs == C.TIME_UNSET) break
            startUs += window.durationUs
        }
        trackStartUs = startUs

        val duration = player.duration
        trackDurationUs = if (duration == C.TIME_UNSET) C.TIME_UNSET else duration * 1000L
    }
}
