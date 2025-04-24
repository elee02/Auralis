package com.humblebeeai.auralis.lyrics.parser

/**
 * Represents a single line in an LRC file with timing and text.
 */
data class LrcLine(val timeMs: Long, val text: String)

object LrcParser {
    /**
     * Parses LRC formatted lyrics into a list of LrcLine sorted by time.
     */
    fun parse(lrcContent: String): List<LrcLine> {
        val lines = mutableListOf<LrcLine>()
        val pattern = Regex("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})]([^\\n]+)")
        lrcContent.lines().forEach { line ->
            pattern.findAll(line).forEach { match ->
                val min = match.groupValues[1].toLong()
                val sec = match.groupValues[2].toLong()
                val ms = match.groupValues[3].let { if (it.length == 2) it.toLong() * 10 else it.toLong() }
                val time = min * 60_000 + sec * 1_000 + ms
                val text = match.groupValues[4]
                lines.add(LrcLine(time, text))
            }
        }
        return lines.sortedBy { it.timeMs }
    }
}