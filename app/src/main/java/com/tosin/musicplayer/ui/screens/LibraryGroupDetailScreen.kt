package com.tosin.musicplayer.ui.screens

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.data.models.Playlist
import com.tosin.musicplayer.data.models.Song
import androidx.compose.foundation.lazy.items
import com.tosin.musicplayer.ui.components.SongActionsSheet
import com.tosin.musicplayer.ui.components.SongItem
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
    val songs = remember(tab, groupTitle) {
        viewModel.getSongsForGroup(tab, groupTitle)
    }
    val playerState by viewModel.uiState.collectAsState()
    val playlists by viewModel.playlists.collectAsState()

    // SongActionsSheet state (for single song long-press or group action)
    var actionSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var actionInitialIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showActions by remember { mutableStateOf(false) }

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = groupTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
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
                    // "Add all to..." button for the entire group
                    IconButton(onClick = {
                        actionSongs = songs
                        actionInitialIds = songs.map { it.id }.toSet()
                        showActions = true
                    }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.PlaylistAdd,
                            contentDescription = "Add all to playlist",
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
            contentPadding = standardScreenPadding(top = 0.dp),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.itemSpacing)
        ) {
            // Play all / Shuffle all row
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
                        actionSongs = songs
                        actionInitialIds = setOf(song.id)
                        showActions = true
                    },
                    trailingContent = {
                        IconButton(onClick = {
                            actionSongs = listOf(song)
                            actionInitialIds = setOf(song.id)
                            showActions = true
                        }) {
                            Icon(
                                Icons.AutoMirrored.Rounded.PlaylistAdd,
                                contentDescription = "Actions",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        }
    }
}
