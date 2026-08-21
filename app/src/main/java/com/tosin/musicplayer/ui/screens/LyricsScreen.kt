package com.tosin.musicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tosin.musicplayer.ui.components.StatusBarColorEffect
import com.tosin.musicplayer.ui.extensions.orDefaultAlbumArt
import com.tosin.musicplayer.ui.screens.lyrics.LyricLineCard
import com.tosin.musicplayer.ui.screens.lyrics.LyricsAppearanceDialog
import com.tosin.musicplayer.ui.screens.lyrics.LyricsFontFamily
import com.tosin.musicplayer.ui.screens.lyrics.LyricsFontSize
import com.tosin.musicplayer.ui.screens.lyrics.LyricsTextAlign
import com.tosin.musicplayer.ui.screens.lyrics.parseLyricLines
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import com.tosin.musicplayer.ui.icons.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsScreen(
    viewModel: PlayerViewModel,
    onNavigateBack: () -> Unit,
    onOpenLyricsEditor: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val song = uiState.currentSong
    val lyrics = song?.lyrics.orEmpty()
    val parsedLines = remember(lyrics) { parseLyricLines(lyrics) }
    val listState = rememberLazyListState()

    var showAppearanceDialog by remember { mutableStateOf(false) }
    var fontSizeChoice by remember { mutableStateOf(LyricsFontSize.Medium) }
    var textAlignChoice by remember { mutableStateOf(LyricsTextAlign.Center) }
    var fontFamilyChoice by remember { mutableStateOf(LyricsFontFamily.SansSerif) }

    val currentLineIndex = remember(parsedLines, uiState.progress) {
        if (parsedLines.isEmpty()) -1 else parsedLines.indexOfLast { it.timeMs <= uiState.progress }.coerceAtLeast(0)
    }

    StatusBarColorEffect(Color.Black)

    LaunchedEffect(Unit) {
        viewModel.setLyricsVisible(true)
        val appearance = viewModel.loadLyricsAppearance()
        if (appearance != null) {
            fontSizeChoice = LyricsFontSize.entries.firstOrNull { it.name == appearance.fontSize } ?: fontSizeChoice
            textAlignChoice = LyricsTextAlign.entries.firstOrNull { it.name == appearance.textAlign } ?: textAlignChoice
            fontFamilyChoice = LyricsFontFamily.entries.firstOrNull { it.name == appearance.fontFamily } ?: fontFamilyChoice
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.setLyricsVisible(false) }
    }

    LaunchedEffect(currentLineIndex) {
        if (currentLineIndex >= 0) {
            listState.animateScrollToItem(currentLineIndex)
        }
    }

    if (showAppearanceDialog) {
        LyricsAppearanceDialog(
            fontSize = fontSizeChoice,
            onFontSizeSelected = {
                fontSizeChoice = it
                viewModel.saveLyricsAppearance(it.name, textAlignChoice.name, fontFamilyChoice.name)
            },
            textAlign = textAlignChoice,
            onTextAlignSelected = {
                textAlignChoice = it
                viewModel.saveLyricsAppearance(fontSizeChoice.name, it.name, fontFamilyChoice.name)
            },
            fontFamily = fontFamilyChoice,
            onFontFamilySelected = {
                fontFamilyChoice = it
                viewModel.saveLyricsAppearance(fontSizeChoice.name, textAlignChoice.name, it.name)
            },
            onDismiss = { showAppearanceDialog = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = song?.albumArt.orDefaultAlbumArt(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Black.copy(alpha = 0.9f)))
            )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column(modifier = Modifier.fillMaxWidth().padding(end = AppSpacing.large)) {
                            Text(
                                text = song?.title ?: "Unknown",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee()
                            )
                            Text(
                                text = song?.artist ?: "Unknown Artist",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f),
                                maxLines = 1,
                                modifier = Modifier.basicMarquee()
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(AppIcons.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { song?.id?.let(onOpenLyricsEditor) }) {
                            Icon(AppIcons.TextFields, contentDescription = "Edit lyrics", tint = Color.White)
                        }
                        IconButton(onClick = { showAppearanceDialog = true }) {
                            Icon(AppIcons.FormatSize, contentDescription = "Lyrics appearance", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            if (parsedLines.isNotEmpty()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = AppSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.small),
                    contentPadding = PaddingValues(bottom = AppSpacing.large)
                ) {
                    itemsIndexed(parsedLines) { index, line ->
                        LyricLineCard(
                            line = line,
                            isActive = index == currentLineIndex,
                            fontSizeChoice = fontSizeChoice,
                            textAlignChoice = textAlignChoice,
                            fontFamilyChoice = fontFamilyChoice,
                            onSeek = { viewModel.seekTo(it) }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = AppSpacing.large),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f))
                    ) {
                        Column(
                            modifier = Modifier.padding(AppSpacing.large),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (lyrics.isBlank()) "No lyrics found for this song." else lyrics,
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    lineHeight = 38.sp,
                                    fontFamily = when (fontFamilyChoice) {
                                        LyricsFontFamily.Serif -> FontFamily.Serif
                                        LyricsFontFamily.SansSerif -> FontFamily.SansSerif
                                    }
                                ),
                                textAlign = when (textAlignChoice) {
                                    LyricsTextAlign.Left -> TextAlign.Start
                                    LyricsTextAlign.Center -> TextAlign.Center
                                    LyricsTextAlign.Right -> TextAlign.End
                                    LyricsTextAlign.Justify -> TextAlign.Justify
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
