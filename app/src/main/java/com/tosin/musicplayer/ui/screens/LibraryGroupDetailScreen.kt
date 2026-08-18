package com.tosin.musicplayer.ui.screens

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.tosin.musicplayer.ui.components.CategoryActionsSheet
import com.tosin.musicplayer.ui.components.SongActionsSheet
import com.tosin.musicplayer.ui.components.SongItem
import com.tosin.musicplayer.ui.state.LibraryGroup
import com.tosin.musicplayer.ui.state.LibraryTab
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.theme.standardScreenPadding
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryGroupDetailScreen(
    viewModel: PlayerViewModel,
    tab: LibraryTab,
    groupTitle: String,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: () -> Unit
) {
    val playerState by viewModel.uiState.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val songs = remember(tab, groupTitle, playerState.songs) {
        viewModel.getSongsForGroup(tab, groupTitle)
    }

    var selectedSongForActions by remember { mutableStateOf<Song?>(null) }
    var showCategoryActions by remember { mutableStateOf(false) }

    selectedSongForActions?.let { song ->
        SongActionsSheet(
            song = song,
            playlists = playlists,
            onDismiss = { selectedSongForActions = null },
            onPlay = { target ->
                val index = songs.indexOfFirst { it.id == target.id }
                if (index != -1) viewModel.onSongClick(songs, index) else viewModel.onSongClick(listOf(target), 0)
                onNavigateToPlayer()
            },
            onPlayNext = { target -> viewModel.playNextSongs(listOf(target)) },
            onAddToCurrentPlaylist = { target -> viewModel.addSongsToQueue(listOf(target)) },
            onAddToPlaylist = { playlistId, songIds -> viewModel.addSongsToPlaylist(playlistId, songIds) },
            onCreateNewPlaylist = { name -> viewModel.createPlaylist(name) }
        )
    }

    if (showCategoryActions) {
        val group = remember(tab, groupTitle, songs) {
            LibraryGroup(
                id = "${tab.name}-$groupTitle",
                title = groupTitle,
                subtitle = "${songs.size} songs",
                songCount = songs.size,
                artwork = songs.firstOrNull { it.albumArt != null }?.albumArt,
                songs = songs
            )
        }
        CategoryActionsSheet(
            group = group,
            playlists = playlists,
            onPlayAll = { groupSongs ->
                if (groupSongs.isNotEmpty()) viewModel.onSongClick(groupSongs, 0)
                onNavigateToPlayer()
            },
            onPlayNext = { groupSongs -> viewModel.playNextSongs(groupSongs) },
            onAddToCurrentPlaylist = { groupSongs -> viewModel.addSongsToQueue(groupSongs) },
            onAddToPlaylist = { playlistId, songIds -> viewModel.addSongsToPlaylist(playlistId, songIds) },
            onCreateNewPlaylist = { name -> viewModel.createPlaylist(name) },
            onDismiss = { showCategoryActions = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = groupTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .basicMarquee()
                        )
                        Text(
                            text = "${tab.label} • ${songs.size} ${if (songs.size == 1) "song" else "songs"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showCategoryActions = true }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.PlaylistAdd,
                            contentDescription = "Category actions",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = standardScreenPadding(top = 0.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.itemSpacing)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.cardPadding, vertical = AppSpacing.small),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
                ) {
                    FilledTonalButton(
                        onClick = {
                            if (songs.isNotEmpty()) {
                                viewModel.onSongClick(songs, 0)
                                onNavigateToPlayer()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(AppSpacing.xSmall))
                        Text("Play All")
                    }
                    OutlinedButton(
                        onClick = {
                            if (songs.isNotEmpty()) {
                                val shuffled = songs.shuffled()
                                viewModel.onSongClick(shuffled, 0)
                                onNavigateToPlayer()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(AppSpacing.xSmall))
                        Text("Shuffle")
                    }
                }
            }

            itemsIndexed(
                items = songs,
                key = { _, song -> song.id }
            ) { index, song ->
                SongItem(
                    song = song,
                    isPlaying = playerState.currentSong?.id == song.id,
                    onClick = {
                        viewModel.onSongClick(songs, index)
                        onNavigateToPlayer()
                    },
                    onLongClick = {
                        selectedSongForActions = song
                    },
                    trailingContent = {
                        IconButton(onClick = { selectedSongForActions = song }) {
                            Icon(
                                Icons.Rounded.MoreVert,
                                contentDescription = "Song options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        }
    }
}
