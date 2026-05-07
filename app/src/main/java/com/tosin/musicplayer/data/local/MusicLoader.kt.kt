package com.tosin.musicplayer.data.local

import android.content.ContentResolver
import android.os.Build
import android.provider.MediaStore
import com.tosin.musicplayer.data.models.Song
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicLoader(
    private val contentResolver: ContentResolver
) {

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
            val folderColumn = cursor.getColumnIndex(folderColumnName)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn).orEmpty()
                val artist = cursor.getString(artistColumn).orEmpty()
                val album = cursor.getString(albumColumn).orEmpty()
                val duration = cursor.getLong(durationColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val folder = if (folderColumn >= 0) cursor.getString(folderColumn) else null

                val contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    .buildUpon()
                    .appendPath(id.toString())
                    .build()

                val albumArtUri = "content://media/external/audio/albumart/$albumId"

                songs.add(
                    Song(
                        id = id,
                        title = title.ifBlank { "Unknown title" },
                        artist = artist.ifBlank { "Unknown artist" },
                        album = album.ifBlank { "Unknown album" },
                        genre = loadGenreForSong(id),
                        folder = extractFolderName(folder),
                        uri = contentUri.toString(),
                        albumArt = albumArtUri,
                        duration = duration
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
}
