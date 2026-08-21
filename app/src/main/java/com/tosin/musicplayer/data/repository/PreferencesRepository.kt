package com.tosin.musicplayer.data.repository

import android.content.Context
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.data.models.SongMetadataOverride
import com.tosin.musicplayer.ui.state.EqBand
import com.tosin.musicplayer.ui.state.EqualizerUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Persists settings and queue state across app restarts.
 */
class PreferencesRepository(private val context: Context) {
    private val prefsFile = File(context.filesDir, "tplayer_prefs.json")
    private val queueFile = File(context.filesDir, "queue_state.json")
    private val metadataFile = File(context.filesDir, "song_metadata_overrides.json")
    private val songsCacheFile = File(context.filesDir, "song_cache.json")
    private val equalizerStateFile = File(context.filesDir, "equalizer_state.json")
    private val lyricsAppearanceFile = File(context.filesDir, "lyrics_appearance.json")

    // --- Settings ---
    suspend fun saveSettings(settings: Map<String, Any>) = withContext(Dispatchers.IO) {
        try {
            val obj = JSONObject()
            settings.forEach { (key, value) ->
                when (value) {
                    is Collection<*> -> {
                        val arr = JSONArray()
                        value.forEach { arr.put(it) }
                        obj.put(key, arr)
                    }
                    is Array<*> -> {
                        val arr = JSONArray()
                        value.forEach { arr.put(it) }
                        obj.put(key, arr)
                    }
                    else -> obj.put(key, value)
                }
            }
            prefsFile.writeText(obj.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun loadSettings(): Map<String, Any> = withContext(Dispatchers.IO) {
        if (!prefsFile.exists()) return@withContext emptyMap()
        try {
            val obj = JSONObject(prefsFile.readText())
            val map = mutableMapOf<String, Any>()
            obj.keys().forEach { key ->
                map[key] = when (val value = obj.get(key)) {
                    is JSONArray -> List(value.length()) { index -> value.get(index) }
                    else -> value
                }
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    // --- Navigation & Sort State Persistence ---
    suspend fun saveNavigationState(route: String, tab: String) = withContext(Dispatchers.IO) {
        try {
            val current = loadSettings().toMutableMap()
            current["lastClosedRoute"] = route
            current["lastClosedLibraryTab"] = tab
            saveSettings(current)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun loadNavigationState(): Pair<String, String> = withContext(Dispatchers.IO) {
        val settings = loadSettings()
        val route = (settings["lastClosedRoute"] as? String) ?: "home"
        val tab = (settings["lastClosedLibraryTab"] as? String) ?: "All"
        Pair(route, tab)
    }

    suspend fun saveTabSortOptions(sortMap: Map<String, String>) = withContext(Dispatchers.IO) {
        try {
            val current = loadSettings().toMutableMap()
            val obj = JSONObject()
            sortMap.forEach { (k, v) -> obj.put(k, v) }
            current["perTabSortOptions"] = obj.toString()
            saveSettings(current)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun loadTabSortOptions(): Map<String, String> = withContext(Dispatchers.IO) {
        val settings = loadSettings()
        val rawJson = settings["perTabSortOptions"] as? String ?: return@withContext emptyMap()
        try {
            val obj = JSONObject(rawJson)
            val map = mutableMapOf<String, String>()
            obj.keys().forEach { key ->
                map[key] = obj.getString(key)
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    // --- Queue Persistence ---
    suspend fun saveQueueState(
        songIds: List<Long>,
        currentIndex: Int,
        positionMs: Long,
        wasPlaying: Boolean
    ) = withContext(Dispatchers.IO) {
        try {
            val obj = JSONObject()
            obj.put("songIds", JSONArray(songIds))
            obj.put("currentIndex", currentIndex)
            obj.put("positionMs", positionMs)
            obj.put("wasPlaying", wasPlaying)
            queueFile.writeText(obj.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun loadQueueState(): QueueState? = withContext(Dispatchers.IO) {
        if (!queueFile.exists()) return@withContext null
        try {
            val obj = JSONObject(queueFile.readText())
            val songIdsArray = obj.getJSONArray("songIds")
            val songIds = List(songIdsArray.length()) { i -> songIdsArray.getLong(i) }
            QueueState(
                songIds = songIds,
                currentIndex = obj.getInt("currentIndex"),
                positionMs = obj.getLong("positionMs"),
                wasPlaying = obj.optBoolean("wasPlaying", true)
            )
        } catch (e: Exception) {
            null
        }
    }

    // --- Resume Position per Track ---
    suspend fun saveResumePosition(songId: Long, positionMs: Long) = withContext(Dispatchers.IO) {
        try {
            val positions = loadResumePositions().toMutableMap()
            positions[songId] = positionMs
            // Keep only 500 most recent
            val trimmed = if (positions.size > 500) {
                positions.entries.toList().takeLast(500).associate { it.key to it.value }
            } else positions
            val positionsFile = File(context.filesDir, "resume_positions.json")
            val obj = JSONObject()
            trimmed.forEach { (id, pos) ->
                obj.put(id.toString(), pos)
            }
            positionsFile.writeText(obj.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getResumePosition(songId: Long): Long = withContext(Dispatchers.IO) {
        loadResumePositions()[songId] ?: 0L
    }

    // --- Per-song metadata overrides ---
    suspend fun loadSongMetadataOverrides(): Map<Long, SongMetadataOverride> = withContext(Dispatchers.IO) {
        if (!metadataFile.exists()) return@withContext emptyMap()
        try {
            val obj = JSONObject(metadataFile.readText())
            val map = mutableMapOf<Long, SongMetadataOverride>()
            obj.keys().forEach { key ->
                val songId = key.toLongOrNull() ?: return@forEach
                val value = obj.getJSONObject(key)
                map[songId] = SongMetadataOverride(
                    title = value.optString("title").takeIf { it.isNotBlank() },
                    artist = value.optString("artist").takeIf { it.isNotBlank() },
                    album = value.optString("album").takeIf { it.isNotBlank() },
                    genre = value.optString("genre").takeIf { it.isNotBlank() },
                    lyrics = value.optString("lyrics").takeIf { it.isNotBlank() }
                )
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun saveSongMetadataOverride(
        songId: Long,
        override: SongMetadataOverride
    ) = withContext(Dispatchers.IO) {
        try {
            val current = loadSongMetadataOverrides().toMutableMap()
            if (
                override.title.isNullOrBlank() &&
                override.artist.isNullOrBlank() &&
                override.album.isNullOrBlank() &&
                override.genre.isNullOrBlank() &&
                override.lyrics.isNullOrBlank()
            ) {
                current.remove(songId)
            } else {
                current[songId] = override
            }
            saveSongMetadataOverrides(current)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveSongMetadataOverrides(overrides: Map<Long, SongMetadataOverride>) {
        val obj = JSONObject()
        overrides.forEach { (songId, override) ->
            val value = JSONObject()
            override.title?.let { value.put("title", it) }
            override.artist?.let { value.put("artist", it) }
            override.album?.let { value.put("album", it) }
            override.genre?.let { value.put("genre", it) }
            override.lyrics?.let { value.put("lyrics", it) }
            obj.put(songId.toString(), value)
        }
        metadataFile.writeText(obj.toString())
    }

    suspend fun saveSongCache(songs: List<Song>) = withContext(Dispatchers.IO) {
        try {
            val jsonArray = JSONArray()
            songs.forEach { song ->
                jsonArray.put(song.toJson())
            }
            songsCacheFile.writeText(jsonArray.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun clearSongCache() = withContext(Dispatchers.IO) {
        try {
            if (songsCacheFile.exists()) {
                songsCacheFile.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun loadSongCache(): List<Song> = withContext(Dispatchers.IO) {
        if (!songsCacheFile.exists()) return@withContext emptyList()
        runCatching {
            val jsonArray = JSONArray(songsCacheFile.readText())
            List(jsonArray.length()) { index ->
                jsonArray.getJSONObject(index).toSong()
            }
        }.getOrDefault(emptyList())
    }

    // --- Equalizer State ---
    suspend fun saveEqualizerState(state: EqualizerUiState) = withContext(Dispatchers.IO) {
        try {
            val obj = JSONObject()
            obj.put("enabled", state.enabled)
            obj.put("selectedPresetId", state.selectedPresetId)
            obj.put("selectedPresetName", state.selectedPresetName)
            obj.put("selectedPresetDescription", state.selectedPresetDescription)
            obj.put("bassBoost", state.bassBoost)
            obj.put("virtualizer", state.virtualizer)
            obj.put("loudness", state.loudness)
            val bandsArray = JSONArray()
            state.bands.forEach { band ->
                bandsArray.put(JSONObject().apply {
                    put("id", band.id)
                    put("level", band.level)
                })
            }
            obj.put("bands", bandsArray)
            equalizerStateFile.writeText(obj.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun loadEqualizerState(): EqualizerUiState? = withContext(Dispatchers.IO) {
        if (!equalizerStateFile.exists()) return@withContext null
        try {
            val obj = JSONObject(equalizerStateFile.readText())
            val bandsArray = obj.optJSONArray("bands")
            val bands = if (bandsArray == null) {
                emptyList()
            } else {
                List(bandsArray.length()) { i ->
                    val band = bandsArray.getJSONObject(i)
                    EqBand(
                        id = band.getInt("id"),
                        frequency = 0,
                        level = band.getInt("level"),
                        minLevel = 0,
                        maxLevel = 0
                    )
                }
            }
            EqualizerUiState(
                enabled = obj.optBoolean("enabled", false),
                bands = bands,
                selectedPresetId = obj.optString("selectedPresetId", "flat"),
                selectedPresetName = obj.optString("selectedPresetName", "Flat"),
                selectedPresetDescription = obj.optString(
                    "selectedPresetDescription",
                    "Balanced sound with no coloration."
                ),
                bassBoost = obj.optInt("bassBoost", 0),
                virtualizer = obj.optInt("virtualizer", 0),
                loudness = obj.optInt("loudness", 0)
            )
        } catch (e: Exception) {
            null
        }
    }

    // --- Lyrics Appearance ---
    suspend fun saveLyricsAppearance(
        fontSize: String,
        textAlign: String,
        fontFamily: String
    ) = withContext(Dispatchers.IO) {
        try {
            val obj = JSONObject()
            obj.put("fontSize", fontSize)
            obj.put("textAlign", textAlign)
            obj.put("fontFamily", fontFamily)
            lyricsAppearanceFile.writeText(obj.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun loadLyricsAppearance(): LyricsAppearance? = withContext(Dispatchers.IO) {
        if (!lyricsAppearanceFile.exists()) return@withContext null
        try {
            val obj = JSONObject(lyricsAppearanceFile.readText())
            LyricsAppearance(
                fontSize = obj.optString("fontSize", "Medium"),
                textAlign = obj.optString("textAlign", "Center"),
                fontFamily = obj.optString("fontFamily", "SansSerif")
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun loadResumePositions(): Map<Long, Long> {
        val positionsFile = File(context.filesDir, "resume_positions.json")
        if (!positionsFile.exists()) return emptyMap()
        return try {
            val obj = JSONObject(positionsFile.readText())
            val map = mutableMapOf<Long, Long>()
            obj.keys().forEach { key ->
                map[key.toLong()] = obj.getLong(key)
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun Song.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("artist", artist)
        put("album", album)
        put("genre", genre)
        put("folder", folder)
        put("folderPath", folderPath)
        put("uri", uri)
        put("albumArt", albumArt)
        put("duration", duration)
        put("lyrics", lyrics)
        put("trackNumber", trackNumber)
        put("year", year)
        put("dateAddedMs", dateAddedMs)
        put("fileSizeBytes", fileSizeBytes)
        put("rating", rating)
        put("playCount", playCount)
        put("lastPlayedMs", lastPlayedMs)
    }

    private fun JSONObject.toSong(): Song = Song(
        id = getLong("id"),
        title = optString("title"),
        artist = optString("artist"),
        album = optString("album"),
        genre = optString("genre").takeIf { it.isNotBlank() },
        folder = optString("folder").takeIf { it.isNotBlank() },
        folderPath = optString("folderPath").takeIf { it.isNotBlank() },
        uri = optString("uri"),
        albumArt = optString("albumArt").takeIf { it.isNotBlank() },
        duration = getLong("duration"),
        lyrics = optString("lyrics").takeIf { it.isNotBlank() },
        trackNumber = optInt("trackNumber", 0),
        year = optIntOrNull("year"),
        dateAddedMs = optLongOrNull("dateAddedMs"),
        fileSizeBytes = optLongOrNull("fileSizeBytes"),
        rating = optIntOrNull("rating"),
        playCount = optInt("playCount", 0),
        lastPlayedMs = optLongOrNull("lastPlayedMs")
    )

    private fun JSONObject.optLongOrNull(key: String): Long? =
        if (has(key) && !isNull(key)) getLong(key) else null

    private fun JSONObject.optIntOrNull(key: String): Int? =
        if (has(key) && !isNull(key)) getInt(key) else null

    private fun JSONObject.optBoolean(key: String, default: Boolean): Boolean =
        if (has(key) && !isNull(key)) getBoolean(key) else default
}

data class QueueState(
    val songIds: List<Long>,
    val currentIndex: Int,
    val positionMs: Long,
    val wasPlaying: Boolean
)

data class LyricsAppearance(
    val fontSize: String,
    val textAlign: String,
    val fontFamily: String
)
