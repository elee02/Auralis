package com.humblebeeai.auralis.lyrics.renderer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.humblebeeai.auralis.R
import com.humblebeeai.auralis.lyrics.parser.LrcLine

/**
 * Custom view to display synchronized lyrics and highlight current line based on playback time.
 */
class SynchronizedLyricsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ScrollView(context, attrs) {
    private val container = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    private var lines: List<LrcLine> = emptyList()
    private var currentIndex = -1

    init {
        addView(container)
    }

    /**
     * Sets the parsed lyrics lines to display.
     */
    fun setLyrics(lines: List<LrcLine>) {
        this.lines = lines
        container.removeAllViews()
        lines.forEach { line ->
            val tv = TextView(context).apply {
                text = line.text
                setTextColor(ContextCompat.getColor(context, R.color.lyrics_inactive))
                setPadding(4, 4, 4, 4)
            }
            container.addView(tv)
        }
        currentIndex = -1
    }

    /**
     * Updates the highlighted line based on the current playback time.
     */
    fun updateTime(timeMs: Long) {
        val newIndex = lines.indexOfLast { it.timeMs <= timeMs }
        if (newIndex != currentIndex && newIndex in lines.indices) {
            // un-highlight previous
            if (currentIndex in 0 until container.childCount) {
                val prev = container.getChildAt(currentIndex) as TextView
                prev.setTextColor(ContextCompat.getColor(context, R.color.lyrics_inactive))
            }
            // highlight new
            val curr = container.getChildAt(newIndex) as TextView
            curr.setTextColor(ContextCompat.getColor(context, R.color.lyrics_active))
            // ensure current line is visible
            scrollTo(0, curr.top)
            currentIndex = newIndex
        }
    }
}