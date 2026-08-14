package com.tosin.musicplayer.ui.screens.player

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LooksOne
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.ShuffleOn
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.components.PlayPauseButton
import com.tosin.musicplayer.ui.viewmodel.RepeatMode

@Composable
fun PlayerMainControls(
    isPlaying: Boolean,
    contentColor: Color,
    onPrevious: () -> Unit,
    onRewind: () -> Unit,
    onPlayPause: () -> Unit,
    onFastForward: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        IconButton(
            onClick = onPrevious,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = "Previous",
                modifier = Modifier.size(36.dp),
                tint = contentColor
            )
        }

        IconButton(
            onClick = onRewind,
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
            isPlaying = isPlaying,
            onClick = onPlayPause
        )

        IconButton(
            onClick = onFastForward,
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
            onClick = onNext,
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
    onOpenVisualizer: () -> Unit,
    onOpenPlaylist: () -> Unit,
    onCycleRepeatMode: () -> Unit,
    onOpenMoreOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onToggleShuffle,
            modifier = Modifier.size(bottomButtonSize)
        ) {
            Icon(
                imageVector = if (shuffleEnabled) Icons.Rounded.ShuffleOn else Icons.Rounded.Shuffle,
                contentDescription = "Shuffle",
                tint = if (shuffleEnabled) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.6f),
                modifier = Modifier.size(bottomIconSize)
            )
        }

        IconButton(
            onClick = onOpenLyrics,
            modifier = Modifier.size(bottomButtonSize)
        ) {
            Icon(
                imageVector = Icons.Rounded.Lyrics,
                contentDescription = "Lyrics",
                tint = if (lyricsVisible) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.6f),
                modifier = Modifier.size(bottomIconSize)
            )
        }

        IconButton(
            onClick = onOpenVisualizer,
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
            onClick = onOpenPlaylist,
            modifier = Modifier.size(bottomButtonSize)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.PlaylistPlay,
                contentDescription = "Playlist",
                tint = contentColor.copy(alpha = 0.6f),
                modifier = Modifier.size(bottomIconSize)
            )
        }

        val repeatIcon = when (repeatMode) {
            RepeatMode.PLAY_ALL_ONCE -> Icons.AutoMirrored.Rounded.ArrowForward
            RepeatMode.PLAY_ONE_ONCE -> Icons.Rounded.LooksOne
            RepeatMode.REPEAT_ALL -> Icons.Rounded.Repeat
            RepeatMode.REPEAT_ONE -> Icons.Rounded.RepeatOne
        }
        val repeatTint = if (repeatMode != RepeatMode.PLAY_ALL_ONCE) {
            MaterialTheme.colorScheme.primary
        } else {
            contentColor.copy(alpha = 0.6f)
        }

        IconButton(
            onClick = {
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
            modifier = Modifier.size(bottomButtonSize)
        ) {
            Icon(
                imageVector = repeatIcon,
                contentDescription = "Repeat Mode",
                tint = repeatTint,
                modifier = Modifier.size(bottomIconSize)
            )
        }

        IconButton(
            onClick = onOpenMoreOptions,
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
