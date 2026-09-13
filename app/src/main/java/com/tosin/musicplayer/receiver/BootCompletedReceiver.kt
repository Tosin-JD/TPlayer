package com.tosin.musicplayer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tosin.musicplayer.widget.WidgetUpdateDispatcher

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            WidgetUpdateDispatcher.updateAll(context)
        }
    }
}
