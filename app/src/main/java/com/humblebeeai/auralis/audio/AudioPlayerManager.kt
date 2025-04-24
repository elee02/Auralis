package com.humblebeeai.auralis.audio

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

object AudioPlayerManager {
    private var exoPlayer: ExoPlayer? = null

    fun initialize(context: Context) {
        exoPlayer = ExoPlayer.Builder(context).build()
    }

    fun play(context: Context, uri: Uri) {
        if (exoPlayer == null) initialize(context)
        exoPlayer?.setMediaItem(MediaItem.fromUri(uri))
        exoPlayer?.prepare()
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }

    /** Returns current playback position in milliseconds, or null if uninitialized. */
    fun getCurrentPosition(): Long? = exoPlayer?.currentPosition

    /** Returns true if playback is ongoing. */
    fun isPlaying(): Boolean = exoPlayer?.isPlaying ?: false

    /** Returns the current audio session ID, or 0 if uninitialized. */
    fun getAudioSessionId(): Int = exoPlayer?.audioSessionId ?: 0

    /**
     * Returns the ExoPlayer instance, initializing if needed.
     */
    fun getPlayer(context: Context): ExoPlayer {
        if (exoPlayer == null) initialize(context)
        return exoPlayer!!
    }
}