package com.tosin.musicplayer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel

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

    var selectedTab by remember { mutableStateOf(LyricsEditorTab.Simple) }
    var simpleLyrics by remember(songId, initialLyrics) { mutableStateOf(initialLyrics) }
    var syncedLines by remember(songId, initialSyncedLines) { mutableStateOf(initialSyncedLines) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Edit Lyrics", fontWeight = FontWeight.Bold) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Close")
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
                        Icon(Icons.Rounded.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)
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
                shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
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
                                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary
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
                                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                            )
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
                            ) {
                                itemsIndexed(syncedLines) { index, line ->
                                    Surface(
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                                        tonalElevation = 1.dp
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(AppSpacing.medium),
                                            verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)
                                        ) {
                                            Text(
                                                text = formatLyricsTime(line.timeMs),
                                                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                                            )
                                            OutlinedTextField(
                                                value = line.text,
                                                onValueChange = { updated ->
                                                    syncedLines = syncedLines.toMutableList().also { list ->
                                                        list[index] = line.copy(text = updated)
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                leadingIcon = {
                                                    Icon(Icons.Rounded.Timer, contentDescription = null)
                                                }
                                            )
                                        }
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
