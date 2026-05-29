package com.tosin.musicplayer.data.repository

import android.content.Context
import com.tosin.musicplayer.data.local.MusicLoader
import com.tosin.musicplayer.data.models.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MusicRepository(
    private val context: Context,
    private val musicLoader: MusicLoader,
    private val preferencesRepository: PreferencesRepository
) {
    private val cacheFile = File(context.filesDir, "cached_songs.json")
    private val _songsFlow = MutableStateFlow<List<Song>>(emptyList())
    val songsFlow: StateFlow<List<Song>> = _songsFlow.asStateFlow()

    init {
        // Load initially from cache
        _songsFlow.value = loadSongsFromCache()
    }

    fun getSongs(): Flow<List<Song>> {
        return songsFlow
    }

    private fun loadSongsFromCache(): List<Song> {
        if (!cacheFile.exists()) return emptyList()
        return try {
            val jsonArray = JSONArray(cacheFile.readText())
            val list = mutableListOf<Song>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    Song(
                        id = obj.getLong("id"),
                        title = obj.getString("title"),
                        artist = obj.getString("artist"),
                        album = obj.getString("album"),
                        genre = if (obj.isNull("genre")) null else obj.getString("genre"),
                        folder = if (obj.isNull("folder")) null else obj.getString("folder"),
                        uri = obj.getString("uri"),
                        albumArt = if (obj.isNull("albumArt")) null else obj.getString("albumArt"),
                        duration = obj.getLong("duration"),
                        lyrics = if (obj.isNull("lyrics")) null else obj.getString("lyrics"),
                        trackNumber = obj.optInt("trackNumber", 0)
                    )
                )
            }
            list.sortedBy { it.title.trim().lowercase() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun saveSongsToCache(songs: List<Song>) {
        try {
            val jsonArray = JSONArray()
            songs.forEach { song ->
                val obj = JSONObject()
                obj.put("id", song.id)
                obj.put("title", song.title)
                obj.put("artist", song.artist)
                obj.put("album", song.album)
                obj.put("genre", song.genre ?: JSONObject.NULL)
                obj.put("folder", song.folder ?: JSONObject.NULL)
                obj.put("uri", song.uri)
                obj.put("albumArt", song.albumArt ?: JSONObject.NULL)
                obj.put("duration", song.duration)
                obj.put("lyrics", song.lyrics ?: JSONObject.NULL)
                obj.put("trackNumber", song.trackNumber)
                jsonArray.put(obj)
            }
            cacheFile.writeText(jsonArray.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun scanForChanges() = withContext(Dispatchers.IO) {
        val currentSongs = musicLoader.loadSongs().sortedBy { it.title.trim().lowercase() }
        val cachedSongs = _songsFlow.value
        
        // Update cache and flow only if different
        if (currentSongs != cachedSongs) {
            saveSongsToCache(currentSongs)
            _songsFlow.value = currentSongs
        }
        updateLastScanDate()
    }

    suspend fun fullScan() = withContext(Dispatchers.IO) {
        if (cacheFile.exists()) {
            cacheFile.delete()
        }
        val currentSongs = musicLoader.loadSongs().sortedBy { it.title.trim().lowercase() }
        saveSongsToCache(currentSongs)
        _songsFlow.value = currentSongs
        updateLastScanDate()
    }

    private suspend fun updateLastScanDate() {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
        val dateStr = dateFormat.format(Date())
        val settings = preferencesRepository.loadSettings().toMutableMap()
        settings["lastScanDate"] = dateStr
        preferencesRepository.saveSettings(settings)
    }
}
