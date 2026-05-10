package com.tosin.musicplayer.ui.screens

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.components.SongItem
import com.tosin.musicplayer.ui.state.HomeUiState
import com.tosin.musicplayer.ui.state.LibraryGroup
import com.tosin.musicplayer.ui.state.LibraryTab
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PlayerViewModel,
    onNavigateToPlayer: () -> Unit,
    onRequestAudioPermission: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToGroupDetail: (LibraryTab, String) -> Unit,
    onNavigateToSearch: () -> Unit = {},
    onNavigateToPlaylists: () -> Unit = {}
) {
    val uiState by viewModel.homeUiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { uiState.tabOrder.size })
    val coroutineScope = rememberCoroutineScope()
    
    val colorScheme = MaterialTheme.colorScheme
    val brush = remember(
        colorScheme.surface,
        colorScheme.surfaceContainerLowest,
        colorScheme.surfaceContainerHigh
    ) {
        Brush.verticalGradient(
            colors = listOf(
                colorScheme.surface,
                colorScheme.surfaceContainerLowest,
                colorScheme.surfaceContainerHigh
            )
        )
    }

    // Sync pager with selected tab
    LaunchedEffect(uiState.selectedTab) {
        val index = uiState.tabOrder.indexOf(uiState.selectedTab)
        if (index != -1 && index != pagerState.currentPage) {
            pagerState.animateScrollToPage(index)
        }
    }

    // Sync selected tab with pager
    LaunchedEffect(pagerState.currentPage) {
        val tab = uiState.tabOrder[pagerState.currentPage]
        if (tab != uiState.selectedTab) {
            viewModel.selectLibraryTab(tab)
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    windowInsets = WindowInsets(0.dp),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "TPlayer",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(Modifier.width(12.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = "ALPHA",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToSearch) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onNavigateToPlaylists) {
                            Icon(
                                imageVector = Icons.Rounded.QueueMusic,
                                contentDescription = "Playlists",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
                
                var showReorderDialog by remember { mutableStateOf(false) }

                if (showReorderDialog) {
                    TabReorderDialog(
                        tabOrder = uiState.tabOrder,
                        onReorder = { from, to -> viewModel.reorderTabs(from, to) },
                        onDismiss = { showReorderDialog = false }
                    )
                }

                PrimaryScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    edgePadding = 16.dp
                ) {
                    uiState.tabOrder.forEachIndexed { index, tab ->
                        Tab(
                            selected = uiState.selectedTab == tab,
                            onClick = { 
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            modifier = Modifier.combinedClickable(
                                onClick = { 
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                onLongClick = {
                                    showReorderDialog = true
                                }
                            ),
                            text = { Text(tab.label) },
                            icon = {
                                Icon(
                                    imageVector = tab.icon(),
                                    contentDescription = tab.label
                                )
                            }
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                val tab = uiState.tabOrder[pageIndex]
                
                AnimatedContent(
                    targetState = HomeContentState(
                        selectedTab = tab,
                        isLoading = uiState.isLoading,
                        hasAudioPermission = uiState.hasAudioPermission
                    ),
                    transitionSpec = {
                        (slideInHorizontally { it / 8 } + fadeIn())
                            .togetherWith(slideOutHorizontally { -it / 10 } + fadeOut())
                    },
                    label = "home-content"
                ) { contentState ->
                    when {
                        !contentState.hasAudioPermission -> PermissionState(
                            onRequestAudioPermission = onRequestAudioPermission
                        )
                        contentState.isLoading -> LoadingState()
                        contentState.selectedTab == LibraryTab.All -> AllSongsTab(
                            uiState = uiState,
                            viewModel = viewModel,
                            onNavigateToPlayer = onNavigateToPlayer
                        )
                        else -> LibraryGroupsTab(
                            tab = contentState.selectedTab,
                            groups = uiState.libraryGroups,
                            onGroupClick = { group ->
                                onNavigateToGroupDetail(contentState.selectedTab, group.title)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AllSongsTab(
    uiState: HomeUiState,
    viewModel: PlayerViewModel,
    onNavigateToPlayer: () -> Unit
) {
    val playerState by viewModel.uiState.collectAsState()

    if (uiState.songs.isEmpty()) {
        EmptyLibraryState(
            title = "No songs found",
            message = "Add music to this device and it will appear here in alphabetical order."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            LibrarySummary(
                title = "All songs",
                subtitle = "${uiState.songs.size} songs • arranged alphabetically"
            )
        }

        itemsIndexed(
            items = uiState.songs,
            key = { _, song -> song.id }
        ) { index, song ->
            SongItem(
                song = song,
                isPlaying = playerState.currentSong?.id == song.id,
                onClick = {
                    viewModel.onSongClick(uiState.songs, index)
                    onNavigateToPlayer()
                }
            )
        }
    }
}

@Composable
private fun LibraryGroupsTab(
    tab: LibraryTab,
    groups: List<LibraryGroup>,
    onGroupClick: (LibraryGroup) -> Unit
) {
    if (groups.isEmpty()) {
        EmptyLibraryState(
            title = "Nothing in ${tab.label.lowercase()} yet",
            message = "Once audio metadata is available, it will show up here."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            LibrarySummary(
                title = tab.label,
                subtitle = "${groups.size} ${if (groups.size == 1) "section" else "sections"}"
            )
        }

        items(groups, key = { it.id }) { group ->
            Surface(
                onClick = { onGroupClick(group) },
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = tab.icon(),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = group.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = group.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = group.songCount.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionState(
    onRequestAudioPermission: () -> Unit
) {
    EmptyLibraryState(
        title = "Audio permission needed",
        message = "Allow access to local audio so TPlayer can build your library.",
        action = {
            TextButton(onClick = onRequestAudioPermission) {
                Text("Grant access")
            }
        }
    )
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading your library",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun EmptyLibraryState(
    title: String,
    message: String,
    action: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.LibraryMusic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            action?.invoke()
        }
    }
}

@Composable
private fun LibrarySummary(
    title: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


private data class HomeContentState(
    val selectedTab: LibraryTab,
    val isLoading: Boolean,
    val hasAudioPermission: Boolean
)

@Composable
fun TabReorderDialog(
    tabOrder: List<LibraryTab>,
    onReorder: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reorder Tabs") },
        text = {
            Column {
                tabOrder.forEachIndexed { index, tab ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(tab.icon(), null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text(tab.label)
                        }
                        Row {
                            IconButton(
                                onClick = { if (index > 0) onReorder(index, index - 1) },
                                enabled = index > 0
                            ) {
                                Icon(Icons.Rounded.KeyboardArrowUp, "Move Up")
                            }
                            IconButton(
                                onClick = { if (index < tabOrder.size - 1) onReorder(index, index + 1) },
                                enabled = index < tabOrder.size - 1
                            ) {
                                Icon(Icons.Rounded.KeyboardArrowDown, "Move Down")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}
