package com.tosin.musicplayer.data.repository

import android.content.Context
import com.tosin.musicplayer.data.models.Playlist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class PlaylistRepository(private val context: Context) {
    private val playlistFile = File(context.filesDir, "playlists.json")
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    init {
        _playlists.value = loadPlaylistsSync()
    }

    suspend fun createPlaylist(name: String): Playlist = withContext(Dispatchers.IO) {
        val playlist = Playlist(
            id = UUID.randomUUID().toString(),
            name = name
        )
        val current = loadPlaylistsSync().toMutableList()
        current.add(playlist)
        savePlaylists(current)
        _playlists.value = current
        playlist
    }

    suspend fun deletePlaylist(playlistId: String) = withContext(Dispatchers.IO) {
        val current = loadPlaylistsSync().filter { it.id != playlistId }
        savePlaylists(current)
        _playlists.value = current
    }

    suspend fun renamePlaylist(playlistId: String, newName: String) = withContext(Dispatchers.IO) {
        val current = loadPlaylistsSync().map {
            if (it.id == playlistId) it.copy(name = newName, updatedAt = System.currentTimeMillis())
            else it
        }
        savePlaylists(current)
        _playlists.value = current
    }

    suspend fun addSongToPlaylist(playlistId: String, songId: Long) = withContext(Dispatchers.IO) {
        val current = loadPlaylistsSync().map {
            if (it.id == playlistId && !it.songIds.contains(songId)) {
                it.copy(songIds = it.songIds + songId, updatedAt = System.currentTimeMillis())
            } else it
        }
        savePlaylists(current)
        _playlists.value = current
    }

    suspend fun removeSongFromPlaylist(playlistId: String, songId: Long) = withContext(Dispatchers.IO) {
        val current = loadPlaylistsSync().map {
            if (it.id == playlistId) {
                it.copy(songIds = it.songIds - songId, updatedAt = System.currentTimeMillis())
            } else it
        }
        savePlaylists(current)
        _playlists.value = current
    }

    suspend fun getPlaylist(playlistId: String): Playlist? = withContext(Dispatchers.IO) {
        loadPlaylistsSync().find { it.id == playlistId }
    }

    suspend fun refreshPlaylists() = withContext(Dispatchers.IO) {
        _playlists.value = loadPlaylistsSync()
    }

    private fun loadPlaylistsSync(): List<Playlist> {
        if (!playlistFile.exists()) return emptyList()
        return try {
            val jsonArray = JSONArray(playlistFile.readText())
            List(jsonArray.length()) { i ->
                val obj = jsonArray.getJSONObject(i)
                val songIdsArray = obj.getJSONArray("songIds")
                val songIds = List(songIdsArray.length()) { j -> songIdsArray.getLong(j) }
                Playlist(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    songIds = songIds,
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun savePlaylists(playlists: List<Playlist>) {
        try {
            val jsonArray = JSONArray()
            playlists.forEach { playlist ->
                val obj = JSONObject()
                obj.put("id", playlist.id)
                obj.put("name", playlist.name)
                obj.put("songIds", JSONArray(playlist.songIds))
                obj.put("createdAt", playlist.createdAt)
                obj.put("updatedAt", playlist.updatedAt)
                jsonArray.put(obj)
            }
            playlistFile.writeText(jsonArray.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
