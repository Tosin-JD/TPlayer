package com.tosin.musicplayer.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.viewmodel.SettingsViewModel
import com.tosin.musicplayer.ui.icons.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playback", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            SettingsSubHeader("Audio Options")

            ThemedSettingsItem(
                icon = AppIcons.SkipNext,
                title = "Gapless Playback",
                subtitle = "Seamless transitions between tracks",
                trailing = {
                    Switch(
                        checked = uiState.gaplessPlayback,
                        onCheckedChange = { viewModel.toggleGaplessPlayback(it) }
                    )
                }
            )

            ThemedSettingsItem(
                icon = AppIcons.Tune,
                title = "Crossfade",
                subtitle = "Smooth fade between tracks",
                trailing = {
                    Switch(
                        checked = uiState.crossfadeEnabled,
                        onCheckedChange = { viewModel.toggleCrossfade(it) }
                    )
                }
            )

            if (uiState.crossfadeEnabled) {
                ThemedSettingsItem(
                    icon = AppIcons.Timelapse,
                    title = "Crossfade Duration",
                    subtitle = "${uiState.crossfadeDuration} seconds",
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                if (uiState.crossfadeDuration > 1) viewModel.setCrossfadeDuration(uiState.crossfadeDuration - 1)
                            }) {
                                Icon(AppIcons.Remove, contentDescription = "Decrease")
                            }
                            Text(
                                "${uiState.crossfadeDuration}s",
                                style = MaterialTheme.typography.titleMedium
                            )
                            IconButton(onClick = {
                                if (uiState.crossfadeDuration < 12) viewModel.setCrossfadeDuration(uiState.crossfadeDuration + 1)
                            }) {
                                Icon(AppIcons.Add, contentDescription = "Increase")
                            }
                        }
                    }
                )
            }

            ThemedSettingsItem(
                icon = AppIcons.Restore,
                title = "Auto-Resume",
                subtitle = "Remember playback position for each track",
                trailing = {
                    Switch(
                        checked = uiState.autoResumeEnabled,
                        onCheckedChange = { viewModel.toggleAutoResume(it) }
                    )
                }
            )

            ThemedSettingsItem(
                icon = AppIcons.History,
                title = "Remember Last Play",
                subtitle = "Resume the last song and its exact position when the app opens",
                trailing = {
                    Switch(
                        checked = uiState.rememberLastPlay,
                        onCheckedChange = { viewModel.toggleRememberLastPlay(it) }
                    )
                }
            )

            ThemedSettingsItem(
                icon = AppIcons.VolumeOff,
                title = "Pause on 0 Volume",
                subtitle = "Pause playback when device volume is muted or zero",
                trailing = {
                    Switch(
                        checked = uiState.pauseOnZeroVolume,
                        onCheckedChange = { viewModel.togglePauseOnZeroVolume(it) }
                    )
                }
            )

            Spacer(Modifier.weight(1f))

            ResetSettingsButton(
                label = "Reset Playback Settings",
                onClick = { showResetDialog = true }
            )

            Spacer(Modifier.height(AppSpacing.xLarge))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = { Icon(AppIcons.RestartAlt, contentDescription = null) },
            title = { Text("Reset Playback Settings?") },
            text = { Text("This will reset gapless, crossfade, and resume settings to their defaults.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetPlaybackSettings()
                    showResetDialog = false
                }) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }
}
