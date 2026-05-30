package com.tosin.musicplayer.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import kotlin.math.abs
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualizerScreen(
    viewModel: PlayerViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val song = uiState.currentSong
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val progressFraction = remember(song, uiState.progress) {
        val duration = song?.duration?.takeIf { it > 0 } ?: 1L
        (uiState.progress % duration).toFloat() / duration.toFloat()
    }
    val activeColor = MaterialTheme.colorScheme.primary
    val accentColor = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = { Text("Visualizer", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = AppSpacing.large, vertical = AppSpacing.large),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = song?.title ?: "No track playing",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = song?.artist ?: "Open a song to animate the waves",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawWaveBars(
                                wavePhase = wavePhase,
                                progressFraction = progressFraction,
                                playing = uiState.isPlaying,
                                pulse = pulse,
                                activeColor = activeColor,
                                accentColor = accentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawWaveBars(
    wavePhase: Float,
    progressFraction: Float,
    playing: Boolean,
    pulse: Float,
    activeColor: Color,
    accentColor: Color
) {
    val barCount = 28
    val barWidth = size.width / (barCount * 2f)
    val spacing = barWidth
    val centerY = size.height / 2f
    val baseAmplitude = size.height * 0.26f * pulse
    val gradient = Brush.linearGradient(
        colors = listOf(
            activeColor.copy(alpha = 0.35f),
            activeColor,
            accentColor
        )
    )

    repeat(barCount) { index ->
        val x = index * (barWidth + spacing) + spacing / 2f
        val rhythm = sin((wavePhase * 8f) + (index * 0.5f) + (progressFraction * 12f))
        val envelope = abs(sin(progressFraction * Math.PI.toFloat() * 2f + index * 0.18f))
        val energy = if (playing) 0.45f + abs(rhythm) * 0.9f else 0.18f + abs(rhythm) * 0.35f
        val height = (baseAmplitude * energy * (0.55f + envelope * 0.55f)).coerceAtLeast(size.height * 0.08f)
        val top = centerY - height / 2f
        val radius = CornerRadius(barWidth * 0.5f, barWidth * 0.5f)
        drawRoundRect(
            brush = gradient,
            topLeft = androidx.compose.ui.geometry.Offset(x, top),
            size = androidx.compose.ui.geometry.Size(barWidth, height),
            cornerRadius = radius
        )
    }

    drawCircle(
        color = activeColor.copy(alpha = 0.08f),
        radius = size.minDimension * 0.38f,
        center = androidx.compose.ui.geometry.Offset(size.width / 2f, centerY)
    )
}
