package com.tosin.musicplayer.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue


@Composable
fun ProgressBar(
    progress: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    val target = if (duration > 0) progress.toFloat() / duration else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = target,
        label = "progressAnim"
    )

    Slider(
        value = animatedProgress,
        onValueChange = {
            onSeek((it * duration).toLong())
        }
    )
}