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
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

class EqualizerActivity : AppCompatActivity() {
    private lateinit var equalizer: Equalizer
    private lateinit var textBandCount: TextView
    private lateinit var textPresetName: TextView
    private lateinit var btnResetEq: Button
    private lateinit var btnSavePreset: Button
    private lateinit var prefs: SharedPreferences
    private var currentCustomPresetName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equalizer)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        textBandCount = findViewById(R.id.textBandCount)
        textPresetName = findViewById(R.id.textPresetName)
        btnResetEq = findViewById(R.id.btnResetEq)
        btnSavePreset = findViewById(R.id.btnSavePreset)

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
                updateSeekBarsToCurrentBandLevels()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        })

        // add band control seekbars
        val container = findViewById<LinearLayout>(R.id.bandsContainer)
        val bandCount = equalizer.numberOfBands.toInt()
        textBandCount.text = "Band count: $bandCount"
        if (bandCount < 10) {
            textBandCount.append(" (10-band not available on this device)")
        }
        val range = equalizer.bandLevelRange  // short array of [min, max]
        val minLevel = range[0].toInt()
        val maxLevel = range[1].toInt()
        
        for (i in 0 until bandCount) {
            val freq = equalizer.getCenterFreq(i.toShort()) / 1000 // in Hz
            val tv = TextView(this).apply { text = "${freq} Hz" }
            val sb = SeekBar(this).apply {
                max = maxLevel - minLevel
                progress = equalizer.getBandLevel(i.toShort()).toInt() - minLevel
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        equalizer.setBandLevel(i.toShort(), (progress + minLevel).toShort())
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })
            }
            container.addView(tv)
            container.addView(sb)
        }

        // Restore band levels if available
        restoreBandLevels(equalizer, bandCount)

        btnResetEq.setOnClickListener {
            for (i in 0 until bandCount) {
                equalizer.setBandLevel(i.toShort(), 0)
            }
            updateSeekBarsToCurrentBandLevels()
            saveBandLevels(equalizer, bandCount)
            updatePresetNameDisplay("Flat")
        }
        btnSavePreset.setOnClickListener {
            showSavePresetDialog(equalizer, bandCount)
        }
        btnSavePreset.setOnLongClickListener {
            showLoadPresetDialog(equalizer, equalizer.numberOfBands.toInt())
            true
        }
    }

    private fun updateSeekBarsToCurrentBandLevels() {
        val container = findViewById<LinearLayout>(R.id.bandsContainer)
        val bandCount = equalizer.numberOfBands.toInt()
        val range = equalizer.bandLevelRange
        val minLevel = range[0].toInt()
        
        // Only update the SeekBars (every second view in the container)
        for (i in 0 until bandCount) {
            val sbIndex = i * 2 + 1 // Skip text views
            if (sbIndex < container.childCount) {
                val view = container.getChildAt(sbIndex)
                if (view is SeekBar) {
                    view.progress = equalizer.getBandLevel(i.toShort()).toInt() - minLevel
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        equalizer.release()
    }

    private fun updatePresetNameDisplay(name: String) {
        textPresetName.text = "Current preset: $name"
        currentCustomPresetName = name
    }

    private fun saveBandLevels(eq: Equalizer, bandCount: Int) {
        val levels = (0 until bandCount).map { eq.getBandLevel(it.toShort()).toInt() }
        prefs.edit().putString("eq_bands", levels.joinToString(",")).apply()
        currentCustomPresetName?.let { prefs.edit().putString("eq_preset", it).apply() }
    }

    private fun restoreBandLevels(eq: Equalizer, bandCount: Int) {
        val saved = prefs.getString("eq_bands", null)
        if (saved != null) {
            val levels = saved.split(",").mapNotNull { it.toIntOrNull() }
            for (i in 0 until minOf(bandCount, levels.size)) {
                eq.setBandLevel(i.toShort(), levels[i].toShort())
            }
            updatePresetNameDisplay(prefs.getString("eq_preset", "Custom") ?: "Custom")
        } else {
            updatePresetNameDisplay("Flat")
        }
    }

    private fun showSavePresetDialog(eq: Equalizer, bandCount: Int) {
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Save Custom Preset")
            .setMessage("Enter a name for your preset:")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val name = input.text.toString().ifBlank { "Custom" }
                val levels = (0 until bandCount).map { eq.getBandLevel(it.toShort()).toInt() }
                val allPresets = prefs.getStringSet("custom_presets", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
                allPresets.add(name)
                prefs.edit().putStringSet("custom_presets", allPresets).apply()
                prefs.edit().putString("custom_$name", levels.joinToString(",")).apply()
                updatePresetNameDisplay(name)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLoadPresetDialog(eq: Equalizer, bandCount: Int) {
        val allPresets = prefs.getStringSet("custom_presets", setOf())?.toList() ?: listOf()
        if (allPresets.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Custom Presets")
                .setMessage("No custom presets saved.")
                .setPositiveButton("OK", null)
                .show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Load Custom Preset")
            .setItems(allPresets.toTypedArray()) { _, which ->
                val name = allPresets[which]
                val levelsStr = prefs.getString("custom_$name", null)
                if (levelsStr != null) {
                    val levels = levelsStr.split(",").mapNotNull { it.toIntOrNull() }
                    for (i in 0 until minOf(bandCount, levels.size)) {
                        eq.setBandLevel(i.toShort(), levels[i].toShort())
                    }
                    updateSeekBarsToCurrentBandLevels()
                    saveBandLevels(eq, bandCount)
                    updatePresetNameDisplay(name)
                }
            }
            .setNegativeButton("Delete Preset") { _, _ ->
                showDeletePresetDialog()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun showDeletePresetDialog() {
        val allPresets = prefs.getStringSet("custom_presets", setOf())?.toMutableList() ?: mutableListOf()
        if (allPresets.isEmpty()) return
        AlertDialog.Builder(this)
            .setTitle("Delete Custom Preset")
            .setItems(allPresets.toTypedArray()) { _, which ->
                val name = allPresets[which]
                allPresets.removeAt(which)
                prefs.edit().putStringSet("custom_presets", allPresets.toSet()).remove("custom_$name").apply()
                if (currentCustomPresetName == name) updatePresetNameDisplay("Flat")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}