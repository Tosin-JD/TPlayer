package com.tosin.musicplayer.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.data.models.Playlist
import com.tosin.musicplayer.data.models.Song
import com.tosin.musicplayer.ui.components.SongItem
import com.tosin.musicplayer.ui.components.SongActionsSheet
import com.tosin.musicplayer.ui.components.StorageScopeSelector
import com.tosin.musicplayer.ui.state.HomeUiState
import com.tosin.musicplayer.ui.state.LibraryGroup
import com.tosin.musicplayer.ui.state.LibrarySortOption
import com.tosin.musicplayer.ui.state.LibraryTab
import com.tosin.musicplayer.ui.state.StorageScope
import com.tosin.musicplayer.ui.state.matchesStorageScope
import com.tosin.musicplayer.ui.state.removableStorageVolumes
import com.tosin.musicplayer.ui.state.displayName
import com.tosin.musicplayer.ui.state.requestEject
import com.tosin.musicplayer.ui.theme.AppSpacing
import com.tosin.musicplayer.ui.theme.standardScreenPadding
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import kotlinx.coroutines.launch
import com.tosin.musicplayer.ui.state.hasRemovableStorage
import com.tosin.musicplayer.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PlayerViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToPlayer: () -> Unit,
    onRequestAudioPermission: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToGroupDetail: (LibraryTab, String) -> Unit,
    onNavigateToSearch: () -> Unit = {},
    onNavigateToPlaylists: () -> Unit = {}
) {
    val uiState by viewModel.homeUiState.collectAsState()
    val settingsState by settingsViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val availableStorageScopes = remember(context) {
        if (context.hasRemovableStorage()) {
            StorageScope.entries.toList()
        } else {
            listOf(StorageScope.Internal)
        }
    }
    val removableVolumes = remember(context) { context.removableStorageVolumes() }
    var showStorageMenu by remember { mutableStateOf(false) }
    
    val activeTabs = remember(settingsState.tabOrder, settingsState.visibleTabs) {
        settingsState.tabOrder
            .filter { it in settingsState.visibleTabs }
            .mapNotNull { name -> 
                try {
                    LibraryTab.valueOf(name)
                } catch (e: Exception) {
                    null
                }
            }
    }

    // Fallback if activeTabs is somehow empty
    val safeActiveTabs = if (activeTabs.isEmpty()) listOf(LibraryTab.All) else activeTabs

    val pagerState = rememberPagerState(pageCount = { safeActiveTabs.size })
    val coroutineScope = rememberCoroutineScope()
    val sortState = remember { mutableStateMapOf<LibraryTab, LibrarySortOption>() }
    
    // Sync pager with selected tab
    LaunchedEffect(uiState.selectedTab, safeActiveTabs) {
        val index = safeActiveTabs.indexOf(uiState.selectedTab)
        if (index != -1 && index != pagerState.currentPage) {
            pagerState.animateScrollToPage(index)
        }
    }

    // Sync selected tab with pager
    LaunchedEffect(pagerState.currentPage, safeActiveTabs) {
        if (pagerState.currentPage < safeActiveTabs.size) {
            val tab = safeActiveTabs[pagerState.currentPage]
            if (tab != uiState.selectedTab) {
                viewModel.selectLibraryTab(tab)
            }
        }
    }

    Scaffold(
        topBar = {
            Column() {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
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
                                imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                                contentDescription = "Playlists",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (removableVolumes.isNotEmpty()) {
                            IconButton(onClick = { showStorageMenu = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = "External storage",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            DropdownMenu(
                                expanded = showStorageMenu,
                                onDismissRequest = { showStorageMenu = false }
                            ) {
                                removableVolumes.forEach { volume ->
                                    val label = volume.displayName(context)
                                    DropdownMenuItem(
                                        text = { Text("Eject $label") },
                                        onClick = {
                                            val success = volume.requestEject()
                                            Toast.makeText(
                                                context,
                                                if (success) "Eject requested for $label" else "Unable to eject $label",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            showStorageMenu = false
                                        }
                                    )
                                }
                            }
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = AppSpacing.large, vertical = AppSpacing.small),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
                ) {
                    safeActiveTabs.forEachIndexed { index, tab ->
                        FilterChip(
                            selected = uiState.selectedTab == tab,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            label = { Text(tab.label) },
                            trailingIcon = { Icon(tab.icon(), contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                if (pageIndex >= safeActiveTabs.size) return@HorizontalPager
                val tab = safeActiveTabs[pageIndex]
                
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
                            settingsViewModel = settingsViewModel,
                            tab = contentState.selectedTab,
                            onNavigateToPlayer = onNavigateToPlayer,
                            sortBy = sortState[LibraryTab.All] ?: LibrarySortOption.TitleAz,
                            onSortChange = { sortState[LibraryTab.All] = it },
                            availableStorageScopes = availableStorageScopes
                        )
                        else -> LibraryGroupsTab(
                            tab = contentState.selectedTab,
                            groups = uiState.libraryGroups,
                            settingsViewModel = settingsViewModel,
                            sortBy = sortState[contentState.selectedTab] ?: LibrarySortOption.TitleAz,
                            onSortChange = { sortState[contentState.selectedTab] = it },
                            onGroupClick = { group ->
                                onNavigateToGroupDetail(contentState.selectedTab, group.title)
                            },
                            availableStorageScopes = availableStorageScopes
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
    settingsViewModel: SettingsViewModel,
    tab: LibraryTab,
    onNavigateToPlayer: () -> Unit,
    sortBy: LibrarySortOption,
    onSortChange: (LibrarySortOption) -> Unit,
    availableStorageScopes: List<StorageScope>
) {
    val playerState by viewModel.uiState.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    val storageScope = remember(settingsViewModel.uiState.collectAsState().value, availableStorageScopes) {
        val scope = settingsViewModel.getStorageScopeForTab(tab.label)
        if (scope !in availableStorageScopes && availableStorageScopes.isNotEmpty()) {
            availableStorageScopes.first()
        } else {
            scope
        }
    }
    val visibleSongs = remember(uiState.songs, sortBy, storageScope) {
        sortSongs(uiState.songs.filter { it.matchesStorageScope(storageScope) }, sortBy)
    }
    var actionSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var actionInitialIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showActions by remember { mutableStateOf(false) }

    if (visibleSongs.isEmpty()) {
        EmptyLibraryState(
            title = "No songs found",
            message = "Add music to this device and it will appear here in alphabetical order."
        )
        return
    }

    if (showCreatePlaylistDialog) {
        var playlistName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("New Playlist") },
            text = {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text("Playlist Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (playlistName.isNotBlank()) {
                            viewModel.createPlaylist(playlistName)
                            showCreatePlaylistDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showActions) {
        SongActionsSheet(
            songs = actionSongs,
            initialSelectedIds = actionInitialIds,
            playlists = playlists,
            onDismiss = { showActions = false },
            onAddToQueue = { viewModel.addSongsToQueue(it) },
            onPlayNext = { viewModel.playNextSongs(it) },
            onAddToPlaylist = { playlistId, songIds -> viewModel.addSongsToPlaylist(playlistId, songIds) }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = standardScreenPadding(top = 0.dp, bottom = 0.dp),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.itemSpacing)
    ) {
        item {
            LibrarySummary(
                title = "All songs",
                subtitle = "${visibleSongs.size} songs",
                onSortClick = { showSortMenu = true },
                sortLabel = sortBy.label
            )
            if (availableStorageScopes.size > 1) {
                StorageScopeSelector(
                    selected = storageScope,
                    onSelected = { settingsViewModel.setStorageScopeForTab(tab.label, it) },
                    modifier = Modifier.padding(top = AppSpacing.small, bottom = AppSpacing.small),
                    availableScopes = availableStorageScopes
                )
            }
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }
            ) {
                LibrarySortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            onSortChange(option)
                            showSortMenu = false
                        }
                    )
                }
            }
        }

        itemsIndexed(
            items = visibleSongs,
            key = { _, song -> song.id }
        ) { index, song ->
            SongItem(
                song = song,
                isPlaying = playerState.currentSong?.id == song.id,
                onClick = {
                    viewModel.onSongClick(visibleSongs, index)
                    onNavigateToPlayer()
                },
                onLongClick = {
                    actionSongs = visibleSongs
                    actionInitialIds = setOf(song.id)
                    showActions = true
                },
                trailingContent = {
                    IconButton(onClick = {
                        actionSongs = listOf(song)
                        actionInitialIds = setOf(song.id)
                        showActions = true
                    }) {
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Rounded.PlaylistAdd, contentDescription = "Actions", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    }
}

@Composable
private fun LibraryGroupsTab(
    tab: LibraryTab,
    groups: List<LibraryGroup>,
    settingsViewModel: SettingsViewModel,
    sortBy: LibrarySortOption,
    onSortChange: (LibrarySortOption) -> Unit,
    onGroupClick: (LibraryGroup) -> Unit,
    availableStorageScopes: List<StorageScope>
) {
    val storageScope = remember(settingsViewModel.uiState.collectAsState().value, availableStorageScopes) {
        val scope = settingsViewModel.getStorageScopeForTab(tab.label)
        if (scope !in availableStorageScopes && availableStorageScopes.isNotEmpty()) {
            availableStorageScopes.first()
        } else {
            scope
        }
    }
    val sortedGroups = remember(groups, sortBy) {
        sortLibraryGroups(filterGroupsForStorage(groups, storageScope), sortBy)
    }
    var showSortMenu by remember { mutableStateOf(false) }

    if (groups.isEmpty()) {
        EmptyLibraryState(
            title = "Nothing in ${tab.label.lowercase()} yet",
            message = "Once audio metadata is available, it will show up here."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = standardScreenPadding(top = 0.dp),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.itemSpacing)
    ) {
        item {
            LibrarySummary(
                title = tab.label,
                subtitle = "${sortedGroups.size} ${if (sortedGroups.size == 1) "section" else "sections"}",
                onSortClick = { showSortMenu = true },
                sortLabel = sortBy.label
            )
            if (availableStorageScopes.size > 1) {
                StorageScopeSelector(
                    selected = storageScope,
                    onSelected = { settingsViewModel.setStorageScopeForTab(tab.label, it) },
                    modifier = Modifier.padding(top = AppSpacing.small, bottom = AppSpacing.small),
                    availableScopes = availableStorageScopes
                )
            }
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }
            ) {
                LibrarySortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            onSortChange(option)
                            showSortMenu = false
                        }
                    )
                }
            }
        }

        items(sortedGroups, key = { it.id }) { group ->
            Surface(
                onClick = { onGroupClick(group) },
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.cardPadding, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
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
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = group.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
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
            .padding(horizontal = AppSpacing.xLarge, vertical = AppSpacing.xLarge),
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
    subtitle: String,
    onSortClick: () -> Unit,
    sortLabel: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
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
        FilledTonalIconButton(onClick = onSortClick) {
            Icon(
                imageVector = Icons.Rounded.Sort,
                contentDescription = "Sort by $sortLabel"
            )
        }
    }
}


private data class HomeContentState(
    val selectedTab: LibraryTab,
    val isLoading: Boolean,
    val hasAudioPermission: Boolean
)

private fun sortSongs(
    songs: List<com.tosin.musicplayer.data.models.Song>,
    sortBy: LibrarySortOption
): List<com.tosin.musicplayer.data.models.Song> {
    return when (sortBy) {
        LibrarySortOption.TitleAz -> songs.sortedBy { it.title.trim().lowercase() }
        LibrarySortOption.ArtistAz -> songs.sortedBy { it.artist.trim().lowercase() }
        LibrarySortOption.AlbumAz -> songs.sortedBy { it.album.trim().lowercase() }
        LibrarySortOption.Genre -> songs.sortedBy { it.genre.orEmpty().trim().lowercase() }
        LibrarySortOption.ReleaseYear -> songs.sortedByDescending { it.year ?: 0 }
        LibrarySortOption.Duration -> songs.sortedByDescending { it.duration }
        LibrarySortOption.TrackNumber -> songs.sortedBy { it.trackNumber }
        LibrarySortOption.PopularityPlays -> songs.sortedByDescending { it.playCount }
        LibrarySortOption.DateAdded -> songs.sortedByDescending { it.dateAddedMs ?: 0L }
        LibrarySortOption.Rating -> songs.sortedByDescending { it.rating ?: 0 }
        LibrarySortOption.RecentlyPlayed -> songs.sortedByDescending { it.lastPlayedMs ?: 0L }
        LibrarySortOption.FileSize -> songs.sortedByDescending { it.fileSizeBytes ?: 0L }
    }
}

private fun sortLibraryGroups(
    groups: List<LibraryGroup>,
    sortBy: LibrarySortOption
): List<LibraryGroup> {
    return when (sortBy) {
        LibrarySortOption.TitleAz -> groups.sortedBy { it.title.trim().lowercase() }
        LibrarySortOption.ArtistAz -> groups.sortedBy { it.songs.firstOrNull()?.artist.orEmpty().trim().lowercase() }
        LibrarySortOption.AlbumAz -> groups.sortedBy { it.songs.firstOrNull()?.album.orEmpty().trim().lowercase() }
        LibrarySortOption.Genre -> groups.sortedBy { it.title.trim().lowercase() }
        LibrarySortOption.ReleaseYear -> groups.sortedByDescending { it.songs.maxOfOrNull { song -> song.year ?: 0 } ?: 0 }
        LibrarySortOption.Duration -> groups.sortedByDescending { it.songs.sumOf { song -> song.duration } }
        LibrarySortOption.TrackNumber -> groups.sortedBy { it.songs.minOfOrNull { song -> song.trackNumber } ?: Int.MAX_VALUE }
        LibrarySortOption.PopularityPlays -> groups.sortedByDescending { it.songs.sumOf { song -> song.playCount } }
        LibrarySortOption.DateAdded -> groups.sortedByDescending { it.songs.maxOfOrNull { song -> song.dateAddedMs ?: 0L } ?: 0L }
        LibrarySortOption.Rating -> groups.sortedByDescending {
            val ratings = it.songs.mapNotNull { song -> song.rating }
            if (ratings.isEmpty()) 0.0 else ratings.average()
        }
        LibrarySortOption.RecentlyPlayed -> groups.sortedByDescending { it.songs.maxOfOrNull { song -> song.lastPlayedMs ?: 0L } ?: 0L }
        LibrarySortOption.FileSize -> groups.sortedByDescending { it.songs.sumOf { song -> song.fileSizeBytes ?: 0L } }
    }
}

private fun filterGroupsForStorage(
    groups: List<LibraryGroup>,
    storageScope: StorageScope
): List<LibraryGroup> {
    return groups.mapNotNull { group ->
        val filteredSongs = group.songs.filter { it.matchesStorageScope(storageScope) }
        if (filteredSongs.isEmpty()) null
        else group.copy(songs = filteredSongs, songCount = filteredSongs.size)
    }
}
