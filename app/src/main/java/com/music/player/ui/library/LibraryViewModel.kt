package com.music.player.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.player.data.entities.Song
import com.music.player.data.repository.MusicRepository
import com.music.player.data.SettingsManager
import com.music.player.data.entities.Playlist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _sortOrder = MutableStateFlow(SortOrder.TITLE_ASC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    val videoToAudioEnabled: StateFlow<Boolean> = settingsManager.videoToAudioEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val songs: StateFlow<List<Song>> = combine(
        repository.allSongs,
        _searchQuery,
        _sortOrder,
        videoToAudioEnabled
    ) { allSongs, query, sort, includeVideos ->
        var filteredList = if (includeVideos) {
            allSongs
        } else {
            allSongs.filter { !it.isVideo }
        }

        if (query.isNotEmpty()) {
            filteredList = filteredList.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true)
            }
        }

        when (sort) {
            SortOrder.TITLE_ASC -> filteredList.sortedBy { it.title }
            SortOrder.TITLE_DESC -> filteredList.sortedByDescending { it.title }
            SortOrder.ARTIST_ASC -> filteredList.sortedBy { it.artist }
            SortOrder.ALBUM_ASC -> filteredList.sortedBy { it.album }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val audioFolders: StateFlow<Map<String, List<Song>>> = songs
        .map { list -> list.filter { !it.isVideo }.groupBy { it.folderName } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val videoFolders: StateFlow<Map<String, List<Song>>> = songs
        .map { list -> list.filter { it.isVideo }.groupBy { it.folderName } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val playlists: StateFlow<List<Playlist>> = repository.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = repository.favoriteSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Automatically refresh when video toggle changes
        viewModelScope.launch {
            videoToAudioEnabled.collect { enabled ->
                repository.scanMedia(includeVideo = enabled)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSortOrderChange(order: SortOrder) {
        _sortOrder.value = order
    }

    fun refreshMedia() {
        viewModelScope.launch {
            repository.scanMedia(includeVideo = videoToAudioEnabled.value)
        }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            repository.deleteSong(song)
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song)
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun addSongToPlaylist(songId: Long, playlistId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(songId, playlistId)
        }
    }

    fun getPlaylistWithSongs(playlistId: Long) = repository.getPlaylistWithSongs(playlistId)
}

enum class SortOrder {
    TITLE_ASC, TITLE_DESC, ARTIST_ASC, ALBUM_ASC
}
