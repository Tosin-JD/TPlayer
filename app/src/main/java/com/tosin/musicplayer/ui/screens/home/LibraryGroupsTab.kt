package com.tosin.musicplayer.ui.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.components.CategoryActionsSheet
import com.tosin.musicplayer.ui.components.StorageScopeSelector
import com.tosin.musicplayer.ui.state.LibraryGroup
import com.tosin.musicplayer.ui.state.LibrarySortOption
import com.tosin.musicplayer.ui.state.LibraryTab
import com.tosin.musicplayer.ui.state.StorageScope
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.theme.engine.customAppSurface
import com.tosin.musicplayer.ui.theme.standardScreenPadding
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import com.tosin.musicplayer.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LibraryGroupsTab(
    tab: LibraryTab,
    groups: List<LibraryGroup>,
    playerViewModel: PlayerViewModel,
    settingsViewModel: SettingsViewModel,
    sortBy: LibrarySortOption,
    onSortChange: (LibrarySortOption) -> Unit,
    onGroupClick: (LibraryGroup) -> Unit,
    availableStorageScopes: List<StorageScope>
) {
    val playlists by playerViewModel.playlists.collectAsState()
    val storageScope = remember(settingsViewModel.uiState.collectAsState().value, availableStorageScopes) {
        val scope = settingsViewModel.getStorageScopeForTab(tab.label)
        if (scope !in availableStorageScopes && availableStorageScopes.isNotEmpty()) {
            availableStorageScopes.first()
        } else {
            scope
        }
    }
    val sortedGroups = remember(groups, sortBy, storageScope) {
        sortLibraryGroups(filterGroupsForStorage(groups, storageScope), sortBy)
    }
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedGroupForActions by remember { mutableStateOf<LibraryGroup?>(null) }

    if (groups.isEmpty()) {
        EmptyLibraryState(
            title = "Nothing in ${tab.label.lowercase()} yet",
            message = "Once audio metadata is available, it will show up here."
        )
        return
    }

    if (showSortMenu) {
        LibrarySortSheet(
            selected = sortBy,
            onSelect = onSortChange,
            onDismiss = { showSortMenu = false }
        )
    }

    selectedGroupForActions?.let { group ->
        CategoryActionsSheet(
            group = group,
            playlists = playlists,
            onPlayAll = { songs ->
                if (songs.isNotEmpty()) {
                    playerViewModel.onSongClick(songs, 0)
                }
            },
            onPlayNext = { songs ->
                playerViewModel.playNextSongs(songs)
            },
            onAddToCurrentPlaylist = { songs ->
                playerViewModel.addSongsToQueue(songs)
            },
            onAddToPlaylist = { playlistId, songIds ->
                playerViewModel.addSongsToPlaylist(playlistId, songIds)
            },
            onCreateNewPlaylist = { name ->
                playerViewModel.createPlaylist(name)
            },
            onAddAllToFavorites = { songs ->
                songs.forEach { song ->
                    if (!playerViewModel.isFavorite(song.id)) {
                        playerViewModel.toggleFavorite(song.id)
                    }
                }
            },
            onDismiss = { selectedGroupForActions = null }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = standardScreenPadding(top = 0.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.itemSpacing)
    ) {
        item {
            LibrarySummary(
                title = tab.label,
                subtitle = "${sortedGroups.size} ${if (sortedGroups.size == 1) "section" else "sections"}",
                onSortClick = { showSortMenu = true },
                sortLabel = sortBy.label,
                storageScope = if (availableStorageScopes.size > 1) storageScope else null,
                onStorageSelected = if (availableStorageScopes.size > 1) {{ scope -> settingsViewModel.setStorageScopeForTab(tab.label, scope) }} else null,
                availableStorageScopes = availableStorageScopes
            )
        }

        items(sortedGroups, key = { it.id }) { group ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .customAppSurface(
                        shape = MaterialTheme.shapes.extraLarge,
                        backgroundColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                    .combinedClickable(
                        onClick = { onGroupClick(group) },
                        onLongClick = { selectedGroupForActions = group }
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.cardPadding, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .customAppSurface(
                                shape = MaterialTheme.shapes.large,
                                backgroundColor = MaterialTheme.colorScheme.surface
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = group.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = group.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                    }

                    Text(
                        text = group.songCount.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
