package com.tosin.musicplayer.data.models

data class PlayEvent(
    val songId: Long,
    val timestamp: Long, // Epoch millis
    val durationMs: Long
)

data class SongStats(
    val song: Song,
    val playCount: Int,
    val totalMinutes: Long
)
