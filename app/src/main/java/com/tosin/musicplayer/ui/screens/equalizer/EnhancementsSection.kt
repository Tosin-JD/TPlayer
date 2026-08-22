package com.tosin.musicplayer.ui.screens.equalizer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.icons.AppIcons
import com.tosin.musicplayer.ui.theme.AppSpacing
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun EnhancementsSection(
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
            icon = AppIcons.GraphicEq,
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
                    icon = AppIcons.GraphicEq,
                    title = "Bass Boost",
                    value = bassBoost,
                    valueSuffix = "%",
                    valueRangeLabel = "0-100%",
                    enabled = enabled,
                    onValueChange = onBassBoostChange,
                    valueRange = 0..1000
                )

                EnhancementSliderCard(
                    icon = AppIcons.AutoAwesome,
                    title = "Spatial Width",
                    value = virtualizer,
                    valueSuffix = "%",
                    valueRangeLabel = "0-100%",
                    enabled = enabled,
                    onValueChange = onVirtualizerChange,
                    valueRange = 0..1000
                )

                EnhancementSliderCard(
                    icon = AppIcons.GraphicEq,
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
    icon: ImageVector,
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

private fun displayEnhancementValue(title: String, value: Int): String {
    return when (title) {
        "Loudness" -> String.format(Locale.US, "%.0f", value.toFloat())
        else -> String.format(Locale.US, "%.0f", value / 10f)
    }
}
