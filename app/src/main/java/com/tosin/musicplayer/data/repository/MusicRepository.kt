package com.tosin.musicplayer.data.repository

import com.tosin.musicplayer.data.local.MusicLoader
import com.tosin.musicplayer.data.models.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MusicRepository(
    private val musicLoader: MusicLoader
) {

    fun getSongs(): Flow<List<Song>> = flow {
        emit(
            musicLoader.loadSongs()
                .sortedBy { it.title.trim().lowercase() }
        )
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
}
