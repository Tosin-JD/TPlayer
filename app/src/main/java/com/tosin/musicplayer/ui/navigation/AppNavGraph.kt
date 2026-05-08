package com.tosin.musicplayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.tosin.musicplayer.ui.screens.HomeScreen
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
            PlayerScreen(viewModel, onOpenPlaylist = { /* no-op: implement playlist navigation if added */ })
        }

        composable("settings") {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
