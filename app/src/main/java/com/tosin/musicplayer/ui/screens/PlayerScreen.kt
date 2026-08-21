package com.tosin.musicplayer.ui.screens

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.tosin.musicplayer.ui.components.ProgressBar
import com.tosin.musicplayer.ui.components.StatusBarColorEffect
import com.tosin.musicplayer.ui.extensions.orDefaultAlbumArt
import com.tosin.musicplayer.ui.screens.player.*
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import com.tosin.musicplayer.ui.icons.AppIcons

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onOpenPlaylist: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenVisualizer: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSongEditor: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()

    val context = LocalContext.current
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp < 360
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val bottomIconSize = if (isSmallScreen) 44.dp else 32.dp
    val bottomButtonSize = if (isSmallScreen) 60.dp else 48.dp
    val albumArtSize = if (isLandscape) 240.dp else 320.dp
    val swipeThresholdPx = with(LocalDensity.current) { 56.dp.toPx() }
    val albumSwipeThresholdPx = with(LocalDensity.current) { 72.dp.toPx() }
    val albumDragRangePx = with(LocalDensity.current) { albumArtSize.toPx() }

    var bgColor by remember { mutableStateOf(surfaceColor) }
    var contentColor by remember { mutableStateOf(onSurfaceColor) }

    var showSpeedDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showMoreOptionsSheet by remember { mutableStateOf(false) }
    var showABRepeatDialog by remember { mutableStateOf(false) }

    var lastSongId by remember { mutableStateOf<Long?>(null) }
    var slideDirection by remember { mutableStateOf(1) }

    LaunchedEffect(state.currentSong?.id) {
        val prevId = lastSongId
        val currentSong = state.currentSong
        val queue = state.queue
        if (prevId != null && currentSong != null && queue.isNotEmpty()) {
            val prevIndex = queue.indexOfFirst { it.id == prevId }
            val currentIndex = queue.indexOfFirst { it.id == currentSong.id }
            if (prevIndex != -1 && currentIndex != -1) {
                slideDirection = if (currentIndex == 0 && prevIndex == queue.lastIndex) {
                    1
                } else if (currentIndex == queue.lastIndex && prevIndex == 0) {
                    -1
                } else if (currentIndex > prevIndex) {
                    1
                } else {
                    -1
                }
            }
        }
        lastSongId = currentSong?.id
    }

    StatusBarColorEffect(bgColor)

    LaunchedEffect(state.currentSong?.albumArt) {
        val data = state.currentSong?.albumArt.orDefaultAlbumArt()
        try {
            val request = ImageRequest.Builder(context)
                .data(data)
                .allowHardware(false)
                .build()

            val result = ImageLoader(context).execute(request)
            if (result is SuccessResult) {
                val drawable = result.drawable
                val bitmap = (drawable as? BitmapDrawable)?.bitmap
                bitmap?.let {
                    val palette = Palette.from(it).generate()
                    val swatch = palette.dominantSwatch ?: palette.vibrantSwatch
                    swatch?.rgb?.let { colorInt ->
                        bgColor = Color(colorInt)
                        contentColor = if (palette.dominantSwatch?.titleTextColor != null) {
                            Color(palette.dominantSwatch!!.titleTextColor)
                        } else {
                            if (Color(colorInt).luminance() > 0.5f) Color.Black else Color.White
                        }
                    }
                }
            }
        } catch (_: Exception) {
            bgColor = surfaceColor
            contentColor = onSurfaceColor
        }
    }

    PlayerDialogContainer(
        state = state,
        viewModel = viewModel,
        context = context,
        showSpeedDialog = showSpeedDialog,
        showSleepTimerDialog = showSleepTimerDialog,
        showMoreOptionsSheet = showMoreOptionsSheet,
        showABRepeatDialog = showABRepeatDialog,
        showDeleteConfirm = showDeleteConfirm,
        onDismissSpeed = { showSpeedDialog = false },
        onDismissSleep = { showSleepTimerDialog = false },
        onDismissMoreOptions = { showMoreOptionsSheet = false },
        onDismissABRepeat = { showABRepeatDialog = false },
        onDismissDeleteConfirm = { showDeleteConfirm = false },
        onOpenSongEditor = onOpenSongEditor,
        onShowDeleteConfirm = { showDeleteConfirm = true },
        onOpenSpeedDialog = { showSpeedDialog = true },
        onOpenSleepTimerDialog = { showSleepTimerDialog = true },
        onOpenABRepeatDialog = { showABRepeatDialog = true }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = AppSpacing.small)
            .pointerInput(onNavigateBack, onOpenLyrics) {
                var dragDistance = 0f
                detectVerticalDragGestures(
                    onDragStart = {},
                    onVerticalDrag = { change, dragAmount ->
                        dragDistance += dragAmount
                        change.consume()
                    },
                    onDragEnd = {
                        if (dragDistance > swipeThresholdPx) {
                            onNavigateBack()
                        } else if (dragDistance < -swipeThresholdPx) {
                            onOpenLyrics()
                        }
                        dragDistance = 0f
                    },
                    onDragCancel = { dragDistance = 0f }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val isFav = state.currentSong?.id?.let { it in favoriteIds } == true

        PlayerTopBar(
            contentColor = contentColor,
            sleepTimerRemaining = state.sleepTimerRemaining,
            playbackSpeed = state.playbackSpeed,
            isFavorite = isFav,
            onNavigateBack = onNavigateBack,
            onOpenSleepTimer = { showSleepTimerDialog = true },
            onOpenSpeedDialog = { showSpeedDialog = true },
            onOpenEqualizer = onOpenEqualizer,
            onToggleFavorite = { state.currentSong?.id?.let { viewModel.toggleFavorite(it) } }
        )

        Spacer(Modifier.height(AppSpacing.large))

        PlayerAlbumArt(
            currentSong = state.currentSong,
            albumArtSize = albumArtSize,
            slideDirection = slideDirection,
            albumDragRangePx = albumDragRangePx,
            albumSwipeThresholdPx = albumSwipeThresholdPx,
            onPrevious = {
                slideDirection = -1
                viewModel.previous()
            },
            onNext = {
                slideDirection = 1
                viewModel.next()
            }
        )

        Spacer(Modifier.height(AppSpacing.sectionSpacing))

        Text(
            text = state.currentSong?.title ?: "No Song Playing",
            style = MaterialTheme.typography.headlineMedium,
            color = contentColor,
            maxLines = 1,
            modifier = Modifier.basicMarquee()
        )

        Text(
            text = state.currentSong?.artist ?: "Unknown Artist",
            style = MaterialTheme.typography.titleSmall,
            color = contentColor.copy(alpha = 0.6f),
            maxLines = 1,
            modifier = Modifier.basicMarquee()
        )

        Text(
            text = state.currentSong?.album ?: "Unknown Album",
            style = MaterialTheme.typography.bodySmall,
            color = contentColor.copy(alpha = 0.4f),
            maxLines = 1,
            modifier = Modifier.basicMarquee()
        )

        Spacer(Modifier.height(AppSpacing.large))

        if (state.abRepeatA != null || state.abRepeatB != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = { viewModel.clearABRepeat() },
                    label = {
                        val a = state.abRepeatA?.let { formatTime(it) } ?: "—"
                        val b = state.abRepeatB?.let { formatTime(it) } ?: "—"
                        Text("A-B: $a → $b", color = contentColor)
                    },
                    trailingIcon = {
                        Icon(AppIcons.Close, "Clear A-B", tint = contentColor, modifier = Modifier.size(16.dp))
                    }
                )
            }
            Spacer(Modifier.height(AppSpacing.small))
        }

        ProgressBar(
            progress = state.progress,
            duration = state.currentSong?.duration ?: 0L,
            onSeek = { viewModel.seekTo(it) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(state.progress),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.6f)
            )
            Text(
                text = formatTime(state.currentSong?.duration ?: 0L),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.6f)
            )
        }

        Spacer(Modifier.height(AppSpacing.large))

        PlayerMainControls(
            isPlaying = state.isPlaying,
            contentColor = contentColor,
            onPrevious = {
                slideDirection = -1
                viewModel.previous()
            },
            onPlayPause = {
                if (state.isPlaying) viewModel.pause() else viewModel.play()
            },
            onNext = {
                slideDirection = 1
                viewModel.next()
            }
        )

        Spacer(
            modifier = Modifier
                .heightIn(min = AppSpacing.medium)
                .weight(1f)
        )

        PlayerBottomBar(
            shuffleEnabled = state.shuffleEnabled,
            lyricsVisible = state.lyricsVisible,
            repeatMode = state.repeatMode,
            contentColor = contentColor,
            bottomButtonSize = bottomButtonSize,
            bottomIconSize = bottomIconSize,
            onToggleShuffle = { viewModel.toggleShuffle() },
            onOpenLyrics = onOpenLyrics,
            onOpenPlaylist = onOpenPlaylist,
            onCycleRepeatMode = { viewModel.cycleRepeatMode() },
            onOpenMoreOptions = { showMoreOptionsSheet = true }
        )
    }
}
