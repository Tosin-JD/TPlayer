package com.tosin.musicplayer.ui.components

import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable

@Composable
fun ProgressBar(
    progress: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    val progressFloat = if (duration > 0) progress.toFloat() / duration else 0f

    Slider(
        value = progressFloat,
        onValueChange = { onSeek((it * duration).toLong()) }
    )
}