package com.tosin.musicplayer.data.models

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String?,
    val folder: String?,
    val uri: String,
    val albumArt: String?,
    val duration: Long,
    val lyrics: String? = null,
    val trackNumber: Int = 0
)

data class Playlist(
    val id: String,
    val name: String,
    val songIds: List<Long> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
