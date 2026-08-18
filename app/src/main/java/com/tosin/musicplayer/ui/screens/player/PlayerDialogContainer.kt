package com.tosin.musicplayer.ui.screens.player

import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.tosin.musicplayer.ui.state.PlayerUiState
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import com.tosin.musicplayer.ui.viewmodel.RepeatMode

@Composable
fun PlayerDialogContainer(
    state: PlayerUiState,
    viewModel: PlayerViewModel,
    context: Context,
    showSpeedDialog: Boolean,
    showSleepTimerDialog: Boolean,
    showMoreOptionsSheet: Boolean,
    showABRepeatDialog: Boolean,
    showDeleteConfirm: Boolean,
    onDismissSpeed: () -> Unit,
    onDismissSleep: () -> Unit,
    onDismissMoreOptions: () -> Unit,
    onDismissABRepeat: () -> Unit,
    onDismissDeleteConfirm: () -> Unit,
    onOpenSongEditor: (Long) -> Unit,
    onShowDeleteConfirm: () -> Unit,
    onOpenSpeedDialog: () -> Unit,
    onOpenSleepTimerDialog: () -> Unit,
    onOpenABRepeatDialog: () -> Unit
) {
    if (showSpeedDialog) {
        SpeedPickerDialog(
            currentSpeed = state.playbackSpeed,
            onSpeedSelected = { speed ->
                viewModel.setPlaybackSpeed(speed)
                onDismissSpeed()
            },
            onDismiss = onDismissSpeed
        )
    }

    if (showSleepTimerDialog) {
        SleepTimerDialog(
            currentRemaining = state.sleepTimerRemaining,
            onSetTimer = { minutes ->
                viewModel.setSleepTimer(minutes)
                onDismissSleep()
            },
            onCancel = {
                viewModel.cancelSleepTimer()
                onDismissSleep()
            },
            onDismiss = onDismissSleep
        )
    }

    if (showMoreOptionsSheet) {
        PlayerMoreOptionsSheet(
            currentSong = state.currentSong,
            playbackSpeed = state.playbackSpeed,
            sleepTimerRemaining = state.sleepTimerRemaining,
            abRepeatA = state.abRepeatA,
            abRepeatB = state.abRepeatB,
            onDismiss = onDismissMoreOptions,
            onOpenSongEditor = onOpenSongEditor,
            onSetAsRingtone = { song ->
                viewModel.setAsRingtone(context, song) { success ->
                    val msg = if (success) "Ringtone updated" else "Unable to set ringtone"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            onShowDeleteConfirm = onShowDeleteConfirm,
            onOpenSpeedDialog = onOpenSpeedDialog,
            onOpenSleepTimerDialog = onOpenSleepTimerDialog,
            onOpenABRepeatDialog = onOpenABRepeatDialog,
            onStopAfterCurrentSong = {
                if (state.repeatMode != RepeatMode.PLAY_ONE_ONCE) {
                    viewModel.cycleRepeatMode()
                }
                Toast.makeText(context, "Will stop playing after current song", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showABRepeatDialog) {
        ABRepeatDialog(
            currentA = state.abRepeatA,
            currentB = state.abRepeatB,
            currentProgress = state.progress,
            duration = state.currentSong?.duration ?: 0L,
            onSetA = { viewModel.setABRepeatA() },
            onSetB = { viewModel.setABRepeatB() },
            onClear = { viewModel.clearABRepeat() },
            onDismiss = onDismissABRepeat
        )
    }

    if (showDeleteConfirm && state.currentSong != null) {
        AlertDialog(
            onDismissRequest = onDismissDeleteConfirm,
            icon = { Icon(Icons.Rounded.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete permanently?") },
            text = {
                Text("This will permanently delete \"${state.currentSong?.title}\" from your device storage. This action cannot be reversed.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val song = state.currentSong
                        if (song != null) {
                            viewModel.deleteSong(song) { success ->
                                val msg = if (success) "Song deleted permanently" else "Unable to delete file from storage"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                        onDismissDeleteConfirm()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteConfirm) {
                    Text("Cancel")
                }
            }
        )
    }
}
