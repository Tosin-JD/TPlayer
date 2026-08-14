package com.tosin.musicplayer.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ProgressBar(
    progress: Long,
    duration: Long,
    onSeek: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier,
    trackHeight: Dp = 4.dp,
    trackAlpha: Float = 0.45f
) {
    val target = if (duration > 0) progress.toFloat() / duration else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = target,
        label = "progressAnim"
    )

    if (onSeek == null) {
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = modifier
                .fillMaxWidth()
                .height(trackHeight),
            trackColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = trackAlpha * 0.35f),
            color = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = trackAlpha)
        )
    } else {
        Slider(
            value = animatedProgress,
            onValueChange = {
                onSeek((it * duration).toLong())
            },
            modifier = modifier,
            colors = SliderDefaults.colors(
                thumbColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                activeTrackColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                inactiveTrackColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        )
    }
}