package com.tosin.musicplayer.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
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
    var actionSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var actionInitialIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showActions by remember { mutableStateOf(false) }

    if (visibleSongs.isEmpty()) {
        EmptyLibraryState(
            title = "No songs found",
            message = "Add music to this device and it will appear here in alphabetical order."
        )
        return
    }

    if (showCreatePlaylistDialog) {
        var playlistName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("New Playlist") },
            text = {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text("Playlist Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (playlistName.isNotBlank()) {
                            viewModel.createPlaylist(playlistName)
                            showCreatePlaylistDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showActions) {
        SongActionsSheet(
            songs = actionSongs,
            initialSelectedIds = actionInitialIds,
            playlists = playlists,
            onDismiss = { showActions = false },
            onAddToQueue = { viewModel.addSongsToQueue(it) },
            onPlayNext = { viewModel.playNextSongs(it) },
            onAddToPlaylist = { playlistId, songIds -> viewModel.addSongsToPlaylist(playlistId, songIds) }
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
        contentPadding = standardScreenPadding(top = 0.dp, bottom = 0.dp),
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
                    actionSongs = visibleSongs
                    actionInitialIds = setOf(song.id)
                    showActions = true
                },
                trailingContent = {
                    IconButton(onClick = {
                        actionSongs = listOf(song)
                        actionInitialIds = setOf(song.id)
                        showActions = true
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.PlaylistAdd, contentDescription = "Actions", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    }
}
