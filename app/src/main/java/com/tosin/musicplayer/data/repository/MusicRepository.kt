package com.tosin.musicplayer.data.repository

import com.tosin.musicplayer.data.local.MusicLoader
import com.tosin.musicplayer.data.local.ScanProgress
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.data.models.SongMetadataOverride
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicRepository(
    private val musicLoader: MusicLoader,
    private val preferencesRepository: PreferencesRepository,
    private val statsRepository: StatsRepository
) {

    suspend fun deleteSong(songUriString: String): Boolean {
        return musicLoader.deleteSong(android.net.Uri.parse(songUriString))
    }

    suspend fun writeTags(songId: Long, title: String, artist: String, album: String): Boolean {
        return musicLoader.writeTags(songId, title, artist, album)
    }

    fun getSongs(): Flow<List<Song>> = channelFlow {
        val cachedSongs = loadPreparedSongsFromCache()
        val hasCachedSongs = cachedSongs.isNotEmpty()
        if (hasCachedSongs) {
            send(cachedSongs)
        }

        launch(Dispatchers.IO) {
            val freshSongs = refreshLibraryFromDevice()
            if (!hasCachedSongs || freshSongs != cachedSongs) {
                send(freshSongs)
            }
        }
    }

    suspend fun loadAllSongs(onProgress: suspend (ScanProgress) -> Unit = {}): List<Song> {
        val cachedSongs = loadPreparedSongsFromCache()
        if (cachedSongs.isNotEmpty()) return cachedSongs
        return refreshLibraryFromDevice(onProgress)
    }

    /**
     * Scan for changes in the music library (new/removed songs).
     */
    suspend fun scanForChanges(onProgress: suspend (ScanProgress) -> Unit = {}) {
        refreshLibraryFromDevice(onProgress)
    }

    /**
     * Perform a full re-scan of the music library.
     */
    suspend fun fullScan(onProgress: suspend (ScanProgress) -> Unit = {}) {
        preferencesRepository.clearSongCache()
        refreshLibraryFromDevice(onProgress)
    }

    private suspend fun loadPreparedSongsFromCache(): List<Song> {
        val cachedSongs = preferencesRepository.loadSongCache()
        return if (cachedSongs.isNotEmpty()) {
            prepareSongs(cachedSongs)
        } else {
            emptyList()
        }
    }

    private suspend fun refreshLibraryFromDevice(onProgress: suspend (ScanProgress) -> Unit = {}): List<Song> {
        val songs = prepareSongs(musicLoader.loadSongs(onProgress))
        preferencesRepository.saveSongCache(songs)
        return songs
    }

    private suspend fun prepareSongs(rawSongs: List<Song>): List<Song> {
        val settings = preferencesRepository.loadSettings()
        val excludedFolders = settings.stringList("excludedFolders", emptyList()).toSet()
        val metadataOverrides = preferencesRepository.loadSongMetadataOverrides()
        val playbackMetrics = statsRepository.loadPlaybackMetrics()

        return rawSongs
            .asSequence()
            .filterNot { it.isExcluded(excludedFolders) }
            .map { song ->
                val override = metadataOverrides[song.id]
                val metrics = playbackMetrics[song.id]
                song.applyMetadataOverride(override)
                    .applyPlaybackMetrics(metrics)
            }
            .sortedWith(songComparator())
            .toList()
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

    private fun Song.applyPlaybackMetrics(metrics: PlaybackMetrics?): Song {
        if (metrics == null) return this
        return copy(
            playCount = metrics.playCount,
            lastPlayedMs = metrics.lastPlayedMs
        )
    }

    private fun songComparator(): Comparator<Song> {
        return compareBy(
            { it.title.trim().lowercase() },
            { it.artist.trim().lowercase() },
            { it.album.trim().lowercase() }
        )
    }

    private fun normalizePath(path: String): String {
        return path.trim().trimEnd('/')
    }
}
