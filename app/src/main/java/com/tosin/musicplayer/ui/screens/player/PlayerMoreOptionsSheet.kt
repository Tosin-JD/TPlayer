package com.tosin.musicplayer.ui.screens.player

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PauseCircleOutline
import androidx.compose.material.icons.rounded.RepeatOneOn
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.icons.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerMoreOptionsSheet(
    currentSong: Song?,
    playbackSpeed: Float,
    sleepTimerRemaining: Long?,
    abRepeatA: Long?,
    abRepeatB: Long?,
    isFavorite: Boolean,
    playerBackgroundColor: androidx.compose.ui.graphics.Color,
    onDismiss: () -> Unit,
    onOpenSongEditor: (Long) -> Unit,
    onSetAsRingtone: (Song) -> Unit,
    onShowDeleteConfirm: () -> Unit,
    onOpenSpeedDialog: () -> Unit,
    onOpenSleepTimerDialog: () -> Unit,
    onOpenABRepeatDialog: () -> Unit,
    onStopAfterCurrentSong: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = playerBackgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppSpacing.xLarge)
        ) {
            Text(
                text = "More Options",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = AppSpacing.large, vertical = AppSpacing.medium)
            )

            // 1. Favorite / Love toggle
            ListItem(
                headlineContent = { Text(if (isFavorite) "Remove from favorites" else "Add to favorites") },
                supportingContent = { Text(if (isFavorite) "This song is in your favorites" else "Love this song") },
                leadingContent = {
                    Icon(
                        if (isFavorite) AppIcons.Favorite else AppIcons.FavoriteBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.clickable {
                    onDismiss()
                    onToggleFavorite()
                }
            )

            // 2. Stop playing after this song
            ListItem(
                headlineContent = { Text("Stop playing after this song") },
                supportingContent = { Text("Pause automatically when the current track finishes") },
                leadingContent = { Icon(AppIcons.PauseCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.clickable {
                    onDismiss()
                    onStopAfterCurrentSong()
                }
            )

            // 3. Sleep timer
            ListItem(
                headlineContent = { Text("Sleep timer") },
                supportingContent = {
                    if (sleepTimerRemaining != null) {
                        Text("${sleepTimerRemaining / 60000} mins remaining")
                    } else {
                        Text("Set auto-off timer")
                    }
                },
                leadingContent = { Icon(AppIcons.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.clickable {
                    onDismiss()
                    onOpenSleepTimerDialog()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = AppSpacing.small))

            if (currentSong != null) {
                ListItem(
                    headlineContent = { Text("Edit tags") },
                    supportingContent = { Text("Update title, artist, album and genre") },
                    leadingContent = { Icon(AppIcons.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable {
                        onDismiss()
                        onOpenSongEditor(currentSong.id)
                    }
                )

                ListItem(
                    headlineContent = { Text("Set as ringtone") },
                    supportingContent = { Text("Make this song your default ringtone") },
                    leadingContent = { Icon(AppIcons.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable {
                        onDismiss()
                        onSetAsRingtone(currentSong)
                    }
                )

                ListItem(
                    headlineContent = { Text("Share song") },
                    supportingContent = { Text("Send the audio file to another app") },
                    leadingContent = { Icon(AppIcons.PlaylistPlay, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable {
                        onDismiss()
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "audio/*"
                            putExtra(Intent.EXTRA_STREAM, Uri.parse(currentSong.uri))
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share song"))
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = AppSpacing.small))

            ListItem(
                headlineContent = { Text("Playback Speed") },
                supportingContent = { Text("${playbackSpeed}x") },
                leadingContent = { Icon(AppIcons.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.clickable {
                    onDismiss()
                    onOpenSpeedDialog()
                }
            )

            ListItem(
                headlineContent = { Text("A-B Repeat") },
                supportingContent = {
                    if (abRepeatA != null || abRepeatB != null) {
                        val a = abRepeatA?.let { formatTime(it) } ?: "—"
                        val b = abRepeatB?.let { formatTime(it) } ?: "—"
                        Text("Active: $a to $b")
                    } else {
                        Text("Off")
                    }
                },
                leadingContent = { Icon(AppIcons.RepeatOneOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.clickable {
                    onDismiss()
                    onOpenABRepeatDialog()
                }
            )

            if (currentSong != null) {
                ListItem(
                    headlineContent = { Text("Delete permanently", color = MaterialTheme.colorScheme.error) },
                    supportingContent = { Text("Remove the file from storage forever") },
                    leadingContent = { Icon(AppIcons.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable {
                        onDismiss()
                        onShowDeleteConfirm()
                    }
                )
            }
        }
    }
}
