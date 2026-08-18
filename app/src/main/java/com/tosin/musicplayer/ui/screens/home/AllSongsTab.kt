package com.tosin.musicplayer.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.ui.components.SongActionsSheet
import com.tosin.musicplayer.ui.components.SongItem
import com.tosin.musicplayer.ui.components.StorageScopeSelector
import com.tosin.musicplayer.ui.state.HomeUiState
import com.tosin.musicplayer.ui.state.LibrarySortOption
import com.tosin.musicplayer.ui.state.LibraryTab
import com.tosin.musicplayer.ui.state.StorageScope
import com.tosin.musicplayer.ui.state.matchesStorageScope
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.theme.standardScreenPadding
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import com.tosin.musicplayer.ui.viewmodel.SettingsViewModel

@Composable
internal fun AllSongsTab(
    uiState: HomeUiState,
    viewModel: PlayerViewModel,
    settingsViewModel: SettingsViewModel,
    tab: LibraryTab,
    onNavigateToPlayer: () -> Unit,
    sortBy: LibrarySortOption,
    onSortChange: (LibrarySortOption) -> Unit,
    availableStorageScopes: List<StorageScope>
) {
    val playerState by viewModel.uiState.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }
    val storageScope = remember(settingsViewModel.uiState.collectAsState().value, availableStorageScopes) {
        val scope = settingsViewModel.getStorageScopeForTab(tab.label)
        if (scope !in availableStorageScopes && availableStorageScopes.isNotEmpty()) {
            availableStorageScopes.first()
        } else {
            scope
        }
    }
    val visibleSongs = remember(uiState.songs, sortBy, storageScope) {
        sortSongs(uiState.songs.filter { it.matchesStorageScope(storageScope) }, sortBy)
    }
    var selectedSongForActions by remember { mutableStateOf<Song?>(null) }

    if (visibleSongs.isEmpty()) {
        EmptyLibraryState(
            title = "No songs found",
            message = "Add music to this device and it will appear here in alphabetical order."
        )
        return
    }

    selectedSongForActions?.let { song ->
        SongActionsSheet(
            song = song,
            playlists = playlists,
            onDismiss = { selectedSongForActions = null },
            onPlay = { target ->
                val index = visibleSongs.indexOfFirst { it.id == target.id }
                if (index != -1) viewModel.onSongClick(visibleSongs, index) else viewModel.onSongClick(listOf(target), 0)
                onNavigateToPlayer()
            },
            onPlayNext = { target -> viewModel.playNextSongs(listOf(target)) },
            onAddToCurrentPlaylist = { target -> viewModel.addSongsToQueue(listOf(target)) },
            onAddToPlaylist = { playlistId, songIds -> viewModel.addSongsToPlaylist(playlistId, songIds) },
            onCreateNewPlaylist = { name -> viewModel.createPlaylist(name) }
        )
    }

    if (showSortMenu) {
        LibrarySortSheet(
            selected = sortBy,
            onSelect = onSortChange,
            onDismiss = { showSortMenu = false }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = standardScreenPadding(top = 0.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.itemSpacing)
    ) {
        item {
            LibrarySummary(
                title = "All songs",
                subtitle = "${visibleSongs.size} songs",
                onSortClick = { showSortMenu = true },
                sortLabel = sortBy.label
            )
            if (availableStorageScopes.size > 1) {
                StorageScopeSelector(
                    selected = storageScope,
                    onSelected = { settingsViewModel.setStorageScopeForTab(tab.label, it) },
                    modifier = Modifier.padding(top = AppSpacing.small, bottom = AppSpacing.small),
                    availableScopes = availableStorageScopes
                )
            }
        }

        itemsIndexed(
            items = visibleSongs,
            key = { _, song -> song.id }
        ) { index, song ->
            SongItem(
                song = song,
                isPlaying = playerState.currentSong?.id == song.id,
                onClick = {
                    viewModel.onSongClick(visibleSongs, index)
                    onNavigateToPlayer()
                },
                onLongClick = {
                    selectedSongForActions = song
                },
                trailingContent = {
                    IconButton(onClick = { selectedSongForActions = song }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "Song options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    }
}
