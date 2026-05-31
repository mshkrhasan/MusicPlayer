package com.music.player

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import com.music.player.ui.navigation.MusicNavigation
import com.music.player.ui.player.PlayerViewModel
import com.music.player.ui.settings.SettingsViewModel
import com.music.player.ui.theme.MusicPlayerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val playerViewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start the PlaybackService to ensure MediaSession and notification are active
        val serviceIntent = Intent(this, com.music.player.playback.PlaybackService::class.java)
        startService(serviceIntent)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        handleIntent(intent)

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            MusicPlayerTheme(settingsViewModel = settingsViewModel) {
                MusicNavigation(playerViewModel = playerViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            playerViewModel.playExternalUri(uri)
            playerViewModel.triggerShowPlayer()
        }
    }
}
