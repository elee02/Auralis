package com.humblebeeai.auralis.lyrics.fetcher

import com.humblebeeai.auralis.data.entity.Song
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Response model for lyrics API.
 */
data class LyricsResponse(val lyrics: String?)

/**
 * Retrofit API definition for lyrics fetching.
 */
interface LyricsApi {
    @GET("/v1/{artist}/{title}")
    suspend fun getLyrics(
        @Path("artist") artist: String,
        @Path("title") title: String
    ): LyricsResponse
}

/**
 * Fetches lyrics from an online API.
 */
object LyricsFetcher {
    private val api: LyricsApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.lyrics.ovh")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LyricsApi::class.java)
    }

    /**
     * Attempts to fetch lyrics for the given song, returns lyrics text or null.
     */
    suspend fun fetchOnline(song: Song): String? {
        return try {
            val response = api.getLyrics(song.artist ?: "", song.title)
            response.lyrics
        } catch (e: Exception) {
            null
        }
    }
}