package com.tosin.musicplayer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.ui.extensions.orDefaultAlbumArt
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.theme.engine.customAppSurface

@Composable
fun SongItem(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val containerColor by animateColorAsState(
        targetValue = if (isPlaying) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        label = "song-item-container"
    )

    // Use Box + customAppSurface instead of Surface to avoid double-rendering
    // customAppSurface handles: background fill, clip, border, shadow, glow
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .customAppSurface(
                shape = MaterialTheme.shapes.large,
                backgroundColor = containerColor
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onLongClick?.invoke() }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.cardPadding, vertical = AppSpacing.medium),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.albumArt.orDefaultAlbumArt(),
                contentDescription = null,
                modifier = Modifier
                    .size(58.dp)
                    .clip(MaterialTheme.shapes.medium)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
                Text(
                    text = "${song.artist} • ${song.album}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
            }

            if (trailingContent != null) {
                trailingContent()
            } else if (isPlaying) {
                AssistChip(
                    onClick = onClick,
                    label = { Text("Playing") }
                )
            }
        }
    }
}
