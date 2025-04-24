package com.humblebeeai.auralis.lyrics.parser

import org.junit.Assert.assertEquals
import org.junit.Test

class LrcParserTest {
    private val sampleLrc = """
        [00:12.00]First line
        [00:05.50]Second line
        [00:20.100]Third line
    """.trimIndent()

    @Test
    fun testParseAndSort() {
        val lines = LrcParser.parse(sampleLrc)
        assertEquals(3, lines.size)
        assertEquals(5500, lines[0].timeMs)
        assertEquals("Second line", lines[0].text)
        assertEquals(12000, lines[1].timeMs)
        assertEquals("First line", lines[1].text)
        assertEquals(20100, lines[2].timeMs)
        assertEquals("Third line", lines[2].text)
    }

    @Test
    fun testEmptyContent() {
        val lines = LrcParser.parse("")
        assertEquals(0, lines.size)
    }
}