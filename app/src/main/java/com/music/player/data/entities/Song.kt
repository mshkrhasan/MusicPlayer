package com.music.player.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: Long,
    val uri: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val artworkUri: String?,
    val isFavorite: Boolean = false,
    val isVideo: Boolean = false,
    val folderName: String = "Unknown"
)
