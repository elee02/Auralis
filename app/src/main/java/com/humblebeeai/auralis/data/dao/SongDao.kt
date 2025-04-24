package com.humblebeeai.auralis.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.humblebeeai.auralis.data.entity.Song

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: Song)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>)

    @Query("SELECT * FROM songs")
    suspend fun getAllSongs(): List<Song>

    @Query("SELECT * FROM songs WHERE artist = :artist")
    suspend fun findByArtist(artist: String): List<Song>

    @Update
    suspend fun update(song: Song)

    @Delete
    suspend fun delete(song: Song)
}