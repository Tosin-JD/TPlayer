package com.tosin.musicplayer.data.repository

import android.content.Context
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

    // --- Settings ---
    suspend fun saveSettings(settings: Map<String, Any>) = withContext(Dispatchers.IO) {
        try {
            val obj = JSONObject()
            settings.forEach { (key, value) ->
                obj.put(key, value)
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
                map[key] = obj.get(key)
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
        positionMs: Long
    ) = withContext(Dispatchers.IO) {
        try {
            val obj = JSONObject()
            obj.put("songIds", JSONArray(songIds))
            obj.put("currentIndex", currentIndex)
            obj.put("positionMs", positionMs)
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
                positionMs = obj.getLong("positionMs")
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
}

data class QueueState(
    val songIds: List<Long>,
    val currentIndex: Int,
    val positionMs: Long
)
