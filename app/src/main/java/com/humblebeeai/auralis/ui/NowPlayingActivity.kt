package com.humblebeeai.auralis.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.humblebeeai.auralis.R
import com.humblebeeai.auralis.data.AppDatabase
import com.humblebeeai.auralis.data.entity.Playlist
import com.humblebeeai.auralis.data.entity.PlaylistSongCrossRef
import com.humblebeeai.auralis.data.entity.Song
import com.humblebeeai.auralis.lyrics.fetcher.LyricsFetcher
import com.humblebeeai.auralis.lyrics.parser.EmbeddedLyricsExtractor
import com.humblebeeai.auralis.lyrics.parser.LrcParser
import com.humblebeeai.auralis.lyrics.renderer.SynchronizedLyricsView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import android.content.ComponentName
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import com.humblebeeai.auralis.audio.MusicService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

class NowPlayingActivity : AppCompatActivity() {
    private lateinit var lyricsView: SynchronizedLyricsView
    private lateinit var titleText: TextView
    private lateinit var artistText: TextView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnPrev: ImageButton

    private var songs: List<Song> = emptyList()
    private var currentIndex = 0
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_now_playing)

        titleText = findViewById(R.id.textNowTitle)
        artistText = findViewById(R.id.textNowArtist)
        lyricsView = findViewById(R.id.lyricsView)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnNext = findViewById(R.id.btnNext)
        btnPrev = findViewById(R.id.btnPrev)

        @Suppress("DEPRECATION")
        songs = intent.getParcelableArrayListExtra("songs") ?: emptyList()
        currentIndex = intent.getIntExtra("currentIndex", 0)
        
        // Build MediaController bound to MusicService
        val token = SessionToken(this, ComponentName(this, MusicService::class.java))
        controllerFuture = MediaController.Builder(this, token)
            .buildAsync()
            
        controllerFuture.addListener({
            val ctrl = controllerFuture.get()
            // listen for state changes to update UI
            ctrl.addListener(object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    updateSongInfo()
                }
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    btnPlayPause.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
                }
            })
            
            // Prepare playback queue
            val mediaItems = songs.map { song ->
                MediaItem.Builder()
                    .setUri(song.uri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(song.title)
                            .setArtist(song.artist)
                            .build()
                    )
                    .build()
            }
            ctrl.setMediaItems(mediaItems, currentIndex, 0L)
            ctrl.prepare()
            ctrl.play()
        }, MoreExecutors.directExecutor())

        btnPlayPause.setOnClickListener {
            controller?.let { ctrl ->
                if (ctrl.isPlaying) {
                    ctrl.pause()
                } else {
                    ctrl.play()
                }
            }
        }
        
        btnNext.setOnClickListener {
            controller?.seekToNextMediaItem()
        }
        
        btnPrev.setOnClickListener {
            controller?.seekToPreviousMediaItem()
        }

        lifecycleScope.launch {
            loadLyrics()
        }

        lifecycleScope.launch {
            while (true) {
                controller?.let { ctrl ->
                    lyricsView.updateTime(ctrl.currentPosition)
                }
                delay(200)
            }
        }
    }

    private fun updateSongInfo() {
        controller?.let { ctrl ->
            val idx = ctrl.currentMediaItemIndex
            if (idx in songs.indices) {
                val song = songs[idx]
                titleText.text = song.title
                artistText.text = song.artist ?: ""
            }
        }
    }

    private suspend fun loadLyrics() {
        delay(500) // Wait for controller to be ready
        controller?.let { ctrl ->
            // get current song from mediaController
            val idx = ctrl.currentMediaItemIndex
            if (idx !in songs.indices) return
            val song = songs[idx]
            val embedded = EmbeddedLyricsExtractor.extract(this, song.uri)
            val content = embedded ?: withContext(Dispatchers.IO) {
                LyricsFetcher.fetchOnline(song)
            }
            content?.let {
                val lines = LrcParser.parse(it)
                lyricsView.setLyrics(lines)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.menu_equalizer -> startActivity(Intent(this, EqualizerActivity::class.java))
            R.id.menu_add_to_playlist -> showAddToPlaylistDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAddToPlaylistDialog() {
        // Display a temporary toast message since playlist functionality isn't implemented in the database yet
        Toast.makeText(
            this@NowPlayingActivity,
            "Playlist functionality will be available in a future update",
            Toast.LENGTH_SHORT
        ).show()
        
        /* The commented section below shows how playlist functionality would be implemented
        // This code will work once you add Playlist and PlaylistSongCrossRef entities to your Room database
        // and implement the PlaylistDao interface with the necessary methods
        
        val db = AppDatabase.getInstance(this)
        
        lifecycleScope.launch {
            controller?.let { ctrl ->
                val idx = ctrl.currentMediaItemIndex
                if (idx >= 0 && idx < songs.size) {
                    val song = songs[idx]
                    
                    // Instead of using playlists, let's just show the song information
                    val songInfo = "Song: ${song.title} by ${song.artist ?: "Unknown"}"
                    
                    withContext(Dispatchers.Main) {
                        AlertDialog.Builder(this@NowPlayingActivity)
                            .setTitle("Selected Song")
                            .setMessage(songInfo)
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            }
        }
        */
    }
    
    override fun onDestroy() {
        super.onDestroy()
        MediaController.releaseFuture(controllerFuture)
    }
}