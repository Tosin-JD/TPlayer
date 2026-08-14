package com.tosin.musicplayer.ui.screens.player

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreVert
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerMoreOptionsSheet(
    currentSong: Song?,
    playbackSpeed: Float,
    sleepTimerRemaining: Long?,
    abRepeatA: Long?,
    abRepeatB: Long?,
    onDismiss: () -> Unit,
    onOpenSongEditor: (Long) -> Unit,
    onSetAsRingtone: (Song) -> Unit,
    onShowDeleteConfirm: () -> Unit,
    onOpenSpeedDialog: () -> Unit,
    onOpenSleepTimerDialog: () -> Unit,
    onOpenABRepeatDialog: () -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
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

            if (currentSong != null) {
                ListItem(
                    headlineContent = { Text("Edit tags") },
                    supportingContent = { Text("Update title, artist, album and genre") },
                    leadingContent = { Icon(Icons.Rounded.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable {
                        onDismiss()
                        onOpenSongEditor(currentSong.id)
                    }
                )

                ListItem(
                    headlineContent = { Text("Set as ringtone") },
                    supportingContent = { Text("Make this song your default ringtone") },
                    leadingContent = { Icon(Icons.Rounded.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable {
                        onDismiss()
                        onSetAsRingtone(currentSong)
                    }
                )

                ListItem(
                    headlineContent = { Text("Share song") },
                    supportingContent = { Text("Send the audio file to another app") },
                    leadingContent = { Icon(Icons.AutoMirrored.Rounded.PlaylistPlay, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
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

                ListItem(
                    headlineContent = { Text("Delete permanently") },
                    supportingContent = { Text("Remove the file from storage forever") },
                    leadingContent = { Icon(Icons.Rounded.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable {
                        onDismiss()
                        onShowDeleteConfirm()
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = AppSpacing.small))

            ListItem(
                headlineContent = { Text("Playback Speed") },
                supportingContent = { Text("${playbackSpeed}x") },
                leadingContent = { Icon(Icons.Rounded.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.clickable {
                    onDismiss()
                    onOpenSpeedDialog()
                }
            )

            ListItem(
                headlineContent = { Text("Sleep Timer") },
                supportingContent = {
                    if (sleepTimerRemaining != null) {
                        Text("${sleepTimerRemaining / 60000} mins remaining")
                    } else {
                        Text("Off")
                    }
                },
                leadingContent = { Icon(Icons.Rounded.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.clickable {
                    onDismiss()
                    onOpenSleepTimerDialog()
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
                leadingContent = { Icon(Icons.Rounded.RepeatOneOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.clickable {
                    onDismiss()
                    onOpenABRepeatDialog()
                }
            )
        }
    }
}
