package com.tosin.musicplayer.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tosin.musicplayer.ui.viewmodel.EqualizerViewModel
import kotlinx.coroutines.delay
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    viewModel: EqualizerViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Equalizer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.setPreset(0) }) {
                        Text("Reset")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Master Toggle
            Surface(
                color = if (uiState.enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.setEnabled(!uiState.enabled) }
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (uiState.enabled) "Equalizer Active" else "Equalizer Inactive",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Enable system-wide audio effects",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.enabled,
                        onCheckedChange = { viewModel.setEnabled(it) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Real-time Visualizer (Dummy animated)
            AnimatedVisualizer(uiState.enabled)

            Spacer(Modifier.height(24.dp))

            // Presets
            Text(
                text = "Presets",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(uiState.presets) { index, preset ->
                    FilterChip(
                        selected = uiState.selectedPreset == index,
                        onClick = { viewModel.setPreset(index) },
                        label = { Text(preset) },
                        enabled = uiState.enabled
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Bands
            Text(
                text = "Frequency Bands",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                uiState.bands.forEach { band ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .width(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Slider(
                                value = band.level.toFloat(),
                                onValueChange = { viewModel.setBandLevel(band.id, it.toInt()) },
                                valueRange = band.minLevel.toFloat()..band.maxLevel.toFloat(),
                                enabled = uiState.enabled,
                                modifier = Modifier
                                    .graphicsLayer {
                                        rotationZ = -90f
                                    }
                                    .width(180.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (band.frequency < 1000) "${band.frequency}Hz" else "${band.frequency / 1000}kHz",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "${band.level / 100}dB",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Enhancements
            Text(
                text = "Audio Enhancements",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            EnhancementSlider(
                label = "Bass Boost",
                value = uiState.bassBoost.toFloat(),
                onValueChange = { viewModel.setBassBoost(it.toInt()) },
                valueRange = 0f..1000f,
                enabled = uiState.enabled,
                icon = Icons.Rounded.Audiotrack
            )

            EnhancementSlider(
                label = "Spatial Audio",
                value = uiState.virtualizer.toFloat(),
                onValueChange = { viewModel.setVirtualizer(it.toInt()) },
                valueRange = 0f..1000f,
                enabled = uiState.enabled,
                icon = Icons.Rounded.SurroundSound
            )

            EnhancementSlider(
                label = "Loudness Enhancer",
                value = uiState.loudness.toFloat(),
                onValueChange = { viewModel.setLoudness(it.toInt()) },
                valueRange = 0f..2000f,
                enabled = uiState.enabled,
                icon = Icons.AutoMirrored.Rounded.VolumeUp
            )
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun EnhancementSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            Text("${(value / valueRange.endInclusive * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            enabled = enabled
        )
    }
}

@Composable
fun AnimatedVisualizer(enabled: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val barCount = 40
            val barWidth = width / barCount
            val primaryColor = if (enabled) Color(0xFF00D1FF) else Color.Gray.copy(alpha = 0.3f)

            for (i in 0 until barCount) {
                val x = i * barWidth
                val variation = sin(phase + i * 0.5f) * 0.5f + 0.5f
                val barHeight = if (enabled) (height * 0.2f) + (height * 0.6f * variation) else height * 0.1f
                
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(x + barWidth * 0.1f, height / 2 - barHeight / 2),
                    size = Size(barWidth * 0.8f, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                )
            }
        }
    }
}

// Extension to allow vertical rotation for Slider
@Composable
fun VerticalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    // This is handled via graphicsLayer in the main screen for now as Compose doesn't have native VerticalSlider yet
}
