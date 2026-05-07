package com.tosin.musicplayer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun RotatingAlbumArt(
    image: String?,
    isPlaying: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "albumArtRotation")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing)
        ),
        label = "albumArtRotationLoop"
    )

    val animatedRotation by animateFloatAsState(
        targetValue = if (isPlaying) rotation else 0f,
        label = "rotationPause"
    )

    AsyncImage(
        model = image,
        contentDescription = null,
        modifier = Modifier
            .size(300.dp)
            .rotate(animatedRotation)
    )
}
