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
import com.tosin.musicplayer.player.PlayerController
import com.tosin.musicplayer.ui.navigation.AppNavGraph
import com.tosin.musicplayer.ui.theme.TPlayerTheme
import com.tosin.musicplayer.ui.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val musicLoader = MusicLoader(contentResolver)
                val repository = MusicRepository(musicLoader)
                val playerController = PlayerController(this@MainActivity)
                @Suppress("UNCHECKED_CAST")
                return PlayerViewModel(repository, playerController) as T
            }
        }

        val viewModel = ViewModelProvider(this, factory).get(PlayerViewModel::class.java)
        val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            viewModel.onAudioPermissionResult(isGranted)
        }
        val hasAudioPermission = ContextCompat.checkSelfPermission(
            this,
            audioPermission
        ) == PackageManager.PERMISSION_GRANTED

        viewModel.onAudioPermissionResult(hasAudioPermission)

        setContent {
            TPlayerTheme {
                AppNavGraph(
                    viewModel = viewModel,
                    onRequestAudioPermission = { permissionLauncher.launch(audioPermission) }
                )
            }
        }

        if (!hasAudioPermission) {
            permissionLauncher.launch(audioPermission)
        }
    }
}
