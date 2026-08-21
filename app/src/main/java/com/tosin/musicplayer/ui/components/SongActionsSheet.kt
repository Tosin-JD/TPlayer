package com.tosin.musicplayer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.icons.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongActionsSheet(
    song: Song,
    playlists: List<Playlist>,
    isFavorite: Boolean = false,
    onDismiss: () -> Unit,
    onPlay: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToCurrentPlaylist: (Song) -> Unit,
    onAddToPlaylist: (String, List<Long>) -> Unit,
    onCreateNewPlaylist: (String) -> Unit,
    onToggleFavorite: ((Song) -> Unit)? = null,
    onRemoveFromPlaylist: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showPlaylistSelection by remember { mutableStateOf(false) }

    if (showPlaylistSelection) {
        PlaylistSelectionSheet(
            playlists = playlists,
            songCount = 1,
            onPlaylistSelected = { playlist ->
                onAddToPlaylist(playlist.id, listOf(song.id))
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
                    text = song.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "${song.artist} • ${song.album}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )

                Spacer(Modifier.height(AppSpacing.medium))

                ListItem(
                    headlineContent = { Text("Play") },
                    leadingContent = { Icon(AppIcons.Play, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable {
                        onPlay(song)
                        onDismiss()
                    }
                )

                ListItem(
                    headlineContent = { Text("Play next") },
                    leadingContent = { Icon(AppIcons.SkipNext, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable {
                        onPlayNext(song)
                        onDismiss()
                    }
                )

                if (onToggleFavorite != null) {
                    ListItem(
                        headlineContent = { Text(if (isFavorite) "Remove from favorites" else "Add to favorites") },
                        leadingContent = {
                            Icon(
                                if (isFavorite) AppIcons.Favorite else AppIcons.FavoriteBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.clickable {
                            onToggleFavorite(song)
                            onDismiss()
                        }
                    )
                }

                ListItem(
                    headlineContent = { Text("Add to current playlist") },
                    leadingContent = { Icon(AppIcons.Queue, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable {
                        onAddToCurrentPlaylist(song)
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

                if (onRemoveFromPlaylist != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = AppSpacing.small))

                    ListItem(
                        headlineContent = { Text("Remove from playlist", color = MaterialTheme.colorScheme.error) },
                        leadingContent = { Icon(AppIcons.RemoveCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        modifier = Modifier.clickable {
                            onRemoveFromPlaylist()
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}
