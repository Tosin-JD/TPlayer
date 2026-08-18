package com.tosin.musicplayer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.tosin.musicplayer.data.models.Playlist
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.ui.state.LibraryGroup
import com.tosin.musicplayer.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryActionsSheet(
    group: LibraryGroup,
    playlists: List<Playlist>,
    onPlayAll: (List<Song>) -> Unit,
    onPlayNext: (List<Song>) -> Unit,
    onAddToCurrentPlaylist: (List<Song>) -> Unit,
    onAddToPlaylist: (String, List<Long>) -> Unit,
    onCreateNewPlaylist: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showPlaylistSelection by remember { mutableStateOf(false) }

    if (showPlaylistSelection) {
        PlaylistSelectionSheet(
            playlists = playlists,
            songCount = group.songs.size,
            onPlaylistSelected = { playlist ->
                onAddToPlaylist(playlist.id, group.songs.map { it.id })
                showPlaylistSelection = false
                onDismiss()
            },
            onCreateNewPlaylist = { name ->
                onCreateNewPlaylist(name)
            },
            onDismiss = {
                showPlaylistSelection = false
                onDismiss()
            }
        )
    } else {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.large, vertical = AppSpacing.medium)
            ) {
                Text(
                    text = group.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${group.songs.size} ${if (group.songs.size == 1) "song" else "songs"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(AppSpacing.medium))

                ListItem(
                    headlineContent = { Text("Play category") },
                    leadingContent = { Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable {
                        onPlayAll(group.songs)
                        onDismiss()
                    }
                )

                ListItem(
                    headlineContent = { Text("Play next") },
                    leadingContent = { Icon(Icons.Rounded.SkipNext, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable {
                        onPlayNext(group.songs)
                        onDismiss()
                    }
                )

                ListItem(
                    headlineContent = { Text("Add to current playlist") },
                    leadingContent = { Icon(Icons.Rounded.QueueMusic, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable {
                        onAddToCurrentPlaylist(group.songs)
                        onDismiss()
                    }
                )

                ListItem(
                    headlineContent = { Text("Add to playlist") },
                    leadingContent = { Icon(Icons.AutoMirrored.Rounded.PlaylistAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable {
                        showPlaylistSelection = true
                    }
                )
            }
        }
    }
}
