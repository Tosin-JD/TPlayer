package com.tosin.musicplayer.ui.screens


import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tosin.musicplayer.ui.components.PlayPauseButton
import com.tosin.musicplayer.ui.components.ProgressBar
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel

@Composable
public fun PlayerScreen(
    viewModel: PlayerViewModel
) {
    val state by viewModel.uiState.collectAsState()

    val rotation by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = if (state.isPlaying) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(40.dp))

        // 🎨 Album Art (Animated rotation)
        AsyncImage(
            model = state.currentSong?.albumArt,
            contentDescription = null,
            modifier = Modifier
                .size(300.dp)
                .rotate(rotation)
        )

        Spacer(Modifier.height(24.dp))

        // 🎵 Song Info
        Text(
            text = state.currentSong?.title ?: "",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = state.currentSong?.artist ?: "",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(24.dp))

        // 📊 Progress
        ProgressBar(
            progress = state.progress,
            duration = state.currentSong?.duration ?: 0L,
            onSeek = { viewModel.seekTo(it) }
        )

        Spacer(Modifier.height(24.dp))

        // ▶️ Controls
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = { viewModel.previous() }) {
                Text("Prev")
            }

            PlayPauseButton(
                isPlaying = state.isPlaying,
                onClick = {
                    if (state.isPlaying) viewModel.pause()
                    else viewModel.play()
                }
            )

            TextButton(onClick = { viewModel.next() }) {
                Text("Next")
            }
        }
    }
}