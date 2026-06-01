package com.tosin.musicplayer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import com.tosin.musicplayer.ui.state.EqBand
import com.tosin.musicplayer.ui.state.EqualizerPresetUi
import com.tosin.musicplayer.ui.components.StatusBarColorEffect
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.viewmodel.EqualizerViewModel
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    viewModel: EqualizerViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isCompact = configuration.screenWidthDp < 360
    val horizontalPadding = if (isCompact) AppSpacing.medium else AppSpacing.large

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface
        )
    )
    val topBackgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)

    StatusBarColorEffect(topBackgroundColor)

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Equalizer", fontWeight = FontWeight.Bold) },
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.setPreset("flat") },
                        enabled = uiState.isAvailable
                    ) {
                        Icon(
                            Icons.Rounded.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Flat")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding, vertical = AppSpacing.medium),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
            ) {
                EqualizerHeroCard(
                    enabled = uiState.enabled,
                    selectedPresetName = uiState.selectedPresetName,
                    selectedPresetDescription = uiState.selectedPresetDescription,
                    isAvailable = uiState.isAvailable,
                    onToggle = { viewModel.setEnabled(it) }
                )

                AnimatedVisibility(visible = !uiState.isAvailable) {
                    EqualizerNoticeCard()
                }

                PresetsSection(
                    presets = uiState.presets,
                    selectedPresetId = uiState.selectedPresetId,
                    enabled = uiState.enabled && uiState.isAvailable,
                    onPresetClick = { viewModel.setPreset(it) }
                )

                BandsSection(
                    bands = uiState.bands,
                    enabled = uiState.enabled && uiState.isAvailable,
                    onBandChange = { bandId, value -> viewModel.setBandLevel(bandId, value) }
                )

                EnhancementsSection(
                    enabled = uiState.enabled && uiState.isAvailable,
                    bassBoost = uiState.bassBoost,
                    virtualizer = uiState.virtualizer,
                    loudness = uiState.loudness,
                    onBassBoostChange = { viewModel.setBassBoost(it) },
                    onVirtualizerChange = { viewModel.setVirtualizer(it) },
                    onLoudnessChange = { viewModel.setLoudness(it) }
                )

                Spacer(Modifier.height(AppSpacing.xLarge))
            }
        }
    }
}

@Composable
private fun EqualizerHeroCard(
    enabled: Boolean,
    selectedPresetName: String,
    selectedPresetDescription: String,
    isAvailable: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (enabled) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        label = "eq-hero-color"
    )

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = cardColors(containerColor = containerColor),
        elevation = cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(68.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (enabled) "Equalizer On" else "Equalizer Off",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isAvailable) {
                        "Shape your sound with expressive presets and manual tuning."
                    } else {
                        "This device does not expose a controllable equalizer session."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(AppSpacing.small))
                Text(
                    text = "Active preset: $selectedPresetName",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = selectedPresetDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                enabled = isAvailable
            )
        }
    }
}

@Composable
private fun EqualizerNoticeCard() {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        elevation = cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            Icon(
                imageVector = Icons.Rounded.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Equalizer unavailable",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "The device or player session did not grant audio effect control. Try restarting playback.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun PresetsSection(
    presets: List<EqualizerPresetUi>,
    selectedPresetId: String,
    enabled: Boolean,
    onPresetClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
        SectionHeader(
            icon = Icons.Rounded.Waves,
            title = "Presets",
            subtitle = "Pick a sound profile, then fine tune it if you want."
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
            items(presets, key = { it.id }) { preset ->
                PresetCard(
                    preset = preset,
                    selected = preset.id == selectedPresetId,
                    enabled = enabled,
                    onClick = { onPresetClick(preset.id) }
                )
            }
        }
    }
}

@Composable
private fun PresetCard(
    preset: EqualizerPresetUi,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        label = "preset-card-color"
    )

    Card(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        colors = cardColors(containerColor = containerColor),
        elevation = cardElevation(defaultElevation = if (selected) 4.dp else 1.dp),
        modifier = Modifier.width(172.dp)
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.large),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (selected) Icons.Rounded.AutoAwesome else Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                )
                if (selected) {
                    FilterChip(
                        selected = true,
                        onClick = onClick,
                        label = { Text("Active") }
                    )
                }
            }

            Text(
                text = preset.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = preset.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BandsSection(
    bands: List<EqBand>,
    enabled: Boolean,
    onBandChange: (Int, Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
        SectionHeader(
            icon = Icons.Rounded.Tune,
            title = "Frequency Bands",
            subtitle = "Slide each band to sculpt the mix. Changes switch the preset to Custom."
        )

        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            elevation = cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.large),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
            ) {
                bands.forEach { band ->
                    BandControlRow(
                        band = band,
                        enabled = enabled,
                        onValueChange = { onBandChange(band.id, it) }
                    )
                    if (band.id != bands.last().id) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@Composable
private fun BandControlRow(
    band: EqBand,
    enabled: Boolean,
    onValueChange: (Int) -> Unit
) {
    val frequencyText = rememberBandFrequency(band.frequency)
    val levelText = rememberBandDb(band.level)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = frequencyText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = levelText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Band ${band.id + 1}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = band.level.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = band.minLevel.toFloat()..band.maxLevel.toFloat(),
            enabled = enabled,
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.primary,
                thumbColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun EnhancementsSection(
    enabled: Boolean,
    bassBoost: Int,
    virtualizer: Int,
    loudness: Int,
    onBassBoostChange: (Int) -> Unit,
    onVirtualizerChange: (Int) -> Unit,
    onLoudnessChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
            SectionHeader(
                icon = Icons.Rounded.GraphicEq,
                title = "Enhancements",
                subtitle = "Add extra body, width, and loudness where the track needs it."
            )

        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            elevation = cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.large),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
            ) {
                EnhancementSliderCard(
                    icon = Icons.Rounded.GraphicEq,
                    title = "Bass Boost",
                    value = bassBoost,
                    valueSuffix = "%",
                    valueRangeLabel = "0-100%",
                    enabled = enabled,
                    onValueChange = onBassBoostChange,
                    valueRange = 0..1000
                )

                EnhancementSliderCard(
                    icon = Icons.Rounded.AutoAwesome,
                    title = "Spatial Width",
                    value = virtualizer,
                    valueSuffix = "%",
                    valueRangeLabel = "0-100%",
                    enabled = enabled,
                    onValueChange = onVirtualizerChange,
                    valueRange = 0..1000
                )

                EnhancementSliderCard(
                    icon = Icons.Rounded.GraphicEq,
                    title = "Loudness",
                    value = loudness,
                    valueSuffix = "mB",
                    valueRangeLabel = "0-2000 mB",
                    enabled = enabled,
                    onValueChange = onLoudnessChange,
                    valueRange = 0..2000
                )
            }
        }
    }
}

@Composable
private fun EnhancementSliderCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: Int,
    valueSuffix: String,
    valueRangeLabel: String,
    enabled: Boolean,
    onValueChange: (Int) -> Unit,
    valueRange: IntRange
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = valueRangeLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = "${displayEnhancementValue(title, value)}$valueSuffix",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            enabled = enabled,
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.primary,
                thumbColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(10.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun rememberBandFrequency(frequency: Int): String {
    return if (frequency >= 1000) {
        String.format(Locale.US, "%.1fkHz", frequency / 1000f)
    } else {
        "${frequency}Hz"
    }
}

private fun rememberBandDb(level: Int): String {
    return String.format(Locale.US, "%+.1f dB", level / 100f)
}

private fun displayEnhancementValue(title: String, value: Int): String {
    return when (title) {
        "Loudness" -> String.format(Locale.US, "%.0f", value.toFloat())
        else -> String.format(Locale.US, "%.0f", value / 10f)
    }
}
