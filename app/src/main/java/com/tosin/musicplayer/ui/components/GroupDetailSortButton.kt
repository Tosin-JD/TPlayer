package com.tosin.musicplayer.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.icons.AppIcons

/**
 * A sort button for the TopAppBar in library group detail screens.
 * Equal width and height (40dp) to match the category actions button.
 */
@Composable
fun GroupDetailSortButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp)
    ) {
        Icon(
            AppIcons.Sort,
            contentDescription = "Sort by",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
