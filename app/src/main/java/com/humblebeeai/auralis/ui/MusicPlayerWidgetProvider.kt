package com.humblebeeai.auralis.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.humblebeeai.auralis.R
import androidx.media3.session.SessionToken
import androidx.media3.session.MediaController
import androidx.media3.common.MediaItem
import com.humblebeeai.auralis.audio.MusicService

class MusicPlayerWidgetProvider : AppWidgetProvider() {
    companion object {
        private const val ACTION_PLAY_PAUSE = "com.humblebeeai.auralis.ACTION_PLAY_PAUSE"
        private const val ACTION_NEXT = "com.humblebeeai.auralis.ACTION_NEXT"
        private const val ACTION_PREV = "com.humblebeeai.auralis.ACTION_PREV"

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_music_player)
            // get playback state and metadata via MediaController
            val token = SessionToken(context, ComponentName(context, MusicService::class.java))
            val controller = MediaController.Builder(context, token).build()
            val mediaItem: MediaItem? = controller.currentMediaItem
            val title = mediaItem?.mediaMetadata?.title ?: ""
            val artist = mediaItem?.mediaMetadata?.artist ?: ""
            views.setTextViewText(R.id.textWidgetTitle, title)
            views.setTextViewText(R.id.textWidgetArtist, artist)
            val icon = if (controller.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            views.setImageViewResource(R.id.btnWidgetPlayPause, icon)
            // set pending intents
            val pkg = context.packageName
            val playPauseIntent = Intent(context, MusicPlayerWidgetProvider::class.java).apply { action = ACTION_PLAY_PAUSE }
            val nextIntent = Intent(context, MusicPlayerWidgetProvider::class.java).apply { action = ACTION_NEXT }
            val prevIntent = Intent(context, MusicPlayerWidgetProvider::class.java).apply { action = ACTION_PREV }
            views.setOnClickPendingIntent(R.id.btnWidgetPlayPause,
                PendingIntent.getBroadcast(context, 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            views.setOnClickPendingIntent(R.id.btnWidgetNext,
                PendingIntent.getBroadcast(context, 1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            views.setOnClickPendingIntent(R.id.btnWidgetPrev,
                PendingIntent.getBroadcast(context, 2, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id ->
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // control playback via MediaController
        val token = SessionToken(context, ComponentName(context, MusicService::class.java))
        val controller = MediaController.Builder(context, token).build()
        when (intent.action) {
            ACTION_PLAY_PAUSE -> if (controller.isPlaying) controller.pause() else controller.play()
            ACTION_NEXT -> controller.seekToNextMediaItem()
            ACTION_PREV -> controller.seekToPreviousMediaItem()
        }
        // refresh all widgets
        val manager = AppWidgetManager.getInstance(context)
        val cn = ComponentName(context, MusicPlayerWidgetProvider::class.java)
        manager.getAppWidgetIds(cn).forEach { id ->
            updateWidget(context, manager, id)
        }
    }
}