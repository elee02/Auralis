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
    private lateinit var btnSkipBack: ImageButton
    private lateinit var btnSkipForward: ImageButton
    private lateinit var btnOffsetMinus: android.widget.Button
    private lateinit var btnOffsetPlus: android.widget.Button
    private lateinit var textLyricsOffset: android.widget.TextView
    private lateinit var btnImportEditLyrics: android.widget.Button
    private var lyricsOffsetMs: Long = 0L
    private val OFFSET_STEP_MS = 500L
    private val OFFSET_MIN_MS = -10000L
    private val OFFSET_MAX_MS = 10000L
    private var currentLyricsRaw: String? = null

    companion object {
        private const val SKIP_INTERVAL_MS = 10_000L // 10 seconds, configurable
    }

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
        btnSkipBack = findViewById(R.id.btnSkipBack)
        btnSkipForward = findViewById(R.id.btnSkipForward)
        btnOffsetMinus = findViewById(R.id.btnOffsetMinus)
        btnOffsetPlus = findViewById(R.id.btnOffsetPlus)
        textLyricsOffset = findViewById(R.id.textLyricsOffset)
        btnImportEditLyrics = findViewById(R.id.btnImportEditLyrics)
        btnImportEditLyrics.setOnClickListener {
            showLyricsImportEditDialog()
        }

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

        btnSkipBack.setOnClickListener {
            controller?.let { ctrl ->
                val newPos = (ctrl.currentPosition - SKIP_INTERVAL_MS).coerceAtLeast(0L)
                ctrl.seekTo(newPos)
            }
        }
        btnSkipForward.setOnClickListener {
            controller?.let { ctrl ->
                val duration = ctrl.duration.takeIf { it > 0 } ?: Long.MAX_VALUE
                val newPos = (ctrl.currentPosition + SKIP_INTERVAL_MS).coerceAtMost(duration)
                ctrl.seekTo(newPos)
            }
        }

        btnOffsetMinus.setOnClickListener {
            adjustGlobalOffset(-OFFSET_STEP_MS)
        }
        btnOffsetPlus.setOnClickListener {
            adjustGlobalOffset(OFFSET_STEP_MS)
        }
        updateLyricsOffsetText()

        lifecycleScope.launch {
            loadLyrics()
        }

        lifecycleScope.launch {
            while (true) {
                controller?.let { ctrl ->
                    lyricsView.updateTime(ctrl.currentPosition + lyricsOffsetMs)
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
                currentLyricsRaw = it
                val lines = LrcParser.parse(it)
                lyricsView.setLyrics(lines)
            }
        }
    }

    private fun adjustGlobalOffset(deltaMs: Long) {
        lyricsOffsetMs = (lyricsOffsetMs + deltaMs).coerceIn(OFFSET_MIN_MS, OFFSET_MAX_MS)
        updateLyricsOffsetText()
    }

    private fun updateLyricsOffsetText() {
        textLyricsOffset.text = "Offset: ${lyricsOffsetMs} ms"
    }

    private fun showLyricsImportEditDialog() {
        val editText = EditText(this)
        editText.setText(currentLyricsRaw ?: "")
        AlertDialog.Builder(this)
            .setTitle("Import or Edit Lyrics")
            .setMessage("Paste LRC or plain lyrics below:")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val input = editText.text.toString()
                currentLyricsRaw = input
                val lines = LrcParser.parse(input)
                lyricsView.setLyrics(lines)
            }
            .setNegativeButton("Cancel", null)
            .show()
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