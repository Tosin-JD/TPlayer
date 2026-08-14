package com.tosin.musicplayer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.components.StatusBarColorEffect
import com.tosin.musicplayer.ui.screens.equalizer.BandsSection
import com.tosin.musicplayer.ui.screens.equalizer.EnhancementsSection
import com.tosin.musicplayer.ui.screens.equalizer.EqualizerHeroCard
import com.tosin.musicplayer.ui.screens.equalizer.EqualizerNoticeCard
import com.tosin.musicplayer.ui.screens.equalizer.PresetsSection
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.viewmodel.EqualizerViewModel

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
        topBar = {
            TopAppBar(
                title = { Text("Equalizer", fontWeight = FontWeight.Bold) },
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
