package com.tosin.musicplayer.data.models

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val uri: String,
    val albumArt: String?,
    val duration: Long
)