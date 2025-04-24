package com.humblebeeai.auralis.ui

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.humblebeeai.auralis.R
import com.humblebeeai.auralis.audio.AudioPlayerManager
import android.media.audiofx.Equalizer

class EqualizerActivity : AppCompatActivity() {
    private lateinit var equalizer: Equalizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equalizer)

        // init equalizer for current audio session
        val sessionId = AudioPlayerManager.getAudioSessionId()
        equalizer = Equalizer(0, sessionId).apply { enabled = true }

        // setup presets spinner
        val spinner = findViewById<Spinner>(R.id.spinnerPresets)
        val presetCount = equalizer.numberOfPresets
        val names = Array(presetCount.toInt()) { i -> equalizer.getPresetName(i.toShort()) }
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.setSelection(0)
        spinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                equalizer.usePreset(position.toShort())
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        })

        // add band control seekbars
        val container = findViewById<LinearLayout>(R.id.bandsContainer)
        val bandCount = equalizer.numberOfBands
        val range = equalizer.bandLevelRange  // short array of [min, max]
        for (i in 0 until bandCount) {
            val bandIndex = i.toShort()
            val freq = equalizer.getCenterFreq(bandIndex) / 1000 // in Hz
            val tv = TextView(this).apply { text = "${freq} Hz" }
            val sb = SeekBar(this).apply {
                max = (range[1] - range[0]).toInt()
                progress = (equalizer.getBandLevel(bandIndex) - range[0]).toInt()
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        equalizer.setBandLevel(bandIndex, (progress + range[0]).toShort())
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })
            }
            container.addView(tv)
            container.addView(sb)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        equalizer.release()
    }
}