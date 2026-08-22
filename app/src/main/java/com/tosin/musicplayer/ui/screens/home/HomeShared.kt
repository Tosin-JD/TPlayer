package com.tosin.musicplayer.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.components.StorageScopeBottomSheet
import com.tosin.musicplayer.ui.state.LibraryTab
import com.tosin.musicplayer.ui.state.StorageScope
import com.tosin.musicplayer.ui.components.StorageScopeBottomSheet
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.theme.engine.customAppSurface
import com.tosin.musicplayer.ui.icons.AppIcons

internal data class HomeContentState(
    val selectedTab: LibraryTab,
    val isLoading: Boolean,
    val hasAudioPermission: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibrarySummary(
    title: String,
    subtitle: String,
    onSortClick: () -> Unit,
    sortLabel: String,
    storageScope: StorageScope? = null,
    onStorageSelected: ((StorageScope) -> Unit)? = null,
    availableStorageScopes: List<StorageScope> = emptyList()
) {
    var showStorageSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showStorageSheet && storageScope != null) {
        StorageScopeBottomSheet(
            selected = storageScope,
            availableScopes = availableStorageScopes,
            onSelect = {
                onStorageSelected?.invoke(it)
                showStorageSheet = false
            },
            onDismiss = { showStorageSheet = false },
            sheetState = sheetState
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (storageScope != null && onStorageSelected != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .customAppSurface(
                            shape = MaterialTheme.shapes.small,
                            backgroundColor = MaterialTheme.colorScheme.surface
                        )
                        .clickable { showStorageSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = storageScope.icon(),
                        contentDescription = "Storage: ${storageScope.label}",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.width(8.dp))
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .customAppSurface(
                        shape = MaterialTheme.shapes.small,
                        backgroundColor = MaterialTheme.colorScheme.surface
                    )
                    .clickable(onClick = onSortClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AppIcons.Sort,
                    contentDescription = "Sort by $sortLabel",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun StorageScope.icon(): ImageVector = when (this) {
    StorageScope.Internal -> AppIcons.Person
    StorageScope.SdCard -> AppIcons.Storage
    StorageScope.Both -> AppIcons.Storage
}

@Composable
internal fun PermissionState(
    onRequestAudioPermission: () -> Unit
) {
    EmptyLibraryState(
        title = "Audio permission needed",
        message = "Allow access to local audio so TPlayer can build your library.",
        action = {
            TextButton(onClick = onRequestAudioPermission) {
                Text("Grant access")
            }
        }
    )
}

@Composable
internal fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading your library",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
internal fun EmptyLibraryState(
    title: String,
    message: String,
    action: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.xLarge, vertical = AppSpacing.xLarge),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .customAppSurface(
                        shape = MaterialTheme.shapes.extraLarge,
                        backgroundColor = MaterialTheme.colorScheme.surface
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AppIcons.LibraryMusic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            action?.invoke()
        }
    }
}
