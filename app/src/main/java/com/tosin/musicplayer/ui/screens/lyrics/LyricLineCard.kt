package com.tosin.musicplayer.ui.screens.lyrics

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.icons.AppIcons

@Composable
fun LyricLineCard(
    line: ParsedLyricLine,
    isActive: Boolean,
    fontSizeChoice: LyricsFontSize,
    textAlignChoice: LyricsTextAlign,
    fontFamilyChoice: LyricsFontFamily,
    onSeek: (Long) -> Unit
) {
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
    val contentColor = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else Color.White

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(AppSpacing.medium).clip(RoundedCornerShape(4.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistChip(
                onClick = { onSeek(line.timeMs) },
                label = { Text(formatTime(line.timeMs), color = contentColor) },
                leadingIcon = { Icon(AppIcons.Tune, contentDescription = null, tint = contentColor) }
            )
            Spacer(Modifier.width(AppSpacing.medium))
            Text(
                text = line.text.ifBlank { " " },
                modifier = Modifier.weight(1f).padding(vertical = AppSpacing.xSmall).graphicsLayer(scaleX = activeScale, scaleY = activeScale),
                color = contentColor,
                style = MaterialTheme.typography.headlineSmall.copy(
                    lineHeight = when (fontSizeChoice) {
                        LyricsFontSize.Small -> 30.sp
                        LyricsFontSize.Medium -> 34.sp
                        LyricsFontSize.Large -> 40.sp
                    },
                    fontFamily = when (fontFamilyChoice) {
                        LyricsFontFamily.Serif -> FontFamily.Serif
                        LyricsFontFamily.SansSerif -> FontFamily.SansSerif
                    },
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
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

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
