package com.tosin.musicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tosin.musicplayer.data.local.MusicLoader
import com.tosin.musicplayer.data.repository.MusicRepository
import com.tosin.musicplayer.data.repository.PlaylistRepository
import com.tosin.musicplayer.data.repository.PreferencesRepository
import com.tosin.musicplayer.player.PlayerController
import com.tosin.musicplayer.ui.navigation.AppNavGraph
import com.tosin.musicplayer.ui.theme.TPlayerTheme
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel
import com.tosin.musicplayer.ui.viewmodel.SettingsViewModel
import com.tosin.musicplayer.data.repository.StatsRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val statsRepository = StatsRepository(this)
        val playlistRepository = PlaylistRepository(this)
        val preferencesRepository = PreferencesRepository(this)

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val musicLoader = MusicLoader(contentResolver)
                val repository = MusicRepository(musicLoader)
                val playerController = PlayerController(this@MainActivity, statsRepository)

                return when {
                    modelClass.isAssignableFrom(PlayerViewModel::class.java) -> {
                        PlayerViewModel(
                            repository,
                            playerController,
                            statsRepository,
                            playlistRepository,
                            preferencesRepository
                        ) as T
                    }
                    modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                        SettingsViewModel() as T
                    }
                    else -> throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }

        val playerViewModel = ViewModelProvider(this, factory)[PlayerViewModel::class.java]
        val settingsViewModel = ViewModelProvider(this, factory)[SettingsViewModel::class.java]

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

        setContent {
            TPlayerTheme {
                AppNavGraph(
                    viewModel = playerViewModel,
                    settingsViewModel = settingsViewModel,
                    onRequestAudioPermission = { permissionLauncher.launch(audioPermission) }
                )
            }
        }

        if (!hasAudioPermission) {
            permissionLauncher.launch(audioPermission)
        }
    }
}
