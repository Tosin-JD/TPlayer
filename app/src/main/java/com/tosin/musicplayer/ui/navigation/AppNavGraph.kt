package com.tosin.musicplayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.tosin.musicplayer.ui.screens.CurrentPlaylistScreen
import com.tosin.musicplayer.ui.screens.HomeScreen
import com.tosin.musicplayer.ui.screens.LibraryGroupDetailScreen
import com.tosin.musicplayer.ui.screens.LyricsScreen
import com.tosin.musicplayer.ui.screens.PlayerScreen
import com.tosin.musicplayer.ui.screens.SettingsScreen
import com.tosin.musicplayer.ui.state.LibraryTab
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import com.tosin.musicplayer.ui.viewmodel.SettingsViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavGraph(
    viewModel: PlayerViewModel,
    settingsViewModel: SettingsViewModel = viewModel(),
    onRequestAudioPermission: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "home") {

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
                onRequestAudioPermission = onRequestAudioPermission
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

        composable("settings") {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
