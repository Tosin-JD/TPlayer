package com.tosin.musicplayer.ui.screens

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
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Appearance", fontWeight = FontWeight.Bold) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
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
        ) {
            // ── Theme ──
            SettingsSubHeader("Theme")

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

            HorizontalDivider(modifier = Modifier.padding(vertical = AppSpacing.xSmall))

            // ── Layout ──
            SettingsSubHeader("Layout")

            // Reorder tabs
            ListItem(
                headlineContent = { Text("Home Screen Tabs") },
                supportingContent = { Text("Reorder and toggle tabs on the home screen") },
                leadingContent = {
                    Icon(Icons.Rounded.ViewList, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            )

            Column(modifier = Modifier.padding(horizontal = AppSpacing.large)) {
                uiState.tabOrder.forEachIndexed { index, tab ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = uiState.visibleTabs.contains(tab),
                            onCheckedChange = { viewModel.toggleTabVisibility(tab) }
                        )
                        Text(tab, modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { if (index > 0) viewModel.reorderTab(index, index - 1) },
                            enabled = index > 0
                        ) {
                            Icon(Icons.Rounded.ArrowUpward, contentDescription = "Move Up")
                        }
                        IconButton(
                            onClick = { if (index < uiState.tabOrder.size - 1) viewModel.reorderTab(index, index + 1) },
                            enabled = index < uiState.tabOrder.size - 1
                        ) {
                            Icon(Icons.Rounded.ArrowDownward, contentDescription = "Move Down")
                        }
                    }
                }
            }


            Spacer(Modifier.weight(1f))

            // Reset Appearance Settings
            ResetSettingsButton(
                label = "Reset Appearance",
                onClick = { showResetDialog = true }
            )

            Spacer(Modifier.height(AppSpacing.xLarge))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = { Icon(Icons.Rounded.RestartAlt, contentDescription = null) },
            title = { Text("Reset Appearance?") },
            text = { Text("This will reset your theme, colors, and tab layouts to defaults.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetAppearanceSettings()
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
