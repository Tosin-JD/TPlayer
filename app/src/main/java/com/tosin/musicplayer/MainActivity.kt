package com.tosin.musicplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.tosin.musicplayer.data.local.MusicLoader
import com.tosin.musicplayer.data.repository.MusicRepository
import com.tosin.musicplayer.data.repository.PlaylistRepository
import com.tosin.musicplayer.data.repository.PreferencesRepository
import com.tosin.musicplayer.player.PlayerController
import com.tosin.musicplayer.ui.theme.AppThemePreset
import com.tosin.musicplayer.ui.navigation.AppNavGraph
import com.tosin.musicplayer.ui.theme.TPlayerTheme
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import com.tosin.musicplayer.ui.viewmodel.SettingsViewModel
import com.tosin.musicplayer.ui.viewmodel.EqualizerViewModel
import com.tosin.musicplayer.ui.viewmodel.StatsViewModel
import com.tosin.musicplayer.data.repository.StatsRepository
import com.tosin.musicplayer.data.repository.ThemeDataStoreRepository
import com.tosin.musicplayer.ui.theme.engine.UniversalAppTheme
import com.tosin.musicplayer.ui.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {
    private lateinit var playerViewModel: PlayerViewModel
    private lateinit var playerController: PlayerController
    private lateinit var musicRepository: MusicRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val statsRepository = StatsRepository(this)
        val playlistRepository = PlaylistRepository(this)
        val preferencesRepository = PreferencesRepository(this)
        val themeDataStoreRepository = ThemeDataStoreRepository(this)
        val musicLoader = MusicLoader(contentResolver)
        playerController = PlayerController(this@MainActivity, statsRepository, preferencesRepository)
        musicRepository = MusicRepository(musicLoader, preferencesRepository, statsRepository, this)

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(PlayerViewModel::class.java) -> {
                        PlayerViewModel(
                            musicRepository,
                            playerController,
                            playlistRepository,
                            preferencesRepository
                        ) as T
                    }
                    modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                        SettingsViewModel(preferencesRepository, musicRepository) as T
                    }
                    modelClass.isAssignableFrom(EqualizerViewModel::class.java) -> {
                        EqualizerViewModel(preferencesRepository) as T
                    }
                    modelClass.isAssignableFrom(StatsViewModel::class.java) -> {
                        StatsViewModel(musicRepository, statsRepository) as T
                    }
                    modelClass.isAssignableFrom(ThemeViewModel::class.java) -> {
                        ThemeViewModel(themeDataStoreRepository) as T
                    }
                    else -> throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }

        playerViewModel = ViewModelProvider(this, factory)[PlayerViewModel::class.java]
        val settingsViewModel = ViewModelProvider(this, factory)[SettingsViewModel::class.java]
        val equalizerViewModel = ViewModelProvider(this, factory)[EqualizerViewModel::class.java]
        val statsViewModel = ViewModelProvider(this, factory)[StatsViewModel::class.java]
        val themeViewModel = ViewModelProvider(this, factory)[ThemeViewModel::class.java]

        val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            playerViewModel.onAudioPermissionResult(isGranted)
        }
        val hasAudioPermission = ContextCompat.checkSelfPermission(
            this,
            audioPermission
        ) == PackageManager.PERMISSION_GRANTED

        playerViewModel.onAudioPermissionResult(hasAudioPermission)
        handleIncomingIntent(intent)

        setContent {
            val settingsUiState by settingsViewModel.uiState.collectAsState()
            val themeState by themeViewModel.themeState.collectAsState()

            androidx.compose.runtime.LaunchedEffect(settingsUiState.pauseOnZeroVolume) {
                playerViewModel.setPauseOnZeroVolumeEnabled(settingsUiState.pauseOnZeroVolume)
            }
            androidx.compose.runtime.LaunchedEffect(settingsUiState.autoResumeEnabled) {
                playerViewModel.setAutoResumeEnabled(settingsUiState.autoResumeEnabled)
            }
            androidx.compose.runtime.LaunchedEffect(settingsUiState.excludedFolders) {
                playerViewModel.setExcludedFolders(settingsUiState.excludedFolders.toSet())
                playerViewModel.refreshLibrary()
            }

            UniversalAppTheme(
                state = themeState
            ) {
                AppNavGraph(
                    viewModel = playerViewModel,
                    settingsViewModel = settingsViewModel,
                    equalizerViewModel = equalizerViewModel,
                    statsViewModel = statsViewModel,
                    themeViewModel = themeViewModel,
                    onRequestAudioPermission = { permissionLauncher.launch(audioPermission) }
                )
            }
        }

        if (!hasAudioPermission) {
            permissionLauncher.launch(audioPermission)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val nonNullIntent = intent ?: return
        val uri = when (nonNullIntent.action) {
            Intent.ACTION_VIEW -> nonNullIntent.data
            Intent.ACTION_SEND -> nonNullIntent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            Intent.ACTION_SEND_MULTIPLE -> nonNullIntent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.firstOrNull()
            else -> null
        } ?: return

        val type = nonNullIntent.type?.lowercase().orEmpty()
        if (nonNullIntent.action == Intent.ACTION_VIEW || type.startsWith("audio/") || type.startsWith("video/")) {
            playerViewModel.playUri(uri)
        }
    }
}
