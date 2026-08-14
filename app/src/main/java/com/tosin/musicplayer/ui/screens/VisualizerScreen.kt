package com.tosin.musicplayer.ui.screens

import android.media.audiofx.Visualizer
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
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.player.EqualizerManager
import com.tosin.musicplayer.ui.components.StatusBarColorEffect
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import kotlin.math.max
import kotlin.math.sqrt

private const val BAR_COUNT = 48

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualizerScreen(
    viewModel: PlayerViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val song = uiState.currentSong
    val playing = uiState.isPlaying

    val bars = remember { FloatArray(BAR_COUNT) }
    var barState by remember { mutableStateOf(FloatArray(BAR_COUNT)) }

    DisposableEffect(song?.id, playing) {
        val sessionId = EqualizerManager.currentAudioSessionId
        val visualizer = if (sessionId > 0 && playing) {
            runCatching {
                Visualizer(sessionId).apply {
                    setDataCaptureListener(
                        object : Visualizer.OnDataCaptureListener {
                            override fun onWaveFormDataCapture(
                                visualizer: Visualizer?,
                                waveform: ByteArray?,
                                samplingRate: Int
                            ) = Unit

                            override fun onFftDataCapture(
                                visualizer: Visualizer?,
                                fft: ByteArray?,
                                samplingRate: Int
                            ) {
                                updateBars(fft, bars)
                            }
                        },
                        max(1000, Visualizer.getMaxCaptureRate() / 4),
                        false,
                        true
                    )
                    enabled = true
                }
            }.getOrNull()
        } else {
            null
        }
        onDispose {
            runCatching { visualizer?.enabled = false }
            visualizer?.release()
        }
    }

    LaunchedEffect(playing) {
        while (true) {
            withFrameNanos {
                if (playing) {
                    barState = bars.copyOf()
                } else {
                    for (i in bars.indices) bars[i] *= 0.85f
                    barState = bars.copyOf()
                }
            }
        }
    }

    val activeColor = MaterialTheme.colorScheme.primary
    val midColor = MaterialTheme.colorScheme.tertiary
    val highColor = MaterialTheme.colorScheme.secondary
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
                        IconButton(
                            onClick = { if (playing) viewModel.pause() else viewModel.play() },
                            enabled = song != null
                        ) {
                            Icon(
                                imageVector = if (playing) {
                                    Icons.Rounded.Pause
                                } else {
                                    Icons.Rounded.PlayArrow
                                },
                                contentDescription = if (playing) "Pause" else "Play",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
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
                    text = song?.artist ?: "Play a song to see live audio",
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
                            drawFftBars(
                                bars = barState,
                                playing = playing,
                                activeColor = activeColor,
                                midColor = midColor,
                                highColor = highColor
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacing.large, vertical = AppSpacing.medium),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FftLegendRow("Low", activeColor, playing)
                            FftLegendRow("Mid", midColor, playing)
                            FftLegendRow("High", highColor, playing)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FftLegendRow(
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

private fun updateBars(fft: ByteArray?, target: FloatArray) {
    if (fft == null) return
    val n = fft.size
    if (n < 4) return
    val binCount = n / 2
    val scale = (n / 4f).coerceAtLeast(1f)

    for (i in target.indices) {
        val start = binCount * i / target.size
        val end = (binCount * (i + 1) / target.size).coerceAtLeast(start + 1)
        var peak = 0f
        for (bin in start until end) {
            val re = fft[bin * 2].toInt().toFloat()
            val im = fft[bin * 2 + 1].toInt().toFloat()
            val magnitude = sqrt(re * re + im * im) / scale
            if (magnitude > peak) peak = magnitude
        }
        val shaped = sqrt(peak.coerceIn(0f, 1f))
        target[i] += (shaped - target[i]) * 0.35f
    }
}

private fun DrawScope.drawFftBars(
    bars: FloatArray,
    playing: Boolean,
    activeColor: Color,
    midColor: Color,
    highColor: Color
) {
    val centerY = size.height * 0.5f
    val barGap = 3.dp.toPx()
    val barWidth = (size.width / bars.size) - barGap
    val maxBarHeight = size.height * 0.42f

    drawCircle(
        color = activeColor.copy(alpha = if (playing) 0.1f else 0.05f),
        radius = size.minDimension * 0.42f,
        center = Offset(size.width * 0.5f, centerY)
    )

    val gradient = Brush.verticalGradient(
        colors = listOf(activeColor, midColor, highColor),
        startY = centerY - maxBarHeight,
        endY = centerY + maxBarHeight
    )

    bars.forEachIndexed { index, value ->
        val x = index * (barWidth + barGap)
        val barHeight = value * maxBarHeight
        val radius = CornerRadius(barWidth / 2f, barWidth / 2f)

        drawRoundRect(
            brush = gradient,
            topLeft = Offset(x, centerY - barHeight),
            size = androidx.compose.ui.geometry.Size(barWidth, barHeight.coerceAtLeast(1f)),
            cornerRadius = radius
        )
        drawRoundRect(
            brush = gradient,
            topLeft = Offset(x, centerY),
            size = androidx.compose.ui.geometry.Size(barWidth, barHeight.coerceAtLeast(1f)),
            cornerRadius = radius
        )
    }

    drawCircle(
        color = activeColor.copy(alpha = if (playing) 0.08f else 0.03f),
        radius = size.minDimension * 0.24f,
        center = Offset(size.width * 0.5f, centerY)
    )

    if (!playing) {
        drawCircle(
            color = activeColor.copy(alpha = 0.15f),
            radius = size.minDimension * 0.012f,
            center = Offset(size.width * 0.5f, centerY)
        )
    }
}
