package com.humblebeeai.auralis.media

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.humblebeeai.auralis.data.entity.Song

object MediaScanner {
    /**
     * Scans the device for audio files and returns a list of Song entities.
     */
    fun scanMedia(context: Context): List<Song> {
        val songs = mutableListOf<Song>()
        // Only query necessary columns
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM
        )
        val queryUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        // Use a more efficient cursor loop
        context.contentResolver.query(queryUri, projection, null, null, null)?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getLong(idIndex)
                    val title = cursor.getString(titleIndex) ?: "Unknown"
                    val artist = cursor.getString(artistIndex)
                    val album = cursor.getString(albumIndex)
                    val contentUri = ContentUris.withAppendedId(queryUri, id).toString()
                    songs.add(Song(id, title, artist, album, contentUri))
                } while (cursor.moveToNext())
            }
        }
        // For very large libraries, consider paginating the query and batching inserts
        return songs
    }
}