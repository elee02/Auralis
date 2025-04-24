package com.humblebeeai.auralis.ui

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.humblebeeai.auralis.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import android.content.ComponentName
import android.view.KeyEvent
import com.humblebeeai.auralis.audio.MusicService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

class SleepTimerActivity : AppCompatActivity() {
    private lateinit var controllerFuture: ListenableFuture<MediaController>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep_timer)

        // prepare MediaController to control playback
        val token = SessionToken(this, ComponentName(this, MusicService::class.java))
        controllerFuture = MediaController.Builder(this, token).buildAsync()

        val rg = findViewById<RadioGroup>(R.id.rgDurations)
        val btnStart = findViewById<Button>(R.id.buttonStartTimer)

        btnStart.setOnClickListener {
            val selectedId = rg.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(this, "Select a duration", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val minutes = when (findViewById<RadioButton>(selectedId).tag.toString()) {
                "15" -> 15
                "30" -> 30
                "60" -> 60
                else -> 0
            }
            Toast.makeText(this, "Sleep timer set for $minutes minutes", Toast.LENGTH_SHORT).show()
            
            lifecycleScope.launch {
                delay(minutes * 60_000L)
                
                if (controllerFuture.isDone) {
                    val controller = controllerFuture.get()
                    controller.pause()
                    Toast.makeText(this@SleepTimerActivity, "Sleep timer ended", Toast.LENGTH_SHORT).show()
                }
                
                finish()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        MediaController.releaseFuture(controllerFuture)
    }
}