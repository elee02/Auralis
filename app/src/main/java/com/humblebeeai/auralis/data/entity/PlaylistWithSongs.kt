package com.humblebeeai.auralis.data.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Represents a Playlist and its associated Songs.
 */
data class PlaylistWithSongs(
    @Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = androidx.room.Junction(
            value = PlaylistSongCrossRef::class,
            parentColumn = "playlistId",
            entityColumn = "songId"
        )
    )
    val songs: List<Song>
)