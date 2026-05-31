package com.music.player.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.music.player.ui.library.LibraryScreen
import com.music.player.ui.library.LibraryViewModel
import com.music.player.ui.player.MiniPlayer
import com.music.player.ui.player.NowPlayingScreen
import com.music.player.ui.player.PlayerViewModel
import com.music.player.ui.settings.SettingsScreen
import com.music.player.ui.settings.SettingsViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object LibraryRoute

@Serializable
object SettingsRoute

@Serializable
object PlaylistRoute

@Serializable
data class PlaylistDetailRoute(val playlistId: Long, val playlistName: String)

@Composable
fun MusicNavigation(
    playerViewModel: PlayerViewModel = viewModel()
) {
    val libraryViewModel: LibraryViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()
    
    val tabs = listOf(LibraryRoute, PlaylistRoute, SettingsRoute)
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    
    val backStack = remember { mutableStateListOf<Any>(LibraryRoute) }
    val showNowPlaying by playerViewModel.showPlayer.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Column {
                // MiniPlayer above NavigationBar
                MiniPlayer(
                    viewModel = playerViewModel,
                    onClick = { playerViewModel.triggerShowPlayer() }
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    tabs.forEachIndexed { index, route ->
                        val isSelected = pagerState.currentPage == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                scope.launch { pagerState.animateScrollToPage(index) }
                            },
                            icon = {
                                when (route) {
                                    is LibraryRoute -> Icon(Icons.Rounded.LibraryMusic, null)
                                    is PlaylistRoute -> Icon(Icons.Rounded.PlaylistPlay, null)
                                    is SettingsRoute -> Icon(Icons.Rounded.Settings, null)
                                    else -> {}
                                }
                            },
                            label = {
                                Text(
                                    when (route) {
                                        is LibraryRoute -> "Library"
                                        is PlaylistRoute -> "Playlists"
                                        is SettingsRoute -> "Settings"
                                        else -> ""
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true // Allow swipe to change tab
            ) { page ->
                when (tabs[page]) {
                        is LibraryRoute -> {
                            val songs by libraryViewModel.songs.collectAsState()
                            val currentSong by playerViewModel.currentSong.collectAsState()
                            LibraryScreen(
                                viewModel = libraryViewModel,
                                currentPlayingSongId = currentSong?.id,
                                onSongClick = { song ->
                                    val index = songs.indexOf(song).coerceAtLeast(0)
                                    playerViewModel.playSong(songs, index)
                                    playerViewModel.triggerShowPlayer()
                                },
                                onSettingsClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                                onPlaylistsClick = { scope.launch { pagerState.animateScrollToPage(1) } }
                            )
                        }
                    is PlaylistRoute -> {
                        com.music.player.ui.playlist.PlaylistScreen(
                            viewModel = libraryViewModel,
                            onPlaylistClick = { playlist ->
                                backStack.add(PlaylistDetailRoute(playlist.id, playlist.name))
                            },
                            onFavoritesClick = {
                                backStack.add(com.music.player.ui.playlist.FavoritesRoute)
                            },
                            onBack = { /* Handled by system back or stack */ }
                        )
                    }
                    is SettingsRoute -> {
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onBack = { /* Handled by system back or stack */ }
                        )
                    }
                }
            }
            
            // Handle secondary navigation (Detail screens)
            if (backStack.size > 1) {
                val currentRoute = backStack.last()
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
                    when (currentRoute) {
                        is com.music.player.ui.playlist.FavoritesRoute -> {
                            val currentSong by playerViewModel.currentSong.collectAsState()
                            com.music.player.ui.playlist.FavoritesScreen(
                                viewModel = libraryViewModel,
                                currentPlayingSongId = currentSong?.id,
                                onSongClick = { song, songs ->
                                    val index = songs.indexOf(song).coerceAtLeast(0)
                                    playerViewModel.playSong(songs, index)
                                    playerViewModel.triggerShowPlayer()
                                },
                                onBack = { backStack.removeAt(backStack.size - 1) }
                            )
                        }
                        is PlaylistDetailRoute -> {
                            val currentSong by playerViewModel.currentSong.collectAsState()
                            com.music.player.ui.playlist.PlaylistDetailScreen(
                                playlist = com.music.player.data.entities.Playlist(id = currentRoute.playlistId, name = currentRoute.playlistName),
                                viewModel = libraryViewModel,
                                currentPlayingSongId = currentSong?.id,
                                onSongClick = { song, songs ->
                                    val index = songs.indexOf(song).coerceAtLeast(0)
                                    playerViewModel.playSong(songs, index)
                                    playerViewModel.triggerShowPlayer()
                                },
                                onBack = { backStack.removeAt(backStack.size - 1) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Handle back button for secondary navigation
    if (backStack.size > 1 && !showNowPlaying) {
        androidx.activity.compose.BackHandler {
            backStack.removeAt(backStack.size - 1)
        }
    }

    if (showNowPlaying) {
        androidx.activity.compose.BackHandler {
            playerViewModel.dismissPlayer()
        }
        NowPlayingScreen(
            viewModel = playerViewModel,
            onClose = { playerViewModel.dismissPlayer() }
        )
    }
}
