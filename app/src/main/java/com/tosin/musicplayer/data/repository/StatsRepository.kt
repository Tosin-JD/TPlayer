package com.tosin.musicplayer.data.repository

import android.content.Context
import com.tosin.musicplayer.data.models.PlayEvent
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.data.models.SongStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*

class StatsRepository(private val context: Context) {
    private val statsFile = File(context.filesDir, "play_stats.json")

    suspend fun recordPlay(songId: Long, durationMs: Long) = withContext(Dispatchers.IO) {
        val events = loadEvents().toMutableList()
        events.add(PlayEvent(songId, System.currentTimeMillis(), durationMs))
        saveEvents(events)
    }

    suspend fun getMostPlayed(
        songs: List<Song>,
        startTime: Long = 0L
    ): List<SongStats> = withContext(Dispatchers.IO) {
        val events = loadEvents().filter { it.timestamp >= startTime }
        val songMap = songs.associateBy { it.id }

        events.groupBy { it.songId }
            .mapNotNull { (id, songEvents) ->
                val song = songMap[id] ?: return@mapNotNull null
                SongStats(
                    song = song,
                    playCount = songEvents.size,
                    totalMinutes = songEvents.sumOf { it.durationMs } / 60000
                )
            }
            .sortedByDescending { it.playCount }
    }

    private fun loadEvents(): List<PlayEvent> {
        if (!statsFile.exists()) return emptyList()
        return try {
            val jsonString = statsFile.readText()
            val jsonArray = JSONArray(jsonString)
            List(jsonArray.length()) { i ->
                val obj = jsonArray.getJSONObject(i)
                PlayEvent(
                    obj.getLong("songId"),
                    obj.getLong("timestamp"),
                    obj.getLong("durationMs")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveEvents(events: List<PlayEvent>) {
        try {
            val jsonArray = JSONArray()
            events.takeLast(10000).forEach { event -> // Keep last 10k events to avoid huge file
                val obj = JSONObject()
                obj.put("songId", event.songId)
                obj.put("timestamp", event.timestamp)
                obj.put("durationMs", event.durationMs)
                jsonArray.put(obj)
            }
            statsFile.writeText(jsonArray.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
