package com.humblebeeai.auralis.data.dao

import androidx.room.*
import com.humblebeeai.auralis.data.entity.Playlist
import com.humblebeeai.auralis.data.entity.PlaylistSongCrossRef
import com.humblebeeai.auralis.data.entity.PlaylistWithSongs

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(ref: PlaylistSongCrossRef)

    @Transaction
    @Query("SELECT * FROM playlists")
    suspend fun getPlaylistsWithSongs(): List<PlaylistWithSongs>
}