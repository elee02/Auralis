package com.humblebeeai.auralis.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.humblebeeai.auralis.data.AppDatabase
import com.humblebeeai.auralis.media.MediaScanner
import com.humblebeeai.auralis.data.entity.Song
import com.humblebeeai.auralis.R
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val recycler = findViewById<RecyclerView>(R.id.recyclerViewSongs)
        recycler.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            // scan device and populate database
            val songs = MediaScanner.scanMedia(this@MainActivity)
            AppDatabase.getInstance(this@MainActivity).songDao().insertSongs(songs)
            val allSongs = AppDatabase.getInstance(this@MainActivity).songDao().getAllSongs()

            val adapter = SongAdapter(allSongs) { song: Song ->
                val intent = Intent(this@MainActivity, NowPlayingActivity::class.java).apply {
                    putParcelableArrayListExtra("songs", ArrayList(allSongs))
                    putExtra("currentIndex", allSongs.indexOf(song))
                }
                startActivity(intent)
            }
            recycler.adapter = adapter
        }
    }
}