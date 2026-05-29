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
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MusicVideo
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
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

    val showBottomNav = currentRoute in listOf("home", "stats")
    val showMiniPlayer = currentRoute != "player" && currentRoute != "lyrics"

    Scaffold(
        bottomBar = {
            Column(
                modifier = if (!showBottomNav) Modifier.navigationBarsPadding() else Modifier
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
                if (showBottomNav) {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                            selected = currentRoute == "home",
                            onClick = { 
                                if (currentRoute != "home") {
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Rounded.BarChart, contentDescription = "Stats") },
                            label = { Text("Stats") },
                            selected = currentRoute == "stats",
                            onClick = { navController.navigate("stats") }
                        )
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
                    onOpenEqualizer = {
                        navController.navigate("equalizer")
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("equalizer") {
                EqualizerScreen(
                    viewModel = equalizerViewModel,
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
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("stats") {
                StatsScreen(
                    viewModel = viewModel,
                    onNavigateToPlayer = { navController.navigate("player") }
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
