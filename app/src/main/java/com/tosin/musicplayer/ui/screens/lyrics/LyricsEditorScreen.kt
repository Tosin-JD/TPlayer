package com.tosin.musicplayer.ui.screens.lyrics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import com.tosin.musicplayer.ui.icons.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsEditorScreen(
    viewModel: PlayerViewModel,
    songId: Long,
    onNavigateBack: () -> Unit
) {
    val song = viewModel.getSongById(songId)
    val initialLyrics = song?.lyrics.orEmpty()
    val initialSyncedLines = remember(initialLyrics) { parseEditableLyrics(initialLyrics) }

    val uiState by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableStateOf(LyricsEditorTab.Simple) }
    var simpleLyrics by remember(songId, initialLyrics) { mutableStateOf(initialLyrics) }
    var syncedLines by remember(songId, initialSyncedLines) { mutableStateOf(initialSyncedLines) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Lyrics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = "Close")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val lyricsToSave = when (selectedTab) {
                                LyricsEditorTab.Simple -> simpleLyrics
                                LyricsEditorTab.Synced -> buildSyncedLyrics(syncedLines)
                                else -> simpleLyrics
                            }
                            viewModel.saveLyrics(songId, lyricsToSave) {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(AppIcons.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = AppSpacing.large, vertical = AppSpacing.medium)
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                LyricsEditorTab.entries.forEachIndexed { index, tab ->
                    SegmentedButton(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        shape = SegmentedButtonDefaults.itemShape(index, LyricsEditorTab.entries.size)
                    ) {
                        Text(tab.label)
                    }
                }
                }

            Surface(
                tonalElevation = 2.dp,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = AppSpacing.medium)
            ) {
                when (selectedTab) {
                    LyricsEditorTab.Simple -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(AppSpacing.medium)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
                        ) {
                            Text(
                                text = "Embedded lyrics",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            OutlinedTextField(
                                value = simpleLyrics,
                                onValueChange = { simpleLyrics = it },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 12,
                                placeholder = { Text("Paste or edit the song lyrics here") }
                            )
                        }
                    }
                    LyricsEditorTab.Synced -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(AppSpacing.medium)
                        ) {
                            Text(
                                text = "Synced lyrics",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
                            ) {
                                itemsIndexed(syncedLines) { index, line ->
                                    Surface(
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                                        tonalElevation = 1.dp
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(AppSpacing.medium),
                                            verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)
                                            ) {
                                                Text(
                                                    text = formatLyricsTime(line.timeMs),
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                TextButton(
                                                    onClick = {
                                                        syncedLines = syncedLines.toMutableList().also { list ->
                                                            list[index] = line.copy(timeMs = uiState.progress.coerceAtLeast(0L))
                                                        }
                                                    }
                                                ) {
                                                    Text(
                                                        "Set to current ${formatLyricsTime(uiState.progress)}",
                                                        maxLines = 1
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        syncedLines = syncedLines.toMutableList().also { list ->
                                                            list.removeAt(index)
                                                        }
                                                    }
                                                ) {
                                                    Icon(AppIcons.DeleteOutline, contentDescription = "Delete line")
                                                }
                                            }
                                            OutlinedTextField(
                                                value = line.text,
                                                onValueChange = { updated ->
                                                    syncedLines = syncedLines.toMutableList().also { list ->
                                                        list[index] = line.copy(text = updated)
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                leadingIcon = {
                                                    Icon(AppIcons.Timer, contentDescription = null)
                                                }
                                            )
                                        }
                                    }
                                }
                                item {
                                    OutlinedButton(
                                        onClick = {
                                            val nextTime = (syncedLines.lastOrNull()?.timeMs ?: 0L) + 1000L
                                            syncedLines = syncedLines + EditableLyricLine(nextTime, "")
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = AppSpacing.xSmall)
                                    ) {
                                        Icon(AppIcons.Add, contentDescription = null)
                                        Spacer(Modifier.width(AppSpacing.xSmall))
                                        Text("Add line")
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(AppSpacing.medium)
                        ) {
                            OutlinedTextField(
                                value = simpleLyrics,
                                onValueChange = { simpleLyrics = it },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 12,
                                placeholder = { Text("Paste or edit the song lyrics here") }
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class LyricsEditorTab(val label: String) {
    Simple("Simple"),
    Synced("Synced")
}

private data class EditableLyricLine(
    val timeMs: Long,
    val text: String
)

private fun parseEditableLyrics(lyrics: String): List<EditableLyricLine> {
    val pattern = Regex("""\[(\d{1,2}):(\d{2})(?:[.:](\d{1,2}))?]""")
    return lyrics.lineSequence()
        .mapNotNull { line ->
            val matches = pattern.findAll(line).toList()
            if (matches.isEmpty()) return@mapNotNull null
            val text = pattern.replace(line, "").trim()
            matches.mapNotNull { match ->
                val minutes = match.groupValues[1].toLongOrNull() ?: return@mapNotNull null
                val seconds = match.groupValues[2].toLongOrNull() ?: return@mapNotNull null
                val fraction = match.groupValues[3]
                val timeMs = minutes * 60_000 + seconds * 1_000 + when (fraction.length) {
                    1 -> fraction.toLongOrNull()?.times(100) ?: 0L
                    2 -> fraction.toLongOrNull()?.times(10) ?: 0L
                    else -> 0L
                }
                EditableLyricLine(timeMs, text)
            }
        }
        .flatten()
        .sortedBy { it.timeMs }
        .toList()
}

private fun buildSyncedLyrics(lines: List<EditableLyricLine>): String {
    return lines.joinToString(separator = "\n") { line ->
        "[${formatLyricsTime(line.timeMs)}]${line.text}"
    }
}

private fun formatLyricsTime(timeMs: Long): String {
    val totalSeconds = (timeMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hundredths = (timeMs % 1000) / 10
    return "%02d:%02d.%02d".format(minutes, seconds, hundredths)
}
