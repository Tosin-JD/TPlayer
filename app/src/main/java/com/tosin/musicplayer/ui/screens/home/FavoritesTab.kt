package com.tosin.musicplayer.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.tosin.musicplayer.ui.icons.AppIcons
import com.tosin.musicplayer.ui.state.LibrarySortOption
import com.tosin.musicplayer.ui.state.LibraryTab
import com.tosin.musicplayer.ui.state.StorageScope
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.theme.standardScreenPadding
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel

@Composable
internal fun FavoritesTab(
    viewModel: PlayerViewModel,
    tab: LibraryTab,
    onNavigateToPlayer: () -> Unit,
    sortBy: LibrarySortOption,
    onSortChange: (LibrarySortOption) -> Unit,
    availableStorageScopes: List<StorageScope>
) {
    val playerState by viewModel.uiState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val allSongs = playerState.songs
    val favoriteSongs = remember(allSongs, favoriteIds) {
        allSongs.filter { it.id in favoriteIds }
    }

    var showSortMenu by remember { mutableStateOf(false) }
    var selectedSongForActions by remember { mutableStateOf<Song?>(null) }

    if (favoriteSongs.isEmpty()) {
        EmptyLibraryState(
            title = "No favorites yet",
            message = "Tap the heart icon on any song to add it to your favorites."
        )
        return
    }

    selectedSongForActions?.let { song ->
        SongActionsSheet(
            song = song,
            playlists = playlists,
            onDismiss = { selectedSongForActions = null },
            onPlay = { target ->
                val index = favoriteSongs.indexOfFirst { it.id == target.id }
                if (index != -1) viewModel.onSongClick(favoriteSongs, index) else viewModel.onSongClick(listOf(target), 0)
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
                title = "Favorites",
                subtitle = "${favoriteSongs.size} songs",
                onSortClick = { showSortMenu = true },
                sortLabel = sortBy.label
            )
        }

        itemsIndexed(favoriteSongs, key = { _, song -> song.id }) { index, song ->
            SongItem(
                song = song,
                isPlaying = playerState.currentSong?.id == song.id,
                onClick = {
                    viewModel.onSongClick(favoriteSongs, index)
                    onNavigateToPlayer()
                },
                onLongClick = { selectedSongForActions = song },
                trailingContent = {
                    IconButton(onClick = { viewModel.toggleFavorite(song.id) }) {
                        Icon(
                            AppIcons.Favorite,
                            contentDescription = "Remove from favorites",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    }
}
