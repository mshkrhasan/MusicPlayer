package com.music.player.ui.library

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.music.player.data.entities.Song
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    currentPlayingSongId: Long? = null,
    onSongClick: (Song) -> Unit,
    onSettingsClick: () -> Unit,
    onPlaylistsClick: () -> Unit
) {
    val songs by viewModel.songs.collectAsState()
    val audioFolders by viewModel.audioFolders.collectAsState()
    val videoFolders by viewModel.videoFolders.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val playlists by viewModel.playlists.collectAsState()

    val tabs = listOf("All Music", "Folders", "Video Folders")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    var selectedSongForPlaylist by rememberSaveable { mutableStateOf<Song?>(null) }

    val permissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.READ_MEDIA_AUDIO)
    } else {
        rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    // Notification Permission for Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        LaunchedEffect(notificationPermissionState.status) {
            if (!notificationPermissionState.status.isGranted) {
                notificationPermissionState.launchPermissionRequest()
            }
        }
    }

    LaunchedEffect(permissionState.status.isGranted) {
        if (permissionState.status.isGranted) {
            viewModel.refreshMedia()
        }
    }

    if (!permissionState.status.isGranted) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Permission required to access music")
                Spacer(modifier = Modifier.size(16.dp))
                androidx.compose.material3.Button(onClick = { permissionState.launchPermissionRequest() }) {
                    Text(text = "Grant Permission")
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                TopAppBar(
                    title = { Text("Library", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = onPlaylistsClick) {
                            Icon(Icons.AutoMirrored.Rounded.PlaylistPlay, contentDescription = "Playlists")
                        }

                        var showSortMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = { Text(order.name.replace("_", " ")) },
                                    onClick = {
                                        viewModel.onSortOrderChange(order)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                        
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                Icons.Rounded.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    }
                )
                TabRow(selectedTabIndex = pagerState.currentPage) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { 
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(title) }
                        )
                    }
                }
                SearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    onSearch = {},
                    active = false,
                    onActiveChange = {},
                    placeholder = { Text("Search songs, artists...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {}
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalAlignment = Alignment.Top
        ) { page ->
            when (page) {
                0 -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            if (songs.isNotEmpty()) {
                                Text(
                                    "Quick Picks",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(16.dp)
                                )
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    val quickPicks = songs.shuffled().take(10)
                                    items(quickPicks, key = { "quick_${it.id}" }) { song ->
                                        QuickPickItem(song = song, onClick = { onSongClick(song) })
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "All Songs",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                        items(songs, key = { it.id }) { song ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically { it / 2 }
                            ) {
                                SongItem(
                                    song = song,
                                    isCurrentlyPlaying = song.id == currentPlayingSongId,
                                    onClick = { onSongClick(song) },
                                    onFavoriteClick = { viewModel.toggleFavorite(song) },
                                    onDeleteClick = { viewModel.deleteSong(song) },
                                    onAddToPlaylistClick = { selectedSongForPlaylist = song }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    FolderList(
                        padding = PaddingValues(0.dp),
                        folders = audioFolders,
                        currentPlayingSongId = currentPlayingSongId,
                        onSongClick = onSongClick,
                        onFavoriteClick = viewModel::toggleFavorite,
                        onAddToPlaylistClick = { selectedSongForPlaylist = it }
                    )
                }
                2 -> {
                    FolderList(
                        padding = PaddingValues(0.dp),
                        folders = videoFolders,
                        currentPlayingSongId = currentPlayingSongId,
                        onSongClick = onSongClick,
                        onFavoriteClick = viewModel::toggleFavorite,
                        onAddToPlaylistClick = { selectedSongForPlaylist = it }
                    )
                }
            }
        }
    }

    if (selectedSongForPlaylist != null) {
        AlertDialog(
            onDismissRequest = { selectedSongForPlaylist = null },
            title = { Text("Add to Playlist") },
            text = {
                LazyColumn {
                    items(playlists) { playlist ->
                        Text(
                            text = playlist.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.addSongToPlaylist(selectedSongForPlaylist!!.id, playlist.id)
                                    selectedSongForPlaylist = null
                                }
                                .padding(16.dp)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedSongForPlaylist = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SongThumbnail(artworkUri: String?, title: String, isVideo: Boolean, size: Int = 60) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(size / 5))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (artworkUri != null) {
            // Background blur layer
            AsyncImage(
                model = artworkUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().blur(10.dp),
                contentScale = ContentScale.Crop,
                alpha = 0.5f
            )
            // Main artwork
            AsyncImage(
                model = artworkUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(0.85f).clip(RoundedCornerShape(size / 8)),
                contentScale = ContentScale.Crop
            )
        } else {
            val initial = title.firstOrNull()?.uppercase() ?: "?"
            Text(
                text = initial,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        if (isVideo) {
            Icon(
                Icons.Rounded.Videocam,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .size((size / 4).dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    .padding(2.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun QuickPickItem(song: Song, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        SongThumbnail(artworkUri = song.artworkUri, title = song.title, isVideo = false, size = 120)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun FolderList(
    padding: PaddingValues,
    folders: Map<String, List<Song>>,
    currentPlayingSongId: Long? = null,
    onSongClick: (Song) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        folders.forEach { (folderName, songs) ->
            item {
                var isExpanded by remember { mutableStateOf(false) }
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = folderName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(text = "${songs.size} tracks", style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(
                            if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                    if (isExpanded) {
                        songs.forEach { song ->
                            SongItem(
                                song = song,
                                isCurrentlyPlaying = song.id == currentPlayingSongId,
                                onClick = { onSongClick(song) },
                                onFavoriteClick = { onFavoriteClick(song) },
                                onDeleteClick = { /* Handled in LibraryScreen */ },
                                onAddToPlaylistClick = { onAddToPlaylistClick(song) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SongItem(
    song: Song,
    isCurrentlyPlaying: Boolean = false,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDeleteClick: () -> Unit = {},
    onAddToPlaylistClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isCurrentlyPlaying) 
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(
            1.dp, 
            if (isCurrentlyPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) 
            else Color.White.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SongThumbnail(artworkUri = song.artworkUri, title = song.title, isVideo = song.isVideo)

            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${song.artist} • ${song.album}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (song.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            var showMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Add to Playlist") },
                    onClick = {
                        onAddToPlaylistClick()
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete from Device") },
                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) },
                    onClick = {
                        onDeleteClick()
                        showMenu = false
                    }
                )
            }
            }
        }
    }
}
