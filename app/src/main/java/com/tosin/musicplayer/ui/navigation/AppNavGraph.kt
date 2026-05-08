package com.tosin.musicplayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.tosin.musicplayer.ui.screens.CurrentPlaylistScreen
import com.tosin.musicplayer.ui.screens.HomeScreen
import com.tosin.musicplayer.ui.screens.LyricsScreen
import com.tosin.musicplayer.ui.screens.PlayerScreen
import com.tosin.musicplayer.ui.screens.SettingsScreen
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import com.tosin.musicplayer.ui.viewmodel.SettingsViewModel

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
                onRequestAudioPermission = onRequestAudioPermission
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
