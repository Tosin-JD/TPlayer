package com.tosin.musicplayer.ui.screens


import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.tosin.musicplayer.ui.components.PlayPauseButton
import com.tosin.musicplayer.ui.components.ProgressBar
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import com.tosin.musicplayer.R
import com.tosin.musicplayer.ui.viewmodel.RepeatMode
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.luminance
import androidx.palette.graphics.Palette
import android.graphics.drawable.BitmapDrawable

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee

@Composable
public fun PlayerScreen(
    viewModel: PlayerViewModel,
    onOpenPlaylist: () -> Unit = {},
    onOpenLyrics: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    
    var bgColor by remember { mutableStateOf(surfaceColor) }
    var contentColor by remember { mutableStateOf(onSurfaceColor) }

    LaunchedEffect(state.currentSong?.albumArt) {
        val data = state.currentSong?.albumArt
        try {
            val request = ImageRequest.Builder(context)
                .data(data ?: R.drawable.album_art)
                .allowHardware(false)
                .build()

            val result = ImageLoader(context).execute(request)
            if (result is SuccessResult) {
                val drawable = result.drawable
                val bitmap = (drawable as? BitmapDrawable)?.bitmap
                bitmap?.let {
                    val palette = Palette.from(it).generate()
                    val swatch = palette.dominantSwatch ?: palette.vibrantSwatch
                    swatch?.rgb?.let { colorInt ->
                        bgColor = Color(colorInt)
                        contentColor = if (palette.dominantSwatch?.titleTextColor != null) {
                            Color(palette.dominantSwatch!!.titleTextColor)
                        } else {
                            if (Color(colorInt).luminance() > 0.5f) Color.Black else Color.White
                        }
                    }
                }
            }
        } catch (_: Exception) {
            bgColor = surfaceColor
            contentColor = onSurfaceColor
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Requirement 2: Button to take user to Library/HomeScreen
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Back to Library",
                    tint = contentColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 🎨 Album Art (static) - Requirement 1: Removed rotation (already static)
        Card(
            modifier = Modifier
                .size(320.dp)
                .aspectRatio(1f),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            AsyncImage(
                model = state.currentSong?.albumArt ?: R.drawable.album_art,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.height(32.dp))

        // 🎵 Song Info - Requirement 1: Marquee for long names
        Text(
            text = state.currentSong?.title ?: "No Song Playing",
            style = MaterialTheme.typography.headlineMedium,
            color = contentColor,
            maxLines = 1,
            modifier = Modifier.basicMarquee()
        )

        Text(
            text = state.currentSong?.artist ?: "Unknown Artist",
            style = MaterialTheme.typography.titleMedium,
            color = contentColor.copy(alpha = 0.7f)
        )

        Spacer(Modifier.height(32.dp))

        // 📊 Progress
        ProgressBar(
            progress = state.progress,
            duration = state.currentSong?.duration ?: 0L,
            onSeek = { viewModel.seekTo(it) }
        )

        Spacer(Modifier.height(32.dp))

        // ▶️ Controls
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { viewModel.previous() },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "Previous",
                    modifier = Modifier.size(36.dp),
                    tint = contentColor
                )
            }

            PlayPauseButton(
                isPlaying = state.isPlaying,
                onClick = {
                    if (state.isPlaying) viewModel.pause()
                    else viewModel.play()
                }
            )

            IconButton(
                onClick = { viewModel.next() },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Next",
                    modifier = Modifier.size(36.dp),
                    tint = contentColor
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Bottom row with shuffle, lyrics, playlist, repeat - Requirement 6
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            IconButton(onClick = { viewModel.toggleShuffle() }) {
                Icon(
                    imageVector = if (state.shuffleEnabled) Icons.Rounded.ShuffleOn else Icons.Rounded.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (state.shuffleEnabled) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.6f)
                )
            }

            IconButton(onClick = { onOpenLyrics() }) {
                Icon(
                    imageVector = Icons.Rounded.Lyrics,
                    contentDescription = "Lyrics",
                    tint = if (state.lyricsVisible) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.6f)
                )
            }

            IconButton(onClick = { onOpenPlaylist() }) {
                Icon(
                    imageVector = Icons.Rounded.PlaylistPlay,
                    contentDescription = "Playlist",
                    tint = contentColor.copy(alpha = 0.6f)
                )
            }

            IconButton(onClick = { viewModel.cycleRepeatMode() }) {
                Icon(
                    imageVector = when (state.repeatMode) {
                        RepeatMode.OFF -> Icons.Rounded.Repeat
                        RepeatMode.REPEAT_ALL -> Icons.Rounded.Repeat
                        RepeatMode.REPEAT_ONE -> Icons.Rounded.RepeatOne
                        RepeatMode.PLAY_ONE_ONCE -> Icons.Rounded.RepeatOne
                        RepeatMode.PLAY_ALL_ONCE -> Icons.Rounded.Repeat
                    },
                    contentDescription = "Repeat",
                    tint = if (state.repeatMode == RepeatMode.OFF) contentColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}