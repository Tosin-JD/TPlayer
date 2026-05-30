package com.tosin.musicplayer.data.local

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.tosin.musicplayer.data.models.Song
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicLoader(
    private val contentResolver: ContentResolver
) {

    private companion object {
        const val COLUMN_DATE_ADDED = "date_added"
        const val COLUMN_SIZE = "_size"
        const val COLUMN_YEAR = "year"
        const val COLUMN_TRACK = "track"
    }

    suspend fun loadSongs(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val folderColumnName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.RELATIVE_PATH
        } else {
            MediaStore.Audio.Media.DATA
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            COLUMN_DATE_ADDED,
            COLUMN_SIZE,
            COLUMN_YEAR,
            COLUMN_TRACK,
            folderColumnName
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        runCatching {
            contentResolver.query(
                uri,
                projection,
                selection,
                null,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )
        }.getOrNull()?.use { cursor ->

            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val dateAddedColumn = cursor.getColumnIndex(COLUMN_DATE_ADDED)
            val sizeColumn = cursor.getColumnIndex(COLUMN_SIZE)
            val yearColumn = cursor.getColumnIndex(COLUMN_YEAR)
            val trackColumn = cursor.getColumnIndex(COLUMN_TRACK)
            val folderColumn = cursor.getColumnIndex(folderColumnName)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn).orEmpty()
                val artist = cursor.getString(artistColumn).orEmpty()
                val album = cursor.getString(albumColumn).orEmpty()
                val duration = cursor.getLong(durationColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val folder = if (folderColumn >= 0) cursor.getString(folderColumn) else null
                val dateAddedSeconds = if (dateAddedColumn >= 0 && !cursor.isNull(dateAddedColumn)) {
                    cursor.getLong(dateAddedColumn)
                } else null
                val sizeBytes = if (sizeColumn >= 0 && !cursor.isNull(sizeColumn)) {
                    cursor.getLong(sizeColumn)
                } else null
                val year = if (yearColumn >= 0 && !cursor.isNull(yearColumn)) {
                    cursor.getInt(yearColumn).takeIf { it > 0 }
                } else null
                val trackNumber = if (trackColumn >= 0 && !cursor.isNull(trackColumn)) {
                    cursor.getInt(trackColumn).takeIf { it > 0 } ?: 0
                } else 0
                val rating = null

                val contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    .buildUpon()
                    .appendPath(id.toString())
                    .build()

                val albumArtUri = Uri.parse("content://media/external/audio/albumart/$albumId")
                // Check if album art actually exists for this album
                val hasAlbumArt = try {
                    contentResolver.openAssetFileDescriptor(albumArtUri, "r")?.use { true } ?: false
                } catch (e: Exception) {
                    false
                }

                songs.add(
                    Song(
                        id = id,
                        title = title.ifBlank { "Unknown title" },
                        artist = artist.ifBlank { "Unknown artist" },
                        album = album.ifBlank { "Unknown album" },
                        genre = loadGenreForSong(id),
                        folder = extractFolderName(folder),
                        folderPath = normalizeFolderPath(folder),
                        uri = contentUri.toString(),
                        albumArt = if (hasAlbumArt) albumArtUri.toString() else null,
                        duration = duration,
                        lyrics = null,
                        trackNumber = trackNumber,
                        year = year,
                        dateAddedMs = dateAddedSeconds?.times(1000L),
                        fileSizeBytes = sizeBytes,
                        rating = rating
                    )
                )
            }
        }

        songs
    }

    private fun loadGenreForSong(audioId: Long): String? {
        val genreUri = MediaStore.Audio.Genres.getContentUriForAudioId("external", audioId.toInt())
        return runCatching {
            contentResolver.query(
                genreUri,
                arrayOf(MediaStore.Audio.Genres.NAME),
                null,
                null,
                null
            )
        }.getOrNull()?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }?.takeIf { it.isNullOrBlank().not() }
    }

    private fun extractFolderName(rawPath: String?): String? {
        val normalizedPath = rawPath
            ?.trim()
            ?.trimEnd('/')
            ?.takeIf { it.isNotBlank() }
            ?: return null

        return File(normalizedPath).name.takeIf { it.isNotBlank() }
    }

    private fun normalizeFolderPath(rawPath: String?): String? {
        return rawPath
            ?.trim()
            ?.trimEnd('/')
            ?.takeIf { it.isNotBlank() }
    }
}
