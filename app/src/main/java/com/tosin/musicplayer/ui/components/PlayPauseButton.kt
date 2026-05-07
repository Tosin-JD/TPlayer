package com.tosin.musicplayer.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause

@Composable
fun PlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val transition = updateTransition(targetState = isPlaying, label = "playPause")

    val scale by transition.animateFloat(label = "scale") {
        if (it) 1.2f else 1f
    }

    IconButton(
        onClick = onClick,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {

        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "iconTransition"
        ) { playing ->

            if (playing) {
                Icon(Icons.Default.Pause, contentDescription = null)
            } else {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
            }
        }
    }
}
