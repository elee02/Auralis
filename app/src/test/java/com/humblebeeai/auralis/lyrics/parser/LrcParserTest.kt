package com.humblebeeai.auralis.lyrics.parser

import org.junit.Test
import org.junit.Assert.*

class LrcParserTest {

    @Test
    fun testParseLrcFormat() {
        val lrcContent = """
            [ti:Song Title]
            [ar:Artist Name]
            [al:Album Name]
            [00:00.00]This is the first line
            [00:05.20]This is the second line
            [00:10.50]This is the third line
        """.trimIndent()
        val lyricsResult = LrcParser.parse(lrcContent)
        assertEquals(3, lyricsResult.size)
        assertEquals(0, lyricsResult[0].timeMs)
        assertEquals("This is the first line", lyricsResult[0].text)
        assertEquals(5200, lyricsResult[1].timeMs)
        assertEquals("This is the second line", lyricsResult[1].text)
        assertEquals(10500, lyricsResult[2].timeMs)
        assertEquals("This is the third line", lyricsResult[2].text)
    }

    @Test
    fun testEmptyInput() {
        val result = LrcParser.parse("")
        assertTrue(result.isEmpty())
    }
}