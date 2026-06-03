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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.tosin.musicplayer.ui.components.MiniPlayer
import com.tosin.musicplayer.ui.components.StatusBarColorEffect
import com.tosin.musicplayer.ui.screens.*
import com.tosin.musicplayer.ui.state.LibraryTab
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import com.tosin.musicplayer.ui.viewmodel.SettingsViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import com.tosin.musicplayer.ui.viewmodel.EqualizerViewModel

@Composable
fun AppNavGraph(
    viewModel: PlayerViewModel,
    settingsViewModel: SettingsViewModel,
    equalizerViewModel: EqualizerViewModel,
    onRequestAudioPermission: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val playerUiState by viewModel.uiState.collectAsState()

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
            floatingActionButton = {
                if (currentRoute == "home") {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = fabLift)
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
            NavHost(
                navController, 
                startDestination = "home",
                modifier = Modifier.padding(paddingValues)
            ) {

            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    settingsViewModel = settingsViewModel,
                    onNavigateToPlayer = {
                        navController.navigate("player")
                    },
                    onNavigateToSettings = {
                        navController.navigate("settings")
                    },
                    onNavigateToGroupDetail = { tab, title ->
                        val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
                        navController.navigate("groupDetail/${tab.name}/$encodedTitle")
                    },
                    onRequestAudioPermission = onRequestAudioPermission,
                    onNavigateToSearch = {
                        navController.navigate("search")
                    },
                    onNavigateToPlaylists = {
                        navController.navigate("playlists")
                    }
                )
            }

            composable(
                route = "groupDetail/{tabName}/{groupTitle}"
            ) { backStackEntry ->
                val tabName = backStackEntry.arguments?.getString("tabName")
                val groupTitle = backStackEntry.arguments?.getString("groupTitle")?.let {
                    URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                }
                val tab = LibraryTab.valueOf(tabName ?: LibraryTab.Album.name)

                LibraryGroupDetailScreen(
                    viewModel = viewModel,
                    tab = tab,
                    groupTitle = groupTitle ?: "Unknown",
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPlayer = { navController.navigate("player") }
                )
            }

            composable(
                route = "player",
                enterTransition = {
                    slideInVertically { fullHeight -> fullHeight } + fadeIn()
                },
                exitTransition = {
                    slideOutVertically { fullHeight -> fullHeight } + fadeOut()
                },
                popEnterTransition = {
                    slideInVertically { fullHeight -> fullHeight } + fadeIn()
                },
                popExitTransition = {
                    slideOutVertically { fullHeight -> fullHeight } + fadeOut()
                }
            ) {
                PlayerScreen(
                    viewModel = viewModel,
                    onOpenPlaylist = {
                        navController.navigate("currentPlaylist")
                    },
                    onOpenLyrics = {
                        navController.navigate("lyrics")
                    },
                    onOpenVisualizer = {
                        navController.navigate("visualizer")
                    },
                    onOpenEqualizer = {
                        navController.navigate("equalizer")
                    },
                    onOpenSongEditor = { songId ->
                        navController.navigate("songEditor/$songId")
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "songEditor/{songId}",
                arguments = listOf(navArgument("songId") { type = NavType.LongType }),
                enterTransition = {
                    slideInVertically { it } + fadeIn()
                },
                exitTransition = {
                    slideOutVertically { it } + fadeOut()
                },
                popEnterTransition = {
                    slideInVertically { it } + fadeIn()
                },
                popExitTransition = {
                    slideOutVertically { it } + fadeOut()
                }
            ) { backStackEntry ->
                val songId = backStackEntry.arguments?.getLong("songId") ?: 0L
                SongEditorScreen(
                    viewModel = viewModel,
                    songId = songId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("equalizer") {
                EqualizerScreen(
                    viewModel = equalizerViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "lyricsEditor/{songId}",
                arguments = listOf(navArgument("songId") { type = NavType.LongType }),
                enterTransition = {
                    slideInVertically { it } + fadeIn()
                },
                exitTransition = {
                    slideOutVertically { it } + fadeOut()
                },
                popEnterTransition = {
                    slideInVertically { it } + fadeIn()
                },
                popExitTransition = {
                    slideOutVertically { it } + fadeOut()
                }
            ) { backStackEntry ->
                val songId = backStackEntry.arguments?.getLong("songId") ?: 0L
                LyricsEditorScreen(
                    viewModel = viewModel,
                    songId = songId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("currentPlaylist") {
                CurrentPlaylistScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onPlaySong = { song ->
                        viewModel.playSongFromQueue(song)
                        navController.popBackStack()
                    }
                )
            }

            composable("lyrics") {
                LyricsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onOpenVisualizer = { navController.navigate("visualizer") },
                    onOpenLyricsEditor = { songId ->
                        navController.navigate("lyricsEditor/$songId")
                    }
                )
            }

            composable("stats") {
                StatsScreen(
                    viewModel = viewModel,
                    onNavigateToHome = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    onNavigateToPlayer = {
                        navController.navigate("player")
                    }
                )
            }

            composable("visualizer") {
                VisualizerScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("settings") {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToGeneral = { navController.navigate("settings/general") },
                    onNavigateToAppearance = { navController.navigate("settings/appearance") },
                    onNavigateToPlayback = { navController.navigate("settings/playback") },
                    onNavigateToAbout = { navController.navigate("settings/about") }
                )
            }

            composable("settings/general") {
                GeneralSettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("settings/appearance") {
                AppearanceSettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("settings/playback") {
                PlaybackSettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("settings/about") {
                AboutSettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("search") {
                SearchScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPlayer = { navController.navigate("player") }
                )
            }

            composable("playlists") {
                PlaylistScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPlayer = { navController.navigate("player") },
                    onNavigateToPlaylistDetail = { playlistId ->
                        navController.navigate("playlistDetail/$playlistId")
                    }
                )
            }

            composable(
                route = "playlistDetail/{playlistId}"
            ) { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
                PlaylistDetailScreen(
                    viewModel = viewModel,
                    playlistId = playlistId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPlayer = { navController.navigate("player") }
                )
            }
            }
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
                }
            )
        }
    }
}
