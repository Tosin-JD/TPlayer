package com.tosin.musicplayer.ui.screens

import android.app.Activity
import android.graphics.drawable.BitmapDrawable
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.RepeatOneOn
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.ShuffleOn
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.palette.graphics.Palette
import androidx.core.view.WindowCompat
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.tosin.musicplayer.ui.components.PlayPauseButton
import com.tosin.musicplayer.ui.components.ProgressBar
import com.tosin.musicplayer.ui.extensions.orDefaultAlbumArt
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import com.tosin.musicplayer.ui.viewmodel.RepeatMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onOpenPlaylist: () -> Unit = {},
    onOpenLyrics: () -> Unit = {},
    onOpenVisualizer: () -> Unit = {},
    onOpenEqualizer: () -> Unit = {},
    onOpenSongEditor: (Long) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val view = LocalView.current
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp < 360
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val bottomIconSize = if (isSmallScreen) 44.dp else 32.dp
    val bottomButtonSize = if (isSmallScreen) 60.dp else 48.dp
    val albumArtSize = if (isLandscape) 240.dp else 320.dp
    val swipeThresholdPx = with(LocalDensity.current) { 96.dp.toPx() }

    var bgColor by remember { mutableStateOf(surfaceColor) }
    var contentColor by remember { mutableStateOf(onSurfaceColor) }

    var showSpeedDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showSystemBars by remember { mutableStateOf(false) }
    var hideBarsJob by remember { mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val activity = context as? Activity
        val window = activity?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        controller?.systemBarsBehavior =
            androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose {
            controller?.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        }
    }

    LaunchedEffect(bgColor) {
        val activity = context as? Activity
        val window = activity?.window ?: return@LaunchedEffect
        window.statusBarColor = bgColor.toArgb()
        window.navigationBarColor = bgColor.toArgb()
    }

    LaunchedEffect(showSystemBars) {
        val activity = context as? Activity
        val window = activity?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        if (showSystemBars) {
            controller?.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            hideBarsJob?.cancel()
            hideBarsJob = coroutineScope.launch {
                delay(5000)
                showSystemBars = false
            }
        } else {
            controller?.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        }
    }

    LaunchedEffect(state.currentSong?.albumArt) {
        val data = state.currentSong?.albumArt.orDefaultAlbumArt()
        try {
            val request = ImageRequest.Builder(context)
                .data(data)
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

    var showMoreOptionsSheet by remember { mutableStateOf(false) }
    var showABRepeatDialog by remember { mutableStateOf(false) }

    if (showMoreOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreOptionsSheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AppSpacing.xLarge)
            ) {
                Text(
                    text = "More Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = AppSpacing.large, vertical = AppSpacing.medium)
                )

                if (state.currentSong != null) {
                    androidx.compose.material3.ListItem(
                        headlineContent = { Text("Edit tags") },
                        supportingContent = { Text("Update title, artist, album and genre") },
                        leadingContent = { Icon(Icons.Rounded.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.clickable {
                            showMoreOptionsSheet = false
                            onOpenSongEditor(state.currentSong!!.id)
                        }
                    )

                    androidx.compose.material3.ListItem(
                        headlineContent = { Text("Set as ringtone") },
                        supportingContent = { Text("Make this song your default ringtone") },
                        leadingContent = { Icon(Icons.Rounded.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.clickable {
                            showMoreOptionsSheet = false
                            state.currentSong?.uri?.let { uriString ->
                                runCatching {
                                    android.media.RingtoneManager.setActualDefaultRingtoneUri(
                                        context,
                                        android.media.RingtoneManager.TYPE_RINGTONE,
                                        Uri.parse(uriString)
                                    )
                                    Toast.makeText(context, "Ringtone updated", Toast.LENGTH_SHORT).show()
                                }.getOrElse {
                                    Toast.makeText(context, "Unable to set ringtone", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )

                    androidx.compose.material3.ListItem(
                        headlineContent = { Text("Share song") },
                        supportingContent = { Text("Send the audio file to another app") },
                        leadingContent = { Icon(Icons.AutoMirrored.Rounded.PlaylistPlay, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.clickable {
                            showMoreOptionsSheet = false
                            state.currentSong?.let { song ->
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "audio/*"
                                    putExtra(Intent.EXTRA_STREAM, Uri.parse(song.uri))
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share song"))
                            }
                        }
                    )

                    androidx.compose.material3.ListItem(
                        headlineContent = { Text("Delete permanently") },
                        supportingContent = { Text("Remove the file from storage forever") },
                        leadingContent = { Icon(Icons.Rounded.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        modifier = Modifier.clickable {
                            showMoreOptionsSheet = false
                            showDeleteConfirm = true
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = AppSpacing.small))
                
                // Speed
                androidx.compose.material3.ListItem(
                    headlineContent = { Text("Playback Speed") },
                    supportingContent = { Text("${state.playbackSpeed}x") },
                    leadingContent = { Icon(androidx.compose.material.icons.Icons.Rounded.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable {
                        showMoreOptionsSheet = false
                        showSpeedDialog = true
                    }
                )

                // Sleep Timer
                androidx.compose.material3.ListItem(
                    headlineContent = { Text("Sleep Timer") },
                    supportingContent = { 
                        if (state.sleepTimerRemaining != null) {
                            Text("${state.sleepTimerRemaining!! / 60000} mins remaining")
                        } else {
                            Text("Off")
                        }
                    },
                    leadingContent = { Icon(androidx.compose.material.icons.Icons.Rounded.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable {
                        showMoreOptionsSheet = false
                        showSleepTimerDialog = true
                    }
                )

                // A-B Repeat
                androidx.compose.material3.ListItem(
                    headlineContent = { Text("A-B Repeat") },
                    supportingContent = {
                        if (state.abRepeatA != null || state.abRepeatB != null) {
                            val a = state.abRepeatA?.let { formatTime(it) } ?: "—"
                            val b = state.abRepeatB?.let { formatTime(it) } ?: "—"
                            Text("Active: $a to $b")
                        } else {
                            Text("Off")
                        }
                    },
                    leadingContent = { Icon(androidx.compose.material.icons.Icons.Rounded.RepeatOneOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable {
                        showMoreOptionsSheet = false
                        showABRepeatDialog = true
                    }
                )
            }
        }
    }

    if (showABRepeatDialog) {
        ABRepeatDialog(
            currentA = state.abRepeatA,
            currentB = state.abRepeatB,
            currentProgress = state.progress,
            duration = state.currentSong?.duration ?: 0L,
            onSetA = { viewModel.setABRepeatA() },
            onSetB = { viewModel.setABRepeatB() },
            onClear = { viewModel.clearABRepeat() },
            onDismiss = { showABRepeatDialog = false }
        )
    }

    if (showDeleteConfirm && state.currentSong != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Rounded.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete permanently?") },
            text = {
                Text("This will permanently delete \"${state.currentSong?.title}\". This cannot be reversed.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val song = state.currentSong
                        if (song != null) {
                            runCatching {
                                val deleted = context.contentResolver.delete(Uri.parse(song.uri), null, null)
                                if (deleted > 0) {
                                    viewModel.stop()
                                    viewModel.refreshLibrary()
                                    Toast.makeText(context, "Song deleted", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Unable to delete song", Toast.LENGTH_SHORT).show()
                                }
                            }.getOrElse {
                                Toast.makeText(context, "Unable to delete song", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showDeleteConfirm = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(
                    start = AppSpacing.small,
                    end = AppSpacing.small,
                    top = 0.dp,
                    bottom = if (showSystemBars) {
                        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    } else {
                        0.dp
                    }
                )
                .pointerInput(onNavigateBack) {
                    var dragDistance = 0f
                    detectVerticalDragGestures(
                        onDragStart = {
                            showSystemBars = true
                        },
                        onVerticalDrag = { change, dragAmount ->
                            dragDistance += dragAmount
                            change.consume()
                        },
                        onDragEnd = {
                            if (dragDistance > swipeThresholdPx) {
                                onNavigateBack()
                            }
                            dragDistance = 0f
                        },
                        onDragCancel = { dragDistance = 0f }
                    )
                },
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

                Column(modifier = Modifier.weight(1f).padding(horizontal = AppSpacing.small)) {
                    Text(
                        text = state.currentSong?.title ?: "No Song Playing",
                        style = MaterialTheme.typography.titleMedium,
                        color = contentColor,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                    Text(
                        text = state.currentSong?.artist ?: "Unknown Artist",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f),
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
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
                        Spacer(Modifier.width(AppSpacing.small))
                    }

                    // Speed indicator
                    if (state.playbackSpeed != 1.0f) {
                        AssistChip(
                            onClick = { showSpeedDialog = true },
                            label = {
                                Text("${state.playbackSpeed}x", color = contentColor)
                            }
                        )
                        Spacer(Modifier.width(AppSpacing.small))
                    }

                    IconButton(onClick = onOpenVisualizer) {
                        Icon(
                            Icons.Rounded.GraphicEq,
                            contentDescription = "Visualizer",
                            tint = contentColor
                        )
                    }

                    IconButton(onClick = onOpenEqualizer) {
                        Icon(
                            Icons.Rounded.Tune,
                            contentDescription = "Equalizer",
                            tint = contentColor
                        )
                    }
                }
            }

        Spacer(Modifier.height(AppSpacing.large))

        // Album Art
        Card(
            modifier = Modifier
                .size(albumArtSize)
                .aspectRatio(1f),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            AsyncImage(
                model = state.currentSong?.albumArt.orDefaultAlbumArt(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.height(AppSpacing.sectionSpacing))

        // Song Info (Artist only, title in top bar)
        Text(
            text = state.currentSong?.artist ?: "Unknown Artist",
            style = MaterialTheme.typography.headlineSmall,
            color = contentColor.copy(alpha = 0.8f),
            maxLines = 1,
            modifier = Modifier.basicMarquee()
        )

        Spacer(Modifier.height(AppSpacing.large))

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
            Spacer(Modifier.height(AppSpacing.small))
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

        Spacer(Modifier.height(AppSpacing.large))

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

        // Bottom row: shuffle, lyrics, playlist, repeat, ellipsis
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppSpacing.large),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.toggleShuffle() },
                modifier = Modifier.size(bottomButtonSize)
            ) {
                Icon(
                    imageVector = if (state.shuffleEnabled) Icons.Rounded.ShuffleOn else Icons.Rounded.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (state.shuffleEnabled) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(bottomIconSize)
                )
            }

            IconButton(
                onClick = { onOpenLyrics() },
                modifier = Modifier.size(bottomButtonSize)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lyrics,
                    contentDescription = "Lyrics",
                    tint = if (state.lyricsVisible) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(bottomIconSize)
                )
            }

            IconButton(
                onClick = { onOpenVisualizer() },
                modifier = Modifier.size(bottomButtonSize)
            ) {
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = "Visualizer",
                    tint = contentColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(bottomIconSize)
                )
            }

            IconButton(
                onClick = { onOpenPlaylist() },
                modifier = Modifier.size(bottomButtonSize)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.PlaylistPlay,
                    contentDescription = "Playlist",
                    tint = contentColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(bottomIconSize)
                )
            }

            val repeatIcon = when (state.repeatMode) {
                RepeatMode.PLAY_ALL_ONCE -> Icons.Rounded.Repeat
                RepeatMode.PLAY_ONE_ONCE -> Icons.Rounded.RepeatOne
                RepeatMode.REPEAT_ALL -> Icons.Rounded.Repeat
                RepeatMode.REPEAT_ONE -> Icons.Rounded.RepeatOne
                else -> Icons.Rounded.Repeat
            }
            val repeatTint = if (state.repeatMode == RepeatMode.REPEAT_ALL || state.repeatMode == RepeatMode.REPEAT_ONE) {
                MaterialTheme.colorScheme.primary
            } else {
                contentColor.copy(alpha = 0.6f)
            }
            
            val repeatAccessibility = when (state.repeatMode) {
                RepeatMode.PLAY_ALL_ONCE -> "Repeat Mode: All once"
                RepeatMode.PLAY_ONE_ONCE -> "Repeat Mode: One once"
                RepeatMode.REPEAT_ALL -> "Repeat Mode: All repeat"
                RepeatMode.REPEAT_ONE -> "Repeat Mode: One repeat"
                else -> "Repeat Mode"
            }

            IconButton(
                onClick = { viewModel.cycleRepeatMode() },
                modifier = Modifier.size(bottomButtonSize)
            ) {
                Icon(
                    imageVector = repeatIcon,
                    contentDescription = repeatAccessibility,
                    tint = repeatTint,
                    modifier = Modifier.size(bottomIconSize)
                )
            }

            IconButton(
            onClick = { showMoreOptionsSheet = true },
                modifier = Modifier.size(bottomButtonSize)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "More Options",
                    tint = contentColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(bottomIconSize)
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
                            .padding(vertical = AppSpacing.xSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSpeed == speed,
                            onClick = { onSpeedSelected(speed) }
                        )
                        Spacer(Modifier.width(AppSpacing.small))
                        Text(
                            text = "${speed}x",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (speed == 1.0f) {
                            Spacer(Modifier.width(AppSpacing.small))
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
                    Spacer(Modifier.height(AppSpacing.small))
                    TextButton(onClick = onCancel) {
                        Text("Cancel Timer", color = MaterialTheme.colorScheme.error)
                    }
                    HorizontalDivider(Modifier.padding(vertical = AppSpacing.small))
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

@Composable
private fun ABRepeatDialog(
    currentA: Long?,
    currentB: Long?,
    currentProgress: Long,
    duration: Long,
    onSetA: () -> Unit,
    onSetB: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("A-B Repeat") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
                Text(
                    text = "Current Position: ${formatTime(currentProgress)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Start Point (A)", style = MaterialTheme.typography.labelLarge)
                        Text(currentA?.let { formatTime(it) } ?: "Not Set", style = MaterialTheme.typography.bodyLarge)
                    }
                    FilledTonalButton(onClick = onSetA) {
                        Text("Set A")
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("End Point (B)", style = MaterialTheme.typography.labelLarge)
                        Text(currentB?.let { formatTime(it) } ?: "Not Set", style = MaterialTheme.typography.bodyLarge)
                    }
                    FilledTonalButton(onClick = onSetB, enabled = currentA != null) {
                        Text("Set B")
                    }
                }
                
                if (currentA != null || currentB != null) {
                    TextButton(
                        onClick = {
                            onClear()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear A-B Repeat", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}
