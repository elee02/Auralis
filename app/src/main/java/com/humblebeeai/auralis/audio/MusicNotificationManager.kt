package com.humblebeeai.auralis.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.humblebeeai.auralis.ui.NowPlayingActivity

class MusicNotificationManager(
    private val context: Context,
    session: MediaSession
) {
    companion object {
        private const val CHANNEL_ID = "auralis_playback_channel"
        private const val NOTIFICATION_ID = 1
    }

    private val notificationManager = PlayerNotificationManager.Builder(
        context,
        NOTIFICATION_ID,
        CHANNEL_ID
    )
        .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: Player): CharSequence =
                player.mediaMetadata.title ?: ""

            override fun createCurrentContentIntent(player: Player): PendingIntent? {
                val intent = Intent(context, NowPlayingActivity::class.java)
                return PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

            override fun getCurrentContentText(player: Player): CharSequence? =
                player.mediaMetadata.artist

            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap? {
                // Optionally fetch and provide album art asynchronously
                return null
            }
        })
        .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
            override fun onNotificationCancelled(
                notificationId: Int,
                dismissedByUser: Boolean
            ) {
                (context as MediaSessionService).stopForeground(true)
            }

            override fun onNotificationPosted(
                notificationId: Int,
                notification: Notification,
                ongoing: Boolean
            ) {
                if (ongoing) {
                    (context as MediaSessionService).startForeground(
                        notificationId,
                        notification
                    )
                } else {
                    (context as MediaSessionService).stopForeground(false)
                }
            }
        })
        .build().apply {
            setPlayer(session.player)
        }

    init {
        createChannel()
    }

    private fun createChannel() {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Playback",
            NotificationManager.IMPORTANCE_LOW
        )
        nm.createNotificationChannel(channel)
    }
}