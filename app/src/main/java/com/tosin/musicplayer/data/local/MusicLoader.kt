package com.tosin.musicplayer.data.local

import android.content.ContentResolver
import android.provider.MediaStore
import com.tosin.musicplayer.data.models.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicLoader(
    private val contentResolver: ContentResolver
) {

    suspend fun loadSongs(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        contentResolver.query(
            uri,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )?.use { cursor ->

            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val duration = cursor.getLong(durationColumn)
                val albumId = cursor.getLong(albumIdColumn)

                val contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    .buildUpon()
                    .appendPath(id.toString())
                    .build()

                val albumArtUri = "content://media/external/audio/albumart/$albumId"

                songs.add(
                    Song(
                        id = id,
                        title = title,
                        artist = artist,
                        uri = contentUri.toString(),
                        albumArt = albumArtUri,
                        duration = duration
                    )
                )
            }
        }

        songs
    }
}