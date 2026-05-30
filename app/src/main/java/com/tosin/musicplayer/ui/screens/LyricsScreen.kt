package com.tosin.musicplayer.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tosin.musicplayer.ui.extensions.orDefaultAlbumArt
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsScreen(
    viewModel: PlayerViewModel,
    onNavigateBack: () -> Unit,
    onOpenVisualizer: () -> Unit = {},
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
        if (parsedLines.isEmpty()) -1 else parsedLines.lastIndexOfLastBefore(uiState.progress)
    }

    LaunchedEffect(currentLineIndex) {
        if (currentLineIndex >= 0) {
            listState.animateScrollToItem(currentLineIndex.coerceAtLeast(0))
        }
    }

    if (showAppearanceDialog) {
        LyricsAppearanceDialog(
            fontSize = fontSizeChoice,
            onFontSizeSelected = { fontSizeChoice = it },
            textAlign = textAlignChoice,
            onTextAlignSelected = { textAlignChoice = it },
            fontFamily = fontFamilyChoice,
            onFontFamilySelected = { fontFamilyChoice = it },
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
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { song?.id?.let(onOpenLyricsEditor) }) {
                            Icon(
                                imageVector = Icons.Rounded.TextFields,
                                contentDescription = "Edit lyrics",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { showAppearanceDialog = true }) {
                            Icon(
                                imageVector = Icons.Rounded.FormatSize,
                                contentDescription = "Lyrics appearance",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = onOpenVisualizer) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = "Open visualizer",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            if (parsedLines.isNotEmpty()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = AppSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.small),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = AppSpacing.large)
                ) {
                    itemsIndexed(parsedLines) { index, line ->
                        val isActive = index == currentLineIndex
                        val activeScale by animateFloatAsState(
                            targetValue = if (isActive) 1.02f else 1f,
                            animationSpec = tween(250),
                            label = "lyricScale"
                        )
                        val containerColor by animateColorAsState(
                            targetValue = if (isActive) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f)
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.36f)
                            },
                            animationSpec = tween(250),
                            label = "lyricColor"
                        )
                        val contentColor = if (isActive) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            Color.White
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(),
                            colors = CardDefaults.cardColors(containerColor = containerColor),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = AppSpacing.medium, vertical = AppSpacing.medium)
                                    .clip(RoundedCornerShape(24.dp)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AssistChip(
                                    onClick = { },
                                    label = {
                                        Text(
                                            formatTime(line.timeMs),
                                            color = contentColor
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Rounded.Tune,
                                            contentDescription = null,
                                            tint = contentColor
                                        )
                                    }
                                )
                                Spacer(Modifier.width(AppSpacing.medium))
                                Text(
                                    text = line.text.ifBlank { " " },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = AppSpacing.xSmall)
                                        .graphicsLayer(scaleX = activeScale, scaleY = activeScale),
                                    color = contentColor,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        lineHeight = when (fontSizeChoice) {
                                            LyricsFontSize.Small -> 30.sp
                                            LyricsFontSize.Medium -> 34.sp
                                            LyricsFontSize.Large -> 40.sp
                                            else -> 34.sp
                                        },
                                        fontFamily = when (fontFamilyChoice) {
                                            LyricsFontFamily.Serif -> FontFamily.Serif
                                            LyricsFontFamily.SansSerif -> FontFamily.SansSerif
                                            else -> FontFamily.SansSerif
                                        },
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    textAlign = when (textAlignChoice) {
                                        LyricsTextAlign.Left -> TextAlign.Start
                                        LyricsTextAlign.Center -> TextAlign.Center
                                        LyricsTextAlign.Right -> TextAlign.End
                                        LyricsTextAlign.Justify -> TextAlign.Justify
                                        else -> TextAlign.Center
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = AppSpacing.large),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f)
                        )
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
                                        else -> FontFamily.SansSerif
                                    }
                                ),
                                textAlign = when (textAlignChoice) {
                                    LyricsTextAlign.Left -> TextAlign.Start
                                    LyricsTextAlign.Center -> TextAlign.Center
                                    LyricsTextAlign.Right -> TextAlign.End
                                    LyricsTextAlign.Justify -> TextAlign.Justify
                                    else -> TextAlign.Center
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LyricsAppearanceDialog(
    fontSize: LyricsFontSize,
    onFontSizeSelected: (LyricsFontSize) -> Unit,
    textAlign: LyricsTextAlign,
    onTextAlignSelected: (LyricsTextAlign) -> Unit,
    fontFamily: LyricsFontFamily,
    onFontFamilySelected: (LyricsFontFamily) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lyrics Appearance") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
                Text("Font size", style = MaterialTheme.typography.labelLarge)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    LyricsFontSize.entries.forEachIndexed { index, size ->
                        SegmentedButton(
                            selected = fontSize == size,
                            onClick = { onFontSizeSelected(size) },
                            shape = SegmentedButtonDefaults.itemShape(index, LyricsFontSize.entries.size)
                        ) {
                            Text(size.label)
                        }
                    }
                }

                Text("Alignment", style = MaterialTheme.typography.labelLarge)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    LyricsTextAlign.entries.forEachIndexed { index, align ->
                        SegmentedButton(
                            selected = textAlign == align,
                            onClick = { onTextAlignSelected(align) },
                            shape = SegmentedButtonDefaults.itemShape(index, LyricsTextAlign.entries.size)
                        ) {
                            Text(align.label)
                        }
                    }
                }

                Text("Font family", style = MaterialTheme.typography.labelLarge)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    LyricsFontFamily.entries.forEachIndexed { index, family ->
                        SegmentedButton(
                            selected = fontFamily == family,
                            onClick = { onFontFamilySelected(family) },
                            shape = SegmentedButtonDefaults.itemShape(index, LyricsFontFamily.entries.size)
                        ) {
                            Text(family.label)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private data class ParsedLyricLine(
    val timeMs: Long,
    val text: String
)

private enum class LyricsFontSize(val label: String) {
    Small("Small"),
    Medium("Medium"),
    Large("Large")
}

private enum class LyricsTextAlign(val label: String) {
    Left("Left"),
    Center("Center"),
    Right("Right"),
    Justify("Justify")
}

private enum class LyricsFontFamily(val label: String) {
    Serif("Serif"),
    SansSerif("Sans serif")
}

private fun parseLyricLines(lyrics: String): List<ParsedLyricLine> {
    val timestampPattern = Regex("""\[(\d{1,2}):(\d{2})(?:[.:](\d{1,2}))?]""")
    return lyrics.lineSequence()
        .mapNotNull { rawLine ->
            val matches = timestampPattern.findAll(rawLine).toList()
            if (matches.isEmpty()) return@mapNotNull null
            val text = timestampPattern.replace(rawLine, "").trim()
            matches.mapNotNull { match ->
                val minutes = match.groupValues[1].toLongOrNull() ?: return@mapNotNull null
                val seconds = match.groupValues[2].toLongOrNull() ?: return@mapNotNull null
                val centiseconds = match.groupValues.getOrNull(3)?.takeIf { it.isNotBlank() }?.toLongOrNull() ?: 0L
                val timeMs = minutes * 60_000 + seconds * 1_000 + centiseconds * if (match.groupValues[3].length == 1) 100 else 10
                ParsedLyricLine(timeMs, text)
            }
        }
        .flatten()
        .sortedBy { it.timeMs }
        .toList()
}

private fun List<ParsedLyricLine>.lastIndexOfLastBefore(progressMs: Long): Int {
    val index = indexOfLast { it.timeMs <= progressMs }
    return if (index >= 0) index else 0
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
