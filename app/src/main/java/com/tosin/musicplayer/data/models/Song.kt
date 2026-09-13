package com.tosin.musicplayer.data.models

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String?,
    val folder: String?,
    val folderPath: String? = null,
    val uri: String,
    val albumArt: String?,
    val duration: Long,
    val lyrics: String? = null,
    val trackNumber: Int = 0,
    val year: Int? = null,
    val dateAddedMs: Long? = null,
    val fileSizeBytes: Long? = null,
    val rating: Int? = null,
    val playCount: Int = 0,
    val lastPlayedMs: Long? = null,
    val volumeName: String? = null
)

data class Playlist(
    val id: String,
    val name: String,
    val songIds: List<Long> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Returns true if the song matches the given search query.
 * Matches against title, artist, album, genre, and folder (case-insensitive).
 */
fun Song.matchesSearch(query: String): Boolean {
    if (query.isBlank()) return true
    val q = query.lowercase()
    return title.lowercase().contains(q) ||
           artist.lowercase().contains(q) ||
           album.lowercase().contains(q) ||
           (genre?.lowercase()?.contains(q) == true) ||
           (folder?.lowercase()?.contains(q) == true)
}
