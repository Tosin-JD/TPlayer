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
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSubHeader("Audio Options")

            ListItem(
                headlineContent = { Text("Gapless Playback") },
                supportingContent = { Text("Seamless transitions between tracks") },
                leadingContent = {
                    Icon(AppIcons.SkipNext, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Switch(
                        checked = uiState.gaplessPlayback,
                        onCheckedChange = { viewModel.toggleGaplessPlayback(it) }
                    )
                }
            )

            ListItem(
                headlineContent = { Text("Crossfade") },
                supportingContent = { Text("Smooth fade between tracks") },
                leadingContent = {
                    Icon(AppIcons.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Switch(
                        checked = uiState.crossfadeEnabled,
                        onCheckedChange = { viewModel.toggleCrossfade(it) }
                    )
                }
            )

            if (uiState.crossfadeEnabled) {
                ListItem(
                    headlineContent = { Text("Crossfade Duration") },
                    supportingContent = { Text("${uiState.crossfadeDuration} seconds") },
                    leadingContent = {
                        Icon(AppIcons.Timelapse, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    },
                    trailingContent = {
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
                    },
                    modifier = Modifier.animateContentSize()
                )
            }

            ListItem(
                headlineContent = { Text("Auto-Resume") },
                supportingContent = { Text("Remember playback position for each track") },
                leadingContent = {
                    Icon(AppIcons.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Switch(
                        checked = uiState.autoResumeEnabled,
                        onCheckedChange = { viewModel.toggleAutoResume(it) }
                    )
                }
            )

            ListItem(
                headlineContent = { Text("Remember Last Play") },
                supportingContent = { Text("Resume the last song and its exact position when the app opens") },
                leadingContent = {
                    Icon(AppIcons.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Switch(
                        checked = uiState.rememberLastPlay,
                        onCheckedChange = { viewModel.toggleRememberLastPlay(it) }
                    )
                }
            )

            ListItem(
                headlineContent = { Text("Pause on 0 Volume") },
                supportingContent = { Text("Pause playback when device volume is muted or zero") },
                leadingContent = {
                    Icon(AppIcons.VolumeOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
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
