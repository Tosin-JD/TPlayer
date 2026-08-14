package com.tosin.musicplayer.ui.screens.player

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.ui.theme.AppSpacing

@Composable
fun PlayerTopBar(
    currentSong: Song?,
    contentColor: Color,
    sleepTimerRemaining: Long?,
    playbackSpeed: Float,
    onNavigateBack: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenSpeedDialog: () -> Unit,
    onOpenVisualizer: () -> Unit,
    onOpenEqualizer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
                text = currentSong?.title ?: "No Song Playing",
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )
            Text(
                text = currentSong?.artist ?: "Unknown Artist",
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.7f),
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )
        }

        Row {
            if (sleepTimerRemaining != null) {
                val remaining = sleepTimerRemaining / 1000
                val mins = remaining / 60
                val secs = remaining % 60
                AssistChip(
                    onClick = onOpenSleepTimer,
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

            if (playbackSpeed != 1.0f) {
                AssistChip(
                    onClick = onOpenSpeedDialog,
                    label = {
                        Text("${playbackSpeed}x", color = contentColor)
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
}
