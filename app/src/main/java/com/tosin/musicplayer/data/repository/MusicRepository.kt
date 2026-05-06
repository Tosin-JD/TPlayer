package com.tosin.musicplayer.data.repository

import com.tosin.musicplayer.data.local.MusicLoader
import com.tosin.musicplayer.data.models.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MusicRepository(
    private val musicLoader: MusicLoader
) {

    fun getSongs(): Flow<List<Song>> = flow {
        val songs = musicLoader.loadSongs()
        emit(songs)
    }
}