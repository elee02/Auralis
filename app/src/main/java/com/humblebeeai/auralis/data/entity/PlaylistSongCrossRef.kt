package com.humblebeeai.auralis.data.entity

import androidx.room.Entity

/**
 * Cross-reference between Playlist and Song for many-to-many relation.
 */
@Entity(tableName = "playlist_song_cross_ref", primaryKeys = ["playlistId", "songId"])
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long
)