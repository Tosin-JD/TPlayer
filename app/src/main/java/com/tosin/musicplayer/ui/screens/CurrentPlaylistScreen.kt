package com.tosin.musicplayer.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.ui.components.PlaylistItemCard
import com.tosin.musicplayer.ui.components.SongActionsSheet
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.theme.standardScreenPadding
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentPlaylistScreen(
    viewModel: PlayerViewModel,
    onNavigateBack: () -> Unit,
    onPlaySong: (Song) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val queue = uiState.queue
    val currentSong = uiState.currentSong
    val playlists by viewModel.playlists.collectAsState()

    var isEditMode by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    var draggedSongId by remember { mutableStateOf<Long?>(null) }
    var draggedOffsetY by remember { mutableStateOf(0f) }
    var draggedIndex by remember { mutableStateOf(-1) }
    var draggedItemHeightPx by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        if (!isEditMode) {
            val index = queue.indexOfFirst { it.id == currentSong?.id }
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }

    var selectedSongForActions by remember { mutableStateOf<Song?>(null) }
    var showActions by remember { mutableStateOf(false) }

    selectedSongForActions?.let { song ->
        SongActionsSheet(
            song = song,
            playlists = playlists,
            onDismiss = { showActions = false; selectedSongForActions = null },
            onPlay = { target -> onPlaySong(target) },
            onPlayNext = { target -> viewModel.playNextSongs(listOf(target)) },
            onAddToCurrentPlaylist = { target -> viewModel.addSongsToQueue(listOf(target)) },
            onAddToPlaylist = { playlistId, songIds -> viewModel.addSongsToPlaylist(playlistId, songIds) },
            onCreateNewPlaylist = { name -> viewModel.createPlaylist(name) }
        )
    }

    val showMiniPlayer = currentSong != null
    val extraBottomPadding = if (showMiniPlayer) 88.dp else AppSpacing.xLarge

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Current Playlist", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isEditMode = !isEditMode }) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Rounded.Check else Icons.Rounded.Edit,
                            contentDescription = if (isEditMode) "Done" else "Edit Order",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { paddingValues ->
        val density = LocalDensity.current
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = standardScreenPadding(top = 0.dp, bottom = extraBottomPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.itemSpacing)
        ) {
            itemsIndexed(
                items = queue,
                key = { _, song -> song.id }
            ) { index, song ->
                val isDragging = draggedSongId == song.id
                val dragScale by animateFloatAsState(
                    targetValue = if (isDragging) 0.96f else 1f,
                    animationSpec = tween(durationMillis = 180),
                    label = "playlistDragScale"
                )
                val animatedColor by animateColorAsState(
                    targetValue = when {
                        isDragging -> MaterialTheme.colorScheme.surfaceVariant
                        currentSong?.id == song.id -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceContainerLow
                    },
                    animationSpec = tween(durationMillis = 180),
                    label = "playlistItemColor"
                )

                PlaylistItemCard(
                    song = song,
                    isCurrentlyPlaying = currentSong?.id == song.id,
                    isEditMode = isEditMode,
                    isDragging = isDragging,
                    containerColor = animatedColor,
                    onItemClick = {
                        if (isEditMode) {
                            Toast.makeText(
                                context,
                                "Edit mode: long-press and drag to reorder",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            onPlaySong(song)
                        }
                    },
                    onLongClick = {
                        if (!isEditMode) {
                            selectedSongForActions = song
                            showActions = true
                        }
                    },
                    modifier = Modifier
                        .animateItem()
                        .onSizeChanged { size ->
                            if (draggedSongId == song.id) {
                                draggedItemHeightPx = size.height
                            }
                        }
                        .graphicsLayer {
                            translationY = if (isDragging) draggedOffsetY else 0f
                            scaleX = if (isDragging) dragScale else 1f
                            scaleY = if (isDragging) dragScale else 1f
                        },
                    dragModifier = Modifier.pointerInput(song.id, isEditMode) {
                        if (!isEditMode) return@pointerInput

                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggedSongId = song.id
                                draggedOffsetY = 0f
                                draggedIndex = index
                                draggedItemHeightPx = 0
                            },
                            onDragEnd = {
                                draggedSongId = null
                                draggedOffsetY = 0f
                                draggedIndex = -1
                            },
                            onDragCancel = {
                                draggedSongId = null
                                draggedOffsetY = 0f
                                draggedIndex = -1
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                if (draggedSongId != song.id || draggedIndex < 0) return@detectDragGesturesAfterLongPress

                                draggedOffsetY += dragAmount.y
                                val dragThreshold = with(density) { 72.dp.toPx() }
                                val itemStep = (draggedItemHeightPx.takeIf { it > 0 }?.toFloat() ?: dragThreshold) +
                                    with(density) { AppSpacing.itemSpacing.toPx() }
                                val maxIndex = viewModel.uiState.value.queue.lastIndex
                                val targetIndex = (draggedIndex + (draggedOffsetY / dragThreshold).roundToInt())
                                    .coerceIn(0, maxIndex)
                                if (targetIndex != draggedIndex) {
                                    viewModel.reorderCurrentQueue(draggedIndex, targetIndex)
                                    draggedOffsetY -= (targetIndex - draggedIndex) * itemStep
                                    draggedIndex = targetIndex
                                }
                            }
                        )
                    }
                )
            }

            if (queue.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Playlist is empty",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
