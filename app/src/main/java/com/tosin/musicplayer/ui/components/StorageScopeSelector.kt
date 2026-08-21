package com.tosin.musicplayer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import com.tosin.musicplayer.ui.icons.AppIcons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.state.StorageScope
import com.tosin.musicplayer.ui.theme.AppSpacing
import kotlinx.coroutines.launch

private fun StorageScope.icon(): ImageVector = when (this) {
    StorageScope.Internal -> AppIcons.Person
    StorageScope.SdCard -> AppIcons.Storage
    StorageScope.Both -> AppIcons.Storage
}

private fun StorageScope.description(): String = when (this) {
    StorageScope.Internal -> "Show songs from internal storage only"
    StorageScope.SdCard -> "Show songs from SD card only"
    StorageScope.Both -> "Show songs from all storage"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageScopeSelector(
    selected: StorageScope,
    onSelected: (StorageScope) -> Unit,
    modifier: Modifier = Modifier,
    availableScopes: List<StorageScope> = StorageScope.entries
) {
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    FilterChip(
        selected = false,
        onClick = { showSheet = true },
        label = { Text(selected.label) },
        modifier = modifier
    )

    if (showSheet) {
        StorageScopeBottomSheet(
            selected = selected,
            availableScopes = availableScopes,
            onSelect = {
                onSelected(it)
                showSheet = false
            },
            onDismiss = { showSheet = false },
            sheetState = sheetState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageScopeBottomSheet(
    selected: StorageScope,
    availableScopes: List<StorageScope>,
    onSelect: (StorageScope) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.large, vertical = AppSpacing.medium)
        ) {
            Text(
                text = "Storage Source",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Choose which device to show songs from",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = AppSpacing.xSmall, bottom = AppSpacing.large)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                availableScopes.forEach { scope ->
                    val isSelected = scope == selected
                    StorageScopeOption(
                        scope = scope,
                        isSelected = isSelected,
                        onClick = { onSelect(scope) }
                    )
                }
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
private fun StorageScopeOption(
    scope: StorageScope,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    val bgColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        color = bgColor,
        border = BorderStroke(1.5.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Icon(
                scope.icon(),
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scope.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                )
                Text(
                    text = scope.description(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
