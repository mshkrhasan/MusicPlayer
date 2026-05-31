package com.music.player.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.music.player.data.dao.PlaylistDao
import com.music.player.data.dao.SongDao
import com.music.player.data.entities.Playlist
import com.music.player.data.entities.PlaylistSongCrossRef
import com.music.player.data.entities.Song

@Database(
    entities = [Song::class, Playlist::class, PlaylistSongCrossRef::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        const val DATABASE_NAME = "music_player_db"
    }
}
