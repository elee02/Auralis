package com.humblebeeai.auralis.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.humblebeeai.auralis.data.entity.Song

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>): Unit

    @Query("SELECT * FROM songs")
    suspend fun getAllSongs(): List<Song>
}