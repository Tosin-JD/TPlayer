package com.tosin.musicplayer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tosin.musicplayer.ui.state.StorageScope
import com.tosin.musicplayer.ui.theme.AppSpacing

@Composable
fun StorageScopeSelector(
    selected: StorageScope,
    onSelected: (StorageScope) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
        StorageScope.entries.forEach { scope ->
            FilterChip(
                selected = selected == scope,
                onClick = { onSelected(scope) },
                label = { Text(scope.label) }
            )
        }
    }
}
