package com.tosin.musicplayer.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tosin.musicplayer.ui.components.MiniPlayer
import com.tosin.musicplayer.ui.components.StatusBarColorEffect
import com.tosin.musicplayer.ui.theme.engine.customAppSurface
import com.tosin.musicplayer.ui.viewmodel.EqualizerViewModel
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import com.tosin.musicplayer.ui.viewmodel.SettingsViewModel
import com.tosin.musicplayer.ui.viewmodel.StatsViewModel
import com.tosin.musicplayer.ui.viewmodel.ThemeViewModel
import com.tosin.musicplayer.ui.icons.AppIcons

@Composable
fun AppNavGraph(
    viewModel: PlayerViewModel,
    settingsViewModel: SettingsViewModel,
    equalizerViewModel: EqualizerViewModel,
    statsViewModel: StatsViewModel,
    onRequestAudioPermission: () -> Unit,
    themeViewModel: ThemeViewModel? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val playerUiState by viewModel.uiState.collectAsState()

    var hasRestoredNav by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!hasRestoredNav) {
            hasRestoredNav = true
            val (savedRoute, _) = settingsViewModel.loadNavigationState()
            val validRoutes = setOf("home", "player", "settings", "stats", "playlists", "favorites", "search", "lyrics", "equalizer", "currentPlaylist", "settings/general", "settings/appearance", "settings/playback", "settings/about")
            if (savedRoute.isNotBlank() && savedRoute in validRoutes && savedRoute != "home") {
                navController.navigate(savedRoute) {
                    launchSingleTop = true
                }
            }
        }
    }

    LaunchedEffect(currentRoute) {
        if (!currentRoute.isNullOrBlank()) {
            val currentTab = viewModel.homeUiState.value.selectedTab.name
            settingsViewModel.saveNavigationState(currentRoute, currentTab)
        }
    }

    val showMiniPlayer = currentRoute != "player" && playerUiState.currentSong != null
    val fabLift by animateDpAsState(
        targetValue = if (showMiniPlayer) 104.dp else 0.dp,
        label = "miniPlayerFabLift"
    )

    when (currentRoute) {
        "player", "lyrics" -> Unit
        else -> StatusBarColorEffect(MaterialTheme.colorScheme.surface)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            floatingActionButton = {
                if (currentRoute == "home") {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(bottom = fabLift)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .customAppSurface(
                                    shape = MaterialTheme.shapes.small,
                                    backgroundColor = MaterialTheme.colorScheme.surface
                                )
                                .clickable { navController.navigate("stats") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                AppIcons.BarChart,
                                contentDescription = "Open Stats",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .customAppSurface(
                                    shape = MaterialTheme.shapes.small,
                                    backgroundColor = MaterialTheme.colorScheme.surface
                                )
                                .clickable { navController.navigate("search") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                AppIcons.Search,
                                contentDescription = "Open Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            AppRoutes(
                navController = navController,
                viewModel = viewModel,
                settingsViewModel = settingsViewModel,
                equalizerViewModel = equalizerViewModel,
                statsViewModel = statsViewModel,
                onRequestAudioPermission = onRequestAudioPermission,
                themeViewModel = themeViewModel,
                modifier = Modifier.padding(paddingValues)
            )
        }

        // MiniPlayer floating overlay – outside Scaffold so no background is drawn behind it
        AnimatedVisibility(
            visible = showMiniPlayer,
            enter = slideInVertically { fullHeight -> fullHeight } + fadeIn(),
            exit = slideOutVertically { fullHeight -> fullHeight } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            MiniPlayer(
                viewModel = viewModel,
                onExpand = {
                    navController.navigate("player")
                },
                onStop = {
                    viewModel.stop()
                },
                onCollapse = {
                    // Only collapse UI overlay if needed, do not stop audio playback
                }
            )
        }
    }
}
