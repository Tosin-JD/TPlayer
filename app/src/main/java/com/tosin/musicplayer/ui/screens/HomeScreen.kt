package com.tosin.musicplayer.ui.screens


import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.components.SongItem
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    viewModel: PlayerViewModel,
    onNavigateToPlayer: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        itemsIndexed(
            items = state.songs,
            key = { _, song -> song.id }
        ) { index, song ->

            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
            ) {
                SongItem(
                    song = song,
                    isPlaying = state.currentSong?.id == song.id,
                    onClick = {
                        viewModel.onSongClick(state.songs, index)
                        onNavigateToPlayer()
                    }
                )
            }
        }
    }
}