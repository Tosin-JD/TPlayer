package com.tosin.musicplayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.tosin.musicplayer.ui.screens.HomeScreen
import com.tosin.musicplayer.ui.screens.PlayerScreen
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel

@Composable
fun AppNavGraph(viewModel: PlayerViewModel) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "home") {

        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToPlayer = {
                    navController.navigate("player")
                }
            )
        }

        composable("player") {
            PlayerScreen(viewModel)
        }
    }
}