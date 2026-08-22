package com.tosin.musicplayer.ui.screens.misceleneous

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.ui.components.SongActionsSheet
import com.tosin.musicplayer.ui.components.SongItem
import com.tosin.musicplayer.ui.icons.AppIcons
import com.tosin.musicplayer.ui.screens.home.EmptyLibraryState
import com.tosin.musicplayer.ui.screens.home.LibrarySortSheet
import com.tosin.musicplayer.ui.screens.home.LibrarySummary
import com.tosin.musicplayer.ui.state.LibrarySortOption
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.theme.standardScreenPadding
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: PlayerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: () -> Unit
) {
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val playerState by viewModel.uiState.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val favoriteSongs = remember(playerState.songs, favoriteIds) {
        playerState.songs.filter { it.id in favoriteIds }
    }

    var sortBy by remember { mutableStateOf(LibrarySortOption.TitleAz) }
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedSongForActions by remember { mutableStateOf<Song?>(null) }

    selectedSongForActions?.let { song ->
        SongActionsSheet(
            song = song,
            playlists = playlists,
            isFavorite = viewModel.isFavorite(song.id),
            onDismiss = { selectedSongForActions = null },
            onPlay = { target ->
                val index = favoriteSongs.indexOfFirst { it.id == target.id }
                if (index != -1) viewModel.onSongClick(favoriteSongs, index) else viewModel.onSongClick(listOf(target), 0)
                onNavigateToPlayer()
            },
            onPlayNext = { target -> viewModel.playNextSongs(listOf(target)) },
            onAddToCurrentPlaylist = { target -> viewModel.addSongsToQueue(listOf(target)) },
            onAddToPlaylist = { playlistId, songIds -> viewModel.addSongsToPlaylist(playlistId, songIds) },
            onCreateNewPlaylist = { name -> viewModel.createPlaylist(name) },
            onToggleFavorite = { target -> viewModel.toggleFavorite(target.id) }
        )
    }

    if (showSortMenu) {
        LibrarySortSheet(
            selected = sortBy,
            onSelect = { option ->
                sortBy = option
            },
            onDismiss = { showSortMenu = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (favoriteSongs.isNotEmpty()) {
                            val shuffled = favoriteSongs.shuffled()
                            viewModel.onSongClick(shuffled, 0)
                            onNavigateToPlayer()
                        }
                    }) {
                        Icon(AppIcons.Shuffle, contentDescription = "Shuffle")
                    }
                    IconButton(onClick = {
                        if (favoriteSongs.isNotEmpty()) {
                            viewModel.onSongClick(favoriteSongs, 0)
                            onNavigateToPlayer()
                        }
                    }) {
                        Icon(AppIcons.Play, contentDescription = "Play All")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        if (favoriteSongs.isEmpty()) {
            EmptyLibraryState(
                title = "No favorites yet",
                message = "Tap the heart icon on any song to add it to your favorites."
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
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
                    onLongClick = { selectedSongForActions = song }
                )
            }
        }
    }
}
