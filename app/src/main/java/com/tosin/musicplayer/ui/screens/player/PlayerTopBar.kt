package com.tosin.musicplayer.ui.screens.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.icons.AppIcons
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.theme.engine.customAppSurface

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
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Back button (Left side)
        Box(
            modifier = Modifier
                .size(48.dp)
                .customAppSurface(
                    shape = MaterialTheme.shapes.small,
                    backgroundColor = Color.Black.copy(alpha = 0.15f)
                )
                .clickable(onClick = onNavigateBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = AppIcons.KeyboardArrowDown,
                contentDescription = "Back to Library",
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )
        }

        // 2. Flexible Spacer pushes all action icons to the far right
        Spacer(modifier = Modifier.weight(1f))

        // 3. Action Buttons Group (Right side)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            // Sleep Timer Badge
            if (sleepTimerRemaining != null) {
                val remaining = sleepTimerRemaining / 1000
                val mins = remaining / 60
                val secs = remaining % 60
                Box(
                    modifier = Modifier
                        .customAppSurface(
                            shape = MaterialTheme.shapes.small,
                            backgroundColor = Color.Black.copy(alpha = 0.15f)
                        )
                        .clickable(onClick = onOpenSleepTimer)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            AppIcons.Timer,
                            contentDescription = "Sleep Timer",
                            tint = contentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "$mins:${secs.toString().padStart(2, '0')}",
                            color = contentColor,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // Playback Speed Badge
            if (playbackSpeed != 1.0f) {
                Box(
                    modifier = Modifier
                        .customAppSurface(
                            shape = MaterialTheme.shapes.small,
                            backgroundColor = Color.Black.copy(alpha = 0.15f)
                        )
                        .clickable(onClick = onOpenSpeedDialog)
                ) {
                    Text(
                        "${playbackSpeed}x",
                        color = contentColor,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            // Equalizer Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .customAppSurface(
                        shape = MaterialTheme.shapes.small,
                        backgroundColor = Color.Black.copy(alpha = 0.15f)
                    )
                    .clickable(onClick = onOpenEqualizer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    AppIcons.Tune,
                    contentDescription = "Equalizer",
                    tint = contentColor
                )
            }

            // Favorite Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .customAppSurface(
                        shape = MaterialTheme.shapes.small,
                        backgroundColor = if (isFavorite) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.15f)
                    )
                    .clickable(onClick = onToggleFavorite),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFavorite) AppIcons.Favorite else AppIcons.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}