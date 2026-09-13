package com.tosin.musicplayer.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.tosin.musicplayer.data.repository.PreferencesRepository
import com.tosin.musicplayer.data.repository.StatsRepository
import com.tosin.musicplayer.player.PlaybackService
import com.tosin.musicplayer.player.PlayerController
import com.tosin.musicplayer.ui.viewmodel.RepeatMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class WidgetActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        val action = intent.action ?: return
        if (action == WidgetConstants.ACTION_WIDGET_OPEN_APP) {
            val launchIntent = appContext.packageManager.getLaunchIntentForPackage(appContext.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            if (launchIntent != null) appContext.startActivity(launchIntent)
            return
        }

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.Main).launch {
            var controller: PlayerController? = null
            try {
                // Ensure service is running so MediaController can bind
                val serviceIntent = Intent(appContext, PlaybackService::class.java)
                runCatching {
                    ContextCompat.startForegroundService(appContext, serviceIntent)
                }

                controller = PlayerController(
                    context = appContext,
                    statsRepository = StatsRepository(appContext),
                    preferencesRepository = PreferencesRepository(appContext)
                )

                // Wait up to 3 seconds for connection
                val connected = withTimeoutOrNull(3000L) {
                    controller.awaitConnection()
                    true
                } ?: false

                if (connected) {
                    val isServiceActive = controller.isServiceActive()

                    when (action) {
                        WidgetConstants.ACTION_WIDGET_PLAY -> {
                            if (isServiceActive) {
                                controller.play()
                            } else {
                                controller.restoreSavedQueue(startPlaying = true)
                            }
                        }
                        WidgetConstants.ACTION_WIDGET_PAUSE -> {
                            if (isServiceActive) controller.pause()
                        }
                        WidgetConstants.ACTION_WIDGET_STOP -> {
                            if (isServiceActive) controller.stop()
                        }
                        WidgetConstants.ACTION_WIDGET_NEXT -> {
                            if (isServiceActive) {
                                controller.next()
                            } else {
                                val restored = controller.restoreSavedQueue(startPlaying = false)
                                if (restored) controller.next()
                            }
                        }
                        WidgetConstants.ACTION_WIDGET_PREV -> {
                            if (isServiceActive) {
                                controller.previous()
                            } else {
                                val restored = controller.restoreSavedQueue(startPlaying = false)
                                if (restored) controller.previous()
                            }
                        }
                        WidgetConstants.ACTION_WIDGET_SHUFFLE -> {
                            val isEnabled = intent.getBooleanExtra(WidgetConstants.EXTRA_SHUFFLE_ENABLED, false)
                            controller.setShuffleEnabled(!isEnabled)
                        }
                        WidgetConstants.ACTION_WIDGET_REPEAT -> {
                            val currentModeName = intent.getStringExtra(WidgetConstants.EXTRA_REPEAT_MODE)
                                ?: RepeatMode.PLAY_ALL_ONCE.name
                            val currentMode = runCatching { RepeatMode.valueOf(currentModeName) }
                                .getOrDefault(RepeatMode.PLAY_ALL_ONCE)
                            val nextMode = when (currentMode) {
                                RepeatMode.PLAY_ALL_ONCE, RepeatMode.PLAY_ONE_ONCE -> RepeatMode.REPEAT_ALL
                                RepeatMode.REPEAT_ALL -> RepeatMode.REPEAT_ONE
                                RepeatMode.REPEAT_ONE -> RepeatMode.PLAY_ALL_ONCE
                            }
                            controller.setRepeatMode(nextMode)
                        }
                    }
                }

                WidgetUpdateDispatcher.updateAll(appContext)
            } finally {
                controller?.release()
                pendingResult.finish()
            }
        }
    }
}