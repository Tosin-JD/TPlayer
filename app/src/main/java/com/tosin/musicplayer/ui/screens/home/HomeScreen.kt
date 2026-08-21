package com.tosin.musicplayer.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.tosin.musicplayer.ui.state.LibrarySortOption
import com.tosin.musicplayer.ui.state.LibraryTab
import com.tosin.musicplayer.ui.state.StorageScope
import com.tosin.musicplayer.ui.state.hasRemovableStorage
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import com.tosin.musicplayer.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
    val sortState = rememberLibrarySortState()
    val tabSortOptionsMap by settingsViewModel.tabSortOptions.collectAsState()

    LaunchedEffect(tabSortOptionsMap) {
        tabSortOptionsMap.forEach { (tabName, optionName) ->
            val tab = LibraryTab.entries.firstOrNull { it.name == tabName }
            val option = LibrarySortOption.entries.firstOrNull { it.name == optionName }
            if (tab != null && option != null) {
                sortState[tab] = option
            }
        }
    }

    val activeTabs = remember(settingsState.tabOrder, settingsState.visibleTabs) {
        val visibleSet = settingsState.visibleTabs.map { it.lowercase() }.toSet()
        settingsState.tabOrder
            .filter { it.lowercase() in visibleSet }
            .mapNotNull { name ->
                LibraryTab.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
            }
    }

    // Fallback if activeTabs is somehow empty
    val safeActiveTabs = if (activeTabs.isEmpty()) listOf(LibraryTab.All) else activeTabs

    val pagerState = rememberPagerState(pageCount = { safeActiveTabs.size })
    val coroutineScope = rememberCoroutineScope()

    // Sync pager with selected tab or when tab order/visibility changes
    LaunchedEffect(uiState.selectedTab, safeActiveTabs) {
        val targetIndex = safeActiveTabs.indexOf(uiState.selectedTab)
        if (targetIndex != -1) {
            if (pagerState.currentPage != targetIndex && !pagerState.isScrollInProgress) {
                pagerState.scrollToPage(targetIndex)
            }
        } else {
            // Selected tab is no longer active (e.g. was hidden)
            val fallbackIndex = pagerState.currentPage.coerceIn(0, safeActiveTabs.size - 1)
            val fallbackTab = safeActiveTabs[fallbackIndex]
            viewModel.selectLibraryTab(fallbackTab)
        }
    }

    // Sync selected tab with pager when user scrolls
    LaunchedEffect(pagerState, safeActiveTabs) {
        snapshotFlow { pagerState.settledPage }
            .collect { settledPage ->
                if (settledPage in safeActiveTabs.indices) {
                    val tab = safeActiveTabs[settledPage]
                    if (tab != uiState.selectedTab) {
                        viewModel.selectLibraryTab(tab)
                    }
                }
            }
    }

    var tabForBottomSheet by remember { mutableStateOf<LibraryTab?>(null) }

    Scaffold(
        topBar = {
            HomeTopBar(
                selectedTab = uiState.selectedTab,
                tabs = safeActiveTabs,
                onTabSelected = { index ->
                    if (index in safeActiveTabs.indices) {
                        viewModel.selectLibraryTab(safeActiveTabs[index])
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                },
                onTabLongClick = { tab ->
                    tabForBottomSheet = tab
                },
                onNavigateToPlaylists = onNavigateToPlaylists,
                onNavigateToSettings = onNavigateToSettings
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        tabForBottomSheet?.let { selectedTabForSheet ->
            val indexInActive = safeActiveTabs.indexOf(selectedTabForSheet)
            TabManagementBottomSheet(
                tab = selectedTabForSheet,
                tabIndex = indexInActive,
                totalTabs = safeActiveTabs.size,
                onMoveLeft = {
                    settingsViewModel.moveTabLeft(selectedTabForSheet.name)
                },
                onMoveRight = {
                    settingsViewModel.moveTabRight(selectedTabForSheet.name)
                },
                onHideTab = {
                    settingsViewModel.hideTab(selectedTabForSheet.name)
                },
                onDismiss = {
                    tabForBottomSheet = null
                }
            )
        }
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

                when {
                    !uiState.hasAudioPermission -> PermissionState(
                        onRequestAudioPermission = onRequestAudioPermission
                    )
                    uiState.isLoading -> LoadingState()
                    tab == LibraryTab.All -> AllSongsTab(
                        uiState = uiState,
                        viewModel = viewModel,
                        settingsViewModel = settingsViewModel,
                        tab = tab,
                        onNavigateToPlayer = onNavigateToPlayer,
                        sortBy = sortState[LibraryTab.All] ?: LibrarySortOption.TitleAz,
                        onSortChange = { option ->
                            sortState[LibraryTab.All] = option
                            settingsViewModel.setTabSortOption(LibraryTab.All.name, option.name)
                        },
                        availableStorageScopes = availableStorageScopes
                    )
                    else -> LibraryGroupsTab(
                        tab = tab,
                        groups = uiState.libraryGroups,
                        playerViewModel = viewModel,
                        settingsViewModel = settingsViewModel,
                        sortBy = sortState[tab] ?: LibrarySortOption.TitleAz,
                        onSortChange = { option ->
                            sortState[tab] = option
                            settingsViewModel.setTabSortOption(tab.name, option.name)
                        },
                        onGroupClick = { group ->
                            onNavigateToGroupDetail(tab, group.title)
                        },
                        availableStorageScopes = availableStorageScopes
                    )
                }
            }
        }
    }
}
