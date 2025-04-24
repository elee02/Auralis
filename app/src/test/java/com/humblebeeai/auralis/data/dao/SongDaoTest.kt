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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import kotlinx.coroutines.runBlocking

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
    fun testInsertAndGetSong() = runBlocking {
        val song = Song(1, "Test Song", "Test Artist", "Test Album", "/path/to/file.mp3")
        songDao.insert(song)
        val allSongs = songDao.getAllSongs()
        assertEquals(1, allSongs.size)
        val retrieved = allSongs[0]
        assertEquals(song.id, retrieved.id)
        assertEquals(song.title, retrieved.title)
        assertEquals(song.artist, retrieved.artist)
        assertEquals(song.album, retrieved.album)
        assertEquals(song.uri, retrieved.uri)
    }

    @Test
    fun testUpdateSong() = runBlocking {
        val song = Song(2, "Old Title", "Old Artist", "Old Album", "/old/path.mp3")
        songDao.insert(song)
        val updated = Song(2, "New Title", "New Artist", "New Album", "/new/path.mp3")
        songDao.update(updated)
        val allSongs = songDao.getAllSongs()
        assertEquals(1, allSongs.size)
        val retrieved = allSongs[0]
        assertEquals(updated.title, retrieved.title)
        assertEquals(updated.artist, retrieved.artist)
        assertEquals(updated.album, retrieved.album)
        assertEquals(updated.uri, retrieved.uri)
    }

    @Test
    fun testDeleteSong() = runBlocking {
        val song = Song(3, "Delete Me", "Artist", "Album", "/delete.mp3")
        songDao.insert(song)
        songDao.delete(song)
        val allSongs = songDao.getAllSongs()
        assertTrue(allSongs.isEmpty())
    }

    @Test
    fun testQueryByArtist() = runBlocking {
        val song1 = Song(4, "Song A", "Artist X", "Album 1", "/a.mp3")
        val song2 = Song(5, "Song B", "Artist Y", "Album 2", "/b.mp3")
        val song3 = Song(6, "Song C", "Artist X", "Album 3", "/c.mp3")
        songDao.insert(song1)
        songDao.insert(song2)
        songDao.insert(song3)
        val artistXSongs = songDao.findByArtist("Artist X")
        assertEquals(2, artistXSongs.size)
        assertTrue(artistXSongs.any { it.title == "Song A" })
        assertTrue(artistXSongs.any { it.title == "Song C" })
    }
}