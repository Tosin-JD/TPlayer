package com.tosin.musicplayer.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.components.StatusBarColorEffect
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

    val wavePhase by produceState(initialValue = 0f, key1 = uiState.isPlaying, key2 = song?.id) {
        if (!uiState.isPlaying || song == null) {
            value = 0f
            return@produceState
        }

        while (true) {
            androidx.compose.runtime.withFrameNanos { frameTime ->
                value = ((frameTime % 6_000_000_000L).toFloat() / 6_000_000_000f)
            }
        }
    }

    val progressFraction = remember(song, uiState.progress) {
        val duration = song?.duration?.takeIf { it > 0 } ?: 1L
        (uiState.progress % duration).toFloat() / duration.toFloat()
    }

    val activeColor = MaterialTheme.colorScheme.primary
    val beatColor = MaterialTheme.colorScheme.tertiary
    val trebleColor = MaterialTheme.colorScheme.secondary
    val sopranoColor = MaterialTheme.colorScheme.error
    val topBackgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)

    StatusBarColorEffect(topBackgroundColor)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        topBackgroundColor,
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
                    windowInsets = WindowInsets(0, 0, 0, 0),
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
                    .padding(
                        start = AppSpacing.large,
                        top = 0.dp,
                        end = AppSpacing.large,
                        bottom = AppSpacing.large
                    ),
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
                    Box(modifier = Modifier.fillMaxSize()) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRhythmVisualizer(
                                wavePhase = wavePhase,
                                progressFraction = progressFraction,
                                playing = uiState.isPlaying,
                                activeColor = activeColor,
                                beatColor = beatColor,
                                trebleColor = trebleColor,
                                sopranoColor = sopranoColor
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacing.large, vertical = AppSpacing.medium),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RhythmLegendRow("Bass", activeColor, uiState.isPlaying)
                            RhythmLegendRow("Beat", beatColor, uiState.isPlaying)
                            RhythmLegendRow("Treble", trebleColor, uiState.isPlaying)
                            RhythmLegendRow("Soprano", sopranoColor, uiState.isPlaying)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RhythmLegendRow(
    label: String,
    color: Color,
    playing: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = color, shape = RoundedCornerShape(999.dp))
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = AppSpacing.small),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (playing) 0.9f else 0.55f)
        )
    }
}

private fun DrawScope.drawRhythmVisualizer(
    wavePhase: Float,
    progressFraction: Float,
    playing: Boolean,
    activeColor: Color,
    beatColor: Color,
    trebleColor: Color,
    sopranoColor: Color
) {
    val lanes = listOf(
        RhythmLane(activeColor, 0.22f, 0.18f, 0.18f, 0.0f),
        RhythmLane(beatColor, 0.42f, 0.24f, 0.26f, 0.45f),
        RhythmLane(trebleColor, 0.63f, 0.17f, 0.32f, 0.9f),
        RhythmLane(sopranoColor, 0.82f, 0.14f, 0.36f, 1.35f)
    )

    drawCircle(
        color = activeColor.copy(alpha = if (playing) 0.12f else 0.06f),
        radius = size.minDimension * 0.42f,
        center = Offset(size.width * 0.5f, size.height * 0.5f)
    )

    lanes.forEachIndexed { index, lane ->
        val laneY = size.height * lane.centerRatio
        val amplitude = size.height * lane.amplitudeRatio * if (playing) 1f else 0.35f
        val path = Path().apply {
            moveTo(0f, laneY)
            val steps = 72
            repeat(steps + 1) { step ->
                val x = size.width * step / steps.toFloat()
                val normalized = x / size.width
                val majorWave = sin((normalized * (4.2f + index * 0.9f) * Math.PI.toFloat() * 2f) + wavePhase * (5.1f + index * 0.8f))
                val minorWave = sin((normalized * (8.6f + index * 1.3f) * Math.PI.toFloat() * 2f) - wavePhase * (2.8f + index * 0.35f) + lane.phaseOffset)
                val rhythmEnvelope = 0.5f + abs(sin(progressFraction * Math.PI.toFloat() * 2f + normalized * Math.PI.toFloat() * (3.0f + index * 0.45f))) * 0.5f
                val beatLift = if (playing) abs(sin(wavePhase * Math.PI.toFloat() * (5.5f + index * 0.3f) + normalized * 6.2f)) else 0.1f
                val y = laneY + (majorWave * 0.7f + minorWave * 0.3f) * amplitude * rhythmEnvelope * (0.7f + beatLift * 0.3f)
                if (step == 0) moveTo(x, y) else lineTo(x, y)
            }
        }

        val brush = Brush.linearGradient(
            colors = listOf(
                lane.color.copy(alpha = 0.15f),
                lane.color,
                lane.color.copy(alpha = 0.9f)
            ),
            start = Offset(0f, laneY - amplitude),
            end = Offset(size.width, laneY + amplitude)
        )

        drawPath(
            path = path,
            brush = brush,
            style = Stroke(
                width = size.height * 0.018f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )

        val glowCount = 4
        repeat(glowCount) { glowIndex ->
            val t = (glowIndex + 1) / (glowCount + 1f)
            val x = size.width * t
            val wave = sin((t * (4.2f + index * 0.9f) * Math.PI.toFloat() * 2f) + wavePhase * (5.1f + index * 0.8f))
            val y = laneY + wave * amplitude * 0.72f
            val pulse = 1f + abs(sin(wavePhase * Math.PI.toFloat() * 4.4f + glowIndex * 0.65f + index * 0.28f)) * if (playing) 0.7f else 0.2f
            drawCircle(
                color = lane.color.copy(alpha = if (playing) 0.38f else 0.14f),
                radius = size.minDimension * 0.015f * pulse,
                center = Offset(x, y)
            )
        }
    }

    drawCircle(
        color = beatColor.copy(alpha = if (playing) 0.08f else 0.03f),
        radius = size.minDimension * (if (playing) 0.26f else 0.2f),
        center = Offset(size.width * 0.5f, size.height * 0.5f)
    )
}

private data class RhythmLane(
    val color: Color,
    val centerRatio: Float,
    val amplitudeRatio: Float,
    val thicknessRatio: Float,
    val phaseOffset: Float
)
