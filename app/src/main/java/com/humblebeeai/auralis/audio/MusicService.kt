package com.humblebeeai.auralis.audio

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.Player
import com.humblebeeai.auralis.data.AppDatabase
import com.humblebeeai.auralis.data.entity.Song
import kotlinx.coroutines.runBlocking
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaSession
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import android.app.PendingIntent
import android.content.Intent
import com.humblebeeai.auralis.ui.NowPlayingActivity

class MusicService : MediaSessionService() {
    private lateinit var session: MediaSession
    private lateinit var songs: List<Song>

    override fun onCreate() {
        super.onCreate()
        // initialize ExoPlayer via your AudioPlayerManager
        val player = AudioPlayerManager.getPlayer(this)
        // preload song list
        songs = runBlocking { AppDatabase.getInstance(applicationContext).songDao().getAllSongs() }
        // create a PendingIntent so notifications / session clients open the Now Playing screen
        val activityIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, NowPlayingActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // build Media3 session
        session = MediaSession.Builder(this, player)
            .setSessionActivity(activityIntent)
            .build()
        // start foreground playback notification
        MusicNotificationManager(this, session)
    }

    override fun onDestroy() {
        super.onDestroy()
        session.release()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return session
    }
}