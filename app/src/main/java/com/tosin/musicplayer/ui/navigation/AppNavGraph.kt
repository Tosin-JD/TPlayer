package com.tosin.musicplayer.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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

@Composable
fun AppNavGraph(
    viewModel: PlayerViewModel,
    settingsViewModel: SettingsViewModel,
    onRequestAudioPermission: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Routes where bottom bar should be hidden
    val hideBottomBar = currentRoute in listOf("player", "lyrics", "search")

    Scaffold(
        bottomBar = {
            if (!hideBottomBar) {
                Column {
                    MiniPlayer(
                        viewModel = viewModel,
                        onClick = { navController.navigate("player") }
                    )
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

            composable("player") {
                PlayerScreen(
                    viewModel = viewModel,
                    onOpenPlaylist = {
                        navController.navigate("currentPlaylist")
                    },
                    onOpenLyrics = {
                        navController.navigate("lyrics")
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
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
