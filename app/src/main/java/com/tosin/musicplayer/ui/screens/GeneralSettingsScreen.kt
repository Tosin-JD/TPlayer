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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.components.StorageScopeSelector
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.state.StorageScope
import com.tosin.musicplayer.ui.state.hasRemovableStorage
import com.tosin.musicplayer.ui.viewmodel.SettingsEvent
import com.tosin.musicplayer.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import java.io.File

private enum class ScanAction {
    CHANGES,
    FULL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val availableStorageScopes = remember(context) {
        if (context.hasRemovableStorage()) {
            StorageScope.entries.toList()
        } else {
            listOf(StorageScope.Internal)
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    var showResetDialog by remember { mutableStateOf(false) }
    var showFolderPicker by remember { mutableStateOf(false) }
    var showScanDialog by remember { mutableStateOf(false) }
    var pendingScanAction by remember { mutableStateOf(ScanAction.CHANGES) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.ScanFinished -> {
                    showScanDialog = false
                    snackbarHostState.showSnackbar(
                        if (event.isFullScan) {
                            "Full scan finished"
                        } else {
                            "Library scan finished"
                        }
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("General", fontWeight = FontWeight.Bold) },
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
        ) {
            if (uiState.isScanning) {
                val progressFraction = if (uiState.scanTotal > 0) {
                    uiState.scanProgress.toFloat() / uiState.scanTotal.toFloat()
                } else {
                    0f
                }
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.large, vertical = AppSpacing.small),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.scanLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${uiState.scanProgress}/${uiState.scanTotal} items • ${uiState.scanFolderCount} folders",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                Spacer(Modifier.height(AppSpacing.small))

            // ── Notifications ──
            SettingsSubHeader("Notifications")

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

            HorizontalDivider(modifier = Modifier.padding(vertical = AppSpacing.xSmall))

            // ── Library Scanning ──
            SettingsSubHeader("Library")

            ListItem(
                headlineContent = { Text("Scan for Changes") },
                supportingContent = { Text("Check for new or removed songs") },
                leadingContent = {
                    Icon(Icons.Rounded.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    FilledTonalButton(
                        onClick = {
                            pendingScanAction = ScanAction.CHANGES
                            showScanDialog = true
                        }
                    ) {
                        Text("Scan")
                    }
                }
            )

            ListItem(
                headlineContent = { Text("Full Scan") },
                supportingContent = { Text("Re-scan entire music library") },
                leadingContent = {
                    Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    FilledTonalButton(
                        onClick = {
                            pendingScanAction = ScanAction.FULL
                            showScanDialog = true
                        }
                    ) {
                        Text("Full Scan")
                    }
                }
            )

            ListItem(
                headlineContent = { Text("Last Library Scan") },
                supportingContent = { Text(uiState.lastScanDate) },
                leadingContent = {
                    Icon(Icons.Rounded.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            )

            SettingsSubHeader("Storage Source")

            ListItem(
                headlineContent = { Text("Default Library Storage") },
                supportingContent = { Text("Choose which device the library should read from by default") },
                leadingContent = {
                    Icon(Icons.Rounded.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            )
            StorageScopeSelector(
                selected = StorageScope.entries.firstOrNull { it.name == uiState.storageScopeAll } ?: StorageScope.Both,
                onSelected = { viewModel.setStorageScopeForTab("All", it) },
                modifier = Modifier.padding(horizontal = AppSpacing.large, vertical = AppSpacing.small),
                availableScopes = availableStorageScopes
            )

            ListItem(
                headlineContent = { Text("Remember Last Play") },
                supportingContent = { Text("Resume the last song and playback position when the app opens again") },
                leadingContent = {
                    Icon(Icons.Rounded.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Switch(
                        checked = uiState.rememberLastPlay,
                        onCheckedChange = { viewModel.toggleRememberLastPlay(it) }
                    )
                }
            )

            SettingsSubHeader("Excluded Folders")

            ListItem(
                headlineContent = { Text("Manage excluded folders") },
                supportingContent = {
                    if (uiState.excludedFolders.isEmpty()) {
                        Text("All folders are currently included")
                    } else {
                        Text("${uiState.excludedFolders.size} folder(s) excluded")
                    }
                },
                leadingContent = {
                    Icon(Icons.Rounded.FolderOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    FilledTonalButton(onClick = { showFolderPicker = true }) {
                        Text("Choose")
                    }
                }
            )

            Spacer(Modifier.weight(1f))

            // Reset General Settings
            ResetSettingsButton(
                label = "Reset General Settings",
                onClick = { showResetDialog = true }
            )

            Spacer(Modifier.height(AppSpacing.xLarge))
            }
        }
    }

    if (showScanDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isScanning) {
                    showScanDialog = false
                }
            },
            icon = { Icon(Icons.Rounded.Sync, contentDescription = null) },
            title = {
                Text(
                    if (pendingScanAction == ScanAction.FULL) {
                        "Full scan library"
                    } else {
                        "Scan for changes"
                    }
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                    Text(
                        if (pendingScanAction == ScanAction.FULL) {
                            "Full scan rebuilds the library from MediaStore and refreshes cached metadata."
                        } else {
                            "Scan for changes checks for new or removed songs and updates the library."
                        }
                    )
                    Text(
                        "You can move this scan to the background and keep using the app while it finishes.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (uiState.isScanning) {
                        val progressFraction = if (uiState.scanTotal > 0) {
                            uiState.scanProgress.toFloat() / uiState.scanTotal.toFloat()
                        } else {
                            0f
                        }
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "${uiState.scanProgress}/${uiState.scanTotal} items • ${uiState.scanFolderCount} folders",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        uiState.scanCurrentFolder?.let { folder ->
                            Text(
                                text = "Now scanning: $folder",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (pendingScanAction == ScanAction.FULL) {
                            viewModel.fullScan()
                        } else {
                            viewModel.scanForChanges()
                        }
                    },
                    enabled = !uiState.isScanning
                ) {
                    Text("Start scan")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (pendingScanAction == ScanAction.FULL) {
                            viewModel.fullScan()
                        } else {
                            viewModel.scanForChanges()
                        }
                        showScanDialog = false
                    }
                ) {
                    Text("Background scan")
                }
            }
        )
    }

    if (showFolderPicker) {
        ModalBottomSheet(
            onDismissRequest = { showFolderPicker = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.large, vertical = AppSpacing.medium)
            ) {
                Text(
                    text = "Exclude folders",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Songs in excluded folders will disappear everywhere in the app and will not play in playlists.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = AppSpacing.xSmall, bottom = AppSpacing.medium)
                )

                if (uiState.availableFolders.isEmpty()) {
                    Text(
                        text = "No folders found yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = AppSpacing.large)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
                        uiState.availableFolders.forEach { folder ->
                            val selected = uiState.excludedFolders.contains(folder.path)
                            Card(
                                onClick = { viewModel.toggleExcludedFolder(folder.path) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selected) {
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerLow
                                    }
                                )
                            ) {
                                ListItem(
                                    headlineContent = {
                                        Text(folder.label, fontWeight = FontWeight.SemiBold)
                                    },
                                    supportingContent = {
                                        Text(folder.path, maxLines = 1)
                                    },
                                    leadingContent = {
                                        Icon(
                                            Icons.Rounded.Folder,
                                            contentDescription = null,
                                            tint = if (selected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingContent = {
                                        Checkbox(
                                            checked = selected,
                                            onCheckedChange = { viewModel.toggleExcludedFolder(folder.path) }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(AppSpacing.medium))
                TextButton(
                    onClick = { showFolderPicker = false },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = { Icon(Icons.Rounded.RestartAlt, contentDescription = null) },
            title = { Text("Reset General Settings?") },
            text = { Text("This will reset notification and scanning settings to their defaults.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetGeneralSettings()
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

@Composable
internal fun SettingsSubHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = AppSpacing.large, vertical = AppSpacing.medium)
    )
}

@Composable
internal fun ResetSettingsButton(
    label: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.large, vertical = AppSpacing.medium),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        )
    ) {
        Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(AppSpacing.small))
        Text(label, fontWeight = FontWeight.Medium)
    }
}
