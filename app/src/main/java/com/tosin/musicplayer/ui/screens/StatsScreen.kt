package com.tosin.musicplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.components.SongItem
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import java.util.*

enum class StatsRange(val label: String) {
    Today("Today"),
    ThisWeek("This Week"),
    ThisMonth("This Month"),
    ThisYear("This Year"),
    AllTime("All Time")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: PlayerViewModel,
    onNavigateToPlayer: () -> Unit
) {
    val mostPlayed by viewModel.mostPlayed.collectAsState()
    var selectedRange by remember { mutableStateOf(StatsRange.AllTime) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(selectedRange) {
        val startTime = when (selectedRange) {
            StatsRange.Today -> getStartOfToday()
            StatsRange.ThisWeek -> getStartOfWeek()
            StatsRange.ThisMonth -> getStartOfMonth()
            StatsRange.ThisYear -> getStartOfYear()
            StatsRange.AllTime -> 0L
        }
        viewModel.loadStats(startTime)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Most Played", fontWeight = FontWeight.Bold) },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Rounded.History, contentDescription = "Time Range")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            StatsRange.entries.forEach { range ->
                                DropdownMenuItem(
                                    text = { Text(range.label) },
                                    onClick = {
                                        selectedRange = range
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { _ ->
        if (mostPlayed.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.BarChart, null, Modifier.size(64.dp), MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    Spacer(Modifier.height(16.dp))
                    Text("No play data for this period", style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mostPlayed) { stat ->
                    SongItem(
                        song = stat.song,
                        isPlaying = false, // We don't necessarily know if it's playing from this screen
                        onClick = {
                            viewModel.onSongClick(mostPlayed.map { it.song }, mostPlayed.indexOf(stat))
                            onNavigateToPlayer()
                        }
                    )
                    // Optional: Add play count and duration badge
                }
            }
        }
    }
}

private fun getStartOfToday(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun getStartOfWeek(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    return cal.timeInMillis
}

private fun getStartOfMonth(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    return cal.timeInMillis
}

private fun getStartOfYear(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_YEAR, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    return cal.timeInMillis
}
