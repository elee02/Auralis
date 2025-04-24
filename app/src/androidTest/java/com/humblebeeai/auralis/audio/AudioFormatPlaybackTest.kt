package com.humblebeeai.auralis.audio

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.exoplayer2.ExoPlaybackException
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test to check playback support for common audio formats.
 * Place sample files in device storage or use test URIs.
 */
@RunWith(AndroidJUnit4::class)
class AudioFormatPlaybackTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    // Replace these URIs with actual test files on your device/emulator
    private val testUris = mapOf(
        "MP3" to "content://media/external/audio/media/1", // update with real URIs
        "AAC" to "content://media/external/audio/media/2",
        "FLAC" to "content://media/external/audio/media/3",
        "WAV" to "content://media/external/audio/media/4",
        "OGG" to "content://media/external/audio/media/5"
    )

    @Test
    fun testAudioFormatPlayback() {
        for ((format, uriString) in testUris) {
            try {
                AudioPlayerManager.play(context, Uri.parse(uriString))
                Thread.sleep(1000) // Let it try to play for a second
                assertTrue("Playback should start for $format", AudioPlayerManager.isPlaying())
                AudioPlayerManager.pause()
            } catch (e: ExoPlaybackException) {
                assertTrue("Playback failed for $format: ${e.message}", false)
            } catch (e: Exception) {
                assertTrue("Unexpected error for $format: ${e.message}", false)
            }
        }
    }
}
