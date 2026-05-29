package com.tosin.musicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tosin.musicplayer.ui.extensions.orDefaultAlbumArt
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel

@Composable
fun MiniPlayer(
    viewModel: PlayerViewModel,
    onExpand: () -> Unit,
    onStop: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val song = uiState.currentSong ?: return

    val dragThreshold = 72

    Surface(
        color = androidx.compose.ui.graphics.Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .pointerInput(Unit) {
                var dragDistance = 0f
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        dragDistance += dragAmount
                        change.consume()
                    },
                    onDragEnd = {
                        if (dragDistance < -dragThreshold) {
                            onExpand()
                        }
                        dragDistance = 0f
                    },
                    onDragCancel = { dragDistance = 0f }
                )
            }
            .clickable { onExpand() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppSpacing.medium, vertical = AppSpacing.small),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AppSpacing.medium, vertical = AppSpacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = song.albumArt.orDefaultAlbumArt(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.width(AppSpacing.medium))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { viewModel.previous() }) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous")
                }

                IconButton(
                    onClick = {
                        if (uiState.isPlaying) viewModel.pause() else viewModel.play()
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (uiState.isPlaying) "Pause" else "Play"
                    )
                }

                IconButton(onClick = { viewModel.next() }) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Next")
                }

                IconButton(
                    onClick = onStop,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Rounded.Stop, contentDescription = "Stop")
                }
            }
        }
    }
}
