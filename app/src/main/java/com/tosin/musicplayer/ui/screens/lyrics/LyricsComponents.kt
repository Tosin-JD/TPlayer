package com.tosin.musicplayer.ui.screens.lyrics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tosin.musicplayer.ui.theme.AppSpacing

enum class LyricsFontSize(val label: String) {
    Small("Small"),
    Medium("Medium"),
    Large("Large")
}

enum class LyricsTextAlign(val label: String) {
    Left("Left"),
    Center("Center"),
    Right("Right"),
    Justify("Justify")
}

enum class LyricsFontFamily(val label: String) {
    Serif("Serif"),
    SansSerif("Sans serif")
}

data class ParsedLyricLine(
    val timeMs: Long,
    val text: String
)

@Composable
fun LyricsAppearanceDialog(
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
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

fun parseLyricLines(lyrics: String): List<ParsedLyricLine> {
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
