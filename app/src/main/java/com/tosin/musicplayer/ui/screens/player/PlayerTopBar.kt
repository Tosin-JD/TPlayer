package com.tosin.musicplayer.ui.screens.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
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
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.icons.AppIcons

@Composable
fun PlayerTopBar(
    contentColor: Color,
    sleepTimerRemaining: Long?,
    playbackSpeed: Float,
    isFavorite: Boolean,
    onNavigateBack: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenSpeedDialog: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = AppIcons.KeyboardArrowDown,
                contentDescription = "Back to Library",
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )
        }

        Row(modifier = Modifier.weight(1f)) {
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
                            AppIcons.Timer,
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

            IconButton(onClick = onOpenEqualizer) {
                Icon(
                    AppIcons.Tune,
                    contentDescription = "Equalizer",
                    tint = contentColor
                )
            }
        }

        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (isFavorite) AppIcons.Favorite else AppIcons.FavoriteBorder,
                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.6f)
            )
        }
    }
}
