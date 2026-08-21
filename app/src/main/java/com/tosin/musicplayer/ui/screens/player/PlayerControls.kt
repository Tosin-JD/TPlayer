package com.tosin.musicplayer.ui.screens.player

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.components.PlayPauseButton
import com.tosin.musicplayer.ui.theme.engine.customAppSurface
import com.tosin.musicplayer.ui.viewmodel.RepeatMode
import com.tosin.musicplayer.ui.icons.AppIcons

@Composable
fun PlayerMainControls(
    isPlaying: Boolean,
    contentColor: Color,
    onPrevious: () -> Unit,
    onRewind: () -> Unit,
    onStop: () -> Unit,
    onPlayPause: () -> Unit,
    onFastForward: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // Row 1: Previous, Play/Pause, Next — HUGE
        Row(
            horizontalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Previous
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(MaterialTheme.shapes.small)
                    .customAppSurface(
                        shape = MaterialTheme.shapes.small,
                        backgroundColor = Color.Black.copy(alpha = 0.15f)
                    )
                    .clickable(onClick = onPrevious),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AppIcons.SkipPrevious,
                    contentDescription = "Previous",
                    modifier = Modifier.size(48.dp),
                    tint = contentColor
                )
            }

            // Play/Pause
            PlayPauseButton(
                isPlaying = isPlaying,
                onClick = onPlayPause
            )

            // Next
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(MaterialTheme.shapes.small)
                    .customAppSurface(
                        shape = MaterialTheme.shapes.small,
                        backgroundColor = Color.Black.copy(alpha = 0.15f)
                    )
                    .clickable(onClick = onNext),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AppIcons.SkipNext,
                    contentDescription = "Next",
                    modifier = Modifier.size(48.dp),
                    tint = contentColor
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Row 2: Rewind 5s, Stop, Fast Forward 5s — MEDIUM
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Rewind 5s
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.small)
                    .customAppSurface(
                        shape = MaterialTheme.shapes.small,
                        backgroundColor = Color.Black.copy(alpha = 0.12f)
                    )
                    .clickable(onClick = onRewind),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AppIcons.Replay5,
                    contentDescription = "Rewind 5s",
                    modifier = Modifier.size(28.dp),
                    tint = contentColor.copy(alpha = 0.7f)
                )
            }

            // Stop
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.small)
                    .customAppSurface(
                        shape = MaterialTheme.shapes.small,
                        backgroundColor = Color.Black.copy(alpha = 0.12f)
                    )
                    .clickable(onClick = onStop),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AppIcons.Stop,
                    contentDescription = "Stop",
                    modifier = Modifier.size(28.dp),
                    tint = contentColor.copy(alpha = 0.7f)
                )
            }

            // Forward 5s
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.small)
                    .customAppSurface(
                        shape = MaterialTheme.shapes.small,
                        backgroundColor = Color.Black.copy(alpha = 0.12f)
                    )
                    .clickable(onClick = onFastForward),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AppIcons.Forward5,
                    contentDescription = "Forward 5s",
                    modifier = Modifier.size(28.dp),
                    tint = contentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun PlayerBottomBar(
    shuffleEnabled: Boolean,
    lyricsVisible: Boolean,
    repeatMode: RepeatMode,
    contentColor: Color,
    bottomButtonSize: Dp,
    bottomIconSize: Dp,
    onToggleShuffle: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenPlaylist: () -> Unit,
    onCycleRepeatMode: () -> Unit,
    onOpenMoreOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle
        Box(
            modifier = Modifier
                .size(bottomButtonSize)
                .clip(MaterialTheme.shapes.small)
                .customAppSurface(
                    shape = MaterialTheme.shapes.small,
                    backgroundColor = if (shuffleEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.12f)
                )
                .clickable(onClick = onToggleShuffle),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (shuffleEnabled) AppIcons.ShuffleOn else AppIcons.Shuffle,
                contentDescription = "Shuffle",
                tint = if (shuffleEnabled) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.6f),
                modifier = Modifier.size(bottomIconSize)
            )
        }

        // Lyrics
        Box(
            modifier = Modifier
                .size(bottomButtonSize)
                .clip(MaterialTheme.shapes.small)
                .customAppSurface(
                    shape = MaterialTheme.shapes.small,
                    backgroundColor = if (lyricsVisible) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.12f)
                )
                .clickable(onClick = onOpenLyrics),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = AppIcons.Lyrics,
                contentDescription = "Lyrics",
                tint = if (lyricsVisible) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.6f),
                modifier = Modifier.size(bottomIconSize)
            )
        }

        // Playlist
        Box(
            modifier = Modifier
                .size(bottomButtonSize)
                .clip(MaterialTheme.shapes.small)
                .customAppSurface(
                    shape = MaterialTheme.shapes.small,
                    backgroundColor = Color.Black.copy(alpha = 0.12f)
                )
                .clickable(onClick = onOpenPlaylist),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = AppIcons.PlaylistPlay,
                contentDescription = "Playlist",
                tint = contentColor.copy(alpha = 0.6f),
                modifier = Modifier.size(bottomIconSize)
            )
        }

        // Repeat
        val repeatIcon = when (repeatMode) {
            RepeatMode.PLAY_ALL_ONCE -> AppIcons.ArrowForward
            RepeatMode.PLAY_ONE_ONCE -> AppIcons.LooksOne
            RepeatMode.REPEAT_ALL -> AppIcons.Repeat
            RepeatMode.REPEAT_ONE -> AppIcons.RepeatOne
        }
        val repeatActive = repeatMode != RepeatMode.PLAY_ALL_ONCE
        val repeatTint = if (repeatActive) {
            MaterialTheme.colorScheme.primary
        } else {
            contentColor.copy(alpha = 0.6f)
        }

        Box(
            modifier = Modifier
                .size(bottomButtonSize)
                .clip(MaterialTheme.shapes.small)
                .customAppSurface(
                    shape = MaterialTheme.shapes.small,
                    backgroundColor = if (repeatActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.12f)
                )
                .clickable {
                    val nextMode = when (repeatMode) {
                        RepeatMode.PLAY_ALL_ONCE -> RepeatMode.PLAY_ONE_ONCE
                        RepeatMode.PLAY_ONE_ONCE -> RepeatMode.REPEAT_ALL
                        RepeatMode.REPEAT_ALL -> RepeatMode.REPEAT_ONE
                        RepeatMode.REPEAT_ONE -> RepeatMode.PLAY_ALL_ONCE
                    }
                    val statusText = when (nextMode) {
                        RepeatMode.PLAY_ALL_ONCE -> "Play all once"
                        RepeatMode.PLAY_ONE_ONCE -> "Play one once"
                        RepeatMode.REPEAT_ALL -> "Repeat all"
                        RepeatMode.REPEAT_ONE -> "Repeat one"
                    }
                    Toast.makeText(context, statusText, Toast.LENGTH_SHORT).show()
                    onCycleRepeatMode()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = repeatIcon,
                contentDescription = "Repeat Mode",
                tint = repeatTint,
                modifier = Modifier.size(bottomIconSize)
            )
        }

        // More Options
        Box(
            modifier = Modifier
                .size(bottomButtonSize)
                .clip(MaterialTheme.shapes.small)
                .customAppSurface(
                    shape = MaterialTheme.shapes.small,
                    backgroundColor = Color.Black.copy(alpha = 0.12f)
                )
                .clickable(onClick = onOpenMoreOptions),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = AppIcons.MoreVert,
                contentDescription = "More Options",
                tint = contentColor.copy(alpha = 0.6f),
                modifier = Modifier.size(bottomIconSize)
            )
        }
    }
}
