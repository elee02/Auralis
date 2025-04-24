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

class NowPlayingActivity : AppCompatActivity() {
    private lateinit var lyricsView: SynchronizedLyricsView
    private lateinit var titleText: TextView
    private lateinit var artistText: TextView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnPrev: ImageButton

    private var songs: List<Song> = emptyList()
    private var currentIndex = 0
    private lateinit var mediaController: MediaController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_now_playing)

        titleText = findViewById(R.id.textNowTitle)
        artistText = findViewById(R.id.textNowArtist)
        lyricsView = findViewById(R.id.lyricsView)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnNext = findViewById(R.id.btnNext)
        btnPrev = findViewById(R.id.btnPrev)

        songs = intent.getParcelableArrayListExtra("songs") ?: emptyList()
        currentIndex = intent.getIntExtra("currentIndex", 0)
        // Build MediaController bound to MusicService
        val token = SessionToken(this, ComponentName(this, MusicService::class.java))
        mediaController = MediaController.Builder(this, token).build().also { ctrl ->
            // listen for state changes to update UI
            ctrl.addListener(object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    updateSongInfo()
                }
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    btnPlayPause.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
                }
            })
        }
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
        mediaController.setMediaItems(mediaItems, currentIndex, 0L)
        mediaController.prepare()
        mediaController.play()

        btnPlayPause.setOnClickListener {
            if (mediaController.isPlaying) {
                mediaController.pause()
            } else {
                mediaController.play()
            }
        }
        btnNext.setOnClickListener {
            mediaController.seekToNextMediaItem()
        }
        btnPrev.setOnClickListener {
            mediaController.seekToPreviousMediaItem()
        }

        lifecycleScope.launch {
            loadLyrics()
        }

        lifecycleScope.launch {
            while (true) {
                val position = mediaController.currentPosition
                lyricsView.updateTime(position)
                delay(200)
            }
        }
    }

    private fun updateSongInfo() {
        val idx = mediaController.currentMediaItemIndex
        if (idx in songs.indices) {
            val song = songs[idx]
            titleText.text = song.title
            artistText.text = song.artist ?: ""
        }
    }

    private suspend fun loadLyrics() {
        // get current song from mediaController
        val idx = mediaController.currentMediaItemIndex
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
        val dao = AppDatabase.getInstance(this).playlistDao()
        lifecycleScope.launch {
            val playlists = dao.getPlaylistsWithSongs().map { it.playlist }
            val names = playlists.map { it.name }.toMutableList().apply { add("Create New Playlist") }
            withContext(Dispatchers.Main) {
                AlertDialog.Builder(this@NowPlayingActivity)
                    .setTitle("Add to Playlist")
                    .setItems(names.toTypedArray()) { _, which ->
                        if (which < playlists.size) {
                            val playlist = playlists[which]
                            // get current song from mediaController
                            val idx2 = mediaController.currentMediaItemIndex
                            if (idx2 !in songs.indices) return@setItems
                            val song = songs[idx2]
                            lifecycleScope.launch {
                                dao.addSongToPlaylist(
                                    PlaylistSongCrossRef(playlist.id, song.id)
                                )
                                Toast.makeText(
                                    this@NowPlayingActivity,
                                    "Added to ${playlist.name}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            // Create new
                            val input = EditText(this@NowPlayingActivity)
                            AlertDialog.Builder(this@NowPlayingActivity)
                                .setTitle("New Playlist")
                                .setView(input)
                                .setPositiveButton("Create") { _, _ ->
                                    val name = input.text.toString().trim()
                                    if (name.isNotEmpty()) {
                                        lifecycleScope.launch {
                                            val id = dao.insertPlaylist(Playlist(name = name))
                                            val song = songs[mediaController.currentMediaItemIndex]
                                            dao.addSongToPlaylist(PlaylistSongCrossRef(id, song.id))
                                            Toast.makeText(
                                                this@NowPlayingActivity,
                                                "Playlist '$name' created and song added",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                                .setNegativeButton("Cancel", null)
                                .show()
                        }
                    }
                    .show()
            }
        }
    }
}