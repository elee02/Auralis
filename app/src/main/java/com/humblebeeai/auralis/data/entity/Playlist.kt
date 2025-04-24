package com.humblebeeai.auralis.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
 data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)