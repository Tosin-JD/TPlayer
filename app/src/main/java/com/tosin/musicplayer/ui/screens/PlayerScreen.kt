package com.tosin.musicplayer.ui.screens

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LinearScale
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.ShuffleOn
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
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
import kotlinx.coroutines.launch

private val BottomIconSize   = 28.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onOpenPlaylist: () -> Unit = {},
    onOpenLyrics: () -> Unit = {},
    onOpenEqualizer: () -> Unit = {},
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
    var showMoreSheet by remember { mutableStateOf(false) }
    var showAbDialog by remember { mutableStateOf(false) }

    val moreSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

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

    // ── Dialogs ────────────────────────────────────────────────────────────
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

    if (showAbDialog) {
        AbRepeatDialog(
            abRepeatA = state.abRepeatA,
            abRepeatB = state.abRepeatB,
            currentPosition = state.progress,
            duration = state.currentSong?.duration ?: 0L,
            onSetA = { ms -> viewModel.setAbRepeatAAt(ms) },
            onSetB = { ms -> viewModel.setAbRepeatBAt(ms) },
            onClear = { viewModel.clearABRepeat() },
            onDismiss = { showAbDialog = false }
        )
    }

    // ── "More" bottom sheet ────────────────────────────────────────────────
    if (showMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            sheetState = moreSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = AppSpacing.large)
            ) {
                Text(
                    text = "More options",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(
                        horizontal = AppSpacing.large,
                        vertical = AppSpacing.small
                    )
                )
                HorizontalDivider()

                // A-B Repeat
                val abActive = state.abRepeatA != null
                val abLabel = when {
                    state.abRepeatA == null -> "Set A-B Repeat"
                    state.abRepeatB == null -> "A set — tap to set B"
                    else -> {
                        val a = formatTime(state.abRepeatA!!)
                        val b = formatTime(state.abRepeatB!!)
                        "A-B: $a → $b  (tap to edit)"
                    }
                }
                val abTooltipState = rememberTooltipState()
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        Surface(
                            color = MaterialTheme.colorScheme.inverseSurface,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "A-B Repeat",
                                color = MaterialTheme.colorScheme.inverseOnSurface,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    },
                    state = abTooltipState
                ) {
                    ListItem(
                        headlineContent = { Text(abLabel) },
                        supportingContent = { Text("Loop a section of the track") },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Rounded.LinearScale,
                                contentDescription = "A-B Repeat",
                                tint = if (abActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        trailingContent = if (abActive) ({
                            IconButton(onClick = { viewModel.clearABRepeat() }) {
                                Icon(Icons.Rounded.Close, "Clear A-B",
                                    tint = MaterialTheme.colorScheme.error)
                            }
                        }) else null,
                        modifier = Modifier
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = { scope.launch { abTooltipState.show() } },
                                    onTap = {
                                        scope.launch { moreSheetState.hide() }.invokeOnCompletion {
                                            showMoreSheet = false
                                            showAbDialog = true
                                        }
                                    }
                                )
                            }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = AppSpacing.large))

                // Playback Speed
                ListItem(
                    headlineContent = { Text("Play/Pause Speed") },
                    supportingContent = {
                        Text(if (state.playbackSpeed == 1.0f) "Normal" else "${state.playbackSpeed}x")
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.Speed,
                            contentDescription = "Speed",
                            tint = if (state.playbackSpeed != 1.0f) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    modifier = Modifier.clickable {
                        scope.launch { moreSheetState.hide() }.invokeOnCompletion {
                            showMoreSheet = false
                            showSpeedDialog = true
                        }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = AppSpacing.large))

                // Sleep Timer
                ListItem(
                    headlineContent = { Text("Sleep Timer") },
                    supportingContent = {
                        val remaining = state.sleepTimerRemaining
                        if (remaining != null) {
                            val mins = remaining / 60000
                            val secs = (remaining % 60000) / 1000
                            Text("${mins}m ${secs}s remaining")
                        } else {
                            Text("Off")
                        }
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.Timer,
                            contentDescription = "Sleep Timer",
                            tint = if (state.sleepTimerRemaining != null) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    modifier = Modifier.clickable {
                        scope.launch { moreSheetState.hide() }.invokeOnCompletion {
                            showMoreSheet = false
                            showSleepTimerDialog = true
                        }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = AppSpacing.large))

                // Equalizer
                ListItem(
                    headlineContent = { Text("Equalizer") },
                    supportingContent = { Text("Adjust sound frequencies") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = "Equalizer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    modifier = Modifier.clickable {
                        scope.launch { moreSheetState.hide() }.invokeOnCompletion {
                            showMoreSheet = false
                            onOpenEqualizer()
                        }
                    }
                )
            }
        }
    }

    // ── Main UI ────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = AppSpacing.large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
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

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = AppSpacing.small)
            ) {
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

            Row(verticalAlignment = Alignment.CenterVertically) {
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
                if (state.playbackSpeed != 1.0f) {
                    AssistChip(
                        onClick = { showSpeedDialog = true },
                        label = { Text("${state.playbackSpeed}x", color = contentColor) }
                    )
                    Spacer(Modifier.width(AppSpacing.small))
                }
            }
        }

        Spacer(Modifier.height(AppSpacing.large))

        // Album Art
        Card(
            modifier = Modifier
                .size(300.dp)
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

        Text(
            text = state.currentSong?.artist ?: "Unknown Artist",
            style = MaterialTheme.typography.headlineSmall,
            color = contentColor.copy(alpha = 0.8f),
            maxLines = 1,
            modifier = Modifier.basicMarquee()
        )

        Spacer(Modifier.height(AppSpacing.large))

        // A-B indicator bar (shown when active)
        if (state.abRepeatA != null || state.abRepeatB != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = { showAbDialog = true },
                    label = {
                        val a = state.abRepeatA?.let { formatTime(it) } ?: "—"
                        val b = state.abRepeatB?.let { formatTime(it) } ?: "—"
                        Text("A-B: $a → $b", color = contentColor)
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Rounded.Close, "Clear A-B",
                            tint = contentColor,
                            modifier = Modifier.size(16.dp)
                        )
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

        // Main transport controls
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { viewModel.previous() }, modifier = Modifier.size(56.dp)) {
                Icon(
                    Icons.Rounded.SkipPrevious, "Previous",
                    modifier = Modifier.size(36.dp), tint = contentColor
                )
            }
            IconButton(onClick = { viewModel.rewind() }, modifier = Modifier.size(48.dp)) {
                Icon(
                    Icons.Rounded.Replay10, "Rewind 10s",
                    modifier = Modifier.size(28.dp), tint = contentColor
                )
            }
            PlayPauseButton(
                isPlaying = state.isPlaying,
                onClick = { if (state.isPlaying) viewModel.pause() else viewModel.play() }
            )
            IconButton(onClick = { viewModel.fastForward() }, modifier = Modifier.size(48.dp)) {
                Icon(
                    Icons.Rounded.Forward10, "Forward 10s",
                    modifier = Modifier.size(28.dp), tint = contentColor
                )
            }
            IconButton(onClick = { viewModel.next() }, modifier = Modifier.size(56.dp)) {
                Icon(
                    Icons.Rounded.SkipNext, "Next",
                    modifier = Modifier.size(36.dp), tint = contentColor
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // ── Bottom action row: 5 items ─────────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppSpacing.large)
        ) {
            // 1 · Shuffle
            BottomActionButton(tooltip = "Shuffle", onClick = { viewModel.toggleShuffle() }) {
                Icon(
                    imageVector = if (state.shuffleEnabled) Icons.Rounded.ShuffleOn else Icons.Rounded.Shuffle,
                    contentDescription = "Shuffle",
                    modifier = Modifier.size(BottomIconSize),
                    tint = if (state.shuffleEnabled) MaterialTheme.colorScheme.primary
                    else contentColor.copy(alpha = 0.75f)
                )
            }

            // 2 · Lyrics
            BottomActionButton(tooltip = "Lyrics", onClick = { onOpenLyrics() }) {
                Icon(
                    imageVector = Icons.Rounded.Lyrics,
                    contentDescription = "Lyrics",
                    modifier = Modifier.size(BottomIconSize),
                    tint = if (state.lyricsVisible) MaterialTheme.colorScheme.primary
                    else contentColor.copy(alpha = 0.75f)
                )
            }

            // 3 · Playlist
            BottomActionButton(tooltip = "Playlist", onClick = { onOpenPlaylist() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.PlaylistPlay,
                    contentDescription = "Playlist",
                    modifier = Modifier.size(BottomIconSize),
                    tint = contentColor.copy(alpha = 0.75f)
                )
            }

            // 4 · Repeat (icon only — no text label, label is in contentDescription only)
            val repeatIcon = when (state.repeatMode) {
                RepeatMode.PLAY_ALL_ONCE -> Icons.Rounded.Repeat
                RepeatMode.PLAY_ONE_ONCE -> Icons.Rounded.RepeatOne
                RepeatMode.REPEAT_ALL    -> Icons.Rounded.Repeat
                RepeatMode.REPEAT_ONE    -> Icons.Rounded.RepeatOne
                else                     -> Icons.Rounded.Repeat
            }
            val repeatActive = state.repeatMode == RepeatMode.REPEAT_ALL ||
                    state.repeatMode == RepeatMode.REPEAT_ONE
            val repeatA11y = when (state.repeatMode) {
                RepeatMode.PLAY_ALL_ONCE -> "Repeat: play all once"
                RepeatMode.PLAY_ONE_ONCE -> "Repeat: play one once"
                RepeatMode.REPEAT_ALL    -> "Repeat: all (active)"
                RepeatMode.REPEAT_ONE    -> "Repeat: one (active)"
                else                     -> "Repeat"
            }
            BottomActionButton(tooltip = repeatA11y, onClick = { viewModel.cycleRepeatMode() }) {
                Icon(
                    imageVector = repeatIcon,
                    contentDescription = repeatA11y,      // a11y only — NO visible text
                    modifier = Modifier.size(BottomIconSize),
                    tint = if (repeatActive) MaterialTheme.colorScheme.primary
                    else contentColor.copy(alpha = 0.75f)
                )
            }

            // 5 · More (three dots)
            BottomActionButton(tooltip = "More options", onClick = { showMoreSheet = true }) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "More options",
                    modifier = Modifier.size(BottomIconSize),
                    tint = contentColor.copy(alpha = 0.75f)
                )
            }
        }
    }
}

// ── Reusable bottom-row button with long-press tooltip ─────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomActionButton(
    tooltip: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            Surface(
                color = MaterialTheme.colorScheme.inverseSurface,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = tooltip,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        },
        state = tooltipState
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(52.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { scope.launch { tooltipState.show() } },
                        onTap = { onClick() }
                    )
                }
        ) {
            content()
        }
    }
}

// ── A-B Repeat dialog ──────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AbRepeatDialog(
    abRepeatA: Long?,
    abRepeatB: Long?,
    currentPosition: Long,
    duration: Long,
    onSetA: (Long) -> Unit,
    onSetB: (Long) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    // Editable text fields seeded from current values
    var aText by remember(abRepeatA) {
        mutableStateOf(abRepeatA?.let { formatTime(it) } ?: formatTime(currentPosition))
    }
    var bText by remember(abRepeatB) {
        mutableStateOf(abRepeatB?.let { formatTime(it) } ?: "")
    }
    var aError by remember { mutableStateOf(false) }
    var bError by remember { mutableStateOf(false) }

    fun parseTime(text: String): Long? {
        val parts = text.trim().split(":")
        return if (parts.size == 2) {
            val m = parts[0].toLongOrNull() ?: return null
            val s = parts[1].toLongOrNull() ?: return null
            if (s >= 60) return null
            val ms = (m * 60 + s) * 1000L
            if (duration > 0 && ms > duration) null else ms
        } else null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("A-B Repeat") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
                Text(
                    text = "Set a start point (A) and end point (B) to loop a section.\nFormat: m:ss  e.g. 1:30",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Point A
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
                ) {
                    OutlinedTextField(
                        value = aText,
                        onValueChange = { aText = it; aError = false },
                        label = { Text("Start (A)") },
                        placeholder = { Text("m:ss") },
                        isError = aError,
                        supportingText = if (aError) ({ Text("Invalid time") }) else null,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        aText = formatTime(currentPosition)
                        aError = false
                    }) { Text("Use now") }
                }

                // Point B
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
                ) {
                    OutlinedTextField(
                        value = bText,
                        onValueChange = { bText = it; bError = false },
                        label = { Text("End (B)") },
                        placeholder = { Text("m:ss") },
                        isError = bError,
                        supportingText = if (bError) ({ Text("Invalid time") }) else null,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        bText = formatTime(currentPosition)
                        bError = false
                    }) { Text("Use now") }
                }

                if (abRepeatA != null) {
                    TextButton(
                        onClick = { onClear(); onDismiss() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Clear A-B", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val aMs = parseTime(aText)
                val bMs = parseTime(bText)
                aError = aMs == null
                bError = bMs == null || (aMs != null && bMs <= aMs)
                if (!aError && !bError) {
                    onSetA(aMs!!)
                    onSetB(bMs!!)
                    onDismiss()
                }
            }) { Text("Set loop") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ── Speed picker dialog ────────────────────────────────────────────────────
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
                        RadioButton(selected = currentSpeed == speed, onClick = { onSpeedSelected(speed) })
                        Spacer(Modifier.width(AppSpacing.small))
                        Text("${speed}x", style = MaterialTheme.typography.bodyLarge)
                        if (speed == 1.0f) {
                            Spacer(Modifier.width(AppSpacing.small))
                            Text("(Normal)", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

// ── Sleep timer dialog ─────────────────────────────────────────────────────
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
                    Text("Timer active: ${mins}min remaining",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(AppSpacing.small))
                    TextButton(onClick = onCancel) {
                        Text("Cancel Timer", color = MaterialTheme.colorScheme.error)
                    }
                    HorizontalDivider(Modifier.padding(vertical = AppSpacing.small))
                }
                durations.forEach { minutes ->
                    TextButton(onClick = { onSetTimer(minutes) }, modifier = Modifier.fillMaxWidth()) {
                        Text("$minutes minutes", modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

// ── Helpers ────────────────────────────────────────────────────────────────
private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}