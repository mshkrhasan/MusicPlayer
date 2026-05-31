package com.music.player.ui.player

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.music.player.data.SettingsManager
import com.music.player.data.entities.Song
import com.music.player.data.repository.MusicRepository
import com.music.player.playback.PlaybackService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val player: Player,
    private val repository: MusicRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _showPlayer = MutableStateFlow(false)
    val showPlayer: StateFlow<Boolean> = _showPlayer.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null

    private val listener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val song = mediaItem?.localConfiguration?.tag as? Song
            _currentSong.value = song
            updateState()
            
            // Save last song
            song?.let {
                viewModelScope.launch {
                    settingsManager.saveLastPlaybackState(it.id, player.currentPosition)
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateState()
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            updateState()
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            updateState()
        }
    }

    init {
        player.addListener(listener)
        
        // Connect to MediaSession to ensure notification is managed by system correctly
        val sessionToken = SessionToken(context, android.content.ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            // Controller is connected, helps with system integration
        }, MoreExecutors.directExecutor())

        // Restore last state only if nothing is currently playing (e.g. from an external intent)
        viewModelScope.launch {
            delay(500) // Give a small buffer for external intents to arrive
            if (player.mediaItemCount == 0) {
                val lastId = settingsManager.lastSongId.first()
                val lastPos = settingsManager.lastPosition.first()
                if (lastId != null && lastId != -1L) {
                    val song = repository.getSongById(lastId)
                    if (song != null) {
                        prepareLastSong(song, lastPos)
                    }
                }
            }
        }

        viewModelScope.launch {
            while (isActive) {
                if (player.isPlaying) {
                    updateState()
                    // Periodically save position only for valid library songs
                    currentSong.value?.let {
                        if (it.id != -1L) {
                            settingsManager.saveLastPlaybackState(it.id, player.currentPosition)
                        }
                    }
                }
                delay(1000)
            }
        }
    }

    private fun prepareLastSong(song: Song, position: Long) {
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setArtworkUri(song.artworkUri?.let { Uri.parse(it) })
                    .build()
            )
            .setTag(song)
            .build()
        player.setMediaItem(mediaItem)
        player.seekTo(position)
        player.prepare()
        _currentSong.value = song
    }

    private fun updateState() {
        _playbackState.value = PlaybackState(
            isPlaying = player.isPlaying,
            currentPosition = player.currentPosition,
            duration = player.duration.coerceAtLeast(0),
            shuffleModeEnabled = player.shuffleModeEnabled,
            repeatMode = player.repeatMode
        )
    }

    fun triggerShowPlayer() {
        _showPlayer.value = true
    }

    fun dismissPlayer() {
        _showPlayer.value = false
    }

    fun playSong(songs: List<Song>, startIndex: Int) {
        val selectedSong = songs.getOrNull(startIndex) ?: return
        
        if (_currentSong.value?.id == selectedSong.id) {
            // If the song is already loaded, just play it if it's paused
            if (!player.isPlaying) {
                player.play()
            }
            return
        }

        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .setArtworkUri(song.artworkUri?.let { Uri.parse(it) })
                        .build()
                )
                .setTag(song)
                .build()
        }
        player.setMediaItems(mediaItems, startIndex, 0)
        player.prepare()
        player.play()
    }

    fun playExternalUri(uri: Uri) {
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(uri.lastPathSegment ?: "External File")
                    .build()
            )
            .build()
        
        val song = Song(
            id = -1,
            uri = uri.toString(),
            title = uri.lastPathSegment ?: "External File",
            artist = "External",
            album = "External",
            duration = 0,
            artworkUri = null
        )
        
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        _currentSong.value = song
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    fun skipToNext() {
        player.seekToNext()
    }

    fun skipToPrevious() {
        player.seekToPrevious()
    }

    fun toggleShuffle() {
        player.shuffleModeEnabled = !player.shuffleModeEnabled
    }

    fun toggleRepeat() {
        player.repeatMode = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.removeListener(listener)
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }
}

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val shuffleModeEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF
)
