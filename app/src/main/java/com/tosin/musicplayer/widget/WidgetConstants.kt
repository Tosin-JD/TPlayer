package com.tosin.musicplayer.widget

object WidgetConstants {
    const val ACTION_WIDGET_UPDATE = "com.tosin.musicplayer.widget.ACTION_UPDATE"
    const val ACTION_WIDGET_PLAY = "com.tosin.musicplayer.widget.ACTION_PLAY"
    const val ACTION_WIDGET_PAUSE = "com.tosin.musicplayer.widget.ACTION_PAUSE"
    const val ACTION_WIDGET_STOP = "com.tosin.musicplayer.widget.ACTION_STOP"
    const val ACTION_WIDGET_NEXT = "com.tosin.musicplayer.widget.ACTION_NEXT"
    const val ACTION_WIDGET_PREV = "com.tosin.musicplayer.widget.ACTION_PREV"
    const val ACTION_WIDGET_SHUFFLE = "com.tosin.musicplayer.widget.ACTION_SHUFFLE"
    const val ACTION_WIDGET_REPEAT = "com.tosin.musicplayer.widget.ACTION_REPEAT"
    const val ACTION_WIDGET_OPEN_APP = "com.tosin.musicplayer.widget.ACTION_OPEN_APP"

    const val EXTRA_SHUFFLE_ENABLED = "extra_shuffle_enabled"
    const val EXTRA_REPEAT_MODE = "extra_repeat_mode"
    const val EXTRA_WIDGET_IDS = "widget_ids"

    const val PREFS_WIDGET_STATE = "widget_state.json"
    const val PREFS_WIDGET_BITMAP = "widget_artwork.png"
}
