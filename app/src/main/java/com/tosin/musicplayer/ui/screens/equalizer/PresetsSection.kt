package com.tosin.musicplayer.ui.screens.equalizer

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.icons.AppIcons
import com.tosin.musicplayer.ui.state.EqualizerPresetUi
import com.tosin.musicplayer.ui.theme.AppSpacing

@Composable
fun SectionHeader(
    icon: ImageVector,
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

@Composable
fun PresetsSection(
    presets: List<EqualizerPresetUi>,
    selectedPresetId: String,
    enabled: Boolean,
    onPresetClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
        SectionHeader(
            icon = AppIcons.Waves,
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
                    imageVector = if (selected) AppIcons.AutoAwesome else AppIcons.MusicNote,
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
