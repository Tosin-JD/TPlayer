package com.tosin.musicplayer.ui.screens.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.ui.extensions.orDefaultAlbumArt
import kotlin.math.abs

@Composable
fun PlayerAlbumArt(
    currentSong: Song?,
    albumArtSize: Dp,
    slideDirection: Int,
    albumDragRangePx: Float,
    albumSwipeThresholdPx: Float,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    var albumDragOffsetX by remember(currentSong?.id) { mutableFloatStateOf(0f) }

    AnimatedContent(
        targetState = currentSong,
        transitionSpec = {
            if (slideDirection > 0) {
                (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                (slideOutHorizontally { width -> -width } + fadeOut())
            } else {
                (slideInHorizontally { width -> -width } + fadeIn()) togetherWith
                (slideOutHorizontally { width -> width } + fadeOut())
            }
        },
        label = "albumArtTransition",
        modifier = modifier
    ) { targetSong ->
        Card(
            modifier = Modifier
                .size(albumArtSize)
                .aspectRatio(1f)
                .graphicsLayer {
                    translationX = albumDragOffsetX
                    val dragFraction = (albumDragOffsetX / albumDragRangePx).coerceIn(-1f, 1f)
                    scaleX = 1f - (abs(dragFraction) * 0.06f)
                    scaleY = 1f - (abs(dragFraction) * 0.06f)
                    rotationZ = dragFraction * 4f
                    alpha = 1f - (abs(dragFraction) * 0.12f)
                }
                .pointerInput(targetSong?.id) {
                    var dragDistance = 0f
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            dragDistance = (dragDistance + dragAmount).coerceIn(-albumDragRangePx, albumDragRangePx)
                            albumDragOffsetX = dragDistance
                            change.consume()
                        },
                        onDragEnd = {
                            when {
                                dragDistance > albumSwipeThresholdPx -> {
                                    onPrevious()
                                }
                                dragDistance < -albumSwipeThresholdPx -> {
                                    onNext()
                                }
                            }
                            dragDistance = 0f
                            albumDragOffsetX = 0f
                        },
                        onDragCancel = {
                            dragDistance = 0f
                            albumDragOffsetX = 0f
                        }
                    )
                },
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            AsyncImage(
                model = targetSong?.albumArt.orDefaultAlbumArt(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
