package com.music.player.ui.playlist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.music.player.data.entities.Song
import com.music.player.ui.library.LibraryViewModel
import com.music.player.ui.library.SongItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: LibraryViewModel,
    currentPlayingSongId: Long? = null,
    onSongClick: (Song, List<Song>) -> Unit,
    onBack: () -> Unit
) {
    val favoriteSongs by viewModel.favoriteSongs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(favoriteSongs) { song ->
                SongItem(
                    song = song,
                    isCurrentlyPlaying = song.id == currentPlayingSongId,
                    onClick = { onSongClick(song, favoriteSongs) },
                    onFavoriteClick = { viewModel.toggleFavorite(song) }
                )
            }
        }
    }
}
