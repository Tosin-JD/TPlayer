package com.tosin.musicplayer.ui.screens.equalizer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.icons.AppIcons
import com.tosin.musicplayer.ui.state.EqBand
import com.tosin.musicplayer.ui.theme.AppSpacing
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun BandsSection(
    bands: List<EqBand>,
    enabled: Boolean,
    onBandChange: (Int, Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
        SectionHeader(
            icon = AppIcons.Tune,
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
    val frequencyText = if (band.frequency >= 1000) {
        String.format(Locale.US, "%.1fkHz", band.frequency / 1000f)
    } else {
        "${band.frequency}Hz"
    }
    val levelText = String.format(Locale.US, "%+.1f dB", band.level / 100f)

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
