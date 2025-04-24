package com.humblebeeai.auralis.lyrics.parser

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaMetadata

object EmbeddedLyricsExtractor {
    /**
     * Extracts embedded lyrics from the given audio URI, if available.
     */
    fun extract(context: Context, uriString: String): String? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, android.net.Uri.parse(uriString))
            retriever.embeddedPicture // use to force metadata load
            retriever.extractMetadata(MediaMetadata.METADATA_KEY_LYRICS)
        } catch (e: Exception) {
            null
        } finally {
            retriever.release()
        }
    }
}