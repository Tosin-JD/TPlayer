package com.tosin.musicplayer.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FloatingActionButton
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
import com.tosin.musicplayer.ui.viewmodel.EqualizerViewModel
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import com.tosin.musicplayer.ui.viewmodel.SettingsViewModel
import com.tosin.musicplayer.ui.viewmodel.StatsViewModel

@Composable
fun AppNavGraph(
    viewModel: PlayerViewModel,
    settingsViewModel: SettingsViewModel,
    equalizerViewModel: EqualizerViewModel,
    statsViewModel: StatsViewModel,
    onRequestAudioPermission: () -> Unit
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
            val validRoutes = setOf("home", "player", "settings", "stats", "playlists", "search", "visualizer", "lyrics", "equalizer", "currentPlaylist", "settings/general", "settings/appearance", "settings/playback", "settings/about")
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
        "player", "visualizer", "lyrics" -> Unit
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
                        FloatingActionButton(onClick = { navController.navigate("stats") }) {
                            Icon(Icons.Rounded.BarChart, contentDescription = "Open Stats")
                        }
                        FloatingActionButton(onClick = { navController.navigate("search") }) {
                            Icon(Icons.Rounded.Search, contentDescription = "Open Search")
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
