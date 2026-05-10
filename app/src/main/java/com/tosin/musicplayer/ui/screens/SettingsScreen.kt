package com.tosin.musicplayer.ui.screens

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                windowInsets = WindowInsets(0.dp),
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Appearance ──
            SettingsSectionHeader("Appearance")

            ListItem(
                headlineContent = { Text("Dark Mode") },
                supportingContent = { Text("Adjust the app theme for low light") },
                leadingContent = {
                    Icon(Icons.Rounded.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Switch(
                        checked = uiState.isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                }
            )

            ListItem(
                headlineContent = { Text("Dynamic Color") },
                supportingContent = { Text("Use colors from your wallpaper (Android 12+)") },
                leadingContent = {
                    Icon(Icons.Rounded.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Switch(
                        checked = uiState.useDynamicColor,
                        onCheckedChange = { viewModel.toggleDynamicColor(it) }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ── Playback ──
            SettingsSectionHeader("Playback")

            ListItem(
                headlineContent = { Text("Gapless Playback") },
                supportingContent = { Text("Seamless transitions between tracks") },
                leadingContent = {
                    Icon(Icons.Rounded.SkipNext, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
                    Icon(Icons.Rounded.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
                        Icon(Icons.Rounded.Timelapse, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                if (uiState.crossfadeDuration > 1) viewModel.setCrossfadeDuration(uiState.crossfadeDuration - 1)
                            }) {
                                Icon(Icons.Rounded.Remove, contentDescription = "Decrease")
                            }
                            Text(
                                "${uiState.crossfadeDuration}s",
                                style = MaterialTheme.typography.titleMedium
                            )
                            IconButton(onClick = {
                                if (uiState.crossfadeDuration < 12) viewModel.setCrossfadeDuration(uiState.crossfadeDuration + 1)
                            }) {
                                Icon(Icons.Rounded.Add, contentDescription = "Increase")
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
                    Icon(Icons.Rounded.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Switch(
                        checked = uiState.autoResumeEnabled,
                        onCheckedChange = { viewModel.toggleAutoResume(it) }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ── Notifications ──
            SettingsSectionHeader("Notifications")

            ListItem(
                headlineContent = { Text("Media Notifications") },
                supportingContent = { Text("Show playback controls in notification") },
                leadingContent = {
                    Icon(Icons.Rounded.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Switch(
                        checked = uiState.showNotifications,
                        onCheckedChange = { viewModel.toggleNotifications(it) }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ── About ──
            SettingsSectionHeader("About")

            ListItem(
                headlineContent = { Text("TPlayer") },
                supportingContent = { Text("Version 1.0 • Alpha") },
                leadingContent = {
                    Icon(Icons.Rounded.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            )

            ListItem(
                headlineContent = { Text("Last Library Scan") },
                supportingContent = { Text(uiState.lastScanDate) },
                leadingContent = {
                    Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}