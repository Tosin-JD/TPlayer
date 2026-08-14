package com.tosin.musicplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.components.SongItem
import com.tosin.musicplayer.ui.components.menu.SelectionBottomSheet
import com.tosin.musicplayer.ui.components.menu.statsRangeOptions
import com.tosin.musicplayer.ui.components.menu.statsSortOptions
import com.tosin.musicplayer.ui.state.SortBy
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.theme.standardScreenPadding
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import com.tosin.musicplayer.ui.viewmodel.StatsViewModel

fun formatDuration(minutes: Long): String {
    val safeMinutes = minutes.coerceAtLeast(0L)
    val days = safeMinutes / (24 * 60)
    val hours = (safeMinutes % (24 * 60)) / 60
    val mins = safeMinutes % 60

    return when {
        days > 0 -> "${days}d ${hours}h ${mins}min"
        hours > 0 -> "${hours}h ${mins}min"
        else -> "${mins}min"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    statsViewModel: StatsViewModel,
    playerViewModel: PlayerViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToPlayer: () -> Unit
) {
    val mostPlayed by statsViewModel.mostPlayed.collectAsState()
    val selectedRange by statsViewModel.selectedRange.collectAsState()
    val sortBy by statsViewModel.sortBy.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }
    var showRangeMenu by remember { mutableStateOf(false) }

    val sortedMostPlayed = remember(mostPlayed, sortBy) {
        when (sortBy) {
            SortBy.PLAY_COUNT -> mostPlayed.sortedByDescending { it.playCount }
            SortBy.DURATION -> mostPlayed.sortedByDescending { it.totalMinutes }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Most Played", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToHome) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back to Home")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Rounded.FilterList, contentDescription = "Sort By")
                        }
                        if (showSortMenu) {
                            SelectionBottomSheet(
                                title = "Sort by",
                                options = statsSortOptions(),
                                selected = sortBy,
                                onSelect = statsViewModel::selectSort,
                                onDismiss = { showSortMenu = false }
                            )
                        }
                    }
                    Box {
                        IconButton(onClick = { showRangeMenu = true }) {
                            Icon(Icons.Rounded.History, contentDescription = "Time Range")
                        }
                        if (showRangeMenu) {
                            SelectionBottomSheet(
                                title = "Time range",
                                options = statsRangeOptions(),
                                selected = selectedRange,
                                onSelect = statsViewModel::selectRange,
                                onDismiss = { showRangeMenu = false }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (mostPlayed.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = AppSpacing.xLarge),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.BarChart,
                        null,
                        Modifier.size(64.dp),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "No play data for this period",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = standardScreenPadding(top = 0.dp),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.itemSpacing)
            ) {
                items(sortedMostPlayed) { stat ->
                    SongItem(
                        song = stat.song,
                        isPlaying = false,
                        onClick = {
                            playerViewModel.onSongClick(
                                sortedMostPlayed.map { it.song },
                                sortedMostPlayed.indexOf(stat)
                            )
                            onNavigateToPlayer()
                        },
                        trailingContent = {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${stat.playCount} plays",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = formatDuration(stat.totalMinutes),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
