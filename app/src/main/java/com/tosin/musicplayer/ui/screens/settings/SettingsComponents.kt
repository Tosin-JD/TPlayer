package com.tosin.musicplayer.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.tosin.musicplayer.ui.state.FolderEntry
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.icons.AppIcons

enum class ScanAction {
    CHANGES,
    FULL
}

@Composable
fun ScanDialog(
    pendingScanAction: ScanAction,
    isScanning: Boolean,
    scanProgress: Int,
    scanTotal: Int,
    scanFolderCount: Int,
    scanCurrentFolder: String?,
    onStartScan: () -> Unit,
    onBackgroundScan: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!isScanning) onDismiss()
        },
        icon = { Icon(AppIcons.Sync, contentDescription = null) },
        title = {
            Text(if (pendingScanAction == ScanAction.FULL) "Full scan library" else "Scan for changes")
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
                if (isScanning) {
                    val progressFraction = if (scanTotal > 0) scanProgress.toFloat() / scanTotal.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "$scanProgress/$scanTotal items • $scanFolderCount folders",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    scanCurrentFolder?.let { folder ->
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
                onClick = onStartScan,
                enabled = !isScanning
            ) {
                Text("Start scan")
            }
        },
        dismissButton = {
            TextButton(onClick = onBackgroundScan) {
                Text("Background scan")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcludedFolderBottomSheet(
    availableFolders: List<FolderEntry>,
    excludedFolders: Set<String>,
    onToggleExcludedFolder: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
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

            if (availableFolders.isEmpty()) {
                Text(
                    text = "No folders found yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = AppSpacing.large)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
                    availableFolders.forEach { folder ->
                        val selected = excludedFolders.contains(folder.path)
                        Card(
                            onClick = { onToggleExcludedFolder(folder.path) },
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
                                headlineContent = { Text(folder.label, fontWeight = FontWeight.SemiBold) },
                                supportingContent = { Text(folder.path, maxLines = 1) },
                                leadingContent = {
                                    Icon(
                                        AppIcons.Folder,
                                        contentDescription = null,
                                        tint = if (selected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingContent = {
                                    Checkbox(
                                        checked = selected,
                                        onCheckedChange = { onToggleExcludedFolder(folder.path) }
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(AppSpacing.medium))
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Close")
            }
        }
    }
}
