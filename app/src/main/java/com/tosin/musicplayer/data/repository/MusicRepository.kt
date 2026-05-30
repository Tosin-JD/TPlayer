package com.tosin.musicplayer.data.repository

import com.tosin.musicplayer.data.local.MusicLoader
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.data.models.SongMetadataOverride
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MusicRepository(
    private val musicLoader: MusicLoader,
    private val preferencesRepository: PreferencesRepository
) {

    fun getSongs(): Flow<List<Song>> = flow {
        val settings = preferencesRepository.loadSettings()
        val excludedFolders = settings.stringList("excludedFolders", emptyList()).toSet()
        val metadataOverrides = preferencesRepository.loadSongMetadataOverrides()

        emit(
            musicLoader.loadSongs()
                .asSequence()
                .filterNot { it.isExcluded(excludedFolders) }
                .map { song -> song.applyMetadataOverride(metadataOverrides[song.id]) }
                .sortedBy { it.title.trim().lowercase() }
                .toList()
        )
    }

    suspend fun loadAllSongs(): List<Song> {
        val metadataOverrides = preferencesRepository.loadSongMetadataOverrides()
        return musicLoader.loadSongs()
            .asSequence()
            .map { song -> song.applyMetadataOverride(metadataOverrides[song.id]) }
            .sortedBy { it.title.trim().lowercase() }
            .toList()
    }

    /**
     * Scan for changes in the music library (new/removed songs).
     */
    suspend fun scanForChanges() {
        // Re-load songs to pick up any changes
        musicLoader.loadSongs()
    }

    /**
     * Perform a full re-scan of the music library.
     */
    suspend fun fullScan() {
        // Re-load all songs from scratch
        musicLoader.loadSongs()
    }

    private fun Map<String, Any>.stringList(key: String, default: List<String>): List<String> =
        (this[key] as? List<*>)?.mapNotNull { it as? String } ?: default

    private fun Song.isExcluded(excludedFolders: Set<String>): Boolean {
        val folderPathMatches = folderPath?.let { normalizePath(it) }?.let { it in excludedFolders } == true
        val folderNameMatches = folder?.let { it in excludedFolders } == true
        return folderPathMatches || folderNameMatches
    }

    private fun Song.applyMetadataOverride(override: SongMetadataOverride?): Song {
        if (override == null) return this
        return copy(
            title = override.title?.takeIf { it.isNotBlank() } ?: title,
            artist = override.artist?.takeIf { it.isNotBlank() } ?: artist,
            album = override.album?.takeIf { it.isNotBlank() } ?: album,
            genre = override.genre?.takeIf { it.isNotBlank() } ?: genre,
            lyrics = override.lyrics?.takeIf { it.isNotBlank() } ?: lyrics
        )
    }

    private fun normalizePath(path: String): String {
        return path.trim().trimEnd('/')
    }
}
