package com.tosin.musicplayer.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.tosin.musicplayer.ui.state.LibrarySortOption
import com.tosin.musicplayer.ui.state.LibraryTab
import com.tosin.musicplayer.ui.state.StorageScope
import com.tosin.musicplayer.ui.state.hasRemovableStorage
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import com.tosin.musicplayer.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

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

    var tabForBottomSheet by remember { mutableStateOf<LibraryTab?>(null) }

    Scaffold(
        topBar = {
            HomeTopBar(
                selectedTab = uiState.selectedTab,
                tabs = safeActiveTabs,
                onTabSelected = { index ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
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
                            onSortChange = { option ->
                                sortState[LibraryTab.All] = option
                                settingsViewModel.setTabSortOption(LibraryTab.All.name, option.name)
                            },
                            availableStorageScopes = availableStorageScopes
                        )
                        else -> LibraryGroupsTab(
                            tab = contentState.selectedTab,
                            groups = uiState.libraryGroups,
                            playerViewModel = viewModel,
                            settingsViewModel = settingsViewModel,
                            sortBy = sortState[contentState.selectedTab] ?: LibrarySortOption.TitleAz,
                            onSortChange = { option ->
                                sortState[contentState.selectedTab] = option
                                settingsViewModel.setTabSortOption(contentState.selectedTab.name, option.name)
                            },
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
