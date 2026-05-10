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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
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

    var showSpeedDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }

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

    // Speed dialog
    if (showSpeedDialog) {
        SpeedPickerDialog(
            currentSpeed = state.playbackSpeed,
            onSpeedSelected = { speed ->
                viewModel.setPlaybackSpeed(speed)
                showSpeedDialog = false
            },
            onDismiss = { showSpeedDialog = false }
        )
    }

    // Sleep timer dialog
    if (showSleepTimerDialog) {
        SleepTimerDialog(
            currentRemaining = state.sleepTimerRemaining,
            onSetTimer = { minutes ->
                viewModel.setSleepTimer(minutes)
                showSleepTimerDialog = false
            },
            onCancel = {
                viewModel.cancelSleepTimer()
                showSleepTimerDialog = false
            },
            onDismiss = { showSleepTimerDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar with back + extra controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Back to Library",
                    tint = contentColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Row {
                // Sleep timer indicator
                if (state.sleepTimerRemaining != null) {
                    val remaining = state.sleepTimerRemaining!! / 1000
                    val mins = remaining / 60
                    val secs = remaining % 60
                    AssistChip(
                        onClick = { showSleepTimerDialog = true },
                        label = {
                            Text(
                                "$mins:${secs.toString().padStart(2, '0')}",
                                color = contentColor
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Timer,
                                contentDescription = "Sleep Timer",
                                tint = contentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                }

                // Speed indicator
                if (state.playbackSpeed != 1.0f) {
                    AssistChip(
                        onClick = { showSpeedDialog = true },
                        label = {
                            Text("${state.playbackSpeed}x", color = contentColor)
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Album Art
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

        // Song Info
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

        Spacer(Modifier.height(24.dp))

        // A-B Repeat indicators
        if (state.abRepeatA != null || state.abRepeatB != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = { viewModel.clearABRepeat() },
                    label = {
                        val a = state.abRepeatA?.let { formatTime(it) } ?: "—"
                        val b = state.abRepeatB?.let { formatTime(it) } ?: "—"
                        Text("A-B: $a → $b", color = contentColor)
                    },
                    trailingIcon = {
                        Icon(Icons.Rounded.Close, "Clear A-B", tint = contentColor, modifier = Modifier.size(16.dp))
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        // Progress
        ProgressBar(
            progress = state.progress,
            duration = state.currentSong?.duration ?: 0L,
            onSeek = { viewModel.seekTo(it) }
        )

        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(state.progress),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.6f)
            )
            Text(
                text = formatTime(state.currentSong?.duration ?: 0L),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.6f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Main Controls
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

            // Rewind
            IconButton(
                onClick = { viewModel.rewind() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Replay10,
                    contentDescription = "Rewind 10s",
                    modifier = Modifier.size(28.dp),
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

            // Fast Forward
            IconButton(
                onClick = { viewModel.fastForward() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Forward10,
                    contentDescription = "Forward 10s",
                    modifier = Modifier.size(28.dp),
                    tint = contentColor
                )
            }

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

        // Bottom row: shuffle, lyrics, A-B, speed, timer, playlist, repeat
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

            // A-B Repeat
            IconButton(onClick = {
                when {
                    state.abRepeatA == null -> viewModel.setABRepeatA()
                    state.abRepeatB == null -> viewModel.setABRepeatB()
                    else -> viewModel.clearABRepeat()
                }
            }) {
                Icon(
                    imageVector = Icons.Rounded.RepeatOneOn,
                    contentDescription = "A-B Repeat",
                    tint = if (state.abRepeatA != null) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.6f)
                )
            }

            IconButton(onClick = { showSpeedDialog = true }) {
                Icon(
                    imageVector = Icons.Rounded.Speed,
                    contentDescription = "Speed",
                    tint = if (state.playbackSpeed != 1.0f) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.6f)
                )
            }

            IconButton(onClick = { showSleepTimerDialog = true }) {
                Icon(
                    imageVector = Icons.Rounded.Timer,
                    contentDescription = "Sleep Timer",
                    tint = if (state.sleepTimerRemaining != null) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.6f)
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

@Composable
private fun SpeedPickerDialog(
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Playback Speed") },
        text = {
            Column {
                speeds.forEach { speed ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSpeed == speed,
                            onClick = { onSpeedSelected(speed) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${speed}x",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (speed == 1.0f) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "(Normal)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun SleepTimerDialog(
    currentRemaining: Long?,
    onSetTimer: (Int) -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    val durations = listOf(5, 10, 15, 30, 45, 60, 90, 120)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sleep Timer") },
        text = {
            Column {
                if (currentRemaining != null) {
                    val mins = currentRemaining / 60000
                    Text(
                        text = "Timer active: ${mins}min remaining",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onCancel) {
                        Text("Cancel Timer", color = MaterialTheme.colorScheme.error)
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                }
                durations.forEach { minutes ->
                    TextButton(
                        onClick = { onSetTimer(minutes) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "$minutes minutes",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}