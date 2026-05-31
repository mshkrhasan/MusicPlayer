package com.music.player.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.music.player.data.dao.PlaylistDao
import com.music.player.data.dao.SongDao
import com.music.player.data.entities.Playlist
import com.music.player.data.entities.PlaylistSongCrossRef
import com.music.player.data.entities.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao
) {
    val allSongs: Flow<List<Song>> = songDao.getAllSongs()
    val favoriteSongs: Flow<List<Song>> = songDao.getFavoriteSongs()
    val playlists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    suspend fun getSongById(songId: Long): Song? = songDao.getSongById(songId)

    suspend fun scanMedia(includeVideo: Boolean = false) {
        withContext(Dispatchers.IO) {
            val songs = mutableListOf<Song>()
            
            // Scan Audio
            val audioProjection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.BUCKET_DISPLAY_NAME
            )

            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                audioProjection,
                "${MediaStore.Audio.Media.IS_MUSIC} != 0",
                null,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)
                    val artist = cursor.getString(artistColumn)
                    val album = cursor.getString(albumColumn)
                    val duration = cursor.getLong(durationColumn)
                    val albumId = cursor.getLong(albumIdColumn)
                    val bucketName = cursor.getString(bucketColumn) ?: "Internal"
                    val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString()
                    val artworkUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId).toString()

                    songs.add(Song(id, uri, title, artist, album, duration, artworkUri, folderName = bucketName))
                }
            }

            if (includeVideo) {
                // Scan Video
                val videoProjection = arrayOf(
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.TITLE,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.BUCKET_DISPLAY_NAME
                )

                context.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    videoProjection,
                    null,
                    null,
                    "${MediaStore.Video.Media.TITLE} ASC"
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                    val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                    val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                    val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val title = cursor.getString(titleColumn)
                        val duration = cursor.getLong(durationColumn)
                        val bucketName = cursor.getString(bucketColumn) ?: "Internal"
                        val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id).toString()

                        songs.add(Song(id, uri, title, "Video", "Videos", duration, null, isVideo = true, folderName = bucketName))
                    }
                }
            }

            songDao.insertSongs(songs)
        }
    }

    suspend fun deleteSong(song: Song) {
        withContext(Dispatchers.IO) {
            // Delete from Room
            songDao.deleteSongById(song.id)
            
            // Delete from Local Storage (if permitted)
            try {
                val uri = Uri.parse(song.uri)
                context.contentResolver.delete(uri, null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun toggleFavorite(song: Song) {
        songDao.setFavorite(song.id, !song.isFavorite)
    }

    suspend fun createPlaylist(name: String) {
        playlistDao.insertPlaylist(Playlist(name = name))
    }

    suspend fun addSongToPlaylist(songId: Long, playlistId: Long) {
        playlistDao.insertSongToPlaylist(PlaylistSongCrossRef(playlistId, songId))
    }

    suspend fun removeSongFromPlaylist(songId: Long, playlistId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }
    
    fun getPlaylistWithSongs(playlistId: Long) = playlistDao.getPlaylistWithSongs(playlistId)
}
