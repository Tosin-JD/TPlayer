package com.tosin.musicplayer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tosin.musicplayer.data.models.Song

@Composable
fun SongItem(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        if (isPlaying) MaterialTheme.colorScheme.secondary
        else MaterialTheme.colorScheme.background
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        AsyncImage(
            model = song.albumArt,
            contentDescription = null,
            modifier = Modifier.size(56.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(song.title, style = MaterialTheme.typography.titleMedium)
            Text(song.artist, style = MaterialTheme.typography.bodyMedium)
        }
    }
}