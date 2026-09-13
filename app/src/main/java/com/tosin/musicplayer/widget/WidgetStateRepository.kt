package com.tosin.musicplayer.widget

import android.content.Context
import androidx.core.util.AtomicFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class WidgetStateRepository(private val context: Context) {
    private val mutex = Mutex()
    private val stateFile = File(context.filesDir, WidgetConstants.PREFS_WIDGET_STATE)
    private val artFile = File(context.filesDir, WidgetConstants.PREFS_WIDGET_BITMAP)

    suspend fun saveState(state: WidgetState) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val json = JSONObject().apply {
                put("title", state.title)
                put("artist", state.artist)
                put("album", state.album)
                put("isPlaying", state.isPlaying)
                put("isShuffleEnabled", state.isShuffleEnabled)
                put("repeatMode", state.repeatMode)
                put("progressMs", state.progressMs)
                put("durationMs", state.durationMs)
                put("albumArtUri", state.albumArtUri)
                put("isEmptyQueue", state.isEmptyQueue)
                put("isOffline", state.isOffline)
                put("lastUpdatedMs", state.lastUpdatedMs)
            }
            val atomicFile = AtomicFile(stateFile)
            var fos: FileOutputStream? = null
            try {
                fos = atomicFile.startWrite()
                fos.write(json.toString().toByteArray(Charsets.UTF_8))
                atomicFile.finishWrite(fos)
            } catch (e: Exception) {
                if (fos != null) atomicFile.failWrite(fos)
            }
        }
    }

    suspend fun loadState(): WidgetState = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!stateFile.exists()) return@withLock WidgetState()
            runCatching {
                val json = JSONObject(stateFile.readText())
                WidgetState(
                    title = json.optString("title"),
                    artist = json.optString("artist"),
                    album = json.optString("album"),
                    isPlaying = json.optBoolean("isPlaying", false),
                    isShuffleEnabled = json.optBoolean("isShuffleEnabled", false),
                    repeatMode = json.optString("repeatMode", "PLAY_ALL_ONCE"),
                    progressMs = json.optLong("progressMs", 0L),
                    durationMs = json.optLong("durationMs", 0L),
                    albumArtUri = json.optString("albumArtUri").takeIf { it.isNotBlank() },
                    isEmptyQueue = json.optBoolean("isEmptyQueue", true),
                    isOffline = json.optBoolean("isOffline", false),
                    lastUpdatedMs = json.optLong("lastUpdatedMs", System.currentTimeMillis())
                )
            }.getOrElse { WidgetState() }
        }
    }

    suspend fun saveArtwork(bytes: ByteArray) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val atomicFile = AtomicFile(artFile)
            var fos: FileOutputStream? = null
            try {
                fos = atomicFile.startWrite()
                fos.write(bytes)
                atomicFile.finishWrite(fos)
            } catch (e: Exception) {
                if (fos != null) atomicFile.failWrite(fos)
            }
        }
    }

    suspend fun loadArtworkBytes(): ByteArray? = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!artFile.exists()) null else runCatching { artFile.readBytes() }.getOrNull()
        }
    }

    suspend fun clearArtwork() = withContext(Dispatchers.IO) {
        mutex.withLock {
            runCatching {
                if (artFile.exists()) artFile.delete()
            }
        }
    }
}