package com.tosin.musicplayer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.data.models.Playlist
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.ui.theme.AppSpacing
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongActionsSheet(
    songs: List<Song>,
    initialSelectedIds: Set<Long>,
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onAddToQueue: (List<Song>) -> Unit,
    onPlayNext: (List<Song>) -> Unit,
    onAddToPlaylist: (String, List<Long>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selected = remember(songs, initialSelectedIds) {
        mutableStateMapOf<Long, Boolean>().apply {
            songs.forEach { put(it.id, it.id in initialSelectedIds) }
        }
    }
    var showPlaylistDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.large, vertical = AppSpacing.medium)) {
            Text(
                text = "Song Actions",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = AppSpacing.small)
            )
            Text(
                text = "Select one or more songs, then choose what to do with them.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small), modifier = Modifier.padding(vertical = AppSpacing.medium)) {
                FilterChip(
                    selected = songs.isNotEmpty() && selected.values.all { it },
                    onClick = {
                        songs.forEach { selected[it.id] = true }
                    },
                    label = { Text("Select all") }
                )
                FilterChip(
                    selected = selected.values.none { it },
                    onClick = {
                        songs.forEach { selected[it.id] = false }
                    },
                    label = { Text("Clear") }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AppSpacing.medium)
            ) {
                items(songs, key = { it.id }) { song ->
                    ListItem(
                        headlineContent = { Text(song.title, maxLines = 1) },
                        supportingContent = { Text("${song.artist} • ${song.album}", maxLines = 1) },
                        leadingContent = {
                            Checkbox(
                                checked = selected[song.id] == true,
                                onCheckedChange = { selected[song.id] = it }
                            )
                        }
                    )
                }
            }

            val selectedSongs = songs.filter { selected[it.id] == true }

            FilledTonalButton(
                onClick = { onAddToQueue(selectedSongs) },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedSongs.isNotEmpty()
            ) {
                Icon(Icons.Rounded.QueueMusic, contentDescription = null)
                Text("Add to Queue")
            }

            TextButton(
                onClick = { onPlayNext(selectedSongs) },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedSongs.isNotEmpty()
            ) {
                Icon(Icons.Rounded.SkipNext, contentDescription = null)
                Text("Play Next")
            }

            TextButton(
                onClick = { showPlaylistDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedSongs.isNotEmpty()
            ) {
                Icon(Icons.AutoMirrored.Rounded.PlaylistAdd, contentDescription = null)
                Text("Add to Playlist")
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }
        }
    }

    if (showPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showPlaylistDialog = false },
            title = { Text("Add to Playlist") },
            text = {
                if (playlists.isEmpty()) {
                    Text("No playlists available. Create one first.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
                        playlists.forEach { playlist ->
                            TextButton(
                                onClick = {
                                    val currentSelectedIds = songs.filter { selected[it.id] == true }.map { it.id }
                                    onAddToPlaylist(playlist.id, currentSelectedIds)
                                    showPlaylistDialog = false
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(playlist.name)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlaylistDialog = false }) { Text("Cancel") }
            }
        )
    }
}

