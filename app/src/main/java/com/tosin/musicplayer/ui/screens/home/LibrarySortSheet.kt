package com.tosin.musicplayer.ui.screens.home

import androidx.compose.runtime.Composable
import com.tosin.musicplayer.ui.components.menu.SelectionBottomSheet
import com.tosin.musicplayer.ui.components.menu.librarySortOptions
import com.tosin.musicplayer.ui.state.LibrarySortOption

@Composable
internal fun LibrarySortSheet(
    selected: LibrarySortOption,
    onSelect: (LibrarySortOption) -> Unit,
    onDismiss: () -> Unit
) {
    SelectionBottomSheet(
        title = "Sort by",
        options = librarySortOptions(),
        selected = selected,
        onSelect = onSelect,
        onDismiss = onDismiss
    )
}
