package com.tosin.musicplayer.ui.screens

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.data.models.Playlist
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
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
    var songToAdd by remember { mutableStateOf<com.tosin.musicplayer.data.models.Song?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    if (songToAdd != null) {
        AlertDialog(
            onDismissRequest = { songToAdd = null },
            title = { Text("Add to Playlist") },
            text = {
                if (playlists.isEmpty()) {
                    Text("No playlists available. Create one first.")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(playlists) { playlist ->
                            TextButton(
                                onClick = {
                                    viewModel.addSongToPlaylist(playlist.id, songToAdd!!.id)
                                    songToAdd = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(playlist.name, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    songToAdd = null
                    showCreatePlaylistDialog = true
                }) {
                    Text("Create New Playlist")
                }
            },
            dismissButton = {
                TextButton(onClick = { songToAdd = null }) { Text("Cancel") }
            }
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
                            text = tab.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
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
                    trailingContent = {
                        IconButton(onClick = { songToAdd = song }) {
                            Icon(androidx.compose.material.icons.Icons.AutoMirrored.Rounded.PlaylistAdd, contentDescription = "Add to playlist", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            }
        }
    }
}
