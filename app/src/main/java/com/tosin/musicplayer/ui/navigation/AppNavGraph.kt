package com.tosin.musicplayer.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.tosin.musicplayer.ui.components.MiniPlayer
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

    val showMiniPlayer = currentRoute != "player"

    Scaffold(
        floatingActionButton = {
            if (currentRoute == "home") {
                FloatingActionButton(onClick = { navController.navigate("stats") }) {
                    Icon(Icons.Rounded.BarChart, contentDescription = "Open Stats")
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier.navigationBarsPadding()
            ) {
                AnimatedVisibility(
                    visible = showMiniPlayer,
                    enter = slideInVertically { fullHeight -> fullHeight } + fadeIn(),
                    exit = slideOutVertically { fullHeight -> fullHeight } + fadeOut()
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
}
