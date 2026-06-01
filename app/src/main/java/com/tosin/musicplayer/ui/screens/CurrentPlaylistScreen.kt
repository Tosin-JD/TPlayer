package com.tosin.musicplayer.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.ui.components.SongActionsSheet
import com.tosin.musicplayer.ui.extensions.orDefaultAlbumArt
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

    // Scroll to currently playing song on startup
    LaunchedEffect(currentSong) {
        if (!isEditMode) {
            val index = queue.indexOfFirst { it.id == currentSong?.id }
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }

    // Action sheet state for long press
    var actionSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var actionInitialIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showActions by remember { mutableStateOf(false) }

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

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Current Playlist", fontWeight = FontWeight.Bold) },
                windowInsets = WindowInsets(0, 0, 0, 0),
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
            contentPadding = standardScreenPadding(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.itemSpacing)
        ) {
            itemsIndexed(
                items = queue,
                key = { _, song -> song.id }
            ) { index, song ->
                var itemOffset by remember { mutableStateOf(0f) }
                var isDragging by remember { mutableStateOf(false) }

                PlaylistItem(
                    song = song,
                    isCurrentlyPlaying = currentSong?.id == song.id,
                    isEditMode = isEditMode,
                    isDragging = isDragging,
                    onItemClick = {
                        if (!isEditMode) {
                            onPlaySong(song)
                        }
                    },
                    onLongClick = {
                        if (!isEditMode) {
                            actionSongs = queue
                            actionInitialIds = setOf(song.id)
                            showActions = true
                        }
                    },
                    modifier = Modifier
                        .offset { IntOffset(0, itemOffset.roundToInt()) }
                        .animateContentSize(),
                    dragHandleModifier = Modifier.pointerInput(index, queue.size) {
                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
                            },
                            onDragEnd = {
                                isDragging = false
                                itemOffset = 0f
                            },
                            onDragCancel = {
                                isDragging = false
                                itemOffset = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                itemOffset += dragAmount.y
                                val dragThreshold = with(density) { 72.dp.toPx() }
                                val targetIndex = index + (itemOffset / dragThreshold).roundToInt()
                                if (targetIndex != index && targetIndex in queue.indices) {
                                    viewModel.reorderCurrentQueue(index, targetIndex)
                                    itemOffset = 0f
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlaylistItem(
    song: Song,
    isCurrentlyPlaying: Boolean,
    isEditMode: Boolean,
    isDragging: Boolean,
    onItemClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    dragHandleModifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = if (isDragging) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (isCurrentlyPlaying) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 8.dp else if (isCurrentlyPlaying) 4.dp else 2.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = onItemClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.cardPadding, vertical = AppSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEditMode) {
                Box(
                    modifier = dragHandleModifier
                        .padding(end = AppSpacing.medium)
                        .size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DragHandle,
                        contentDescription = "Drag to reorder",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Album art thumbnail
            AsyncImage(
                model = song.albumArt.orDefaultAlbumArt(),
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(AppSpacing.large))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isCurrentlyPlaying) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCurrentlyPlaying) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }
                )
            }

            if (!isEditMode && isCurrentlyPlaying) {
                Icon(
                    imageVector = Icons.Rounded.Equalizer,
                    contentDescription = "Now playing",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
