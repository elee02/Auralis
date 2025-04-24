package com.humblebeeai.auralis.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.humblebeeai.auralis.data.AppDatabase
import com.humblebeeai.auralis.media.MediaScanner
import com.humblebeeai.auralis.data.entity.Song
import com.humblebeeai.auralis.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val REQUEST_AUDIO_PERMISSION = 101
        private const val TAG = "MainActivity"
    }
    
    private lateinit var debugTextView: TextView
    private lateinit var recyclerView: RecyclerView
    
    // Pick the right permission for the current OS level
    private val audioPermission: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize views
        debugTextView = findViewById(R.id.debugTextView)
        recyclerView = findViewById(R.id.recyclerViewSongs)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // First update the debug text
        debugTextView.text = "Checking permissions..."
        
        checkAudioPermissionAndLoad()
    }
    
    private fun checkAudioPermissionAndLoad() {
        if (ContextCompat.checkSelfPermission(this, audioPermission) != PackageManager.PERMISSION_GRANTED) {
            debugTextView.text = "Requesting permission..."
            ActivityCompat.requestPermissions(
                this,
                arrayOf(audioPermission),
                REQUEST_AUDIO_PERMISSION
            )
        } else {
            debugTextView.text = "Permission granted, scanning music..."
            loadSongs()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == REQUEST_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                debugTextView.text = "Permission granted, scanning music..."
                loadSongs()
            } else {
                debugTextView.text = "Permission denied. Cannot load music."
                Toast.makeText(this, "Permission denied. Cannot load music.", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun loadSongs() {
        lifecycleScope.launch {
            try {
                debugTextView.text = "Scanning for songs..."
                Log.d(TAG, "Starting media scan")
                
                val mediaScanner = MediaScanner(this@MainActivity)
                val songs = mediaScanner.scanMedia()
                debugTextView.text = "Found ${songs.size} songs, saving to database..."
                Log.d(TAG, "Found ${songs.size} songs")
                
                AppDatabase.getInstance(this@MainActivity).songDao().insertSongs(songs)
                debugTextView.text = "Loading songs from database..."
                
                val allSongs = AppDatabase.getInstance(this@MainActivity).songDao().getAllSongs()
                Log.d(TAG, "Loaded ${allSongs.size} songs from database")
                
                withContext(Dispatchers.Main) {
                    if (allSongs.isEmpty()) {
                        debugTextView.text = "No songs found. Please add music to your device."
                    } else {
                        debugTextView.text = "Displaying ${allSongs.size} songs"
                        
                        val adapter = SongAdapter(allSongs) { song: Song ->
                            val intent = Intent(this@MainActivity, NowPlayingActivity::class.java).apply {
                                putParcelableArrayListExtra("songs", ArrayList(allSongs))
                                putExtra("currentIndex", allSongs.indexOf(song))
                            }
                            startActivity(intent)
                        }
                        recyclerView.adapter = adapter
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading songs", e)
                withContext(Dispatchers.Main) {
                    debugTextView.text = "Error: ${e.message}"
                    Toast.makeText(this@MainActivity, "Error loading songs: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}