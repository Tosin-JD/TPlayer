package com.tosin.musicplayer.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.components.StatusBarColorEffect
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import com.tosin.musicplayer.ui.visualizer.VisualizerType
import com.tosin.musicplayer.ui.visualizer.viewmodel.VisualizerViewModel
import com.tosin.musicplayer.ui.icons.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualizerScreen(
    viewModel: PlayerViewModel,
    visualizerViewModel: VisualizerViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateBack: () -> Unit
) {
    val playerState by viewModel.uiState.collectAsState()
    val magnitudes by visualizerViewModel.magnitudes.collectAsState()
    val currentType by visualizerViewModel.currentType.collectAsState()
    val song = playerState.currentSong
    val playing = playerState.isPlaying

    // Attach/release native Visualizer with audio session lifecycle
    DisposableEffect(song?.id, playing) {
        visualizerViewModel.attachAudioSession(playing)
        onDispose { visualizerViewModel.releaseVisualizer() }
    }

    // Animation frame loop
    var timeMs by remember { mutableLongStateOf(0L) }
    LaunchedEffect(playing) {
        while (true) {
            withFrameNanos { nanos ->
                timeMs = nanos / 1_000_000L
                if (!playing) visualizerViewModel.decayMagnitudes()
            }
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val accentColor = MaterialTheme.colorScheme.secondary
    val topBg = primaryColor.copy(alpha = 0.22f)
    StatusBarColorEffect(topBg)

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(topBg, MaterialTheme.colorScheme.surface,
                       MaterialTheme.colorScheme.surfaceContainerLow)
            )
        )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                VisualizerTopBar(playing, song != null, onNavigateBack,
                    { if (playing) viewModel.pause() else viewModel.play() })
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding)
                    .padding(horizontal = AppSpacing.large),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(song?.title ?: "No track playing",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold)
                Text(song?.artist ?: "Play a song to see live audio",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                StylePicker(currentType) { visualizerViewModel.setVisualizerType(it) }
                Spacer(Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme
                            .surfaceContainerHighest.copy(alpha = 0.55f))
                ) {
                    val style = visualizerViewModel.currentStyle
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        with(style) {
                            render(magnitudes, playing, primaryColor, accentColor, timeMs)
                        }
                    }
                }
                Spacer(Modifier.height(AppSpacing.large))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VisualizerTopBar(
    playing: Boolean, songExists: Boolean,
    onBack: () -> Unit, onTogglePlay: () -> Unit
) {
    TopAppBar(
        title = { Text("Visualizer", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(AppIcons.ArrowBack, "Back")
            }
        },
        actions = {
            IconButton(onClick = onTogglePlay, enabled = songExists) {
                Icon(if (playing) AppIcons.Pause else AppIcons.Play,
                    if (playing) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.primary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
private fun StylePicker(current: VisualizerType, onSelect: (VisualizerType) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()) {
        VisualizerType.entries.forEach { type ->
            FilterChip(selected = type == current, onClick = { onSelect(type) },
                label = { Text(type.displayName, style = MaterialTheme.typography.labelMedium) })
        }
    }
}
