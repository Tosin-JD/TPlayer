package com.tosin.musicplayer.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import com.tosin.musicplayer.MainActivity
import com.tosin.musicplayer.R

object WidgetRemoteViewsFactory {

    fun buildResponsive(
        context: Context,
        state: WidgetState,
        artworkBytes: ByteArray?,
        options: Bundle?,
        providerClassName: String? = null
    ): RemoteViews {
        // Fast-path: dedicated providers always render their target layout
        if (providerClassName?.contains("CompactWidgetProvider") == true) {
            return buildSingle(context, state, artworkBytes, WidgetSizing.SizeMode.COMPACT)
        }
        if (providerClassName?.contains("FullscreenWidgetProvider") == true) {
            return buildSingle(context, state, artworkBytes, WidgetSizing.SizeMode.FULLSCREEN)
        }

        // On Android 12+ (API 31+), provide responsive RemoteViews mapping
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val viewsMap = mapOf(
                SizeF(80f, 80f) to buildSingle(context, state, artworkBytes, WidgetSizing.SizeMode.TINY),
                SizeF(250f, 60f) to buildSingle(context, state, artworkBytes, WidgetSizing.SizeMode.COMPACT),
                SizeF(180f, 110f) to buildSingle(context, state, artworkBytes, WidgetSizing.SizeMode.SMALL),
                SizeF(260f, 150f) to buildSingle(context, state, artworkBytes, WidgetSizing.SizeMode.MEDIUM),
                SizeF(300f, 200f) to buildSingle(context, state, artworkBytes, WidgetSizing.SizeMode.LARGE),
                SizeF(280f, 280f) to buildSingle(context, state, artworkBytes, WidgetSizing.SizeMode.FULLSCREEN)
            )
            return RemoteViews(viewsMap)
        }

        val sizeMode = WidgetSizing.resolveSize(options, providerClassName)
        return buildSingle(context, state, artworkBytes, sizeMode)
    }

    fun build(
        context: Context,
        state: WidgetState,
        artworkBytes: ByteArray?,
        options: Bundle?,
        providerClassName: String? = null
    ): RemoteViews {
        val sizeMode = WidgetSizing.resolveSize(options, providerClassName)
        return buildSingle(context, state, artworkBytes, sizeMode)
    }

    private fun buildSingle(
        context: Context,
        state: WidgetState,
        artworkBytes: ByteArray?,
        sizeMode: WidgetSizing.SizeMode
    ): RemoteViews {
        val layoutId = when (sizeMode) {
            WidgetSizing.SizeMode.TINY -> R.layout.widget_player_tiny
            WidgetSizing.SizeMode.SMALL -> R.layout.widget_player_small
            WidgetSizing.SizeMode.MEDIUM -> R.layout.widget_player_medium
            WidgetSizing.SizeMode.LARGE -> R.layout.widget_player_large
            WidgetSizing.SizeMode.COMPACT -> R.layout.widget_player_compact
            WidgetSizing.SizeMode.FULLSCREEN -> R.layout.widget_player_fullscreen
        }

        return RemoteViews(context.packageName, layoutId).apply {
            val density = context.resources.displayMetrics.density
            val isEmpty = state.isEmptyQueue || state.title.isBlank()

            // Text Setup
            val titleText = if (isEmpty) {
                context.getString(R.string.widget_nothing_playing)
            } else if (sizeMode == WidgetSizing.SizeMode.COMPACT && state.artist.isNotBlank()) {
                "${state.title} · ${state.artist}"
            } else {
                state.title
            }
            val artistText = if (isEmpty) "" else if (state.artist.isBlank()) context.getString(R.string.widget_empty_artist) else state.artist
            val albumText = if (isEmpty) "" else if (state.album.isBlank()) context.getString(R.string.widget_empty_album) else state.album

            setTextViewText(R.id.widget_title, titleText)
            setTextViewText(R.id.widget_artist, artistText)
            setTextViewText(R.id.widget_album, albumText)

            val progressText = if (isEmpty || state.durationMs <= 0) "" else buildProgressText(state.progressMs, state.durationMs)
            setTextViewText(R.id.widget_progress_text, progressText)

            // Play / Pause Icon
            val playIcon = if (state.isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
            setImageViewResource(R.id.widget_play_pause, playIcon)

            // Shuffle Button
            setImageViewResource(R.id.widget_shuffle, R.drawable.ic_widget_shuffle)
            setInt(
                R.id.widget_shuffle,
                "setBackgroundResource",
                if (state.isShuffleEnabled) R.drawable.widget_btn_bg_active else R.drawable.widget_btn_bg
            )

            // Repeat Button
            val isRepeatOne = state.repeatMode == "REPEAT_ONE"
            val isRepeatActive = state.repeatMode == "REPEAT_ALL" || isRepeatOne
            setImageViewResource(
                R.id.widget_repeat,
                if (isRepeatOne) R.drawable.ic_widget_repeat_one else R.drawable.ic_widget_repeat
            )
            setInt(
                R.id.widget_repeat,
                "setBackgroundResource",
                if (isRepeatActive) R.drawable.widget_btn_bg_active else R.drawable.widget_btn_bg
            )

            // Artwork Rendering
            val rawBitmap = if (artworkBytes != null && artworkBytes.isNotEmpty()) {
                runCatching { BitmapFactory.decodeByteArray(artworkBytes, 0, artworkBytes.size) }.getOrNull()
            } else null

            if (rawBitmap != null) {
                when (sizeMode) {
                    WidgetSizing.SizeMode.COMPACT -> {
                        val circleBitmap = WidgetArtworkHelper.createCircularBitmap(rawBitmap, (40 * density).toInt().coerceAtLeast(1))
                        setImageViewBitmap(R.id.widget_artwork, circleBitmap)
                    }
                    WidgetSizing.SizeMode.FULLSCREEN -> {
                        val roundedBitmap = WidgetArtworkHelper.createRoundedBitmap(rawBitmap, (160 * density).toInt().coerceAtLeast(1), (160 * density).toInt().coerceAtLeast(1), 16f, density)
                        setImageViewBitmap(R.id.widget_artwork, roundedBitmap)

                        val bgDimmed = WidgetArtworkHelper.createDimmedBackgroundBitmap(rawBitmap, (240 * density).toInt().coerceAtLeast(1), (240 * density).toInt().coerceAtLeast(1))
                        setImageViewBitmap(R.id.widget_bg_art, bgDimmed)
                    }
                    WidgetSizing.SizeMode.TINY -> {
                        val roundedBitmap = WidgetArtworkHelper.createRoundedBitmap(rawBitmap, (100 * density).toInt().coerceAtLeast(1), (100 * density).toInt().coerceAtLeast(1), 12f, density)
                        setImageViewBitmap(R.id.widget_artwork, roundedBitmap)
                    }
                    else -> {
                        val roundedBitmap = WidgetArtworkHelper.createRoundedBitmap(rawBitmap, (80 * density).toInt().coerceAtLeast(1), (80 * density).toInt().coerceAtLeast(1), 12f, density)
                        setImageViewBitmap(R.id.widget_artwork, roundedBitmap)
                    }
                }
            } else {
                setImageViewResource(R.id.widget_artwork, R.drawable.widget_placeholder_art)
                if (sizeMode == WidgetSizing.SizeMode.FULLSCREEN) {
                    setImageViewResource(R.id.widget_bg_art, R.drawable.widget_placeholder_art)
                }
            }

            // Fullscreen Progress Bar
            if (sizeMode == WidgetSizing.SizeMode.FULLSCREEN) {
                val maxProgress = 1000
                val progress = if (state.durationMs > 0) {
                    ((state.progressMs * maxProgress) / state.durationMs).toInt().coerceIn(0, maxProgress)
                } else 0
                setProgressBar(R.id.widget_progress_bar, maxProgress, progress, false)
            }

            // PendingIntent Setup
            val playPauseAction = if (state.isPlaying) WidgetConstants.ACTION_WIDGET_PAUSE else WidgetConstants.ACTION_WIDGET_PLAY
            setOnClickPendingIntent(R.id.widget_play_pause, createBroadcastPendingIntent(context, playPauseAction))
            setOnClickPendingIntent(R.id.widget_stop, createBroadcastPendingIntent(context, WidgetConstants.ACTION_WIDGET_STOP))
            setOnClickPendingIntent(R.id.widget_next, createBroadcastPendingIntent(context, WidgetConstants.ACTION_WIDGET_NEXT))
            setOnClickPendingIntent(R.id.widget_prev, createBroadcastPendingIntent(context, WidgetConstants.ACTION_WIDGET_PREV))

            val shuffleIntent = Intent(context, WidgetActionReceiver::class.java).apply {
                action = WidgetConstants.ACTION_WIDGET_SHUFFLE
                putExtra(WidgetConstants.EXTRA_SHUFFLE_ENABLED, state.isShuffleEnabled)
            }
            setOnClickPendingIntent(
                R.id.widget_shuffle,
                PendingIntent.getBroadcast(
                    context,
                    WidgetConstants.ACTION_WIDGET_SHUFFLE.hashCode(),
                    shuffleIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            val repeatIntent = Intent(context, WidgetActionReceiver::class.java).apply {
                action = WidgetConstants.ACTION_WIDGET_REPEAT
                putExtra(WidgetConstants.EXTRA_REPEAT_MODE, state.repeatMode)
            }
            setOnClickPendingIntent(
                R.id.widget_repeat,
                PendingIntent.getBroadcast(
                    context,
                    WidgetConstants.ACTION_WIDGET_REPEAT.hashCode(),
                    repeatIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            val appIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                WidgetConstants.ACTION_WIDGET_OPEN_APP.hashCode(),
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

            // Ensure transport row is visible
            if (sizeMode != WidgetSizing.SizeMode.TINY && sizeMode != WidgetSizing.SizeMode.COMPACT) {
                setViewVisibility(R.id.widget_transport_row, View.VISIBLE)
            }
        }
    }

    private fun createBroadcastPendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, WidgetActionReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildProgressText(progressMs: Long, durationMs: Long): String {
        fun format(ms: Long): String {
            val totalSec = (ms / 1000).coerceAtLeast(0)
            val min = totalSec / 60
            val sec = totalSec % 60
            return "%d:%02d".format(min, sec)
        }
        return "${format(progressMs)} / ${format(durationMs)}"
    }
}