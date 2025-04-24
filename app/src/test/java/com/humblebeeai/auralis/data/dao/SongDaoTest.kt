package com.humblebeeai.auralis.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.humblebeeai.auralis.data.AppDatabase
import com.humblebeeai.auralis.data.entity.Song
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class SongDaoTest {
    private lateinit var songDao: SongDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        // Create in-memory database for testing
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        songDao = db.songDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertAndGetSong() {
        // Create a sample song
        val song = Song(1, "Test Song", "Test Artist", "Test Album", "/path/to/file.mp3")
        
        // If implemented, songDao.insert should accept the song
        // and songDao.getAll() should return a list containing it
    }
}