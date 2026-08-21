package com.tosin.musicplayer.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tosin.musicplayer.ui.screens.*
import com.tosin.musicplayer.ui.screens.home.HomeScreen
import com.tosin.musicplayer.ui.state.LibraryTab
import com.tosin.musicplayer.ui.viewmodel.EqualizerViewModel
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import com.tosin.musicplayer.ui.viewmodel.SettingsViewModel
import com.tosin.musicplayer.ui.viewmodel.StatsViewModel
import com.tosin.musicplayer.ui.viewmodel.ThemeViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppRoutes(
    navController: NavHostController,
    viewModel: PlayerViewModel,
    settingsViewModel: SettingsViewModel,
    equalizerViewModel: EqualizerViewModel,
    statsViewModel: StatsViewModel,
    onRequestAudioPermission: () -> Unit,
    themeViewModel: ThemeViewModel? = null,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
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
                },
                onNavigateToFavorites = {
                    navController.navigate("favorites")
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
                statsViewModel = statsViewModel,
                playerViewModel = viewModel,
                onNavigateToHome = {
                    navController.popBackStack()
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
                themeViewModel = themeViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToThemeStudio = { navController.navigate("settings/theme_studio") }
            )
        }

        composable("settings/theme_studio") {
            if (themeViewModel != null) {
                ThemeStudioScreen(
                    viewModel = themeViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
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

        composable("favorites") {
            FavoritesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayer = { navController.navigate("player") }
            )
        }
    }
}
